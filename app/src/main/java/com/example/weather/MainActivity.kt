package com.example.weather
import com.google.gson.Gson
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var fm: FragmentManager
    private val briefWeatherFragment = BriefWeatherFragment()
    private val detailedWeatherFragment = DetailedWeatherFragment()
    private lateinit var citySpinner: Spinner
    private var selectedCity = "Irkutsk"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fm = supportFragmentManager
        citySpinner = findViewById(R.id.citySpinner)
        val cities = listOf("Irkutsk", "Sochi", "Moscow", "Novosibirsk", "Habarovsk")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)
        citySpinner.adapter = adapter
        if (fm.findFragmentById(R.id.container_fragm) == null) {
            fm.beginTransaction().add(R.id.container_fragm, briefWeatherFragment).commit()
        }
        findViewById<Button>(R.id.btnBrief).setOnClickListener {
            loadWeather()
            fm.beginTransaction().replace(R.id.container_fragm, briefWeatherFragment).commit()
        }

        findViewById<Button>(R.id.btnDetailed).setOnClickListener {
            loadWeather()
            fm.beginTransaction().replace(R.id.container_fragm, detailedWeatherFragment).commit()
        }
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCity = citySpinner.selectedItem.toString()
                loadWeather()
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
        loadWeather()
    }
    private fun loadWeather() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val weatherData = fetchWeatherData()
                runOnUiThread {
                    updateFragments(weatherData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun fetchWeatherData(): WeatherResponse {
        val API_KEY = "4dac63af9f28277443bf2d581d6cc50b"
        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=$selectedCity&appid=$API_KEY&units=metric&lang=ru"
        val stream = URL(weatherURL).openStream()
        val reader = InputStreamReader(stream)
        return Gson().fromJson(reader, WeatherResponse::class.java)
    }
    private fun updateFragments(weatherData: WeatherResponse) {
        if (briefWeatherFragment.isAdded) briefWeatherFragment.updateWeather(weatherData)
        if (detailedWeatherFragment.isAdded) detailedWeatherFragment.updateWeather(weatherData)
    }
}
