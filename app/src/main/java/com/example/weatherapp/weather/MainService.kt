package com.example.weatherapp.weather

import android.annotation.SuppressLint
import com.example.weatherapp.R
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.DialogManager
import com.example.weatherapp.util.PermissionValidator
import com.google.android.gms.location.*
import com.google.gson.Gson
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainService {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var context: Context
    private lateinit var bindingView: ActivityMainBinding

    private lateinit var mSharedPreferences: SharedPreferences

    companion object{
        val instance = MainService()
    }

    fun setFusedLocationClient(ctxt: Context, binding: ActivityMainBinding){
        context = ctxt
        bindingView = binding
        mSharedPreferences = ctxt.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private val mLocationCallback = object: LocationCallback(){
        override fun onLocationResult(location: LocationResult) {
            val mLastLocation: Location = location.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("GPS", "$latitude")
            val longitude = mLastLocation.longitude
            Log.i("GPS", "$longitude")

            getLocationWeatherDetails(latitude, longitude)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    fun getLocationWeatherDetails(lat: Double, lon: Double){
        if(PermissionValidator.instance.isNetworkAvailable(context)){
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherEndpoint = retrofit.create(WeatherEndpoint::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(lat, lon, Constants.METRIC_UNIT, Constants.APP_ID)

            val loadDialog = DialogManager.instance.backgroundDialog(context)
            loadDialog.show()

            listCall.enqueue(object: Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if(response.isSuccessful){
                        loadDialog.dismiss()
                        val weatherList: WeatherResponse? = response.body()

                        //Save it on sharedPreferences
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                        setupUI()
                        Log.i("Response Result", "$weatherList")
                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> Log.e("ERROR 400", "Bad Connection")
                            404 -> Log.e("ERROR 404", "Not Found")
                            else -> Log.e("ERROR", "Generic Error")
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    loadDialog.dismiss()
                    Log.e("ERROR", t.message.toString())
                }

            })
        }else{
            Toast.makeText(context, "You have not internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    fun setupUI(){
        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if(!weatherResponseJsonString.isNullOrBlank()) {
            val weatherList =
                Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (i in weatherList.weather.indices) {
                Log.i("Weather Name", weatherList.weather.toString())
                bindingView.tvMain.text = weatherList.weather[i].main
                bindingView.tvMainDescription.text = weatherList.weather[i].description

                bindingView.tvHumidity.text =
                    weatherList.main.temp.toString() + getUnit(context.applicationContext.resources.configuration.locale.toString())
                bindingView.tvHumidityDescription.text =
                    "${weatherList.main.humidity.toString()} per cent"

                bindingView.tvTemp.text = "${weatherList.main.temp_min.toString()} min"
                bindingView.tvTempDescription.text = "${weatherList.main.temp_max.toString()} max"

                bindingView.tvSpeed.text = "${weatherList.wind.speed}"
                bindingView.tvSpeedDescription.text = "miles/hour"

                bindingView.tvLocation.text = "${weatherList.name}"
                bindingView.tvLocationDescription.text = "${weatherList.sys.country}"

                bindingView.tvSunrise.text = unixTime(weatherList.sys.sunrise)
                bindingView.tvSunset.text = unixTime(weatherList.sys.sunset)


                when (weatherList.weather[i].icon) {
                    "01d" -> bindingView.ivMain.setImageResource(R.drawable.sunny)
                    "02d" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "03d" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "04d" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "04n" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "10d" -> bindingView.ivMain.setImageResource(R.drawable.rain)
                    "11d" -> bindingView.ivMain.setImageResource(R.drawable.storm)
                    "13d" -> bindingView.ivMain.setImageResource(R.drawable.snowflake)
                    "01n" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "02n" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "03n" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "10n" -> bindingView.ivMain.setImageResource(R.drawable.cloud)
                    "11n" -> bindingView.ivMain.setImageResource(R.drawable.rain)
                    "13n" -> bindingView.ivMain.setImageResource(R.drawable.snowflake)
                }
            }
        }
    }

    private fun getUnit(value: String):String?{
        var value = "°C"
        if("US" == value || "LR" == value || "MM" == value){
            value="°F"
        }
        return value
    }

    private fun unixTime(timex: Long): String?{
        val date = Date(timex*1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}