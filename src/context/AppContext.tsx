import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  Generator,
  PatternAnalyzer,
  kouDistribution,
  resolveTargetMonth,
} from '../engine/Lotto7Engine';
import type { AppLanguage, Strings } from '../i18n/localizedStrings';
import {
  formatHistoryLine,
  formatLookupTotal,
  formatPageInfo,
  getStrings,
} from '../i18n/localizedStrings';
import type {
  AnalysisSummary,
  Draw,
  GeneratedCombo,
  GenerationHistory,
  WinningNumber,
} from '../models/types';
import * as repo from '../data/repository';
import { parseRoundNumber } from '../data/officialWinningFetcher';

interface LottoState {
  isLoading: boolean;
  summary: AnalysisSummary | null;
  combos: GeneratedCombo[];
  selectedMonth: number | null;
  errorMessage: string | null;
  savedWinningCount: number;
}

interface WinningState {
  items: WinningNumber[];
  totalCount: number;
  isSyncing: boolean;
  showDialog: boolean;
  editing: WinningNumber | null;
  roundInput: string;
  dateInput: string;
  numbersInput: string;
  errorMessage: string | null;
  confirmDelete: WinningNumber | null;
}

interface LookupState {
  isLoading: boolean;
  items: Draw[];
  searchQuery: string;
  currentPage: number;
  totalPages: number;
  totalCount: number;
}

interface HistoryState {
  items: GenerationHistory[];
  currentPage: number;
  totalPages: number;
  totalCount: number;
  isLoading: boolean;
}

interface SettingsState {
  isImporting: boolean;
  importMessage: string | null;
  importError: boolean;
}

interface AppContextValue {
  language: AppLanguage;
  strings: Strings;
  lottoState: LottoState;
  winningState: WinningState;
  lookupState: LookupState;
  historyState: HistoryState;
  settingsState: SettingsState;
  setLanguage: (lang: AppLanguage) => void;
  regenerate: () => void;
  setMonth: (month: number) => void;
  refreshWinningNumbers: () => void;
  openWinningDialog: (entity?: WinningNumber) => void;
  closeWinningDialog: () => void;
  updateRoundInput: (v: string) => void;
  updateDateInput: (v: string) => void;
  updateNumbersInput: (v: string) => void;
  saveWinning: () => Promise<void>;
  requestDeleteWinning: (entity: WinningNumber) => void;
  cancelDeleteWinning: () => void;
  confirmDeleteWinning: () => Promise<void>;
  loadHistoryPage: (page: number) => void;
  setLookupSearch: (query: string) => void;
  loadLookupPage: (page: number) => void;
  autoRegisterFromExcel: () => void;
  fetchFromOfficialSite: () => void;
  formatHistoryLine: (createdAt: number, numbers: number[]) => string;
  formatPageInfo: (current: number, total: number) => string;
  formatLookupTotal: (count: number) => string;
}

