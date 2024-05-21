package com.example.mobile_assignment.workout

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemWorkoutPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan

class WorkoutPlanAdapter(
    private val onItemClicked: (CustomPlan) -> Unit,
    private val onStartWorkoutClicked: (CustomPlan) -> Unit,
    private val showStartWorkoutButton: Boolean = true // Add a flag to show/hide the button
//    val fn: (ViewHolder, CustomPlan) -> Unit = { _, _ -> }
) : ListAdapter<CustomPlan, WorkoutPlanAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<CustomPlan>() {
        override fun areItemsTheSame(a: CustomPlan, b: CustomPlan) = a.id == b.id
        override fun areContentsTheSame(a: CustomPlan, b: CustomPlan) = a == b
    }

    class ViewHolder(val binding: ItemWorkoutPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWorkoutPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customPlan = getItem(position)

        holder.binding.tvWorkoutTitle.text = customPlan.name
        holder.binding.tvWorkoutDetails.text = "${customPlan.exerciseIds.size} Exercises"
        holder.binding.tvWorkoutTime.text = customPlan.timeOfDay
        customPlan.photo?.let {
            val imageBytes = it.toBytes()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.workoutPlanImg.setImageBitmap(bitmap)
        }
        customPlan.status.let {
            if (it == 1) {
                holder.binding.btnStartWorkout.text = "Completed"
            }
        }
        holder.binding.btnStartWorkout.visibility = if (showStartWorkoutButton) View.VISIBLE else View.GONE

        // Set click listener for the item view to view details
        holder.itemView.setOnClickListener {
            onItemClicked(customPlan)
        }

        // Set click listener for the "Start Workout" button
        holder.binding.btnStartWorkout.setOnClickListener {
            onStartWorkoutClicked(customPlan)
        }
    }

}