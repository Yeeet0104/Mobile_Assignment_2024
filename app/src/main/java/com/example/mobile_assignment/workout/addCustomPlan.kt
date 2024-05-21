package com.example.mobile_assignment.workout

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddCustomPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import util.cropToBlob
import util.toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class addCustomPlan : Fragment() {
    private lateinit var binding: FragmentAddCustomPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    private val db = FirebaseFirestore.getInstance()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCustomPlanBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupImagePicker()
        restoreImage()

        binding.exercises.setOnClickListener {
            findNavController().navigate(R.id.addExercise)
        }
        // Set up click listener for the exercises section
        binding.btnDone.setOnClickListener {
            saveCustomPlan()
            clearInputs()
            toast("Custom Plan Created Successfully!")
        }

        binding.btnSelectDays.setOnClickListener {
            showDaysOfWeekDialog { selectedDays ->
                exerciseViewModel.setSelectedDays(selectedDays)
                binding.etDaysSelected.text = selectedDays.joinToString(", ")

            }
        }


        binding.btnSelectTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                exerciseViewModel.setSelectedTime(time)
                binding.etTimeSelected.text = time
            }
        }
        return binding.root
    }


    private fun setupRecyclerView() {
        val adapter = SelectedExerciseAdapter(exerciseViewModel)
        binding.rvSelectedExercises.layoutManager = LinearLayoutManager(context)
        binding.rvSelectedExercises.adapter = adapter

        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner, { exercises ->
            adapter.submitList(exercises)
            binding.tvNoExercises.visibility = if (exercises.isEmpty()) View.VISIBLE else View.GONE
            Log.d("addCustomPlan", "Selected exercises: $exercises")
            Log.d("addCustomPlan", "Selected exercises: $exercises.isEmpty()")
        })
    }

    private fun setupObservers() {
        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner, { exercises ->
            (binding.rvSelectedExercises.adapter as SelectedExerciseAdapter).submitList(exercises)
            binding.tvNoExercises.visibility = if (exercises.isEmpty()) View.VISIBLE else View.GONE
        })

        exerciseViewModel.selectedDays.observe(viewLifecycleOwner) { days ->
            binding.etDaysSelected.text = days.joinToString(", ")
        }

        exerciseViewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.etTimeSelected.text = time
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivCustomImage.setImageURI(it)
            exerciseViewModel.setSelectedImageUri(it)
        }
    }

    private fun selectImage() {
        getContent.launch("image/*")
    }

    private fun setupImagePicker() {
        binding.ivCustomImage.setOnClickListener {
            selectImage()
        }
    }
    private fun restoreImage() {
        exerciseViewModel.selectedImageUri.value?.let {
            binding.ivCustomImage.setImageURI(it)
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveCustomPlan() {
        val planName = binding.etPlanName.text.toString().trim()
        val targetedBodyPart = binding.etTargetedBody.text.toString().trim()
        val restDuration = binding.etRestDuration.text.toString().trim()
        val daysOfWeek = binding.etDaysSelected.text.toString().split(", ").filter { it.isNotEmpty() }
        val timeOfDay = binding.etTimeSelected.text.toString().trim()

        val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
        val exerciseIds = selectedExercises.map { it.id }

        if (planName.isEmpty() || targetedBodyPart.isEmpty() || restDuration.isEmpty() || exerciseIds.isEmpty() || daysOfWeek.isEmpty() || timeOfDay.isEmpty()) {
            toast("Please fill all fields and select at least one exercise, day, and time")
            return
        }

        val userId = getCurrentUserId()

        val imageBlob = binding.ivCustomImage.cropToBlob(300, 300)

        // Check for duplicate plan name
        checkDuplicatePlanName(planName, userId) { isDuplicate ->
            if (isDuplicate) {
                toast("A workout plan with this name already exists. Please choose a different name.")
            } else {
                // No duplicate found, proceed to save the custom plan
                saveCustomPlanToDb(planName, targetedBodyPart, restDuration, exerciseIds, imageBlob, daysOfWeek, timeOfDay, userId)
            }
        }
    }

    private fun checkDuplicatePlanName(planName: String, userId: String, callback: (Boolean) -> Unit) {
        db.collection("customPlans").document(userId).collection("plans").document(planName).get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                toast("Error checking for duplicate plan name: ${e.message}")
            }
    }
    private fun saveCustomPlanToDb(
        planName: String,
        targetedBodyPart: String,
        restDuration: String,
        exerciseIds: List<String>,
        imageBlob: Blob?,
        daysOfWeek: List<String>,
        timeOfDay: String,
        userId: String
    ) {
        val customPlan = CustomPlan(
            id = planName, // Use the plan name as the document ID
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds,
            photo = imageBlob,
            daysOfWeek = daysOfWeek,
            timeOfDay = timeOfDay
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                clearInputs()
//                scheduleWorkoutNotification(requireContext(), customPlan)
                findNavController().navigateUp()
            },
            onFailure = { e ->
                toast("Failed to save custom plan: ${e.message}")
            }
        )
    }
    private fun getCurrentUserId(): String {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("userId", "U001") ?: "U001"
    }
    private fun clearInputs() {
        binding.etPlanName.text.clear()
        binding.etTargetedBody.text.clear()
        binding.etRestDuration.text.clear()
        binding.etDaysSelected.text = ""
        binding.etTimeSelected.text = ""
        exerciseViewModel.clearSelectedImageUri()
        exerciseViewModel.clearSelectedExercises()
        exerciseViewModel.clearSelectedDaysAndTime()
        binding.ivCustomImage.setImageResource(R.drawable.outline_add_circle_outline_24)
    }

    private fun showTimePickerDialog(onTimeSet: (Int, Int) -> Unit) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun showDaysOfWeekDialog(onDaysSelected: (List<String>) -> Unit) {
        val daysOfWeek = MultiSelectSpinner.daysOfWeek
        val currentSelections = exerciseViewModel.selectedDays.value ?: emptyList()
        val selectedDays = BooleanArray(daysOfWeek.size) { index ->
            daysOfWeek[index] in currentSelections
        }

        AlertDialog.Builder(context)
            .setTitle("Select Days of the Week")
            .setMultiChoiceItems(daysOfWeek.toTypedArray(), selectedDays) { _, which, isChecked ->
                selectedDays[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selectedList = daysOfWeek.filterIndexed { index, _ -> selectedDays[index] }
                onDaysSelected(selectedList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}