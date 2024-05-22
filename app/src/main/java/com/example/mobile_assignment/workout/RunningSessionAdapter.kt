package com.example.mobile_assignment.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemRunningSessionBinding
import com.example.mobile_assignment.workout.Data.RunningSession
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RunningSessionAdapter : RecyclerView.Adapter<RunningSessionAdapter.RunningSessionViewHolder>() {

    private var runningSessions = emptyList<RunningSession>()

    class RunningSessionViewHolder(private val binding: ItemRunningSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(runningSession: RunningSession) {
            val dateFormatted = formatDate(runningSession.startTime)
            val startTimeFormatted = formatTime(runningSession.startTime)
            val endTimeFormatted = formatTime(runningSession.endTime)
            val durationFormatted = calculateDuration(runningSession.startTime, runningSession.endTime)
            val distanceInKm = runningSession.distance / 1000
            val distanceFormatted = String.format("%.2f", distanceInKm)
            val averageSpeedFormatted = String.format("%.2f", runningSession.averageSpeed)

            binding.tvDate.text = "Date: $dateFormatted"
            binding.tvStartTime.text = "Start Time: $startTimeFormatted"
            binding.tvEndTime.text = "End Time: $endTimeFormatted"
            binding.tvDistance.text = "Distance: $distanceFormatted km"
            binding.tvAverageSpeed.text = "Avg Speed: $averageSpeedFormatted km/h"
            binding.tvDuration.text = "Duration: $durationFormatted"
        }


        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
        private fun calculateDuration(startTime: Long, endTime: Long): String {
            val durationMillis = endTime - startTime
            val minutes = (durationMillis / (1000 * 60) % 60).toInt()
            val hours = (durationMillis / (1000 * 60 * 60) % 24).toInt()
            return "$hours hr $minutes min"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunningSessionViewHolder {
        val binding = ItemRunningSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunningSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunningSessionViewHolder, position: Int) {
        holder.bind(runningSessions[position])
    }

    override fun getItemCount() = runningSessions.size

    fun submitList(runningSessions: List<RunningSession>) {
        this.runningSessions = runningSessions
        notifyDataSetChanged()
    }
}