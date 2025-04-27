package com.google.samples.slide.touch

import android.animation.ValueAnimator
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.google.samples.slide.Slide.SlideDirection
import com.google.samples.slide.SlideBuilder
import com.google.samples.slide.SlideListener
import com.google.samples.slide.TAG_SLIDE_TOUCH_CONSUMER
import kotlin.math.abs

class SlideDownTouchConsumer(
    builder: SlideBuilder,
    notifier: SlideListener,
    private val slideLength: Float
) : AbsTouchConsumer(builder, notifier) {

    private val touchSlop = ViewConfiguration.get(mBuilder.mSliderView.context).scaledPagingTouchSlop
    private var mGoingUp = false
    private var mGoingDown = false

    private val sRect = Rect()
    private fun isUpEventInView(view: View, event: MotionEvent): Boolean {
        view.getHitRect(sRect)
        return sRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    fun slideDown(touchedView: View, event: MotionEvent): Boolean {
        Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideDown, touchedView: " + touchedView + ", event:" + event.actionMasked)
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mStartPositionX = event.rawX
                    mStartPositionY = event.rawY
                    mViewStartPositionY = mBuilder.mSliderView.translationY
                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideDown(ACTION_DOWN) -> mViewStartPositionY:$mViewStartPositionY")
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val difference = event.rawY - mStartPositionY
                    val moveTo = mViewStartPositionY + difference
                    val percents = moveTo * 100 / slideLength
                    calculateDirection(event)

                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideDown(ACTION_MOVE) -> difference: $difference")
                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "                          moveTo:$moveTo, slideLength:$slideLength, percent:$percents")
                    if (moveTo > 0) {
                        mNotifier.notifyPercentChanged(percents, SlideDirection.DOWN)
                        mBuilder.mSliderView.translationY = moveTo
                        return true
                    } else {
                        return false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(mStartPositionX - event.rawX) < touchSlop && abs(mStartPositionY - event.rawY) < touchSlop) {
                        return false
                    }
                    val slideAnimationFrom = mBuilder.mSliderView.translationY
                    if (slideAnimationFrom == mViewStartPositionY) {
                        return !isUpEventInView(mBuilder.mSliderView, event)
                    }
                    val scrollableAreaConsumed = mBuilder.mSliderView.translationY > slideLength

                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideDown(ACTION_UP) -> slideAnimationFrom: $slideAnimationFrom, mViewStartPositionY:$mViewStartPositionY, mBuilder.mSliderView.translationY:${mBuilder.mSliderView.translationY}")
                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "                        mGoingDown: $mGoingDown, scrollableAreaConsumed:$scrollableAreaConsumed, slideLength:$slideLength")

                    if (scrollableAreaConsumed && mGoingDown) {
                        mAnimationProcessor.setValuesAndStart(slideAnimationFrom, slideLength)
                    } else {
                        mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0f)
                    }
                    return true
                }
            }
        } finally {
            mPrevPositionY = event.rawY
            mPrevPositionX = event.rawX
        }
        return false
    }

    private fun calculateDirection(event: MotionEvent) {
        if (abs(event.rawY - mPrevPositionY) < touchSlop) {
            return
        }
        mGoingUp = mPrevPositionY - event.rawY > 0
        mGoingDown = mPrevPositionY - event.rawY < 0
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Float
        mBuilder.mSliderView.translationY = value
    }
}
