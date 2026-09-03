# Project: Saathi Architecture Deliberation and Android Kotlin Scaffold Implementation

## Architecture Overview
Saathi is a privacy-first, on-device Android scam protection assistant engineered specifically for elderly users in India (and globally) who are disproportionately targeted by digital financial fraud (OTP harvesting, fake courier/bank KYC coercion, fake customer support APKs, and social engineering).

The system operates across four primary decoupled subsystems:
1. **Sensory Ingestion & Accessibility Subsystem (`service`)**:
   - `SaathiAccessibilityService`: Listens for window content and accessibility focus mutations (`TYPE_WINDOW_CONTENT_CHANGED`, `TYPE_VIEW_TEXT_CHANGED`, `TYPE_VIEW_FOCUSED`).
   - Debounced event processing (250-400ms window) with DFS traversal of `AccessibilityNodeInfo` hierarchies, defensive `.recycle()` management, and immutable `UiNodeSnapshot` extraction.
2. **Deterministic On-Device Threat Detection Engine (`engine`)**:
   - `ScamDetectionEngine`: Multi-stage text processing pipeline.
   - Stage 1: Zero-PII sanitization (`ZeroPiiSanitizer`) and SHA-256 masking.
   - Stage 2: Unicode NFKD normalization, zero-width character stripping (`\u200B-\u200F`, `\uFEFF`, `\u00AD`), confusable homoglyph translation, Leetspeak unfolding, and Devanagari/Hinglish tokenization (`TextNormalizer`).
   - Stage 3: Aho-Corasick / Prefix Trie multi-keyword matching + compiled Regex rule matching.
   - Stage 4: Risk scoring, urgency/threat category aggregation, and temporal alert cooldown logic.
3. **DPI-Independent Accessible Overlay Subsystem (`overlay`)**:
   - `OverlayManager`: Controls system alert windows using `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
   - `SaathiOverlayView`: High-contrast, large-font accessible UI for senior citizens. Supports dual presentation modes: Non-intrusive ambient warning banner (touch passthrough `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL`) and high-friction modal alert (urgency barrier with 3-second hold to dismiss for high-risk attacks).
4. **Self-Healing & Watchdog Subsystem (`watchdog`)**:
   - `WatchdogService`: Isolated out-of-process service (`:watchdog`) acting as an active heartbeat monitor for `SaathiAccessibilityService`.
   - Multi-tier resilience mesh: `JobScheduler` periodic keep-alive, `AlarmManager` exact alarms, and OEM broadcast hooks (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`).
5. **Local-Only Privacy Persistence Subsystem (`data`)**:
   - Room Database (`AppDatabase`) maintaining `PatternEntity` (pre-compiled scam signatures), `AuditLogEntity` (anonymized, zero-PII security events), and `UserPreferencesEntity`.

