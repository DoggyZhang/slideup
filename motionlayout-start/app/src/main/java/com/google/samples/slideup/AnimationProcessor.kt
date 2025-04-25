package com.google.samples.slideup

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.util.Log

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
internal class AnimationProcessor(private val mBuilder: SlideUpBuilder, updateListener: AnimatorUpdateListener, listener: Animator.AnimatorListener) {
    private var mValueAnimator: ValueAnimator? = null
    var slideAnimationTo: Float = 0f
        private set

    init {
        createAnimation(updateListener, listener)
    }

    fun endAnimation() {
        mValueAnimator?.let {
            if (it.values != null && it.isRunning) {
                it.end()
            }
        } ?: let {
            Log.w("zhangfei", "endAnimation fail, for mValueAnimator is null")
        }
    }

    fun paramsChanged() {
        mValueAnimator?.let {
            it.setDuration(mBuilder.mAutoSlideDuration.toLong())
            it.interpolator = mBuilder.mInterpolator
        } ?: let {
            Log.w("zhangfei", "paramsChanged fail, for mValueAnimator is null")
        }
    }

    val isAnimationRunning: Boolean
        get() = mValueAnimator != null && mValueAnimator?.isRunning == true

    fun setValuesAndStart(from: Float, to: Float) {
        Log.d("zhangfei", "setValuesAndStart, from:$from, to:$to")
        slideAnimationTo = to
        mValueAnimator?.let {
            it.setFloatValues(from, to)
            it.start()
        } ?: let {
            Log.w("zhangfei", "setValuesAndStart fail, for mValueAnimator is null")
        }
    }

    private fun createAnimation(updateListener: AnimatorUpdateListener, listener: Animator.AnimatorListener) {
        mValueAnimator = ValueAnimator.ofFloat().apply {
            setDuration(mBuilder.mAutoSlideDuration.toLong())
            interpolator = mBuilder.mInterpolator
            addUpdateListener(updateListener)
            addListener(listener)
        }
    }
}
