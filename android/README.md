# ロト番号 (Lotto Number)

日本ロト7の本数字パターンを分析し、番号を自動生成するAndroidアプリです。

## 機能

| メニュー | 説明 |
|---------|------|
| **ロト番号** | エクセル本数字データ(676回)を分析して10組自動生成。登録済み当選数字も反映 |
| **当選数字** | 発表された当選数字の登録・修正・削除 |
| **生成履歴** | 自動生成番号を日時降順で10件ずつ表示 |
| **設定** | 表示言語 (日本語 / 한국어 / English) |

- 公式当選発表サイトへのリンク
- アプリ名: **ロト番号** (日本語)
- 使用・開発メニューアル: [docs/MANUAL.md](../docs/MANUAL.md)

## ビルド

```bash
cd android
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Android Studioで `android` フォルダを開いて実行することもできます (JDK 17 必要)。

## データ更新

```bash
source .venv/bin/activate
python android/export_draws.py
```

## 構成

```
android/app/src/main/
├── assets/draws.json          # 抽選データ
├── java/com/lotto7/generator/
│   ├── Lotto7Engine.kt        # パターン分析・生成
│   ├── AppViewModel.kt        # 画面状態管理
│   ├── data/                  # Room DB, DataStore
│   ├── i18n/                  # 多言語
│   └── ui/screens/            # 各画面
```
