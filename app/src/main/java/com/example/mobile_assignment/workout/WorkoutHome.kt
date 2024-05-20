package com.example.mobile_assignment.workout

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentWorkoutHomeBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import util.convertTimeOfDayToDate
import java.util.Calendar


class workoutHome : Fragment() {
    private lateinit var binding : FragmentWorkoutHomeBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWorkoutHomeBinding.inflate(inflater, container, false)

        binding.workoutProgress.max = 3
        binding.workoutProgress.progress = 1

        binding.caloriesProgress.max = 1500
        binding.caloriesProgress.progress = 750

        setupRecyclerView()
        setupObservers()

        binding.btnAddMore.setOnClickListener {
            findNavController().navigate(R.id.addCustomPlan)
        }

        binding.btnManageExercises.setOnClickListener {
            findNavController().navigate(R.id.manageExercises)
        }


        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = WorkoutPlanAdapter { holder, customPlan ->
            exerciseViewModel.selectCustomPlan(customPlan)
            nav.navigate(R.id.fragment_workout_plan_details)
        }
        binding.rvWorkouts.layoutManager = LinearLayoutManager(context)
        binding.rvWorkouts.adapter = adapter
    }

    private fun setupObservers() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "U001") ?: "U001"

        exerciseViewModel.todaysCustomPlans.observe(viewLifecycleOwner) { customPlans ->
            val unstartedPlans = customPlans.filter { it.status == 0 }
            (binding.rvWorkouts.adapter as WorkoutPlanAdapter).submitList(unstartedPlans)
        }

        exerciseViewModel.fetchCustomPlans(userId)
    }


}