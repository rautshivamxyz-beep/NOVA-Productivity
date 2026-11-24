package com.nova.productivity

import android.content.Context

object NovaBlocklist {
    // Bundled offline defaults. Matching is exact-domain or subdomain matching.
    private val domains = setOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adnxs.com",
        "adsrvr.org",
        "adform.net",
        "advertising.com",
        "taboola.com",
        "outbrain.com",
        "criteo.com",
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "smartadserver.com",
        "casalemedia.com",
        "quantserve.com",
        "scorecardresearch.com",
        "bluekai.com",
        "demdex.net",
        "agkn.com",
        "exelator.com",
        "google-analytics.com",
        "googletagmanager.com",
        "analytics.google.com",
        "hotjar.com",
        "clarity.ms",
        "segment.io",
        "mixpanel.com",
        "amplitude.com",
        "app-measurement.com",
        "adjust.com",
        "appsflyer.com",
        "branch.io",
        "kochava.com",
        "facebook.net",
        "connect.facebook.net",
        "pixel.facebook.com",
        "ads-twitter.com",
        "analytics.twitter.com",
        "snap.licdn.com",
        "static.ads-twitter.com",
        "tracking.miui.com",
        "tracking.intl.miui.com"
    )

    val domainCount: Int
        get() = domains.size

    fun isBlocked(host: String): Boolean {
        val normalized = host.trim().trimEnd('.').lowercase()
        if (normalized.isBlank()) return false
        return domains.any { normalized == it || normalized.endsWith("." + it) }
    }

    fun blockedTotal(context: Context): Int {
        return context.getSharedPreferences("nova_local_data", Context.MODE_PRIVATE).getInt("blocked_total", 0)
    }

    fun recordBlocked(context: Context) {
        val prefs = context.getSharedPreferences("nova_local_data", Context.MODE_PRIVATE)
        synchronized(this) {
            val count = prefs.getInt("blocked_total", 0)
            prefs.edit().putInt("blocked_total", count + 1).apply()
        }
    }
}
