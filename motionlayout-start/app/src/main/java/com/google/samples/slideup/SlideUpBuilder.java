package com.google.samples.slideup;

import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Default constructor for {@link SlideUp}</p>
 */
public final class SlideUpBuilder {
    private boolean mStateRestored = false;

    View mSliderView;
    float mDensity;
    float mTouchableArea;
    boolean mIsRTL;
    SlideUp.State mStartState = SlideUp.State.SHOWED;
    List<SlideUp.Listener> mListeners = new ArrayList<>();
    boolean mDebug = false;
    int mAutoSlideDuration = 300;
    SlideUp.SlideDirection mSlideDirection = SlideUp.SlideDirection.DOWN;
    boolean mGesturesEnabled = true;
    boolean mHideKeyboard = false;
    TimeInterpolator mInterpolator = new DecelerateInterpolator();
    View mAlsoScrollView;
    SlideUp.SlideTo mSlideTo = SlideUp.SlideTo.SELF;
    int mSpecifySlideTo = 0;


    /**
     * <p>Construct a SlideUp by passing the view or his child to use for the generation</p>
     */
    public SlideUpBuilder(View sliderView) {
        Internal.checkNonNull(sliderView, "View can't be null");
        mSliderView = sliderView;
        mDensity = sliderView.getResources().getDisplayMetrics().density;
        //mIsRTL = sliderView.getResources().getBoolean(R.bool.is_right_to_left);
    }

    /**
     * <p>Define a start state on screen</p>
     *
     * @param startState <b>(default - <b color="#EF6C00">{@link SlideUp.State#HIDDEN}</b>)</b>
     */
    public SlideUpBuilder StartState(@NonNull SlideUp.State startState) {
        if (!mStateRestored) {
            mStartState = startState;
        }
        return this;
    }

    /**
     * <p>Define slide direction, <b>this parameter affects the motion vector slider</b></p>
     */
    public SlideUpBuilder SlideDirection(@NonNull SlideUp.SlideDirection direction) {
        if (!mStateRestored) {
            mSlideDirection = direction;
        }
        return this;
    }

    /**
     * <p>Define a {@link SlideUp.Listener} for this SlideUp</p>
     *
     * @param listeners {@link List} of listeners
     */
    public SlideUpBuilder listeners(@NonNull List<SlideUp.Listener> listeners) {
        if (listeners != null) {
            mListeners.addAll(listeners);
        }
        return this;
    }

    /**
     * <p>Define a {@link SlideUp.Listener} for this SlideUp</p>
     *
     * @param listeners array of listeners
     */
    public SlideUpBuilder listeners(@NonNull SlideUp.Listener... listeners) {
        List<SlideUp.Listener> listeners_list = new ArrayList<>();
        Collections.addAll(listeners_list, listeners);
        return listeners(listeners_list);
    }

    /**
     * <p>Turning on/off debug logging for all handled events</p>
     *
     * @param enabled <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public SlideUpBuilder loggingEnabled(boolean enabled) {
        if (!mStateRestored) {
            mDebug = enabled;
        }
        return this;
    }

    /**
     * <p>Define duration of animation (whenever you use {@link SlideUp#hide()} or {@link SlideUp#show()} methods)</p>
     *
     * @param duration <b>(default - <b color="#EF6C00">300</b>)</b>
     */
    public SlideUpBuilder autoSlideDuration(int duration) {
        if (!mStateRestored) {
            mAutoSlideDuration = duration;
        }
        return this;
    }

    /**
     * <p>Define touchable area <b>(in px)</b> for interaction</p>
     *
     * @param area <b>(default - <b color="#EF6C00">300dp</b>)</b>
     */
    public SlideUpBuilder touchableAreaPx(float area) {
        if (!mStateRestored) {
            mTouchableArea = area;
        }
        return this;
    }

    /**
     * <p>Define touchable area <b>(in dp)</b> for interaction</p>
     *
     * @param area <b>(default - <b color="#EF6C00">300dp</b>)</b>
     */
    public SlideUpBuilder touchableAreaDp(float area) {
        if (!mStateRestored) {
            mTouchableArea = area * mDensity;
        }
        return this;
    }

