package com.example.mobile_assignment.workout

data class workoutPlan(
    val time: String,
    val title: String,
    val details: String
)


data class Exercise(
    val workoutID : String,
    val name : String,
    val duration : String
)
