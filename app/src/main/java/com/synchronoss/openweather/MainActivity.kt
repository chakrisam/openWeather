package com.synchronoss.openweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.synchronoss.openweather.api.ApiService
import com.synchronoss.openweather.api.ApiUtils
import com.synchronoss.openweather.databinding.ActivityMainBinding
import com.synchronoss.openweather.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    var apiService: ApiService? = null
    var appId = "5e30019148e480a7cd32b6e77380807a"
    var lat = "35"
    var lng = "139"
    val PERMISSION_ID = 99
    private lateinit var wifiManager: WifiManager
    lateinit var mFusedLocationClient: FusedLocationProviderClient


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get the API Instance
        apiService = ApiUtils.apiService
        //initiate the locationServices
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Set the ClickListeners for the button to receive the Weather Update
        binding.getWeatherBtn.setOnClickListener(this)

        wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (wifiManager.isWifiEnabled) {
                    getCurrentData(lat, lng, appId)
                } else {
                    Toast.makeText(
                            this@MainActivity,
                            "Turn on Wifi to access the app",
                            Toast.LENGTH_LONG
                    ).show()

                }

                handler.postDelayed(this, 10000)//
            }
        }
        handler.postDelayed(runnable, 10000)

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

    private fun getCurrentData(lat: String, lng: String, appId: String) {
        binding.progressBar.visibility = View.VISIBLE
        apiService?.getCurrentWeatherData(lat, lng, appId)?.enqueue(object :
                Callback<WeatherResponse?> {
            override fun onResponse(
                    @NonNull call: Call<WeatherResponse?>,
                    @NonNull response: Response<WeatherResponse?>
            ) {
                binding.progressBar.visibility = View.GONE

                if (response.code() == 200) {
                    val weatherResponse: WeatherResponse = response.body()!!
                    val stringBuilder = """
                            Country: ${weatherResponse.sys?.country.toString()}
                            Temperature: ${weatherResponse.main?.temp.toString()}
                            Temperature(Min): ${weatherResponse.main?.temp_min.toString()}
                            Temperature(Max): ${weatherResponse.main?.temp_max.toString()}
                            Humidity: ${weatherResponse.main?.humidity.toString()}
                            Pressure: ${weatherResponse.main?.pressure}
                            """.trimIndent()
                    binding.weatherTv.text = stringBuilder
                }
            }

            override fun onFailure(
                    @NonNull call: Call<WeatherResponse?>,
                    @NonNull t: Throwable
            ) {
                binding.progressBar.visibility = View.GONE

                binding.weatherTv.text = t.message
            }
        })

    }


}
