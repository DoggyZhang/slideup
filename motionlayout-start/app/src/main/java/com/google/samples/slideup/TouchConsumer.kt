package com.google.samples.slideup

import android.view.MotionEvent
import android.view.View

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
internal open class TouchConsumer(
    var mBuilder: SlideUpBuilder,
    var mNotifier: LoggerNotifier,
    var mAnimationProcessor: AnimationProcessor
) {
    var mCanSlide: Boolean = true

    var mStartPositionY: Float = 0f
    var mStartPositionX: Float = 0f

    @Volatile
    var mPrevPositionY: Float = 0f

    @Volatile
    var mPrevPositionX: Float = 0f
    var mViewStartPositionY: Float = 0f
    var mViewStartPositionX: Float = 0f

    val end: Int
        get() = if (mBuilder.mIsRTL) {
            mBuilder.mSliderView.left
        } else {
            mBuilder.mSliderView.right
        }

    val start: Int
        get() = if (mBuilder.mIsRTL) {
            mBuilder.mSliderView.right
        } else {
            mBuilder.mSliderView.left
        }

    val top: Int
        get() = mBuilder.mSliderView.top

    val bottom: Int
        get() = mBuilder.mSliderView.bottom

    fun touchFromAlsoSlide(touchedView: View, event: MotionEvent?): Boolean {
        return touchedView === mBuilder.mAlsoScrollView
    }
}
