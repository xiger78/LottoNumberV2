export interface Draw {
  round: string;
  date: string;
  month: number | null;
  nums: number[];
}

export interface AnalysisSummary {
  totalDraws: number;
  latestRound: string;
  latestDate: string;
  latestNums: number[];
  hotNumbers: number[];
  coldNumbers: number[];
  sumAverage: number;
  sumMin: number;
  sumMax: number;
  topOddPatterns: [number, number][];
  targetMonth: number | null;
}

export interface GeneratedCombo {
  index: number;
  numbers: number[];
  sum: number;
  oddCount: number;
  kouDistribution: number[];
}

export interface WinningNumber {
  id: number;
  roundLabel: string;
  drawDate: string;
  numbers: string;
  updatedAt: number;
}

export interface GenerationHistory {
  id: number;
  createdAt: number;
  numbers: string;
}

export interface ImportResult {
  added: number;
  skipped: number;
  message: string;
}

export interface FetchedWinning {
  roundLabel: string;
  drawDate: string;
  numbers: number[];
}

export const OFFICIAL_LOTO7_URL =
  'https://www.mizuhobank.co.jp/takarakuji/check/loto/loto7/index.html';
