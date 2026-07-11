package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.modernkey.keyboard.ModernKeyApp
import com.modernkey.keyboard.font.FontStyle

class FontBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, def: Int = 0
) : View(context, attrs, def) {

    interface OnFontSelectedListener { fun onFontSelected(style: FontStyle) }
    var listener: OnFontSelectedListener? = null

    private val prefs get() = ModernKeyApp.instance.preferences
    private val dp    = resources.displayMetrics.density
    private val styles = FontStyle.entries
    private var selectedIdx = 0

    // M3 colors
    private var cBg      = Color.parseColor("#2B2930")
    private var cChip    = Color.parseColor("#49454F")
    private var cActive  = Color.parseColor("#6750A4")
    private var cText    = Color.parseColor("#E6E1E5")
    private var cActTxt  = Color.parseColor("#FFFFFF")
    private var cDivider = Color.parseColor("#49454F")

    private val bgPaint   = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txtPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.DEFAULT
    }
    private val divPaint  = Paint(Paint.ANTI_ALIAS_FLAG)

    private val chipRects = mutableListOf<RectF>()
    private var scrollX   = 0f
    private var lastTouchX = 0f
    private var isDragging  = false

    private val chipW  = 68f * dp
    private val chipH  = 28f * dp
    private val chipMg = 6f * dp
    private val cr     = 20f * dp

    fun setSelectedFont(style: FontStyle) {
        selectedIdx = styles.indexOf(style).coerceAtLeast(0)
        invalidate()
    }

    fun applyTheme() {
        if (prefs.isDarkMode) {
            cBg = Color.parseColor("#2B2930"); cChip = Color.parseColor("#49454F")
            cActive = Color.parseColor("#6750A4"); cText = Color.parseColor("#938F99")
            cActTxt = Color.WHITE; cDivider = Color.parseColor("#49454F")
        } else {
            cBg = Color.parseColor("#F3EFF4"); cChip = Color.parseColor("#E8DEF8")
            cActive = Color.parseColor("#6750A4"); cText = Color.parseColor("#49454F")
            cActTxt = Color.WHITE; cDivider = Color.parseColor("#CAC4D0")
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Background
        bgPaint.color = cBg
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Bottom divider
        divPaint.color = cDivider
        canvas.drawLine(0f, height - 1f, width.toFloat(), height - 1f, divPaint)

        chipRects.clear()
        val h = height.toFloat()
        val chipY = (h - chipH) / 2f
        var x = chipMg - scrollX

        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), height.toFloat())

        for ((i, style) in styles.withIndex()) {
            val r = RectF(x, chipY, x + chipW, chipY + chipH)
            chipRects.add(r)

            val isSelected = i == selectedIdx
            chipPaint.color = if (isSelected) cActive else cChip
            canvas.drawRoundRect(r, cr, cr, chipPaint)

            txtPaint.color    = if (isSelected) cActTxt else cText
            txtPaint.textSize = 12f * dp
            val tm = txtPaint.fontMetrics
            val label = style.preview.take(4)
            canvas.drawText(label, r.centerX(), r.centerY() - (tm.ascent + tm.descent) / 2f, txtPaint)

            x += chipW + chipMg
        }
        canvas.restore()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = ev.x; isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = lastTouchX - ev.x
                if (Math.abs(dx) > 8f) isDragging = true
                if (isDragging) {
                    val maxScroll = (styles.size * (chipW + chipMg)) - width + chipMg
                    scrollX = (scrollX + dx).coerceIn(0f, maxScroll.coerceAtLeast(0f))
                    lastTouchX = ev.x
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val touchX = ev.x + scrollX
                    chipRects.forEachIndexed { i, r ->
                        val adjustedR = RectF(r.left + scrollX, r.top, r.right + scrollX, r.bottom)
                        if (adjustedR.contains(ev.x + scrollX, ev.y + 0f)) {
                            // simpler: find which chip
                        }
                    }
                    // Find tapped chip by raw position
                    val rawX   = ev.x + scrollX - chipMg
                    val idx    = (rawX / (chipW + chipMg)).toInt()
                    if (idx in styles.indices) {
                        selectedIdx = idx
                        listener?.onFontSelected(styles[idx])
                        invalidate()
                    }
                }
            }
        }
        return true
    }
}
