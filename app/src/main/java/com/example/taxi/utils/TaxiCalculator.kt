package com.example.taxi.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.preference.UserPreferenceManager
import kotlin.math.roundToInt

object TaxiCalculator {


    @RequiresApi(Build.VERSION_CODES.N)
    fun getCurrentDriveCost(
        dashboardData: DashboardData,
        preferenceManager: UserPreferenceManager,
        secondsElapsed: Long
    ) :String{

        val moneyTotalDistance = preferenceManager.loadMileageData()

        Log.d("orta", "getCurrentDriveCost: ${dashboardData.getDistanceText()}")
        val waitTime = ((preferenceManager.getFinishedTimeAcceptOrder() - preferenceManager.getTransitionTime()) / 1000).toInt() + secondsElapsed
        val currentDriveCost: Int? = moneyTotalDistance?.let { calculateTotalCost(it,dashboardData.getDistanceText() * 1000).toInt() }
//            plus((waitTime / 60.0) * preferenceManager.getCostWaitTimePerMinute()

        return PhoneNumberUtil.formatMoneyNumberPlate(currentDriveCost.toString())
    }


    fun calculateTotalCost(mileagePrices: List<MileageData>, remainingDistance: Double): Double {
        var remainingDistanceVar = remainingDistance.toDouble()
        var previousKm = 0.0
        var totalCost = 0.0

        if (mileagePrices.isNotEmpty()) {
            for (mileagePrice in mileagePrices) {
                val currentKm = mileagePrice.value * 1000.0 // value in meters
                val distanceToConsider = minOf(remainingDistanceVar, currentKm - previousKm)
                totalCost += (distanceToConsider / 1000) * mileagePrice.price
                remainingDistanceVar -= distanceToConsider
                previousKm = currentKm
                if (remainingDistanceVar <= 0) {
                    break
                }
            }
            if (remainingDistanceVar > 0) {
                val lastMileagePrice = mileagePrices.last()
                val standardPricePerKm = lastMileagePrice.price
                totalCost += (remainingDistanceVar / 1000) * standardPricePerKm
            }
            totalCost = kotlin.math.round(totalCost / 500) * 500
        }

        return totalCost
    }
     fun roundToNearestMultiple(number: Int): Int {
       val multiple = 500
        val remainder = number % multiple
        return if (remainder < multiple / 2) {
            number - remainder
        } else {
            number + (multiple - remainder)
        }
    }

}

data class TaxiData(
    var costPerKm: Int? = 0,
    var costWaitTimePerMinute: Int? = 0,
    var costOutCenter: Int? = 0,
    var startCost: Int? = 0,
    var minimalDistance: Int? = 0,
    var minWaitTime: Int? = 0,
    var currentDriveDistance: Int? = 0
)