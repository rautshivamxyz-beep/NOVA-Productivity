# NOVA Productivity

NOVA Productivity is a local-first Android life-control app. The 0.4.0 release adds a Study Hub to the 0.3.0 Life OS foundation. a unified Life OS dashboard with on-device planning and money tracking while retaining the original browser, local proxy and everyday tools.

## Version 0.4.0 — Study Hub and offline learning library

- Search public Internet Archive collections for downloadable PDF material by topic.
- Create quick revision notes and longer introductory notes from linked Wikipedia topic extracts.
- Save personal notes locally and browse the offline library.
- Download public PDFs through Android Download Manager into the phone Downloads folder.
- Topic-search shortcuts for NCERT, DIKSHA, SWAYAM and OpenStax.
- No API key, account or subscription is required for the built-in search and notes features.

## Version 0.3.0 — Life OS foundation

- New five-area navigation: Home, Life, Money, Control and More.
- Life Command Center with tasks, goal progress, daily habits and ready-made routine checklists.
- Universal search across NOVA tasks, goals, habits, routines and the saved quick note.
- Local life timeline records key actions taken in NOVA.
- Money manager records manually entered income and expenses, categories and notes, with monthly totals and balance.
- New device control area links to the existing local HTTP/SOCKS5 proxy, browser and utility tools.
- App-private SharedPreferences JSON storage; no account or cloud service is required for these features.
- Premium dark Compose interface designed for simple one-tap workflows.

## Existing tools retained

- On-device HTTP CONNECT and SOCKS5 forward proxy, bound to 127.0.0.1:8787 only.
- User-started foreground service and persistent notification while the proxy is active.
- Built-in browser with bundled offline ad/tracker domain blocklist.
- Quick notes, secure random password generator, SHA-256 hash tool, calculator and common unit conversions.
- Read-only internal storage availability indicator.

## Important limitations

- The proxy listens only on the phone's loopback address. It is not a VPN and does not automatically route all phone traffic.
- The browser and proxy blocker is not system-wide. System-wide filtering requires an opt-in Android VpnService implementation.
- Study Hub searches public Internet Archive PDFs and Wikipedia introductory extracts. Search coverage is not universal; official-source shortcuts open topic searches, and each publisher controls availability and licensing. Revision notes are source-based extracts, not AI-written syllabus-complete notes.
- Money records are manual personal records, not bank-synced statements. NOVA does not connect to a bank or initiate payments.
- Routines in 0.3.0 are daily checklists, not automated Android actions. The automation engine is planned for a future release.
- Encrypted vault, local web server, device bridge, backup/restore, advanced DNS firewall and full automation are future modules, not included in 0.3.0.
- Android restrictions and permissions determine which device controls a third-party app can provide.

## Build

Every push to main triggers the Android GitHub Actions workflow. After a successful run, open Actions, choose Android APK build, open the latest run, then download the NOVA-Productivity-debug-apk artifact.

Project package: `com.nova.productivity`  
Minimum Android: 8.0 (API 26)  
Target / compile SDK: 35  
Version: 0.4.0
