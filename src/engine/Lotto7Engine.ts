import type { AnalysisSummary, Draw } from '../models/types';

const MIN_NUM = 1;
const MAX_NUM = 37;
const MAIN_COUNT = 7;

export function kouOf(n: number): number {
  if (n <= 7) return 1;
  if (n <= 14) return 2;
  if (n <= 21) return 3;
  if (n <= 28) return 4;
  return 5;
}

export function kouDistribution(nums: number[]): number[] {
  const counts = [0, 0, 0, 0, 0, 0];
  nums.forEach((n) => {
    counts[kouOf(n)]++;
  });
  return counts.slice(1);
}

export class PatternAnalyzer {
  freq: Record<number, number> = {};
  monthFreq: Record<number, Record<number, number>> = {};
  kouPatterns: Record<string, number> = {};
  oddPatterns: Record<number, number> = {};
  sums: number[] = [];
  sumAvg = 0;
  sumStd = 0;
  topKou = new Set<string>();
  topOdd = new Set<number>();

  constructor(private draws: Draw[]) {
    this.analyze();
  }

  private analyze() {
    this.draws.forEach((draw) => {
      draw.nums.forEach((n) => {
        this.freq[n] = (this.freq[n] ?? 0) + 1;
        if (draw.month != null) {
          if (!this.monthFreq[draw.month]) this.monthFreq[draw.month] = {};
          this.monthFreq[draw.month][n] = (this.monthFreq[draw.month][n] ?? 0) + 1;
        }
      });
      const kou = kouDistribution(draw.nums);
      const kouKey = kou.join(',');
      this.kouPatterns[kouKey] = (this.kouPatterns[kouKey] ?? 0) + 1;
      const odd = draw.nums.filter((n) => n % 2 === 1).length;
      this.oddPatterns[odd] = (this.oddPatterns[odd] ?? 0) + 1;
      this.sums.push(draw.nums.reduce((a, b) => a + b, 0));
    });

    this.sumAvg = this.sums.reduce((a, b) => a + b, 0) / this.sums.length;
    const variance =
      this.sums.reduce((acc, s) => acc + (s - this.sumAvg) ** 2, 0) / this.sums.length;
    this.sumStd = Math.sqrt(variance);

    this.topKou = new Set(
      Object.entries(this.kouPatterns)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 30)
        .map(([k]) => k)
    );
    this.topOdd = new Set(
      Object.entries(this.oddPatterns)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 4)
        .map(([k]) => Number(k))
    );
  }

  numberWeights(
    targetMonth?: number | null,
    savedWinningNumbers: number[][] = []
  ): Record<number, number> {
    const weights: Record<number, number> = {};
    for (let n = MIN_NUM; n <= MAX_NUM; n++) {
      weights[n] = this.freq[n] ?? 1;
    }
    if (targetMonth != null && this.monthFreq[targetMonth]) {
      Object.entries(this.monthFreq[targetMonth]).forEach(([n, count]) => {
        weights[Number(n)] = (weights[Number(n)] ?? 0) + count * 0.5;
      });
    }
    if (savedWinningNumbers.length > 0) {
      const recentFreq: Record<number, number> = {};
      savedWinningNumbers.slice(0, 20).forEach((nums) => {
        nums.forEach((n) => {
          recentFreq[n] = (recentFreq[n] ?? 0) + 1;
        });
      });
      Object.entries(recentFreq).forEach(([n, count]) => {
        weights[Number(n)] = (weights[Number(n)] ?? 1) * Math.max(1 - count * 0.12, 0.3);
      });
      const allWinning = new Set(savedWinningNumbers.flat());
      for (let n = MIN_NUM; n <= MAX_NUM; n++) {
        if (!allWinning.has(n)) {
          weights[n] = (weights[n] ?? 1) * 1.08;
        }
      }
    }
    return weights;
  }

  buildSummary(targetMonth: number | null): AnalysisSummary {
    const hot = Object.entries(this.freq)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([n]) => Number(n));
    const cold = Object.entries(this.freq)
      .sort((a, b) => a[1] - b[1])
      .slice(0, 5)
      .map(([n]) => Number(n));
    const topOdd = Object.entries(this.oddPatterns)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([k, v]) => [Number(k), v] as [number, number]);
    const latest = this.draws[this.draws.length - 1];

    return {
      totalDraws: this.draws.length,
      latestRound: latest.round,
      latestDate: latest.date,
      latestNums: latest.nums,
      hotNumbers: hot,
      coldNumbers: cold,
      sumAverage: this.sumAvg,
      sumMin: Math.min(...this.sums),
      sumMax: Math.max(...this.sums),
      topOddPatterns: topOdd,
      targetMonth,
    };
  }
}

