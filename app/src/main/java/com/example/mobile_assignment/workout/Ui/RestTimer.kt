package com.example.mobile_assignment.workout.Ui

import android.os.Bundle
import android.os.CountDownTimer
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
import com.example.mobile_assignment.databinding.FragmentRestTimerBinding
import com.example.mobile_assignment.workout.Data.WorkoutSharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class RestTimer : Fragment() {
    private lateinit var binding : FragmentRestTimerBinding
    private var timer: CountDownTimer? = null
    private val sharedViewModel: WorkoutSharedViewModel by activityViewModels()
    private var countDownTimer: CountDownTimer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestTimerBinding.inflate(inflater, container, false)
        startRestTimer(45 * 1000L) // 45 seconds

        // Hide the ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        // Hide the Bottom Navigation Bar
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bv)
        bottomNav?.visibility = View.GONE

        binding.btnDoneRest.setOnClickListener {
            timer?.cancel()
            findNavController().navigateUp()
        }

        binding.btnSkipRest.setOnClickListener {
            timer?.cancel()
            findNavController().navigateUp()
        }
        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        timer?.cancel()
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            timer?.cancel()
            findNavController().popBackStack(R.id.workoutHome, true)
        }
        return binding.root
    }
    private fun startRestTimer(duration: Long) {
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvRestTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                binding.tvRestTimer.text = "0"
                playSound()
                findNavController().navigateUp()
            }
        }.start()
    }
    private fun playSound() {
//        val mediaPlayer = MediaPlayer.create(context, R.raw.timer_finish_sound)
//        mediaPlayer.start()
    }
}