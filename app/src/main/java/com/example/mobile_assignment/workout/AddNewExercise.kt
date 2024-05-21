package com.example.mobile_assignment.workout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddNewExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.firebase.firestore.Blob
import util.cropToBlob
import util.setImageBlob
import util.toast


class AddNewExercise : Fragment() {
    private lateinit var binding: FragmentAddNewExerciseBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private var imageUri: Uri? = null
    private var currentExercise: Exercise? = null
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewExerciseBinding.inflate(inflater, container, false)
        binding.rbDuration.isChecked = true
        binding.etCaloriesBurnt.inputType = InputType.TYPE_CLASS_NUMBER
        exerciseViewModel.selectedExercise.observe(viewLifecycleOwner) { exercise ->
            exercise?.let {
                currentExercise = it
                binding.etExerciseName.setText(it.name)
                binding.etYouTubeId.setText("https://www.youtube.com/watch?v=${it.youtubeId}")
                binding.etSteps.setText(it.steps.joinToString(", "))
                if (it.duration != 0) {
                    binding.rbDuration.isChecked = true
                    binding.etExerciseValue.setText(it.duration.toString())
                    binding.etExerciseValue.hint = "Duration (e.g., 30s)"
                } else {
                    binding.rbReps.isChecked = true
                    binding.etExerciseValue.setText(it.reps.toString())
                    binding.etExerciseValue.hint = "Reps (e.g., 15)"
                }
                it.photo?.let { photo ->
                    binding.ivnewExerciseImage.setImageBlob(photo)
                }
                binding.tvAddNewExercise.text = "Edit Exercise"
                binding.etCaloriesBurnt.setText(it.caloriesBurn.toString())
                setToolbarTitle("Edit Exercise")
            } ?: run {
                currentExercise = null
                binding.etExerciseName.text.clear()
                binding.etYouTubeId.text.clear()
                binding.etSteps.text.clear()
                binding.etExerciseValue.text.clear()
                binding.ivnewExerciseImage.setImageDrawable(null)
                binding.tvAddNewExercise.text = "Add New Exercise"
                binding.rbDuration.isChecked = true
                binding.etCaloriesBurnt.text.clear()
                setToolbarTitle("Add New Exercise")
            }
        }


        binding.ivnewExerciseImage.setOnClickListener {
            openFileChooser()
        }

        binding.btnSaveExercise.setOnClickListener {
            val name = binding.etExerciseName.text.toString()
            val youtubeUrl = binding.etYouTubeId.text.toString()
            val steps = binding.etSteps.text.toString().split(",").map { it.trim() }
            val isDuration = binding.rbDuration.isChecked
            val isReps = binding.rbReps.isChecked
            val caloriesBurnt = binding.etCaloriesBurnt.text.toString().toInt()

            if (name.isNotEmpty() && (isDuration || isReps) && youtubeUrl.isNotEmpty()) {
                val youtubeId = extractYouTubeId(youtubeUrl)
                if (youtubeId != null) {
                    if (currentExercise != null) {
                        // Update existing exercise
                        val photo = if (imageUri != null) binding.ivnewExerciseImage.cropToBlob(300, 300) else currentExercise?.photo
                        saveExercise(currentExercise!!.id, name, binding.etExerciseValue.text.toString().toInt(), youtubeId, photo, steps, isDuration,caloriesBurnt)
                    } else {
                        // Create new exercise
                        exerciseViewModel.generateCustomId { customId ->
                            val photo = if (imageUri != null) binding.ivnewExerciseImage.cropToBlob(300, 300) else null
                            saveExercise(customId, name, binding.etExerciseValue.text.toString().toInt(), youtubeId, photo, steps, isDuration,caloriesBurnt)

                        }
                    }
                    toast("Exercise Created Successfully")
                    findNavController().navigateUp()
                } else {
                    toast("Invalid YouTube URL")
                }
            } else {
                toast("Please fill in all required fields")
            }
        }

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDuration -> {
                    binding.etExerciseValue.hint = "Duration (e.g., 30s)"
                    binding.etExerciseValue.inputType = InputType.TYPE_CLASS_NUMBER
                }
                R.id.rbReps -> {
                    binding.etExerciseValue.hint = "Reps (e.g., 15)"
                    binding.etExerciseValue.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }
        }
        return binding.root
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivnewExerciseImage.setImageURI(it)
        }
    }

    private fun openFileChooser() {
        getContent.launch("image/*")
    }
    private fun saveExercise(customId: String, name: String, value: Int, youtubeId: String, photo: Blob?, steps: List<String>, isDuration: Boolean,caloriesBurnt:Int) {
        val exercise = Exercise(
            id = customId,
            name = name,
            duration = if (isDuration) value else 0,
            reps = if (!isDuration) value else 0,
            youtubeId = youtubeId,
            photo = photo,
            steps = steps,
            caloriesBurn = caloriesBurnt
        )
        exerciseViewModel.addExercise(exercise)
    }

    private fun extractYouTubeId(url: String): String? {
        val regex = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:v|embed|watch\\?v=|watch\\?.+&v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})\$".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1)
    }
    private fun setToolbarTitle(title: String) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }
}