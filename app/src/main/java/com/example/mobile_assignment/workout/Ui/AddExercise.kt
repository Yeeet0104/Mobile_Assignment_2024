package com.example.mobile_assignment.workout.Ui

import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentAddExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import android.widget.SearchView
import com.example.mobile_assignment.workout.ExerciseAdapter

class addExercise : Fragment() {
    private lateinit var binding: FragmentAddExerciseBinding
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private var allExercises = listOf<Exercise>()
    private var isEdit = false
    private val auth: AuthVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddExerciseBinding.inflate(inflater, container, false)
        isEdit = arguments?.getBoolean("isEdit") ?: false

        setupRecyclerView()
        setupObservers()
        setupSearchView()

        binding.btnDone.setOnClickListener {
            if (isEdit) {
                val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
                val customPlan = exerciseViewModel.selectedCustomPlan.value
                if (customPlan != null) {
                    val updatedPlan = customPlan.copy(exerciseIds = selectedExercises.map { it.id })
                    val userId = getCurrentUserId()
                    exerciseViewModel.updateCustomPlan(updatedPlan, userId, onSuccess = {
                        findNavController().navigateUp() // Navigate back to the previous screen
                    }, onFailure = {
                    })
                }
            }else{
                findNavController().navigateUp()
            }
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
        exerciseViewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            allExercises = exercises
            exerciseAdapter.submitList(exercises)
        }

    }
    private fun setupSearchView() {
        binding.svSearchExercise.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterExercises(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterExercises(newText)
                return true
            }
        })
    }
    private fun filterExercises(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allExercises
        } else {
            allExercises.filter { it.name.contains(query, ignoreCase = true) }
        }
        exerciseAdapter.submitList(filteredList)
    }

    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }
}