package org.hans.rest.json

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.hans.model.trainsasone.TrainsAsOneWorkout
import org.hans.service.TrainAsOneWorkoutService
import org.jsoup.select.Elements

@Path("/trainasone/workouts")
class TrainAsOneWorkoutResource(private val trainAsOneWorkoutService: TrainAsOneWorkoutService) {

    @GET()
    fun getAllTrainAsOneWorkouts(): List<TrainsAsOneWorkout> {
        return this.trainAsOneWorkoutService.getNextWorkouts()
    }
}