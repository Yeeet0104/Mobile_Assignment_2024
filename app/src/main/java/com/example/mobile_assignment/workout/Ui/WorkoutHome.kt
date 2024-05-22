package com.example.mobile_assignment.workout.Ui

import Login.data.AuthVM
import android.content.Context
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
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.example.mobile_assignment.workout.WorkoutPlanAdapter
import com.example.mobile_assignment.workout.Data.WorkoutSharedViewModel
import util.toast


class workoutHome : Fragment() {
    private lateinit var binding : FragmentWorkoutHomeBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val workoutSharedViewModel: WorkoutSharedViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWorkoutHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()

        binding.btnAddMore.setOnClickListener {
            val userId = auth.getPreferences().getString("id", "")
            if (userId == "") {
                toast("User not found")
                return@setOnClickListener
            }
            findNavController().navigate(R.id.addCustomPlan)
        }

        binding.btnManageExercises.setOnClickListener {
            val userId = auth.getPreferences().getString("id", "")
            if (userId == "") {
                toast("User not found")
                return@setOnClickListener
            }
            findNavController().navigate(R.id.manageExercises)
        }
        binding.workoutViewAll.workoutViewAllContainer.setOnClickListener {
            val userId = auth.getPreferences().getString("id", "")
            if (userId == "") {
                toast("User not found")
                return@setOnClickListener
            }
            findNavController().navigate(R.id.viewAllWorkoutPlan)
        }
        if (getCurrentRole() == 1){
            binding.btnManageExercises.visibility = View.VISIBLE
        }else{
            binding.btnManageExercises.visibility = View.GONE
        }
        binding.workoutChatBot.setOnClickListener {
            findNavController().navigate(R.id.workoutChatBot2)
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
        val userId = auth.getPreferences().getString("id", "")
        if (userId == "") {
            toast("User not found")
            return
        }
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

        exerciseViewModel.fetchCustomPlans(getCurrentUserId())

    }

    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }
    private fun getCurrentRole(): Int {
        val sharedPreferences = auth.getPreferences()
        val role = sharedPreferences.getInt("role", 0)
        return role ?: 0
    }
}