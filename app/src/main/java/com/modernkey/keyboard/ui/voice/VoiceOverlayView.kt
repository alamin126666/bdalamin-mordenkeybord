package com.modernkey.keyboard.ui.voice

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class VoiceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isListening = false
    private var pulseRadius = 0f
    private var pulseAlpha  = 255

    private val micPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        style = Paint.Style.FILL
    }
    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1200
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val f = it.animatedValue as Float
            pulseRadius = 80f + f * 60f
            pulseAlpha  = ((1f - f) * 180).toInt()
            invalidate()
        }
    }

    fun startListening() {
        isListening = true
        pulseAnimator.start()
        invalidate()
    }

    fun stopListening() {
        isListening = false
        pulseAnimator.cancel()
        pulseRadius = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f

        if (isListening && pulseRadius > 0f) {
            pulsePaint.alpha = pulseAlpha
            canvas.drawCircle(cx, cy, pulseRadius, pulsePaint)
        }

        // Draw mic circle
        micPaint.alpha = 255
        canvas.drawCircle(cx, cy, 72f, micPaint)

        // Draw mic icon (simple rect)
        val micPaintWhite = Paint(micPaint).apply { color = Color.WHITE }
        val rrect = RectF(cx - 18f, cy - 38f, cx + 18f, cy + 10f)
        canvas.drawRoundRect(rrect, 14f, 14f, micPaintWhite)
        canvas.drawArc(
            RectF(cx - 30f, cy - 20f, cx + 30f, cy + 40f),
            0f, 180f, false,
            Paint(micPaintWhite).apply { style = Paint.Style.STROKE; strokeWidth = 6f }
        )
        canvas.drawLine(cx, cy + 40f, cx, cy + 55f, Paint(micPaintWhite).apply { strokeWidth = 6f })
        canvas.drawLine(cx - 20f, cy + 55f, cx + 20f, cy + 55f, Paint(micPaintWhite).apply { strokeWidth = 6f })

        if (!isListening) {
            canvas.drawText("Tap to speak", cx, cy + 100f, textPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator.cancel()
    }
}
