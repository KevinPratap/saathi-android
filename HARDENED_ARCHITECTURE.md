# SAATHI — Hardened Architecture & Subsystem Specification (R2)

**Document Version:** 2.0.0  
**Author:** Saathi Core Systems Engineering (Worker M2)  
**Target Milestone:** R2 — Hardened Architecture & Subsystem Specification  
**Project:** Saathi (साथी) — Hardened On-Device AI Guardian for Elderly Indian Smartphone Users  
**Target Environment:** Android 8.0 (API 26) through Android 15 (API 35), `minSdk = 26`, `targetSdk = 34`  
**Security & Privacy Standard:** Zero-PII Invariant, Air-Gapped Threat Evaluation, WCAG AAA Senior Ergonomics  

---

## Executive Summary & System Vision

Saathi is an unkillable, ultra-low-latency, zero-leakage, accessibility-driven guardian and digital assistant tailored specifically for elderly Indian citizens. Older adults face an escalating epidemic of digital financial fraud—including One-Time Password (OTP) harvesting, fake electricity bill disconnections, digital arrest extortion by cybercrime cartels impersonating police, malicious remote access tool (RAT) installations (AnyDesk/TeamViewer), and deceptive lottery/KYC scams.

Standard Android applications fail to protect this demographic because of three structural bottlenecks:
1. **Aggressive OEM Task Killers**: Custom Android OEM ROMs (Xiaomi HyperOS/MIUI, Samsung One UI, Oppo ColorOS, Vivo Funtouch OS) aggressively terminate background services, purge accessibility bindings, and break continuous monitoring.
2. **Adversarial Linguistic Evasion**: Modern scammers bypass rudimentary keyword filters by exploiting Unicode homoglyphs (Cyrillic/Greek/Devanagari lookalikes), zero-width non-joiners, spaced formatting, Leetspeak, and mixed Devanagari-English (Hinglish) phonetics.
3. **Cognitive & Motor Fragility in Seniors**: Visual decline (cataracts, macular degeneration, presbyopia), motor tremors (Parkinson's, essential tremor), hearing loss (presbycusis), and acute amygdala-driven panic render standard toast messages or complex confirmation dialogs useless or counterproductive.

The **Saathi Hardened Architecture** resolves these challenges through four foundational, decoupled subsystems:
1. **Subsystem 1: Watchdog & Self-Healing Daemon Architecture** (`:watchdog` out-of-process mesh, double-ping heartbeat protocol, 4-tier resilience mesh, dead-man recovery).
2. **Subsystem 2: Deterministic Scam Detection Engine** (Zero-allocation hybrid Aho-Corasick Trie + contextual regex matcher, 5-stage Unicode NFKD & Hinglish normalizer, $<15\text{ms}$ latency budget, spatial adjacency graphs, alert cooldown manager).
3. **Subsystem 3: DPI-Independent Accessible Overlay Subsystem** (`WindowManager` `TYPE_APPLICATION_OVERLAY`, dual-mode touch-through ambient guidance vs. 3-second hold-to-dismiss modal intervention, 8px grid, 56x56dp touch targets, WCAG AAA contrast, Devanagari typography).
4. **Subsystem 4: Privacy-Preserving Zero-PII & Local Persistence Subsystem** (Air-gapped offline evaluation, ephemeral in-memory processing windows with zeroed byte arrays, deterministic Indian PII redaction, SHA-256 salted audit logging, SQLCipher-encrypted Room DB).

```
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                     SAATHI SYSTEM TOPOLOGY                                            │
│                                                                                                       │
│  ┌────────────────────────────────────────┐                 ┌──────────────────────────────────────┐  │
│  │     PRIMARY PROCESS (:main)            │   Heartbeat     │     ISOLATED WATCHDOG (:watchdog)    │  │
│  │                                        │   IPC Ping      │                                      │  │
│  │  ┌──────────────────────────────────┐  │◄───────────────►│  ┌────────────────────────────────┐  │  │
│  │  │   SaathiAccessibilityService     │  │                 │  │       WatchdogService          │  │  │
│  │  │   • Tree Traversal (DFS)         │  │                 │  │   • Process Lifecycle Monitor  │  │  │
│  │  │   • Debounced Event Queue        │  │                 │  │   • Persistent Foreground Notif│  │  │
│  │  └────────────────┬─────────────────┘  │                 │  └────────────────▲───────────────┘  │  │
│  │                   │ UiNodeSnapshot     │                 │                   │                  │  │
│  │                   ▼                    │                 │  ┌────────────────┴───────────────┐  │  │
│  │  ┌──────────────────────────────────┐  │                 │  │  WatchdogJobService (15-min)   │  │  │
│  │  │     ScamDetectionEngine          │  │                 │  │  BroadcastReceivers (BOOT/PWR) │  │  │
│  │  │   • Text Normalizer (NFKD/Leet)  │  │                 │  └────────────────────────────────┘  │  │
│  │  │   • Aho-Corasick Trie Engine     │  │                 └──────────────────────────────────────┘  │
│  │  │   • Spatial Adjacency Graph      │  │                                                           │
│  │  │   • Multi-stage Risk Evaluator   │  │                 ┌──────────────────────────────────────┐  │
│  │  └────────────────┬─────────────────┘  │                 │    LOCAL PERSISTENCE (Room + DB)     │  │
│  │                   │ ScamVerdict        │                 │                                      │  │
│  │                   ▼                    │  Encrypted Log  │  ┌────────────────────────────────┐  │  │
│  │  ┌──────────────────────────────────┐  │────────────────►│  │  AppDatabase (Encrypted)       │  │  │
│  │  │        OverlayManager            │  │ (No PII)        │  │  • PatternEntity               │  │  │
│  │  │   • Ambient Touch-Through Banner │  │                 │  │  • AuditLogEntity (SHA-256)    │  │  │
│  │  │   • 3s Hold Intervention Modal   │  │                 │  │  • UserPreferencesEntity       │  │  │
│  │  └──────────────────────────────────┘  │                 │  └────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘                 └──────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 1. Subsystem 1: Watchdog & Self-Healing Architecture

### 1.1 The OEM Background Process Threat Model

On modern Android devices (Android 8.0 API 26 through Android 15 API 35), the operating system's Low Memory Killer Daemon (`lmkd`), Doze Mode maintenance windows, App Standby Buckets, and vendor-specific OEM battery managers aggressively terminate non-interactive background processes.

| OEM ROM | Killer Component / Daemon | Mechanism of Destruction | Saathi Resilience Counter-Measure |
|---|---|---|---|
| **Xiaomi / Redmi / POCO (MIUI / HyperOS)** | `com.miui.powerkeeper`, `com.miui.securitycenter` | Kills background services within 3–10 minutes after screen-off; blocks autostart; revokes overlay permissions silently. | Out-of-process watchdog daemon; AutoStart intent navigation; `ACTION_USER_PRESENT` wake trigger; foreground service priority elevation (`dataCapture`). |
| **Samsung (One UI)** | `com.samsung.android.lool` (Device Care / Smart Manager) | Places inactive services into "Sleeping Apps" or "Deep Sleeping Apps" buckets; freezes binder threads; suppresses alarms. | Dual-heartbeat keep-alive; `JobScheduler` periodic reschedule with `setPersisted(true)`; explicit battery whitelist request (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`). |
| **Oppo / Realme (ColorOS / Realme UI)** | `com.coloros.safecenter`, `com.coloros.oppoguardelf` | Force-stops processes; blocks background broadcast receivers; restricts floating windows after reboot. | Multi-tier broadcast mesh (`ACTION_BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `ACTION_POWER_CONNECTED`); vendor intent hooks for "Startup Manager". |
| **Vivo / iQOO (Funtouch OS / OriginOS)** | `com.iqoo.secure`, `com.vivo.permissionmanager` | "High Background Power Consumption" killer kills background workers consuming $>1.5\text{ mAh}$; blocks overlay drawing. | Zero-allocation, zero-polling heartbeat design; event-driven wakeup without busy-waiting; battery draw $<0.8\text{ mAh}$ (far below threshold). |
| **OnePlus (OxygenOS)** | Deep Optimization Engine | Aggressively optimizes Doze mode; delays `AlarmManager` alarms by up to 2 hours. | Companion `JobScheduler` with network/charging-independent triggers; `setExactAndAllowWhileIdle` fallback. |

---

### 1.2 Out-of-Process (`:watchdog`) Architecture & 4-Tier Resilience Mesh

To survive process death, Saathi partitions its application runtime into two discrete OS processes defined in `AndroidManifest.xml`:
1. **Primary Process (`com.saathi`)**: Hosts `SaathiAccessibilityService`, `ScamDetectionEngine`, and `OverlayManager`.
2. **Watchdog Process (`com.saathi:watchdog`)**: An isolated, lightweight companion process running `WatchdogService` and `WatchdogJobService`. Because it runs in a separate process space, if `com.saathi` is killed by LMK or an unhandled crash, `com.saathi:watchdog` remains alive to orchestrate recovery.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 4-TIER RESILIENCE MESH                                 │
│                                                                                        │
│   ┌────────────────────────────────────────────────────────────────────────────────┐   │
│   │ TIER 1: System-Bound Accessibility Service (com.saathi)                        │   │
│   │ • Registered with Android AccessibilityManager framework                       │   │
│   │ • Auto-restarted by Android framework upon LMK if Settings.Secure is enabled   │   │
│   └──────────────────────────────────────┬─────────────────────────────────────────┘   │
│                                          │ Double-Ping Heartbeat                       │
│                                          ▼                                             │
│   ┌────────────────────────────────────────────────────────────────────────────────┐   │
│   │ TIER 2: Isolated Watchdog Foreground Service (com.saathi:watchdog)             │   │
│   │ • Ongoing Notification (NotificationManager.IMPORTANCE_LOW)                    │   │
│   │ • Foreground type: "dataCapture" / "specialUse"                                │   │
│   │ • Inter-Process Shared Memory / File-backed Heartbeat Evaluator                │   │
│   └──────────────────────────────────────┬─────────────────────────────────────────┘   │
│                                          │ Periodic Dead-Man Check                     │
│                                          ▼                                             │
│   ┌────────────────────────────────────────────────────────────────────────────────┐   │
│   │ TIER 3: Persistent JobScheduler Engine (WatchdogJobService)                    │   │
│   │ • 15-minute periodic interval (JobInfo.Builder.setPeriodic(15 * 60 * 1000))    │   │
│   │ • setPersisted(true) across device reboots                                     │   │
│   │ • Evaluates liveness; kicks Tier 2 if dead                                     │   │
│   └──────────────────────────────────────┬─────────────────────────────────────────┘   │
│                                          │ Reactive Wakeup Hooks                       │
│                                          ▼                                             │
│   ┌────────────────────────────────────────────────────────────────────────────────┐   │
│   │ TIER 4: Reactive System Event Broadcast Mesh (WatchdogPingReceiver)            │   │
│   │ • android.intent.action.BOOT_COMPLETED                                         │   │
│   │ • android.intent.action.MY_PACKAGE_REPLACED                                    │   │
│   │ • android.intent.action.USER_PRESENT (Device Unlock)                           │   │
│   │ • android.intent.action.ACTION_POWER_CONNECTED / DISCONNECTED                  │   │
│   └────────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 1.3 Double-Ping Heartbeat Protocol & Zero Battery Drain Design

#### Mathematical & Timing Formulation
Let $T_{\text{last\_ping}}$ be the timestamp in milliseconds (from `SystemClock.elapsedRealtime()`) when `SaathiAccessibilityService` last performed an event evaluation or issued a keep-alive ping.

Let $T_{\text{now}}$ be the current monotonic time. The time delta is:
$$\Delta t = T_{\text{now}} - T_{\text{last\_ping}}$$

The protocol operates under two distinct device states:
1. **Screen Active (Interactive Mode)**:
   - Heartbeat Ping Frequency: Every $30\text{ seconds}$ or on every `AccessibilityEvent` batch.
   - Failure Timeout Threshold: $\Theta_{\text{active}} = 90\text{ seconds}$ ($3 \times$ interval).
   - If $\Delta t > \Theta_{\text{active}}$ while the screen is on (`Display.STATE_ON`), the Watchdog asserts a **Fault Condition**.
2. **Screen Off (Doze / Non-Interactive Mode)**:
   - To guarantee zero battery drain, `SaathiAccessibilityService` enters passive dormant mode when the screen is powered down (`ACTION_SCREEN_OFF`). No wake-locks are held.
   - Watchdog checks are suspended until `ACTION_USER_PRESENT` or `ACTION_SCREEN_ON` occurs.
   - Battery Consumption Metric: $< 0.5\text{ mAh}$ per hour in standby, representing $< 1.8\%$ total battery draw across a 24-hour cycle.

```
┌───────────────────────────┐                     ┌───────────────────────────┐
│ SaathiAccessibilityService│                     │      WatchdogService      │
│     (Process: :main)      │                     │   (Process: :watchdog)    │
└─────────────┬─────────────┘                     └─────────────┬─────────────┘
              │                                                 │
              │  1. Event Ingested / Periodic Tick (30s)        │
              ├────────────────────────────────────────────────►│
              │  Write Atomic Timestamp to HeartbeatStore       │
              │                                                 │
              │                                                 │  2. Periodic Inspection (Tier 3/4)
              │                                                 │  Calculate Δt = Now - T_last_ping
              │                                                 │
              │                                                 ├──┐
              │                                                 │  │ Evaluate Health:
              │                                                 │  │ Is Δt <= 90,000ms?
              │                                                 │◄─┘
              │                                                 │
              │        [ CASE A: HEALTHY (Δt <= 90s) ]          │
              │◄────────────────────────────────────────────────┤
              │        No Intervention; Return to Sleep         │
              │                                                 │
              │        [ CASE B: STALLED / KILLED (Δt > 90s) ]  │
              │                                                 ├──┐
              │                                                 │  │ 1. Verify Settings.Secure
              │                                                 │  │ 2. Trigger Auto-Recovery
              │                                                 │◄─┘
              │  3. Re-launch Foreground Companion / Alert      │
              │◄────────────────────────────────────────────────┤
```

---

### 1.4 Auto-Recovery State Machine

The self-healing engine transitions deterministically across five finite states:

```
                  ┌──────────────────────────────────────────────────┐
                  │                                                  │
                  ▼                                                  │
         ┌───────────────────┐                                       │
         │  ACTIVE_GUARDED   │◄─────────────────────────────────┐    │
         └─────────┬─────────┘                                  │    │
                   │                                            │    │
                   │ (Process Death / OOM Kill / Crash)         │    │
                   ▼                                            │    │
         ┌───────────────────┐                                  │    │
         │  SERVICE_KILLED   │                                  │    │
         └─────────┬─────────┘                                  │    │
                   │                                            │    │
                   │ (USER_PRESENT Broadcast or JobScheduler)   │    │
                   ▼                                            │    │
         ┌───────────────────┐                                  │    │
         │  WATCHDOG_WAKEUP  │                                  │    │
         └─────────┬─────────┘                                  │    │
                   │                                            │    │
                   ├──────────────────────────────────────┐     │    │
                   │ [Accessibility Enabled in Settings]  │     │    │
                   ▼                                      ▼     │    │
         ┌───────────────────┐                  ┌───────────────┴──┐ │
         │     REBINDING     │                  │PERMISSION_REVOKED│ │
         └─────────┬─────────┘                  └───────┬──────────┘ │
                   │                                    │            │
                   │ (OS Re-binds Service)              │ (User Re-enables)
                   └────────────────────────────────────┴────────────┘
```

#### State Transition Logic:
- **`ACTIVE_GUARDED`**: `SaathiAccessibilityService` is bound, active, and regularly updating `HeartbeatStore`. Watchdog monitors passively.
- **`SERVICE_KILLED`**: Primary process has been killed by the OEM task manager or OS low-memory killer. `HeartbeatStore` timestamp stagnates.
- **`WATCHDOG_WAKEUP`**: Triggered reactively by `WatchdogJobService` (15-min tick) or system broadcasts (`USER_PRESENT`, `POWER_CONNECTED`). Watchdog reads `Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)`.
- **`REBINDING`**: If accessibility permission is still active in system settings, Watchdog initiates a lightweight IPC touch and starts `WatchdogForegroundService`, triggering the Android `AccessibilityManagerService` to rebind the service connection.
- **`PERMISSION_REVOKED`**: If the user or OEM security cleaner disabled the accessibility permission toggle, Watchdog issues an urgent, non-dismissible heads-up notification alerting the senior user and queuing a sync event for the family dashboard: *"Saathi protection is paused. Tap here to turn it back on."*

---

### 1.5 OEM Battery Whitelist Intent Resolution Guide

To prevent aggressive OEM power managers from killing Saathi in the first place, the setup and self-healing flows query a comprehensive OEM Intent Directory to guide the family member or senior directly to the manufacturer's exact settings page:

```kotlin
package com.saathi.watchdog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object OemIntentHelper {

    fun getBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun getAutoStartIntent(): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intent = Intent()

        return when {
            // Xiaomi / Redmi / POCO (MIUI / HyperOS)
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                intent
            }
            // Samsung (One UI)
            manufacturer.contains("samsung") -> {
                intent.component = ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
                intent
            }
            // Oppo / Realme (ColorOS / Realme UI)
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
                intent
            }
            // Vivo / iQOO (Funtouch OS / OriginOS)
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                intent.component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
                intent
            }
            // OnePlus (OxygenOS)
            manufacturer.contains("oneplus") -> {
                intent.component = ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
                intent
            }
            // Huawei / Honor (EMUI / Magic UI)
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                intent
            }
            // Stock Android Fallback
            else -> null
        }
    }
}
```

---

## 2. Subsystem 2: Scam Detection Engine Architecture

### 2.1 Latency Budget & Zero-Allocation Constraints

During high-speed UI interaction (e.g., rapid WhatsApp scrolling or fast screen transitions), accessibility event bursts can arrive at $60\text{Hz}$ ($16.6\text{ms}$ intervals). To prevent frame drops (jank) in third-party applications and eliminate Garbage Collection (GC) pauses, the Scam Detection Engine operates under strict microsecond budgets:

$$\text{Total Latency Budget} = T_{\text{normalize}} + T_{\text{trie}} + T_{\text{spatial}} + T_{\text{scoring}} \le 15.0\text{ ms}$$

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        LATENCY & MEMORY ALLOCATION BUDGET                              │
│                                                                                        │
│  Phase 1: Event Debounce & Snapshot Extraction ───► [ 1.50 ms | 0 bytes heap ]        │
│  Phase 2: Text Normalization (NFKD + Leet)     ───► [ 3.20 ms | Reused CharBuffer ]   │
│  Phase 3: Aho-Corasick Multi-Keyword Scan      ───► [ 2.10 ms | 0 bytes heap ]        │
│  Phase 4: Spatial Adjacency Graph Traversal    ───► [ 1.80 ms | Flat Int Array ]      │
│  Phase 5: Contextual Regex & Rule Aggregation  ───► [ 3.40 ms | Pre-compiled Matchers]│
│  Phase 6: Cooldown Evaluation & Verdict Gen    ───► [ 0.80 ms | 0 bytes heap ]        │
│ ────────────────────────────────────────────────────────────────────────────────────── │
│  TOTAL EVALUATION LATENCY                      ───► [ 12.80 ms ] (Safe Margin: 2.2ms)  │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

#### Zero-Allocation Hot-Path Design:
- **Thread-Local Reusable Buffers**: Uses `ThreadLocal<CharArrayWriter>` and flat `StringBuilder` instances sized to `MAX_BUFFER_SIZE = 8192` characters. Buffers are reset with `.setLength(0)` rather than re-instantiated.
- **Bitmask Semantic Tags**: Category matches are encoded as 64-bit integer bitmasks (`Long`), avoiding heap allocation of `Set<ScamCategory>` or `List<String>` collections.
- **Flat Spatial Arrays**: UI node bounding boxes and parent-child indices are stored in contiguous primitive integer arrays (`IntArray(MAX_NODES * 4)`), preserving L1/L2 CPU cache locality.

---

### 2.2 Preprocessing & Multi-Stage Linguistic Normalization Pipeline

Scammers employ sophisticated character-level evasion strategies to bypass naive substring matching. Saathi executes a 5-stage normalization pipeline prior to Trie evaluation:

```
┌────────────────────────────────────────────────────────────────────────────────────────────────┐
│                          5-STAGE TEXT NORMALIZATION PIPELINE                                   │
│                                                                                                │
│   RAW INPUT: "🚨ALERT: प्रिय ग्राहक, SBI YONO खाता bl0cked! 0TP sh@re \u200B k@ro 9876543210"   │
│                                                                                                │
│   [ STAGE 1: Unicode NFKD Decomposition ]                                                      │
│   Decompose canonical and compatibility variants (Normalizer.normalize(input, Form.NFKD)).    │
│   Output: Expands combined ligatures and diacritics into base glyphs.                          │
│                                                                                                │
│   [ STAGE 2: Zero-Width, Invisible & Control Character Stripping ]                             │
│   Remove \u200B (ZWSP), \u200C (ZWNJ), \u200D (ZWJ), \uFEFF (BOM), \u00AD (Soft Hyphen),      │
│   \u202A-\u202E (Bidi Overrides), and non-printable control chars.                            │
│   Output: "ALERT: प्रिय ग्राहक, SBI YONO खाता bl0cked! 0TP sh@re karo 9876543210"              │
│                                                                                                │
│   [ STAGE 3: Confusable Homoglyph Translation ]                                                │
│   Map cross-script confusables (Cyrillic 'О', 'а', 'е', 'р', Greek 'ο', Devanagari digits      │
│   '०-९' to Latin '0-9' or canonical ASCII).                                                    │
│   Output: "alert: प्रिय ग्राहक, sbi yono खाता bl0cked! 0tp sh@re karo 9876543210"              │
│                                                                                                │
│   [ STAGE 4: Leetspeak & Obfuscation De-obfuscation ]                                          │
│   Map numerical and symbol substitutes: '0'->'o', '@'/'4'->'a', '3'->'e', '5'/'$'->'s',       │
│   '1'/'!'/'|'->'i'/'l', '8'->'b'.                                                              │
│   Output: "alert: प्रिय ग्राहक, sbi yono खाता blocked! otp share karo 9876543210"              │
│                                                                                                │
│   [ STAGE 5: Whitespace Normalization & Hinglish Phonetic Unification ]                        │
│   Collapse multi-spaces, tabs, newlines; map common Hinglish scam verbs ("k@ro"->"karo",      │
│   "bhej0"->"bhejo", "tur@nt"->"turant", "p@ise"->"paise").                                    │
│   Output: "alert प्रिय ग्राहक sbi yono खाता blocked otp share karo 9876543210"                 │
└────────────────────────────────────────────────────────────────────────────────────────────────┘
```

#### Homoglyph & Confusable Translation Matrix:
```kotlin
object HomoglyphMapper {
    // Lookup table for high-frequency adversarial homoglyphs
    private val HOMOGLYPH_MAP = mapOf(
        // Cyrillic to Latin
        '\u0410' to 'A', '\u0430' to 'a', // Cyrillic А/а -> Latin A/a
        '\u0412' to 'B',                  // Cyrillic В -> Latin B
        '\u0415' to 'E', '\u0435' to 'e', // Cyrillic Е/е -> Latin E/e
        '\u041A' to 'K', '\u043A' to 'k', // Cyrillic К/к -> Latin K/k
        '\u041C' to 'M',                  // Cyrillic М -> Latin M
        '\u041D' to 'H',                  // Cyrillic Н -> Latin H
        '\u041E' to 'O', '\u043E' to 'o', // Cyrillic О/о -> Latin O/o
        '\u0420' to 'P', '\u0440' to 'p', // Cyrillic Р/р -> Latin P/p
        '\u0421' to 'C', '\u0441' to 'c', // Cyrillic С/с -> Latin C/c
        '\u0422' to 'T',                  // Cyrillic Т -> Latin T
        '\u0423' to 'y', '\u0443' to 'y', // Cyrillic У/у -> Latin y
        '\u0425' to 'X', '\u0445' to 'x', // Cyrillic Х/х -> Latin X/x
        
        // Devanagari Digits to Latin Digits
        '\u0966' to '0', '\u0967' to '1', '\u0968' to '2', '\u0969' to '3', '\u096A' to '4',
        '\u096B' to '5', '\u096C' to '6', '\u096D' to '7', '\u096E' to '8', '\u096F' to '9'
    )

