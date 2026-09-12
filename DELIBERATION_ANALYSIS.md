# SAATHI ARCHITECTURAL DELIBERATION & FAILURE MODE ANALYSIS
**Document Version:** 1.0.0 | **Author:** Teamwork Worker M1 | **Target Milestone:** R1 Deliberation Analysis  
**Project:** Saathi (साथी) — Hardened AI Companion & Threat Interception Assistant for Elderly Android Users  
**Date:** 2026-09-03 | **Status:** Approved / Production-Grade Blueprint  

---

## Table of Contents
1. [Executive Summary & Problem Framing](#1-executive-summary--problem-framing)
2. [Macro System Architecture & Operating Environment](#2-macro-system-architecture--operating-environment)
3. [Exhaustive Multi-Dimensional Failure Mode Catalog (FM-01 to FM-20)](#3-exhaustive-multi-dimensional-failure-mode-catalog)
   - [Vector 1: OS & OEM Lifecycle Hardening (FM-01 – FM-04)](#vector-1-os--oem-lifecycle-hardening)
   - [Vector 2: Accessibility API & Dynamic Screen Mutation Fragility (FM-05 – FM-12)](#vector-2-accessibility-api--dynamic-screen-mutation-fragility)
   - [Vector 3: Adversarial Scam Tactics & Multi-Modal Evasion (FM-13 – FM-16)](#vector-3-adversarial-scam-tactics--multi-modal-evasion)
   - [Vector 4: Elderly Ergonomic, Auditory & Cognitive UX Dynamics (FM-17 – FM-19)](#vector-4-elderly-ergonomic-auditory--cognitive-ux-dynamics)
   - [Vector 5: Google Play Store Policy & Security Compliance (FM-20)](#vector-5-google-play-store-policy--security-compliance)
4. [Cross-Vector Compounding Failure Scenarios](#4-cross-vector-compounding-failure-scenarios)
5. [Google Play Store Policy & Privacy Security Blueprint](#5-google-play-store-policy--privacy-security-blueprint)
6. [Subsystem Implementation Directives & Downstream Contracts](#6-subsystem-implementation-directives--downstream-contracts)
7. [Comprehensive Verification & Falsification Matrix](#7-comprehensive-verification--falsification-matrix)

---

## 1. Executive Summary & Problem Framing

### 1.1 The Societal Crisis: Elderly Financial Exploitation in India & Emerging Digital Economies
India is undergoing the most rapid digital payment transformation in human history. Over 140 million senior citizens (aged 60+) have been transitioned from physical cash, paper passbooks, and branch banking into a digital-first economy powered by the Unified Payments Interface (UPI), Aadhaar-enabled Payment Systems (AePS), and instant messaging networks (WhatsApp, Telegram). While smartphone adoption among seniors has surged past 40%, digital literacy, security intuition, and technological ergonomics have lagged severely.

According to data from the Indian Cyber Crime Coordination Centre (I4C) and the Reserve Bank of India (RBI), financial cyber fraud targeting seniors exceeded ₹11,000 crore ($1.3B USD) in 2024–2025 alone. The vast majority of these losses are concentrated in high-pressure social engineering attacks:
- **KYC Expiration & Bank Account Suspension Scams**: Fraudsters pose as SBI, HDFC, or PNB bank officials demanding immediate document verification via malicious SMS or WhatsApp links.
- **Utility Disconnection Threats**: Automated and manual threats warning that domestic electricity, gas, or water connections will be severed at 9:30 PM unless a small token fee (₹10 or ₹100) is paid via a provided link or third-party APK.
- **Digital Arrest & Law Enforcement Impersonation**: Coercive video/audio calls where scammers pose as CBI, Mumbai Police, or Supreme Court judges, threatening immediate imprisonment unless funds are liquidated into "court escrow accounts".
- **Malicious Remote Support APKs & OTP Harvesting**: Trickery coercing seniors into installing screen-sharing tools (AnyDesk, TeamViewer QuickSupport) or fake customer support APKs (`sbi_yono_update.apk`, `electricity_support.apk`) that harvest credentials and intercept One-Time Passwords (OTPs).

### 1.2 The Technological Gap of Existing Solutions
Mainstream security and assist products fail senior citizens due to fundamental architectural mismatches:
1. **Antivirus & Telecom Spam Filters (e.g. Truecaller)**: Truecaller and carrier-level filters operate exclusively at the call/SMS signaling boundary. They are blind to encrypted in-app messaging (WhatsApp, Telegram), dynamic web forms (phishing portals loaded in Chrome Custom Tabs), and in-app transaction prompts.
2. **Platform AI Assistants (e.g. Google Assistant, Apple Siri)**: Cloud-first conversational agents require explicit voice initiation, cannot inspect or parse arbitrary third-party application UI view hierarchies, and do not possess real-time threat evaluation engines for financial transactions.
3. **Static Tutorials & Family Support**: YouTube tutorials and family guidance assume baseline digital literacy (ability to navigate settings, read small English text, distinguish system dialogues from web popups). When family members are absent, seniors are left entirely unprotected during the critical 15-second decision window when a scam occurs.

### 1.3 The Saathi Mission & Operating Paradigm
Saathi (साथी) is an on-device, real-time AI companion, visual navigator, and threat interception assistant engineered specifically for elderly users in India and globally. Saathi operates across three complementary modes:
- **Teaching Mode**: Interactive visual pointing arrows and calm vernacular text-to-speech guidance explaining step-by-step tasks (e.g., sending a WhatsApp voice note, viewing a utility bill) directly overlaid on the live third-party app.
- **Safety Mode**: Always-on, deterministic background scam detection that parses on-screen text, links, and transaction prompts, instantly intervening with high-visibility visual barriers and voice alerts *before* an OTP is shared or a fraudulent payment is confirmed.
- **Agent Mode**: Guided voice navigation assisting seniors through complex transactional flows (e.g., electricity bill payment on PhonePe) while deliberately withholding automation on sensitive steps (PIN entry, biometric confirmation) to maintain strict user sovereignty.

### 1.4 The Edge Architecture Challenge
Operating Saathi requires executing at the very perimeter of the Android OS sandbox:
- Utilizing Android's `AccessibilityService` API to parse live UI component trees without root privileges.
- Projecting interactive visual indicators and urgency barriers via `TYPE_APPLICATION_OVERLAY` windows.
- Maintaining continuous 24/7 background liveness despite extreme proprietary OEM battery-killer subsystems.
- Evaluating complex multi-lingual (English, Hindi Devanagari, Romanized Hinglish) text streams with sub-15ms latency under zero cloud connectivity to guarantee total privacy and zero PII exfiltration.

---

## 2. Macro System Architecture & Operating Environment

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                          ANDROID RUNTIME LAYER                                       │
│                                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                                 THIRD-PARTY APPLICATION LAYER                                │   │
│   │                                                                                              │   │
│   │   ┌───────────────────┐    ┌───────────────────┐    ┌─────────────────┐    ┌─────────────┐   │   │
│   │   │ WhatsApp/Telegram │    │ PhonePe / GPay    │    │ Chrome WebViews │    │ Flutter/RN  │   │   │
│   │   │ (Chat / OTP SMS)  │    │ (UPI Transactions)│    │ (Phishing DOM)  │    │ (Custom UI) │   │   │
│   │   └─────────┬─────────┘    └─────────┬─────────┘    └────────┬────────┘    └──────┬──────┘   │   │
│   └─────────────┼────────────────────────┼───────────────────────┼────────────────────┼──────────┘   │
│                 │                        │                       │                    │              │
│                 ▼                        ▼                       ▼                    ▼              │
│   ┌──────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                   ACCESSIBILITY SUBSYSTEM (AccessibilityManagerService)                      │   │
│   │                                                                                              │   │
│   │   • Event Dispatcher (TYPE_WINDOW_CONTENT_CHANGED, TYPE_VIEW_TEXT_CHANGED, TYPE_VIEW_FOCUSED)│   │
│   │   • Virtual Node Proxies & Binder IPC (AccessibilityNodeInfo Hierarchy)                      │   │
│   └──────────────────────────────────────────────┬───────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────┼───────────────────────────────────────────────────┘
                                                   │
                                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   SAATHI ENGINE RUNTIME SANDBOX                                      │
│                                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │ 1. SENSORY INGESTION LAYER (com.saathi.service)                                              │   │
│   │                                                                                              │   │
│   │   ┌───────────────────────────────┐     ┌────────────────────────────────────────────────┐   │   │
│   │   │  SaathiAccessibilityService   │ ──► │  EventDebouncer (150-250ms Trailing Window)    │   │   │
│   │   │  • Recursive DFS Traversal    │     │  • Package Whitelist & Event Type Filtering    │   │   │
│   │   │  • Immutable Snapshot Builder │     │  • Recycle Safety (.recycle() in finally)      │   │   │
│   │   └───────────────┬───────────────┘     └────────────────────────────────────────────────┘   │   │
│   └───────────────────┼──────────────────────────────────────────────────────────────────────────┘   │
│                       │                                                                              │
│                       ▼ (UiNodeSnapshot)                                                             │
│   ┌──────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │ 2. DETERMINISTIC THREAT DETECTION ENGINE (com.saathi.engine)                                 │   │
│   │                                                                                              │   │
│   │   ┌─────────────────────────┐    ┌─────────────────────────┐    ┌────────────────────────┐   │   │
│   │   │ ZeroPiiSanitizer        │ ─► │ TextNormalizer          │ ─► │ Threat Detection Core │   │   │
│   │   │ • OTP Stripping         │    │ • Unicode NFKD Decomp   │    │ • Aho-Corasick Trie    │   │   │
│   │   │ • Password Suppression  │    │ • Invisible Char Strip  │    │ • Compiled Regex Pool  │   │   │
│   │   │ • Card/PII Masking      │    │ • Confusable Homoglyphs │    │ • Hinglish/Devanagari  │   │   │
│   │   │ • SHA-256 Telemetry     │    │ • Leetspeak Unfolder    │    │ • Adjacency / Scorer   │   │   │
│   │   └─────────────────────────┘    └─────────────────────────┘    └───────────┬────────────┘   │   │
│   └─────────────────────────────────────────────────────────────────────────────┼────────────────┘   │
│                                                                                 │                    │
│                                            ┌────────────────────────────────────┴────────────────┐   │
│                                            ▼                                                     ▼   │
│   ┌──────────────────────────────────────────────────┐   ┌───────────────────────────────────────┐   │
│   │ 3. ACCESSIBLE OVERLAY SUBSYSTEM                  │   │ 4. PERSISTENCE & PRIVACY LAYER        │   │
│   │    (com.saathi.overlay)                          │   │    (com.saathi.data)                  │   │
│   │                                                  │   │                                       │   │
│   │   ┌──────────────────────────────────────────┐   │   │   ┌───────────────────────────────┐   │   │
│   │   │ OverlayManager (WindowManager)           │   │   │   │ Room Database (AppDatabase)   │   │   │
│   │   │ • TYPE_APPLICATION_OVERLAY               │   │   │   │ • PatternEntity (Signatures)  │   │   │
│   │   │ • Coordinate Normalization & Insets      │   │   │   │ • AuditLogEntity (SHA-256)    │   │   │
│   │   │ • Dual Mode (Banner vs 3s Hold Modal)    │   │   │   │ • UserPreferencesEntity       │   │   │
│   │   └──────────────────────────────────────────┘   │   │   └───────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────┘   └───────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                   ▲
                                                   │ Dual IPC Heartbeat / Health Check
┌──────────────────────────────────────────────────┴───────────────────────────────────────────────────┐
│ 5. SELF-HEALING & RESILIENCE MESH (com.saathi.watchdog - Out-of-Process: `:watchdog`)                 │
│                                                                                                      │
│   ┌───────────────────────────────┐  ┌──────────────────────────────┐  ┌─────────────────────────┐   │
│   │ WatchdogService (Daemon)      │  │ WorkManager / JobScheduler   │  │ Multi-Receiver Mesh     │   │
│   │ • 15s Health Probe            │  │ • 15-Min Periodic KeepAlive  │  │ • BootReceiver          │   │
│   │ • Auto-Restart Trigger        │  │ • Exact Alarms (Doze Bypass) │  │ • PackageReplacedRecv   │   │
│   │ • Accessibility Unbind Alert  │  │ • Battery Opt Whitelist Flow │  │ • UserPresentReceiver   │   │
│   └───────────────────────────────┘  └──────────────────────────────┘  └─────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---
## 3. Exhaustive Multi-Dimensional Failure Mode Catalog

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       FAILURE MODE TAXONOMY MATRIX                                     │
├────────────┬────────┬────────────────────────────────────────────────────────────────┬─────────────────┤
│ Vector     │ ID     │ Failure Mode Canonical Name                                    │ Severity Level  │
├────────────┼────────┼────────────────────────────────────────────────────────────────┼─────────────────┤
│ OS & OEM   │ FM-01  │ OEM Aggressive Process Freezing & AutoStart Killer Termination │ CRITICAL (P0)   │
│            │ FM-02  │ Android 14/15 Sideload Restricted Settings Gating              │ HIGH (P1)       │
│            │ FM-03  │ AccessibilityService Crash Throttling & Silent Disconnection   │ CRITICAL (P0)   │
│            │ FM-04  │ Deep Doze & App Standby Restricted Bucket Network/Alarm Gating │ HIGH (P1)       │
├────────────┼────────┼────────────────────────────────────────────────────────────────┼─────────────────┤
│ A11y & UI  │ FM-05  │ Jetpack Compose Unmerged Semantics Tree & Text Fragmentation   │ HIGH (P1)       │
│ Mutation   │ FM-06  │ Flutter Semantics Tree Uninitialized / Delayed Bridge Act.     │ CRITICAL (P0)   │
│            │ FM-07  │ Canvas, SurfaceView & OpenGL/Vulkan Total Node Blackout        │ HIGH (P1)       │
│            │ FM-08  │ WebView Virtual Node Latency & Dynamic SPA Mutation Race       │ HIGH (P1)       │
│            │ FM-09  │ Asynchronous View Recycling & Stale Node Pointer Invalidation  │ CRITICAL (P0)   │
│            │ FM-10  │ Soft Keyboard (IME) Window Inset Coordinate Desynchronization  │ MEDIUM (P2)     │
│            │ FM-11  │ Multi-Window, Split-Screen & Display Cutout Coordinate Shift   │ MEDIUM (P2)     │
│            │ FM-12  │ High-Frequency AccessibilityEvent Flooding & IPC Starvation    │ HIGH (P1)       │
├────────────┼────────┼────────────────────────────────────────────────────────────────┼─────────────────┤
│ Adversary  │ FM-13  │ Unicode Homoglyph, Mixed-Script & Confusable Bypass            │ CRITICAL (P0)   │
│ Evasion    │ FM-14  │ Zero-Width Characters, Directional Overrides & Token Splitting │ CRITICAL (P0)   │
│            │ FM-15  │ Leetspeak, Phonetic Deconstruction & Multi-Lingual Hinglish    │ HIGH (P1)       │
│            │ FM-16  │ Multi-Modal Voice-Call Coercion & Social-Engineering Bypass    │ CRITICAL (P0)   │
├────────────┼────────┼────────────────────────────────────────────────────────────────┼─────────────────┤
│ Elderly    │ FM-17  │ Essential Motor Tremors, Accidental Multi-Taps & Mis-Dismissal │ HIGH (P1)       │
│ Ergonomics │ FM-18  │ False Alarm Fatigue ("Cry-Wolf Effect") & Panic Hijacking      │ HIGH (P1)       │
│ & UX       │ FM-19  │ Presbycusis High-Frequency Hearing Loss & Speech Rate Mismatch │ MEDIUM (P2)     │
├────────────┼────────┼────────────────────────────────────────────────────────────────┼─────────────────┤
│ Compliance │ FM-20  │ Google Play Accessibility Policy Violation & Foreground Ban    │ CRITICAL (P0)   │
└────────────┴────────┴────────────────────────────────────────────────────────────────┴─────────────────┘
```

---

### Vector 1: OS & OEM Lifecycle Hardening

#### FM-01: OEM Aggressive Process Freezing & AutoStart Killer Termination
- **Vector**: OS & OEM Lifecycle Hardening
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: An 68-year-old grandfather in Jaipur is using a Redmi Note 13 (Xiaomi HyperOS / MIUI 14). The phone has been sitting on his nightstand locked for 35 minutes. A fraudster posing as an electricity board employee sends a phishing SMS: *"Bijli bill pending. Power will be cut at 9 PM. Call 9876543210 immediately."* When the user unlocks the phone and taps the SMS, Saathi fails to trigger any alert because the Xiaomi Security Daemon killed the Saathi process 20 minutes earlier to save battery. The user calls the scammer and loses ₹45,000.
- **Root Cause Analysis**:
  - Proprietary OEM security suites (Xiaomi `com.miui.securitycenter`, Vivo `com.iqoo.secure` iManager, Oppo `com.coloros.safecenter`, Samsung `Device Care`) enforce custom kernel-level task-killing algorithms that override standard Android Open Source Project (AOSP) lifecycle rules.
  - Even when an application runs a valid foreground service (`startForeground()`) with an ongoing status bar notification, OEM ROMs monitor process CPU and socket activity during screen-off states.
  - After a 10–30 minute timeout, the OEM daemon moves the app's Linux UID into a frozen control group (`/sys/fs/cgroup/freezer/frozen`), issues `SIGSTOP` or `SIGKILL`, and severs the Binder IPC connection between `AccessibilityManagerService` and the application's `AccessibilityService`. The OS does not invoke `onDestroy()`, leaving the accessibility binding in a stale, dead state.
  - On Xiaomi devices, swiping the app away from the "Recent Apps" screen sends a hard `SIGKILL` unless the user has manually enabled the "Lock App" padlock icon in the Recents overview.
- **Reproduction / Trigger Conditions**:
  1. Install app on Xiaomi MIUI 14/HyperOS or Vivo Funtouch OS 13+.
  2. Enable Saathi Accessibility Service and start foreground monitoring.
  3. Turn off screen and leave device idle for 30 minutes, or swipe Saathi away from the Recent Apps tray.
  4. Unlock device and trigger an accessibility event (e.g. open a chat containing scam text).
  5. *Observed*: `onAccessibilityEvent` is never called; process is dead.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Multi-Tiered Out-of-Process Watchdog Daemon**:
     - Maintain an isolated daemon service running in an independent process (`android:process=":watchdog"`).
     - The watchdog service maintains a bidirectional heartbeat IPC channel with `SaathiAccessibilityService`. If a heartbeat is missed for >30 seconds while the screen is on, the watchdog issues a restart sequence.
  2. **Four-Layer Broadcast & Alarm Mesh**:
     - Register high-priority broadcast receivers for: `Intent.ACTION_BOOT_COMPLETED`, `Intent.ACTION_MY_PACKAGE_REPLACED`, `Intent.ACTION_USER_PRESENT` (screen unlock), `Intent.ACTION_POWER_CONNECTED`, and `Intent.ACTION_POWER_DISCONNECTED`.
     - Schedule recurring `AlarmManager.setExactAndAllowWhileIdle()` pings every 15 minutes to verify service binding status and wake the CPU briefly to re-establish dropped binder endpoints.
     - Schedule a backup `WorkManager` `PeriodicWorkRequest` (15-minute interval) using `ExistingPeriodicWorkPolicy.KEEP`.
  3. **Automated OEM AutoStart & Battery Optimization Whitelist Helper**:
     - Implement an automated device profiler (`OemPermissionHelper`) that inspects `Build.MANUFACTURER` and `Build.BRAND`.
     - During initial onboarding (completed by the family member), present a tailored step-by-step UI with direct deep-link intents to the specific OEM permission managers:
       - *Xiaomi*: `Intent("miui.intent.action.APP_PERMISSION_MANAGER")`, Component: `com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity`.
       - *Vivo*: Component `com.iqoo.secure/.safeguard.PurviewTabActivity` or `com.vivo.permissionmanager/.activity.BgStartUpManagerActivity`.
       - *Oppo/Realme*: Component `com.coloros.safecenter/.permission.startup.StartupAppListActivity`.
       - *Samsung*: `Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")`.
  4. **Family Heartbeat Telemetry & Remote Safety Indicator**:
     - Transmit an encrypted, zero-PII liveness timestamp to the family companion dashboard (or local BLE sync). If Saathi has been silent for >45 minutes during daylight hours, the family member receives a notification: *"Saathi protection on Papa's phone is currently asleep. Please tap to revive."*
- **Falsifiable Verification Method**:
  - Test on a physical Xiaomi device running HyperOS. Put phone into screen-off idle for 60 minutes. Unlock and dispatch a simulated scam text. Verify that `WatchdogService` and `UserPresentReceiver` revived the accessibility pipeline, and alert fires within 250ms of unlock.

---

#### FM-02: Android 14/15 Sideload Restricted Settings Gating
- **Vector**: OS & OEM Lifecycle Hardening
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: An NRI son working in California downloads the Saathi APK release and sends it to his 70-year-old mother in Pune over WhatsApp. She downloads and installs the APK. Following the setup guide, she navigates to *Settings -> Accessibility -> Saathi*. The toggle switch is completely greyed out, displaying a system warning: *"Restricted setting: For your security, this setting is currently unavailable."* Unable to enable the service, she abandons onboarding, leaving her phone unprotected.
- **Root Cause Analysis**:
  - In Android 13 and strictly enforced in Android 14 (API 34) and Android 15 (API 35), Google introduced `AppOpsManager.OP_ACCESS_RESTRICTED_PERMISSIONS`.
  - When an application is installed via a non-session package installer (e.g. sideloaded via WhatsApp, Telegram, Gmail attachment, or a web browser using `ACTION_VIEW` intent with `application/vnd.android.package-archive`), the OS flags the package source as untrusted and applies restricted settings gating.
  - Under this restricted state, critical platform capabilities—specifically `android.permission.BIND_ACCESSIBILITY_SERVICE` and `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`—are locked at the OS framework level. The user cannot toggle the switch from standard Accessibility Settings.
- **Reproduction / Trigger Conditions**:
  1. Build Saathi APK.
  2. Transfer APK via WhatsApp or direct download link to a device running Android 14 or 15.
  3. Install via system package installer.
  4. Open Android System Settings -> Accessibility -> Saathi.
  5. *Observed*: Toggle is disabled with "Restricted setting" notice.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Programmatic Restricted State Detection**:
     - At application startup during the onboarding wizard, inspect whether accessibility permissions can be bound.
     - On API 33+, query the package's restricted setting status via `AppOpsManager` or evaluate whether `Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)` can be modified.
  2. **Vernacular Illustrated & Spoken Bypass Navigation**:
     - If the restricted state is detected, Saathi immediately launches an accessible, full-screen guidance dialog featuring high-contrast visuals and calm voice instructions in the chosen vernacular language (Hindi, Tamil, etc.):
       - *Step 1*: "Saathi will now open App Info. (साथी अभी ऐप जानकारी खोलेगा)"
       - *Step 2*: "Tap the three dots (⋮) in the top right corner. (ऊपर दाईं ओर तीन बिंदुओं पर टैप करें)"
       - *Step 3*: "Tap 'Allow restricted settings' and enter your screen lock PIN/fingerprint. ('प्रतिबंधित सेटिंग्स की अनुमति दें' चुनें)"
       - *Step 4*: "Return to Saathi to complete setup."
     - Deep-link intent launched directly: `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", packageName, null) }`.
  3. **Official Distribution Packaging**:
     - Primary distribution via Google Play Store (using standard Google Play session installers), which completely bypasses the restricted setting flag.
- **Falsifiable Verification Method**:
  - Sideload APK onto an Android 14 Pixel emulator (`adb install -r saathi.apk`). Launch onboarding. Confirm that the app detects the restricted state, displays the exact 3-step illustrated bypass guide, and successfully directs the user to the unlocked accessibility toggle.

---

#### FM-03: AccessibilityService Crash Throttling & Silent Disconnection
- **Vector**: OS & OEM Lifecycle Hardening
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: An elderly user opens a bloated e-commerce application (e.g. Amazon or Flipkart during a festival sale) with thousands of nested UI nodes. During the recursive UI tree traversal, Saathi encounters an unhandled `StackOverflowError` or `OutOfMemoryError` in a background thread, causing `SaathiAccessibilityService` to crash. The user sees no error, but Android unbinds the service. Five minutes later, the user opens a malicious phishing link from SMS, and Saathi fails to intervene.
- **Root Cause Analysis**:
  - Android's `AccessibilityManagerService` (AMS) monitors the stability of connected accessibility clients.
  - If a bound `AccessibilityService` crashes or throws an unhandled exception during event callbacks (`onAccessibilityEvent`, `onServiceConnected`), the AMS immediately tears down the Binder connection.
  - If multiple crashes occur within a short temporal window, the framework flags the service as repeatedly failing and completely removes it from the secure setting `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`.
  - The OS does *not* attempt to restart or re-bind crashed accessibility services automatically without explicit manual intervention by the user in System Settings.
- **Reproduction / Trigger Conditions**:
  1. Force an unhandled runtime exception (e.g. `throw RuntimeException("Simulated Tree Crash")`) inside `onAccessibilityEvent()`.
  2. Trigger the event 3 times in rapid succession.
  3. Check `Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)`.
  4. *Observed*: Saathi is removed from enabled services list; no further accessibility events are dispatched to the app.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Fail-Safe Bounded Traversal with Global Exception Boundaries**:
     - Enforce a strict recursion depth limit (max depth = 32) and node count limit (max nodes = 500) in tree traversals.
     - Wrap all event handling and snapshot generation within defensive `try-catch(Throwable)` blocks. Any traversal error must fail open silently for that single event without crashing the service process.
  2. **Active Out-of-Process Liveness Probe**:
     - The `:watchdog` process runs a background loop querying `AccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)`.
     - It checks whether `ComponentName(context, SaathiAccessibilityService::class.java)` is present in the enabled list.
  3. **High-Priority Immediate Recovery Notification**:
     - If the watchdog detects that Saathi has been disabled or unbound, it immediately posts a high-priority, persistent notification (`NotificationCompat.PRIORITY_MAX`, `CATEGORY_ALARM`):
       - Title: *"⚠️ Saathi Protection Needs Re-enabling"*
       - Body: *"Tap here to turn protection back on. (सुरक्षा फिर से चालू करने के लिए यहाँ टैप करें)"*
       - Intent: `Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)`.
     - Concurrently triggers a distinct, gentle haptic pattern and spoken voice reminder if the screen is unlocked.
- **Falsifiable Verification Method**:
  - Execute unit and integration tests injecting arbitrary exceptions during traversal. Confirm zero crashes escape the service handler, and simulate an external AMS disablement to confirm the watchdog notification triggers within 15 seconds.

---

#### FM-04: Deep Doze & App Standby Restricted Bucket Network/Alarm Gating
- **Vector**: OS & OEM Lifecycle Hardening
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: The device sits unused on a table overnight. Android transitions into Deep Doze mode. At 6:00 AM, an automated scam SMS arrives. The user picks up the phone while it is still in a Deep Doze maintenance window. When the user opens the SMS, Saathi attempts to verify the text against a cloud LLM endpoint or download updated threat signatures. The network call hangs or times out, and the user clicks the scam link before the cloud response arrives.
- **Root Cause Analysis**:
  - Android Deep Doze (introduced in Android 6.0 and hardened in Android 12–15) enforces severe resource restrictions when a device is stationary with screen off on battery power:
    - Complete network access cutoff for all background and non-whitelisted applications.
    - Standard `AlarmManager` alarms are deferred until periodic maintenance windows.
    - CPU `WakeLocks` are ignored.
    - Jobs scheduled via `JobScheduler` and `WorkManager` are deferred.
  - If the application is placed into the "Restricted" App Standby Bucket (common for utility apps that users rarely launch directly into their main activity), background job execution windows are restricted to once per day.
- **Reproduction / Trigger Conditions**:
  1. Force device into deep doze via ADB: `adb shell dumpsys deviceidle force-idle`.
  2. Dispatch an incoming scam event requiring threat evaluation.
  3. *Observed*: Any network-dependent cloud verification stalls or fails with `SocketTimeoutException`.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **100% Offline-First Deterministic On-Device Threat Engine**:
     - The core scam detection engine (Regex Pool, Aho-Corasick Multi-Keyword Trie, Unicode NFKD Normalizer, Homoglyph Translation Engine) is 100% self-contained on-device.
     - Threat evaluation executes entirely in local CPU memory within <15ms with **zero network dependency**.
  2. **Battery Optimization Exemption Request**:
     - During onboarding, prompt the user/family to grant battery optimization exemption via `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
  3. **Cloud Layer Strictly for Non-Urgent Secondary Enrichment**:
     - Cloud LLM interactions are strictly quarantined to non-time-critical features (e.g. conversational teaching mode explanations).
     - For safety interception, if a cloud request is ever initiated as a background check, it is governed by a strict 2000ms timeout with automatic fallback to local deterministic classification.
- **Falsifiable Verification Method**:
  - Put device into forced Doze mode (`adb shell dumpsys deviceidle force-idle`) with airplane mode enabled (zero connectivity). Ingest a synthetic OTP scam text. Verify that the on-device Trie/Regex engine flags the scam and renders the overlay within 50ms.

---
### Vector 2: Accessibility API & Dynamic Screen Mutation Fragility

#### FM-05: Jetpack Compose Unmerged Semantics Tree & Text Fragmentation
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: A modern fintech app built entirely with Jetpack Compose displays a fraudulent transaction confirmation screen. The scammer has crafted an amount transfer prompt: *"Send ₹50,000 to Electricity Support"*. In Jetpack Compose, the label is written across three separate `Text()` composables inside an unmerged `Row`. Saathi's scam regex expects the phrase `"Send ₹.*to.*Electricity"` in a single node text property. Because the text is split across three distinct sibling accessibility nodes, the regex fails to match, and no warning is shown.
- **Root Cause Analysis**:
  - Jetpack Compose does not use traditional `android.view.View` hierarchies. Instead, it generates a virtual **Semantics Tree** that is projected into Android's `AccessibilityNodeInfo` system.
  - Compose maintains two distinct semantics trees:
    1. *Merged Semantics Tree*: Collapses children for high-level screen readers (TalkBack) when `Modifier.semantics(mergeDescendants = true)` is applied.
    2. *Unmerged Semantics Tree*: Exposes every individual composable as a separate accessibility node.
  - When third-party developers do not set `mergeDescendants = true` on parent layouts (which is common across custom fintech components), compound sentences and interactive button labels are fragmented into separate sibling nodes with disjointed text properties.
- **Reproduction / Trigger Conditions**:
  1. Render a Compose layout with `Row { Text("Verify OTP: "); Text("987654"); Text(" to transfer") }`.
  2. Traverse the resulting `AccessibilityNodeInfo` tree via standard single-node text extraction.
  3. Evaluate a regex `Regex("Verify OTP: \d+ to transfer")` on each node individually.
  4. *Observed*: Matches = 0.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Hierarchical Spatial Container Aggregation**:
     - In `SaathiAccessibilityService`, when performing recursive DFS traversal, maintain a container-level text accumulator.
     - When inspecting any container node (`ViewGroup`, Compose `LayoutNode`), collect and concatenate text snippets from all immediate sibling and descendant nodes into a combined spatial text block.
  2. **Spatial Proximity Windowing**:
     - Maintain an immutable list of spatial text tokens with their corresponding screen bounding rectangles (`Rect`).
     - If two text nodes have vertical distance < 48dp and horizontal overlap or alignment, concatenate their normalized text representations with a single whitespace separator before running regex and Trie matching.
  3. **Multi-Node Context Aggregator**:
     - Feed both individual node texts and aggregated container texts into `ScamDetectionEngine.evaluate()`.
- **Falsifiable Verification Method**:
  - Create a unit test fixture with a synthetic `UiNodeSnapshot` tree containing three split text fragments representing a bank KYC scam. Run `ScamDetectionEngine.evaluate()`. Verify that the aggregated spatial evaluator successfully triggers a high-confidence match.

---

#### FM-06: Flutter Semantics Tree Uninitialized / Delayed Bridge Activation
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: An elderly user opens a modern crypto-wallet or international remittance app built using Flutter. A fraudulent payment request is displayed on the screen. Saathi's `onAccessibilityEvent` receives only an empty root node (`android.view.View` belonging to `FlutterView`) with zero child nodes and zero text content. Saathi is completely blind to the screen content, and the user transfers their life savings.
- **Root Cause Analysis**:
  - Flutter renders its entire user interface directly onto an OpenGL/Vulkan Skia/Impeller canvas and does not create native Android `View` objects for its widgets (`Text`, `ElevatedButton`, `Column`).
  - To optimize rendering performance and conserve memory, the Flutter C++ engine **disables accessibility semantics tree generation by default**.
  - Flutter's native bridge (`io.flutter.view.AccessibilityBridge`) only instantiates and populates the accessibility node tree after it receives an initial accessibility gesture or explicit accessibility focus event (`ACTION_ACCESSIBILITY_FOCUS`).
  - If Saathi operates purely as a passive listener to `TYPE_WINDOW_CONTENT_CHANGED` without dispatching focus actions, Flutter's `AccessibilityBridge` remains dormant, presenting an entirely blank accessibility sub-tree.
- **Reproduction / Trigger Conditions**:
  1. Launch a Flutter app with accessibility services active in passive mode.
  2. Ingest `rootInActiveWindow`.
  3. Count total child nodes of `FlutterView`.
  4. *Observed*: Child count = 0; text content = `null`.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Service Configuration Flags**:
     - Configure `AccessibilityServiceInfo` with `FLAG_INCLUDE_NOT_VIEW_STATE | FLAG_REPORT_VIEW_EVENTS | FLAG_REQUEST_TOUCH_EXPLORATION_MODE`.
  2. **Synthetic Proactive Focus Probing for Flutter Windows**:
     - When `onAccessibilityEvent` detects a window transition (`TYPE_WINDOW_STATE_CHANGED`) to a package or class containing `io.flutter` or `FlutterView`, check if the root node has zero children.
     - If child count is zero, immediately dispatch a non-invasive synthetic accessibility action: `rootNode.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)`.
     - This action forces Flutter's `AccessibilityBridge` to instantly initialize its C++ semantics tree and dispatch a subsequent `TYPE_WINDOW_CONTENT_CHANGED` event containing the fully populated node hierarchy.
- **Falsifiable Verification Method**:
  - In an automated integration test against a Flutter test harness, verify that sending `ACTION_ACCESSIBILITY_FOCUS` to an empty `FlutterView` results in child node count > 0 within 150ms.

---

#### FM-07: Canvas, SurfaceView & OpenGL/Vulkan Total Node Blackout
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: A user opens a casual gaming app or a custom web3 app that renders lottery prize wheels directly to an Android `SurfaceView` using native C++ OpenGL ES. The visual display says: *"Congratulations! You won ₹25,00,000! Enter UPI PIN below to deposit processing fee"*. Because the entire UI is rendered in raw GPU pixels, Saathi's accessibility traversal sees only a single opaque `SurfaceView` with no text nodes, missing the scam entirely.
- **Root Cause Analysis**:
  - `SurfaceView`, `TextureView`, and native rendering pipelines bypass the Android `View` hierarchy entirely.
  - Pixels are drawn directly to a dedicated hardware compositor layer managed by `SurfaceFlinger`.
  - Unless the third-party developer has explicitly implemented an `AccessibilityNodeProvider` (which games and rogue apps virtually never do), Android OS has zero structural awareness of the text or controls rendered within the surface.
- **Reproduction / Trigger Conditions**:
  1. Open an OpenGL `GLSurfaceView` drawing text onto a frame buffer.
  2. Traverse the accessibility tree.
  3. *Observed*: Node class is `android.view.SurfaceView`; `text` is null; `childCount` is 0.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Opaque Surface Detection & Protective Heuristic**:
     - In `SaathiAccessibilityService`, identify opaque rendering nodes by checking `className == "android.view.SurfaceView"` or `className == "android.view.TextureView"` spanning >60% of the display area.
  2. **Context-Aware Voice & Banner Caution**:
     - If an opaque canvas window is detected while an active financial transaction or high-risk package is present, display a gentle warning banner: *"Saathi cannot verify this custom screen. Please be careful and NEVER enter your UPI PIN to receive money."*
  3. **On-Demand Local OCR Fallback Pipeline (Architecture Hook)**:
     - Provide an on-device OCR pipeline hook (using Google ML Kit Digital Ink/Text Recognition executing 100% offline). The OCR module is triggered selectively when an opaque surface is detected during user-initiated Teaching/Safety queries without streaming continuous frames.
- **Falsifiable Verification Method**:
  - In a test fixture containing a mock `SurfaceView` filling the screen, verify that the traversal engine detects the opaque surface type and emits an `OPAQUE_SURFACE_DETECTED` warning state.

---

#### FM-08: WebView Virtual Node Latency & Dynamic SPA Mutation Race
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: An elderly victim clicks an SMS link leading to a fraudulent electricity bill payment portal loaded inside a Chrome Custom Tab or an embedded `WebView`. The web page is a dynamic React Single Page Application (SPA) that loads a fake payment modal 400ms after the initial HTML skeleton renders. Saathi evaluates the page immediately upon page load, finds no scam text, and does not re-scan when the modal pops up. The user enters their banking credentials.
- **Root Cause Analysis**:
  - In WebViews and Chrome, HTML DOM nodes are translated into Android virtual `AccessibilityNodeInfo` objects by the Chromium Blink accessibility engine via cross-process IPC.
  - In dynamic SPAs (React, Vue, Angular), DOM mutations occur asynchronously via client-side JavaScript.
  - There is an intrinsic latency (150–500ms) between the visual rendering of a dynamic DOM element and the asynchronous generation and delivery of the corresponding `AccessibilityEvent` to the Android system service.
  - If the accessibility service evaluates the UI once on `TYPE_WINDOW_STATE_CHANGED` and ignores subsequent fine-grained content mutations, it misses dynamically injected phishing modals.
- **Reproduction / Trigger Conditions**:
  1. Load a web page inside a WebView that dynamically injects a `<div id="scam">Enter your NetBanking Password</div>` 350ms after window load.
  2. Record accessibility snapshots.
  3. *Observed*: Initial snapshot at t=50ms is empty; if mutation events are ignored, threat is missed.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Fine-Grained Content Change Subtree Listening**:
     - Register `AccessibilityServiceInfo.eventTypes` for `TYPE_WINDOW_CONTENT_CHANGED` with `AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE` and `AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT`.
  2. **Trailing-Edge Dynamic Mutation Debouncer**:
     - Implement an `EventDebouncer` with a 200ms trailing-edge delay. When rapid DOM mutations occur in a WebView, reset the debounce timer and execute tree traversal once the DOM mutation stream stabilizes.
  3. **Node Content Hashing for Rapid Delta Evaluation**:
     - Compute a fast 64-bit Murmur3/xxHash of extracted node text strings. If the hash matches the previous snapshot, bypass redundant regex evaluation in 0.1ms; if the hash changes, execute full scan immediately.
- **Falsifiable Verification Method**:
  - Feed simulated event sequences representing an initial empty WebView followed by a 300ms delayed DOM injection of an OTP theft prompt. Verify that `EventDebouncer` triggers evaluation and intercepts the threat within 500ms total.

---
#### FM-09: Asynchronous View Recycling & Stale Node Pointer Invalidation
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: The user is flinging/scrolling rapidly through a long WhatsApp chat history containing dozens of forwarded messages. Saathi launches a background coroutine to traverse and analyze the node tree. While the coroutine is executing, `RecyclerView` recycles the off-screen views. Calling `.getText()` or `.getBoundsInScreen()` on the stale `AccessibilityNodeInfo` binder proxy throws an `IllegalStateException`, crashes the coroutine, or reads corrupted coordinates, causing overlay arrows to point to the wrong messages.
- **Root Cause Analysis**:
  - `AccessibilityNodeInfo` objects are native C++ Binder proxy wrappers allocated and pooled by the Android OS.
  - When views in `RecyclerView`, `ListView`, or Compose `LazyColumn` are scrolled off-screen, the underlying native views are recycled and rebound to new data.
  - The Binder proxy handle held by the client application becomes invalid or reassigned to a different UI element.
  - Retaining `AccessibilityNodeInfo` references across asynchronous thread boundaries or coroutine suspension points violates Android memory contracts and results in `IllegalStateException: AccessibilityNodeInfo is not initialized` or severe memory leaks.
- **Reproduction / Trigger Conditions**:
  1. Start a fast automated fling on a 1000-item `RecyclerView`.
  2. Capture root `AccessibilityNodeInfo` and pass it to `Dispatchers.Default` for delayed traversal.
  3. Access `node.text` after a 100ms delay.
  4. *Observed*: `IllegalStateException` or stale/corrupted node bounds.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Synchronous Immutable Snapshot Extraction Pattern**:
     - Traverse the `AccessibilityNodeInfo` tree **synchronously** on the main service thread during the event callback.
     - Immediately copy all required primitive attributes (`text: String`, `contentDescription: String`, `className: String`, `packageName: String`, `boundsInScreen: Rect`, `isClickable: Boolean`, `isPassword: Boolean`) into an immutable Kotlin data class: `UiNodeSnapshot`.
  2. **Defensive Recycling Protocol**:
     - Wrap every node acquisition in a `try-finally` block and call `.recycle()` on every `AccessibilityNodeInfo` instance immediately after primitive extraction (for API < 30) or immediately discard references for ARC (API 30+).
  3. **Zero-Binder Leaks to Engine**:
     - Only pass the pure immutable `UiNodeSnapshot` to the coroutine channels, `ScamDetectionEngine`, and `OverlayManager`. Raw Android UI framework binder handles never cross the boundary.
- **Falsifiable Verification Method**:
  - Execute a stress test extracting 10,000 nodes under simulated 60fps view recycling. Confirm zero `IllegalStateException` instances and zero memory leaks.

---

#### FM-10: Soft Keyboard (IME) Window Inset Coordinate Desynchronization
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **MEDIUM (P2)**
- **Real-World Threat Scenario**: In Teaching Mode, Saathi is guiding an elderly user to enter a phone number in WhatsApp. Saathi draws a glowing orange circle around the input box at screen coordinates `(x=100, y=1400)`. The user taps the field; Gboard animates upward from the bottom, pushing the WhatsApp input box up to `(y=800)`. Saathi's overlay arrow remains stationary at `y=1400`, now pointing directly into the middle of the keyboard's QWERTY keys, thoroughly confusing the user.
- **Root Cause Analysis**:
  - Saathi's overlay view is attached to the system `WindowManager` as a full-screen window using `LayoutParams.TYPE_APPLICATION_OVERLAY` with `FLAG_LAYOUT_IN_SCREEN`.
  - When the Input Method Editor (IME / soft keyboard) appears, the underlying target application window resizes or pans (`windowSoftInputMode="adjustResize"` or `adjustPan`).
  - However, the system overlay window is rendered on a separate Z-layer spanning the physical display dimensions. If the overlay does not listen to system window insets, it remains anchored to obsolete physical coordinates.
- **Reproduction / Trigger Conditions**:
  1. Attach an overlay pointing at a bottom input field.
  2. Open the software keyboard.
  3. *Observed*: Target view moves upward, but overlay pointer remains at old bottom coordinates.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Window Insets Reactive Listener**:
     - Attach a `View.OnApplyWindowInsetsListener` to `SaathiOverlayView` to monitor `WindowInsetsCompat.Type.ime()`.
  2. **Dynamic Inset Correction & Re-Anchoring**:
     - Listen for `AccessibilityEvent.TYPE_WINDOWS_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED`.
     - When keyboard animation starts, obtain the updated `boundsInScreen` of the target node and re-render the pointer coordinates smoothly.
     - If the target node is occluded by the IME, automatically translate the guidance tooltip above the keyboard boundary with an anchor line.
- **Falsifiable Verification Method**:
  - Trigger keyboard visibility change in an automated UI test. Verify that overlay pointer coordinates update to match the panned node bounds within 100ms.

---

#### FM-11: Multi-Window, Split-Screen & Display Cutout Coordinate Distortion
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **MEDIUM (P2)**
- **Real-World Threat Scenario**: An elderly user on a Samsung Galaxy Z Fold 5 opens WhatsApp in split-screen mode side-by-side with a YouTube video. Saathi draws a guidance circle intending to highlight the "Attach" button. Because the coordinate calculation assumes full-screen 1080x2400 dimensions, the circle draws on the YouTube side of the screen instead of WhatsApp.
- **Root Cause Analysis**:
  - In multi-window, split-screen, or freeform desktop modes, `AccessibilityNodeInfo.getBoundsInScreen(Rect outBounds)` returns bounds relative to the current display coordinate space.
  - On foldable devices with camera hole punches, notches, and navigation bars, `WindowManager` coordinates diverge from display metrics if `DisplayCutout` safe insets and letterbox letterboxing offsets are not accounted for.
- **Reproduction / Trigger Conditions**:
  1. Launch device in 50/50 vertical split-screen mode.
  2. Query `node.getBoundsInScreen(rect)`.
  3. Draw canvas overlay without multi-window origin transformation.
  4. *Observed*: Pointer is displaced by the split-screen offset.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Coordinate Normalization Matrix**:
     - In `OverlayManager`, query `WindowManager.getCurrentWindowMetrics()` (API 30+) or `DisplayMetrics` to determine active window boundaries and `DisplayCutout.getSafeInsets()`.
     - Normalize all node bounds to relative normalized coordinates `[0.0, 1.0]` before mapping them to the overlay canvas drawing coordinates.
  2. **Multi-Window Bounds Verification**:
     - Verify that the target node's bounding rectangle is fully contained within the visible active window bounds before rendering pointer arrows.
- **Falsifiable Verification Method**:
  - Test overlay rendering in split-screen mode (top/bottom and left/right). Confirm indicator centers align within +/- 2dp of the physical button center.

---

#### FM-12: High-Frequency AccessibilityEvent Flooding & IPC Starvation
- **Vector**: Accessibility API & Dynamic Screen Mutation
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: The user opens a stock-market ticker app, a live sports score app, or a delivery tracker (e.g. Swiggy/Zomato live GPS animation) that emits 60 `TYPE_WINDOW_CONTENT_CHANGED` events per second. Saathi attempts to parse the entire accessibility tree on every event. The phone becomes hot, battery drops by 10% in 15 minutes, the UI stutters, and the Android Binder IPC transaction buffer saturates, causing real scam detection events in WhatsApp to be dropped.
- **Root Cause Analysis**:
  - Each `AccessibilityEvent` and subsequent node tree query involves synchronous inter-process communication (IPC) across Android's Binder driver between the target app, `system_server`, and Saathi.
  - Continuous animated UI mutations flood the Binder buffer.
  - If the client service does not throttle event processing, the main thread of the service is starved, causing garbage collection thrashing and massive battery consumption.
- **Reproduction / Trigger Conditions**:
  1. Inject a flood of 100 `TYPE_WINDOW_CONTENT_CHANGED` events per second into `onAccessibilityEvent`.
  2. Measure CPU utilization and memory allocations.
  3. *Observed*: CPU spikes to 100%; Binder transaction buffer warnings in Logcat.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Event Type & Package Level Whitelist Filter**:
     - Immediately drop events from known benign animated system packages (e.g. system UI status bar clocks, live wallpapers).
     - Filter out non-actionable event subtypes in `onAccessibilityEvent` before touching the tree.
  2. **Conflated Flow Pipeline with Rate-Limiting Debounce**:
     - Pipe incoming events into a Kotlin Coroutines `MutableSharedFlow` configured with `BufferOverflow.DROP_OLDEST`.
     - Apply a 200ms `debounce()` window for content mutations, ensuring a maximum of 5 full tree traversals per second under extreme mutation load.
  3. **Early Exit DFS Traversal**:
     - If the root package has not changed and root node text hash is identical to the previous snapshot, terminate traversal immediately at depth 0.
- **Falsifiable Verification Method**:
  - Stress-test the service with 500 events injected in 5 seconds. Verify that CPU utilization remains <5% and exactly 0 Binder buffer overflows occur.

---
### Vector 3: Adversarial Scam Tactics & Multi-Modal Evasion

#### FM-13: Unicode Homoglyph, Mixed-Script & Confusable Bypass
- **Vector**: Adversarial Scam Tactics & Multi-Modal Evasion
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: A fraudster sends a WhatsApp message: *"Aapka ЅВI account block ho gaya hai. Turant ОТР share karein"* where the letters `Ѕ`, `В`, `І`, `О`, `Т`, `Р` are drawn from the Cyrillic Unicode script (`U+0405`, `U+0412`, `U+0406`, `U+041E`, `U+0422`, `U+0420`). To human eyes, the message looks 100% identical to English "SBI" and "OTP". However, a standard ASCII regex `\bOTP\b` or `\bSBI\b` fails completely because the binary codepoints do not match ASCII integers. Saathi shows no warning, and the senior is scammed.
- **Root Cause Analysis**:
  - The Unicode standard contains thousands of glyphs across Cyrillic, Greek, Latin, Cherokee, and Mathematical Alphanumeric blocks that are visually indistinguishable (homoglyphs / confusables).
  - Attackers systematically substitute Latin letters in trigger words ("OTP", "KYC", "BANK", "PAY") with cross-script confusables to defeat static keyword filters and ASCII regular expressions.
- **Reproduction / Trigger Conditions**:
  1. Ingest string: `"\u041E\u0422\u0420"` (Cyrillic `ОТР`).
  2. Match against standard regex: `Regex("\\bOTP\\b")`.
  3. *Observed*: `isMatch == false`.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Multi-Stage Unicode Normalization Pipeline**:
     - Step 1: Apply Unicode Compatibility Decomposition (NFKD) via `java.text.Normalizer.normalize(text, Normalizer.Form.NFKD)` to decompose composite glyphs and diacritics.
     - Step 2: Strip non-spacing combining marks using regex `\p{M}+`.
  2. **High-Performance Confusable Translation Map**:
     - Implement a dedicated `HomoglyphMapper` with an array-backed lookup table mapping all known Cyrillic, Greek, Latin Extended, and Math Alphanumeric homoglyphs to their canonical Latin/Devanagari ASCII equivalents:
       - Cyrillic `А, В, С, Е, Н, І, Ј, К, М, О, Р, Ѕ, Т, Х, У, а, е, о, р, с, у, х` -> ASCII `A, B, C, E, H, I, J, K, M, O, P, S, T, X, Y, a, e, o, p, c, u, x`.
       - Greek `Α, Β, Ε, Ζ, Η, Ι, Κ, Μ, Ν, Ο, Ρ, Τ, Υ, Χ` -> ASCII `A, B, E, Z, H, I, K, M, N, O, P, T, Y, X`.
  3. **Canonical Stream Threat Evaluation**:
     - Pass the canonicalized, single-script string to the Aho-Corasick Trie and Regex engines.
- **Falsifiable Verification Method**:
  - Run unit test suite `HomoglyphMapperTest` against 50 homoglyph variations of `"OTP"`, `"KYC"`, `"SBI"`, and `"ELECTRICITY"`. Confirm 100% resolution to canonical tokens and 100% scam detection rate.

---

#### FM-14: Zero-Width Characters, Directional Overrides & Token Splitting
- **Vector**: Adversarial Scam Tactics & Multi-Modal Evasion
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: A scammer constructs a message: `O\u200BT\u200CP\u200D` by interleaving zero-width spaces (`U+200B`), zero-width non-joiners (`U+200C`), and zero-width joiners (`U+200D`) between the letters of "OTP". Alternatively, they prepend a Right-to-Left Override (`U+202E`) to reverse character byte streams while maintaining visual left-to-right display. The word renders visibly as "OTP" on WhatsApp, but standard string search fails.
- **Root Cause Analysis**:
  - Zero-width format control characters and BiDi directional formatting codes are invisible to the user when rendered by Android's HarfBuzz/Skia text shaping engine.
  - However, in raw byte and char sequences, they exist as distinct characters, breaking string adjacency, regex boundary matches (`\b`), and Trie transitions.
- **Reproduction / Trigger Conditions**:
  1. Ingest string: `"O\u200BT\u200CP"`.
  2. Evaluate `trie.search("OTP")` or `regex.find()`.
  3. *Observed*: No match found.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Invisible Character & Formatting Stripper**:
     - In `TextNormalizer`, execute a high-speed pre-sanitization pass stripping all invisible, formatting, and control codepoints:
       - Zero-Width Space (`U+200B`), Zero-Width Non-Joiner (`U+200C`), Zero-Width Joiner (`U+200D`), Left-to-Right / Right-to-Left Marks (`U+200E`, `U+200F`), Byte Order Mark (`U+FEFF`), Soft Hyphen (`U+00AD`).
       - Bidirectional overrides and embeddings (`U+202A` to `U+202E`, `U+2066` to `U+2069`).
     - Kotlin implementation:
       ```kotlin
       private val INVISIBLE_CHARS_REGEX = Regex("[\u200B-\u200F\uFEFF\u00AD\u202A-\u202E\u2060-\u2069]")
       fun stripInvisible(input: String): String = input.replace(INVISIBLE_CHARS_REGEX, "")
       ```
  2. **Punctuation & Whitespace Normalization**:
     - Collapse consecutive whitespace, hyphens, and dots within token clusters.
- **Falsifiable Verification Method**:
  - In `TextNormalizerTest`, verify that strings with interleaved zero-width characters across English and Devanagari text (`"ओ\u200Bटी\u200Cपी"`) are stripped cleanly to `"OTP"` and `"ओटीपी"`.

---

#### FM-15: Leetspeak, Phonetic Deconstruction & Multi-Lingual Hinglish Mixing
- **Vector**: Adversarial Scam Tactics & Multi-Modal Evasion
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: An attacker sends a message in Romanized Hinglish with leetspeak: *"Sir ur S-B-1 acc0unt is bl0cked. Send 0.T.P to unbl0ck turant"*. Standard English dictionaries fail on Hinglish words ("turant", "khata"), and standard regex fails on `S-B-1` and `0.T.P`.
- **Root Cause Analysis**:
  - Fraudsters in India heavily rely on bilingual Hindi-English code-mixing (Hinglish) written in the Latin alphabet, interweaving numeral substitutions (`0` for `O`, `1` for `I`/`L`, `5` for `S`, `@` for `A`) and punctuation separators.
  - A static English-only dictionary or simple keyword matching fails to handle the multi-lingual combinatorial permutations of Indian fraud vernacular.
- **Reproduction / Trigger Conditions**:
  1. Input: `"0-T-P bhej do acc0unt block ho gaya"`.
  2. Evaluate standard English scam rule set.
  3. *Observed*: Missed detection.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Leetspeak Unfolding & Digit-Symbol Translation**:
     - Implement `LeetspeakMapper` that translates common leetspeak substitutions into canonical letters:
       - `0` -> `o`, `1` -> `i`/`l`, `3` -> `e`, `4` -> `a`, `5` -> `s`, `7` -> `t`, `@` -> `a`, `$` -> `s`.
     - Strip separating delimiters (`-`, `.`, `_`, `/`, `*`) between single-character runs.
  2. **Comprehensive Dual-Script Vernacular Trie Lexicon**:
     - Maintain an on-device Aho-Corasick Trie loaded with multi-lingual threat signatures:
       - *Hindi Devanagari*: `ओटीपी`, `खाता ब्लॉक`, `केवाईसी`, `बिजली बिल`, `लॉटरी`, `इनाम`, `तुरंत भेजें`, `रिश्वत`, `पुलिस वारंट`.
       - *Romanized Hinglish*: `khata block`, `bijli bill`, `turant`, `kat jayega`, `inaam`, `lottery jeeti`, `paisa bhejo`.
       - *English*: `otp`, `kyc expired`, `account suspended`, `electricity disconnect`, `claim prize`.
  3. **Fuzzy Separator Regular Expressions**:
     - Deploy regex rules with flexible token boundaries: `\b[oO0][\W_]*[tT][\W_]*[pP]\b`.
- **Falsifiable Verification Method**:
  - Run `SyntheticScamBenchmarkTest` across 30+ multi-lingual test fixtures containing Hinglish, Devanagari, and Leetspeak variants. Verify 100% detection accuracy.

---

#### FM-16: Multi-Modal Voice-Call Coercion & Social-Engineering Bypass
- **Vector**: Adversarial Scam Tactics & Multi-Modal Evasion
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: An 74-year-old retired schoolteacher receives a WhatsApp voice call from a fraudster pretending to be a CBI officer. The scammer threatens him with a fake "digital arrest warrant" for money laundering. The scammer orders him: *"Open your banking app right now. A warning box might appear on your screen—ignore it, it's just a system glitch. Tap 'I Understand' immediately and transfer the verification deposit."* When Saathi displays a warning modal, the frightened victim immediately taps the "I Understand" button as instructed by the scammer on the call, losing ₹3,00,000.
- **Root Cause Analysis**:
  - Social engineering creates an acute psychological state of fear and urgency (amygdala hijacking).
  - When the scammer maintains continuous verbal control over an active GSM or VoIP call, a conventional single-tap dismiss button on a security overlay provides zero friction against coerced overrides.
- **Reproduction / Trigger Conditions**:
  1. Simulate an active audio call (`AudioManager.mode == MODE_IN_CALL` or `MODE_IN_COMMUNICATION`).
  2. Trigger an OTP scam detection.
  3. Single-tap the screen.
  4. *Observed*: Overlay dismisses instantly, allowing the coerced user to complete the transfer.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Active Telephony / Call State Detection**:
     - Query Android's `AudioManager.getMode()` in `OverlayManager`.
     - Detect if `mode == AudioManager.MODE_IN_CALL` (GSM call) or `mode == AudioManager.MODE_IN_COMMUNICATION` (WhatsApp/Telegram/VoIP call).
  2. **High-Friction Intervention Modal with 3-Second Hold Barrier**:
     - When a high-risk scam is detected **during an active voice call**, escalate from a lightweight banner to a full-screen **High-Friction Lockdown Modal**.
     - Replace standard single-tap dismissal with an unbreakable **3-Second Press-and-Hold Barrier** featuring a circular animated progress ring. Accidental taps or quick coaching taps cannot dismiss the barrier.
  3. **Direct Speaker Audio Counter-Coercion**:
     - Route an audible, calm, high-priority spoken alert directly through the device's main loudspeaker (overriding the earpiece):
       - *"सावधान! फोन पर बात करने वाला व्यक्ति धोखेबाज़ हो सकता है। कृपया तुरंत फोन काटें और अपने परिवार से बात करें।" (Warning! The person on the phone may be a fraudster. Please disconnect the call immediately and speak with your family.)*
  4. **Emergency Family Notification**:
     - Dispatch an immediate high-priority alert to the family member's registered companion dashboard.
- **Falsifiable Verification Method**:
  - In a test harness simulating `MODE_IN_COMMUNICATION`, trigger an OTP threat. Verify that the overlay enforces the 3-second hold barrier, ignores single-tap events, and routes spoken audio alert to `STREAM_ALARM`/loudspeaker.

---
### Vector 4: Elderly Ergonomic, Auditory & Cognitive UX Dynamics

#### FM-17: Essential Motor Tremors, Accidental Multi-Taps & Mis-Dismissals
- **Vector**: Elderly Ergonomic, Auditory & Cognitive UX Dynamics
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: An 80-year-old grandmother with Parkinson's disease or essential hand tremor tries to tap a button in Saathi's Teaching Mode. Her trembling finger oscillates at 6 Hz, striking the screen three times in 120ms. In standard Android views, this multi-tap triggers the primary button, then immediately triggers whatever dialog button appears next, accidentally dismissing the guidance flow and confusing her.
- **Root Cause Analysis**:
  - Neurological motor degradation in seniors produces involuntary tremors (4–8 Hz oscillation) and impaired touch precision (drag slips, edge hits).
  - Rapid multi-touch `ACTION_DOWN` / `ACTION_UP` sequences generated within 50–150ms cause event flooding in standard UI click listeners.
  - Sub-optimal touch target sizes (<48dp) and tight spacing cause accidental mis-taps on adjacent interactive elements.
- **Reproduction / Trigger Conditions**:
  1. Deliver 3 synthetic touch events spaced 40ms apart on a Saathi overlay button.
  2. *Observed*: Multiple click callbacks execute; overlay dismisses unexpectedly.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Global Touch Debounce & Slop Filter**:
     - Implement a strict touch debouncer on all interactive overlay components:
       ```kotlin
       var lastClickTime = 0L
       fun onDebouncedClick(action: () -> Unit) {
           val now = SystemClock.elapsedRealtime()
           if (now - lastClickTime > 450L) {
               lastClickTime = now
               action()
           }
       }
       ```
     - Enforce a minimum 450ms lockout window between recognized touch inputs.
  2. **Ergonomic Touch Targets**:
     - Enforce a minimum touch target dimension of **56dp × 56dp** (exceeding standard 48dp guidelines) with at least **16dp** padding between adjacent touchable elements.
  3. **Non-Blocking Touch Passthrough for Pointing Overlays**:
     - For visual pointing arrows and glowing beacon rings in Teaching Mode, attach the overlay with `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE`. All physical touch events pass through cleanly to the underlying app without Saathi intercepting or misdirecting touches.
- **Falsifiable Verification Method**:
  - In `OverlayViewTest`, inject 5 simulated touch events spaced 50ms apart. Confirm exactly 1 click action is dispatched.

---

#### FM-18: False Alarm Fatigue ("Cry-Wolf Effect") & Panic Hijacking
- **Vector**: Elderly Ergonomic, Auditory & Cognitive UX Dynamics
- **Severity**: **HIGH (P1)**
- **Real-World Threat Scenario**: Saathi is configured with overly aggressive regexes. Every time the user logs into their legitimate SBI YONO banking app or orders groceries on Blinkit, Saathi pops up a flashing red warning modal: *"DANGER! OTP DETECTED!"*. After experiencing this 5 times in two days during normal tasks, the user develops severe alert fatigue. When a real scam message arrives on WhatsApp, the user reflexively dismisses Saathi's warning without reading it, losing their savings.
- **Root Cause Analysis**:
  - Over-triggering false alarms on benign, routine user workflows destroys user trust ("Cry-Wolf syndrome").
  - Flashing red alarm dialogs and harsh siren tones induce panic, which narrows cognitive focus (amygdala hijack), paradoxically making elderly users more pliable to scammer coercion.
- **Reproduction / Trigger Conditions**:
  1. Open official banking app (e.g. `com.sbi.yono`).
  2. Receive normal login OTP.
  3. *Observed*: Full-screen intrusive security warning triggers, interrupting routine login.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Trusted Financial Application Context Whitelist**:
     - Maintain an encrypted local whitelist of verified financial applications (e.g. official bank apps, Google Pay, PhonePe, Paytm, BHIM).
     - When an OTP is generated or viewed *inside a whitelisted banking app initiated by the user*, classify the risk as low/benign and suppress full-screen interruptions.
     - Differentiate between **unsolicited incoming chat messages** (WhatsApp, Telegram, SMS) vs **internal banking app screens**.
  2. **Tiered Dynamic Severity Scoring & Cooldown Engine**:
     - *LOW Risk (Score 1-3)*: Internal audit log only. Zero UI interruption.
     - *MEDIUM Risk (Score 4-6)*: Non-intrusive ambient top banner with soft amber warning. Touch passthrough enabled.
     - *HIGH Risk (Score 7+)*: Full-screen modal intervention with 3-second hold barrier.
     - Implement `AlertCooldownManager`: Enforce a 5-minute per-pattern per-app cooldown to prevent repetitive alert spamming.
  3. **Calm Assertive De-Escalation Visual & Auditory Design**:
     - Use warm earthy colors (Saathi Warm `#FF9800`, Saathi Calm `#009686`) rather than alarming flashing neon reds.
     - Use reassuring, respectful language: *"Dada, ek minute rukiye. Yeh message nakli lag raha hai." (Dada, please pause for a moment. This message looks fake.)*
- **Falsifiable Verification Method**:
  - Verify that simulated OTP arrival inside whitelisted banking package `com.phonepe.app` results in zero modal interruptions, while identical text in `com.whatsapp` triggers the appropriate safety intervention.

---

#### FM-19: Presbycusis High-Frequency Hearing Loss & Speech Rate Disconnect
- **Vector**: Elderly Ergonomic, Auditory & Cognitive UX Dynamics
- **Severity**: **MEDIUM (P2)**
- **Real-World Threat Scenario**: Saathi speaks out guidance using the default Android `TextToSpeech` engine configured at default 1.0x speed and standard high-pitch formant. An 76-year-old grandfather with age-related high-frequency hearing loss (presbycusis) hears only muffled sounds and cannot distinguish sibilants and consonants ('s', 'sh', 'f', 't'). Unable to understand the voice guidance, he gives up on using the app.
- **Root Cause Analysis**:
  - Presbycusis is the progressive bilateral symmetrical sensorineural hearing loss occurring with age, characterized by severe attenuation of frequencies above 2000 Hz.
  - High-speed synthesized speech (1.0x–1.2x) with high-frequency pitch components causes acoustic smearing, eliminating phoneme separation for elderly listeners.
- **Reproduction / Trigger Conditions**:
  1. Initialize `TextToSpeech` with default platform parameters (`speechRate = 1.0f`, `pitch = 1.0f`).
  2. Synthesize complex vernacular sentence.
  3. *Observed*: Speech pace is rushed; high-frequency consonants are difficult to parse for hearing-impaired seniors.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Calibrated Low-Frequency Resonant Formant TTS Profile**:
     - In `AudioFeedbackHelper`, configure `TextToSpeech` with calibrated elderly-friendly acoustic parameters:
       ```kotlin
       tts.setSpeechRate(0.82f)  // Relaxed, unhurried cadence
       tts.setPitch(0.92f)       // Lower, warmer resonant formant
       ```
  2. **Structured Pauses & Conversational Phrasing**:
     - Inject strategic 400ms silence breaks between instructions:
       - `"Dada... [pause 400ms] Niche dekhiye... [pause 400ms] Hara button dabaiye."`
  3. **Dual Multi-Modal Sensory Synchronization**:
     - Synchronize spoken voice prompts with gentle, distinct haptic pulses (short vibration for confirmation, double pulse for warning) and simultaneous large-font on-screen text banners (minimum 20sp).
- **Falsifiable Verification Method**:
  - Verify in unit tests that `AudioFeedbackHelper` initializes TTS with speech rate <= 0.85f and pitch <= 0.95f across all supported locales (Hindi, English, Tamil, Bengali).

---

### Vector 5: Google Play Store Policy & Security Compliance

#### FM-20: Google Play Accessibility Policy Violation & Foreground Ban
- **Vector**: Google Play Store Policy & Security Compliance
- **Severity**: **CRITICAL (P0)**
- **Real-World Threat Scenario**: Saathi is submitted to the Google Play Console. Within 48 hours, the submission is rejected, and the developer account receives a strike with the notice: *"Policy Violation: Accessibility API Declared Use-Case Mandate. Your app requests Accessibility permissions for unauthorized security/antivirus/monitoring purposes."*
- **Root Cause Analysis**:
  - Google Play enforces strict automated and human policy reviews for applications declaring `android.permission.BIND_ACCESSIBILITY_SERVICE`.
  - Under the November 2021 Play Store Policy update:
    1. The Accessibility API **cannot** be used for general antivirus, malware scanning, remote access, or call recording purposes.
    2. The app's **primary declared core purpose** must be to assist users with disabilities (vision, motor, cognitive) or elder accessibility needs.
    3. The app **must** display a prominent, unbundled in-app disclosure prior to launching the system permission request.
    4. The app **must not** intercept or log user passwords, credit card numbers, or sensitive PII.
- **Reproduction / Trigger Conditions**:
  1. Submit an app requesting `BIND_ACCESSIBILITY_SERVICE` without prominent disclosure or categorized as a pure "Antivirus/Security" tool.
  2. *Observed*: Automated rejection or immediate removal from Google Play Store.
- **Concrete Architectural Mitigations & Countermeasures**:
  1. **Formal Accessibility Tool Designation & Service Configuration**:
     - In `AndroidManifest.xml` and `res/xml/accessibility_service_config.xml`, declare Saathi as an **Accessibility Assistance & Navigation Tool** providing visual pointing guidance and cognitive fraud assistance for seniors with digital literacy and motor/vision challenges.
     - Specify `android:accessibilityFeedbackType="feedbackSpoken|feedbackVisual"`.
  2. **Mandatory Standalone Prominent In-App Disclosure**:
     - Implement a dedicated, unbundled onboarding disclosure activity/fragment displayed **before** invoking `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
     - The disclosure clearly states in plain language:
       - Exactly what data is accessed (screen text and button positions).
       - Why it is accessed (to provide spoken guidance, draw guide arrows, and alert against deceptive fraud messages).
       - That all data is processed strictly in local device memory and **never transmitted to external servers**.
       - Requires an explicit, affirmative user tap on "I Agree & Continue".
  3. **Strict Zero-Keylogging & Zero-PII Security Firewall**:
     - In `SaathiAccessibilityService`, enforce an impenetrable interceptor:
       ```kotlin
       if (node.isPassword || node.packageName?.contains("auth") == true) {
           return // Immediately drop node without reading or logging text
       }
       ```
     - Strip all 4–6 digit numeric OTP codes via regex before any internal snapshot or log storage.
     - 100% offline-first engine architecture guarantees zero exfiltration of user screen data.
- **Falsifiable Verification Method**:
  - Review manifest metadata, XML service configuration, Prominent Disclosure UI flow, and verify via unit tests that `node.isPassword == true` immediately halts extraction.

---
## 4. Cross-Vector Compounding Failure Scenarios

In production deployment across real-world Indian households, failure modes rarely occur in isolation. Multiple technical and human fragilities interact to form compounding, multi-vector failure cascades.

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                  MULTI-VECTOR FAILURE CASCADE MATRIX                                 │
├──────────────────────────┬──────────────────────────────────────────┬────────────────────────────────┤
│ Compounding Scenario     │ Interacting Failure Modes                │ Compound Failure Result        │
├──────────────────────────┼──────────────────────────────────────────┼────────────────────────────────┤
│ A. Active Call Coercion  │ • FM-01 (OEM AutoStart Killer)           │ Coerced senior on phone with   │
│    on HyperOS with       │ • FM-11 (Split-Screen Distortion)        │ scammer in split-screen loses  │
│    Tremor Mis-Tap        │ • FM-16 (Voice Call Coercion)            │ entire bank balance in 45s due │
│                          │ • FM-17 (Motor Tremor Multi-Tap)         │ to dead service & easy dismiss.│
├──────────────────────────┼──────────────────────────────────────────┼────────────────────────────────┤
│ B. Fast-Scrolling        │ • FM-05 (Compose Split Semantics)        │ Fast-scrolling user misses     │
│    Fintech Feed with     │ • FM-06 (Flutter Dormant Bridge)         │ dynamic zero-width phishing    │
│    Zero-Width Obfuscation│ • FM-09 (Recycled Node Pointer Crash)    │ prompt; app crashes silently.  │
│                          │ • FM-14 (Zero-Width Split Attack)        │                                │
├──────────────────────────┼──────────────────────────────────────────┼────────────────────────────────┤
│ C. Sideloaded APK in     │ • FM-02 (Sideload Restricted Settings)   │ User gives up during setup or  │
│    Deep Doze with IME    │ • FM-04 (Deep Doze Network Gating)       │ gets confused by misaligned    │
│    Desync & Alert Panic  │ • FM-10 (Soft Keyboard Inset Desync)     │ pointers; false alarms induce  │
│                          │ • FM-18 (False Alarm Fatigue)            │ complete uninstallation.       │
└──────────────────────────┴──────────────────────────────────────────┴────────────────────────────────┘
```

### Cascade A: Active Call Coercion on HyperOS with Split-Screen & Tremors
1. The victim's phone (Xiaomi HyperOS) was idle for 30 minutes. The watchdog must have revived the service upon unlock (`FM-01`).
2. The victim answers a WhatsApp voice call from a scammer. The victim enters split-screen mode with PhonePe (`FM-11`).
3. The scammer sends a malicious UPI collect request. The overlay must accurately calculate split-screen offsets (`FM-11`).
4. The scammer shouts over the call to bypass the alert. Saathi detects `MODE_IN_COMMUNICATION` and enforces the 3-second hold barrier (`FM-16`).
5. The victim's trembling fingers generate multi-taps; the 450ms touch debounce prevents accidental dismissal (`FM-17`).

### Cascade B: Fast-Scrolling Dynamic Chat with Adversarial Obfuscation
1. The victim scrolls rapidly through an active chat. View recycling must not crash the service (`FM-09`).
2. The scam message uses Cyrillic homoglyphs and zero-width spaces (`FM-13`, `FM-14`).
3. Text is split across Jetpack Compose siblings (`FM-05`).
4. Saathi's spatial container aggregator unites the fragments, the normalizer strips zero-width tokens and maps homoglyphs, and the Trie flags the scam in 12ms.

---

## 5. Google Play Store Policy & Privacy Security Blueprint

To ensure complete compliance with Google Play Developer Program Policies and ensure 100% privacy preservation, Saathi adheres to the following structural principles:

### 5.1 Play Console Declared Purpose Narrative
```text
Declared Purpose:
Saathi is an Accessibility Assistant designed specifically for senior citizens, individuals with digital literacy barriers, and users with mild visual or motor impairments. Saathi utilizes the Android AccessibilityService API to:
1. Provide spoken vernacular screen guidance and high-contrast visual pointing aids to help seniors navigate complex digital interfaces.
2. Provide cognitive fraud protection by identifying deceptive patterns (e.g. unsolicited OTP sharing requests and phishing coercions) and warning vulnerable users before they complete irreversible financial actions.
```

### 5.2 Mandatory Prominent In-App Disclosure Architecture
- **Screen Location**: Standalone full-screen activity (`ProminentDisclosureActivity`) displayed immediately upon completing the language selection step.
- **Content Requirements**:
  - Clearly explains that Saathi requires `AccessibilityService` permissions to observe on-screen text and user interactions.
  - States explicitly: *"Saathi does NOT read your passwords, PINs, or financial account numbers. All processing happens 100% on your device. Zero personal data is sent to external servers."*
  - Requires explicit affirmative tap on "Accept & Continue" before launching `Settings.ACTION_ACCESSIBILITY_SETTINGS`.

### 5.3 Zero-PII & Privacy Boundary Firewall
```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               ZERO-PII SECURITY BOUNDARY                               │
│                                                                                        │
│   Incoming UI Node ──► [isPassword == true?] ──────YES─────► [DROP IMMEDIATELY]        │
│                                │                                                       │
│                                NO                                                      │
│                                ▼                                                       │
│                        [ZeroPiiSanitizer]                                              │
│                        • Strip 4-6 digit OTPs (\b\d{4,6}\b) -> [REDACTED_OTP]          │
│                        • Mask Credit/Debit Cards -> [REDACTED_CARD]                    │
│                        • Mask Phone Numbers -> [REDACTED_PHONE]                        │
│                                │                                                       │
│                                ▼                                                       │
│                        [On-Device Threat Engine] (100% Local RAM Execution)            │
│                                │                                                       │
│                                ▼                                                       │
│                        [Local Room Persistence]                                        │
│                        • Store SHA-256 Hashes Only for Audit Logs                      │
│                        • Zero Raw Text Saved to Disk or Database                       │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Subsystem Implementation Directives & Downstream Contracts

The findings of this deliberation analysis mandate concrete architectural contracts for downstream implementation milestones (M2–M4):

### 6.1 Subsystem 1: Sensory Ingestion & Service (`com.saathi.service`)
- **Class**: `SaathiAccessibilityService : AccessibilityService()`
- **Directives**:
  - `onAccessibilityEvent(event)` must execute within defensive `try-catch(Throwable)` blocks.
  - Implement recursive DFS traversal with strict bounds (`maxDepth = 32`, `maxNodes = 500`).
  - Immediately copy all node primitives into an immutable `UiNodeSnapshot` and call `.recycle()` on every node in `finally` blocks.
  - Implement `EventDebouncer` with a 200ms trailing-edge window for `TYPE_WINDOW_CONTENT_CHANGED`.
  - Check `node.isPassword` at depth 0 and immediately abort extraction if true.

### 6.2 Subsystem 2: Threat Detection Engine (`com.saathi.engine`)
- **Class**: `ScamDetectionEngine : IScamDetectionEngine`
- **Directives**:
  - Multi-stage pipeline: `ZeroPiiSanitizer` -> `TextNormalizer` -> `HomoglyphMapper` -> `LeetspeakMapper` -> `Trie / Regex Evaluator` -> `RiskEvaluator`.
  - Implement Unicode NFKD decomposition and invisible character stripping regex: `[\u200B-\u200F\uFEFF\u00AD\u202A-\u202E\u2060-\u2069]`.
  - Maintain an on-device Aho-Corasick Trie loaded with Devanagari, English, and Hinglish threat patterns.
  - Enforce per-pattern cooldowns via `AlertCooldownManager` (300s window).
  - Benchmark latency must remain <15ms on mid-range Android hardware.

### 6.3 Subsystem 3: Accessible Overlay (`com.saathi.overlay`)
- **Class**: `OverlayManager : IOverlayManager`, `SaathiOverlayView : FrameLayout`
- **Directives**:
  - Use `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
  - For visual pointers: attach with `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE`.
  - For security modals: attach with `FLAG_NOT_FOCUSABLE`.
  - Check `AudioManager.getMode()`. If `MODE_IN_CALL` or `MODE_IN_COMMUNICATION`, render the 3-second hold barrier instead of a single-tap dismiss button.
  - Enforce 56dp × 56dp minimum touch target dimensions and 450ms global touch debounce.
  - Listen to `WindowInsetsCompat.Type.ime()` to automatically adjust coordinates during soft keyboard visibility shifts.

### 6.4 Subsystem 4: Self-Healing & Watchdog (`com.saathi.watchdog`)
- **Class**: `WatchdogService : Service()` (in `:watchdog` process)
- **Directives**:
  - Maintain dual-heartbeat IPC with `SaathiAccessibilityService`.
  - Register broadcast receivers: `BootReceiver`, `PackageReplacedReceiver`, `UserPresentReceiver`.
  - Query `AccessibilityManager.getEnabledAccessibilityServiceList()` every 15s. If unbound, post high-priority re-enablement notification with deep-link intent.
  - Schedule `WorkManager` periodic keep-alive and `AlarmManager.setExactAndAllowWhileIdle()` heartbeats.

### 6.5 Subsystem 5: Persistence & Privacy (`com.saathi.data`)
- **Classes**: `AppDatabase`, `PatternDao`, `AuditLogDao`, `UserPreferencesDao`
- **Directives**:
  - Entities: `PatternEntity`, `AuditLogEntity`, `UserPreferencesEntity`.
  - In `AuditLogEntity`, store only SHA-256 hashes of matched patterns and timestamps. Never persist raw screen text or OTPs.
  - Provide pre-populated seed patterns for launch categories (OTP Theft, Bank KYC Suspension, Electricity Disconnection, Lottery Fraud, Digital Arrest).

---

## 7. Comprehensive Verification & Falsification Matrix

The following matrix establishes the objective, falsifiable verification criteria for all 20 failure modes, forming the mandatory test harness contract for Milestone M4:

| Failure Mode ID | Category | Severity | Falsifiable Verification Harness & Method | Success Metric |
|---|---|---|---|---|
| **FM-01** | OS/OEM | CRITICAL (P0) | 45-min screen-off idle test on physical Xiaomi HyperOS device; dispatch simulated WhatsApp scam on unlock. | Service revives; alert renders <250ms of unlock. |
| **FM-02** | OS/OEM | HIGH (P1) | Sideload APK onto Android 14 emulator (`adb install`); verify onboarding detects restricted state. | App displays 3-step illustrated voice bypass guide. |
| **FM-03** | OS/OEM | CRITICAL (P0) | Inject runtime `RuntimeException` in `onAccessibilityEvent`; simulate OS AMS disablement. | Zero process crash; watchdog notification within 15s. |
| **FM-04** | OS/OEM | HIGH (P1) | Force Deep Doze (`dumpsys deviceidle force-idle`) in airplane mode; ingest OTP scam text. | On-device engine detects scam in <50ms. |
| **FM-05** | A11y/Mutation | HIGH (P1) | Render 3 split `Text()` composables in Compose layout representing split KYC threat. | Spatial aggregator matches threat with confidence >0.8. |
| **FM-06** | A11y/Mutation | CRITICAL (P0) | Dispatch synthetic focus probe to an empty `FlutterView` in integration test. | Flutter semantics tree populates within 150ms. |
| **FM-07** | A11y/Mutation | HIGH (P1) | Traverse simulated `GLSurfaceView` layout spanning >60% screen area. | Detects opaque canvas; emits warning event. |
| **FM-08** | A11y/Mutation | HIGH (P1) | Inject dynamic DOM modal in WebView 350ms after window load. | Event debouncer captures mutated DOM within 500ms. |
| **FM-09** | A11y/Mutation | CRITICAL (P0) | Fling 1000-item `RecyclerView` at 60fps while extracting 10,000 snapshots. | Exactly 0 unhandled exceptions; 0 memory leaks. |
| **FM-10** | A11y/Mutation | MEDIUM (P2) | Open soft keyboard (Gboard) over target input field in UI Automator test. | Overlay pointer shifts to panned bounds in <100ms. |
| **FM-11** | A11y/Mutation | MEDIUM (P2) | Test overlay rendering in 50/50 split-screen mode on emulator. | Indicator circle aligns within +/- 2dp of button. |
| **FM-12** | A11y/Mutation | HIGH (P1) | Inject 500 `TYPE_WINDOW_CONTENT_CHANGED` events in 5s. | CPU < 5%; max 5 traversals/sec; 0 dropped alerts. |
| **FM-13** | Adversary | CRITICAL (P0) | Test 50 Cyrillic/Greek homoglyph variants of `"OTP"` and `"SBI"`. | 100% normalized to ASCII; 100% scam detection rate. |
| **FM-14** | Adversary | CRITICAL (P0) | Ingest `"O\u200BT\u200CP"` and `"ओ\u200Bटी\u200Cपी"` with zero-width characters. | 100% stripped cleanly; matches OTP rule. |
| **FM-15** | Adversary | HIGH (P1) | Ingest 30 Leetspeak & Hinglish code-mixed fraud phrases (`0-T-P`, `khata block`). | 100% detection rate across benchmark suite. |
| **FM-16** | Adversary | CRITICAL (P0) | Simulate `AudioManager.mode == MODE_IN_CALL`; trigger high-risk OTP threat. | Enforces 3s hold barrier; plays loudspeaker audio. |
| **FM-17** | Elderly UX | HIGH (P1) | Inject 5 touch events spaced 40ms apart on overlay action button. | Exactly 1 single debounced click dispatches. |
| **FM-18** | Elderly UX | HIGH (P1) | Receive login OTP inside whitelisted banking package `com.phonepe.app`. | Zero modal interruptions; logged to audit only. |
| **FM-19** | Elderly UX | MEDIUM (P2) | Initialize TTS across English, Hindi, Tamil, Bengali locales. | Speech rate <= 0.85f; pitch <= 0.95f verified. |
| **FM-20** | Compliance | CRITICAL (P0) | Inspect AndroidManifest, A11y XML config, disclosure UI, and test `node.isPassword`. | Full disclosure present; password node skipped. |

---
*End of Deliberation & Failure Mode Analysis — Produced for Saathi Android Engineering Core.*
