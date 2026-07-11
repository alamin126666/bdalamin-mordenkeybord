package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.modernkey.keyboard.ModernKeyApp
import com.modernkey.keyboard.font.FontConverter
import com.modernkey.keyboard.font.FontStyle

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ── Callbacks ──────────────────────────────────────────────────────────────
    interface KeyListener {
        fun onChar(char: Char)
        fun onBackspace()
        fun onSpace()
        fun onEnter()
        fun onEmojiToggle()
        fun onVoiceToggle()
        fun onShiftToggle()
    }

    var keyListener: KeyListener? = null

    // ── State ──────────────────────────────────────────────────────────────────
    private var isShifted = false
    private var isCapsLock = false
    private var pressedKey: Key? = null
    private var currentFontStyle: FontStyle = FontStyle.NORMAL

    // ── Theming ────────────────────────────────────────────────────────────────
    private val prefs get() = ModernKeyApp.instance.preferences

    // ── Paints ─────────────────────────────────────────────────────────────────
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 42f
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 26f
    }

    // ── Colors (Light Theme defaults) ──────────────────────────────────────────
    private var bgColor     = Color.parseColor("#F5F5F5")
    private var keyColor    = Color.WHITE
    private var keyActColor = Color.parseColor("#DEDEDE")
    private var keyPresColor= Color.parseColor("#E0E0E0")
    private var keyTxtColor = Color.parseColor("#212121")
    private var borderColor = Color.parseColor("#BDBDBD")

    // ── Keys ───────────────────────────────────────────────────────────────────
    private data class Key(
        val label: String,
        val type: KeyType,
        var rect: RectF = RectF(),
        val isAction: Boolean = false,
        val subLabel: String = ""
    )

    private enum class KeyType {
        CHAR, SPACE, BACKSPACE, ENTER, SHIFT, EMOJI, VOICE, SYMBOL
    }

    private val rows: List<List<Key>> = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P").map { Key(it, KeyType.CHAR) },
        listOf("A","S","D","F","G","H","J","K","L").map   { Key(it, KeyType.CHAR) } +
        listOf(Key("⌫", KeyType.BACKSPACE, isAction = true)),
        listOf(Key("⇧", KeyType.SHIFT, isAction = true)) +
        listOf("Z","X","C","V","B","N","M").map            { Key(it, KeyType.CHAR) } +
        listOf(Key(".", KeyType.SYMBOL)),
        listOf(
            Key("😀", KeyType.EMOJI, isAction = true),
            Key(",", KeyType.SYMBOL),
            Key("SPACE", KeyType.SPACE, isAction = true),
            Key("🎙️", KeyType.VOICE, isAction = true),
            Key("↵", KeyType.ENTER, isAction = true)
        )
    )

    // ── Dimensions ─────────────────────────────────────────────────────────────
    private val margin  = 5f
    private val cornerR = 12f
    private var keyH    = 0f
    private var totalH  = 0f

    // ── Init ───────────────────────────────────────────────────────────────────
    init { applyTheme() }

    private fun applyTheme() {
        if (prefs.isDarkMode) {
            bgColor     = Color.parseColor("#1A1A1A")
            keyColor    = Color.parseColor("#2C2C2C")
            keyActColor = Color.parseColor("#141414")
            keyPresColor= Color.parseColor("#3C3C3C")
            keyTxtColor = Color.WHITE
            borderColor = Color.parseColor("#424242")
        } else {
            bgColor     = Color.parseColor("#F5F5F5")
            keyColor    = Color.WHITE
            keyActColor = Color.parseColor("#DEDEDE")
            keyPresColor= Color.parseColor("#E0E0E0")
            keyTxtColor = Color.parseColor("#212121")
            borderColor = Color.parseColor("#BDBDBD")
        }
    }

    fun refreshTheme() { applyTheme(); invalidate() }

    // ── Layout ─────────────────────────────────────────────────────────────────
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        keyH   = when (prefs.keyHeight) { 0 -> 120f; 2 -> 165f; else -> 140f }
        totalH = keyH * rows.size + margin * (rows.size + 1)
        setMeasuredDimension(w.toInt(), totalH.toInt())
        layoutKeys(w)
    }

    private fun layoutKeys(totalWidth: Float) {
        var y = margin
        for ((rowIdx, row) in rows.withIndex()) {
            val isBottomRow = rowIdx == rows.lastIndex
            val isRow2 = rowIdx == 1
            val isRow3 = rowIdx == 2
            val count = row.size
            val availW = totalWidth - margin * (count + 1)
            val baseW  = availW / count

            var x = margin
            for (key in row) {
                val kw = when {
                    key.type == KeyType.SPACE        -> baseW * 3.2f
                    key.type == KeyType.BACKSPACE && isRow2 -> baseW * 1.4f
                    key.type == KeyType.SHIFT && isRow3     -> baseW * 1.4f
                    key.type == KeyType.ENTER        -> baseW * 1.2f
                    else                             -> baseW
                }
                key.rect.set(x, y, x + kw, y + keyH)
                x += kw + margin
            }
            y += keyH + margin
        }
    }

    // ── Draw ───────────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(bgColor)
        for (row in rows) {
            for (key in row) {
                drawKey(canvas, key)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val pressed = key == pressedKey
        val fill = when {
            pressed         -> keyPresColor
            key.isAction   -> keyActColor
            else            -> keyColor
        }
        keyPaint.color = fill
        canvas.drawRoundRect(key.rect, cornerR, cornerR, keyPaint)

        keyBorderPaint.color = borderColor
        canvas.drawRoundRect(key.rect, cornerR, cornerR, keyBorderPaint)

        val cx = key.rect.centerX()
        val cy = key.rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

        textPaint.color = keyTxtColor
        val label = when {
            key.type == KeyType.CHAR && isShifted -> key.label.uppercase()
            key.type == KeyType.CHAR              -> key.label.lowercase()
            key.type == KeyType.SHIFT -> if (isCapsLock) "⇪" else if (isShifted) "⇧" else "⇧"
            else -> key.label
        }
        canvas.drawText(label, cx, cy, textPaint)
    }

    // ── Touch ──────────────────────────────────────────────────────────────────
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKey(event.x, event.y)
                pressedKey = key
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val key = findKey(event.x, event.y)
                if (key != null && key == pressedKey) {
                    onKeyPressed(key)
                }
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float): Key? {
        for (row in rows) {
            for (key in row) {
                if (key.rect.contains(x, y)) return key
            }
        }
        return null
    }

    private fun onKeyPressed(key: Key) {
        if (prefs.vibrationEnabled) vibrate()

        when (key.type) {
            KeyType.CHAR -> {
                val c = if (isShifted || isCapsLock) key.label[0].uppercaseChar()
                        else key.label[0].lowercaseChar()
                keyListener?.onChar(c)
                if (isShifted && !isCapsLock) {
                    isShifted = false
                    invalidate()
                }
            }
            KeyType.SYMBOL    -> keyListener?.onChar(key.label[0])
            KeyType.BACKSPACE -> keyListener?.onBackspace()
            KeyType.SPACE     -> keyListener?.onSpace()
            KeyType.ENTER     -> keyListener?.onEnter()
            KeyType.EMOJI     -> keyListener?.onEmojiToggle()
            KeyType.VOICE     -> keyListener?.onVoiceToggle()
            KeyType.SHIFT     -> {
                when {
                    isCapsLock -> { isCapsLock = false; isShifted = false }
                    isShifted  -> { isCapsLock = true }
                    else       -> { isShifted = true }
                }
                keyListener?.onShiftToggle()
                invalidate()
            }
        }
    }

    // ── Vibration ──────────────────────────────────────────────────────────────
    private fun vibrate() {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun setFontStyle(style: FontStyle) {
        currentFontStyle = style
    }
}
