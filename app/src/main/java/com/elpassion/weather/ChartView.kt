package com.elpassion.weather

import android.content.Context
import android.graphics.Canvas
import android.support.annotation.CallSuper
import android.util.AttributeSet
import android.view.View
import com.elpassion.weather.utils.copyAndReformat
import com.elpassion.weather.utils.deepCopy
import com.elpassion.weather.utils.drawChart
import com.elpassion.weather.utils.moveABitTo
import com.elpassion.weather.utils.pointAtTheEnd
import com.elpassion.weather.utils.resetPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private var drawer: Canvas.() -> Unit = {}
    private var continuation: Continuation<Unit>? = null

    fun doOnDraw(drawer: Canvas.() -> Unit) { this.drawer = drawer }

    suspend fun redraw() {
        require(continuation === null) { "Redrawing is already in progress." }
        invalidate()
        suspendCoroutine<Unit> { continuation = it }
    }

    suspend fun draw(drawer: Canvas.() -> Unit) {
        doOnDraw(drawer)
        redraw()
    }

    @CallSuper
    override fun onDraw(canvas: Canvas) {
        drawer.invoke(canvas)
        continuation?.run { post { resume(Unit) } }
        continuation = null
    }

    var chart: Chart = Chart(0f..100f, 0f..100f, emptyList())
        set(value) {
            field = value
            actor.offer(value)
        }

    private val actor = GlobalScope.actor<Chart>(Dispatchers.Main, Channel.CONFLATED) {

        var currentChart = chart.deepCopy()
        var currentVelocities = chart.deepCopy().resetPoints()

        doOnDraw { drawChart(currentChart) }

        for (destinationChart in this) {

            currentChart = currentChart.copyAndReformat(destinationChart, destinationChart.pointAtTheEnd)
            currentVelocities = currentVelocities.copyAndReformat(destinationChart, Point(0f, 0f))

            while (isActive && isEmpty) {
                currentChart.moveABitTo(destinationChart, currentVelocities)
                redraw()
                delay(16)
            }
        }
    }
}
