package com.accu.cityweather.forecast.repository

import com.accu.cityweather.api.ApiDailyForecast
import com.accu.cityweather.api.WeatherApi
import java.util.Date

class ForeCastRepositoryImpl(
    private val weatherApi: WeatherApi,
    private val degreeToCardinalConverter: DegreeToCardinalConverter
) : ForecastRepository {
    override suspend fun getDaysForecast(
        city: String, units: String, daysCount: Int
    ): List<DayForecast> {
        val response = weatherApi.dailyForecast(query = city, units = units, count = daysCount)
        return if (response.list.isNullOrEmpty()) {
            emptyList()
        } else {
            response.list.map { it.toDayForecast(degreeToCardinalConverter) }
        }
    }

    private fun ApiDailyForecast.toDayForecast(degreeToCardinalConverter: DegreeToCardinalConverter) =
        DayForecast(
            day = Date(dt.toMilliSeconds()),
            sunrise = Date(sunrise.toMilliSeconds()),
            sunset = Date(sunset.toMilliSeconds()),
            maxTemperature = temp.max.toInt(),
            minTemperature = temp.min.toInt(),
            temperature = with(temp) {
                DayTemperature(
                    day = day.toInt(),
                    night = night.toInt(),
                    eve = eve.toInt(),
                    morning = morn.toInt()
                )
            },
            feelsLikeTemperature = with(feels_like) {
                DayTemperature(
                    day = day.toInt(),
                    night = night.toInt(),
                    eve = eve.toInt(),
                    morning = morn.toInt()
                )
            },
            pressure = pressure,
            humidity = humidity,
            wind = if (speed != null && deg != null) Wind(
                speed = speed, direction = degreeToCardinalConverter.convert(deg)
            ) else null,
            rain = Rain(probability = pop.toInt(), size = rain ?: 0.0)
        )

    private fun Long.toMilliSeconds() = this * 1000L
}
