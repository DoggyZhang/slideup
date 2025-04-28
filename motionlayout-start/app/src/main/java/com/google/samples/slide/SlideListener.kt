package com.google.samples.slide

import android.view.MotionEvent

interface SlideListener {
    fun performClick(event: MotionEvent)

    fun notifySlideToEnd()

    fun notifyPercentChanged(percent: Float, dir: Slide.SlideDirection)
}
