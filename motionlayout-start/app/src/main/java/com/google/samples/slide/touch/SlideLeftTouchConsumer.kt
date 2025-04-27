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

class SlideLeftTouchConsumer(
    builder: SlideBuilder,
    notifier: SlideListener,
    private val slideLength: Float
) : AbsTouchConsumer(builder, notifier) {

    private val touchSlop = ViewConfiguration.get(mBuilder.mSliderView.context).scaledPagingTouchSlop
    private var mGoingLeft = false
    private var mGoingRight = false

    private val sRect = Rect()
    private fun isUpEventInView(view: View, event: MotionEvent): Boolean {
        view.getHitRect(sRect)
        return sRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    fun slideLeft(touchedView: View?, event: MotionEvent): Boolean {
        Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideLeft, touchedView: " + touchedView + ", event:" + event.actionMasked)
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mStartPositionX = event.rawX
                    mStartPositionY = event.rawY
                    mViewStartPositionX = mBuilder.mSliderView.translationX
                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideLeft(ACTION_DOWN) -> mViewStartPositionX:$mViewStartPositionX")
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val difference = event.rawX - mStartPositionX
                    val moveTo = mViewStartPositionX + difference
                    val slideLength = slideLength
                    val percents = (abs(moveTo.toDouble()) * 100 / slideLength).toFloat()
                    calculateDirection(event)

                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideLeft(ACTION_MOVE) -> difference: $difference, moveTo:$moveTo, slideLength:$slideLength, percent:$percents")
                    if (moveTo < 0) {
                        Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideLeft(ACTION_MOVE) ===> translationX:$moveTo")
                        mNotifier.notifyPercentChanged(percents, SlideDirection.LEFT)
                        mBuilder.mSliderView.translationX = moveTo
                        return true
                    } else {
                        return false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(mStartPositionX - event.rawX) < touchSlop && abs(mStartPositionY - event.rawY) < touchSlop) {
                        return false
                    }
                    val slideAnimationFrom = mBuilder.mSliderView.translationX
                    if (slideAnimationFrom == mViewStartPositionY) {
                        return !isUpEventInView(mBuilder.mSliderView, event)
                    }
                    val scrollableAreaConsumed = mBuilder.mSliderView.translationX < -slideLength

                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "slideLeft(ACTION_UP) -> slideAnimationFrom: $slideAnimationFrom, mViewStartPositionY:$mViewStartPositionY, mBuilder.mSliderView.translationX:${mBuilder.mSliderView.translationX}")
                    Log.d(TAG_SLIDE_TOUCH_CONSUMER, "                      mGoingUp: $mGoingLeft, scrollableAreaConsumed:$scrollableAreaConsumed, slideLength:$slideLength")

                    if (scrollableAreaConsumed && mGoingLeft) {
                        mAnimationProcessor.setValuesAndStart(slideAnimationFrom, -slideLength)
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
        if (abs(event.rawX - mPrevPositionX) < touchSlop) {
            return
        }
        mGoingLeft = mPrevPositionX - event.rawX > 0
        mGoingRight = mPrevPositionX - event.rawX < 0
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Float
        mBuilder.mSliderView.translationX = value
        Log.d(TAG_SLIDE_TOUCH_CONSUMER, "SlideLeft(onAnimationUpdate) ===> translationX = $value")
    }
}
