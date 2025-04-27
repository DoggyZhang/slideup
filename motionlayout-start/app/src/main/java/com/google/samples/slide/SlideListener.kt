package com.google.samples.slide

interface SlideListener {
    fun notifyPercentChanged(percent: Float, dir: Slide.SlideDirection)

    fun notifyVisibilityChanged(visibility: Int)
}
