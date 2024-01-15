package com.example.weatherapplication


data class WeatherData (


  val main : MainData,
  val wind : WindData,
  val weather : List<WeatherInfo>,
  val name: String

) {
  data class MainData(
    val temp :Double,
    val humidity : Int
  )
  data class WindData(
    val speed : Double
  )

  class WeatherInfo(
    val description : String
  )
}