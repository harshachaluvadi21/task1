package com.example.task3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var results: FaceLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    var showLandmarks: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var currentEmotion: String = "Neutral"
        set(value) {
            field = value
            invalidate()
        }

    init {
        linePaint.color = Color.GREEN
        linePaint.strokeWidth = 2f
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 4f
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 60f
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }

    fun setResults(
        faceLandmarkerResults: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int
    ) {
        results = faceLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = Math.max(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw Emotion Text
        canvas.drawText("Emotion: $currentEmotion", 50f, 100f, textPaint)

        val result = results
        if (result != null && showLandmarks) {
            for (landmarks in result.faceLandmarks()) {
                for (landmark in landmarks) {
                    canvas.drawPoint(
                        landmark.x() * width,
                        landmark.y() * height,
                        pointPaint
                    )
                }
            }
        }
    }

    fun clear() {
        results = null
        invalidate()
    }
}
