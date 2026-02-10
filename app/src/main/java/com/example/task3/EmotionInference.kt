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

        // Eye Distance for normalization
        val eyeDist = calculateDistance(landmarks[33], landmarks[263])

        // MAR (Mouth Aspect Ratio)
        // Indices: upper lip: 13, lower lip: 14, left corner: 61, right corner: 291
        val mar = calculateDistance(landmarks[13], landmarks[14]) / calculateDistance(landmarks[61], landmarks[291])

        // Normalized Eyebrow Distance (Anger/Focus)
        // Left eyebrow inner: 55, Right eyebrow inner: 285
        val normEyebrowDist = calculateDistance(landmarks[55], landmarks[285]) / eyeDist

        // Mouth Corner Height (Smile check)
        // Left corner: 61, Right corner: 291, Upper lip: 13, Lower lip: 14
        val mouthWidth = calculateDistance(landmarks[61], landmarks[291])
        val mouthCenterY = (landmarks[13].y() + landmarks[14].y()) / 2
        val cornersY = (landmarks[61].y() + landmarks[291].y()) / 2
        // Landmarks y increases downwards, so higher corners means smaller cornersY
        val smileIncline = (mouthCenterY - cornersY) / mouthWidth

        // Head Pose (Simplified)
        // Nose tip: 1, Left eye: 33, Right eye: 263
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
        } else if (smileIncline > 0.08f || (mar > 0.15f && smileIncline > 0.05f)) {
            emotion = "Happy"
            confidence = min(smileIncline * 5f + 0.5f, 1f)
        } else if (normEyebrowDist < 0.22f) {
            emotion = "Angry"
            confidence = min(1f, (0.22f - normEyebrowDist) * 10f + 0.8f)
        }

        return EmotionResult(emotion, confidence, pitch, yaw, roll)
    }

    private fun calculateDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        return sqrt((p1.x() - p2.x()).pow(2) + (p1.y() - p2.y()).pow(2))
    }
}