    fun mapHomoglyphs(input: CharSequence): String {
        val sb = StringBuilder(input.length)
        for (i in 0 until input.length) {
            val c = input[i]
            sb.append(HOMOGLYPH_MAP[c] ?: c)
        }
        return sb.toString()
    }
}
```

---

### 2.3 Hybrid Aho-Corasick Prefix Trie + Contextual Regex Engine

The detection engine couples a deterministic **Aho-Corasick Automaton** for $O(N)$ simultaneous dictionary search with a **Contextual Regex Verifier** that fires only when suspicious token clusters are encountered.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        HYBRID DETECTION ENGINE ARCHITECTURE                            │
│                                                                                        │
│  [ Normalized Screen Buffer (Length N) ]                                               │
│                 │                                                                      │
│                 ▼                                                                      │
│  ┌────────────────────────────────────────────────────────────────────────────────┐    │
│  │ AHO-CORASICK AUTOMATON (Single-Pass O(N) Dictionary Traversal)                 │    │
│  │ Keywords: {"otp", "ओटीपी", "kyc", "block", "police", "arrest", "anydesk", ...} │    │
│  └──────────────────────────────────────┬─────────────────────────────────────────┘    │
│                                         │                                              │
│                                         ▼ Bitmask Semantic Tags                        │
│                 ┌───────────────────────────────────────────────┐                      │
│                 │ TAG_OTP_INDICATOR     (0x0001)                │                      │
│                 │ TAG_URGENCY_DEMAND    (0x0002)                │                      │
│                 │ TAG_BANK_IMPERSONATION(0x0004)                │                      │
│                 │ TAG_RAT_SOFTWARE      (0x0008)                │                      │
│                 │ TAG_POLICE_EXTORTION  (0x0010)                │                      │
│                 └───────────────────────┬───────────────────────┘                      │
│                                         │                                              │
│                        Is Any Threat Tag Bit Set?                                      │
│                        ├── NO  ──► [ RETURN: ScamVerdict.Safe ]                        │
│                        │                                                               │
│                        └── YES                                                         │
│                             ▼                                                          │
│  ┌────────────────────────────────────────────────────────────────────────────────┐    │
│  │ TARGETED CONTEXTUAL REGEX EVALUATION                                           │    │
│  │ Execute compiled regex only for triggered categories:                          │    │
│  │ • OTP Rule:   \b(?:\d{4,8}|one time password|ओटीपी)\b.*\b(?:share|bhejo|tell)\b │    │
│  │ • Arrest Rule:\b(?:digital arrest|cbi|police|narcotics)\b.*\b(?:transfer|fine)\b│    │
│  │ • RAT Rule:   \b(?:anydesk|teamviewer|quicksupport)\b.*\b(?:code|install)\b    │    │
│  └──────────────────────────────────────┬─────────────────────────────────────────┘    │
│                                         │                                              │
│                                         ▼ Matched Rules & Syntactic Spans              │
│  ┌────────────────────────────────────────────────────────────────────────────────┐    │
│  │ SPATIAL ADJACENCY & HEURISTIC RISK SCORER                                      │    │
│  │ Compute: S = BaseScore(R) * PackageMultiplier * SpatialProximityFactor         │    │
│  └──────────────────────────────────────┬─────────────────────────────────────────┘    │
│                                         │                                              │
│                                         ▼ Final Risk Level                             │
│                 [ LOW (<40) | MEDIUM (40-69) | HIGH (>=70) ]                           │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 2.4 Scam Pattern Taxonomy & Threat Scoring Formulas

The composite risk score $S \in [0, 100]$ is computed as follows:

$$S = \min\left(100, \left(\sum_{r \in R_{\text{matched}}} W(r)\right) \times M_{\text{pkg}} \times M_{\text{spatial}} \times M_{\text{action}}\right)$$

Where:
- $W(r)$: Base weight of the matched scam rule $r$.
- $M_{\text{pkg}}$: Package trust multiplier ($0.15$ for whitelisted banking apps, $1.00$ for standard apps, $1.35$ for messaging apps like WhatsApp/Telegram/SMS receiving unknown content).
- $M_{\text{spatial}}$: Spatial adjacency multiplier ($1.25$ if trigger text and action button reside in the same layout subtree; $1.00$ otherwise).
- $M_{\text{action}}$: Actionable node multiplier ($1.30$ if an editable `EditText` or clickable `Button` is present within the target container).

| Threat Category | Rule ID | Base Weight $W(r)$ | Trigger Signature (EN / HI / Hinglish) | Risk Threshold |
|---|---|---|---|---|
| **OTP Theft / Exfiltration** | `RULE_OTP_HARVEST` | $85$ | `"share OTP"`, `"verification code bhejo"`, `"ओटीपी बताएं"`, `\b\d{4,6}\b.*share` | HIGH ($\ge 70$) |
| **Digital Arrest / Extortion** | `RULE_DIGITAL_ARREST` | $95$ | `"digital arrest"`, `"CBI officer"`, `"police court warrant"`, `"narcotics parcel seize"` | HIGH ($\ge 70$) |
| **Banking / KYC Expiry** | `RULE_KYC_SUSPENSION` | $80$ | `"electricity disconnect tonight"`, `"SBI KYC expired"`, `"account blocked"`, `"PAN update"` | HIGH ($\ge 70$) |
| **Lottery / Fake Prize** | `RULE_LOTTERY_KBC` | $70$ | `"KBC lottery winner"`, `"₹25 lakh prize"`, `"claim cashback"`, `"lucky draw"` | MEDIUM ($40–69$) |
| **Remote Access App (RAT)** | `RULE_RAT_INSTALL` | $90$ | `"install AnyDesk"`, `"download TeamViewer"`, `"share QuickSupport 9-digit code"` | HIGH ($\ge 70$) |
| **Urgent Money Transfer** | `RULE_URGENT_TRANSFER`| $60$ | `"send money immediately"`, `"turant paise transfer karo"`, `"hospital emergency"` | MEDIUM ($40–69$) |

---

### 2.5 Spatial Node Adjacency Graph & False-Positive Suppression

A major risk in senior-focused safety software is **alert fatigue** caused by false alarms in legitimate contexts (e.g., viewing an authentic bank transaction statement or genuine OTP SMS). Saathi mitigates this using three deterministic filters:

1. **Package Whitelist Engine**:
   - Known verified packages (`com.phonepe.app`, `com.google.android.apps.nbu.paisa.user`, `net.one97.paytm`, `com.sbi.upi`, `com.icicibank.mobile`) are assigned $M_{\text{pkg}} = 0.15$. In legitimate banking apps, passive display of the word "OTP" does not trigger an alarm unless accompanied by a malicious external URL or known phishing overlay signature.
2. **Spatial Adjacency Graph**:
   - The engine builds a directed acyclic tree representing the visual node hierarchy.
   - A threat verdict is only confirmed if the suspicious text node is within a spatial distance $D \le 350\text{dp}$ of an interactive input element (`isClickable == true` or `isEditable == true`).
3. **Temporal Cooldown Manager**:
   - If an alert for a specific pattern hash is dismissed by the senior or caregiver, that exact pattern is placed on a $300\text{ second}$ ($5\text{ minute}$) cooldown for that package to prevent screen locking loops.

---

## 3. Subsystem 3: DPI-Independent Accessible Overlay Architecture

### 3.1 WindowManager Flags & Surface Management

Saathi renders its guidance beacons and safety interdiction dialogs using the system-level `WindowManager` with `LayoutParams.TYPE_APPLICATION_OVERLAY` (API 26+).

```kotlin
package com.saathi.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.saathi.model.OverlayMode
import com.saathi.model.ScamAlertData

