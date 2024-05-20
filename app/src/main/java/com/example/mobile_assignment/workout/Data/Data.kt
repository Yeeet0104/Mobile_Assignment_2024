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
    val photo: Blob? = null // Add this field
)
data class Exercise(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var duration: String = "",
    var reps: String = "",
    var youtubeId: String = "",
    var photo: Blob? = null,
    var steps: List<String> = listOf()
){
    @get:Exclude
    var isSelected: Boolean = false

    override fun toString() = name
}