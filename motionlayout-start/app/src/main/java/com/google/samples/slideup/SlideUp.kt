package com.google.samples.slideup

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IntDef

class SlideUp internal constructor(private val mBuilder: SlideUpBuilder) : OnTouchListener, AnimatorUpdateListener, Animator.AnimatorListener, LoggerNotifier {
    /**
     *
     * Available start states
     */
    enum class State {
        /**
         * State hidden is equal [View.GONE]
         */
        HIDDEN,

        /**
         * State showed is equal [View.VISIBLE]
         */
        SHOWED
    }

    enum class SlideDirection {
        UP,
        DOWN,  //        LEFT,
        //        RIGHT
    }

    enum class SlideTo {
        SELF,  //滑动自身的高度
        PARENT,  //滑动到父布局
        SPECIFY //指定滑动距离
    }


    @IntDef(value = [Gravity.START, Gravity.END, Gravity.TOP, Gravity.BOTTOM])
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class StartVector

    private var mCurrentState: State? = null

    private var mViewHeight = 0f
    private var mViewWidth = 0f

    private var mVerticalTouchConsumer: VerticalTouchConsumer? = null

    //    private HorizontalTouchConsumer mHorizontalTouchConsumer;
    private var mAnimationProcessor: AnimationProcessor = AnimationProcessor(mBuilder, this, this)

    /**
     *
     * Interface to listen to all handled events taking place in the slider
     */
    interface Listener {
        interface Slide : Listener {
            /**
             * @param percent percents of complete slide **(100 = HIDDEN, 0 = SHOWED)**
             */
            fun onSlide(percent: Float)
        }

        interface Visibility : Listener {
            /**
             * @param visibility (**GONE** or **VISIBLE**)
             */
            fun onVisibilityChanged(visibility: Int)
        }

        interface Events : Visibility, Slide
    }

    init {
        init()
    }

    private fun init() {
        mBuilder.mSliderView.setOnTouchListener(this)
        mBuilder.mAlsoScrollView?.setOnTouchListener(this)
        mBuilder.mSliderView.viewTreeObserver.addOnGlobalLayoutListener(
            OnGlobalLayoutSingleListener(mBuilder.mSliderView, Runnable {
                mViewHeight = mBuilder.mSliderView.height.toFloat()
                mViewWidth = mBuilder.mSliderView.width.toFloat()
                when (mBuilder.mSlideDirection) {
                    SlideDirection.UP -> {
                        mBuilder.mSliderView.pivotY = 0f

                        setTouchableAreaVertical()
                    }

                    SlideDirection.DOWN -> {
                        mBuilder.mSliderView.pivotY = 0f
                        setTouchableAreaVertical()
                    }
                }
                createConsumers()
                updateToCurrentState()
            })
        )
        updateToCurrentState()
    }

    private fun setTouchableAreaHorizontal() {
        if (mBuilder.mTouchableArea == 0f) {
            mBuilder.mTouchableArea = mViewWidth //(float) Math.ceil(mViewWidth / 10);
        }
    }

    private fun setTouchableAreaVertical() {
        if (mBuilder.mTouchableArea == 0f) {
            mBuilder.mTouchableArea = mViewHeight //(float) Math.ceil(mViewHeight / 10);
        }
    }

    private fun createConsumers() {
        mVerticalTouchConsumer = VerticalTouchConsumer(mBuilder, this, mAnimationProcessor, slideLength)
        //        mHorizontalTouchConsumer = new HorizontalTouchConsumer(mBuilder, this, mAnimationProcessor);
    }

    val slideLength: Float
        get() {
            when (mBuilder.mSlideTo) {
                SlideTo.SELF -> return mViewHeight
                SlideTo.PARENT -> when (mBuilder.mSlideDirection) {
                    SlideDirection.UP -> {
                        val parent = mBuilder.mSliderView.parent
                        if (parent != null) {
                            return mBuilder.mSliderView.top.toFloat()
                        }
                        return 0f
                    }

                    SlideDirection.DOWN -> {
                        val parent = mBuilder.mSliderView.parent
                        if (parent != null) {
                            return ((parent as ViewGroup).height - mBuilder.mSliderView.bottom).toFloat()
                        }
                        return 0f
                    }
                }

                SlideTo.SPECIFY -> return mBuilder.mSpecifySlideTo.toFloat()
            }
            return 0f
        }

