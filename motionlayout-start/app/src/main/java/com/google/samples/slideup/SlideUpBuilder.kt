package com.google.samples.slideup

import android.animation.TimeInterpolator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.google.samples.slideup.SlideUp.SlideDirection
import com.google.samples.slideup.SlideUp.SlideTo

/**
 *
 * Default constructor for [SlideUp]
 */
class SlideUpBuilder(sliderView: View) {
    private var mStateRestored = false

    var mSliderView: View
    var mDensity: Float
    var mIsRTL: Boolean = false
    var mStartState: SlideUp.State? = SlideUp.State.SHOWED
    var mListeners: MutableList<SlideUp.Listener?> = ArrayList()
    var mDebug: Boolean = false
    var mAutoSlideDuration: Int = 300
    var mSlideDirection: SlideDirection? = SlideDirection.DOWN
    var mGesturesEnabled: Boolean = true
    var mHideKeyboard: Boolean = false
    var mInterpolator: TimeInterpolator? = DecelerateInterpolator()
    var mAlsoScrollView: View? = null
    var mSlideTo: SlideTo = SlideTo.SELF
    var mSpecifySlideTo: Int = 0


    /**
     *
     * Construct a SlideUp by passing the view or his child to use for the generation
     */
    init {
        mSliderView = sliderView
        mDensity = sliderView.resources.displayMetrics.density
        //mIsRTL = sliderView.getResources().getBoolean(R.bool.is_right_to_left);
    }

    /**
     *
     * Define a start state on screen
     *
     * @param startState **(default - **[SlideUp.State.HIDDEN]**)**
     */
    fun StartState(startState: SlideUp.State): SlideUpBuilder {
        if (!mStateRestored) {
            mStartState = startState
        }
        return this
    }

    /**
     *
     * Define slide direction, **this parameter affects the motion vector slider**
     */
    fun SlideDirection(direction: SlideDirection): SlideUpBuilder {
        if (!mStateRestored) {
            mSlideDirection = direction
        }
        return this
    }

    /**
     *
     * Define a [SlideUp.Listener] for this SlideUp
     *
     * @param listeners [List] of listeners
     */
    fun listeners(listeners: List<SlideUp.Listener>): SlideUpBuilder {
        mListeners.addAll(listeners)
        return this
    }

    /**
     *
     * Define a [SlideUp.Listener] for this SlideUp
     *
     * @param listeners array of listeners
     */
    fun listeners(vararg listeners: SlideUp.Listener): SlideUpBuilder {
        return listeners(listeners.toList())
    }

    /**
     *
     * Turning on/off debug logging for all handled events
     *
     * @param enabled **(default - **false**)**
     */
    fun loggingEnabled(enabled: Boolean): SlideUpBuilder {
        if (!mStateRestored) {
            mDebug = enabled
        }
        return this
    }

    /**
     *
     * Define duration of animation (whenever you use [SlideUp.hide] or [SlideUp.show] methods)
     *
     * @param duration **(default - **300**)**
     */
    fun autoSlideDuration(duration: Int): SlideUpBuilder {
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
    fun gesturesEnabled(enabled: Boolean): SlideUpBuilder {
        mGesturesEnabled = enabled
        return this
    }

    /**
     *
     * Define behavior of soft input
     *
     * @param hide **(default - **false**)**
     */
    fun hideSoftInputWhenDisplayed(hide: Boolean): SlideUpBuilder {
        if (!mStateRestored) {
            mHideKeyboard = hide
        }
        return this
    }

    /**
     *
     * Define interpolator for animation (whenever you use [SlideUp.hide] or [SlideUp.show] methods)
     *
     * @param interpolator **(default - **Decelerate interpolator**)**
     */
    fun interpolator(interpolator: TimeInterpolator?): SlideUpBuilder {
        mInterpolator = interpolator
        return this
    }

    /**
     * @param savedState parameters will be restored from this bundle, if it contains them
     */
    fun savedState(savedState: Bundle?): SlideUpBuilder {
        restoreParams(savedState)
        return this
    }


    /**
     *
     * Provide a [View] that will also trigger slide events on the [SlideUp].
     *
     * @param alsoScrollView the other view that will trigger the slide events
     */
    fun slideFromOtherView(alsoScrollView: View?): SlideUpBuilder {
        mAlsoScrollView = alsoScrollView
        return this
    }

    fun slideToSelf(): SlideUpBuilder {
        mSlideTo = SlideTo.SELF
        return this
    }

    fun slideToParent(): SlideUpBuilder {
        mSlideTo = SlideTo.PARENT
        return this
    }

    fun slideTo(length: Int): SlideUpBuilder {
        mSlideTo = SlideTo.SPECIFY
        mSpecifySlideTo = length
        return this
    }


    /**
     *
     * Build the SlideUp and add behavior to view
     */
    fun build(): SlideUp {
        return SlideUp(this)
    }

    /**
     *
     * Trying restore saved state
     */
    private fun restoreParams(savedState: Bundle?) {
        if (savedState == null) return
        mStateRestored = savedState.getBoolean(SlideUp.KEY_STATE_SAVED, false)
        if (savedState.getSerializable(SlideUp.KEY_STATE) != null) {
            mStartState = savedState.getSerializable(SlideUp.KEY_STATE) as SlideUp.State?
        }
        if (savedState.getSerializable(SlideUp.KEY_START_DIRECTION) != null) {
            mSlideDirection = savedState.getSerializable(SlideUp.KEY_START_DIRECTION) as SlideDirection?
        }
        mDebug = savedState.getBoolean(SlideUp.KEY_DEBUG, mDebug)
        mAutoSlideDuration = savedState.getInt(SlideUp.KEY_AUTO_SLIDE_DURATION, mAutoSlideDuration)
        mHideKeyboard = savedState.getBoolean(SlideUp.KEY_HIDE_SOFT_INPUT, mHideKeyboard)
    }
}