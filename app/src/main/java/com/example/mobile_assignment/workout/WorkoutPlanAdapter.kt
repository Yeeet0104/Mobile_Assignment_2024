package com.example.mobile_assignment.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemWorkoutPlanBinding

class WorkoutPlanAdapter (
    val fn: (ViewHolder, workoutPlan) -> Unit = { _, _ -> }
) : ListAdapter<workoutPlan, WorkoutPlanAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<workoutPlan>() {
        override fun areItemsTheSame(a: workoutPlan, b: workoutPlan) = a.title == b.title
        override fun areContentsTheSame(a: workoutPlan, b: workoutPlan) = a == b
    }

    class ViewHolder(val binding: ItemWorkoutPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWorkoutPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = getItem(position)

        holder.binding.tvWorkoutTime.text = workout.time
        holder.binding.tvWorkoutTitle.text = workout.title
        holder.binding.tvWorkoutDetails.text = workout.details
        holder.binding.btnStartWorkout.setOnClickListener {
            // Handle button click
        }

        fn(holder, workout)
    }
}