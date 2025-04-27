package com.google.samples.slideup

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.google.samples.slideup.SlideUp.SlideDirection
import kotlin.math.abs

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
internal class VerticalTouchConsumer(
    builder: SlideUpBuilder,
    notifier: LoggerNotifier,
    animationProcessor: AnimationProcessor,
    private val slideLength: Float
) : TouchConsumer(builder, notifier, animationProcessor) {

    private val touchSlop = ViewConfiguration.get(mBuilder.mSliderView.context).scaledPagingTouchSlop
    private var mGoingUp = false
    private var mGoingDown = false

    fun slideDown(touchedView: View, event: MotionEvent): Boolean {
        Log.d("zhangfei", "consumeBottomToTop, touchedView: " + touchedView + ", event:" + event.actionMasked)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mStartPositionY = event.rawY
                mViewStartPositionY = mBuilder.mSliderView.translationY
                //                mCanSlide = touchFromAlsoSlide(touchedView, event);
//                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
                Log.d("zhangfei", "slideDown(ACTION_DOWN) -> mCanSlide:$mCanSlide")
            }

            MotionEvent.ACTION_MOVE -> {

                val difference = event.rawY - mStartPositionY
                val moveTo = mViewStartPositionY + difference
                val percents = moveTo * 100 / slideLength

                calculateDirection(event)

                Log.d("zhangfei", "slideDown(ACTION_MOVE) -> difference: $difference, mCanSlide:$mCanSlide")
                Log.d("zhangfei", "                          moveTo:$moveTo, slideLength:$slideLength, percent:$percents")
                if (mBuilder.mSlideDirection == SlideDirection.DOWN && moveTo > 0 && mCanSlide) {
                    mNotifier.notifyPercentChanged(percents)
                    mBuilder.mSliderView.translationY = moveTo
                }
            }

            MotionEvent.ACTION_UP -> {
                val slideAnimationFrom = mBuilder.mSliderView.translationY
                if (slideAnimationFrom == mViewStartPositionY) {
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event)
                }
                val scrollableAreaConsumed = mBuilder.mSliderView.translationY > slideLength

                Log.d("zhangfei", "slideDown(ACTION_UP) -> slideAnimationFrom: $slideAnimationFrom, mViewStartPositionY:$mViewStartPositionY, mBuilder.mSliderView.translationY:${mBuilder.mSliderView.translationY}")
                Log.d("zhangfei", "                        mGoingDown: $mGoingDown, scrollableAreaConsumed:$scrollableAreaConsumed, slideLength:$slideLength")

                if (scrollableAreaConsumed && mGoingDown) {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, slideLength)
                } else {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0f)
                }
                mCanSlide = true
                mGoingUp = false
                mGoingDown = false
            }
        }
        mPrevPositionY = event.rawY
        mPrevPositionX = event.rawX
        return true
    }

    fun slideUp(touchedView: View?, event: MotionEvent): Boolean {
        val touchedArea = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mStartPositionY = event.rawY
                mViewStartPositionY = mBuilder.mSliderView.translationY
                //                mCanSlide = touchFromAlsoSlide(touchedView, event);
//                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                Log.d("zhangfei", "slideUp(ACTION_DOWN) -> mCanSlide:$mCanSlide")
            }

            MotionEvent.ACTION_MOVE -> {
                val difference = event.rawY - mStartPositionY
                val moveTo = mViewStartPositionY + difference
                val slideLength = slideLength
                val percents = (abs(moveTo.toDouble()) * 100 / slideLength).toFloat()
                calculateDirection(event)

                Log.d("zhangfei", "slideUp(ACTION_MOVE) -> difference: $difference, mCanSlide:$mCanSlide")
                Log.d("zhangfei", "                        moveTo:$moveTo, slideLength:$slideLength, percent:$percents")
                if (mBuilder.mSlideDirection == SlideDirection.UP && moveTo < 0 && mCanSlide) {
                    mNotifier.notifyPercentChanged(percents)
                    mBuilder.mSliderView.translationY = moveTo
                }
            }

            MotionEvent.ACTION_UP -> {
                val slideAnimationFrom = mBuilder.mSliderView.translationY
                if (slideAnimationFrom == mViewStartPositionY) {
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event)
                }
                val scrollableAreaConsumed = mBuilder.mSliderView.translationY < -slideLength

                Log.d("zhangfei", "slideUp(ACTION_UP) -> slideAnimationFrom: $slideAnimationFrom, mViewStartPositionY:$mViewStartPositionY, mBuilder.mSliderView.translationY:${mBuilder.mSliderView.translationY}")
                Log.d("zhangfei", "                      mGoingUp: $mGoingUp, scrollableAreaConsumed:$scrollableAreaConsumed, slideLength:$slideLength")

                if (scrollableAreaConsumed && mGoingUp) {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, -slideLength)
                } else {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0f)
                }
                mCanSlide = true
            }
        }
        mPrevPositionY = event.rawY
        mPrevPositionX = event.rawX
        return true
    }

    private fun calculateDirection(event: MotionEvent) {
        if (abs(event.rawY - mPrevPositionY) < touchSlop) {
            return
        }
        mGoingUp = mPrevPositionY - event.rawY > 0
        mGoingDown = mPrevPositionY - event.rawY < 0
    }
}
