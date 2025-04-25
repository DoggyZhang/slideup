package com.google.samples.slideup

import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

/**
 * @author pa.gulko zTrap (12.07.2017)
 */
class OnGlobalLayoutSingleListener internal constructor(private val mView: View, private val mRunnable: Runnable) : OnGlobalLayoutListener {
    override fun onGlobalLayout() {
        val observer = mView.viewTreeObserver
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            observer.removeGlobalOnLayoutListener(this)
        } else {
            observer.removeOnGlobalLayoutListener(this)
        }
        mRunnable.run()
    }
}
