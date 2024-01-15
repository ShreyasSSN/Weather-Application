package com.example.weatherapplication

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapplication.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val apiKey = "<your api key>"
    lateinit var retrofit : Retrofit
    lateinit var retrofitAPI : RetrofitAPI
    var cityName : String = ""
    lateinit var call : Call<WeatherData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        mainBinding.buttonWeatherCheck.setOnClickListener {
            showWeather(locationManager)
        }
    }

    fun showWeather(locationManager : LocationManager?){

        getLocation(locationManager)

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitAPI = retrofit.create(RetrofitAPI::class.java)
    }

    private fun getLocation(locationManager: LocationManager?) {
        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else {
            try {
                // Request location updates
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0L, 0f,
                    locationListener)
            } catch (ex: SecurityException) {
                Log.d("Location error", "Security Exception, no location available")
            }
        }
    }

    val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {


            if (mainBinding.editTextCity.text.toString().isNotEmpty()){
                cityName = mainBinding.editTextCity.text.toString()
                call= retrofitAPI.getWeatherData(
                    city = cityName.trim(), latitude = null,
                    longitude = null,
                    apiKey = apiKey)
            }
            else{
                call = retrofitAPI.getWeatherData(
                    city = null, latitude = location.latitude,
                    longitude = location.longitude,
                    apiKey = apiKey)
            }

            call.enqueue(object : Callback<WeatherData>{

                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if(response.code() != 200) {
                        mainBinding.textViewWeather.text = call.request().url().toString()
                        Toast.makeText(applicationContext, "Error code: ${response.code()}",
                            Toast.LENGTH_LONG).show()
                    }
                    else{
                        val result = response.body()
                        showData(result)
                    }
                }
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Toast.makeText(applicationContext, t.localizedMessage, Toast.LENGTH_LONG).show()
                }
            })
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun showData(result: WeatherData?) {
        var temp : Double = result!!.main.temp - 273.15
        temp = String.format("%.2f", temp).toDouble()
        val humidity : Int = result.main.humidity
        val speed : Double = String.format("%.2f", result.wind.speed).toDouble()
        val city : String = result.name
        val weatherList = result.weather
        val description = weatherList[0].description

        showDialogBox(temp, humidity, speed, city, description)
    }

    fun showDialogBox(temp : Double, humidity : Int, speed : Double, city : String, description : String){
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Weather Data")
            .setIcon(R.drawable.weather_icon)
            .setMessage("City Name : ${city}\nDescription : ${description}\nTemperature : ${temp} ºC\n" +
                    "Humidity : ${humidity}\nWind Speed : ${speed} m/s")
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, _ ->
                mainBinding.editTextCity.setText("")
            })
        alertDialog.create().show()
    }
}