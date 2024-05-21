package com.example.mobile_assignment.workout

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ItemExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import java.util.Collections

class ExerciseAdapter (
    val viewModel: ExerciseViewModel,
    val fn: (ViewHolder, Exercise) -> Unit = { _, _ -> }
) : ListAdapter<Exercise, ExerciseAdapter.ViewHolder>(Diff) {
    private var selectedExercises = mutableListOf<Exercise>()
    companion object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    class ViewHolder(val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = getItem(position)

        holder.binding.tvExerciseName.text = exercise.name
        if(exercise.duration != 0) {
            holder.binding.tvExerciseDuration.text ="Durations : " + exercise.duration.toString()
        }else{
            holder.binding.tvExerciseDuration.text = "Reps : " + exercise.reps.toString()
        }

        exercise.photo?.let {
            val imageBytes = it.toBytes()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.imageView.setImageBitmap(bitmap)
        }

        holder.binding.checkBox.isChecked = viewModel.isSelected(exercise)

        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleExerciseSelection(exercise)
                selectedExercises.add(exercise)
            } else {
                viewModel.toggleExerciseSelection(exercise)
                selectedExercises.remove(exercise)
            }
        }

        holder.itemView.setOnClickListener {
            viewModel.selectExercise(exercise)
            holder.itemView.findNavController().navigate(R.id.exerciseDetailsFragment)
        }

        fn(holder, exercise)
    }
}