# ロト番号 (Lotto Number)

日本ロト7の本数字パターンを分析し、番号を自動生成するAndroidアプリです。

## 機能

| メニュー | 説明 |
|---------|------|
| **ロト番号** | 内蔵本数字データ(680回)を分析して10組自動生成。登録済み当選数字も反映 |
| **当選数字** | 発表された当選数字の登録・修正・削除 |
| **当選照会** | 内蔵680回の過去当選本数字を検索・ページ表示 |
| **生成履歴** | 自動生成番号を日時降順で10件ずつ表示 |
| **設定** | 表示言語、本数字自動登録、公式サイトから取得 |

- 公式当選発表サイトへのリンク
- アプリ名: **ロト番号** (日本語)
- 使用・開発メニューアル: [README_JP.md](../README_JP.md)（[English](../README.md) / [한국어](../README_KO.md)）

## ビルド

```bash
cd android
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`  
リリース用: `releases/loto-number-v1.3.1-debug.apk`

Android Studioで `android` フォルダを開いて実行することもできます (JDK 17 必要)。

## データ更新

```bash
source .venv/bin/activate
python android/export_draws.py
```

## 構成

```
android/app/src/main/
├── assets/draws.json          # 抽選データ (680回)
├── java/com/lotto7/generator/
│   ├── Lotto7Engine.kt        # パターン分析・生成
│   ├── AppViewModel.kt        # 画面状態管理
│   ├── data/                  # Room DB, DataStore
│   ├── i18n/                  # 多言語
│   └── ui/screens/            # 各画面
```
