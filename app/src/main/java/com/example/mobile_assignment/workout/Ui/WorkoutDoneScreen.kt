package com.example.mobile_assignment.workout.Ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddCustomPlanBinding
import com.example.mobile_assignment.databinding.FragmentAddNewExerciseBinding
import com.example.mobile_assignment.databinding.FragmentWorkoutDoneScreenBinding


class workoutDoneScreen : Fragment() {
    private lateinit var binding: FragmentWorkoutDoneScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWorkoutDoneScreenBinding.inflate(inflater, container, false)

        binding.BtnbackToHome.setOnClickListener {
            findNavController().navigate(R.id.workoutHome)
        }
        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigate(R.id.workoutHome)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.workoutHome)
        }

        return binding.root
    }


}