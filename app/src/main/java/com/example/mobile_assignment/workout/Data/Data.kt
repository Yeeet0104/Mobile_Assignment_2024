package com.example.mobile_assignment.workout.Data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class CustomPlan(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val targetedBodyPart: String = "",
    val restDuration: String = "",
    var exerciseIds: List<String> = listOf(),
    val photo: Blob? = null, // Add this field
    val daysOfWeek: List<String> = listOf(), // New field for days of the week
    val timeOfDay: String = "",// New field for time of day
    var status: Int = 0, // 0 for not completed, 1 for completed
    var progress: Int = 0 // New field for progress
)
data class Exercise(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var duration: Int = 0,
    var reps: Int = 0,
    var youtubeId: String = "",
    var photo: Blob? = null,
    var steps: List<String> = listOf(),
    var caloriesBurn : Int = 0
){
    @get:Exclude
    var isSelected: Boolean = false

    override fun toString() = name
}