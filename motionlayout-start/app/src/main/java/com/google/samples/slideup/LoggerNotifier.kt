package com.google.samples.slideup

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
internal interface LoggerNotifier {
    fun notifyPercentChanged(percent: Float)

    fun notifyVisibilityChanged(visibility: Int)
}
