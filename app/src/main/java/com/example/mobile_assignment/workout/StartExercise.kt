package com.example.mobile_assignment.workout

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentStartExerciseBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import util.toBitmap

class StartExercise : Fragment() {
    private lateinit var binding : FragmentStartExerciseBinding

    private val sharedViewModel: WorkoutSharedViewModel by activityViewModels()
    private val ExerciseViewModel: ExerciseViewModel by activityViewModels()
    private var countDownTimer: CountDownTimer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartExerciseBinding.inflate(inflater, container, false)
        // Hide the ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        // Hide the Bottom Navigation Bar
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bv)
        bottomNav?.visibility = View.GONE
        sharedViewModel.selectedWorkoutPlan.observe(viewLifecycleOwner) { workoutPlan ->
            workoutPlan?.let { updateUI(it) }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            stopCounter()
            findNavController().popBackStack(R.id.workoutHome, true)
        }

        binding.btnDone.setOnClickListener {
            sharedViewModel.addProgress(ExerciseViewModel.getCurrentUserId())
            sharedViewModel.nextExercise()
            stopAndNavigate()
        }

        binding.btnSkip.setOnClickListener {
            sharedViewModel.nextExercise()
            stopAndNavigate()
        }
        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        stopCounter()
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }

    private fun updateUI(workoutPlan: CustomPlan) {
        val currentExerciseIndex = sharedViewModel.currentExerciseIndex.value ?: 0

        Log.d("StartExercise", "currentExerciseIndex ID: $currentExerciseIndex")
        val exerciseId = workoutPlan.exerciseIds[currentExerciseIndex]
        Log.d("StartExercise", "Exercise ID: $exerciseId")
        val exercises = ExerciseViewModel.getExerciseInfo(exerciseId)
        binding.tvExerciseName.text = exercises!!.name
        binding.ivExerciseImage.setImageBitmap(exercises.photo!!.toBitmap())
        if (exercises.duration != null) {
            binding.tvSetsAndReps.visibility = View.GONE
            binding.progressBarDuration.visibility = View.VISIBLE
            binding.tvcountDown.visibility = View.VISIBLE
            startDurationTimer(exercises.duration)
        } else {
            binding.tvSetsAndReps.visibility = View.VISIBLE
            binding.progressBarDuration.visibility = View.GONE
            binding.tvcountDown.visibility = View.GONE
            binding.tvSetsAndReps.text = "${exercises.reps} reps"
        }
    }
    private fun startDurationTimer(duration: Int) {
        val totalDurationInMillis = duration * 1000L
        countDownTimer = object : CountDownTimer(totalDurationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((totalDurationInMillis - millisUntilFinished) / totalDurationInMillis.toFloat() * 100).toInt()
                binding.progressBarDuration.progress = 100 - progress
                binding.tvcountDown.text = "${millisUntilFinished / 1000} Sec"
            }

            override fun onFinish() {
                binding.progressBarDuration.progress = 0
                sharedViewModel.addProgress(ExerciseViewModel.getCurrentUserId())
                navigateToNextExercise()
            }
        }
        countDownTimer?.start()
    }
    private fun stopAndNavigate() {
        countDownTimer?.cancel()
        navigateToNextExercise()
    }
    private fun stopCounter(){
        countDownTimer?.cancel()
    }
    private fun navigateToNextExercise() {
        val userId = ExerciseViewModel.getCurrentUserId()
        val totalExercises = sharedViewModel.selectedWorkoutPlan.value?.exerciseIds?.size ?: 0
        val currentProgress = sharedViewModel.workoutProgress.value ?: 0

        if (sharedViewModel.isLastExercise()) {
            if (currentProgress == totalExercises) {
                sharedViewModel.markAsCompleted(userId)
                findNavController().navigate(R.id.workoutDoneScreen2)
            }
        } else {
            findNavController().navigate(R.id.restTimer)
        }
    }
}