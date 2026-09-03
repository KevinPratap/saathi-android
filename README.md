# 🤝 Saathi (साथी)

> **AI Companion & Scam Guardian for Elderly Android Users (60+)**

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Tests](https://img.shields.io/badge/Unit_Tests-51%2F51_Passed-brightgreen)](#automated-testing)
[![Latency](https://img.shields.io/badge/Evaluation_Latency-1.3ms-blue)](#performance)
[![Privacy](https://img.shields.io/badge/Privacy-Zero--PII_On--Device-success)](#privacy--security)

---

## 📖 Overview

**Saathi** (Hindi for *Companion*) is an assistive Android application engineered specifically for senior citizens (60+) in India with low digital literacy. Elderly users are disproportionately targeted by financial cyber-fraud (OTP theft, fake UPI payment requests, bank KYC threats, courier scams, and malicious APK downloads).

Saathi operates as an always-on protective and teaching layer over any app using Android's native **`AccessibilityService`** and hardware overlays, **without requiring screen recording (`MediaProjection`), screenshots, or cloud vision**.

---

## 🎯 The Three Modes

| Mode | Trigger | Core Interaction | Safety Boundary |
|---|---|---|---|
| **Teaching Mode** | User asks *"How do I send a voice note on WhatsApp?"* | Inspects screen UI tree in real time; draws a glowing animated pointer arrow directly over the mic button; speaks Hindi/regional TTS instructions. | Pauses if app switches; resumes upon return. |
| **Safety Mode** | Always-on background daemon | Detects fraudulent patterns (OTP theft, account block threats, prize scams) and immediately fires a high-contrast modal interrupt screen before the user can share codes or tap confirm. | **Friction barrier**: Requires a 3-second hold to bypass, preventing accidental tremor taps. |
| **Agent Mode** | User asks *"Pay electricity bill"* | Launches target app (PhonePe) via Intent, navigates through category menus, and highlights circle number/account fields. | **Strictly non-custodial**: Saathi never inputs amounts, never enters UPI PINs, and never clicks final payment authorization. |

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                 Elderly User's Android Device                │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │      SaathiAccessibilityService (Always-On Daemon)    │  │
│  │      - DFS UI tree traversal & node extraction        │  │
│  │      - Debounced event processing (250-400ms)         │  │
│  └──────────────────────────┬────────────────────────────┘  │
│                             │                               │
│                     ┌───────▼────────┐                      │
│                     │  Local Engine  │                      │
│                     │  (On-Device)   │                      │
│                     └───────┬────────┘                      │
│                             │                               │
│         ┌───────────────────┼───────────────────┐           │
│         ▼                   ▼                   ▼           │
│   Teaching Mode        Safety Mode          Agent Mode      │
│   (Spoken TTS +        (Regex/Trie FSM      (App intent +   │
│    pointing arrows)     <50ms interrupt)     guided steps)  │
│         │                   │                   │           │
│         └───────────────────┼───────────────────┘           │
│                             │                               │
│                     ┌───────▼────────┐                      │
│                     │ Room Database  │                      │
│                     │ (Zero-PII Log) │                      │
│                     └────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

### Module Breakdown (`app/src/main/java/com/saathi/`)

- **`com.saathi.engine`** — Deterministic On-Device Threat Detection:
  - `TextNormalizer`: Unicode NFKD normalization, zero-width space stripping (`\u200B-\u200F`, `\uFEFF`), and punctuation removal.
  - `HomoglyphMapper` & `LeetspeakMapper`: Sanitizes mixed Latin/Devanagari scripts and character substitutions (`0TP`, `v3rify`).
  - `Trie`: High-throughput prefix tree for multi-pattern keyword lookups in sub-millisecond time.
  - `RiskEvaluator`: Multi-signal heuristic scoring, keyword adjacency bonuses, and package whitelist checks.
  - `AlertCooldownManager`: 5-minute per-pattern alert damping to eliminate notification fatigue.
- **`com.saathi.overlay`** — Hardware Window Compositor:
  - `SaathiOverlayView`: Canvas drawing for pointing beacons, directional arrows, and a 3-second hold-to-dismiss barrier.
  - `OverlayManager`: Attaches overlays using `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- **`com.saathi.service`** — Sensory Ingestion:
  - `SaathiAccessibilityService`: Real-time listener for window events, extracting bounds and node metadata.
  - `EventDebouncer`: Throttles rapid scrolling and UI mutations.
- **`com.saathi.watchdog`** — OEM Survival Watchdog:
  - `WatchdogService`: Isolated `:watchdog` process maintaining a persistent `dataCapture` foreground notification.
  - `WatchdogJobService` & `BootReceiver`: Periodic `JobScheduler` monitor and device reboot hooks.
  - `OemIntentHelper`: Direct deep-linking to MIUI, ColorOS, and Funtouch auto-start settings.
- **`com.saathi.data`** — Privacy-First Persistence:
  - `AppDatabase`: Local Room database storing pattern signatures, user preferences, and audit trails.
- **`com.saathi.util`** — Privacy Utilities:
  - `ZeroPiiSanitizer`: Strips OTP codes, bank account numbers, and phone numbers before logging.
  - `AudioFeedbackHelper`: Tone frequencies tuned for senior presbycusis.

---

## 🧪 Automated Testing

Saathi includes a comprehensive verification test suite:

```bash
# Run all unit and benchmark tests
./gradlew test
```

### Benchmark Results
- **Total Tests**: **51 / 51 passed** (0 failures, 0 errors).
- **Synthetic Scam Benchmark**: **100% positive hit rate** across 20 scam fixtures (including Devanagari Unicode homoglyphs, zero-width joiners, and leetspeak).
- **False Positives**: **0** on negative controls (normal chats, legitimate bank messages).
- **Average Detection Latency**: **1.3 ms** (far exceeding the <15ms requirement).

---

## 🔒 Privacy & Security Guarantees

1. **Zero Raw Video / Screen Streaming**: Saathi uses structural `AccessibilityNodeInfo` bounds, not `MediaProjection` screen recording.
2. **Zero PII Cloud Leaks**: All threat evaluation happens locally on-device.
3. **Non-Custodial Financial Boundary**: The app is strictly read-and-guide. It can never initiate fund transfers or harvest credentials.

---

## 🚀 Building & Installation

### Requirements
- Android SDK 26 (Android 8.0 Oreo) or higher
- Target SDK 34 (Android 14)
- Java 17+
- Gradle 8.x

```bash
# Clone the repository
git clone https://github.com/KevinPratap/saathi-android.git
cd saathi-android

# Build Debug APK
./gradlew assembleDebug

# Output APK located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License & Author

- **Author**: Kevin Pratap Sidhu
- **Specification**: [SAATHI_SPECIFICATION.md](SAATHI_SPECIFICATION.md)
- **Deliberation Analysis**: [DELIBERATION_ANALYSIS.md](DELIBERATION_ANALYSIS.md)
- **Hardened Architecture**: [HARDENED_ARCHITECTURE.md](HARDENED_ARCHITECTURE.md)
