# NOVA Productivity

A non-AI Android utility app project. The initial build includes a dashboard, network status, proxy endpoint reachability check, Android Private DNS settings shortcut, local note field, secure random password generator, and SHA-256 utility.

## Build an APK on GitHub

Every push to `main` triggers the Android GitHub Actions workflow. When it succeeds, open **Actions → Android APK build → latest successful run → Artifacts** and download `NOVA-Productivity-debug-apk`.

## Build locally

Open this repository in Android Studio (Giraffe or newer), allow Gradle sync, then choose **Build → Build APK(s)**.

## Important feature boundaries

This is a starter implementation, not the complete planned super-app. Proxy profiles currently test TCP endpoint reachability only; they do not tunnel device traffic. DNS ad blocking, VPN routing, per-app firewall rules, packet inspection, ping/traceroute, and a full file manager are not implemented yet. Android permits only one active VPN service at a time, so VPN-based privacy features must be designed carefully.

## Project

- Package: `com.nova.productivity`
- Minimum Android: 8.0 (API 26)
- Target / compile SDK: 35
- Version: 0.1.0
