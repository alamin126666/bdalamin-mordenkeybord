package com.modernkey.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.modernkey.keyboard.ui.keyboard.SuggestionStripView
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

    // ── Views ──────────────────────────────────────────────────────────────────
    private lateinit var root          : LinearLayout
    private lateinit var suggestionBar : SuggestionStripView
    private lateinit var fontBar       : FontBarView
    private lateinit var keyboardView  : KeyboardView
    private lateinit var emojiPanel    : EmojiPanelView
    private lateinit var clipboardPanel: ClipboardPanelView
    private lateinit var voiceOverlay  : VoiceOverlayView

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    override fun onCreateInputView(): View {
        val dp = resources.displayMetrics.density

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 1. Suggestion Strip
        suggestionBar = SuggestionStripView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (44 * dp).toInt()
            )
            listener = object : SuggestionStripView.OnSuggestionClickListener {
                override fun onSuggestionClicked(word: String) {
                    commitSuggestion(word)
                }
            }
        }

        // 2. Font Bar
        fontBar = FontBarView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (40 * dp).toInt()
            )
            listener = object : FontBarView.OnFontSelectedListener {
                override fun onFontSelected(style: FontStyle) {
                    currentFont = style
                    prefs.currentFontStyle = style
                    keyboardView.setFontStyle(style)
                }
            }
            setSelectedFont(prefs.currentFontStyle)
            visibility = if (prefs.showFontBar) View.VISIBLE else View.GONE
        }

        // 3. Keyboard
        keyboardView = KeyboardView(this).apply {
            keyListener = object : KeyboardView.KeyListener {
                override fun onChar(char: Char)   = typeChar(char)
                override fun onBackspace()         = deleteChar()
                override fun onSpace()             = typeText(" ")
                override fun onEnter()             = typeText("\n")
                override fun onShiftToggle()       {}
                override fun onSymbolToggle()      {}
                override fun onEmojiToggle()       = togglePanel(Panel.EMOJI)
                override fun onVoiceToggle()       = togglePanel(Panel.VOICE)
            }
        }

        // 4. Emoji Panel
        emojiPanel = EmojiPanelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (260 * dp).toInt()
            )
            visibility = View.GONE
            listener = object : EmojiPanelView.OnEmojiClickListener {
                override fun onEmojiClicked(emoji: EmojiItem) {
                    typeText(emoji.emoji)
                    showPanel(Panel.KEYBOARD)
                }
                override fun onBackspaceClicked() = deleteChar()
            }
        }

        // 5. Clipboard Panel
        clipboardPanel = ClipboardPanelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (260 * dp).toInt()
            )
            visibility = View.GONE
            listener = object : ClipboardPanelView.Listener {
                override fun onItemClicked(item: ClipboardItem) {
                    typeText(item.content)
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

        // 6. Voice Overlay
        voiceOverlay = VoiceOverlayView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (200 * dp).toInt()
            )
            visibility = View.GONE
            setOnClickListener { togglePanel(Panel.VOICE) }
        }

        root.addView(suggestionBar)
        root.addView(fontBar)
        root.addView(keyboardView)
        root.addView(emojiPanel)
        root.addView(clipboardPanel)
        root.addView(voiceOverlay)

        currentFont = prefs.currentFontStyle
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        showPanel(Panel.KEYBOARD)
        keyboardView.refreshTheme()
        suggestionBar.applyTheme()
        updateSuggestions()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
        scope.cancel()
    }

    // ── Panel Control ─────────────────────────────────────────────────────────
    private fun togglePanel(panel: Panel) {
        if (currentPanel == panel) showPanel(Panel.KEYBOARD) else showPanel(panel)
    }

    private fun showPanel(panel: Panel) {
        currentPanel = panel
        keyboardView  .visibility = View.GONE
        emojiPanel    .visibility = View.GONE
        clipboardPanel.visibility = View.GONE
        voiceOverlay  .visibility = View.GONE
        suggestionBar .visibility = View.GONE
        fontBar       .visibility = View.GONE

        when (panel) {
            Panel.KEYBOARD -> {
                keyboardView  .visibility = View.VISIBLE
                suggestionBar .visibility = View.VISIBLE
                if (prefs.showFontBar) fontBar.visibility = View.VISIBLE
            }
            Panel.EMOJI     -> emojiPanel    .apply { visibility = View.VISIBLE; refresh() }
            Panel.CLIPBOARD -> clipboardPanel.apply { visibility = View.VISIBLE; loadClipboard() }
            Panel.VOICE     -> voiceOverlay  .apply { visibility = View.VISIBLE; startVoiceInput() }
        }
    }

    // ── Text Input ────────────────────────────────────────────────────────────
    private fun typeChar(char: Char) {
        val converted = FontConverter.convert(char.toString(), currentFont)
        currentInputConnection?.commitText(converted, 1)
        updateSuggestions()
    }

    private fun typeText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
        updateSuggestions()
    }

    private fun commitSuggestion(word: String) {
        // Replace current word with suggestion
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val wordStart = before.trimEnd().lastIndexOf(' ').let {
            if (it == -1) 0 else it + 1
        }
        val toDelete = before.length - wordStart
        ic.deleteSurroundingText(toDelete, 0)
        ic.commitText("$word ", 1)
        updateSuggestions()
    }

    // ── Suggestions ───────────────────────────────────────────────────────────
    private fun updateSuggestions() {
        val ic     = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
        val word   = before.trimEnd().split(" ").lastOrNull()?.trim() ?: ""
        val suggestions = getSuggestions(word)
        suggestionBar.setSuggestions(suggestions)
    }

    private fun getSuggestions(prefix: String): List<String> {
        if (prefix.length < 2) return listOf("the", "and", "is")
        val p = prefix.lowercase()
        val commonWords = listOf(
            "the","and","is","in","it","of","to","you","that","was","for",
            "on","are","with","as","this","have","from","or","one","had",
            "but","not","what","all","were","they","been","when","there",
            "can","said","each","about","how","their","if","will","up",
            "other","into","has","her","him","his","how","man","now",
            "only","see","she","them","then","there","these","they","time",
            "two","way","who","will","would","your","could","should","also",
            "after","before","more","very","just","know","take","good","much",
            "well","back","come","here","need","like","make","want","think",
            "great","work","life","world","still","every","never","always",
            "happy","thank","hello","please","sorry","today","tomorrow",
            "because","something","everything","nothing","anything","anyone",
            "someone","everyone","another","people","really","around","again"
        )
        val matches = commonWords.filter { it.startsWith(p) && it != p }
            .sortedBy { it.length }.take(3)

        return when {
            matches.size >= 3 -> matches
            matches.size == 2 -> matches + prefix
            matches.size == 1 -> listOf(prefix) + matches
            else -> listOf(prefix, "${prefix}ing", "${prefix}ed").take(3)
        }
    }

    // ── Voice Input ───────────────────────────────────────────────────────────
    private fun startVoiceInput() {
        if (!voiceManager.isAvailable()) { showPanel(Panel.KEYBOARD); return }
        voiceOverlay.startListening()
        voiceManager.callback = object : VoiceInputManager.Callback {
            override fun onStateChanged(state: VoiceState) {
                if (state == VoiceState.IDLE || state == VoiceState.ERROR) {
                    voiceOverlay.stopListening()
                }
            }
            override fun onPartialResult(text: String) {}
            override fun onResult(text: String) {
                val converted = FontConverter.convert(text, currentFont)
                typeText(converted)
                showPanel(Panel.KEYBOARD)
            }
            override fun onError(errorCode: Int) { showPanel(Panel.KEYBOARD) }
        }
        voiceManager.startListening(prefs.voiceLanguage)
    }

    // ── Clipboard ─────────────────────────────────────────────────────────────
    private fun loadClipboard() {
        scope.launch {
            clipboardRepo.getAllItems().collect { items ->
                clipboardPanel.submitList(items)
            }
        }
    }
}
