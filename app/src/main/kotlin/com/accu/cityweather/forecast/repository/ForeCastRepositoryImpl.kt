package com.accu.cityweather.forecast.repository

import com.accu.cityweather.api.ApiDailyForecast
import com.accu.cityweather.api.ApiWeather
import com.accu.cityweather.api.WeatherApi
import kotlin.math.roundToInt

class ForeCastRepositoryImpl(
    private val weatherApi: WeatherApi,
    private val degreeToCardinalConverter: DegreeToCardinalConverter,
    private val dayDateFormatter: DayDateFormatter,
    private val iconUrlResolver: IconUrlResolver,
) : ForecastRepository {
    override suspend fun getCurrentForecast(
        city: String,
        units: String,
        count: Int
    ): CurrentForecast {
        val response = weatherApi.currentForecast(city, units, count)
        return response.list?.firstOrNull()?.let {
            val weather = it.weather.firstOrNull()
            return@let CurrentForecast(
                temperature = it.main.temp.toInt(),
                feelsLike = it.main.feels_like.toInt(),
                title = weather?.main ?: "-",
                description = weather?.description ?: "-",
                icon = iconUrlResolver.resolve(weather?.icon ?: ""),
                condition = Condition(
                    probability = it.pop.toPercentage(),
                    size = it.rain?.h ?: it.snow?.h ?: 0.0
                ),
            )
        } ?: CurrentForecast()
    }

    override suspend fun getDaysForecast(
        city: String,
        units: String,
        daysCount: Int
    ): List<DayForecast> {
        val response = weatherApi.dailyForecast(query = city, units = units, count = daysCount)
        return if (response.list.isEmpty()) {
            emptyList()
        } else {
            response.list.map { it.toDayForecast(degreeToCardinalConverter) }
        }
    }

    private fun ApiDailyForecast.toDayForecast(degreeToCardinalConverter: DegreeToCardinalConverter) =
        DayForecast(
            day = dayDateFormatter.format(dt),
            description = weather.getDescription(),
            sunrise = dayDateFormatter.formatTime(sunrise),
            sunset = dayDateFormatter.formatTime(sunset),
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
            condition = Condition(probability = pop.toPercentage(), size = rain ?: snow ?: 0.0),
            iconUrl = iconUrlResolver.resolve(weather.getIconUrl())
        )

    private fun List<ApiWeather>.getDescription(): DayDescription {
        val first = firstOrNull()
        return DayDescription(main = first?.main ?: "-", description = first?.description ?: "-")
    }

    private fun List<ApiWeather>.getIconUrl(): String {
        val first = firstOrNull()
        return first?.icon ?: ""
    }

    private fun Double.toPercentage(): Int = (this * 100).roundToInt()
}
