package com.example.mobile_assignment.workout

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentViewAllWorkoutPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel


class viewAllWorkoutPlan : Fragment() {
    private lateinit var binding: FragmentViewAllWorkoutPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private lateinit var workoutPlanAdapter: WorkoutPlanAdapter
    private var fullWorkoutPlanList: List<CustomPlan> = listOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentViewAllWorkoutPlanBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()

        // Set up search functionality
        binding.svSearchExercise.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterWorkoutPlans(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterWorkoutPlans(it) }
                return true
            }
        })


        return binding.root
    }

    private fun setupRecyclerView() {
        workoutPlanAdapter = WorkoutPlanAdapter(
            onItemClicked = { customPlan ->
                exerciseViewModel.selectCustomPlan(customPlan)
                findNavController().navigate(R.id.fragment_workout_plan_details)
            },
            onStartWorkoutClicked = { customPlan ->
            },
            showStartWorkoutButton = false
        )

        binding.rvWorkouts.layoutManager = LinearLayoutManager(context)
        binding.rvWorkouts.adapter = workoutPlanAdapter
    }

    private fun setupObservers() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "U001") ?: "U001"

        exerciseViewModel.customPlans.observe(viewLifecycleOwner) { customPlans ->
            fullWorkoutPlanList = customPlans
            workoutPlanAdapter.submitList(customPlans)
        }

        exerciseViewModel.fetchCustomPlans(userId)
    }

    private fun filterWorkoutPlans(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullWorkoutPlanList
        } else {
            fullWorkoutPlanList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        workoutPlanAdapter.submitList(filteredList)
    }
}