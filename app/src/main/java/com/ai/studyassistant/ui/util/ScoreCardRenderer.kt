package com.ai.studyassistant.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

private const val CARD_WIDTH = 1080
private const val CARD_HEIGHT = 1350

private const val COLOR_NAVY = 0xFF0B1D3D.toInt()
private const val COLOR_ORANGE = 0xFFFF8A3D.toInt()
private const val COLOR_TEAL = 0xFF1FC8D8.toInt()
private const val COLOR_WHITE = 0xFFFFFFFF.toInt()

/**
 * Draws the shareable score card with plain [android.graphics.Canvas] APIs rather than
 * capturing a Composable — the installed Compose BOM predates the graphicsLayer capture
 * API, and a manual draw has no such version dependency.
 */
object ScoreCardRenderer {

    fun render(examLabel: String, score: Int, total: Int, message: String, emoji: String): Bitmap {
        val bitmap = createBitmap(CARD_WIDTH, CARD_HEIGHT)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                intArrayOf(COLOR_NAVY, COLOR_ORANGE, COLOR_TEAL),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), backgroundPaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_WHITE }
        val badgeCenterX = CARD_WIDTH / 2f
        val badgeCenterY = 340f
        canvas.drawCircle(badgeCenterX, badgeCenterY, 130f, badgePaint)

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 130f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, badgeCenterX, badgeCenterY + 48f, emojiPaint)

        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            textSize = 150f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$score / $total", badgeCenterX, 660f, scorePaint)

        val messagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            alpha = 230
            textSize = 52f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(message, badgeCenterX, 740f, messagePaint)

        val examPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(examLabel, badgeCenterX, 830f, examPaint)

        val footerPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            alpha = 46
        }
        val footerRect = RectF(140f, CARD_HEIGHT - 260f, CARD_WIDTH - 140f, CARD_HEIGHT - 160f)
        canvas.drawRoundRect(footerRect, 50f, 50f, footerPillPaint)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ExamAI", badgeCenterX, footerRect.centerY() - 15f, footerPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE
            alpha = 220
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Free JEE, NEET & UPSC prep",
            badgeCenterX,
            footerRect.centerY() + 30f,
            taglinePaint
        )

        return bitmap
    }

    /** Writes the card to app cache and returns a content:// [android.net.Uri] shareable via FileProvider. */
    fun saveToShareableUri(context: Context, bitmap: Bitmap): android.net.Uri {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(shareDir, "score_card.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