const AppContext = createContext<AppContextValue | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<AppLanguage>('ja');
  const [draws, setDraws] = useState<Draw[]>([]);
  const [analyzer, setAnalyzer] = useState<PatternAnalyzer | null>(null);
  const [savedWinning, setSavedWinning] = useState<number[][]>([]);

  const [lottoState, setLottoState] = useState<LottoState>({
    isLoading: true,
    summary: null,
    combos: [],
    selectedMonth: null,
    errorMessage: null,
    savedWinningCount: 0,
  });

  const [winningState, setWinningState] = useState<WinningState>({
    items: [],
    totalCount: 0,
    isSyncing: false,
    showDialog: false,
    editing: null,
    roundInput: '',
    dateInput: '',
    numbersInput: '',
    errorMessage: null,
    confirmDelete: null,
  });

  const [lookupState, setLookupState] = useState<LookupState>({
    isLoading: false,
    items: [],
    searchQuery: '',
    currentPage: 0,
    totalPages: 1,
    totalCount: 0,
  });

  const [historyState, setHistoryState] = useState<HistoryState>({
    items: [],
    currentPage: 0,
    totalPages: 1,
    totalCount: 0,
    isLoading: false,
  });

  const [settingsState, setSettingsState] = useState<SettingsState>({
    isImporting: false,
    importMessage: null,
    importError: false,
  });

  const strings = useMemo(() => getStrings(language), [language]);

  const sortWinning = useCallback((items: WinningNumber[]) => {
    return [...items].sort((a, b) => {
      const ra = parseRoundNumber(a.roundLabel);
      const rb = parseRoundNumber(b.roundLabel);
      if (rb !== ra) return rb - ra;
      return b.updatedAt - a.updatedAt;
    });
  }, []);

  const refreshLookupPage = useCallback(
    (page: number, query?: string) => {
      if (draws.length === 0) return;
      const q = (query ?? lookupState.searchQuery).trim();
      const filtered = q
        ? [...draws].reverse().filter(
            (d) =>
              d.round.includes(q) ||
              d.date.includes(q) ||
              d.nums.some((n) => String(n) === q)
          )
        : [...draws].reverse();
      const pageSize = 10;
      const totalPages = filtered.length === 0 ? 1 : Math.ceil(filtered.length / pageSize);
      const safePage = Math.min(Math.max(page, 0), totalPages - 1);
      const slice = filtered.slice(safePage * pageSize, safePage * pageSize + pageSize);
      setLookupState({
        isLoading: false,
        items: slice,
        searchQuery: q,
        currentPage: safePage,
        totalPages,
        totalCount: draws.length,
      });
    },
    [draws, lookupState.searchQuery]
  );

  const loadWinning = useCallback(async () => {
    const items = sortWinning(await repo.getWinningNumbers());
    const lists = repo.winningNumberLists(items);
    setSavedWinning(lists);
    setWinningState((prev) => ({
      ...prev,
      items,
      totalCount: items.length,
      isSyncing: false,
    }));
    setLottoState((prev) => ({ ...prev, savedWinningCount: lists.length }));
  }, [sortWinning]);

  const showImportResult = useCallback(
    (result: { added: number; skipped: number }) => {
      const s = getStrings(language);
      let msg: string;
      if (result.added === 0 && result.skipped === 0) {
        msg = s.importNone;
      } else if (result.added === 0) {
        msg = s.importAlreadyUpToDate.replace('%d', String(result.skipped));
      } else {
        msg = s.importSuccess
          .replace('%d', String(result.added))
          .replace('%d', String(result.skipped));
      }
      setSettingsState({
        isImporting: false,
        importMessage: msg,
        importError: false,
      });
      loadWinning();
    },
    [language, loadWinning]
  );

  useEffect(() => {
    (async () => {
      try {
        const lang = await repo.getLanguage();
        setLanguageState(lang);
        const loaded = repo.loadDraws();
        setDraws(loaded);
        const pat = new PatternAnalyzer(loaded);
        setAnalyzer(pat);
        await repo.syncWinningFromDraws(loaded);
        const items = sortWinning(await repo.getWinningNumbers());
        const lists = repo.winningNumberLists(items);
        setSavedWinning(lists);
        const month = resolveTargetMonth(loaded);
        setWinningState((prev) => ({
          ...prev,
          items,
          totalCount: items.length,
          isSyncing: false,
        }));
        setLottoState({
          isLoading: false,
          summary: pat.buildSummary(month),
          combos: [],
          selectedMonth: month,
          errorMessage: null,
          savedWinningCount: lists.length,
        });
        const pageSize = 10;
        const reversed = [...loaded].reverse();
        setLookupState({
          isLoading: false,
          items: reversed.slice(0, pageSize),
          searchQuery: '',
          currentPage: 0,
          totalPages: Math.ceil(loaded.length / pageSize),
          totalCount: loaded.length,
        });
      } catch (e) {
        setLottoState((prev) => ({
          ...prev,
          isLoading: false,
          errorMessage: e instanceof Error ? e.message : 'Error',
        }));
      }
    })();
  }, [sortWinning]);

  const setLanguage = useCallback(async (lang: AppLanguage) => {
    await repo.setLanguage(lang);
    setLanguageState(lang);
  }, []);

  const regenerate = useCallback(async () => {
    if (!analyzer || lottoState.isLoading) return;
    const month = lottoState.selectedMonth;
    const gen = new Generator(analyzer, savedWinning);
    const results = gen.generate(10, month);
    const combos: GeneratedCombo[] = results.map((nums, index) => ({
      index: index + 1,
      numbers: nums,
      sum: nums.reduce((a, b) => a + b, 0),
      oddCount: nums.filter((n) => n % 2 === 1).length,
      kouDistribution: kouDistribution(nums),
    }));
    for (const combo of combos) {
      await repo.saveGenerationHistory(combo.numbers);
    }
    setLottoState((prev) => ({
      ...prev,
      combos,
      summary: analyzer.buildSummary(month),
    }));
    const [items, total] = await repo.getHistoryPage(0);
    const totalPages = total === 0 ? 1 : Math.ceil(total / 10);
    setHistoryState({
      items,
      currentPage: 0,
      totalPages,
      totalCount: total,
      isLoading: false,
    });
  }, [analyzer, lottoState.isLoading, lottoState.selectedMonth, savedWinning]);

  const setMonth = useCallback(
    (month: number) => {
      if (!analyzer) return;
      const safeMonth = Math.min(12, Math.max(1, month));
      setLottoState((prev) => ({
        ...prev,
        selectedMonth: safeMonth,
        summary: analyzer.buildSummary(safeMonth),
      }));
    },
    [analyzer]
  );

  const refreshWinningNumbers = useCallback(async () => {
    if (draws.length === 0) return;
    setWinningState((prev) => ({ ...prev, isSyncing: true }));
    await repo.syncWinningFromDraws(draws);
    await loadWinning();
  }, [draws, loadWinning]);

  const openWinningDialog = useCallback((entity?: WinningNumber) => {
    setWinningState((prev) => ({
      ...prev,
      showDialog: true,
      editing: entity ?? null,
      roundInput: entity?.roundLabel ?? '',
      dateInput: entity?.drawDate ?? '',
      numbersInput: entity?.numbers?.replace(/,/g, ' ') ?? '',
      errorMessage: null,
    }));
  }, []);

  const closeWinningDialog = useCallback(() => {
    setWinningState((prev) => ({
      ...prev,
      showDialog: false,
      editing: null,
      errorMessage: null,
    }));
  }, []);

  const saveWinning = useCallback(async () => {
    const state = winningState;
    const nums = repo.parseNumbers(state.numbersInput);
    if (!nums) {
      setWinningState((prev) => ({ ...prev, errorMessage: 'invalid' }));
      return;
    }
    const formatted = repo.formatNumbers(nums);
    if (state.editing == null) {
      await repo.insertWinning({
        roundLabel: state.roundInput.trim(),
        drawDate: state.dateInput.trim(),
        numbers: formatted,
      });
    } else {
      await repo.updateWinning({
        ...state.editing,
        roundLabel: state.roundInput.trim(),
        drawDate: state.dateInput.trim(),
        numbers: formatted,
      });
    }
    closeWinningDialog();
    await loadWinning();
  }, [winningState, closeWinningDialog, loadWinning]);

  const confirmDeleteWinning = useCallback(async () => {
    const entity = winningState.confirmDelete;
    if (!entity) return;
    await repo.deleteWinning(entity);
    setWinningState((prev) => ({ ...prev, confirmDelete: null }));
    await loadWinning();
  }, [winningState.confirmDelete, loadWinning]);

  const loadHistoryPage = useCallback(async (page: number) => {
    setHistoryState((prev) => ({ ...prev, isLoading: true }));
    const [items, total] = await repo.getHistoryPage(page);
    const totalPages = total === 0 ? 1 : Math.ceil(total / 10);
    setHistoryState({
      items,
      currentPage: Math.min(Math.max(page, 0), totalPages - 1),
      totalPages,
      totalCount: total,
      isLoading: false,
    });
  }, []);

  const autoRegisterFromExcel = useCallback(async () => {
    const s = getStrings(language);
    if (draws.length === 0) {
      setSettingsState({
        isImporting: false,
        importMessage: s.dataNotReady,
        importError: true,
      });
      return;
    }
    setSettingsState({
      isImporting: true,
      importMessage: s.importingEmbedded,
      importError: false,
    });
    try {
      const result = await repo.syncWinningFromDraws(draws);
      showImportResult(result);
    } catch (e) {
      setSettingsState({
        isImporting: false,
        importMessage: e instanceof Error ? e.message : s.importFailed,
        importError: true,
      });
    }
  }, [draws, language, showImportResult]);

  const fetchFromOfficialSite = useCallback(async () => {
    const s = getStrings(language);
    setSettingsState({ isImporting: true, importMessage: null, importError: false });
    try {
      const result = await repo.importFromOfficialSite();
      showImportResult(result);
    } catch (e) {
      const msg =
        e instanceof Error && (e.message === 'fetch_failed' || e.message === 'parse_failed')
          ? s.importFailed
          : e instanceof Error
            ? e.message
            : s.importFailed;
      setSettingsState({ isImporting: false, importMessage: msg, importError: true });
    }
  }, [language, showImportResult]);

  const value: AppContextValue = {
    language,
    strings,
    lottoState,
    winningState,
    lookupState,
    historyState,
    settingsState,
    setLanguage,
    regenerate,
    setMonth,
    refreshWinningNumbers,
    openWinningDialog,
    closeWinningDialog,
    updateRoundInput: (v) => setWinningState((prev) => ({ ...prev, roundInput: v })),
    updateDateInput: (v) => setWinningState((prev) => ({ ...prev, dateInput: v })),
    updateNumbersInput: (v) => setWinningState((prev) => ({ ...prev, numbersInput: v })),
    saveWinning,
    requestDeleteWinning: (entity) =>
      setWinningState((prev) => ({ ...prev, confirmDelete: entity })),
    cancelDeleteWinning: () => setWinningState((prev) => ({ ...prev, confirmDelete: null })),
    confirmDeleteWinning,
    loadHistoryPage,
    setLookupSearch: (query) => {
      setLookupState((prev) => ({ ...prev, searchQuery: query, currentPage: 0 }));
      refreshLookupPage(0, query);
    },
    loadLookupPage: (page) => refreshLookupPage(page),
    autoRegisterFromExcel,
    fetchFromOfficialSite,
    formatHistoryLine: (createdAt, numbers) => formatHistoryLine(language, createdAt, numbers),
    formatPageInfo: (current, total) => formatPageInfo(strings.pageInfo, current, total),
    formatLookupTotal: (count) => formatLookupTotal(strings.lookupTotal, count),
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp(): AppContextValue {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}
