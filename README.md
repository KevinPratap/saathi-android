# Saathi — Safety Mode MVP

> Safety MVP for Friday test. Stripped to core: AccessibilityService + scam detection + overlay warning.

## What's in this branch

- `app/` — Minimal Android app for Safety mode only
  - `SaathiAccessibilityService` — Reads UI tree in real-time
  - `SaathiForegroundService` — Foreground service to survive OEM killing
  - `ScamDetector` — Keyword + regex pattern matching
  - `OverlayManager` — Draws warning over any app
- `archive/scaffold/` — Full scaffolded Kotlin codebase (51 tests, multi-mode architecture)
- `archive/docs/` — Full specification, deliberation analysis, architecture docs

## Build

```bash
./gradlew assembleDebug
```

## Friday Test (Sep 5, 2026)

- Device: Samsung Galaxy A13 5G
- Task: "Use your phone to check a message"
- Success: Service stays alive 30 min, ≥2/3 scam detections, 0 OEM kills
- No-go: Any OEM kill, 0 detections, crash, user refusal

## Architecture

```
AccessibilityService → reads UI tree
        ↓
ScamDetector → keyword + regex matching
        ↓
OverlayManager → shows warning over any app
        ↓
SaathiForegroundService → keeps service alive
```

## License

MIT