    private fun updateToCurrentState() {
        when (mBuilder.mStartState) {
            State.HIDDEN -> hideImmediately()
            State.SHOWED -> showImmediately()
            else -> {}
        }
    }

    //region public interface
    /**
     *
     * Trying hide soft input from window
     *
     * @see InputMethodManager.hideSoftInputFromWindow
     */
    fun hideSoftInput() {
        (mBuilder.mSliderView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(mBuilder.mSliderView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     *
     * Trying show soft input to window
     *
     * @see InputMethodManager.showSoftInput
     */
    fun showSoftInput() {
        (mBuilder.mSliderView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(mBuilder.mSliderView, 0)
    }

    val isVisible: Boolean
        /**
         *
         * Returns the visibility status for this view.
         *
         * @return true if view have status [View.VISIBLE]
         */
        get() = mBuilder.mSliderView.visibility == View.VISIBLE

    /**
     *
     * Add Listener which will be used in combination with this SlideUp
     */
    fun addSlideListener(listener: Listener) {
        mBuilder.mListeners.add(listener)
    }

    /**
     *
     * Remove Listener which was used in combination with this SlideUp
     */
    fun removeSlideListener(listener: Listener) {
        mBuilder.mListeners.remove(listener)
    }

    /**
     *
     * Returns typed view which was used as slider
     */
    fun <T : View?> getSliderView(): T? {
        return mBuilder.mSliderView as T
    }

    /**
     *
     * Set duration of animation (whenever you use [.hide] or [.show] methods)
     *
     * @param autoSlideDuration **(default - **300**)**
     */
    fun setAutoSlideDuration(autoSlideDuration: Int) {
        mBuilder.autoSlideDuration(autoSlideDuration)
        mAnimationProcessor.paramsChanged()
    }

    val autoSlideDuration: Float
        /**
         *
         * Returns duration of animation (whenever you use [.hide] or [.show] methods)
         */
        get() = mBuilder.mAutoSlideDuration.toFloat()

    var touchableAreaDp: Float
        /**
         *
         * Returns touchable area **(in dp)** for interaction
         */
        get() = mBuilder.mTouchableArea / mBuilder.mDensity
        /**
         *
         * Set touchable area **(in dp)** for interaction
         *
         * @param touchableArea **(default - **300dp**)**
         */
        set(touchableArea) {
            mBuilder.touchableAreaDp(touchableArea)
        }

    var touchableAreaPx: Float
        /**
         *
         * Returns touchable area **(in px)** for interaction
         */
        get() = mBuilder.mTouchableArea
        /**
         *
         * Set touchable area **(in px)** for interaction
         *
         * @param touchableArea **(default - **300dp**)**
         */
        set(touchableArea) {
            mBuilder.touchableAreaPx(touchableArea)
        }

    val isAnimationRunning: Boolean
        /**
         *
         * Returns running status of animation
         *
         * @return true if animation is running
         */
        get() = mAnimationProcessor.isAnimationRunning

    /**
     *
     * Show view with animation
     */
    fun show() {
        show(false)
    }

    /**
     *
     * Hide view with animation
     */
    fun hide() {
        hide(false)
    }

    /**
     *
     * Hide view without animation
     */
    fun hideImmediately() {
        hide(true)
    }

    /**
     *
     * Show view without animation
     */
    fun showImmediately() {
        show(true)
    }

    var isLoggingEnabled: Boolean
        /**
         *
         * Returns current status of debug logging
         */
        get() = mBuilder.mDebug
        /**
         *
         * Turning on/off debug logging
         *
         * @param enabled **(default - **false**)**
         */
        set(enabled) {
            mBuilder.loggingEnabled(enabled)
        }

    var isGesturesEnabled: Boolean
        /**
         *
         * Returns current status of gestures
         */
        get() = mBuilder.mGesturesEnabled
        /**
         *
         * Turning on/off gestures
         *
         * @param enabled **(default - **true**)**
         */
        set(enabled) {
            mBuilder.gesturesEnabled(enabled)
        }

    var interpolator: TimeInterpolator?
        /**
         *
         * Returns current interpolator
         */
        get() = mBuilder.mInterpolator
        /**
         *
         * Sets interpolator for animation (whenever you use [.hide] or [.show] methods)
         *
         * @param interpolator **(default - **Decelerate interpolator**)**
         */
        set(interpolator) {
            mBuilder.interpolator(interpolator)
            mAnimationProcessor.paramsChanged()
        }

    val slideDirection: SlideDirection?
        /**
         *
         * Returns gravity which used in combination with this SlideUp
         */
        get() = mBuilder.mSlideDirection

    var isHideKeyboardWhenDisplayed: Boolean
        /**
         *
         * Returns current behavior of soft input
         */
        get() = mBuilder.mHideKeyboard
        /**
         *
         * Sets behavior of soft input
         *
         * @param hide **(default - **false**)**
         */
        set(hide) {
            mBuilder.hideSoftInputWhenDisplayed(hide)
        }

    /**
     *
     * Toggle current state with animation
     */
    fun toggle() {
        if (isVisible) {
            hide()
        } else {
            show()
        }
    }

    /**
     *
     * Toggle current state without animation
     */
    fun toggleImmediately() {
        if (isVisible) {
            hideImmediately()
        } else {
            showImmediately()
        }
    }

    /**
     *
     * Saving current parameters of SlideUp
     */
    fun onSaveInstanceState(savedState: Bundle) {
        savedState.putBoolean(KEY_STATE_SAVED, true)
        savedState.putSerializable(KEY_START_DIRECTION, mBuilder.mSlideDirection)
        savedState.putBoolean(KEY_DEBUG, mBuilder.mDebug)
        savedState.putFloat(KEY_TOUCHABLE_AREA, mBuilder.mTouchableArea / mBuilder.mDensity)
        savedState.putSerializable(KEY_STATE, mCurrentState)
        savedState.putInt(KEY_AUTO_SLIDE_DURATION, mBuilder.mAutoSlideDuration)
        savedState.putBoolean(KEY_HIDE_SOFT_INPUT, mBuilder.mHideKeyboard)
    }

    //endregion
    private fun hide(immediately: Boolean) {
        mAnimationProcessor.endAnimation()
        when (mBuilder.mSlideDirection) {
            SlideDirection.UP -> if (immediately) {
                if (mBuilder.mSliderView.height > 0) {
                    mBuilder.mSliderView.translationY = -mViewHeight
                    notifyPercentChanged(100f)
                } else {
                    mBuilder.mStartState = State.HIDDEN
                }
            } else {
                mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, mBuilder.mSliderView.height.toFloat())
            }

            SlideDirection.DOWN -> if (immediately) {
                if (mBuilder.mSliderView.height > 0) {
                    mBuilder.mSliderView.translationY = mViewHeight
                    notifyPercentChanged(100f)
                } else {
                    mBuilder.mStartState = State.HIDDEN
                }
            } else {
                mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, mBuilder.mSliderView.height.toFloat())
            }
        }
    }

    private fun show(immediately: Boolean) {
        mAnimationProcessor.endAnimation()
        when (mBuilder.mSlideDirection) {
            SlideDirection.UP -> {
                if (immediately) {
                    if (mBuilder.mSliderView.height > 0) {
                        mBuilder.mSliderView.translationY = 0f
                        notifyPercentChanged(0f)
                    } else {
                        mBuilder.mStartState = State.SHOWED
                    }
                } else {
                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
                }
                if (immediately) {
                    if (mBuilder.mSliderView.height > 0) {
                        mBuilder.mSliderView.translationY = 0f
                        notifyPercentChanged(0f)
                    } else {
                        mBuilder.mStartState = State.SHOWED
                    }
                } else {
                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
                }
            }

            SlideDirection.DOWN -> if (immediately) {
                if (mBuilder.mSliderView.height > 0) {
                    mBuilder.mSliderView.translationY = 0f
                    notifyPercentChanged(0f)
                } else {
                    mBuilder.mStartState = State.SHOWED
                }
            } else {
                mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mAnimationProcessor.isAnimationRunning) return false
        if (!mBuilder.mGesturesEnabled) {
            mBuilder.mSliderView.performClick()
            return true
        }
        val consumed = when (mBuilder.mSlideDirection) {
            SlideDirection.UP -> mVerticalTouchConsumer?.slideUp(v, event)
            SlideDirection.DOWN -> mVerticalTouchConsumer?.slideDown(v, event)
            else -> throw IllegalArgumentException("You are using not supported gravity")
        } ?: false
        if (!consumed) {
            mBuilder.mSliderView.performClick()
        }
        return true
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Float
        when (mBuilder.mSlideDirection) {
            SlideDirection.UP -> onAnimationSlideUp(value)
            SlideDirection.DOWN -> onAnimationSlideDown(value)
        }
    }

    private fun onAnimationSlideUp(value: Float) {
        Log.d("zhangfei", "onAnimationSlideUp: $value")
        mBuilder.mSliderView.translationY = -value
//        val visibleDistance = mBuilder.mSliderView.top - mBuilder.mSliderView.y
//        val percents = (visibleDistance) * 100 / mViewHeight
//        notifyPercentChanged(percents)
    }

    private fun onAnimationSlideDown(value: Float) {
        Log.d("zhangfei", "onAnimationSlideDown: $value")
        mBuilder.mSliderView.translationY = value
//        val visibleDistance = mBuilder.mSliderView.y - mBuilder.mSliderView.top
//        val percents = (visibleDistance) * 100 / mViewHeight
//        notifyPercentChanged(percents)
    }

    private val start: Int
        //    private void onAnimationUpdateStartToEnd(float value) {
        get() = if (mBuilder.mIsRTL) {
            mBuilder.mSliderView.right
        } else {
            mBuilder.mSliderView.left
        }

    override fun notifyPercentChanged(percent: Float) {
        val pp = percent.coerceIn(0f, 100f)
        if (pp == 100f) {
//            mBuilder.mSliderView.visibility = View.GONE
//            notifyVisibilityChanged(View.GONE)
        } else {
//            mBuilder.mSliderView.visibility = View.VISIBLE
//            if (pp == 0f) {
//                notifyVisibilityChanged(View.VISIBLE)
//            }
        }
        if (mAnimationProcessor.slideAnimationTo == 0f && mBuilder.mHideKeyboard) hideSoftInput()
        if (mBuilder.mListeners.isNotEmpty()) {
            for (i in mBuilder.mListeners.indices) {
                val l = mBuilder.mListeners[i]
                if (l != null) {
                    if (l is Listener.Slide) {
                        l.onSlide(pp)
                        logValue(i, "onSlide", pp)
                    }
                } else {
                    logError(i, "onSlide")
                }
            }
        }
    }

    override fun notifyVisibilityChanged(visibility: Int) {
        if (!mBuilder.mListeners.isEmpty()) {
            for (i in mBuilder.mListeners.indices) {
                val l = mBuilder.mListeners[i]
                if (l != null) {
                    if (l is Listener.Visibility) {
                        l.onVisibilityChanged(visibility)
                        logValue(i, "onVisibilityChanged", if (visibility == View.VISIBLE) "VISIBLE" else if (visibility == View.GONE) "GONE" else visibility)
                    }
                } else {
                    logError(i, "onVisibilityChanged")
                }
            }
        }
        when (visibility) {
            View.VISIBLE -> mCurrentState = State.SHOWED
            View.GONE -> mCurrentState = State.HIDDEN
        }
    }

    override fun onAnimationStart(animator: Animator) {
    }

    override fun onAnimationEnd(animator: Animator) {
    }

    override fun onAnimationCancel(animator: Animator) {
    }

    override fun onAnimationRepeat(animator: Animator) {
    }

    private fun logValue(listener: Int, method: String, message: Any) {
        if (mBuilder.mDebug) {
            Log.e(TAG, String.format("Listener(%1s) (%2$-23s) value = %3\$s", listener, method, message))
        }
    }

    private fun logError(listener: Int, method: String) {
        if (mBuilder.mDebug) {
            Log.d(TAG, String.format("Listener(%1s) (%2$-23s) Listener is null, skip notification...", listener, method))
        }
    }

    companion object {
        private val TAG: String = SlideUp::class.java.simpleName

        val KEY_START_DIRECTION: String = TAG + "_start_direction"
        val KEY_DEBUG: String = TAG + "_debug"
        val KEY_TOUCHABLE_AREA: String = TAG + "_touchable_area"
        val KEY_STATE: String = TAG + "_state"
        val KEY_AUTO_SLIDE_DURATION: String = TAG + "_auto_slide_duration"
        val KEY_HIDE_SOFT_INPUT: String = TAG + "_hide_soft_input"
        val KEY_STATE_SAVED: String = TAG + "_state_saved"
    }
}
