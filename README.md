# NOVA Productivity

NOVA Productivity is an Android utility app designed to run its core features locally on the phone. It does not require a NOVA account, a paid proxy provider, or a NOVA cloud server.

## Included in version 0.2.0

- Premium dark control-center dashboard with connection and feature status.
- On-device HTTP CONNECT and SOCKS5 forward proxy, bound to 127.0.0.1:8787 only.
- User-started foreground service and persistent notification while the proxy is active.
- Built-in browser with a bundled offline ad/tracker domain blocklist.
- The same domain blocklist is applied to domain-based requests handled by the local proxy.
- Quick notes saved on the device.
- Secure random password generator.
- SHA-256 text hashing.
- Basic calculator and common unit conversions.
- Read-only internal storage availability indicator.

## Important Android limits

The proxy listens only on the phone's loopback address. Other devices on Wi-Fi cannot connect to it. Apps on the phone must support a manual HTTP or SOCKS5 proxy setting to use it. Android does not allow an ordinary app to silently change every other app's proxy settings.

The built-in blocklist filters requests made by NOVA's own browser and requests routed through NOVA's local proxy. It is not a system-wide ad blocker or firewall. System-wide filtering would require a separate Android VpnService packet-routing implementation and the user's explicit VPN approval. Apps using their own encrypted DNS or bypassing the proxy are not filtered.

The domain list is bundled in the app and does not update itself from a cloud service. Websites still need an internet connection to load; "local" means NOVA's filtering logic and utility data stay on the device.

NOVA does not decrypt HTTPS traffic, install certificates, or inspect message contents.

## Build

Every push to main triggers the Android GitHub Actions workflow. After a successful run, open Actions, then Android APK build, then the latest run, then Artifacts and download NOVA-Productivity-debug-apk.

Project package: com.nova.productivity  
Minimum Android: 8.0 (API 26)  
Target / compile SDK: 35  
Version: 0.2.0
