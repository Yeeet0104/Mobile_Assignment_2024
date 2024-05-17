package com.example.mobile_assignment.workout.Data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class CustomPlan(
    @DocumentId
    val id: String = "", // Document ID field
    val userId: String = "", // User ID field
    val name: String = "",
    val targetedBodyPart: String = "",
    val restDuration: String = "",
    val exerciseIds: List<String> = listOf()
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