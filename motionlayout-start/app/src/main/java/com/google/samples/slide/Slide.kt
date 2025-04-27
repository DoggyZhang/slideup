package com.google.samples.slide

import android.animation.TimeInterpolator
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
import com.google.samples.slide.touch.SlideTouchConsumer

class Slide(private val mBuilder: SlideBuilder) : OnTouchListener, SlideListener {
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

    enum class SlideDirection(val dir: Int) {
        UP(0x0001),
        DOWN(0x0010),
        VERTICAL(0x0011),
        LEFT(0x0100),
        RIGHT(0x1000),
        HORIZONTAL(0x1100);

        companion object {
            @JvmStatic
            fun isUp(dir: Int): Boolean {
                return (UP.dir and dir) > 0
            }

            @JvmStatic
            fun isDown(dir: Int): Boolean {
                return (DOWN.dir and dir) > 0
            }

            @JvmStatic
            fun isLeft(dir: Int): Boolean {
                return (LEFT.dir and dir) > 0
            }

            @JvmStatic
            fun isRight(dir: Int): Boolean {
                return (RIGHT.dir and dir) > 0
            }
        }
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

    private var touchConsumer: SlideTouchConsumer? = null

    /**
     *
     * Interface to listen to all handled events taking place in the slider
     */
    interface Listener {
        /**
         * @param percent percents of complete slide **(100 = HIDDEN, 0 = SHOWED)**
         */
        fun onSlide(percent: Float)


        /**
         * @param visibility (**GONE** or **VISIBLE**)
         */
        fun onVisibilityChanged(visibility: Int)
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
                    Slide.SlideDirection.UP,
                    Slide.SlideDirection.DOWN,
                    Slide.SlideDirection.VERTICAL -> {
                        mBuilder.mSliderView.pivotY = 0f
                    }

