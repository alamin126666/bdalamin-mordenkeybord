package com.modernkey.keyboard.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.modernkey.keyboard.font.FontStyle

class KeyboardPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FONT_STYLE    = "font_style"
        private const val KEY_THEME         = "theme"
        private const val KEY_KEY_HEIGHT    = "key_height"
        private const val KEY_SHOW_FONT_BAR = "show_font_bar"
        private const val KEY_VOICE_LANG    = "voice_language"
        private const val KEY_AUTO_PUNCT    = "auto_punctuation"
        private const val KEY_SHOW_RECENT   = "show_recent_emoji"
        private const val KEY_ENABLE_CLIP   = "enable_clipboard"
        private const val KEY_MAX_CLIP      = "max_clipboard"
        private const val KEY_KEY_SOUND     = "key_sound"
        private const val KEY_VIBRATION     = "vibration"
        private const val KEY_DARK_MODE     = "dark_mode"
    }

    var currentFontStyle: FontStyle
        get() = FontStyle.entries.getOrElse(
            prefs.getInt(KEY_FONT_STYLE, 0)) { FontStyle.NORMAL }
        set(value) = prefs.edit().putInt(KEY_FONT_STYLE, value.ordinal).apply()

    var theme: Int
        get() = prefs.getInt(KEY_THEME, 0)
        set(value) = prefs.edit().putInt(KEY_THEME, value).apply()

    var keyHeight: Int
        get() = prefs.getInt(KEY_KEY_HEIGHT, 1) // 0=small, 1=medium, 2=large
        set(value) = prefs.edit().putInt(KEY_KEY_HEIGHT, value).apply()

    var showFontBar: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FONT_BAR, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FONT_BAR, value).apply()

    var voiceLanguage: String
        get() = prefs.getString(KEY_VOICE_LANG, "en-US") ?: "en-US"
        set(value) = prefs.edit().putString(KEY_VOICE_LANG, value).apply()

    var autoPunctuation: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PUNCT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_PUNCT, value).apply()

    var showRecentEmoji: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RECENT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_RECENT, value).apply()

    var enableClipboard: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_CLIP, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_CLIP, value).apply()

    var maxClipboardItems: Int
        get() = prefs.getInt(KEY_MAX_CLIP, 20)
        set(value) = prefs.edit().putInt(KEY_MAX_CLIP, value).apply()

    var keySoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
}
