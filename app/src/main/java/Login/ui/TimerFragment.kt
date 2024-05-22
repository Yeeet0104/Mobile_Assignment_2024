package Login.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mobile_assignment.databinding.FragmentTimerBinding
import kotlin.math.roundToInt

class TimerFragment : Fragment() {

    private lateinit var binding: FragmentTimerBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0

    // Inflates the fragment's view and sets up event listeners for buttons
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimerBinding.inflate(inflater, container, false)

        // Set up button click listeners
        binding.startStopButton.setOnClickListener { startStopTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }

        // Initialize the intent to start the timer service
        serviceIntent = Intent(requireContext(), TimerService::class.java)

        return binding.root
    }

    // Registers the BroadcastReceiver to update the time when the fragment resumes
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    // Unregisters the BroadcastReceiver when the fragment pauses
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateTime)
    }

    // Resets the timer by stopping it and resetting the displayed time to zero
    private fun resetTimer() {
        stopTimer()
        time = 0.0
        binding.timeTV.text = getTimeStringFromDouble(time)
    }

    // Toggles between starting and stopping the timer
    private fun startStopTimer() {
        if (timerStarted)
            stopTimer()
        else
            startTimer()
    }

    // Starts the timer by starting the service and updating the button text
    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        requireContext().startService(serviceIntent)
        binding.startStopButton.text = "Stop"
        timerStarted = true
    }

    // Stops the timer by stopping the service and updating the button text
    private fun stopTimer() {
        requireContext().stopService(serviceIntent)
        binding.startStopButton.text = "Start"
        timerStarted = false
    }

    // BroadcastReceiver to update the displayed time from the service
    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTV.text = getTimeStringFromDouble(time)
        }
    }

    // Converts the time from double to a formatted string
    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    // Formats the time components into a string with the format "HH:MM:SS"
    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d:%02d", hour, min, sec)
}
