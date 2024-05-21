package com.example.mobile_assignment.workout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutSharedViewModel  : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _selectedWorkoutPlan = MutableLiveData<CustomPlan?>()
    val selectedWorkoutPlan: LiveData<CustomPlan?> get() = _selectedWorkoutPlan

    private val _currentExerciseIndex = MutableLiveData<Int>()
    val currentExerciseIndex: LiveData<Int> get() = _currentExerciseIndex

    private val _workoutProgress = MutableLiveData<Int>()
    val workoutProgress: LiveData<Int> get() = _workoutProgress
    fun selectWorkoutPlan(plan: CustomPlan) {
        _selectedWorkoutPlan.value = plan
        _currentExerciseIndex.value = 0
        _workoutProgress.value = 0
    }
    init {
        _currentExerciseIndex.value = 0
        _workoutProgress.value = 0
    }

    fun nextExercise() {
        _currentExerciseIndex.value = _currentExerciseIndex.value?.plus(1)
    }
    fun addProgress(userId: String) {
        _workoutProgress.value = (_workoutProgress.value ?: 0) + 1
        _selectedWorkoutPlan.value?.progress = _workoutProgress.value ?: 0
        updateProgressInFirestore(userId)
    }
    fun isLastExercise(): Boolean {
        Log.d("WorkoutSharedViewModel", "isLastExercise: ${_currentExerciseIndex.value} == ${_selectedWorkoutPlan.value?.exerciseIds?.size?.minus(1)}")
        Log.d("WorkoutSharedViewModel", "isLastExercisecurr: ${_currentExerciseIndex.value}")
        Log.d("WorkoutSharedViewModel", "isLastExerciseExisiting: ${_selectedWorkoutPlan.value?.exerciseIds?.size?.minus(0)}")
        return _currentExerciseIndex.value == _selectedWorkoutPlan.value?.exerciseIds?.size?.minus(0)
    }
    fun resetProgress(userId: String) {
        _workoutProgress.value = 0
        _selectedWorkoutPlan.value?.progress = 0
        _selectedWorkoutPlan.value?.status = 0
        updateProgressInFirestore(userId)
    }
    fun markAsCompleted(userId: String) {
        _selectedWorkoutPlan.value?.status = 1
        updateProgressInFirestore(userId)
    }
    private fun updateProgressInFirestore(userId: String) {
        val workoutPlan = _selectedWorkoutPlan.value ?: return
        val docRef = db.collection("customPlans").document(userId).collection("plans").document(workoutPlan.id)
        docRef.update(mapOf(
            "progress" to workoutPlan.progress,
            "status" to workoutPlan.status
        )).addOnSuccessListener {
            Log.d("WorkoutSharedViewModel", "Progress successfully updated in Firestore")
        }.addOnFailureListener { e ->
            Log.e("WorkoutSharedViewModel", "Error updating progress in Firestore", e)
        }
    }
}