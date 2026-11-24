# NOVA Productivity

NOVA Productivity is a local-first Android control center. Version 0.5.0 refreshes the Life OS dashboard and improves the built-in Study Hub while retaining the existing proxy, browser and utilities.

## Version 0.5.0 — Control Center redesign and Study Hub search

- Redesigned Home dashboard with a premium hero panel, quick-launch tiles, task and goal metrics, money overview, and on-device privacy/storage indicators.
- Updated NOVA app-bar branding and navigation styling.
- Study Hub now remembers recent searches on the phone.
- Search filters include education level, subject, English/Hindi language, result type, balanced/exact phrase matching, and PDF sorting.
- PDF discovery uses the public Internet Archive search API with popularity ordering and paginated results.
- Open text references can be discovered from Wikipedia, Wikibooks and Wikisource in English or Hindi.
- Download PDFs through Android Download Manager into the phone's Downloads folder.
- Save quick revision extracts, longer source extracts and personal notes in NOVA's offline library.
- Topic-search shortcuts for NCERT, DIKSHA, SWAYAM and OpenStax.
- No API key, subscription or NOVA account is required for the built-in search and note library.

## Existing Life OS features

- Five-area navigation: Home, Life, Money, Control and More.
- Tasks, goals, daily habits, ready-made routines, universal local search and a local activity timeline.
- Manual income/expense tracking and monthly totals.
- Existing on-device HTTP CONNECT and SOCKS5 proxy bound to 127.0.0.1:8787.
- Built-in browser with bundled offline ad/tracker domain blocklist.
- Quick notes, secure random password generator, SHA-256 hash tool, calculator and common unit conversions.
- Local-first SharedPreferences storage for Life OS and Study Hub metadata.

## Important limitations

- Study Hub searches the Internet Archive and Wikimedia project APIs; it cannot search every website or guarantee syllabus alignment.
- Search-result availability and download rights vary by publisher. Open each source item page and check its licence/copyright terms before reusing or sharing a file.
- Quick and detailed revision notes are source-based extracts, not AI-generated or syllabus-complete chapters.
- NCERT, DIKSHA, SWAYAM and OpenStax shortcuts open topic searches on their websites; the provider controls the content and download availability.
- Downloaded PDFs are stored by Android Download Manager in the phone's Downloads folder. Removing a PDF from NOVA's library also removes its associated Download Manager entry.
- The local proxy is not a VPN and does not automatically route all phone traffic. The browser/proxy blocklist is not system-wide; system-wide filtering requires an opt-in Android VpnService.
- Money records are manual and are not bank-synced. NOVA does not make payments.
- Routines are checklists, not automated Android actions. Full device automation, encrypted vault, local web server, device bridge, backup/restore and advanced DNS firewall are future modules.
- Android permissions and operating-system restrictions determine which device controls a third-party app can provide.

## Build

Every push to `main` triggers the Android GitHub Actions build. After a successful run, open Actions, select the latest **Android APK build**, then download the `NOVA-Productivity-debug-apk` artifact.

Project package: `com.nova.productivity`  
Minimum Android: 8.0 (API 26)  
Target / compile SDK: 35  
Version: 0.5.0
