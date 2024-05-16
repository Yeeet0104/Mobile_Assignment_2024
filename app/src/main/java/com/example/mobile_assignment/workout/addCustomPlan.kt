package com.example.mobile_assignment.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddCustomPlanBinding


class addCustomPlan : Fragment() {
    private lateinit var binding: FragmentAddCustomPlanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCustomPlanBinding.inflate(inflater, container, false)


        binding.exercises.setOnClickListener {
            findNavController().navigate(R.id.addExercise)
        }
        // Set up click listener for the exercises section
        return binding.root
    }



}