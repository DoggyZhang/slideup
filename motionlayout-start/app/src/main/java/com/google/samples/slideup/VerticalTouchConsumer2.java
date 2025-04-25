package com.google.samples.slideup;

import static java.lang.Math.abs;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
class VerticalTouchConsumer2 extends TouchConsumer {
    private boolean mGoingUp = false;
    private boolean mGoingDown = false;

    VerticalTouchConsumer2(SlideUpBuilder builder, LoggerNotifier notifier, AnimationProcessor animationProcessor) {
        super(builder, notifier, animationProcessor);
    }

    boolean consumeBottomToTop(View touchedView, MotionEvent event) {
        Log.d("zhangfei", "consumeBottomToTop, touchedView: " + touchedView + ", event:" + event.getActionMasked());
        float touchedArea = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= mBuilder.mTouchableArea >= touchedArea;
                Log.d("zhangfei", "DOWN -> mViewHeight: " + mViewHeight + ", mCanSlide:" + mCanSlide);
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float percents = abs(moveTo) * 100 / mBuilder.mSliderView.getHeight();
                calculateDirection(event);

                Log.d("zhangfei", "MOVE -> difference: " + difference + ", moveTo:" + moveTo + ", mCanSlide:" + mCanSlide);
                if (mBuilder.mSlideDirection == SlideUp.SlideDirection.UP) {
                    if (moveTo < 0 && mCanSlide) {
                        mNotifier.notifyPercentChanged(percents);
                        mBuilder.mSliderView.setTranslationY(moveTo);
                    }
                } else {
                    if (moveTo > 0 && mCanSlide) {
                        mNotifier.notifyPercentChanged(percents);
                        mBuilder.mSliderView.setTranslationY(moveTo);
                    }
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

    boolean consumeTopToBottom(View touchedView, MotionEvent event) {
        float touchedArea = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewHeight = mBuilder.mSliderView.getHeight();
                mStartPositionY = event.getRawY();
                mViewStartPositionY = mBuilder.mSliderView.getTranslationY();
                mCanSlide = touchFromAlsoSlide(touchedView, event);
                mCanSlide |= getBottom() - mBuilder.mTouchableArea <= touchedArea;
                break;
            case MotionEvent.ACTION_MOVE:
                float difference = event.getRawY() - mStartPositionY;
                float moveTo = mViewStartPositionY + difference;
                float percents = moveTo * 100 / -mBuilder.mSliderView.getHeight();
                calculateDirection(event);

                if (moveTo < 0 && mCanSlide) {
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
