package com.builder.utils

import android.graphics.*
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
    fun apply(source: Bitmap, location: android.location.Location?, address: String, options: WatermarkOptions): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = source.width / 32f
            isAntiAlias = true
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
        }
        
        val margin = 60f
        var currentY = source.height - margin
        
        if (options.showAddress && address.isNotEmpty()) {
            val addrPaint = Paint(paint).apply { textSize = source.width / 40f }
            currentY = drawMultilineText(canvas, address, margin, currentY, addrPaint, (source.width - margin*2).toInt())
            currentY -= 20f
        }
        
        if (options.showCoords && location != null) {
            canvas.drawText("${location.latitude}, ${location.longitude}", margin, currentY, paint)
            currentY -= paint.textSize * 1.3f
        }
        
        val timeStr = if (options.showTime) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) else ""
        val dateStr = if (options.showDate) SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) else ""
        val combined = listOf(dateStr, timeStr).filter { it.isNotEmpty() }.joinToString(" | ")
        
        if (combined.isNotEmpty()) {
            canvas.drawText(combined, margin, currentY, paint)
            currentY -= paint.textSize * 1.3f
        }
        
        if (options.customText.isNotEmpty()) {
            val customPaint = Paint(paint).apply { 
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.YELLOW 
            }
            canvas.drawText(options.customText.uppercase(), margin, currentY, customPaint)
        }
        
        return result
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Int): Float {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) currentLine = testLine else {
                lines.add(currentLine); currentLine = word
            }
        }
        lines.add(currentLine)
        var tempY = y
        for (i in lines.indices.reversed()) {
            canvas.drawText(lines[i], x, tempY, paint)
            if (i != 0) tempY -= paint.textSize * 1.2f
        }
        return tempY - paint.textSize
    }
}