---

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Failure Mode Analysis (OS/OEM Lifecycle) | Catalog failure modes (FM-01 to FM-04) for OEM killers, Doze, cgroup freezing, sideload restrictions | M1 | Survey / Spec |
| 2 | Failure Mode Analysis (A11y & Screen Mutation) | Catalog failure modes (FM-05 to FM-12) for Compose unmerged semantics, WebViews, Flutter, recycled pointers | M1 | Survey / Spec |
| 3 | Failure Mode Analysis (Adversarial Evasion) | Catalog failure modes (FM-13 to FM-16) for homoglyphs, zero-width chars, Leetspeak, Hinglish, voice scams | M1 | Survey / Spec |
| 4 | Failure Mode Analysis (Elderly UX) | Catalog failure modes (FM-17 to FM-19) for tremors/mis-taps, amygdala panic, alert fatigue, presbycusis | M1 | Survey / Spec |
| 5 | Play Store Policy & Security Compliance | Google Play Accessibility API policy compliance, prominent disclosure, zero-keylogging | M1 | Survey / Spec |
| 6 | Watchdog & Self-Healing Spec | 4-tier resilience mesh, dual-heartbeat IPC, OEM battery whitelist intent flow, auto-recovery | M2 | Survey / Spec |
| 7 | Scam Detection Engine Spec | Hybrid Aho-Corasick Trie + Regex, NFKD normalizer, homoglyph mapping, <15ms latency, zero-allocation hot paths | M2 | Survey / Spec |
| 8 | DPI-Independent Overlay Spec | Dual-mode overlay (banner touch-passthrough vs modal 3s hold), 56dp touch targets, high contrast | M2 | Survey / Spec |
| 9 | Zero-PII & Privacy Spec | Ephemeral in-memory window, SHA-256 masking, strict no-network offline-first model, Room audit schema | M2 | Survey / Spec |
| 10 | Gradle Build Scaffold | Gradle configuration, minSdk 26, targetSdk 34, AndroidX, Room, Coroutines, JUnit/Robolectric | M3 | Survey / Spec |
| 11 | SaathiAccessibilityService Implementation | Accessibility service with node traversal, event debouncing, snapshot extraction, overlay trigger | M3 | Survey / Spec |
| 12 | ScamDetectionEngine Implementation | Trie + Regex engine, TextNormalizer, Homoglyph mapper, Leetspeak mapper, RiskEvaluator, Cooldown | M3 | Survey / Spec |
| 13 | OverlayManager & View Implementation | WindowManager TYPE_APPLICATION_OVERLAY, SaathiOverlayView, touch passthrough, 3s hold barrier | M3 | Survey / Spec |
| 14 | WatchdogService & Receivers Implementation | Out-of-process WatchdogService, JobScheduler keep-alive, BootReceiver, WatchdogPingReceiver | M3 | Survey / Spec |
| 15 | Room Persistence Layer Implementation | AppDatabase, PatternDao, AuditLogDao, PatternEntity, AuditLogEntity, UserPreferencesEntity | M3 | Survey / Spec |
| 16 | Zero-PII Sanitizer Implementation | ZeroPiiSanitizer (OTP masking, card/phone hashing, ephemeral storage) | M3 | Survey / Spec |
| 17 | Core Engine & Trie Unit Tests | Unit tests for PrefixTrie, Aho-Corasick matching, TextNormalizer, Homoglyphs, Leetspeak | M4 | Survey / Spec |
| 18 | Scam Detection & Regex Rule Unit Tests | Unit tests for bank KYC, electricity bill, parcel customs, APK install, lottery scam patterns | M4 | Survey / Spec |
| 19 | Cooldown, Debouncer & Sanitizer Tests | Unit tests for AlertCooldownManager, EventDebouncer, ZeroPiiSanitizer, Sha256Hasher | M4 | Survey / Spec |
| 20 | Room DAO & Database Integration Tests | In-memory Room database tests for PatternDao, AuditLogDao, UserPreferencesDao | M4 | Survey / Spec |
| 21 | Synthetic Scam Benchmark Suite | 20+ comprehensive multi-lingual test fixtures (English, Hindi, Homoglyphs, Leet, Negatives) | M4 | Survey / Spec |
| 22 | Multi-Agent Review & Forensic Integrity Audit | Multi-agent review, adversarial stress testing, and forensic audit verification | M5 | Survey / Spec |

---

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | R1 Deep Deliberation & Failure Mode Analysis | Author `DELIBERATION_ANALYSIS.md` covering 20 failure modes across 5 vectors and Play Store compliance | none | PLANNED |
| M2 | R2 Hardened Architecture & Subsystem Spec | Author `HARDENED_ARCHITECTURE.md` covering Watchdog, Scam Engine, Overlay, and Zero-PII | M1 | PLANNED |
| M3 | R3 Executable Android Kotlin Codebase Scaffold | Implement full Android Kotlin scaffold (Gradle, Service, Engine, Overlay, Watchdog, Room, Sanitizer) | M2 | PLANNED |
| M4 | R4 Verification Harness & Test Suite | Implement and execute 100% passing unit, integration, and benchmark test suite | M3 | PLANNED |
| M5 | Final Gate Review & Forensic Integrity Audit | Dual-track Reviewers, Challengers, and Forensic Auditor verification | M4 | PLANNED |

---

## Interface Contracts

### 1. `AccessibilityService` ↔ `ScamDetectionEngine`
```kotlin
package com.saathi.engine

import com.saathi.model.ScamEvaluationResult
import com.saathi.model.UiNodeSnapshot

interface IScamDetectionEngine {
    fun evaluate(snapshot: UiNodeSnapshot): ScamEvaluationResult
    fun evaluateText(text: String, packageName: String): ScamEvaluationResult
    fun loadPatterns(patterns: List<com.saathi.data.entity.PatternEntity>)
}
```