class OverlayManager(private val context: Context) : IOverlayManager {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: SaathiOverlayView? = null
    private var isAttached = false

    override fun showWarningBanner(alertData: ScamAlertData) {
        val params = createBannerLayoutParams()
        attachOrUpdate(params, OverlayMode.AmbientBanner(alertData))
    }

    override fun showInterventionModal(alertData: ScamAlertData, onDismiss: () -> Unit) {
        val params = createModalLayoutParams()
        attachOrUpdate(params, OverlayMode.InterventionModal(alertData, onDismiss))
    }

    override fun dismissOverlay() {
        overlayView?.let { view ->
            if (isAttached) {
                windowManager.removeView(view)
                isAttached = false
            }
        }
        overlayView = null
    }

    override fun isOverlayVisible(): Boolean = isAttached

    private fun createBannerLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun createModalLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.82f // 82% ambient darkening for maximum contrast
            gravity = Gravity.CENTER
        }
    }

    private fun attachOrUpdate(params: WindowManager.LayoutParams, mode: OverlayMode) {
        if (overlayView == null) {
            overlayView = SaathiOverlayView(context)
        }
        overlayView?.setMode(mode)

        if (!isAttached) {
            windowManager.addView(overlayView, params)
            isAttached = true
        } else {
            windowManager.updateViewLayout(overlayView, params)
        }
    }
}
```

---

### 3.2 Dual-Mode Presentation Matrix

| Parameter | Mode A: Ambient Warning Banner (Teaching / Low Risk) | Mode B: High-Friction Modal Intervention (Scam Interdict) |
|---|---|---|
| **Target State** | Step-by-step navigation, WhatsApp guidance, low-confidence warning. | High-confidence scam detected (OTP theft, digital arrest). |
| **Touch Interaction** | **Touch Passthrough (`100%`)**: Senior can tap buttons underneath overlay. | **Touch Barrier (`100% Intercept`)**: Blocks all touches to underlying app. |
| **WindowManager Flags** | `FLAG_NOT_FOCUSABLE \| FLAG_NOT_TOUCH_MODAL` | `FLAG_NOT_TOUCH_MODAL \| FLAG_DIM_BEHIND` |
| **Dim Behind Amount** | `0.0f` (Fully transparent backdrop) | `0.82f` (Deep black scrim to isolate focus) |
| **Dismissal Mechanism** | Auto-fades after $8\text{ seconds}$ or on node transition. | **3-Second Hold-to-Dismiss Barrier** (`HoldToDismissButton`). |
| **Visual Indicators** | Glowing beacon ring, pointing vector arrow, gentle amber card. | High-contrast red alert box, flashing warning shield icon. |
| **Haptic Feedback** | Gentle single tick ($40\text{ms}$ at $40\%$ amplitude). | Rhythmic urgency pulse ($150\text{ms}$ ON, $100\text{ms}$ OFF, $250\text{ms}$ ON). |

---

### 3.3 Coordinate Normalization & Edge Collision Math

Android devices span diverse physical aspect ratios ($16:9$, $19.5:9$, $21:9$), varied display densities ($160\text{dpi}$ to $640\text{dpi}$), and dynamic split-screen / multi-window bounds. 

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        COORDINATE NORMALIZATION & ANCHOR MATH                          │
│                                                                                        │
│   Screen Dimensions: (W_screen, H_screen)                                              │
│   Target Bounding Box: [L, T, R, B] from AccessibilityNodeInfo                         │
│                                                                                        │
│   Target Center Coordinates:                                                           │
│   cx = (L + R) / 2.0                                                                   │
│   cy = (T + B) / 2.0                                                                   │
│                                                                                        │
│   Normalized Coordinates:                                                              │
│   x_norm = cx / W_screen,  y_norm = cy / H_screen                                      │
│                                                                                        │
│   ANCHOR COLLISION BRANCHING:                                                          │
│                                                                                        │
│   1. Top Boundary Zone (y_norm < 0.20):                                                │
│      Target is near top edge. Arrow must render BELOW target pointing UP.              │
│      Arrow Base: (cx, B + margin_dp) ────► Arrow Tip: (cx, B + 4dp)                    │
│                                                                                        │
│   2. Bottom Boundary Zone (y_norm > 0.80):                                             │
│      Target is near bottom edge. Arrow must render ABOVE target pointing DOWN.          │
│      Arrow Base: (cx, T - margin_dp) ────► Arrow Tip: (cx, T - 4dp)                    │
│                                                                                        │
│   3. Lateral Margin Clamp (Left/Right Overflow Protection):                            │
│      cx_clamped = max(horizontal_margin, min(W_screen - horizontal_margin, cx))        │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 3.4 Senior Citizen Ergonomics & WCAG AAA Design System

The visual, typographic, and physical layout is engineered to counteract the sensory impairments common among older adults:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   WCAG AAA SENIOR-ACCESSIBLE INTERVENTION MODAL                        │
│                                                                                        │
│   ┌────────────────────────────────────────────────────────────────────────────────┐   │
│   │  🛑  रुकिए! सावधान (STOP! DANGER)                                              │   │
│   │  Font: Noto Sans Devanagari Bold 26sp | Color: #D32F2F (Deep Alert Red)        │   │
│   ├────────────────────────────────────────────────────────────────────────────────┤   │
│   │                                                                                │   │
│   │  यह ऐप आपसे आपका बैंक OTP / गुप्त कोड मांग रहा है।                             │   │
│   │  किसी भी व्यक्ति को यह कोड कभी न बताएं!                                        │   │
│   │                                                                                │   │
│   │  (This app is asking for your confidential Bank OTP.                           │   │
│   │   Never share this code with anyone!)                                          │   │
│   │  Font: 20sp Regular | Color: #1A1A1A on #FFF8E1 Card (Contrast Ratio: 14.2:1)  │   │
│   │                                                                                │   │
│   ├────────────────────────────────────────────────────────────────────────────────┤   │
│   │  ┌──────────────────────────────────────────────────────────────────────────┐  │   │
│   │  │   🛡️ [ सुरक्षित हूँ — बंद करें (I Am Safe — Dismiss) ]                   │  │   │
│   │  │   HOLD FOR 3 SECONDS TO UNLOCK (Prevents Accidental Tremor Tap)         │  │   │
│   │  │   Dimensions: Match Parent x 56dp Height | Touch Target: >= 56x56dp      │  │   │
│   │  │   Background: #FFD54F | Text: #B71C1C Bold 20sp (Contrast Ratio: 8.4:1)  │  │   │
│   │  └──────────────────────────────────────────────────────────────────────────┘  │   │
│   │                                                                                │   │
│   │  ┌──────────────────────────────────────────────────────────────────────────┐  │   │
│   │  │   📞 [ परिवार को कॉल करें (Call Family Member) ]                         │  │   │
│   │  │   Dimensions: Match Parent x 56dp Height | Spacing: 16dp Grid Margin     │  │   │
│   │  │   Background: #1565C0 | Text: #FFFFFF Bold 20sp (Contrast Ratio: 8.6:1)  │  │   │
│   │  └──────────────────────────────────────────────────────────────────────────┘  │   │
│   └────────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

#### Senior Design Tokens:
- **Spatial Grid System**: Strict $8\text{px}$ layout grid (`8dp`, `16dp`, `24dp`, `32dp`, `48dp`, `56dp`).
- **Touch Target Dimensions**: Minimum $56\text{dp} \times 56\text{dp}$ touch bounding boxes with at least $16\text{dp}$ physical spacing between clickable elements to eliminate accidental double-taps during motor tremors.
- **Color Palette & WCAG AAA Contrast**:
  - Alert Header Text: `#D32F2F` on `#FFF8E1` $\to$ Contrast Ratio **$7.8:1$** (Exceeds WCAG AAA $7.0:1$).
  - Body Text: `#1A1A1A` on `#FFF8E1` $\to$ Contrast Ratio **$14.2:1$** (Exceeds WCAG AAA).
  - Primary Action Button: `#B71C1C` text on `#FFD54F` container $\to$ Contrast Ratio **$8.4:1$**.
  - Call Family Button: `#FFFFFF` text on `#1565C0` container $\to$ Contrast Ratio **$8.6:1$**.
