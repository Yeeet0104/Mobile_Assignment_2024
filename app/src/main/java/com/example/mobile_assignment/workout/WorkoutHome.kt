package com.example.mobile_assignment.workout

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentWorkoutHomeBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import util.convertTimeOfDayToDate
import util.toast
import java.util.Calendar


class workoutHome : Fragment() {
    private lateinit var binding : FragmentWorkoutHomeBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val workoutSharedViewModel: WorkoutSharedViewModel by activityViewModels()
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWorkoutHomeBinding.inflate(inflater, container, false)

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
        binding.workoutViewAll.workoutViewAllContainer.setOnClickListener {
            findNavController().navigate(R.id.viewAllWorkoutPlan)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = WorkoutPlanAdapter(
            onItemClicked = { customPlan ->
                // Handle item view click to view details
                exerciseViewModel.selectCustomPlan(customPlan)
                nav.navigate(R.id.fragment_workout_plan_details)
            },
            onStartWorkoutClicked = { customPlan ->
                // Handle "Start Workout" button click
                workoutSharedViewModel.selectWorkoutPlan(customPlan)
                nav.navigate(R.id.startExercise)
            }
        )
        binding.rvWorkouts.layoutManager = LinearLayoutManager(context)
        binding.rvWorkouts.adapter = adapter
    }

    private fun setupObservers() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "U001") ?: "U001"

        exerciseViewModel.todaysCustomPlans.observe(viewLifecycleOwner) { customPlans ->
            binding.workoutProgress.max = customPlans.size
            binding.workoutTargetDaily.text = "Target : " +  customPlans.size.toString() + " Workouts"
            val totalCaloriesBurnt = customPlans.sumOf { customPlan ->
                customPlan.exerciseIds.sumOf { exerciseId ->
                    val exercise = exerciseViewModel.getExerciseInfo(exerciseId)
                    exercise!!.caloriesBurn.toString().toInt()
                }
            }
            binding.txtTargetDailyCalBurnt.text ="Target : " + "$totalCaloriesBurnt Calories"
            // Count the number of completed workouts
            val completedWorkouts = customPlans.count { it.status == 1 }
            // Set the progress to the number of completed workouts
            if(completedWorkouts != customPlans.size){

                binding.workoutLeft.text = completedWorkouts.toString() + " Workouts Plan Left"
            }else{
                binding.workoutLeft.text = "All Workouts Plan Completed"

            }
            binding.workoutProgress.progress = completedWorkouts
            val completedPlans = customPlans.filter { it.status == 1 }
            val totalCaloriesBurntByUser= completedPlans.sumOf { customPlan ->
                customPlan.exerciseIds.sumOf { exerciseId ->
                    val exercise = exerciseViewModel.getExerciseInfo(exerciseId)
                    exercise?.caloriesBurn?.toInt() ?: 0
                }
            }
            val totalCaloriesBurntLeft = totalCaloriesBurnt - totalCaloriesBurntByUser
            binding.dailyTargetCalBurntLeft.text = "$totalCaloriesBurntLeft Calories Left"
            binding.caloriesProgress.progress = totalCaloriesBurntByUser
            binding.caloriesProgress.max = totalCaloriesBurnt

            (binding.rvWorkouts.adapter as WorkoutPlanAdapter).submitList(customPlans)
        }

        exerciseViewModel.fetchCustomPlans(userId)
    }


}