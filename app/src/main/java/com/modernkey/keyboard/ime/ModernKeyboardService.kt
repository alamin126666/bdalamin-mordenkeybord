package com.modernkey.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.modernkey.keyboard.ModernKeyApp
import com.modernkey.keyboard.clipboard.ClipboardItem
import com.modernkey.keyboard.clipboard.ClipboardRepository
import com.modernkey.keyboard.emoji.EmojiItem
import com.modernkey.keyboard.font.FontConverter
import com.modernkey.keyboard.font.FontStyle
import com.modernkey.keyboard.ui.emoji.EmojiPanelView
import com.modernkey.keyboard.ui.keyboard.ClipboardPanelView
import com.modernkey.keyboard.ui.keyboard.FontBarView
import com.modernkey.keyboard.ui.keyboard.KeyboardView
import com.modernkey.keyboard.ui.voice.VoiceOverlayView
import com.modernkey.keyboard.voice.VoiceInputManager
import com.modernkey.keyboard.voice.VoiceState
import kotlinx.coroutines.*

class ModernKeyboardService : InputMethodService() {

    private val prefs         get() = ModernKeyApp.instance.preferences
    private val clipboardRepo by lazy {
        ClipboardRepository(ModernKeyApp.instance.database.clipboardDao())
    }
    private val voiceManager  by lazy { VoiceInputManager(this) }
    private val scope          = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentFont   = FontStyle.NORMAL
    private var currentPanel  = Panel.KEYBOARD

    private enum class Panel { KEYBOARD, EMOJI, VOICE, CLIPBOARD }

    // ── Views ───────────────────────────────────────────────────────────────────
    private lateinit var root          : LinearLayout
    private lateinit var fontBar       : FontBarView
    private lateinit var keyboardView  : KeyboardView
    private lateinit var emojiPanel    : EmojiPanelView
    private lateinit var clipboardPanel: ClipboardPanelView
    private lateinit var voiceOverlay  : VoiceOverlayView

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Font Bar
        fontBar = FontBarView(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120
            )
            it.listener = object : FontBarView.OnFontSelectedListener {
                override fun onFontSelected(style: FontStyle) {
                    currentFont = style
                    prefs.currentFontStyle = style
                    keyboardView.setFontStyle(style)
                }
            }
            it.setSelectedFont(prefs.currentFontStyle)
        }

        // Keyboard
        keyboardView = KeyboardView(this).also { kv ->
            kv.keyListener = object : KeyboardView.KeyListener {
                override fun onChar(char: Char) = typeChar(char)
                override fun onBackspace() = deleteChar()
                override fun onSpace()    = typeRaw(" ")
                override fun onEnter()    = typeRaw("\n")
                override fun onShiftToggle() {}
                override fun onEmojiToggle() = togglePanel(Panel.EMOJI)
                override fun onVoiceToggle() = togglePanel(Panel.VOICE)
            }
        }

        // Emoji Panel
        emojiPanel = EmojiPanelView(this).also {
            it.visibility = View.GONE
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 660
            )
            it.listener = object : EmojiPanelView.OnEmojiClickListener {
                override fun onEmojiClicked(emoji: EmojiItem) = typeRaw(emoji.emoji)
                override fun onBackspaceClicked() = deleteChar()
            }
        }

        // Clipboard Panel
        clipboardPanel = ClipboardPanelView(this).also {
            it.visibility = View.GONE
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 660
            )
            it.listener = object : ClipboardPanelView.Listener {
                override fun onItemClicked(item: ClipboardItem) {
                    typeRaw(item.content)
                    showPanel(Panel.KEYBOARD)
                }
                override fun onItemDeleted(item: ClipboardItem) {
                    scope.launch { clipboardRepo.deleteItem(item) }
                }
                override fun onItemPinned(item: ClipboardItem) {
                    scope.launch { clipboardRepo.togglePin(item) }
                }
            }
        }

        // Voice Overlay
        voiceOverlay = VoiceOverlayView(this).also {
            it.visibility = View.GONE
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 400
            )
            it.setOnClickListener { togglePanel(Panel.VOICE) }
        }

        root.addView(fontBar)
        root.addView(keyboardView)
        root.addView(emojiPanel)
        root.addView(clipboardPanel)
        root.addView(voiceOverlay)

        currentFont = prefs.currentFontStyle
        if (!prefs.showFontBar) fontBar.visibility = View.GONE

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        showPanel(Panel.KEYBOARD)
        keyboardView.refreshTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
        scope.cancel()
    }

    // ── Panel Switching ────────────────────────────────────────────────────────
    private fun togglePanel(panel: Panel) {
        if (currentPanel == panel) showPanel(Panel.KEYBOARD) else showPanel(panel)
    }

    private fun showPanel(panel: Panel) {
        currentPanel = panel
        keyboardView .visibility = View.GONE
        emojiPanel   .visibility = View.GONE
        clipboardPanel.visibility= View.GONE
        voiceOverlay .visibility = View.GONE

        when (panel) {
            Panel.KEYBOARD   -> { keyboardView.visibility = View.VISIBLE }
            Panel.EMOJI      -> { emojiPanel.visibility = View.VISIBLE; emojiPanel.refresh() }
            Panel.CLIPBOARD  -> { clipboardPanel.visibility = View.VISIBLE; loadClipboard() }
            Panel.VOICE      -> { voiceOverlay.visibility = View.VISIBLE; startVoiceInput() }
        }
    }

    // ── Text Input ─────────────────────────────────────────────────────────────
    private fun typeChar(char: Char) {
        val raw = char.toString()
        val converted = FontConverter.convert(raw, currentFont)
        currentInputConnection?.commitText(converted, 1)
        if (prefs.enableClipboard && currentPanel == Panel.KEYBOARD) {
            // Clipboard handled at paste-time, not type-time
        }
    }

    private fun typeRaw(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    // ── Voice ──────────────────────────────────────────────────────────────────
    private fun startVoiceInput() {
        if (!voiceManager.isAvailable()) {
            showPanel(Panel.KEYBOARD)
            return
        }
        voiceOverlay.startListening()
        voiceManager.callback = object : VoiceInputManager.Callback {
            override fun onStateChanged(state: VoiceState) {
                if (state == VoiceState.IDLE || state == VoiceState.ERROR) {
                    voiceOverlay.stopListening()
                }
            }
            override fun onPartialResult(text: String) { /* optional: show inline */ }
            override fun onResult(text: String) {
                val converted = FontConverter.convert(text, currentFont)
                typeRaw(converted)
                showPanel(Panel.KEYBOARD)
            }
            override fun onError(errorCode: Int) {
                showPanel(Panel.KEYBOARD)
            }
        }
        voiceManager.startListening(prefs.voiceLanguage)
    }

    // ── Clipboard ──────────────────────────────────────────────────────────────
    private fun loadClipboard() {
        scope.launch {
            clipboardRepo.getAllItems().collect { items ->
                clipboardPanel.submitList(items)
            }
        }
    }
}