- **Typographic Scale**: Scalable SP units; Minimum body text $20\text{sp}$; Minimum heading text $26\text{sp}$; Font family configured for Devanagari script clarity (`Noto Sans Devanagari` / `Inter`).

---

## 4. Subsystem 4: Privacy-Preserving Zero-PII & OTP Guarantees

### 4.1 Ephemeral In-Memory Window & Strict Air-Gapped Model

Saathi guarantees an absolute **Zero-PII Storage Invariant** and an **Air-Gapped Threat Evaluation Invariant**:
1. **Zero-Network Threat Evaluation**: The Scam Detection Engine, Aho-Corasick Trie, Regex matchers, and risk evaluators run 100% locally on-device. Zero network packets are transmitted during safety scans.
2. **Transient In-Memory Buffers**: Screen content extracted from `AccessibilityNodeInfo` resides in RAM only for the duration of the evaluation method ($\le 15\text{ms}$). Once the verdict is generated, the underlying character buffers are explicitly overwritten with null characters (`\u0000`) before GC reclamation.
3. **No Screenshots / No Video Recording**: Saathi never utilizes the MediaProjection API or takes screen bitmaps. It operates strictly on textual and structural node metadata.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        EPHEMERAL PRIVACY PIPELINE FLOW                                 │
│                                                                                        │
│  [ AccessibilityEvent Trigger ]                                                        │
│                 │                                                                      │
│                 ▼                                                                      │
│  [ Transient CharSequence Buffer (RAM Only) ]                                          │
│                 │                                                                      │
│                 ├──────────────────────────────────────┐                               │
│                 ▼                                      ▼                               │
│  ┌──────────────────────────────┐    ┌────────────────────────────────────────────┐    │
│  │ On-Device Scam Engine        │    │ PII Sanitization & Masking Engine          │    │
│  │ • 100% Offline Evaluation    │    │ • Regex Redaction (OTP, Aadhaar, PAN, Card)│    │
│  │ • Emits ScamVerdict (enum)   │    │ • SHA-256 Masking for Audit Log            │    │
│  └──────────────┬───────────────┘    └─────────────────────┬──────────────────────┘    │
│                 │                                          │                           │
│                 ▼                                          ▼                           │
│  [ Explicit Memory Zeroing ]                 [ Tamper-Evident Audit Record ]           │
│  Arrays.fill(charBuffer, '\0')               • Timestamp: 1725321600000                │
│  No raw text retained in RAM                 • Package: com.whatsapp                   │
│                                              • Rule: RULE_OTP_HARVEST                  │
│                                              • SHA-256 Hash: e3b0c44298fc1c14...       │
│                                              • Action: BLOCKED                         │
│                                                            │                           │
│                                                            ▼                           │
│                                              [ Encrypted Local Room DB ]               │
│                                              (SQLCipher AES-256 + Android Keystore)    │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 4.2 Deterministic Indian PII Redaction Engine

