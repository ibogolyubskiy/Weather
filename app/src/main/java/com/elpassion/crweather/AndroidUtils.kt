package com.elpassion.crweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.view.Menu
import android.view.ViewGroup
import android.widget.Toast

private val CHART_PAINT = Paint().apply {
    style = Paint.Style.STROKE
    strokeWidth = 3f
    isAntiAlias = true
}

private val AXES_PAINT = Paint().apply {
    style = Paint.Style.STROKE
    strokeWidth = 1f
    isAntiAlias = true
    textSize = 16f
    textAlign = Paint.Align.CENTER
    color = Color.GRAY
}

private val TEXT_LINE_HEIGHT = 20f

operator fun Menu.iterator() = object : Iterator<android.view.MenuItem> {
    private var current = 0
    override fun hasNext() = current < size()
    override fun next() = getItem(current++)
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): android.view.View {
    val inflater = android.view.LayoutInflater.from(context)
    return inflater.inflate(layoutId, this, attachToRoot)
}

fun Canvas.drawCircle(point: Point, radius: Float, paint: Paint)
        = drawCircle(point.x, point.y, radius, paint)

fun Canvas.drawLine(points: Pair<Point, Point>, paint: Paint)
        = drawLine(points.first.x, points.first.y, points.second.x, points.second.y, paint)

fun Canvas.drawChart(chart: Chart) {
    drawChartAxes(chart)
    drawChartLegend(chart)
    for ((_, color, points) in chart.lines) {
        for ((begin, end) in points.changes())
            drawLine(begin.scale(chart.area, insetArea) to end.scale(chart.area, insetArea), CHART_PAINT.withColor(color))
        for (point in points)
            drawCircle(point.scale(chart.area, insetArea), 2f, CHART_PAINT.withColor(color))
    }
}

private fun Canvas.drawChartAxes(chart: Chart) {
    val axesArea = insetArea
    drawBoundaries(axesArea, left = true, bottom = true)
    for (portion in (0 .. 100 step 20).map { it / 100f }) {
        val value = chart.outputRange.portion(portion).asMeasurementString
        val time = chart.inputRange.portion(portion).asTimeMs.asDateString
        drawText(time, axesArea.horizontalRange.portion(portion), axesArea.bottom + TEXT_LINE_HEIGHT + 4f, AXES_PAINT)
        drawText(value, 26f, axesArea.verticalRange.portion(1f - portion), AXES_PAINT)
    }
}

fun Canvas.drawChartLegend(chart: Chart) {
    var textPosX = area.horizontalRange.portion(.9f)
    var textPosY = TEXT_LINE_HEIGHT
    drawText("Forecasts - Updated ${chart.timeMs.asTimeString}", textPosX, textPosY, AXES_PAINT)
    textPosY += TEXT_LINE_HEIGHT
    for ((name, color, _) in chart.lines) {
        drawText(name, textPosX, textPosY, AXES_PAINT.withColor(color))
        textPosY += TEXT_LINE_HEIGHT
    }

}

private val Canvas.insetArea get() = area.apply { inset(50f, 40f) }

private val RectF.verticalRange get() = top .. bottom

private val RectF.horizontalRange get() = left .. right

private fun Canvas.drawBoundaries(rect: RectF, paint: Paint = AXES_PAINT, left: Boolean = false, top: Boolean = false, right: Boolean = false, bottom: Boolean = false) {
    if (left) drawLine(rect.left, rect.top, rect.left, rect.bottom, paint)
    if (top) drawLine(rect.left, rect.top, rect.right, rect.top, paint)
    if (right) drawLine(rect.right, rect.top, rect.right, rect.bottom, paint)
    if (bottom) drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint)
}

private fun Paint.withColor(@ColorInt acolor: Int)
        = Paint().also { it.set(this); it.color = acolor }

fun Context.toast(message: CharSequence) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
