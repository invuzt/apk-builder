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
        
        // Paint setup
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = source.width / 35f // Sedikit diperbesar
            isAntiAlias = true
            setShadowLayer(8f, 2f, 2f, Color.BLACK)
            style = Paint.Style.FILL
        }
        
        val margin = 50f
        var currentY = source.height - margin
        
        // 1. Alamat (Paling Bawah)
        if (options.showAddress) {
            val addrText = if (address.isNotEmpty()) address else "Mencari lokasi..."
            val addrPaint = Paint(paint).apply { textSize = source.width / 45f }
            currentY = drawMultilineText(canvas, addrText, margin, currentY, addrPaint, (source.width - margin*2).toInt())
            currentY -= 30f
        }
        
        // 2. Koordinat (Tengah)
        if (options.showCoords) {
            val coordText = if (location != null) "${location.latitude}, ${location.longitude}" else "GPS Menunggu..."
            canvas.drawText(coordText, margin, currentY, paint)
            currentY -= paint.textSize * 1.5f
        }
        
        // 3. Waktu & Tanggal
        val timeStr = if (options.showTime) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) else ""
        val dateStr = if (options.showDate) SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) else ""
        val combined = listOf(dateStr, timeStr).filter { it.isNotEmpty() }.joinToString(" | ")
        
        if (combined.isNotEmpty()) {
            canvas.drawText(combined, margin, currentY, paint)
            currentY -= paint.textSize * 1.5f
        }
        
        // 4. Custom Text (Paling Atas)
        if (options.customText.isNotEmpty()) {
            val customPaint = Paint(paint).apply { 
                typeface = Typeface.DEFAULT_BOLD
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
            if (paint.measureText(testLine) < maxWidth) currentLine = testLine 
            else { lines.add(currentLine); currentLine = word }
        }
        lines.add(currentLine)
        
        var tempY = y
        // Render dari bawah ke atas agar koordinat selalu di atas alamat
        for (i in lines.indices.reversed()) {
            canvas.drawText(lines[i], x, tempY, paint)
            if (i != 0) tempY -= paint.textSize * 1.2f
        }
        return tempY - paint.textSize
    }
}
