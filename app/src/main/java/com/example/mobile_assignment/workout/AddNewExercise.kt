package com.example.mobile_assignment.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddNewExerciseBinding


class AddNewExercise : Fragment() {
    private lateinit var binding: FragmentAddNewExerciseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewExerciseBinding.inflate(inflater, container, false)

        binding.btnSaveExercise.setOnClickListener {
            // Save the new exercise (implement your logic here)
        }

        return binding.root
    }

}