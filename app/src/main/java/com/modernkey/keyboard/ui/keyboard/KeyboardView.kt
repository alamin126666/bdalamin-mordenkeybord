package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.modernkey.keyboard.ModernKeyApp
import com.modernkey.keyboard.font.FontStyle
import kotlin.math.roundToInt

class KeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
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
        fun onSymbolToggle()
    }
    var keyListener: KeyListener? = null

    // ── State ─────────────────────────────────────────────────────────────────
    private var isShifted  = false
    private var isCapsLock = false
    private var pressedKey : Key? = null
    private var pressAnim  = 0f
    private val prefs get() = ModernKeyApp.instance.preferences

    // ── Material 3 Color Tokens ───────────────────────────────────────────────
    private var cBg         = Color.parseColor("#1C1B1F")
    private var cKey        = Color.parseColor("#2B2930")
    private var cKeyAct     = Color.parseColor("#3B383E")
    private var cKeyPress   = Color.parseColor("#49454F")
    private var cEnter      = Color.parseColor("#6750A4")
    private var cEnterText  = Color.parseColor("#FFFFFF")
    private var cText       = Color.parseColor("#E6E1E5")
    private var cHint       = Color.parseColor("#938F99")
    private var cShadow     = Color.parseColor("#000000")

    // Light theme tokens
    private val cBgL        = Color.parseColor("#F3EFF4")
    private val cKeyL       = Color.parseColor("#FFFFFF")
    private val cKeyActL    = Color.parseColor("#CAC4D0")
    private val cKeyPressL  = Color.parseColor("#E8DEF8")
    private val cEnterL     = Color.parseColor("#6750A4")
    private val cTextL      = Color.parseColor("#1C1B1F")
    private val cHintL      = Color.parseColor("#79747E")

    // ── Paints ────────────────────────────────────────────────────────────────
    private val bgPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txtPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface  = Typeface.DEFAULT
        textAlign = Paint.Align.RIGHT
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style     = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ── Key Model ─────────────────────────────────────────────────────────────
    enum class KeyType { CHAR, SPACE, BACKSPACE, ENTER, SHIFT, EMOJI, VOICE, SYMBOL }

    data class Key(
        val label: String,
        val type : KeyType,
        var rect : RectF = RectF(),
        val hint : String = "",
        val isAction: Boolean = false
    )

    // Number hints for top row
    private val numberHints = listOf("1","2","3","4","5","6","7","8","9","0")

    private val rows: List<List<Key>> = listOf(
        // Row 1 — Q…P with number hints
        listOf("Q","W","E","R","T","Y","U","I","O","P")
            .mapIndexed { i, c -> Key(c, KeyType.CHAR, hint = numberHints[i]) },
        // Row 2 — A…L + Backspace
        listOf("A","S","D","F","G","H","J","K","L")
            .map { Key(it, KeyType.CHAR) } +
        listOf(Key("⌫", KeyType.BACKSPACE, isAction = true)),
        // Row 3 — Shift + Z…M + dot
        listOf(Key("shift", KeyType.SHIFT, isAction = true)) +
        listOf("Z","X","C","V","B","N","M").map { Key(it, KeyType.CHAR) } +
        listOf(Key(".", KeyType.SYMBOL)),
        // Row 4 — action row
        listOf(
            Key("?123",  KeyType.SYMBOL,    isAction = true),
            Key("emoji", KeyType.EMOJI,     isAction = true),
            Key(" ",     KeyType.SPACE,     isAction = true),
            Key("mic",   KeyType.VOICE,     isAction = true),
            Key("enter", KeyType.ENTER,     isAction = true)
        )
    )

    // ── Dimensions ────────────────────────────────────────────────────────────
    private val dp  = resources.displayMetrics.density
    private val mg  = 5f * dp          // key margin
    private val cr  = 10f * dp         // corner radius
    private val sh  = 3f * dp          // shadow size
    private var kH  = 0f               // key height
    private var totalH = 0f

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)  // for shadow support
        applyTheme()
    }

    fun applyTheme() {
        if (prefs.isDarkMode) {
            cBg = Color.parseColor("#1C1B1F"); cKey = Color.parseColor("#2B2930")
            cKeyAct = Color.parseColor("#3B383E"); cKeyPress = Color.parseColor("#49454F")
            cEnter = Color.parseColor("#6750A4"); cText = Color.parseColor("#E6E1E5")
            cHint = Color.parseColor("#938F99")
        } else {
            cBg = cBgL; cKey = cKeyL; cKeyAct = cKeyActL; cKeyPress = cKeyPressL
            cEnter = cEnterL; cText = cTextL; cHint = cHintL
        }
    }

    fun refreshTheme() { applyTheme(); invalidate() }

    // ── Layout ────────────────────────────────────────────────────────────────
    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val w = MeasureSpec.getSize(wSpec).toFloat()
        kH = when (prefs.keyHeight) { 0 -> 44f * dp; 2 -> 58f * dp; else -> 52f * dp }
        totalH = kH * rows.size + mg * (rows.size + 1) + sh * rows.size
        setMeasuredDimension(w.toInt(), totalH.toInt())
        layoutKeys(w)
    }

    private fun layoutKeys(W: Float) {
        var y = mg
        for ((ri, row) in rows.withIndex()) {
            val n = row.size
            val avail = W - mg * (n + 1)
            val base  = avail / n

            // Compute individual widths
            val widths = row.map { k ->
                when {
                    ri == 3 && k.type == KeyType.SPACE     -> base * 3.8f
                    ri == 3 && k.type == KeyType.SYMBOL    -> base * 1.1f
                    ri == 3 && k.type == KeyType.EMOJI     -> base * 0.9f
                    ri == 3 && k.type == KeyType.VOICE     -> base * 0.9f
                    ri == 3 && k.type == KeyType.ENTER     -> base * 1.4f
                    ri == 2 && k.type == KeyType.SHIFT     -> base * 1.4f
                    ri == 1 && k.type == KeyType.BACKSPACE -> base * 1.4f
                    else -> base
                }
            }
            // Redistribute leftover
            val used   = widths.sum() + mg * (n - 1)
            val extra  = (W - mg * 2 - used) / n

            var x = mg
            for ((ki, key) in row.withIndex()) {
                val kw = widths[ki] + extra
                key.rect.set(x, y, x + kw, y + kH)
                x += kw + mg
            }
            y += kH + mg + sh
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        // Background
        bgPaint.color = cBg
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (row in rows) {
            for (key in row) {
                drawKey(canvas, key)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val pressed = key == pressedKey
        val r = key.rect

        // Shadow
        shadowPaint.color = Color.argb(if (prefs.isDarkMode) 80 else 40, 0, 0, 0)
        val sr = RectF(r.left + 1f, r.top + sh, r.right - 1f, r.bottom + sh)
        canvas.drawRoundRect(sr, cr, cr, shadowPaint)

        // Key face
        keyPaint.color = when {
            pressed             -> cKeyPress
            key.type == KeyType.ENTER -> cEnter
            key.isAction        -> cKeyAct
            else                -> cKey
        }
        canvas.drawRoundRect(r, cr, cr, keyPaint)

        val cx = r.centerX()
        val cy = r.centerY()

        when (key.type) {
            KeyType.CHAR -> {
                // Main letter
                txtPaint.color    = cText
                txtPaint.textSize = 17f * dp
                val label = if (isShifted || isCapsLock) key.label.uppercase() else key.label.lowercase()
                val tm = txtPaint.fontMetrics
                canvas.drawText(label, cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)

                // Number hint (top-right)
                if (key.hint.isNotEmpty()) {
                    hintPaint.color    = cHint
                    hintPaint.textSize = 9f * dp
                    canvas.drawText(key.hint, r.right - 6f * dp, r.top + 13f * dp, hintPaint)
                }
            }
            KeyType.SYMBOL -> {
                txtPaint.color    = cText
                txtPaint.textSize = 15f * dp
                val tm = txtPaint.fontMetrics
                canvas.drawText(key.label, cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)
            }
            KeyType.BACKSPACE -> drawBackspaceIcon(canvas, r)
            KeyType.SHIFT     -> drawShiftIcon(canvas, r)
            KeyType.ENTER     -> drawEnterIcon(canvas, r)
            KeyType.SPACE     -> {
                // "English" or language label
                txtPaint.color    = cHint
                txtPaint.textSize = 12f * dp
                val tm = txtPaint.fontMetrics
                canvas.drawText("EN • BN", cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)
            }
            KeyType.EMOJI -> drawEmojiIcon(canvas, r)
            KeyType.VOICE -> drawMicIcon(canvas, r)
            KeyType.SYMBOL -> {
                txtPaint.color    = cText
                txtPaint.textSize = 13f * dp
                val tm = txtPaint.fontMetrics
                canvas.drawText("?123", cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)
            }
        }

        // Special: ?123 label for SYMBOL action key (row 4)
        if (key.type == KeyType.SYMBOL && key.label == "?123") {
            txtPaint.color    = cText
            txtPaint.textSize = 13f * dp
            val tm = txtPaint.fontMetrics
            canvas.drawText("?123", cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)
        }
    }

    private fun drawBackspaceIcon(canvas: Canvas, r: RectF) {
        val cx = r.centerX(); val cy = r.centerY()
        val s  = 11f * dp
        iconPaint.color = cText
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.8f * dp
        iconPaint.strokeJoin  = Paint.Join.ROUND
        iconPaint.strokeCap   = Paint.Cap.ROUND

        val path = Path().apply {
            moveTo(cx - s * 1.1f, cy)
            lineTo(cx - s * 0.3f, cy - s * 0.75f)
            lineTo(cx + s * 1.1f, cy - s * 0.75f)
            lineTo(cx + s * 1.1f, cy + s * 0.75f)
            lineTo(cx - s * 0.3f, cy + s * 0.75f)
            close()
        }
        canvas.drawPath(path, iconPaint)

        // X inside
        val xo = s * 0.25f
        canvas.drawLine(cx - xo, cy - xo, cx + xo, cy + xo, iconPaint)
        canvas.drawLine(cx + xo, cy - xo, cx - xo, cy + xo, iconPaint)
        iconPaint.style = Paint.Style.FILL
    }

    private fun drawShiftIcon(canvas: Canvas, r: RectF) {
        val cx = r.centerX(); val cy = r.centerY()
        val s  = 10f * dp
        iconPaint.color       = if (isShifted || isCapsLock) Color.parseColor("#D0BCFF") else cText
        iconPaint.style       = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.8f * dp
        iconPaint.strokeJoin  = Paint.Join.ROUND
        iconPaint.strokeCap   = Paint.Cap.ROUND

        val path = Path().apply {
            moveTo(cx, cy - s)
            lineTo(cx + s * 1.1f, cy)
            lineTo(cx + s * 0.5f, cy)
            lineTo(cx + s * 0.5f, cy + s * 0.8f)
            lineTo(cx - s * 0.5f, cy + s * 0.8f)
            lineTo(cx - s * 0.5f, cy)
            lineTo(cx - s * 1.1f, cy)
            close()
        }
        canvas.drawPath(path, iconPaint)

        // Caps lock underline
        if (isCapsLock) {
            iconPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(
                RectF(cx - s * 0.5f, cy + s * 0.9f, cx + s * 0.5f, cy + s * 0.9f + 3f * dp),
                2f, 2f, iconPaint)
        }
        iconPaint.style = Paint.Style.FILL
    }

    private fun drawEnterIcon(canvas: Canvas, r: RectF) {
        val cx = r.centerX(); val cy = r.centerY()
        val s  = 9f * dp
        iconPaint.color       = Color.WHITE
        iconPaint.style       = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.8f * dp
        iconPaint.strokeJoin  = Paint.Join.ROUND
        iconPaint.strokeCap   = Paint.Cap.ROUND

        val path = Path().apply {
            moveTo(cx + s, cy - s * 0.6f)
            lineTo(cx + s, cy + s * 0.2f)
            lineTo(cx - s, cy + s * 0.2f)
            moveTo(cx - s * 0.4f, cy - s * 0.5f)
            lineTo(cx - s, cy + s * 0.2f)
            lineTo(cx - s * 0.4f, cy + s * 0.9f)
        }
        canvas.drawPath(path, iconPaint)
        iconPaint.style = Paint.Style.FILL
    }

    private fun drawEmojiIcon(canvas: Canvas, r: RectF) {
        val cx = r.centerX(); val cy = r.centerY()
        val s  = 10f * dp
        iconPaint.color       = cHint
        iconPaint.style       = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.6f * dp

        // Circle
        canvas.drawCircle(cx, cy, s, iconPaint)

        // Eyes
        iconPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx - s * 0.35f, cy - s * 0.2f, s * 0.12f, iconPaint)
        canvas.drawCircle(cx + s * 0.35f, cy - s * 0.2f, s * 0.12f, iconPaint)

        // Smile arc
        iconPaint.style = Paint.Style.STROKE
        canvas.drawArc(RectF(cx - s * 0.5f, cy, cx + s * 0.5f, cy + s * 0.6f),
            0f, 180f, false, iconPaint)
        iconPaint.style = Paint.Style.FILL
    }

    private fun drawMicIcon(canvas: Canvas, r: RectF) {
        val cx = r.centerX(); val cy = r.centerY()
        val s  = 9f * dp
        iconPaint.color       = cHint
        iconPaint.style       = Paint.Style.STROKE
        iconPaint.strokeWidth = 1.6f * dp
        iconPaint.strokeCap   = Paint.Cap.ROUND

        // Mic body
        canvas.drawRoundRect(
            RectF(cx - s * 0.45f, cy - s * 1.1f, cx + s * 0.45f, cy + s * 0.2f),
            s * 0.45f, s * 0.45f, iconPaint)

        // Arc
        canvas.drawArc(RectF(cx - s, cy - s * 0.2f, cx + s, cy + s * 0.8f),
            0f, 180f, false, iconPaint)

        // Stem
        canvas.drawLine(cx, cy + s * 0.8f, cx, cy + s * 1.1f, iconPaint)
        canvas.drawLine(cx - s * 0.5f, cy + s * 1.1f, cx + s * 0.5f, cy + s * 1.1f, iconPaint)
        iconPaint.style = Paint.Style.FILL
    }

    // ── Touch ─────────────────────────────────────────────────────────────────
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = findKey(ev.x, ev.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val key = findKey(ev.x, ev.y)
                if (key != null && key == pressedKey) fireKey(key)
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> { pressedKey = null; invalidate() }
        }
        return true
    }

    private fun findKey(x: Float, y: Float): Key? {
        for (row in rows) for (key in row) if (key.rect.contains(x, y)) return key
        return null
    }

    private fun fireKey(key: Key) {
        if (prefs.vibrationEnabled) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (key.type) {
            KeyType.CHAR -> {
                val c = if (isShifted || isCapsLock) key.label[0].uppercaseChar()
                        else key.label[0].lowercaseChar()
                keyListener?.onChar(c)
                if (isShifted && !isCapsLock) { isShifted = false; invalidate() }
            }
            KeyType.SYMBOL    -> {
                if (key.label == "?123") keyListener?.onSymbolToggle()
                else keyListener?.onChar(key.label[0])
            }
            KeyType.BACKSPACE -> keyListener?.onBackspace()
            KeyType.SPACE     -> keyListener?.onSpace()
            KeyType.ENTER     -> keyListener?.onEnter()
            KeyType.EMOJI     -> keyListener?.onEmojiToggle()
            KeyType.VOICE     -> keyListener?.onVoiceToggle()
            KeyType.SHIFT     -> {
                when {
                    isCapsLock -> { isCapsLock = false; isShifted = false }
                    isShifted  -> isCapsLock = true
                    else       -> isShifted = true
                }
                keyListener?.onShiftToggle()
                invalidate()
            }
        }
    }

    fun setFontStyle(style: FontStyle) {}
}