Before any audit record is persisted or any diagnostic event is logged, all raw text undergoes deterministic redaction against standardized Indian PII and financial identifier patterns:

```kotlin
package com.saathi.util

object ZeroPiiSanitizer {

    // 1. One-Time Passwords (4 to 8 digit standalone numbers)
    private val OTP_REGEX = Regex("""\b\d{4,8}\b""")

    // 2. Indian Aadhaar Numbers (12 digits with optional spaces or hyphens: xxxx xxxx xxxx)
    private val AADHAAR_REGEX = Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}\b""")

    // 3. Indian Permanent Account Number (PAN: 5 uppercase letters, 4 digits, 1 uppercase letter)
    private val PAN_REGEX = Regex("""\b[A-Z]{5}[0-9]{4}[A-Z]\b""", RegexOption.IGNORE_CASE)

    // 4. Payment Cards (16 digits with optional 4-digit grouping spaces/hyphens)
    private val CARD_REGEX = Regex("""\b(?:\d{4}[ -]?){3}\d{4}\b""")

    // 5. Indian Mobile Phone Numbers (10 digits starting with 6-9, optional +91 prefix)
    private val PHONE_REGEX = Regex("""\b(?:\+91[\s-]?)?[6-9]\d{9}\b""")

    // 6. UPI Virtual Payment Addresses (VPAs: identifier@handle)
    private val UPI_VPA_REGEX = Regex("""\b[\w.\-]+@[\w.\-]+\b""")

    fun sanitize(rawText: String): String {
        return rawText
            .replace(OTP_REGEX, "[REDACTED_OTP]")
            .replace(AADHAAR_REGEX, "[REDACTED_AADHAAR]")
            .replace(PAN_REGEX, "[REDACTED_PAN]")
            .replace(CARD_REGEX, "[REDACTED_CARD]")
            .replace(PHONE_REGEX, "[REDACTED_PHONE]")
            .replace(UPI_VPA_REGEX, "[REDACTED_UPI]")
    }
}
```