                    Slide.SlideDirection.LEFT,
                    Slide.SlideDirection.RIGHT,
                    Slide.SlideDirection.HORIZONTAL -> {
                        mBuilder.mSliderView.pivotX = 0f
                    }
                }
                createConsumers()
                updateToCurrentState()
            })
        )
        updateToCurrentState()
    }

    private fun createConsumers() {
        touchConsumer = SlideTouchConsumer(
            mBuilder,
            this,
            slideUpLength = getSlideLength(SlideDirection.UP),
            slideDownLength = getSlideLength(SlideDirection.DOWN),
            slideLeftLength = getSlideLength(SlideDirection.LEFT),
            slideRightLength = getSlideLength(SlideDirection.RIGHT)
        )
    }

    private fun getSlideLength(dir: SlideDirection): Float {
        when (mBuilder.mSlideTo) {
            SlideTo.SELF -> return mViewHeight
            SlideTo.PARENT -> when (dir) {
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

                SlideDirection.LEFT -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return mBuilder.mSliderView.left.toFloat()
                    }
                }

                SlideDirection.RIGHT -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return ((parent as ViewGroup).width - mBuilder.mSliderView.right).toFloat()
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
        // TODO: zhangfei
//        mAnimationProcessor.paramsChanged()
    }

    val autoSlideDuration: Float
        /**
         *
         * Returns duration of animation (whenever you use [.hide] or [.show] methods)
         */
        get() = mBuilder.mAutoSlideDuration.toFloat()


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
//            mAnimationProcessor.paramsChanged()
        }

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
        savedState.putSerializable(KEY_STATE, mCurrentState)
        savedState.putInt(KEY_AUTO_SLIDE_DURATION, mBuilder.mAutoSlideDuration)
        savedState.putBoolean(KEY_HIDE_SOFT_INPUT, mBuilder.mHideKeyboard)
    }

    //endregion
    private fun hide(immediately: Boolean) {
//        mAnimationProcessor.endAnimation()
//        when (mBuilder.mSlideDirection) {
//            SlideDirection.UP -> {
//                if (immediately) {
//                    if (mBuilder.mSliderView.height > 0) {
//                        mBuilder.mSliderView.translationY = -mViewHeight
//                        notifyPercentChanged(100f)
//                    } else {
//                        mBuilder.mStartState = State.HIDDEN
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, mBuilder.mSliderView.height.toFloat())
//                }
//            }
//
//            SlideDirection.DOWN -> {
//                if (immediately) {
//                    if (mBuilder.mSliderView.height > 0) {
//                        mBuilder.mSliderView.translationY = mViewHeight
//                        notifyPercentChanged(100f)
//                    } else {
//                        mBuilder.mStartState = State.HIDDEN
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, mBuilder.mSliderView.height.toFloat())
//                }
//            }
//
//            SlideDirection.LEFT -> {
//                if (immediately) {
//                    if (mBuilder.mSliderView.width > 0) {
//                        mBuilder.mSliderView.translationX = -mViewWidth
//                        notifyPercentChanged(100f)
//                    } else {
//                        mBuilder.mStartState = State.HIDDEN
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationX, mBuilder.mSliderView.width.toFloat())
//                }
//            }
//
//            SlideDirection.RIGHT -> {
//                if (immediately) {
//                    if (mBuilder.mSliderView.height > 0) {
//                        mBuilder.mSliderView.translationX = mViewWidth
//                        notifyPercentChanged(100f)
//                    } else {
//                        mBuilder.mStartState = State.HIDDEN
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationX, mBuilder.mSliderView.width.toFloat())
//                }
//            }
//        }
    }

    private fun show(immediately: Boolean) {
//        mAnimationProcessor.endAnimation()
//        when (mBuilder.mSlideDirection) {
//            SlideDirection.UP -> {
//                if (immediately) {
//                    if (mBuilder.mSliderView.height > 0) {
//                        mBuilder.mSliderView.translationY = 0f
//                        notifyPercentChanged(0f)
//                    } else {
//                        mBuilder.mStartState = State.SHOWED
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
//                }
//                if (immediately) {
//                    if (mBuilder.mSliderView.height > 0) {
//                        mBuilder.mSliderView.translationY = 0f
//                        notifyPercentChanged(0f)
//                    } else {
//                        mBuilder.mStartState = State.SHOWED
//                    }
//                } else {
//                    mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
//                }
//            }
//
//            SlideDirection.DOWN -> if (immediately) {
//                if (mBuilder.mSliderView.height > 0) {
//                    mBuilder.mSliderView.translationY = 0f
//                    notifyPercentChanged(0f)
//                } else {
//                    mBuilder.mStartState = State.SHOWED
//                }
//            } else {
//                mAnimationProcessor.setValuesAndStart(mBuilder.mSliderView.translationY, 0f)
//            }
//
//            SlideDirection.LEFT -> {
//                // TODO: zhangfei
//            }
//
//            SlideDirection.RIGHT -> {
//                // TODO: zhangfei
//            }
//        }
    }


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (touchConsumer?.isAnimationRunning() == true) return false
        if (!mBuilder.mGesturesEnabled) {
            mBuilder.mSliderView.performClick()
            return true
        }

        val consumed = touchConsumer?.onTouch(v, event) ?: false
        if (!consumed) {
            mBuilder.mSliderView.performClick()
        }
        return true
    }

    override fun notifyPercentChanged(percent: Float, dir: SlideDirection) {
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
        if (mBuilder.mListeners.isNotEmpty()) {
            for (i in mBuilder.mListeners.indices) {
                val l = mBuilder.mListeners[i]
                if (l != null) {
                    l.onSlide(pp)
                    logValue(i, "onSlide", pp)
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
                    l.onVisibilityChanged(visibility)
                    logValue(i, "onVisibilityChanged", if (visibility == View.VISIBLE) "VISIBLE" else if (visibility == View.GONE) "GONE" else visibility)
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

    private fun logValue(listener: Int, method: String, message: Any) {
        Log.d(TAG, String.format("Listener(%1s) (%2$-23s) value = %3\$s", listener, method, message))
    }

    private fun logError(listener: Int, method: String) {
        Log.e(TAG, String.format("Listener(%1s) (%2$-23s) Listener is null, skip notification...", listener, method))
    }

    companion object {
        private val TAG: String = Slide::class.java.simpleName

        val KEY_START_DIRECTION: String = TAG + "_start_direction"
        val KEY_STATE: String = TAG + "_state"
        val KEY_AUTO_SLIDE_DURATION: String = TAG + "_auto_slide_duration"
        val KEY_HIDE_SOFT_INPUT: String = TAG + "_hide_soft_input"
        val KEY_STATE_SAVED: String = TAG + "_state_saved"
    }
}
