package com.example.mobile_assignment.workout

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ItemSelectedExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel

class SelectedExerciseAdapter (
    private val viewModel: ExerciseViewModel
) : ListAdapter<Exercise, SelectedExerciseAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    class ViewHolder(val binding: ItemSelectedExerciseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSelectedExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = getItem(position)

        holder.binding.tvExerciseName.text = exercise.name
        holder.binding.tvExerciseDuration.text = exercise.duration

        exercise.photo?.let {
            val imageBytes = it.toBytes()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.imageView.setImageBitmap(bitmap)
        }

        holder.itemView.setOnClickListener {
            viewModel.selectExercise(exercise)
            holder.itemView.findNavController().navigate(R.id.exerciseDetailsFragment)
        }
    }
}