package com.example.mobile_assignment.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentManageExercisesBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel


class manageExercises : Fragment() {

    private lateinit var binding: FragmentManageExercisesBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private var allExercises = listOf<Exercise>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageExercisesBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupSearchView()
        binding.btnAddExercise.setOnClickListener {
            exerciseViewModel.clearSelectedExercise() // Clear selected exercise before adding a new one
            findNavController().navigate(R.id.addNewExercise)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = ManageExerciseAdapter(exerciseViewModel, this)
        binding.rvExercises.layoutManager = LinearLayoutManager(context)
        binding.rvExercises.adapter = adapter
    }

    private fun setupObservers() {
        exerciseViewModel.exercises.observe(viewLifecycleOwner, { exercises ->
            allExercises = exercises
            (binding.rvExercises.adapter as ManageExerciseAdapter).submitList(exercises)
        })
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
        (binding.rvExercises.adapter as ManageExerciseAdapter).submitList(filteredList)
    }
}