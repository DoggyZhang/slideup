package com.google.samples.slideup

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
internal object Internal {
    private val sRect = Rect()

    fun isUpEventInView(view: View, event: MotionEvent): Boolean {
        view.getHitRect(sRect)
        return sRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
}
