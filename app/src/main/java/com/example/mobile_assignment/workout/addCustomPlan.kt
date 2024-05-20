package com.example.mobile_assignment.workout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
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
        val planName = binding.etPlanName.text.toString()
        val targetedBodyPart = binding.etTargetedBody.text.toString()
        val restDuration = binding.etRestDuration.text.toString()

        val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
        val exerciseIds = selectedExercises.map { it.id }

        if (planName.isEmpty() || targetedBodyPart.isEmpty() || restDuration.isEmpty() || exerciseIds.isEmpty()) {
            toast("Please fill all fields and select at least one exercise")
            return
        }

        val userId = getCurrentUserId() // Replace with actual method to get user ID

        // Check if an image is selected
        val imageBlob = binding.ivCustomImage.cropToBlob(300, 300)
        if (imageBlob != null ) {
            saveCustomPlan(planName, targetedBodyPart, restDuration, exerciseIds, imageBlob, userId)
        } else {
            // No image selected, save without image
            saveCustomPlan(planName, targetedBodyPart, restDuration, exerciseIds, null, userId)
        }
    }

    private fun saveCustomPlan(
        planName: String,
        targetedBodyPart: String,
        restDuration: String,
        exerciseIds: List<String>,
        imageBlob: Blob?,
        userId: String
    ) {
        val customPlanId = db.collection("customPlans").document(userId).collection("plans").document().id // Generate a new ID
        val customPlan = CustomPlan(
            id = customPlanId,
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds,
            photo = imageBlob
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                // Clear input fields and reset selected exercises
                clearInputs()
                exerciseViewModel.clearSelectedExercises()
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
        binding.ivCustomImage.setImageResource(R.drawable.outline_add_circle_outline_24) // Set to your default image
    }
}