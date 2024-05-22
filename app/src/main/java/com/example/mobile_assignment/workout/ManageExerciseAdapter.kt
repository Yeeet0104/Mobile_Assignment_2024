package com.example.mobile_assignment.workout

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ItemExerciseManageBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import util.showConfirmationDialog

class ManageExerciseAdapter(
    private val viewModel: ExerciseViewModel,
    private val fragment: Fragment
) : ListAdapter<Exercise, ManageExerciseAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    class ViewHolder(val binding: ItemExerciseManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExerciseManageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = getItem(position)

        holder.binding.tvExerciseName.text = exercise.name
        holder.binding.tvExerciseDuration.text = exercise.duration.toString()
        exercise.photo?.let {
            val imageBytes = it.toBytes()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.ivExerciseImage.setImageBitmap(bitmap)
        }

        holder.binding.ivEdit.setOnClickListener {
            viewModel.selectExercise(exercise)
            fragment.findNavController().navigate(R.id.addNewExercise) // navigate to the add/edit fragment
        }

        holder.binding.ivDelete.setOnClickListener {
            // Show confirmation dialog before deleting
            fragment.showConfirmationDialog(
                message = "Are you sure you want to delete this exercise?",
                positiveText = "Delete",
                negativeText = "Cancel",
                onPositiveClick = {
                    viewModel.deleteExercise(exercise)
                }
            )
        }
        holder.itemView.setOnClickListener {
            viewModel.selectExercise(exercise)
            fragment.findNavController().navigate(R.id.exerciseDetailsFragment) // navigate to the details fragment
        }
    }
}