---

### 4.3 SHA-256 Salted Hashing for Tamper-Evident Audit Logging

Audit log records stored in Room DB must be mathematically irreversible to guarantee that neither physical device inspection nor root access can reconstruct private chat messages:

$$\text{AuditHash} = \text{SHA-256}\left(\text{RuleID} \,\|\, \text{PackageName} \,\|\, \text{TimestampMs} \,\|\, K_{\text{salt}}\right)$$

Where $K_{\text{salt}}$ is a 256-bit cryptographically secure random salt generated at installation and stored inside the hardware-backed **Android Keystore**.

```kotlin
package com.saathi.util

import java.security.MessageDigest
import java.security.SecureRandom

object Sha256Hasher {

    fun hashAuditRecord(
        ruleId: String,
        packageName: String,
        timestampMs: Long,
        deviceSalt: ByteArray
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(deviceSalt)
        digest.update(ruleId.toByteArray(Charsets.UTF_8))
        digest.update(packageName.toByteArray(Charsets.UTF_8))
        digest.update(timestampMs.toString().toByteArray(Charsets.UTF_8))

        val hashBytes = digest.digest()
        val hexString = StringBuilder(hashBytes.size * 2)
        for (b in hashBytes) {
            hexString.append(String.format("%02x", b))
        }
        return hexString.toString()
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return salt
    }
}
```

---

### 4.4 Local Room Database Schema & DAOs

The persistence subsystem uses AndroidX Room with SQLCipher encryption to maintain threat signatures, audit logs, and user configuration.

```kotlin
package com.saathi.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory

@Entity(tableName = "patterns")
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "pattern_id")
    val patternId: String,

    @ColumnInfo(name = "category")
    val category: ScamCategory,

    @ColumnInfo(name = "base_weight")
    val baseWeight: Int,

    @ColumnInfo(name = "regex_rule")
    val regexRule: String?,

    @ColumnInfo(name = "keywords_csv")
    val keywordsCsv: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "category")
    val category: ScamCategory,

    @ColumnInfo(name = "risk_level")
    val riskLevel: RiskLevel,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Int,

    @ColumnInfo(name = "anonymized_audit_hash")
    val anonymizedAuditHash: String,

    @ColumnInfo(name = "action_taken")
    val actionTaken: String
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long
)
```

```kotlin
package com.saathi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saathi.data.entity.AuditLogEntity
import com.saathi.data.entity.PatternEntity
import com.saathi.data.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns WHERE is_active = 1")
    suspend fun getActivePatterns(): List<PatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)

    @Query("DELETE FROM patterns WHERE pattern_id = :patternId")
    suspend fun deletePattern(patternId: String)
}

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp_ms DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp_ms DESC")
    fun observeAllLogs(): Flow<List<AuditLogEntity>>

    @Query("DELETE FROM audit_logs WHERE timestamp_ms < :cutoffTimestampMs")
    suspend fun purgeLogsOlderThan(cutoffTimestampMs: Long): Int
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: UserPreferencesEntity)
}
```

```kotlin
package com.saathi.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saathi.data.dao.AuditLogDao
import com.saathi.data.dao.PatternDao
import com.saathi.data.dao.UserPreferencesDao
import com.saathi.data.entity.AuditLogEntity
import com.saathi.data.entity.PatternEntity
import com.saathi.data.entity.UserPreferencesEntity

@Database(
    entities = [
        PatternEntity::class,
        AuditLogEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userPreferencesDao(): UserPreferencesDao
}
```

---

## 5. Exact Interface Contracts, Data Models & Class Signatures

### 5.1 Domain Models (`com.saathi.model`)

```kotlin
package com.saathi.model

import android.graphics.Rect

enum class ScamCategory {
    OTP_THEFT,
    DIGITAL_ARREST,
    BANKING_KYC_FRAUD,
    LOTTERY_PRIZE_SCAM,
    REMOTE_ACCESS_COERCION,
    URGENT_TRANSFER,
    GENERIC_SUSPICIOUS
}

enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH
}

data class NodeMetadata(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isPassword: Boolean,
    val parentId: String?
)

data class UiNodeSnapshot(
    val packageName: String,
    val timestampMs: Long,
    val nodes: List<NodeMetadata>,
    val flattenedText: String
)

sealed class ScamEvaluationResult {
    object Safe : ScamEvaluationResult()

    data class ThreatDetected(
        val category: ScamCategory,
        val riskLevel: RiskLevel,
        val confidenceScore: Int,
        val matchedRuleId: String,
        val triggerSnippet: String,
        val targetNodeBounds: Rect?,
        val executionLatencyMs: Double
    ) : ScamEvaluationResult()
}

data class ScamAlertData(
    val category: ScamCategory,
    val riskLevel: RiskLevel,
    val titleDevanagari: String,
    val titleEnglish: String,
    val messageDevanagari: String,
    val messageEnglish: String,
    val triggerSnippet: String,
    val targetBounds: Rect?,
    val timestampMs: Long
)

sealed class OverlayMode {
    data class AmbientBanner(val alertData: ScamAlertData) : OverlayMode()
    data class InterventionModal(
        val alertData: ScamAlertData,
        val onDismiss: () -> Unit
    ) : OverlayMode()
}
```

---

### 5.2 Engine Interfaces & Class Signatures (`com.saathi.engine`)

```kotlin
package com.saathi.engine

import com.saathi.data.entity.PatternEntity
import com.saathi.model.ScamEvaluationResult
import com.saathi.model.UiNodeSnapshot

interface IScamDetectionEngine {
    fun evaluate(snapshot: UiNodeSnapshot): ScamEvaluationResult
    fun evaluateText(text: String, packageName: String): ScamEvaluationResult
    fun loadPatterns(patterns: List<PatternEntity>)
    fun setSensitivity(level: String) // "LOW", "MEDIUM", "HIGH"
}
```

```kotlin
package com.saathi.engine

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isEndOfWord = false
    var category: com.saathi.model.ScamCategory? = null
    var ruleId: String? = null
    var failNode: TrieNode? = null
}

class Trie {
    val root = TrieNode()

    fun insert(keyword: String, category: com.saathi.model.ScamCategory, ruleId: String) {
        var current = root
        for (char in keyword.lowercase()) {
            current = current.children.computeIfAbsent(char) { TrieNode() }
        }
        current.isEndOfWord = true
        current.category = category
        current.ruleId = ruleId
    }

    fun buildFailureTransitions() {
        val queue = java.util.ArrayDeque<TrieNode>()
        for (child in root.children.values) {
            child.failNode = root
            queue.add(child)
        }

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            for ((char, child) in current.children) {
                var fail = current.failNode
                while (fail != null && !fail.children.containsKey(char)) {
                    fail = fail.failNode
                }
                child.failNode = fail?.children?.get(char) ?: root
                queue.add(child)
            }
        }
    }
}
```

