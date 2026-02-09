package com.example.task3

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.*

data class EmotionResult(
    val emotion: String,
    val confidence: Float,
    val pitch: Float,
    val yaw: Float,
    val roll: Float
)

object EmotionInference {

    fun infer(landmarks: List<NormalizedLandmark>): EmotionResult {
        if (landmarks.isEmpty()) return EmotionResult("None", 0f, 0f, 0f, 0f)

        // EAR (Eye Aspect Ratio) - simplified for one eye (left)
        // Indices: left eye upper: 159, lower: 145, inner: 133, outer: 33
        val ear = calculateDistance(landmarks[159], landmarks[145]) / calculateDistance(landmarks[133], landmarks[33])

        // MAR (Mouth Aspect Ratio)
        // Indices: upper lip: 13, lower lip: 14, left corner: 61, right corner: 291
        val mar = calculateDistance(landmarks[13], landmarks[14]) / calculateDistance(landmarks[61], landmarks[291])

        // Eyebrow Distance
        // Left eyebrow inner: 55, Right eyebrow inner: 285
        val eyebrowDist = calculateDistance(landmarks[55], landmarks[285])

        // Head Pose (Simplified)
        // Nose tip: 1, Left eye: 33, Right eye: 263, Left mouth: 61, Right mouth: 291
        val nose = landmarks[1]
        val leftEye = landmarks[33]
        val rightEye = landmarks[263]
        
        val yaw = (nose.x() - (leftEye.x() + rightEye.x()) / 2) * 100
        val pitch = (nose.y() - (leftEye.y() + rightEye.y()) / 2) * -100
        val roll = Math.toDegrees(atan2((rightEye.y() - leftEye.y()).toDouble(), (rightEye.x() - leftEye.x()).toDouble())).toFloat()

        var emotion = "Neutral"
        var confidence = 0.8f

        if (mar > 0.5) {
            emotion = "Surprised"
            confidence = min(mar * 1.5f, 1f)
        } else if (mar > 0.1 && mar < 0.4 && ear < 0.25) {
            emotion = "Happy"
            confidence = 0.9f
        } else if (eyebrowDist < 0.05) {
            emotion = "Angry"
            confidence = 0.85f
        }

        return EmotionResult(emotion, confidence, pitch, yaw, roll)
    }

    private fun calculateDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        return sqrt((p1.x() - p2.x()).pow(2) + (p1.y() - p2.y()).pow(2))
    }
}