### 2. `ScamDetectionEngine` ↔ `OverlayManager`
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

### 3. `AccessibilityService` ↔ `WatchdogService`
```kotlin
package com.saathi.watchdog

interface IWatchdogController {
    fun sendHeartbeat(timestamp: Long)
    fun isServiceHealthy(): Boolean
    fun requestServiceRestart()
}
```

### 4. `ScamDetectionEngine` ↔ `Room Persistence`
```kotlin
package com.saathi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saathi.data.entity.PatternEntity
import com.saathi.data.entity.AuditLogEntity

@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns WHERE is_active = 1")
    suspend fun getActivePatterns(): List<PatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)
}

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<AuditLogEntity>
}
```

---

## Code Layout
```
c:/Users/prata/Documents/antigravity/charming-babbage/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/
│       │   │   ├── values/
│       │   │   │   ├── colors.xml
│       │   │   │   ├── strings.xml
│       │   │   │   └── styles.xml
│       │   │   ├── xml/
│       │   │   │   └── accessibility_service_config.xml
│       │   │   └── layout/
│       │   │       ├── overlay_banner.xml
│       │   │       └── overlay_modal.xml
│       │   └── java/com/saathi/
│       │       ├── SaathiApplication.kt
│       │       ├── service/
│       │       │   ├── SaathiAccessibilityService.kt
│       │       │   └── EventDebouncer.kt
│       │       ├── engine/
│       │       │   ├── IScamDetectionEngine.kt
│       │       │   ├── ScamDetectionEngine.kt
│       │       │   ├── Trie.kt
│       │       │   ├── TextNormalizer.kt
│       │       │   ├── HomoglyphMapper.kt
│       │       │   ├── LeetspeakMapper.kt
│       │       │   ├── RiskEvaluator.kt
│       │       │   └── AlertCooldownManager.kt
│       │       ├── overlay/
│       │       │   ├── IOverlayManager.kt
│       │       │   ├── OverlayManager.kt
│       │       │   └── SaathiOverlayView.kt
│       │       ├── watchdog/
│       │       │   ├── IWatchdogController.kt
│       │       │   ├── WatchdogService.kt
│       │       │   ├── WatchdogJobService.kt
│       │       │   ├── BootReceiver.kt
│       │       │   └── WatchdogPingReceiver.kt
│       │       ├── data/
│       │       │   ├── AppDatabase.kt
│       │       │   ├── entity/
│       │       │   │   ├── PatternEntity.kt
│       │       │   │   ├── AuditLogEntity.kt
│       │       │   │   └── UserPreferencesEntity.kt
│       │       │   └── dao/
│       │       │       ├── PatternDao.kt
│       │       │       ├── AuditLogDao.kt
│       │       │       └── UserPreferencesDao.kt
│       │       ├── model/
│       │       │   ├── ScamCategory.kt
│       │       │   ├── RiskLevel.kt
│       │       │   ├── ScamEvaluationResult.kt
│       │       │   ├── ScamAlertData.kt
│       │       │   └── UiNodeSnapshot.kt
│       │       └── util/
│       │           ├── ZeroPiiSanitizer.kt
│       │           ├── Sha256Hasher.kt
│       │           └── AudioFeedbackHelper.kt
│       └── test/
│           └── java/com/saathi/
│               ├── engine/
│               │   ├── TrieTest.kt
│               │   ├── TextNormalizerTest.kt
│               │   ├── HomoglyphMapperTest.kt
│               │   ├── LeetspeakMapperTest.kt
│               │   ├── ScamDetectionEngineTest.kt
│               │   └── AlertCooldownManagerTest.kt
│               ├── service/
│               │   └── EventDebouncerTest.kt
│               ├── util/
│               │   ├── ZeroPiiSanitizerTest.kt
│               │   └── Sha256HasherTest.kt
│               ├── data/
│               │   └── RoomDatabaseTest.kt
│               └── benchmark/
│                   ├── ScamFixtures.kt
│                   └── SyntheticScamBenchmarkTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── DELIBERATION_ANALYSIS.md
├── HARDENED_ARCHITECTURE.md
├── PROJECT.md
└── ORIGINAL_REQUEST.md
```
