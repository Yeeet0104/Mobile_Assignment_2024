package com.example.mobile_assignment.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel


class addExercise : Fragment() {
    private lateinit var binding: FragmentAddExerciseBinding
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddExerciseBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()

        binding.btnDone.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous screen
        }

        binding.btnAddNewExercise.setOnClickListener {
            findNavController().navigate(R.id.addNewExercise)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(exerciseViewModel) { holder, exercise ->
            // Handle exercise item click if needed
        }

        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = exerciseAdapter
        }
    }

    private fun setupObservers() {
        exerciseViewModel.exercises.observe(viewLifecycleOwner, { exercises ->
            exerciseAdapter.submitList(exercises)
        })
    }
}