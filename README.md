# ロト番号 / Lotto Number

日本ロト7 (Loto7) 本数字パターン分析 & 自動生成ツール

## プロジェクト構成

- `lotto7_generator.py` — Python CLI 生成器
- `ロ또7.xlsx` — 抽選履歴データ
- `android/` — Android アプリ (Jetpack Compose)

## Python 実行

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python lotto7_generator.py
```

## Android アプリ

APK ダウンロード: [releases/loto-number-v1.1-debug.apk](../releases/loto-number-v1.1-debug.apk)

詳細は [android/README.md](android/README.md) および [docs/MANUAL.md](docs/MANUAL.md) を参照。

```bash
cd android && ./gradlew assembleDebug
```

## 免責事項

過去データに基づく参考用ツールです。当選を保証しません。
