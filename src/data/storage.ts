import AsyncStorage from '@react-native-async-storage/async-storage';
import type { GenerationHistory, WinningNumber } from '../models/types';
import type { AppLanguage } from '../i18n/localizedStrings';

const KEYS = {
  language: '@loto/language',
  winning: '@loto/winning',
  history: '@loto/history',
  nextWinningId: '@loto/nextWinningId',
  nextHistoryId: '@loto/nextHistoryId',
};

export async function getLanguage(): Promise<AppLanguage> {
  const value = await AsyncStorage.getItem(KEYS.language);
  if (value === 'ko' || value === 'en' || value === 'ja') return value;
  return 'ja';
}

export async function setLanguage(lang: AppLanguage): Promise<void> {
  await AsyncStorage.setItem(KEYS.language, lang);
}

export async function getWinningNumbers(): Promise<WinningNumber[]> {
  const raw = await AsyncStorage.getItem(KEYS.winning);
  return raw ? JSON.parse(raw) : [];
}

export async function saveWinningNumbers(items: WinningNumber[]): Promise<void> {
  await AsyncStorage.setItem(KEYS.winning, JSON.stringify(items));
}

export async function getGenerationHistory(): Promise<GenerationHistory[]> {
  const raw = await AsyncStorage.getItem(KEYS.history);
  return raw ? JSON.parse(raw) : [];
}

export async function saveGenerationHistory(items: GenerationHistory[]): Promise<void> {
  await AsyncStorage.setItem(KEYS.history, JSON.stringify(items));
}

export async function nextWinningId(): Promise<number> {
  const raw = await AsyncStorage.getItem(KEYS.nextWinningId);
  const id = raw ? parseInt(raw, 10) : 1;
  await AsyncStorage.setItem(KEYS.nextWinningId, String(id + 1));
  return id;
}

export async function nextHistoryId(): Promise<number> {
  const raw = await AsyncStorage.getItem(KEYS.nextHistoryId);
  const id = raw ? parseInt(raw, 10) : 1;
  await AsyncStorage.setItem(KEYS.nextHistoryId, String(id + 1));
  return id;
}
