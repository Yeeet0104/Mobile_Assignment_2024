package com.example.mobile_assignment.workout.Data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    private val _selectedExercise = MutableLiveData<Exercise?>()
    val selectedExercise: LiveData<Exercise?> get() = _selectedExercise

    private val _selectedExercises = MutableLiveData<List<Exercise>>()
    val selectedExercises: LiveData<List<Exercise>> get() = _selectedExercises

//    private val _addExerciseStatus = MutableLiveData<Boolean>()
//    val addExerciseStatus: LiveData<Boolean> get() = _addExerciseStatus

    private val _customPlans = MutableLiveData<List<CustomPlan>>()
    val customPlans: LiveData<List<CustomPlan>> get() = _customPlans

    private val _selectedCustomPlan = MutableLiveData<CustomPlan>()
    val selectedCustomPlan: LiveData<CustomPlan> get() = _selectedCustomPlan

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri
    private var listener: ListenerRegistration? = null

    init {
        listener = db.collection("exercises").addSnapshotListener { snap, _ ->
            _exercises.value = snap?.toObjects(Exercise::class.java)
        }
    }

    override fun onCleared() {
        listener?.remove()
    }
    fun selectExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
    }
    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }
    fun addExercise(exercise: Exercise) {
        db.collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .addOnSuccessListener {
//                _addExerciseStatus.value = true
            }
            .addOnFailureListener {
//                _addExerciseStatus.value = false
            }
    }
    fun fetchCustomPlans(userId: String) {
        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snap != null && !snap.isEmpty) {
                    _customPlans.value = snap.toObjects(CustomPlan::class.java)
                } else {
                    _customPlans.value = emptyList()
                }
            }
    }
    fun generateCustomId(callback: (String) -> Unit) {
        db.collection("exercises")
            .get()
            .addOnSuccessListener { result ->
                val idList = result.documents.map { it.id }
                var newId = "E001"
                var idNumber = 1
                while (idList.contains(newId)) {
                    idNumber++
                    newId = "E" + String.format("%03d", idNumber)
                }
                callback(newId)
            }
            .addOnFailureListener {
                callback("E001")
            }
    }
    fun addCustomPlan(customPlan: CustomPlan, userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .document(customPlan.id)
            .set(customPlan)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun toggleExerciseSelection(exercise: Exercise) {
        val currentList = _selectedExercises.value?.toMutableList() ?: mutableListOf()
        if (currentList.contains(exercise)) {
            currentList.remove(exercise)
        } else {
            currentList.add(exercise)
        }
        _selectedExercises.value = currentList
        Log.d("ExerciseViewModel", "Toggled exercise selection: ${exercise.name}")
    }

    fun isSelected(exercise: Exercise): Boolean {
        return _selectedExercises.value?.contains(exercise) ?: false
    }
//    fun fetchExercisesByIds(exerciseIds: List<String>): LiveData<List<Exercise>> {
//        val exercisesLiveData = MutableLiveData<List<Exercise>>()
//        if (exerciseIds.isEmpty()) {
//            exercisesLiveData.value = emptyList()
//            return exercisesLiveData
//        }
//
//        Log.d("ExerciseViewModel", "Fetching exercises with IDs: ${exerciseIds.joinToString()}")
//
//        db.collection("exercises")
//            .whereIn(FieldPath.documentId(), exerciseIds)
//            .get()
//            .addOnSuccessListener { documents ->
//                if (!documents.isEmpty) {
//                    val exercises = documents.toObjects(Exercise::class.java)
//                    Log.d("ExerciseViewModel", "Fetched exercises: ${exercises.joinToString { it.name }}")
//                    exercisesLiveData.value = exercises
//                } else {
//                    Log.d("ExerciseViewModel", "No exercises found.")
//                    exercisesLiveData.value = emptyList()
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("ExerciseViewModel", "Failed to fetch exercises", e)
//                exercisesLiveData.value = emptyList()
//            }
//
//        return exercisesLiveData
//    }
    fun selectCustomPlan(customPlan: CustomPlan) {
        _selectedCustomPlan.value = customPlan
    }

    fun updateSelectedExercises(exercises: List<Exercise>) {
        _selectedExercises.value = exercises
    }

    fun clearSelectedExercises() {
        _selectedExercises.value = emptyList()
    }

    fun deleteExercise(exercise: Exercise) {
        db.collection("exercises").document(exercise.id).delete()
            .addOnSuccessListener {
                Log.d("ExerciseViewModel", "Exercise deleted successfully")
                removeExerciseFromCustomPlans(exercise.id)
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseViewModel", "Failed to delete exercise", e)
            }
    }

    private fun removeExerciseFromCustomPlans(exerciseId: String) {
        val userId = getCurrentUserId() // Implement this method to get the current user ID

        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val batch = db.batch()
                    for (document in snapshot.documents) {
                        val customPlan = document.toObject(CustomPlan::class.java)
                        customPlan?.let {
                            if (it.exerciseIds.contains(exerciseId)) {
                                it.exerciseIds = it.exerciseIds.filter { id -> id != exerciseId }
                                val docRef = db.collection("customPlans")
                                    .document(userId)
                                    .collection("plans")
                                    .document(it.id)
                                batch.set(docRef, it)
                            }
                        }
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("ExerciseViewModel", "Custom plans updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ExerciseViewModel", "Failed to update custom plans", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseViewModel", "Failed to get custom plans", e)
            }
    }
    fun clearSelectedExercise() {
        _selectedExercise.value = null
    }
    private fun getCurrentUserId(): String {
        val sharedPref = getApplication<Application>().getSharedPreferences("userID", Context.MODE_PRIVATE)
        return sharedPref.getString("userId", "U001") ?: "U001"
    }

    fun updateSelectedExercisesByIds(exerciseIds: List<String>) {
        db.collection("exercises")
            .whereIn(FieldPath.documentId(), exerciseIds)
            .get()
            .addOnSuccessListener { documents ->
                val exercises = documents.toObjects(Exercise::class.java)
                _selectedExercises.value = exercises
                Log.d("ExerciseViewModel", "Updated selected exercises by IDs")
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseViewModel", "Failed to update selected exercises by IDs", e)
            }
    }
    fun updateCustomPlan(customPlan: CustomPlan, userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .document(customPlan.id)
            .set(customPlan)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    fun refreshSelectedCustomPlan(customPlanId: String, userId: String) {
        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .document(customPlanId)
            .get()
            .addOnSuccessListener { document ->
                val customPlan = document.toObject(CustomPlan::class.java)
                _selectedCustomPlan.value = customPlan!!
                customPlan?.exerciseIds?.let { updateSelectedExercisesByIds(it) }
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseViewModel", "Failed to refresh custom plan", e)
            }
    }
    fun deleteCustomPlan(planId: String, userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("customPlans")
            .document(userId)
            .collection("plans")
            .document(planId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

}