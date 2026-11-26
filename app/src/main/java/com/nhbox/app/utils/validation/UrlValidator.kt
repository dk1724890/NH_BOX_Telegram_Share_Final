package com.nhbox.app.utils.validation

import android.webkit.URLUtil

object UrlValidator {
    fun isValidStreamOrDownloadUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        return (trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true)) &&
            URLUtil.isValidUrl(trimmed)
    }

    fun isSecureHttpsUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.trim().startsWith("https://", ignoreCase = true) && URLUtil.isValidUrl(url.trim())
    }
}
