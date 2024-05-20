package com.example.mobile_assignment.workout.Data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream

class ExerciseViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    private val _selectedExercise = MutableLiveData<Exercise>()
    val selectedExercise: LiveData<Exercise> get() = _selectedExercise

    private val _selectedExercises = MutableLiveData<List<Exercise>>()
    val selectedExercises: LiveData<List<Exercise>> get() = _selectedExercises

    private val _addExerciseStatus = MutableLiveData<Boolean>()
    val addExerciseStatus: LiveData<Boolean> get() = _addExerciseStatus

    private val _customPlans = MutableLiveData<List<CustomPlan>>()
    val customPlans: LiveData<List<CustomPlan>> get() = _customPlans

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
    fun addExercise(exercise: Exercise) {
        db.collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .addOnSuccessListener {
                _addExerciseStatus.value = true
            }
            .addOnFailureListener {
                _addExerciseStatus.value = false
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
    fun convertImageToBlob(context: Context, imageUri: Uri, callback: (Blob?) -> Unit) {
        val stream = ByteArrayOutputStream()
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        callback(Blob.fromBytes(byteArray))
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
                callback("E001") // Default ID in case of error
            }
    }
    fun addExercisesToCustomWorkoutPlan(selectedExercises: List<Exercise>) {
        // Your logic to add exercises to a custom workout plan
        // This could involve saving the list to Firestore under a specific user's workout plan collection
        for (exercise in selectedExercises) {
            Log.d("ExerciseViewModel", "Adding exercise ${exercise.name} to custom workout plan")
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
    }

    fun isSelected(exercise: Exercise): Boolean {
        return _selectedExercises.value?.contains(exercise) ?: false
    }
    fun updateSelectedExercises(exercises: List<Exercise>) {
        _selectedExercises.value = exercises
    }
}