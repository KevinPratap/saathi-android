# Original User Request

## 2026-09-03T05:39:45+05:30

Deliberate deeply on the Saathi product and technical architecture to uncover edge cases, technical bottlenecks, and cognitive failure modes, then translate those mitigations into a hardened architectural blueprint and working Android Kotlin codebase scaffold.

Working directory: c:/Users/prata/Documents/antigravity/charming-babbage
Integrity mode: development

Reference material: `c:/Users/prata/Documents/antigravity/charming-babbage/SAATHI_SPECIFICATION.md`

## Requirements

### R1. Deep Deliberation & Multi-Dimensional Failure Mode Analysis
Produce an exhaustive analysis detailing non-obvious failure modes across four critical vectors:
1. **OS & OEM Lifecycle**: Background process termination under aggressive battery optimization (Xiaomi MIUI/HyperOS, Vivo Funtouch, Oppo ColorOS, Samsung One UI), Android 14/15 accessibility restrictions, Play Store policy compliance (AccessibilityService declaration requirements and rejection triggers).
2. **Accessibility API & Screen Mutation Fragility**: Dynamic React Native/Flutter apps omitting accessibility nodes, custom drawn canvases (games/crypto apps), keyboard occlusion of target bounds, rapid screen transitions, multi-window/split-screen layout shifts.
3. **Adversarial Scam Tactics & Evasion**: Homoglyph attacks (mixed Latin/Devanagari scripts), spaced/zero-width character injection in scam texts, voice-call coercion where scammer instructs the elderly user to disable Saathi, deceptive overlay spoofing.
4. **Elderly Cognitive & Physiological UX**: Motor tremors causing double-taps or accidental dismissals, hearing impairments / pitch-frequency loss, panic induced by alarming warning colors/sounds, dialectal accents and speech hesitations/stutters triggering premature voice timeouts.

### R2. Hardened Architecture & Subsystem Specification
Define concrete, resilient technical solutions and design patterns for each failure mode discovered in R1:
- Resilient watchdog and self-healing daemon architecture for background survival.
- Hybrid on-device regex + NFKD normalized Trie scam detection engine with false-positive suppression, confidence scoring, and pattern cooldown.
- DPI-independent hardware overlay (`TYPE_APPLICATION_OVERLAY`) with coordinate normalization, collision avoidance, and haptic feedback profiles.
- Privacy-preserving architecture ensuring zero PII/OTP leakage and offline-first safety guarantees.

### R3. Executable Android Kotlin Codebase Scaffold
Implement a complete, production-structured Android Kotlin codebase in the working directory:
- Standard Android project directory structure with Gradle configuration (`minSdk 26`, `targetSdk 34`).
- `SaathiAccessibilityService` implementation with resilient tree traversal, node metadata extraction, and event throttling.
- `OverlayManager` and custom `OverlayView` with canvas drawing for pointing arrows, glowing beacon rings, and non-blocking touch passthrough.
- `ScamDetectionEngine` with Trie and regex pattern matching, Devanagari Unicode NFKD normalization, and sensitivity threshold logic.
- `WatchdogService` and broadcast receivers for OEM process restart and persistent foreground notification management.
- Room database entities and DAOs for local preference storage, pattern cache, and audit logging.

### R4. Verification Harness & Test Suite
Implement automated unit and integration tests verifying the core logic:
- Trie and Regex scam pattern matching test cases (covering standard English, Hindi Devanagari, obfuscated text, homoglyphs, and known-safe app whitelisting).
- UI node normalization and coordinate calculation tests under varied screen aspect ratios.
- Simulated AccessibilityEvent ingestion pipeline tests.

## Acceptance Criteria

### Deliberation & Analysis Completeness
- [ ] Analysis documents at least 15 distinct, non-trivial failure modes across the 4 specified vectors with concrete, falsifiable mitigations.
- [ ] Addresses Play Store policy compliance specifically regarding Accessibility API usage justification for elderly assistance and anti-fraud.

### Code Quality & Architectural Integrity
- [ ] Android Kotlin codebase follows modern architecture principles (Kotlin Coroutines/Flow, Room, clean separation of services, managers, and views).
- [ ] Zero hardcoded credentials or un-sanitized data transmission paths.
- [ ] Background service configuration includes proper foreground service type (`dataCapture`), notification channels, and broadcast receivers.

### Programmatic Verification
- [ ] Automated unit test suite runs and passes with 100% success on all scam detection test fixtures (positive hits, negative controls, and obfuscation tests).
- [ ] Codebase compiles cleanly without unresolved references or missing dependencies.
