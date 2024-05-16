package com.example.mobile_assignment.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddExerciseBinding


class addExercise : Fragment() {
    private lateinit var binding: FragmentAddExerciseBinding
    private lateinit var exerciseAdapter: ExerciseAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddExerciseBinding.inflate(inflater, container, false)

        setupRecyclerView()

        binding.btnDone.setOnClickListener {
            // Handle done action
        }
        binding.btnAddNewExercise.setOnClickListener {
            findNavController().navigate(R.id.addNewExercise)
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter { holder, exercise ->
            // Handle exercise item click if needed
        }

        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = exerciseAdapter
        }

        // Add sample data
        val sampleExercises = listOf(
            Exercise(workoutID = "1", name = "Warm Up", duration = "5:00"),
            Exercise(workoutID = "2", name = "Jumping Jack", duration = "12x"),
            Exercise(workoutID = "3", name = "Skipping", duration = "15x"),
            Exercise(workoutID = "4", name = "Squats", duration = "20x"),
            Exercise(workoutID = "5", name = "Arm Raises", duration = "0:53"),
            Exercise(workoutID = "6", name = "Rest and Drink", duration = "2:00")
        )

        exerciseAdapter.submitList(sampleExercises)
    }
}