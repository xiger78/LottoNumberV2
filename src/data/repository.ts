import drawsData from '../../assets/draws.json';
import type { Draw, FetchedWinning, GenerationHistory, ImportResult, WinningNumber } from '../models/types';
import { fetchLatest, fetchMissingAfter, parseRoundNumber } from './officialWinningFetcher';
import * as storage from './storage';

export function loadDraws(): Draw[] {
  return drawsData as Draw[];
}

export function parseNumbers(raw: string): number[] | null {
  const nums = raw
    .split(/[,\s　\t\n]+/)
    .map((s) => s.trim())
    .filter((s) => s.length > 0)
    .map((s) => parseInt(s, 10))
    .filter((n) => !isNaN(n));
  const unique = [...new Set(nums)].sort((a, b) => a - b);
  if (unique.length !== 7 || unique.some((n) => n < 1 || n > 37)) return null;
  return unique;
}

export function formatNumbers(nums: number[]): string {
  return nums.map((n) => String(n).padStart(2, '0')).join(',');
}

export function winningNumberLists(entities: WinningNumber[]): number[][] {
  return entities.map((e) => parseNumbers(e.numbers)).filter((n): n is number[] => n !== null);
}

export async function getWinningNumbers(): Promise<WinningNumber[]> {
  return storage.getWinningNumbers();
}

export async function insertWinning(entity: Omit<WinningNumber, 'id' | 'updatedAt'>): Promise<WinningNumber> {
  const items = await storage.getWinningNumbers();
  const id = await storage.nextWinningId();
  const newItem: WinningNumber = { ...entity, id, updatedAt: Date.now() };
  items.push(newItem);
  await storage.saveWinningNumbers(items);
  return newItem;
}

export async function updateWinning(entity: WinningNumber): Promise<void> {
  const items = await storage.getWinningNumbers();
  const idx = items.findIndex((i) => i.id === entity.id);
  if (idx >= 0) {
    items[idx] = { ...entity, updatedAt: Date.now() };
    await storage.saveWinningNumbers(items);
  }
}

export async function deleteWinning(entity: WinningNumber): Promise<void> {
  const items = await storage.getWinningNumbers();
  await storage.saveWinningNumbers(items.filter((i) => i.id !== entity.id));
}

export async function saveGenerationHistory(numbers: number[]): Promise<void> {
  const items = await storage.getGenerationHistory();
  const id = await storage.nextHistoryId();
  items.unshift({ id, createdAt: Date.now(), numbers: numbers.join(',') });
  await storage.saveGenerationHistory(items);
}

export async function getHistoryPage(page: number, pageSize = 10): Promise<[GenerationHistory[], number]> {
  const all = await storage.getGenerationHistory();
  const total = all.length;
  const offset = page * pageSize;
  return [all.slice(offset, offset + pageSize), total];
}

async function existingRoundNumbers(): Promise<Set<number>> {
  const items = await storage.getWinningNumbers();
  return new Set(
    items.map((i) => parseRoundNumber(i.roundLabel)).filter((n) => n > 0)
  );
}

async function importFromDraws(draws: Draw[]): Promise<ImportResult> {
  const existing = await existingRoundNumbers();
  let added = 0;
  let skipped = 0;
  const items = await storage.getWinningNumbers();

  for (const draw of draws) {
    const roundNum = parseRoundNumber(draw.round);
    if (existing.has(roundNum)) {
      skipped++;
      continue;
    }
    const id = await storage.nextWinningId();
    items.push({
      id,
      roundLabel: draw.round,
      drawDate: draw.date,
      numbers: formatNumbers(draw.nums),
      updatedAt: Date.now(),
    });
    added++;
    if (roundNum > 0) existing.add(roundNum);
  }

  if (added > 0) await storage.saveWinningNumbers(items);
  return { added, skipped, message: '' };
}

async function importFetched(fetched: FetchedWinning[]): Promise<ImportResult> {
  const existing = await existingRoundNumbers();
  let added = 0;
  let skipped = 0;
  const items = await storage.getWinningNumbers();

  for (const item of fetched) {
    const roundNum = parseRoundNumber(item.roundLabel);
    if (existing.has(roundNum)) {
      skipped++;
      continue;
    }
    const id = await storage.nextWinningId();
    items.push({
      id,
      roundLabel: item.roundLabel,
      drawDate: item.drawDate,
      numbers: formatNumbers(item.numbers),
      updatedAt: Date.now(),
    });
    added++;
    if (roundNum > 0) existing.add(roundNum);
  }

  if (added > 0) await storage.saveWinningNumbers(items);
  return { added, skipped, message: '' };
}

export async function syncWinningFromDraws(draws: Draw[]): Promise<ImportResult> {
  const embedded = await importFromDraws(draws);
  const maxEmbedded = Math.max(...draws.map((d) => parseRoundNumber(d.round)), 0);
  let official: ImportResult = { added: 0, skipped: 0, message: '' };
  try {
    official = await importFetched(await fetchMissingAfter(maxEmbedded));
  } catch {
    // ignore network errors during sync
  }
  return {
    added: embedded.added + official.added,
    skipped: embedded.skipped + official.skipped,
    message: '',
  };
}

export async function importFromOfficialSite(): Promise<ImportResult> {
  const fetched = await fetchLatest();
  return importFetched(fetched);
}

export { getLanguage, setLanguage } from './storage';
