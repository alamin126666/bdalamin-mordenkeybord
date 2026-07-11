package com.modernkey.keyboard.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.modernkey.keyboard.ModernKeyApp
import com.modernkey.keyboard.R
import com.modernkey.keyboard.font.FontStyle

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = getString(R.string.settings_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    private val prefs get() = ModernKeyApp.instance.preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val screen = preferenceManager.createPreferenceScreen(requireContext())
        preferenceScreen = screen

        // ── Keyboard Setup ─────────────────────────────────────────────────────
        addCategory(screen, "⌨️ Keyboard Setup") {
            addButton("Enable Keyboard") {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
            addButton("Set as Default") {
                val imm = requireContext().getSystemService(InputMethodManager::class.java)
                @Suppress("DEPRECATION")
                imm.showInputMethodPicker()
            }
        }

        // ── Appearance ─────────────────────────────────────────────────────────
        addCategory(screen, "🎨 ${getString(R.string.settings_appearance)}") {
            addListPref(
                key = "theme",
                title = getString(R.string.settings_theme),
                entries = resources.getStringArray(R.array.theme_names),
                defaultValue = "0"
            ) { value -> prefs.theme = value.toString().toIntOrNull() ?: 0 }

            addListPref(
                key = "key_height",
                title = getString(R.string.settings_key_height),
                entries = resources.getStringArray(R.array.key_height_names),
                defaultValue = "1"
            ) { value -> prefs.keyHeight = value.toString().toIntOrNull() ?: 1 }

            addSwitch("dark_mode", "Dark Mode", prefs.isDarkMode) {
                prefs.isDarkMode = it
            }
        }

        // ── Font ───────────────────────────────────────────────────────────────
        addCategory(screen, "🔤 ${getString(R.string.settings_font)}") {
            addListPref(
                key = "font_style",
                title = getString(R.string.settings_default_font),
                entries = FontStyle.entries.map { it.displayName }.toTypedArray(),
                defaultValue = "0"
            ) { value -> prefs.currentFontStyle = FontStyle.entries.getOrElse(
                value.toString().toIntOrNull() ?: 0) { FontStyle.NORMAL }
            }

            addSwitch("show_font_bar", getString(R.string.settings_show_font_bar), prefs.showFontBar) {
                prefs.showFontBar = it
            }
        }

        // ── Voice ──────────────────────────────────────────────────────────────
        addCategory(screen, "🎙️ ${getString(R.string.settings_voice)}") {
            addListPref(
                key = "voice_lang",
                title = getString(R.string.settings_voice_language),
                entries = resources.getStringArray(R.array.voice_language_names),
                defaultValue = "0"
            ) { value ->
                val codes = resources.getStringArray(R.array.voice_language_codes)
                prefs.voiceLanguage = codes.getOrElse(value.toString().toIntOrNull() ?: 0) { "en-US" }
            }

            addSwitch("auto_punct", getString(R.string.settings_auto_punctuation), prefs.autoPunctuation) {
                prefs.autoPunctuation = it
            }
        }

        // ── Emoji ──────────────────────────────────────────────────────────────
        addCategory(screen, "😀 ${getString(R.string.settings_emoji)}") {
            addSwitch("show_recent", getString(R.string.settings_show_recent), prefs.showRecentEmoji) {
                prefs.showRecentEmoji = it
            }
            addButton(getString(R.string.settings_clear_recent)) {
                com.modernkey.keyboard.emoji.EmojiRepository.instance.clearRecent()
                showToast("Recent emojis cleared")
            }
        }

        // ── Clipboard ──────────────────────────────────────────────────────────
        addCategory(screen, "📋 ${getString(R.string.settings_clipboard)}") {
            addSwitch("enable_clipboard", getString(R.string.settings_enable_clipboard), prefs.enableClipboard) {
                prefs.enableClipboard = it
            }
            addButton(getString(R.string.settings_clear_clipboard)) {
                showToast("Clipboard cleared")
            }
        }

        // ── Haptics ────────────────────────────────────────────────────────────
        addCategory(screen, "🔊 ${getString(R.string.settings_haptics)}") {
            addSwitch("key_sound", getString(R.string.settings_key_sound), prefs.keySoundEnabled) {
                prefs.keySoundEnabled = it
            }
            addSwitch("vibration", getString(R.string.settings_vibration), prefs.vibrationEnabled) {
                prefs.vibrationEnabled = it
            }
        }

        // ── About ──────────────────────────────────────────────────────────────
        addCategory(screen, "ℹ️ ${getString(R.string.settings_about)}") {
            Preference(requireContext()).apply {
                title = getString(R.string.settings_version)
                summary = "1.0.0"
                screen.addPreference(this)
            }
        }
    }

    // ── DSL helpers ────────────────────────────────────────────────────────────
    private fun addCategory(screen: PreferenceScreen, title: String, block: PreferenceGroup.() -> Unit) {
        val cat = PreferenceCategory(requireContext()).apply { this.title = title }
        screen.addPreference(cat)
        cat.block()
    }

    private fun PreferenceGroup.addSwitch(key: String, title: String, default: Boolean, onChange: (Boolean) -> Unit) {
        addPreference(SwitchPreferenceCompat(requireContext()).apply {
            this.key     = key
            this.title   = title
            this.isChecked = default
            setOnPreferenceChangeListener { _, v -> onChange(v as Boolean); true }
        })
    }

    private fun PreferenceGroup.addButton(title: String, onClick: () -> Unit) {
        addPreference(Preference(requireContext()).apply {
            this.title = title
            setOnPreferenceClickListener { onClick(); true }
        })
    }

    private fun PreferenceGroup.addListPref(
        key: String, title: String, entries: Array<String>,
        defaultValue: String, onChange: (Any) -> Unit
    ) {
        addPreference(ListPreference(requireContext()).apply {
            this.key          = key
            this.title        = title
            this.entries      = entries
            this.entryValues  = Array(entries.size) { it.toString() }
            this.value        = defaultValue
            setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance())
            setOnPreferenceChangeListener { _, v -> onChange(v); true }
        })
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
