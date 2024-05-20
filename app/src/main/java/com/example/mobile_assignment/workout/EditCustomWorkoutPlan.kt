package com.example.mobile_assignment.workout

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentEditCustomWorkoutPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import util.cropToBlob
import util.setImageBlob
import util.toBitmap
import util.toast

class EditCustomWorkoutPlan : Fragment() {
    private lateinit var binding: FragmentEditCustomWorkoutPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()

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
        }

        binding.ivCustomImage.setOnClickListener {
            openFileChooser()
        }

        binding.exercises.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isEdit", true)
            findNavController().navigate(R.id.addExercise, bundle)
        }
        return binding.root
    }

    private fun displayWorkoutPlanDetails() {
        exerciseViewModel.selectedCustomPlan.observe(viewLifecycleOwner) { customPlan ->
            customPlan?.let {
                binding.etPlanName.setText(it.name)
                binding.etTargetedBody.setText(it.targetedBodyPart)
                binding.etRestDuration.setText(it.restDuration)
                it.photo?.let { blob ->
                    binding.ivCustomImage.setImageBitmap(blob.toBitmap())
                }

                // Ensure the selected exercises are updated in the ViewModel
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
    private fun saveCustomPlan() {
        val planName = binding.etPlanName.text.toString()
        val targetedBodyPart = binding.etTargetedBody.text.toString()
        val restDuration = binding.etRestDuration.text.toString()

        val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
        val exerciseIds = selectedExercises.map { it.id }

        if (planName.isEmpty() || targetedBodyPart.isEmpty() || restDuration.isEmpty() || exerciseIds.isEmpty()) {
            toast("Please fill all fields and select at least one exercise")
            return
        }

        val userId = getCurrentUserId()

        val customPlanId = exerciseViewModel.selectedCustomPlan.value?.id ?:
        db.collection("customPlans").document(userId).collection("plans").document().id

        val customPlan = CustomPlan(
            id = customPlanId,
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds,
            photo = exerciseViewModel.selectedImageUri.value?.let { binding.ivCustomImage.cropToBlob(300, 300) }
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                exerciseViewModel.clearSelectedExercises() // Clear selected exercises after saving
                findNavController().navigateUp() // Navigate back
            },
            onFailure = { e ->
                toast("Failed to save custom plan: ${e.message}")
            }
        )
    }
    private fun openFileChooser() {
        getContent.launch("image/*")
    }

    private fun getCurrentUserId(): String {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("userId", "U001") ?: "U001"
    }
}