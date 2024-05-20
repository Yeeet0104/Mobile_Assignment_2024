package com.example.mobile_assignment.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemWorkoutPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan

class WorkoutPlanAdapter(
    val fn: (ViewHolder, CustomPlan) -> Unit = { _, _ -> }
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

        holder.binding.tvWorkoutTime.text = customPlan.name
        holder.binding.tvWorkoutTitle.text = customPlan.targetedBodyPart
        holder.binding.tvWorkoutDetails.text = customPlan.restDuration

        fn(holder, customPlan)
    }
}