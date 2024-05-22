package com.example.mobile_assignment.workout.Ui

import Login.data.AuthVM
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentEditCustomWorkoutPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.example.mobile_assignment.workout.MultiSelectSpinner
import com.example.mobile_assignment.workout.SelectedExerciseAdapter
import com.google.firebase.firestore.FirebaseFirestore
import util.cropToBlob
import util.toBitmap
import util.toUri
import util.toast
import java.util.Calendar

class EditCustomWorkoutPlan : Fragment() {
    private lateinit var binding: FragmentEditCustomWorkoutPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private val auth: AuthVM by activityViewModels()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditCustomWorkoutPlanBinding.inflate(inflater, container, false)

        setupRecyclerView()
        displayWorkoutPlanDetails()

        binding.btnDone.setOnClickListener {
            saveCustomPlan()
            exerciseViewModel.clearSelectedImageUri()
        }

        binding.ivCustomImage.setOnClickListener {
            openFileChooser()
        }

        binding.exercises.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isEdit", true)
            findNavController().navigate(R.id.addExercise, bundle)
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

        exerciseViewModel.selectedDays.observe(viewLifecycleOwner) { days ->
            binding.etDaysSelected.text = days.joinToString(", ")
        }

        exerciseViewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.etTimeSelected.text = time
        }

        // Observe selectedImageUri to restore image
        exerciseViewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.ivCustomImage.setImageURI(it)
            } ?: run {
                binding.ivCustomImage.setImageResource(R.drawable.outline_add_circle_outline_24)
            }
        }

        // Restore image when view is created
        exerciseViewModel.selectedImageUri.value?.let {
            binding.ivCustomImage.setImageURI(it)
        }


        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        clearAllSelected()
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            clearAllSelected()
            findNavController().navigateUp()
        }
        return binding.root
    }
    private fun clearAllSelected() {
        exerciseViewModel.clearSelectedExercises() // Clear selected exercises
        exerciseViewModel.clearSelectedDaysAndTime() // Clear selected exercises
        exerciseViewModel.clearSelectedImageUri() // Clear selected exercises
    }
    private fun displayWorkoutPlanDetails() {
        exerciseViewModel.selectedCustomPlan.observe(viewLifecycleOwner) { customPlan ->
            customPlan?.let {
                binding.etPlanName.setText(it.name)
                binding.etTargetedBody.setText(it.targetedBodyPart)
                binding.etRestDuration.setText(it.restDuration)
                binding.etDaysSelected.text = it.daysOfWeek.joinToString(", ")
                binding.etTimeSelected.text = it.timeOfDay
                exerciseViewModel.setSelectedDays(it.daysOfWeek)
                exerciseViewModel.setSelectedTime(it.timeOfDay)

                it.photo?.let { blob ->
                    binding.ivCustomImage.setImageBitmap(blob.toBitmap())
                    exerciseViewModel.setSelectedImageUri(blob.toUri(requireContext()))
                }

                if (exerciseViewModel.selectedExercises.value.isNullOrEmpty()) {
                    exerciseViewModel.updateSelectedExercisesByIds(it.exerciseIds)
                }
            }
        }
    }
    private fun setupRecyclerView() {
        val adapter = SelectedExerciseAdapter(exerciseViewModel)
        binding.rvEditSelectedExercises.layoutManager = LinearLayoutManager(context)
        binding.rvEditSelectedExercises.adapter = adapter

        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            Log.d("EditCustomWorkoutPlan", "Selected exercises: $exercises")
            updateRecyclerView(exercises)
        }
    }

    private fun updateRecyclerView(exercises: List<Exercise>) {
        val adapter = binding.rvEditSelectedExercises.adapter as SelectedExerciseAdapter
        if (exercises.isEmpty()) {
            binding.rvEditSelectedExercises.visibility = View.GONE
            binding.tvNoExercises.visibility = View.VISIBLE
        } else {
            binding.rvEditSelectedExercises.visibility = View.VISIBLE
            binding.tvNoExercises.visibility = View.GONE
            adapter.submitList(exercises)
        }
    }


    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivCustomImage.setImageURI(it)
            exerciseViewModel.setSelectedImageUri(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveCustomPlan() {
        val planName = binding.etPlanName.text.toString().trim()
        val targetedBodyPart = binding.etTargetedBody.text.toString().trim()
        val restDuration = binding.etRestDuration.text.toString().trim()
        val daysOfWeek = exerciseViewModel.selectedDays.value ?: emptyList()
        val timeOfDay = exerciseViewModel.selectedTime.value ?: ""

        val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
        val exerciseIds = selectedExercises.map { it.id }

        if (planName.isEmpty() || targetedBodyPart.isEmpty() || restDuration.isEmpty() || exerciseIds.isEmpty() || daysOfWeek.isEmpty() || timeOfDay.isEmpty()) {
            toast("Please fill all fields and select at least one exercise, day, and time")
            return
        }

        val userId = getCurrentUserId()

        val customPlanId = exerciseViewModel.selectedCustomPlan.value?.id ?: db.collection("customPlans").document(userId).collection("plans").document().id

        val customPlan = CustomPlan(
            id = customPlanId,
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds,
            photo = exerciseViewModel.selectedImageUri.value?.let { binding.ivCustomImage.cropToBlob(300, 300) },
            daysOfWeek = daysOfWeek,
            timeOfDay = timeOfDay
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                exerciseViewModel.clearSelectedExercises()
                exerciseViewModel.clearSelectedDaysAndTime()
                exerciseViewModel.clearSelectedImageUri()
                findNavController().navigateUp()
            },
            onFailure = { e ->
                toast("Failed to save custom plan: ${e.message}")
            }
        )
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
    private fun openFileChooser() {
        getContent.launch("image/*")
    }

    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }
}