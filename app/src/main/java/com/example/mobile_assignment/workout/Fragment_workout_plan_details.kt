package com.example.mobile_assignment.workout

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentWorkoutPlanDetailsBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import util.showConfirmationDialog
import util.toBitmap
import util.toast

class fragment_workout_plan_details : Fragment() {

    private lateinit var binding: FragmentWorkoutPlanDetailsBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkoutPlanDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        setupRecyclerView()
        displayWorkoutPlanDetails()

        binding.btnStartWorkout.setOnClickListener {
            // Handle start workout logic here
        }
        binding.btnEditWorkoutPlan.setOnClickListener {
            findNavController().navigate(R.id.editCustomWorkoutPlan)
        }

        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                         exerciseViewModel.clearSelectedExercises() // Clear selected exercises
                        Log.d("WorkoutPlanDetailsWOI", "Navigating up")
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.btnDeleteWorkoutPlan.setOnClickListener {
            showConfirmationDialog(
                message = "Are you sure you want to delete this workout plan?",
                positiveText = "Delete",
                negativeText = "Cancel",
                onPositiveClick = { deleteWorkoutPlan() },
                onNegativeClick = { /* Do nothing */ }
            )
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = SelectedExerciseAdapter(exerciseViewModel)

        binding.rvExercises.layoutManager = LinearLayoutManager(context)
        binding.rvExercises.adapter = adapter

        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            Log.d("WorkoutPlanDetails", "Selected exercises: $exercises")
            updateRecyclerView(exercises)
        }
    }
    private fun updateRecyclerView(exercises: List<Exercise>) {
        val adapter = binding.rvExercises.adapter as SelectedExerciseAdapter
        adapter.submitList(exercises)
    }
    override fun onResume() {
        super.onResume()
        // Refresh custom plan when returning to this fragment
        val customPlan = exerciseViewModel.selectedCustomPlan.value
        val userId = getCurrentUserId()
        customPlan?.id?.let { exerciseViewModel.refreshSelectedCustomPlan(it, userId) }
    }
    private fun getCurrentUserId(): String {
        val sharedPref = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE)
        return sharedPref.getString("userId", "U001") ?: "U001"
    }
    private fun displayWorkoutPlanDetails() {
        exerciseViewModel.selectedCustomPlan.observe(viewLifecycleOwner) { customPlan ->
            customPlan?.let {
                binding.tvPlanName.text = it.name
                binding.tvPlanDetails.text =
                    "${it.exerciseIds.size} Exercises | ${it.restDuration} mins"

                it.photo?.let { blob ->
                    binding.ivPlanImage.setImageBitmap(blob.toBitmap())
                }

                exerciseViewModel.updateSelectedExercisesByIds(it.exerciseIds)
            }
        }
    }

    private fun deleteWorkoutPlan() {
        val customPlan = exerciseViewModel.selectedCustomPlan.value
        val userId = getCurrentUserId()

        customPlan?.let {
            exerciseViewModel.deleteCustomPlan(it.id, userId,
                onSuccess = {
                    toast("Workout Plan Deleted")
                    exerciseViewModel.clearSelectedExercises() // Clear selected exercises
                    findNavController().navigateUp() // Navigate back
                },
                onFailure = { e ->
                    toast("Failed to delete workout plan: ${e.message}")
                }
            )
        }
    }
}