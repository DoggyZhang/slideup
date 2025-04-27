package com.google.samples.slide.touch

import android.view.MotionEvent
import android.view.View
import com.google.samples.slide.Slide.SlideDirection
import com.google.samples.slide.SlideBuilder
import com.google.samples.slide.SlideListener

internal class SlideTouchConsumer(
    val builder: SlideBuilder,
    notifier: SlideListener,

    slideUpLength: Float = 0f,
    slideDownLength: Float = 0f,
    slideLeftLength: Float = 0f,
    slideRightLength: Float = 0f
) {

    private val slideUpTouchConsumer = SlideUpTouchConsumer(builder, notifier, slideUpLength)
    private val slideDownTouchConsumer = SlideDownTouchConsumer(builder, notifier, slideDownLength)
    private val slideLeftTouchConsumer = SlideLeftTouchConsumer(builder, notifier, slideLeftLength)
    private val slideRightTouchConsumer = SlideRightTouchConsumer(builder, notifier, slideRightLength)


    fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (SlideDirection.isUp(builder.mSlideDirection.dir)) {
                    slideUpTouchConsumer.slideUp(v, event)
                }
                if (SlideDirection.isDown(builder.mSlideDirection.dir)) {
                    slideDownTouchConsumer.slideDown(v, event)
                }

                if (SlideDirection.isLeft(builder.mSlideDirection.dir)) {
                    slideLeftTouchConsumer.slideLeft(v, event)
                }

                if (SlideDirection.isRight(builder.mSlideDirection.dir)) {
                    slideRightTouchConsumer.slideRight(v, event)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (SlideDirection.isUp(builder.mSlideDirection.dir)) {
                    if (slideUpTouchConsumer.slideUp(v, event)) {
                        return true
                    }
                }
                if (SlideDirection.isDown(builder.mSlideDirection.dir)) {
                    if (slideDownTouchConsumer.slideDown(v, event)) {
                        return true
                    }
                }

                if (SlideDirection.isLeft(builder.mSlideDirection.dir)) {
                    if (slideLeftTouchConsumer.slideLeft(v, event)) {
                        return true
                    }
                }

                if (SlideDirection.isRight(builder.mSlideDirection.dir)) {
                    if (slideRightTouchConsumer.slideRight(v, event)) {
                        return true
                    }
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                if (SlideDirection.isUp(builder.mSlideDirection.dir)) {
                    if (slideUpTouchConsumer.slideUp(v, event)) {
                        return true
                    }
                }
                if (SlideDirection.isDown(builder.mSlideDirection.dir)) {
                    if (slideDownTouchConsumer.slideDown(v, event)) {
                        return true
                    }
                }

                if (SlideDirection.isLeft(builder.mSlideDirection.dir)) {
                    if (slideLeftTouchConsumer.slideLeft(v, event)) {
                        return true
                    }
                }

                if (SlideDirection.isRight(builder.mSlideDirection.dir)) {
                    if (slideRightTouchConsumer.slideRight(v, event)) {
                        return true
                    }
                }
                return false
            }
        }
        return false
    }

    fun isAnimationRunning(): Boolean {
        return slideUpTouchConsumer.mAnimationProcessor.isAnimationRunning
                || slideDownTouchConsumer.mAnimationProcessor.isAnimationRunning
                || slideLeftTouchConsumer.mAnimationProcessor.isAnimationRunning
                || slideRightTouchConsumer.mAnimationProcessor.isAnimationRunning

    }

}
