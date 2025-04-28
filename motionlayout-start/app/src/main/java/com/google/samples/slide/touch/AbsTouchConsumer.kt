package com.google.samples.slide.touch

import android.animation.Animator
import android.animation.ValueAnimator
import com.google.samples.slide.AnimationProcessor
import com.google.samples.slide.SlideBuilder
import com.google.samples.slide.SlideListener

abstract class AbsTouchConsumer(
    var mBuilder: SlideBuilder,
    var mNotifier: SlideListener
) : ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    val mAnimationProcessor = AnimationProcessor(mBuilder, this, this)

    var mStartPositionY: Float = 0f
    var mStartPositionX: Float = 0f

    @Volatile
    var mPrevPositionY: Float = 0f

    @Volatile
    var mPrevPositionX: Float = 0f

    var mViewStartPositionY: Float = 0f
    var mViewStartPositionX: Float = 0f

    val top: Int
        get() = mBuilder.mSliderView.top

    val bottom: Int
        get() = mBuilder.mSliderView.bottom

    override fun onAnimationUpdate(animation: ValueAnimator) {
    }

    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationEnd(animation: Animator) {
        mNotifier.notifySlideToEnd()
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

}