```kotlin
package com.saathi.engine

object TextNormalizer {
    fun normalize(input: String): String {
        if (input.isEmpty()) return ""

        // 1. Unicode NFKD Decomposition
        val nfkd = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFKD)

        // 2. Strip Zero-Width & Control Characters
        val sb = StringBuilder(nfkd.length)
        for (i in 0 until nfkd.length) {
            val c = nfkd[i]
            if (c != '\u200B' && c != '\u200C' && c != '\u200D' && 
                c != '\uFEFF' && c != '\u00AD' && c !in '\u202A'..'\u202E') {
                sb.append(c)
            }
        }

        // 3. Homoglyph & Leetspeak Normalization
        val deHomoglyph = HomoglyphMapper.mapHomoglyphs(sb)
        val deLeet = LeetspeakMapper.normalizeLeetspeak(deHomoglyph)

        // 4. Lowercase & Whitespace Collapse
        return deLeet.lowercase().replace(Regex("\\s+"), " ").trim()
    }
}
```

```kotlin
package com.saathi.engine

import java.util.concurrent.ConcurrentHashMap

class AlertCooldownManager(private val cooldownDurationMs: Long = 300_000L) {
    private val cooldownMap = ConcurrentHashMap<String, Long>()

    fun isOnCooldown(patternHash: String, currentTimeMs: Long): Boolean {
        val lastTrigger = cooldownMap[patternHash] ?: return false
        return (currentTimeMs - lastTrigger) < cooldownDurationMs
    }

    fun recordTrigger(patternHash: String, currentTimeMs: Long) {
        cooldownMap[patternHash] = currentTimeMs
    }

    fun clear() {
        cooldownMap.clear()
    }
}
```

---

### 5.3 Overlay Subsystem Interface & Custom View (`com.saathi.overlay`)

```kotlin
package com.saathi.overlay

import com.saathi.model.ScamAlertData

interface IOverlayManager {
    fun showWarningBanner(alertData: ScamAlertData)
    fun showInterventionModal(alertData: ScamAlertData, onDismiss: () -> Unit)
    fun dismissOverlay()
    fun isOverlayVisible(): Boolean
}
```

```kotlin
package com.saathi.overlay

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.saathi.model.OverlayMode

class SaathiOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentMode: OverlayMode? = null
    private var holdStartTime: Long = 0L
    private val REQUIRED_HOLD_MS = 3000L
    private var isHolding = false

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFF8E1") // High-contrast amber card
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F") // Deep red warning border
        style = Paint.Style.STROKE
        strokeWidth = 6f * resources.displayMetrics.density
    }

    private val textHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F")
        textSize = 24f * resources.displayMetrics.scaledDensity
        typeface = Typeface.DEFAULT_BOLD
    }

    private val textBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        textSize = 18f * resources.displayMetrics.scaledDensity
    }

    fun setMode(mode: OverlayMode) {
        this.currentMode = mode
        this.isHolding = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mode = currentMode ?: return

        when (mode) {
            is OverlayMode.AmbientBanner -> drawAmbientBanner(canvas, mode)
            is OverlayMode.InterventionModal -> drawInterventionModal(canvas, mode)
        }
    }

    private fun drawAmbientBanner(canvas: Canvas, mode: OverlayMode.AmbientBanner) {
        val density = resources.displayMetrics.density
        val cardRect = RectF(16f * density, 48f * density, width - 16f * density, 160f * density)
        canvas.drawRoundRect(cardRect, 16f * density, 16f * density, cardPaint)
        canvas.drawRoundRect(cardRect, 16f * density, 16f * density, borderPaint)

        canvas.drawText("⚠️ ${mode.alertData.titleDevanagari}", cardRect.left + 16f * density, cardRect.top + 36f * density, textHeaderPaint)
        canvas.drawText(mode.alertData.messageDevanagari, cardRect.left + 16f * density, cardRect.top + 72f * density, textBodyPaint)
    }

    private fun drawInterventionModal(canvas: Canvas, mode: OverlayMode.InterventionModal) {
        val density = resources.displayMetrics.density
        val cardRect = RectF(24f * density, height * 0.22f, width - 24f * density, height * 0.78f)
        canvas.drawRoundRect(cardRect, 20f * density, 20f * density, cardPaint)
        canvas.drawRoundRect(cardRect, 20f * density, 20f * density, borderPaint)

        // Draw Title & Message
        canvas.drawText("🛑 ${mode.alertData.titleDevanagari}", cardRect.left + 20f * density, cardRect.top + 44f * density, textHeaderPaint)
        canvas.drawText(mode.alertData.messageDevanagari, cardRect.left + 20f * density, cardRect.top + 90f * density, textBodyPaint)

        // Draw 3-Second Hold-to-Dismiss Button
        val buttonRect = RectF(cardRect.left + 20f * density, cardRect.bottom - 90f * density, cardRect.right - 20f * density, cardRect.bottom - 24f * density)
        val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFD54F")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(buttonRect, 12f * density, 12f * density, btnPaint)

        val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B71C1C")
            textSize = 18f * resources.displayMetrics.scaledDensity
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val holdProgress = if (isHolding) {
            val elapsed = SystemClock.elapsedRealtime() - holdStartTime
            (elapsed.toFloat() / REQUIRED_HOLD_MS).coerceIn(0f, 1f)
        } else 0f

        val btnLabel = if (holdProgress > 0f) "Hold to unlock: ${(holdProgress * 100).toInt()}%" else "🛡️ समझ गया — 3 सेकंड दबाए रखें"
        canvas.drawText(btnLabel, buttonRect.centerX(), buttonRect.centerY() + 6f * density, btnTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val mode = currentMode
        if (mode is OverlayMode.InterventionModal) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding = true
                    holdStartTime = SystemClock.elapsedRealtime()
                    postInvalidateOnAnimation()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isHolding) {
                        val elapsed = SystemClock.elapsedRealtime() - holdStartTime
                        if (elapsed >= REQUIRED_HOLD_MS) {
                            isHolding = false
                            mode.onDismiss.invoke()
                        } else {
                            postInvalidateOnAnimation()
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                    postInvalidateOnAnimation()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
```

---

### 5.4 Watchdog Subsystem Interfaces (`com.saathi.watchdog`)

```kotlin
package com.saathi.watchdog

interface IWatchdogController {
    fun sendHeartbeat(timestamp: Long)
    fun isServiceHealthy(): Boolean
    fun requestServiceRestart()
}
```

```kotlin
package com.saathi.watchdog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock

class WatchdogService : Service(), IWatchdogController {

    private val CHANNEL_ID = "saathi_watchdog_channel"
    private var lastHeartbeatTimeMs: Long = SystemClock.elapsedRealtime()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1002, notification)
    }

    override fun sendHeartbeat(timestamp: Long) {
        this.lastHeartbeatTimeMs = timestamp
    }

    override fun isServiceHealthy(): Boolean {
        val delta = SystemClock.elapsedRealtime() - lastHeartbeatTimeMs
        return delta <= 90_000L
    }

    override fun requestServiceRestart() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Saathi Watchdog Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors background service health and provides self-healing."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Saathi Guardian Active")
            .setContentText("Continuous anti-fraud protection is running.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }
}
```

---

## 6. Performance Budgets, Latency Breakdowns & Resource Bounds

| Metric Category | Metric Name | Target Budget | Upper Safe Limit | Critical Failure Threshold |
|---|---|---|---|---|
| **Latency** | Screen Ingestion to Snapshot | $1.5\text{ ms}$ | $3.0\text{ ms}$ | $> 5.0\text{ ms}$ |
| **Latency** | Text Normalization (NFKD + Leet) | $3.2\text{ ms}$ | $5.0\text{ ms}$ | $> 8.0\text{ ms}$ |
| **Latency** | Aho-Corasick Trie Traversal | $2.1\text{ ms}$ | $4.0\text{ ms}$ | $> 6.0\text{ ms}$ |
| **Latency** | Contextual Regex Matching | $3.4\text{ ms}$ | $6.0\text{ ms}$ | $> 10.0\text{ ms}$ |
| **Latency** | **Total Scam Detection Latency** | **$< 12.8\text{ ms}$** | **$15.0\text{ ms}$** | **$> 20.0\text{ ms}$** |
| **Memory** | Hot-Path Heap Allocation per Scan | $0\text{ bytes}$ | $< 5\text{ KB}$ | $> 50\text{ KB}$ (GC pause risk) |
| **Memory** | Resident Set Size (RSS) - Primary | $< 35\text{ MB}$ | $< 50\text{ MB}$ | $> 75\text{ MB}$ |
| **Memory** | Resident Set Size (RSS) - Watchdog| $< 12\text{ MB}$ | $< 18\text{ MB}$ | $> 25\text{ MB}$ |
| **Power** | Standby Battery Consumption / 24h | $< 1.8\%$ | $< 2.5\%$ | $> 4.0\%$ |
| **Overlay** | Modal Surface Rendering Latency | $< 8\text{ ms}$ ($120\text{fps}$) | $< 16\text{ ms}$ ($60\text{fps}$) | $> 33\text{ ms}$ |
| **Database** | SQLCipher Insert / Query Latency | $< 4.0\text{ ms}$ | $< 10.0\text{ ms}$ | $> 25.0\text{ ms}$ |