export class Generator {
  constructor(
    private analyzer: PatternAnalyzer,
    private savedWinningNumbers: number[][] = []
  ) {}

  private overlapsWinning(nums: number[]): boolean {
    if (this.savedWinningNumbers.length === 0) return false;
    const key = nums.join(',');
    if (this.savedWinningNumbers.some((w) => w.join(',') === key)) return true;
    const set = new Set(nums);
    return this.savedWinningNumbers.some((winning) => {
      const overlap = winning.filter((n) => set.has(n)).length;
      return overlap >= 5;
    });
  }

  private fitsPattern(nums: number[]): boolean {
    if (this.overlapsWinning(nums)) return false;
    const kou = kouDistribution(nums);
    const kouKey = kou.join(',');
    const odd = nums.filter((n) => n % 2 === 1).length;
    const total = nums.reduce((a, b) => a + b, 0);
    if (!this.analyzer.topKou.has(kouKey)) return false;
    if (!this.analyzer.topOdd.has(odd)) return false;
    const min = Math.floor(this.analyzer.sumAvg - 1.8 * this.analyzer.sumStd);
    const max = Math.ceil(this.analyzer.sumAvg + 1.8 * this.analyzer.sumStd);
    if (total < min || total > max) return false;
    return true;
  }

  private weightedPick(weights: Record<number, number>, exclude: Set<number>): number {
    const candidates = Array.from({ length: MAX_NUM - MIN_NUM + 1 }, (_, i) => i + MIN_NUM).filter(
      (n) => !exclude.has(n)
    );
    const totalWeight = candidates.reduce((sum, n) => sum + (weights[n] ?? 1), 0);
    let roll = Math.random() * totalWeight;
    for (const n of candidates) {
      roll -= weights[n] ?? 1;
      if (roll <= 0) return n;
    }
    return candidates[candidates.length - 1];
  }

  generateOne(targetMonth?: number | null): number[] {
    const weights = this.analyzer.numberWeights(targetMonth, this.savedWinningNumbers);
    for (let i = 0; i < 500; i++) {
      const nums = new Set<number>();
      while (nums.size < MAIN_COUNT) {
        nums.add(this.weightedPick(weights, nums));
      }
      const result = Array.from(nums).sort((a, b) => a - b);
      if (this.fitsPattern(result)) return result;
    }
    const nums = new Set<number>();
    while (nums.size < MAIN_COUNT) {
      nums.add(this.weightedPick(weights, nums));
    }
    return Array.from(nums).sort((a, b) => a - b);
  }

  generate(count = 10, targetMonth?: number | null): number[][] {
    const seen = new Set<string>();
    const results: number[][] = [];
    let attempts = 0;
    while (results.length < count && attempts < count * 200) {
      attempts++;
      const nums = this.generateOne(targetMonth);
      const key = nums.join(',');
      if (!seen.has(key)) {
        seen.add(key);
        results.push(nums);
      }
    }
    return results;
  }
}

export function resolveTargetMonth(draws: Draw[], overrideMonth?: number | null): number | null {
  if (overrideMonth != null) return Math.min(12, Math.max(1, overrideMonth));
  const lastMonth = draws[draws.length - 1]?.month;
  if (lastMonth == null) return null;
  return (lastMonth % 12) + 1;
}
