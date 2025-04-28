package com.google.samples.slide

import android.animation.TimeInterpolator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.google.samples.slide.Slide.SlideDirection
import com.google.samples.slide.Slide.SlideTo

class SlideBuilder(sliderView: View) {
    private var mStateRestored = false

    val mSliderView: View = sliderView
    var mListeners: MutableList<Slide.Listener?> = ArrayList()
    var mAutoSlideDuration: Int = 300
    var mGesturesEnabled: Boolean = true
    var mHideKeyboard: Boolean = false
    var mInterpolator: TimeInterpolator? = DecelerateInterpolator()
    var mSlideTo: SlideTo = SlideTo.SELF
    var mSlideDirection: SlideDirection = SlideDirection.DOWN
    var mSpecifySlideTo: Int = 0

    var mAutoSlideToEndPercent: Float = 40f //拖动到多少就自动贴底

    /**
     *
     * Define slide direction, **this parameter affects the motion vector slider**
     */
    fun slideDirection(direction: SlideDirection): SlideBuilder {
        if (!mStateRestored) {
            mSlideDirection = direction
        }
        return this
    }

    /**
     *
     * Define a [Slide.Listener] for this SlideUp
     *
     * @param listeners [List] of listeners
     */
    fun listeners(listeners: List<Slide.Listener>): SlideBuilder {
        mListeners.addAll(listeners)
        return this
    }

    /**
     *
     * Define a [Slide.Listener] for this SlideUp
     *
     * @param listeners array of listeners
     */
    fun listeners(vararg listeners: Slide.Listener): SlideBuilder {
        return listeners(listeners.toList())
    }

    /**
     *
     * Define duration of animation (whenever you use [Slide.hide] or [Slide.show] methods)
     *
     * @param duration **(default - **300**)**
     */
    fun autoSlideDuration(duration: Int): SlideBuilder {
        if (!mStateRestored) {
            mAutoSlideDuration = duration
        }
        return this
    }

    /**
     *
     * Turning on/off sliding on touch event
     *
     * @param enabled **(default - **true**)**
     */
    fun gesturesEnabled(enabled: Boolean): SlideBuilder {
        mGesturesEnabled = enabled
        return this
    }

    /**
     *
     * Define behavior of soft input
     *
     * @param hide **(default - **false**)**
     */
    fun hideSoftInputWhenDisplayed(hide: Boolean): SlideBuilder {
        if (!mStateRestored) {
            mHideKeyboard = hide
        }
        return this
    }

    /**
     *
     * Define interpolator for animation (whenever you use [Slide.hide] or [Slide.show] methods)
     *
     * @param interpolator **(default - **Decelerate interpolator**)**
     */
    fun interpolator(interpolator: TimeInterpolator?): SlideBuilder {
        mInterpolator = interpolator
        return this
    }

    /**
     * @param savedState parameters will be restored from this bundle, if it contains them
     */
    fun savedState(savedState: Bundle?): SlideBuilder {
        restoreParams(savedState)
        return this
    }

    fun slideToSelf(): SlideBuilder {
        mSlideTo = SlideTo.SELF
        return this
    }

    fun slideToParent(): SlideBuilder {
        mSlideTo = SlideTo.PARENT
        return this
    }

    fun slideTo(length: Int): SlideBuilder {
        mSlideTo = SlideTo.SPECIFY
        mSpecifySlideTo = length
        return this
    }


    /**
     *
     * Build the SlideUp and add behavior to view
     */
    fun build(): Slide {
        return Slide(this)
    }

    /**
     *
     * Trying restore saved state
     */
    private fun restoreParams(savedState: Bundle?) {
        if (savedState == null) return
        mStateRestored = savedState.getBoolean(Slide.KEY_STATE_SAVED, false)
        if (savedState.getSerializable(Slide.KEY_START_DIRECTION) != null) {
            mSlideDirection = (savedState.getSerializable(Slide.KEY_START_DIRECTION) as? SlideDirection) ?: SlideDirection.UP
        }
        mAutoSlideDuration = savedState.getInt(Slide.KEY_AUTO_SLIDE_DURATION, mAutoSlideDuration)
        mHideKeyboard = savedState.getBoolean(Slide.KEY_HIDE_SOFT_INPUT, mHideKeyboard)
    }
}