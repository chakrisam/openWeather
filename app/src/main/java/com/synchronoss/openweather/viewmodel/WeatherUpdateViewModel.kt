package com.synchronoss.openweather.viewmodel

import android.app.Application
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.synchronoss.openweather.api.ApiService
import com.synchronoss.openweather.model.WeatherResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Query


class WeatherUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService(context = getApplication())
    private val disposable = CompositeDisposable()
    private lateinit var wifiManager: WifiManager
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val mContext = application
    var appId = "5e30019148e480a7cd32b6e77380807a"
    var lat = "35"
    var lng = "139"
    val PERMISSION_ID = 99
    val weatherResponse = MutableLiveData<WeatherResponse>()
    val apiError = MutableLiveData<Boolean>()
    val apiLoading = MutableLiveData<Boolean>()

    //WorkManager Implementation improvements needed to make the things work as the dependency has to the flow with API or the ViewModel calls

    //    private val workManager = WorkManager.getInstance(mContext)
    /*fun startPeriodicWork()
    {
        val periodicWorkRequest : PeriodicWorkRequest = PeriodicWorkRequestBuilder<LogWorker>(
            15, TimeUnit.MINUTES)
            .addTag(LOG_WORKER_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "MyUniqueWorkName",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    fun stopPeriodicWork()
    {
        workManager.cancelAllWorkByTag(LOG_WORKER_TAG)
    }*/
    /* fun loadDataFromWorker(lifecycleOwner: LifecycleOwner?) {

         val constraints = Constraints.Builder().setRequiresCharging(true)
             .setRequiredNetworkType(NetworkType.UNMETERED).build()


         val periodicWorkRequest =
             PeriodicWorkRequest.Builder(BackgroundTask::class.java, 1, TimeUnit.SECONDS)
                 .setInputData(Data.Builder().putBoolean("isStart", true).build())
                 .setInitialDelay(6000, TimeUnit.MILLISECONDS)
                 .build()

         val workManager = WorkManager.getInstance()

         workManager.enqueue(periodicWorkRequest)

         workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id).observeForever {
             if (it != null) {

                 Log.d("periodicWorkRequest", "Status changed to ${it.state.isFinished}")

             }
         }
     }
     fun registerTimer(context: AppCompatActivity){

         //initiate the locationServices
         mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
         wifiManager =
            mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

         val handler = Handler()
         val runnable: Runnable = object : Runnable {
             override fun run() {
                 if (wifiManager.isWifiEnabled) {
                     getLastLocation();
                 } else {
                     Toast.makeText(
                         mContext,
                         "Turn on Wifi to access the app",
                         Toast.LENGTH_LONG
                     ).show()

                 }

                 handler.postDelayed(this, 10000)//
             }
         }
         handler.postDelayed(runnable, 10000)


     }
     @SuppressLint("MissingPermission")
     private fun getLastLocation() {
         if (checkPermissions()) {
             if (isLocationEnabled()) {

                 mFusedLocationClient.lastLocation.addOnCompleteListener(mContext) { task ->
                     var location: Location? = task.result
                     if (location == null) {
                         requestNewLocationData()
                     } else {

                         lat = location.latitude.toString()
                         lng = location.longitude.toString()
                         callWeatherUpdateApi(lat, lng, appId)

                     }
                 }
             } else {
                 Toast.makeText(mContext, "Turn on location", Toast.LENGTH_LONG).show()
               *//*  val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)*//*
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
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
            callWeatherUpdateApi(lat, lng, appId)

        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
*//*
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            mContext,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }*//*


    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }*/

    // END of the Work Manger integration

    fun callWeatherUpdateApi(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("APPID") app_id: String?
    ) {


        apiLoading.value = true
        disposable.add(
            apiService.getWeatherData(lat, lon, app_id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<WeatherResponse>() {
                    override fun onSuccess(responseObj: WeatherResponse) {
                        weatherResponse.value = responseObj
                        apiError.value = false
                        apiLoading.value = false

                    }

                    override fun onError(e: Throwable) {
                        apiError.value = true
                        apiLoading.value = false
                        e.printStackTrace()
                    }

                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}