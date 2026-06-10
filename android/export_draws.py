#!/usr/bin/env python3
"""엑셀 데이터를 Android assets JSON으로 변환"""

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from lotto7_generator import load_draws

ROOT = Path(__file__).resolve().parent.parent
EXCEL = ROOT / "로또7.xlsx"
OUT = ROOT / "android" / "app" / "src" / "main" / "assets" / "draws.json"


def main():
    if not EXCEL.exists():
        print(f"엑셀 파일 없음: {EXCEL}", file=sys.stderr)
        sys.exit(1)

    draws = load_draws(EXCEL)
    out = [
        {
            "round": str(d["round"]),
            "date": d["date"],
            "month": d["month"],
            "nums": d["nums"],
        }
        for d in draws
    ]
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False)
    print(f"Exported {len(out)} draws -> {OUT}")


if __name__ == "__main__":
    main()
