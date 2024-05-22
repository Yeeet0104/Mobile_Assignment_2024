package com.example.mobile_assignment.workout.Data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class RunningSessionViewModel (application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val _runningSessions = MutableLiveData<List<RunningSession>>()
    val runningSessions: LiveData<List<RunningSession>> = _runningSessions

    fun fetchRunningSessions(userId: String) {
        db.collection("customPlans")
            .document(userId)
            .collection("runningSessions")
            .get()
            .addOnSuccessListener { documents ->
                val runningSessions = documents.map { document ->
                    RunningSession(
                        document.getLong("startTime") ?: 0L,
                        document.getLong("endTime") ?: 0L,
                        document.getDouble("distance")!!,
                        document.getDouble("averageSpeed")!!
                    )
                }
                _runningSessions.value = runningSessions
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
}