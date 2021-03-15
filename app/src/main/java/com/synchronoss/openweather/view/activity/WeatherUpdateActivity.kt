package com.synchronoss.openweather.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.synchronoss.openweather.BaseActivity
import com.synchronoss.openweather.R
import com.synchronoss.openweather.appdata.AppPreference
import com.synchronoss.openweather.constants.PrefConstants
import com.synchronoss.openweather.databinding.ActivityWeatherBinding
import com.synchronoss.openweather.model.LatLng
import com.synchronoss.openweather.viewmodel.WeatherUpdateViewModel


open class WeatherUpdateActivity : BaseActivity(), View.OnClickListener {
    var appId = "5e30019148e480a7cd32b6e77380807a"
    var lat = "35"
    var lng = "139"
    val PERMISSION_ID = 99
    private lateinit var wifiManager: WifiManager
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var viewModel: WeatherUpdateViewModel
    private lateinit var appPreference: AppPreference
    lateinit var locationData: LatLng
    private lateinit var receiver: WifiLevelReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerServices()
        registerClickListeners()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_start -> {
                if (wifiManager.isWifiEnabled) {
                    if (checkPermissions()) {
                        if (isLocationEnabled()) {
                            Toast.makeText(applicationContext, "Started Service", Toast.LENGTH_LONG)
                                .show()
                            startTracking()
                        } else {
                            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intent)
                        }
                    } else {
                        requestPermissions()
                    }
                } else {
                    Toast.makeText(this, "Turn on Wifi to access the app", Toast.LENGTH_LONG).show()

                }
                return true
            }
            R.id.action_stop -> {
                Toast.makeText(applicationContext, "Stopped Service", Toast.LENGTH_LONG).show()
                stopTracking()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerClickListeners() {

        binding.getWeatherBtn.setOnClickListener(this)

    }

    private fun registerServices() {
        appPreference = AppPreference(this)
        appPreference.putString(PrefConstants.APPID, appId)

        val dataCache = appPreference.getString(PrefConstants.DataCache)
        if (dataCache != null && dataCache.isNotEmpty()) {
            binding.weatherTv.text = dataCache
        }

        receiver = WifiLevelReceiver(this)
        registerReceiver(receiver, IntentFilter("UPDATE_WEATHER")) //<----Register
        viewModel = ViewModelProvider(this).get(WeatherUpdateViewModel::class.java)
        wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        //initiate the locationServices
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        observeViewModel()

        //WorkManager Implementation improvements needed to make the things work as the dependency has to the flow with API or the ViewModel calls


        /*  val periodicWorkRequest =
              PeriodicWorkRequest.Builder(BackgroundTask::class.java, 1, TimeUnit.SECONDS)
                  .setInputData(Data.Builder().putBoolean("isStart", true).build())
                  .setInitialDelay(6000, TimeUnit.MILLISECONDS)
                  .build()

          val workManager = WorkManager.getInstance(this)

          workManager.enqueue(periodicWorkRequest)

          workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id).observeForever {
              if (it != null) {

                  Log.i(TAG, "Status changed to ${it.state.isFinished}")

              }
          }
  */
    }


    private fun observeViewModel() {

        viewModel.weatherResponse.observe(this, Observer { weatherResponse ->
            weatherResponse?.let {

                val stringBuilder = """
                            Country: ${weatherResponse.sys?.country.toString()}
                            Temperature: ${weatherResponse.main?.temp.toString()}
                            Temperature(Min): ${weatherResponse.main?.temp_min.toString()}
                            Temperature(Max): ${weatherResponse.main?.temp_max.toString()}
                            Humidity: ${weatherResponse.main?.humidity.toString()}
                            Pressure: ${weatherResponse.main?.pressure}
                            """.trimIndent()
                binding.weatherTv.text = stringBuilder
                appPreference.putString(PrefConstants.DataCache, stringBuilder)
            }
        })
        viewModel.apiLoading.observe(this, Observer { isLoading ->

            isLoading?.let {
                if (it) showProgress("Loading...") else dismissProgress()
            }
        })
        viewModel.apiError.observe(this, Observer { isError ->
            isError?.let {
                if (isError) showAlert("No Data...") else dismissProgress()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {

                        lat = location.latitude.toString()
                        lng = location.longitude.toString()
                        getCurrentData(lat, lng, appId)

                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            lat = mLastLocation.latitude.toString()
            lng = mLastLocation.longitude.toString()
            getCurrentData(lat, lng, appId)

        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    fun getCurrentData(lat: String, lng: String, appId: String) {
        viewModel.callWeatherUpdateApi(lat, lng, appId)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.getWeather_btn -> {
                if (wifiManager.isWifiEnabled) {
                    getLastLocation();
                } else {
                    Toast.makeText(this, "Turn on Wifi to access the app", Toast.LENGTH_LONG).show()

                }
            }

        }
    }

    override fun onStop() {
        super.onStop()
//        unregisterReceiver(receiver) //<-- Unregister to avoid memoryleak
    }

    internal class WifiLevelReceiver(context: WeatherUpdateActivity?) : BroadcastReceiver() {

        var activityInstance = context

        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == "UPDATE_WEATHER") {
                val level = intent.getStringExtra("MESSAGE")
                if (level.equals("update")) {
                    val gson = Gson()
                    val json: String? =
                        activityInstance?.appPreference?.getString(PrefConstants.location)

                    if (json != null) {
                        activityInstance?.locationData = gson.fromJson(json, LatLng::class.java)
                        activityInstance?.getCurrentData(
                            activityInstance?.locationData!!.lat,
                            activityInstance?.locationData!!.lng,
                            activityInstance?.appId.toString()
                        )
                    }

                }
            }
        }
    }
}