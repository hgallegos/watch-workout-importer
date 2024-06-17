package org.hans.service

import com.synerset.unitility.unitsystem.common.Distance
import com.synerset.unitility.unitsystem.util.PhysicalQuantityParsingFactory
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.serialization.json.Json
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hans.model.Workout
import org.hans.model.trainsasone.Intensity
import org.hans.model.trainsasone.Step
import org.hans.model.trainsasone.TrainsAsOneWorkout
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

@ApplicationScoped
class TrainAsOneWorkoutService(
    @ConfigProperty(name = "trainasone.email")
    private val email: String,
    @ConfigProperty(name = "trainasone.password")
    private val password: String
) {


    val cookies: MutableMap<String, String> = this.login(email, password)
    val parsingFactory: PhysicalQuantityParsingFactory = PhysicalQuantityParsingFactory.getDefaultParsingFactory()

    fun getNextWorkouts(): List<TrainsAsOneWorkout> {
        Log.info("Getting next TrainAsOne Workouts")
        val doc = Jsoup.connect("https://beta.trainasone.com/calendarView").cookies(this.cookies).get()
        val found = false;
        val workouts: MutableList<TrainsAsOneWorkout> = mutableListOf()
        val upcoming = doc.select(".today, .future")
        upcoming.stream().limit(7).forEach {
            if (it.select(".workout").isNotEmpty()) {
                val date =
                    MonthDay.parse(
                        it.selectFirst(".title")?.text() ?: throw Exception("Workout date is not correct"),
                        DateTimeFormatter.ofPattern("MMM d")
                    )
                val workoutUrl = it.selectFirst(".workout a")?.absUrl("href")
                if (workoutUrl != null) {
                    workouts.add(this.getWorkout(workoutUrl, date))
                }
            }
        }

        return workouts

    }

    private fun login(email: String, password: String): MutableMap<String, String> {
        val res = Jsoup.connect("https://beta.trainasone.com/login").data("email", email, "password", password)
            .followRedirects(false)
            .method(Connection.Method.POST).execute()
        return res.cookies();
    }

    private fun getWorkout(workoutUrl: String, date: MonthDay): TrainsAsOneWorkout {
        val workoutJsonUrl =
            workoutUrl.replace("plannedWorkout?", "plannedWorkoutDownload?sourceFormat=GARMIN_TRAINING&")
        val workoutDoc = Jsoup.connect(workoutJsonUrl).cookies(this.cookies).get()
        val workoutBase = Jsoup.connect(workoutUrl).cookies(this.cookies).get()

        workoutBase.selectFirst(".detail>span")?.let { parseDuration(it.text()) }
        workoutBase.selectFirst(".detail")?.let { parseDistance(it.text()) }
        return Json {ignoreUnknownKeys = true}.decodeFromString<TrainsAsOneWorkout>(workoutDoc.text())
    }

    private fun parseDuration(durationString: String): Duration {
        val regex =
            """(?=\d+ (hour|minute|second))((?<hours>\d+) hours?)?[, ]*((?<minutes>\d+) minutes?)?[, ]*((?<seconds>\d+) seconds?)?""".toRegex()
        val match =
            regex.find(durationString) ?: throw IllegalArgumentException("No duration found in text: $durationString")
        val durationBuilder = StringBuilder()

        mapOf("hours" to "h", "minutes" to "m", "seconds" to "s").forEach { (k, v) ->
            if (match.groups[k]?.value != null) {
                durationBuilder.append(match.groups[k]?.value).append(v)
            }
        }

        return if (durationBuilder.isBlank()) Duration.ZERO else Duration.parse(durationBuilder.toString())
    }

    private fun parseDistance(distanceString: String): Distance {
        val regex = """\(~?([\d.]+ (k?m))\)""".toRegex()
        val match =
            regex.find(distanceString) ?: throw IllegalArgumentException("No distance found in text: $distanceString")

        val distance = this.parsingFactory.parse(Distance::class.java, match.groups[1]?.value)

        return distance!!
    }

}