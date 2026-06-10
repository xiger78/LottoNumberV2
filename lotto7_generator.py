#!/usr/bin/env python3
"""로또7 본숫자 패턴 분석 및 추천 번호 생성기"""

import argparse
import random
import re
import sys
from collections import Counter
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple

import pandas as pd

MIN_NUM = 1
MAX_NUM = 37
MAIN_COUNT = 7
DEFAULT_EXCEL = "로또7.xlsx"


def kou_of(n: int) -> int:
    """번호가 속하는 口(1~5) 구간"""
    if n <= 7:
        return 1
    if n <= 14:
        return 2
    if n <= 21:
        return 3
    if n <= 28:
        return 4
    return 5


def kou_distribution(nums: List[int]) -> Tuple[int, ...]:
    cnt = Counter(kou_of(n) for n in nums)
    return tuple(cnt.get(i, 0) for i in range(1, 6))


def parse_month(date_str: str) -> Optional[int]:
    m = re.search(r"(\d+)月", str(date_str))
    return int(m.group(1)) if m else None


def load_draws(excel_path: Path) -> List[dict]:
    df = pd.read_excel(excel_path, header=None)
    draws = []

    for i in range(2, len(df)):
        row = df.iloc[i]
        try:
            first = float(row[2])
            if pd.isna(first) or first <= 0:
                continue
            nums = sorted(int(float(row[j])) for j in range(2, 9))
            if len(nums) != MAIN_COUNT or any(n < MIN_NUM or n > MAX_NUM for n in nums):
                continue
            draws.append(
                {
                    "round": row[0],
                    "date": str(row[1]) if pd.notna(row[1]) else "",
                    "month": parse_month(row[1]),
                    "nums": nums,
                    "kou": kou_distribution(nums),
                    "odd_count": sum(1 for n in nums if n % 2),
                    "sum": sum(nums),
                }
            )
        except (TypeError, ValueError):
            continue

    if not draws:
        raise ValueError(f"유효한 추첨 데이터를 찾을 수 없습니다: {excel_path}")
    return draws


class PatternAnalyzer:
    def __init__(self, draws: List[dict]):
        self.draws = draws
        self._analyze()

    def _analyze(self):
        self.freq = Counter()
        self.month_freq: Dict[int, Counter] = {}
        self.pair_freq = Counter()

        for d in self.draws:
            for n in d["nums"]:
                self.freq[n] += 1
                if d["month"]:
                    self.month_freq.setdefault(d["month"], Counter())[n] += 1
            for i, a in enumerate(d["nums"]):
                for b in d["nums"][i + 1 :]:
                    self.pair_freq[(a, b)] += 1

        self.kou_patterns = Counter(d["kou"] for d in self.draws)
        self.odd_patterns = Counter(d["odd_count"] for d in self.draws)
        self.sums = [d["sum"] for d in self.draws]
        self.sum_avg = sum(self.sums) / len(self.sums)
        self.sum_std = (sum((s - self.sum_avg) ** 2 for s in self.sums) / len(self.sums)) ** 0.5

        self.top_kou = [p for p, _ in self.kou_patterns.most_common(30)]
        self.top_odd = [c for c, _ in self.odd_patterns.most_common(4)]

    def number_weights(self, target_month: Optional[int] = None) -> Dict[int, float]:
        weights = {n: float(self.freq.get(n, 1)) for n in range(MIN_NUM, MAX_NUM + 1)}
        if target_month and target_month in self.month_freq:
            month_counter = self.month_freq[target_month]
            for n in range(MIN_NUM, MAX_NUM + 1):
                weights[n] += month_counter.get(n, 0) * 0.5
        return weights

    def summary(self) -> str:
        hot = [n for n, _ in self.freq.most_common(5)]
        cold = [n for n, _ in self.freq.most_common()[-5:]]
        top_kou = self.kou_patterns.most_common(3)
        lines = [
            f"분석 회차: {len(self.draws)}회 (최신: {self.draws[-1]['round']} / {self.draws[-1]['date']})",
            f"최근 당첨 본숫자: {self.draws[-1]['nums']}",
            f"자주 나온 번호 TOP5: {hot}",
            f"적게 나온 번호 TOP5: {cold}",
            f"합계 평균: {self.sum_avg:.1f} (범위 {min(self.sums)}~{max(self.sums)})",
            f"홀수 개수 분포 TOP: {self.odd_patterns.most_common(3)}",
            f"口 분포 패턴 TOP3: {top_kou}",
        ]
        return "\n".join(lines)


