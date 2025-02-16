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
    private lateinit var themeSpinner: Spinner
    private var selectedCity = "Irkutsk"
    private var isDarkTheme = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fm = supportFragmentManager
        citySpinner = findViewById(R.id.citySpinner)
        themeSpinner = findViewById(R.id.themeSpinner)
        val cities = listOf("Irkutsk", "Sochi", "Moscow", "Novosibirsk", "Habarovsk")
        val themes = listOf("Темная тема", "Светлая тема")
        citySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)
        themeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, themes)
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCity = citySpinner.selectedItem.toString()
                loadWeather()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isDarkTheme = position == 0
                applyTheme()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        applyTheme()
        findViewById<Button>(R.id.btnBrief).setOnClickListener {
            fm.beginTransaction().replace(R.id.container_fragm, briefWeatherFragment).commit()
            loadWeather()
        }
        findViewById<Button>(R.id.btnDetailed).setOnClickListener {
            fm.beginTransaction().replace(R.id.container_fragm, detailedWeatherFragment).commit()
            loadWeather()
        }
        if (fm.findFragmentById(R.id.container_fragm) == null) {
            fm.beginTransaction().add(R.id.container_fragm, briefWeatherFragment).commit()
        }
        loadWeather()
    }

    private fun applyTheme() {
        val rootLayout = findViewById<View>(R.id.rootLayout)
        val btnBrief = findViewById<Button>(R.id.btnBrief)
        val btnDetailed = findViewById<Button>(R.id.btnDetailed)

        if (isDarkTheme) {
            rootLayout.setBackgroundResource(R.color.dark_theme_background)
            btnBrief.setBackgroundResource(R.color.dark_theme_button)
            btnDetailed.setBackgroundResource(R.color.dark_theme_button)
        } else {
            rootLayout.setBackgroundResource(R.color.light_theme_background)
            btnBrief.setBackgroundResource(R.color.light_theme_button)
            btnDetailed.setBackgroundResource(R.color.light_theme_button)
        }

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
        if (briefWeatherFragment.isAdded) {
            briefWeatherFragment.updateWeather(weatherData)
        }
        if (detailedWeatherFragment.isAdded) {
            detailedWeatherFragment.updateWeather(weatherData)
        }
    }
}
