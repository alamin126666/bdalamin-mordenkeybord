package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.modernkey.keyboard.ModernKeyApp

class SuggestionStripView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    interface OnSuggestionClickListener {
        fun onSuggestionClicked(word: String)
    }

    var listener: OnSuggestionClickListener? = null
    private var suggestions: List<String> = listOf("the", "and", "is")

    private val dp    = resources.displayMetrics.density
    private val prefs get() = ModernKeyApp.instance.preferences

    // Colors
    private var cBg       = Color.parseColor("#2B2930")
    private var cText     = Color.parseColor("#E6E1E5")
    private var cCenter   = Color.parseColor("#D0BCFF")
    private var cDivider  = Color.parseColor("#49454F")

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val divPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Touch zones
    private val zones = mutableListOf<Pair<RectF, String>>()

    fun setSuggestions(list: List<String>) {
        suggestions = list.take(3)
        invalidate()
    }

    fun applyTheme() {
        if (prefs.isDarkMode) {
            cBg = Color.parseColor("#2B2930")
            cText = Color.parseColor("#E6E1E5")
            cCenter = Color.parseColor("#D0BCFF")
            cDivider = Color.parseColor("#49454F")
        } else {
            cBg = Color.parseColor("#FFFFFF")
            cText = Color.parseColor("#1C1B1F")
            cCenter = Color.parseColor("#6750A4")
            cDivider = Color.parseColor("#CAC4D0")
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        bgPaint.color = cBg
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        zones.clear()
        if (suggestions.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val sectionW = w / 3f
        val cy = h / 2f

        suggestions.forEachIndexed { i, word ->
            val cx = sectionW * i + sectionW / 2f

            // Highlight center suggestion
            if (i == 1) {
                txtPaint.color    = cCenter
                txtPaint.textSize = 14.5f * dp
                txtPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            } else {
                txtPaint.color    = cText
                txtPaint.textSize = 13.5f * dp
                txtPaint.typeface = Typeface.DEFAULT
            }

            val tm = txtPaint.fontMetrics
            canvas.drawText(word, cx, cy - (tm.ascent + tm.descent) / 2f, txtPaint)

            // Touch zone
            zones.add(Pair(RectF(sectionW * i, 0f, sectionW * (i + 1), h), word))
        }

        // Dividers
        divPaint.color       = cDivider
        divPaint.strokeWidth = 1f
        val divH = h * 0.4f
        val divY1 = (h - divH) / 2f
        canvas.drawLine(sectionW, divY1, sectionW, divY1 + divH, divPaint)
        canvas.drawLine(sectionW * 2, divY1, sectionW * 2, divY1 + divH, divPaint)

        // Bottom separator
        divPaint.color = cDivider
        canvas.drawLine(0f, h - 1f, w, h - 1f, divPaint)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            zones.firstOrNull { it.first.contains(ev.x, ev.y) }?.let {
                listener?.onSuggestionClicked(it.second)
            }
        }
        return true
    }
}
