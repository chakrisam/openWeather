package com.synchronoss.openweather.util

import android.app.Service
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.synchronoss.openweather.appdata.AppPreference
import com.synchronoss.openweather.constants.PrefConstants

class BackgroundTask(context: Context, params: WorkerParameters) : Worker(context, params) {
    private var mLocationManager: LocationManager? = null
    var locationUpdate: Location? = null
    private lateinit var appPreference: AppPreference
    private val mContext = context

    override fun doWork(): Result {
        appPreference = AppPreference(mContext)
        initializeLocationManager()

        // Get the location updates through the network or the GPS
        try {
            mLocationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(),
                LOCATION_DISTANCE,
                mLocationListeners[1]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }
        try {
            mLocationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(),
                LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }
        updateLocation()

        return Result.success()
    }



    private fun updateLocation() {
        val gson = Gson()
        val locationJson: String = gson.toJson(locationUpdate)
        appPreference.putString(PrefConstants.location, locationJson)

    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager =
                applicationContext.getSystemService(Service.LOCATION_SERVICE) as LocationManager
        }
    }

    inner class LocationListener(provider: String) : android.location.LocationListener {
        var mLastLocation: Location
        override fun onLocationChanged(location: Location) {
            mLastLocation.set(location)
            locationUpdate = location
        }

        override fun onProviderDisabled(provider: String) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        init {
            mLastLocation = Location(provider)
        }
    }

    private var mLocationListeners = arrayOf<LocationListener>(
        LocationListener(LocationManager.GPS_PROVIDER),
        LocationListener(LocationManager.NETWORK_PROVIDER)
    )

    companion object {
        private const val TAG = "BGTASK"
        private const val LOCATION_INTERVAL = 1000
        private const val LOCATION_DISTANCE = 0.0f
    }
}
