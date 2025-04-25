package com.google.samples.slideup;

import static java.lang.Math.abs;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
class VerticalTouchConsumer extends TouchConsumer {
    private boolean mGoingUp = false;
    private boolean mGoingDown = false;

    VerticalTouchConsumer(SlideUpBuilder builder, LoggerNotifier notifier, AnimationProcessor animationProcessor) {
        super(builder, notifier, animationProcessor);
    }

    boolean slideDown(View touchedView, MotionEvent event) {
        Log.d("zhangfei", "consumeBottomToTop, touchedView: " + touchedView + ", event:" + event.getActionMasked());
        float touchedArea = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
//                mCanSlide = touchFromAlsoSlide(touchedView, event);
//                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
                Log.d("zhangfei", "slideDown(ACTION_DOWN) -> mViewHeight: " + mViewHeight + ", mCanSlide:" + mCanSlide);
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float slideLength = mBuilder.getSlideLength();
                float percents = moveTo * 100 / slideLength;
                calculateDirection(event);

                Log.d("zhangfei", "slideDown(ACTION_MOVE) -> difference: " + difference + ", mCanSlide:" + mCanSlide);
                Log.d("zhangfei", "                          moveTo:" + moveTo + ", slideLength:" + slideLength + ", percent:" + percents);
                if (mBuilder.mSlideDirection == SlideUp.SlideDirection.DOWN && moveTo > 0 && mCanSlide) {
                    mNotifier.notifyPercentChanged(percents);
                    mBuilder.mSliderView.setTranslationY(moveTo);
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = mBuilder.mSliderView.getTranslationY();
                if (slideAnimationFrom == mViewStartPositionY) {
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                }
                boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() > mBuilder.mSliderView.getHeight() / 5;

                if (scrollableAreaConsumed && mGoingDown) {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, mBuilder.mSliderView.getHeight());
                } else {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0);
                }
                mCanSlide = true;
                mGoingUp = false;
                mGoingDown = false;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }

    boolean slideUp(View touchedView, MotionEvent event) {
        float touchedArea = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
//                mCanSlide = touchFromAlsoSlide(touchedView, event);
//                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                Log.d("zhangfei", "slideUp(ACTION_DOWN) -> mViewHeight: " + mViewHeight + ", mCanSlide:" + mCanSlide);
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float slideLength = mBuilder.getSlideLength();
                float percents = abs(moveTo) * 100 / slideLength;
                calculateDirection(event);

                Log.d("zhangfei", "slideUp(ACTION_MOVE) -> difference: " + difference + ", mCanSlide:" + mCanSlide);
                Log.d("zhangfei", "                        moveTo:" + moveTo + ", slideLength:" + slideLength + ", percent:" + percents);
                if (mBuilder.mSlideDirection == SlideUp.SlideDirection.UP && moveTo < 0 && mCanSlide) {
                    mNotifier.notifyPercentChanged(percents);
                    mBuilder.mSliderView.setTranslationY(moveTo);
                }
                break;
            case MotionEvent.ACTION_UP:
                float slideAnimationFrom = -mBuilder.mSliderView.getTranslationY();
                if (slideAnimationFrom == mViewStartPositionY) {
                    return !Internal.isUpEventInView(mBuilder.mSliderView, event);
                }
                boolean scrollableAreaConsumed = mBuilder.mSliderView.getTranslationY() < -mBuilder.mSliderView.getHeight() / 5;

                if (scrollableAreaConsumed && mGoingUp) {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, mBuilder.mSliderView.getHeight() + mBuilder.mSliderView.getTop());
                } else {
                    mAnimationProcessor.setValuesAndStart(slideAnimationFrom, 0);
                }
                mCanSlide = true;
                break;
        }
        mPrevPositionY = event.getRawY();
        mPrevPositionX = event.getRawX();
        return true;
    }

    private void calculateDirection(MotionEvent event) {
        mGoingUp = mPrevPositionY - event.getRawY() > 0;
        mGoingDown = mPrevPositionY - event.getRawY() < 0;
    }
}
