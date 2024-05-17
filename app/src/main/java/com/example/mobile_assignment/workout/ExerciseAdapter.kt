package com.example.mobile_assignment.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemExerciseBinding

class ExerciseAdapter (
    val fn: (ViewHolder, Exercise) -> Unit = { _, _ -> }
) : ListAdapter<Exercise, ExerciseAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.workoutID == b.workoutID
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    class ViewHolder(val binding: ItemExerciseBinding ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = getItem(position)

        holder.binding.tvExerciseName.text = exercise.name
        holder.binding.tvExerciseDuration.text = exercise.duration

        fn(holder, exercise)
    }
}