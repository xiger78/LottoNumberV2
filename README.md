# Loto Number (ロト番号)

> **Manuals:** [English](README.md) · [한국어](README_KO.md) · [日本語](README_JP.md)

User and developer manual for the **Loto Number** React Native app — pattern analysis and auto-generation for Japanese **Loto7**.

> **App name:** ロト番号  
> **Version:** 1.4  
> **Package:** `com.lotto7.generator`  
> **Repository:** https://github.com/xiger78/LottoNumber

---

## 1. Overview

**Loto Number** analyzes **680** past Loto7 main-number draws and generates 10 weighted random combinations based on statistical patterns.

| Item | Details |
|------|---------|
| Lottery | Japanese Loto7 (pick 7 numbers from 1–37) |
| Data source | `assets/draws.json` (680 embedded draws) |
| Default UI language | Japanese |
| Supported languages | 日本語 / 한국어 / English |
| Platform | React Native (Expo) — iOS & Android |

---

## 2. Screen Layout

A **LOTO 7 banner** is always shown below the top bar. Five menus are available from the bottom navigation.

```
┌─────────────────────────────┐
│  TopBar (menu title)        │
├─────────────────────────────┤
│  ★ LOTO 7 Banner            │
├─────────────────────────────┤
│                             │
│  Menu content               │
│                             │
├─────────────────────────────┤
│ Loto│Win │Lookup│Hist│Set  │
└─────────────────────────────┘
```

---

## 3. Menu Guide

### 3.1 Loto Numbers (ロト番号)

Main screen: analyzes draw data and auto-generates numbers.

**Features**

- Pattern analysis over **680 draws** (frequency, kou distribution, odd/even, sum)
- **Monthly pattern** selection (Jan–Dec) — weights numbers by draw month
- **Generate 10 sets** — weighted random + pattern filters
- **Saved winning numbers** — recent winners get lower weight; numbers not in saved draws get a boost
- Link to [Mizuho Bank Loto7 official results](https://www.mizuhobank.co.jp/takarakuji/check/loto/loto7/index.html)

### 3.2 Winning Numbers (当選数字)

View, register, edit, and delete winning main numbers. **Latest draw data is synced automatically.**

### 3.3 Winning Lookup (当選照会)

Browse past winning main numbers from embedded draw data (**680 draws**).

### 3.4 Generation History (生成履歴)

History of auto-generated number sets.

### 3.5 Settings (設定)

Language and data import options (auto-register embedded draws, fetch from official site).

---

## 4. Number Generation Algorithm

```
1. Build per-number weights from 680 draws + saved winning numbers
2. Add monthly pattern weights
3. Pick 7 numbers by weighted random
4. Apply pattern filters:
   - Kou distribution (1–7 / 8–14 / 15–21 / 22–28 / 29–37)
   - Odd count (3–4 most common)
   - Sum range (mean ± 1.8σ)
   - Exclude combos overlapping saved wins by 5+ numbers
5. Output 10 unique sets → saved to Generation History
```

> For entertainment and reference only. **No guarantee of winning.**

---

## 5. Development Environment

### 5.1 Required tools

| Tool | Version |
|------|---------|
| Node.js | 18+ |
| npm or yarn | latest |
| Expo CLI | via `npx expo` |
| iOS | Xcode (macOS, for iOS simulator/device) |
| Android | Android Studio / SDK (for Android emulator/device) |

### 5.2 Run locally

```bash
npm install
npx expo start
```

Then press `a` for Android emulator, `i` for iOS simulator, or scan the QR code with Expo Go.

### 5.3 Build

```bash
# Install EAS CLI (one-time)
npm install -g eas-cli

# Configure and build
eas build --platform android
eas build --platform ios
```

---

## 6. Tech Stack

| Category | Library |
|----------|---------|
| Framework | React Native + Expo ~53 |
| Language | TypeScript |
| Navigation | React Navigation (bottom tabs) |
| Storage | AsyncStorage |
| Data | Embedded `assets/draws.json` |

### Project structure

```
LottoNumber/
├── App.tsx                 # App entry + navigation
├── assets/draws.json       # 680 embedded draws
├── src/
│   ├── engine/             # Lotto7 pattern analysis & generator
│   ├── data/               # Repository, storage, official fetcher
│   ├── i18n/               # JA / KO / EN strings
│   ├── screens/            # 5 tab screens
│   ├── components/         # Banner, number balls, etc.
│   └── context/            # App state (ViewModel equivalent)
├── docs/images/            # Screenshots
└── releases/               # Published APK files (legacy)
```

---

## 7. Disclaimer

This tool uses historical draw statistics for **entertainment and reference only**. It does not guarantee wins or improve odds in any scientifically proven way.