    /**
     * <p>Turning on/off sliding on touch event</p>
     *
     * @param enabled <b>(default - <b color="#EF6C00">true</b>)</b>
     */
    public SlideUpBuilder gesturesEnabled(boolean enabled) {
        mGesturesEnabled = enabled;
        return this;
    }

    /**
     * <p>Define behavior of soft input</p>
     *
     * @param hide <b>(default - <b color="#EF6C00">false</b>)</b>
     */
    public SlideUpBuilder hideSoftInputWhenDisplayed(boolean hide) {
        if (!mStateRestored) {
            mHideKeyboard = hide;
        }
        return this;
    }

    /**
     * <p>Define interpolator for animation (whenever you use {@link SlideUp#hide()} or {@link SlideUp#show()} methods)</p>
     *
     * @param interpolator <b>(default - <b color="#EF6C00">Decelerate interpolator</b>)</b>
     */
    public SlideUpBuilder interpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
        return this;
    }

    /**
     * @param savedState parameters will be restored from this bundle, if it contains them
     */
    public SlideUpBuilder savedState(@Nullable Bundle savedState) {
        restoreParams(savedState);
        return this;
    }


    /**
     * <p>Provide a {@link View} that will also trigger slide events on the {@link SlideUp}.</p>
     *
     * @param alsoScrollView the other view that will trigger the slide events
     */
    public SlideUpBuilder slideFromOtherView(@Nullable View alsoScrollView) {
        mAlsoScrollView = alsoScrollView;
        return this;
    }

    public SlideUpBuilder slideToSelf() {
        mSlideTo = SlideUp.SlideTo.SELF;
        return this;
    }

    public SlideUpBuilder slideToParent() {
        mSlideTo = SlideUp.SlideTo.PARENT;
        return this;
    }

    public SlideUpBuilder slideTo(int length) {
        mSlideTo = SlideUp.SlideTo.SPECIFY;
        mSpecifySlideTo = length;
        return this;
    }


    public float getSlideLength() {
        switch (mSlideTo) {
            case SELF:
                return mSliderView.getHeight();
            case PARENT:
                switch (mSlideDirection) {
                    case UP: {
                        ViewParent parent = mSliderView.getParent();
                        if (parent != null) {
                            return mSliderView.getTop();
                        }
                        return 0;
                    }
                    case DOWN: {
                        ViewParent parent = mSliderView.getParent();
                        if (parent != null) {
                            return ((ViewGroup) parent).getHeight() - mSliderView.getBottom();
                        }
                        return 0;
                    }
                }
                break;
            case SPECIFY:
                return mSpecifySlideTo;
        }
        return 0;
    }

    /**
     * <p>Build the SlideUp and add behavior to view</p>
     */
    public SlideUp build() {
        return new SlideUp(this);
    }

    /**
     * <p>Trying restore saved state</p>
     */
    private void restoreParams(@Nullable Bundle savedState) {
        if (savedState == null) return;
        mStateRestored = savedState.getBoolean(SlideUp.KEY_STATE_SAVED, false);
        if (savedState.getSerializable(SlideUp.KEY_STATE) != null) {
            mStartState = (SlideUp.State) savedState.getSerializable(SlideUp.KEY_STATE);
        }
        if (savedState.getSerializable(SlideUp.KEY_START_DIRECTION) != null) {
            mSlideDirection = (SlideUp.SlideDirection) savedState.getSerializable(SlideUp.KEY_START_DIRECTION);
        }
        mDebug = savedState.getBoolean(SlideUp.KEY_DEBUG, mDebug);
        mTouchableArea = savedState.getFloat(SlideUp.KEY_TOUCHABLE_AREA, mTouchableArea) * mDensity;
        mAutoSlideDuration = savedState.getInt(SlideUp.KEY_AUTO_SLIDE_DURATION, mAutoSlideDuration);
        mHideKeyboard = savedState.getBoolean(SlideUp.KEY_HIDE_SOFT_INPUT, mHideKeyboard);
    }
}