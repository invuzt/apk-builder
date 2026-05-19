package com.builder.utils

import android.graphics.*
import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

data class WatermarkOptions(
    val showTime: Boolean,
    val showDate: Boolean,
    val showCoords: Boolean,
    val showAddress: Boolean,
    val customText: String
)

object WatermarkManager {
    fun apply(source: Bitmap, location: Location?, address: String, options: WatermarkOptions): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = source.width / 35f
            isAntiAlias = true
            setShadowLayer(8f, 2f, 2f, Color.BLACK)
        }
        
        val margin = 50f
        var currentY = source.height - margin
        
        // Watermark Nickname (CakRu) di kanan bawah
        val nickPaint = Paint(paint).apply { 
            textSize = source.width / 45f
            alpha = 180
            textAlign = Paint.Align.RIGHT 
        }
        canvas.drawText("Shot by CakRu", source.width - margin, source.height - margin, nickPaint)
        
        // Logika Watermark Data (Kiri Bawah)
        if (options.showAddress && address.isNotEmpty()) {
            currentY = drawMultilineText(canvas, address, margin, currentY, paint, (source.width * 0.7f).toInt())
            currentY -= 20f
        }
        
        if (options.showCoords) {
            val coordText = if (location != null) "${location.latitude}, ${location.longitude}" else "Searching GPS..."
            canvas.drawText(coordText, margin, currentY, paint)
            currentY -= paint.textSize * 1.4f
        }
        
        val timeStr = if (options.showTime) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) else ""
        val dateStr = if (options.showDate) SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) else ""
        val combined = listOf(dateStr, timeStr).filter { it.isNotEmpty() }.joinToString(" | ")
        
        if (combined.isNotEmpty()) {
            canvas.drawText(combined, margin, currentY, paint)
            currentY -= paint.textSize * 1.4f
        }
        
        if (options.customText.isNotEmpty()) {
            val cp = Paint(paint).apply { color = Color.YELLOW; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText(options.customText.uppercase(), margin, currentY, cp)
        }
        
        return result
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Int): Float {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) currentLine = testLine else { lines.add(currentLine); currentLine = word }
        }
        lines.add(currentLine)
        var tempY = y
        for (i in lines.indices.reversed()) {
            canvas.drawText(lines[i], x, tempY, paint)
            if (i != 0) tempY -= paint.textSize * 1.1f
        }
        return tempY - paint.textSize
    }
}
