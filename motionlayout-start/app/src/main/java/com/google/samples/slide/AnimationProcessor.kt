package com.google.samples.slide

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.util.Log

class AnimationProcessor(
    private val mBuilder: SlideBuilder,
    updateListener: AnimatorUpdateListener
) {
    private var mValueAnimator: ValueAnimator? = null
    var slideAnimationTo: Float = 0f
        private set

    init {
        createAnimation(updateListener)
    }

    fun endAnimation() {
        mValueAnimator?.let {
            if (it.values != null && it.isRunning) {
                it.end()
            }
        } ?: let {
            Log.w(TAG_SLIDE_ANIMATION, "endAnimation fail, for mValueAnimator is null")
        }
    }

    fun paramsChanged() {
        mValueAnimator?.let {
            it.setDuration(mBuilder.mAutoSlideDuration.toLong())
            it.interpolator = mBuilder.mInterpolator
        } ?: let {
            Log.w(TAG_SLIDE_ANIMATION, "paramsChanged fail, for mValueAnimator is null")
        }
    }

    val isAnimationRunning: Boolean
        get() = mValueAnimator != null && mValueAnimator?.isRunning == true

    fun setValuesAndStart(from: Float, to: Float) {
        Log.d(TAG_SLIDE_ANIMATION, "setValuesAndStart, from:$from, to:$to")
        slideAnimationTo = to
        mValueAnimator?.let {
            it.setFloatValues(from, to)
            it.start()
        } ?: let {
            Log.w(TAG_SLIDE_ANIMATION, "setValuesAndStart fail, for mValueAnimator is null")
        }
    }

    private fun createAnimation(updateListener: AnimatorUpdateListener) {
        mValueAnimator = ValueAnimator.ofFloat().apply {
            setDuration(mBuilder.mAutoSlideDuration.toLong())
            interpolator = mBuilder.mInterpolator
            addUpdateListener(updateListener)
            //addListener(listener)
        }
    }
}
