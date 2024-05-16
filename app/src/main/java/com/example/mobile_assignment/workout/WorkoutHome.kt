package com.example.mobile_assignment.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentWorkoutHomeBinding


class workoutHome : Fragment() {
    private lateinit var binding : FragmentWorkoutHomeBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWorkoutHomeBinding.inflate(inflater, container, false)

        binding.workoutProgress.max = 3
        binding.workoutProgress.progress = 1


        binding.caloriesProgress.max = 1500
        binding.caloriesProgress.progress = 750

        val workoutList = listOf(
            workoutPlan("5:00 AM", "Fullbody Workout", "11 Exercises | 32mins"),
            workoutPlan("1:00 PM", "Lowebody Workout", "12 Exercises | 40mins"),
            // Add more workout items as needed
        )
        val adapter = WorkoutPlanAdapter { holder, workout ->
            // You can handle additional interactions here if needed
        }
        adapter.submitList(workoutList)

        binding.rvWorkouts.layoutManager = LinearLayoutManager(context)
        binding.rvWorkouts.adapter = adapter

        binding.btnAddMore.setOnClickListener {
            findNavController().navigate(R.id.addCustomPlan)
        }


        return binding.root
    }
}