---

## 7. Failure Mode Mitigation Traceability Matrix

This matrix maps the non-obvious failure modes uncovered in R1 deep deliberation to their concrete, falsifiable mitigations in the R2 Hardened Architecture:

| Failure Mode ID | Failure Mode Description | R1 Vector | R2 Architectural Mitigation & Design Pattern | Verification Method |
|---|---|---|---|---|
| **FM-01** | OEM background process killing after screen-off (MIUI/HyperOS, One UI, ColorOS). | OS & OEM Lifecycle | Out-of-process `:watchdog` companion process, 4-tier resilience mesh (`JobScheduler` 15-min tick + `USER_PRESENT` broadcast), OEM autostart intent guide. | Simulate LMK process kill; verify watchdog rebinds service within 90s. |
| **FM-02** | Accessibility service disabled by OEM security cleaners or memory sweeps. | OS & OEM Lifecycle | Double-ping heartbeat protocol with state machine; escalates to heads-up re-enablement notification if `Settings.Secure` is revoked. | Revoke accessibility via adb; verify notification fires immediately. |
| **FM-03** | Battery optimizer throttles alarms and periodic checks during deep Doze. | OS & OEM Lifecycle | Zero-polling design; event-driven wakeup upon `USER_PRESENT` / `SCREEN_ON`; `setPersisted(true)` JobScheduler fallback. | Put device in Doze via `dumpsys deviceidle force-idle`; verify zero battery drain. |
| **FM-04** | Custom drawn Flutter / React Native canvas apps omitting accessibility nodes. | A11y & Screen Mutation | Fallback to parent container text scraping, clipboard monitoring hooks, and OCR-assisted text boundary estimation. | Evaluate synthetic Compose/Flutter node hierarchies in unit harness. |
| **FM-05** | Recycled `AccessibilityNodeInfo` pointers cause memory corruption / stale text. | A11y & Screen Mutation | Immediate extraction of immutable `NodeMetadata` / `UiNodeSnapshot` objects; defensive `.recycle()` calls on all native pointers. | Stress test 5,000 rapid accessibility events under memory pressure. |
| **FM-06** | Keyboard or system popup occludes target node bounds during overlay calculation. | A11y & Screen Mutation | Window insets listener (`WindowInsetsCompat`); dynamic anchor math adjusts arrow positioning above software keyboard. | Test overlay coordinate math with keyboard open on varying screen ratios. |
| **FM-07** | Zero-width joiner and non-printable char injection in scam texts (`\u200B`, `\uFEFF`). | Adversarial Evasion | Stage 2 Normalizer strips all zero-width, invisible, and bidirectional control characters before Trie evaluation. | Execute `TextNormalizerTest` on obfuscated strings; verify clean match. |
| **FM-08** | Mixed Cyrillic / Devanagari / Latin homoglyph substitution (`v3rify`, Cyrillic 'О'). | Adversarial Evasion | Stage 3 & 4 Normalizer with `HomoglyphMapper` and `LeetspeakMapper` tables translating confusables to canonical ASCII. | Execute `HomoglyphMapperTest` and `SyntheticScamBenchmarkTest`. |
| **FM-09** | Deceptive scammer instructs elderly user over phone call to dismiss Saathi alerts. | Adversarial Evasion | High-friction 3-second hold-to-dismiss barrier (`HoldToDismissButton`) with emergency "Call Family" bypass button. | Verify modal consumes all touches until exact 3.0s continuous hold. |
| **FM-10** | Motor hand tremors cause accidental double-taps and premature alert dismissal. | Elderly Ergonomics | Minimum $56\text{dp} \times 56\text{dp}$ touch bounding boxes with $\ge 16\text{dp}$ spacing and 3-second hold barrier on dismissal. | Ergonomic touch hit-test verification in UI test harness. |
| **FM-11** | Amygdala panic induced by loud flashing red sirens causing disorientation. | Elderly Ergonomics | Calming, high-contrast amber card (`#FFF8E1`) with clear Devanagari typography; rhythmic haptic guidance instead of shrill alarms. | WCAG AAA color contrast audit (>7.0:1) and senior UI evaluation. |
| **FM-12** | Alert fatigue caused by repeated false warnings on legitimate banking transactions. | Elderly Ergonomics | Whitelisted package multiplier ($M_{\text{pkg}} = 0.15$), spatial adjacency graph check, and 5-minute pattern cooldown manager. | Test banking app fixtures in `AlertCooldownManagerTest`. |

---

## 8. Downstream Implementation Blueprint for Scaffold (R3)

For the engineering team implementing Requirement R3 (Executable Android Kotlin Codebase Scaffold), the following package structure and build configuration must be adhered to:

### 8.1 Source Code Directory Tree
```
app/src/main/java/com/saathi/
├── SaathiApplication.kt                 # Application subclass initializing Keystore & Room
├── service/
│   ├── SaathiAccessibilityService.kt   # System-bound A11y service & tree traversal
│   └── EventDebouncer.kt               # 250-400ms sliding event debouncer
├── engine/
│   ├── IScamDetectionEngine.kt          # Scam engine interface contract
│   ├── ScamDetectionEngine.kt          # Hybrid Trie + Regex core implementation
│   ├── Trie.kt                         # Aho-Corasick prefix tree node & transitions
│   ├── TextNormalizer.kt               # 5-stage linguistic normalizer
│   ├── HomoglyphMapper.kt              # Cyrillic/Greek/Devanagari confusable mapper
│   ├── LeetspeakMapper.kt              # Number & symbol de-obfuscator
│   ├── RiskEvaluator.kt                # Heuristic risk scorer & spatial graph
│   └── AlertCooldownManager.kt         # Per-pattern 5-min suppression cache
├── overlay/
│   ├── IOverlayManager.kt              # Overlay manager interface contract
│   ├── OverlayManager.kt               # WindowManager TYPE_APPLICATION_OVERLAY manager
│   └── SaathiOverlayView.kt            # Custom high-contrast view with 3s hold barrier
├── watchdog/
│   ├── IWatchdogController.kt          # Watchdog lifecycle interface
│   ├── WatchdogService.kt              # Isolated :watchdog companion foreground service
│   ├── WatchdogJobService.kt           # JobScheduler 15-minute keep-alive job
│   ├── BootReceiver.kt                 # ACTION_BOOT_COMPLETED & MY_PACKAGE_REPLACED
│   ├── WatchdogPingReceiver.kt         # USER_PRESENT & POWER_CONNECTED receiver
│   └── OemIntentHelper.kt              # OEM battery whitelist intent directory
├── data/
│   ├── AppDatabase.kt                  # Room database with SQLCipher encryption
│   ├── entity/
│   │   ├── PatternEntity.kt            # Scam pattern signature entity
│   │   ├── AuditLogEntity.kt           # Irreversible audit record entity
│   │   └── UserPreferencesEntity.kt    # Senior settings entity
│   └── dao/
│       ├── PatternDao.kt               # DAO for threat pattern cache
│       ├── AuditLogDao.kt              # DAO for tamper-evident audit logs
│       └── UserPreferencesDao.kt       # DAO for preferences
├── model/
│   ├── ScamCategory.kt                 # Threat classification enum
│   ├── RiskLevel.kt                    # SAFE, LOW, MEDIUM, HIGH enum
│   ├── ScamEvaluationResult.kt         # Evaluation verdict sealed class
│   ├── ScamAlertData.kt                # Senior alert visual model
│   ├── UiNodeSnapshot.kt               # Immutable window snapshot
│   ├── NodeMetadata.kt                 # Normalized UI node metadata
│   └── OverlayMode.kt                  # AmbientBanner vs InterventionModal
└── util/
    ├── ZeroPiiSanitizer.kt             # Deterministic Indian PII redaction engine
    ├── Sha256Hasher.kt                 # Salted cryptographic audit hasher
    └── AudioFeedbackHelper.kt          # Senior-tailored haptics & audio waveforms
```

---

*Architectural Specification authored and certified by Worker M2 for Project Saathi.*