class Lotto7Generator:
    def __init__(self, analyzer: PatternAnalyzer, seed: Optional[int] = None):
        self.analyzer = analyzer
        if seed is not None:
            random.seed(seed)

    def _fits_pattern(self, nums: List[int]) -> bool:
        a = self.analyzer
        kou = kou_distribution(nums)
        odd = sum(1 for n in nums if n % 2)
        total = sum(nums)

        if kou not in a.top_kou:
            return False
        if odd not in a.top_odd:
            return False
        if not (a.sum_avg - 1.8 * a.sum_std <= total <= a.sum_avg + 1.8 * a.sum_std):
            return False
        return True

    def _weighted_pick(self, weights: Dict[int, float], exclude: Set[int]) -> int:
        candidates = [n for n in range(MIN_NUM, MAX_NUM + 1) if n not in exclude]
        w = [weights[n] for n in candidates]
        return random.choices(candidates, weights=w, k=1)[0]

    def generate_one(self, target_month: Optional[int] = None) -> List[int]:
        weights = self.analyzer.number_weights(target_month)

        for _ in range(500):
            nums = set()  # type: Set[int]
            while len(nums) < MAIN_COUNT:
                nums.add(self._weighted_pick(weights, nums))
            result = sorted(nums)
            if self._fits_pattern(result):
                return result

        # 패턴 조건 완화 fallback
        nums = set()
        while len(nums) < MAIN_COUNT:
            nums.add(self._weighted_pick(weights, nums))
        return sorted(nums)

    def generate(self, count: int = 10, target_month: Optional[int] = None) -> List[List[int]]:
        seen = set()  # type: Set[Tuple[int, ...]]
        results = []  # type: List[List[int]]

        attempts = 0
        while len(results) < count and attempts < count * 200:
            attempts += 1
            nums = self.generate_one(target_month)
            key = tuple(nums)
            if key not in seen:
                seen.add(key)
                results.append(nums)
        return results


def main():
    parser = argparse.ArgumentParser(description="로또7 본숫자 패턴 분석 및 추천 번호 생성")
    parser.add_argument("-f", "--file", default=DEFAULT_EXCEL, help="엑셀 파일 경로")
    parser.add_argument("-n", "--count", type=int, default=10, help="생성할 조합 수")
    parser.add_argument("-m", "--month", type=int, help="다음 추첨 월 (1~12, 월별 패턴 반영)")
    parser.add_argument("-s", "--seed", type=int, help="랜덤 시드 (재현용)")
    args = parser.parse_args()

    excel_path = Path(args.file)
    if not excel_path.exists():
        print(f"파일을 찾을 수 없습니다: {excel_path}", file=sys.stderr)
        sys.exit(1)

    draws = load_draws(excel_path)
    analyzer = PatternAnalyzer(draws)
    generator = Lotto7Generator(analyzer, seed=args.seed)

    target_month = args.month
    if target_month is None and draws[-1]["month"]:
        target_month = draws[-1]["month"] % 12 + 1

    print("=" * 50)
    print("  로또7 본숫자 패턴 분석 결과")
    print("=" * 50)
    print(analyzer.summary())
    if target_month:
        print(f"다음 추첨 월 패턴 반영: {target_month}月")
    print()
    print("=" * 50)
    print(f"  추천 본숫자 {args.count}조합")
    print("=" * 50)

    combos = generator.generate(count=args.count, target_month=target_month)
    for i, nums in enumerate(combos, 1):
        formatted = "  ".join(f"{n:02d}" for n in nums)
        kou = kou_distribution(nums)
        odd = sum(1 for n in nums if n % 2)
        print(f"  {i:2d}. [{formatted}]  (합:{sum(nums):3d}  홀:{odd}  口:{kou})")


if __name__ == "__main__":
    main()
