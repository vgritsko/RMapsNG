package com.sm.maps.applib.presentation.viewmodel

object BearingCalculator {
    fun update(newBearing: Float, lastBearing: Float): Float {
        var dif = newBearing - lastBearing
        if (Math.abs(dif) > 180) dif = 360 - dif
        if (Math.abs(dif) < 1) return lastBearing
        if (Math.abs(dif) >= 90) return newBearing
        var result = lastBearing + (90 * Math.signum(dif) * Math.pow(Math.abs(dif).toDouble() / 90, 2.0)).toFloat()
        while (result > 360) result -= 360
        while (result < 0) result += 360
        return result
    }
}
