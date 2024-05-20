package com.example.mobile_assignment.workout

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddCustomPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import util.toast


class addCustomPlan : Fragment() {
    private lateinit var binding: FragmentAddCustomPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private lateinit var selectedExerciseAdapter: SelectedExerciseAdapter
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCustomPlanBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()

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
        selectedExerciseAdapter = SelectedExerciseAdapter(exerciseViewModel)

        binding.rvSelectedExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = selectedExerciseAdapter
        }
    }

    private fun setupObservers() {
        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner, { exercises ->
            selectedExerciseAdapter.submitList(exercises)
        })
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

        val userId = getCurrentUserId() // Replace with actual method to get user ID

        val customPlanId = db.collection("customPlans").document(userId).collection("plans").document().id // Generate a new ID
        val customPlan = CustomPlan(
            id = customPlanId,
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                findNavController().navigateUp() // Navigate back
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
}