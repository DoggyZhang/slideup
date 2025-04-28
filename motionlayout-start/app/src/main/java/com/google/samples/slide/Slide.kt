package com.google.samples.slide

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IntDef
import androidx.core.view.children
import com.google.samples.slide.touch.SlideTouchConsumer

class Slide(private val mBuilder: SlideBuilder) : OnTouchListener, SlideListener {

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

        fun onSlideToEnd()
    }

    init {
        init()
    }

    private fun init() {
        mBuilder.mSliderView.setOnTouchListener(this)
        mBuilder.mSliderView.viewTreeObserver.addOnGlobalLayoutListener(
            OnGlobalLayoutSingleListener(mBuilder.mSliderView, Runnable {
//                mBuilder.mSliderView.touchDelegate = object : TouchDelegate(
//                    Rect(
//                        mBuilder.mSliderView.left,
//                        mBuilder.mSliderView.top,
//                        mBuilder.mSliderView.right,
//                        mBuilder.mSliderView.bottom
//                    ),
//                    mBuilder.mSliderView
//                ) {
//                    override fun onTouchEvent(event: MotionEvent): Boolean {
//                        Log.d("zhangfei", "touchDelegate, event:${event.actionMasked}")
//                        return onTouch(mBuilder.mSliderView, event)
//                    }
//                }

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
            })
        )
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
            SlideTo.PARENT -> when {
                SlideDirection.isUp(dir.dir) -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return mBuilder.mSliderView.top.toFloat() + mViewHeight
                    }
                    return 0f
                }

                SlideDirection.isDown(dir.dir) -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return ((parent as ViewGroup).height - mBuilder.mSliderView.bottom).toFloat() + mViewHeight
                    }
                    return 0f
                }

                SlideDirection.isLeft(dir.dir) -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return mBuilder.mSliderView.left.toFloat() + mViewWidth
                    }
                }

                SlideDirection.isRight(dir.dir) -> {
                    val parent = mBuilder.mSliderView.parent
                    if (parent != null) {
                        return ((parent as ViewGroup).width - mBuilder.mSliderView.right).toFloat() + mViewWidth
                    }
                    return 0f
                }
            }

            SlideTo.SPECIFY -> return mBuilder.mSpecifySlideTo.toFloat()
        }
        return 0f
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
     * Saving current parameters of SlideUp
     */
    fun onSaveInstanceState(savedState: Bundle) {
        savedState.putBoolean(KEY_STATE_SAVED, true)
        savedState.putSerializable(KEY_START_DIRECTION, mBuilder.mSlideDirection)
        savedState.putInt(KEY_AUTO_SLIDE_DURATION, mBuilder.mAutoSlideDuration)
        savedState.putBoolean(KEY_HIDE_SOFT_INPUT, mBuilder.mHideKeyboard)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (touchConsumer?.isAnimationRunning() == true) return true
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

    override fun performClick(event: MotionEvent) {
        if (mBuilder.mSliderView is ViewGroup) {
            for (child in mBuilder.mSliderView.children) {
                if (child.isClickable) {
                    if (isTouchPointInView(child, event.rawX.toInt(), event.rawY.toInt())) {
                        child.performClick()
                        return
                    }
                }
            }
        } else {
            if (mBuilder.mSliderView.isClickable) {
                mBuilder.mSliderView.performClick()
            }
        }
    }

    override fun notifySlideToEnd() {
        if (mBuilder.mListeners.isNotEmpty()) {
            for (listener in mBuilder.mListeners) {
                listener?.onSlideToEnd()
            }
        }
    }

    fun isTouchPointInView(view: View, x: Int, y: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight

        return y in top..bottom && x >= left && x <= right
    }

    override fun notifyPercentChanged(percent: Float, dir: SlideDirection) {
        if (mBuilder.mListeners.isNotEmpty()) {
            for (listener in mBuilder.mListeners) {
                listener?.onSlide(percent)
            }
        }
    }

    companion object {
        private val TAG: String = Slide::class.java.simpleName

        val KEY_START_DIRECTION: String = TAG + "_start_direction"
        val KEY_AUTO_SLIDE_DURATION: String = TAG + "_auto_slide_duration"
        val KEY_HIDE_SOFT_INPUT: String = TAG + "_hide_soft_input"
        val KEY_STATE_SAVED: String = TAG + "_state_saved"
    }
}
