package com.example.mobile_assignment.workout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddNewExerciseBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.firebase.firestore.Blob
import util.toast


class AddNewExercise : Fragment() {
    private lateinit var binding: FragmentAddNewExerciseBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewExerciseBinding.inflate(inflater, container, false)

        binding.btnChooseImage.setOnClickListener {
            openFileChooser()
        }

        binding.btnSaveExercise.setOnClickListener {
            val name = binding.etExerciseName.text.toString()
            val youtubeId = binding.etYouTubeId.text.toString()
            val steps = binding.etSteps.text.toString().split(",").map { it.trim() }
            val isDuration = binding.rbDuration.isChecked
            val isReps = binding.rbReps.isChecked

            if (name.isNotEmpty() && (isDuration || isReps)) {
                exerciseViewModel.generateCustomId { customId ->
                    if (imageUri != null) {
                        exerciseViewModel.convertImageToBlob(requireContext(), imageUri!!) { photo ->
                            if (photo != null) {
                                val value = binding.etExerciseValue.text.toString()
                                if (value.isNotEmpty()) {
                                    saveExercise(customId, name, value, youtubeId, photo, steps, isDuration)
                                    toast("Exercise added successfully")
                                } else {
                                    toast("Please enter value")
                                }
                            } else {
                                toast("Failed to process image")
                            }
                        }
                    } else {
                        val value = binding.etExerciseValue.text.toString()
                        if (value.isNotEmpty()) {
                            saveExercise(customId, name, value, youtubeId, null, steps, isDuration)
                        } else {
                            toast("Please enter value")
                        }
                    }
                }
            } else {
                toast("Please fill in all required fields")
            }
        }

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDuration -> {
                    binding.etExerciseValue.hint = "Duration (e.g., 30s)"
                    binding.etExerciseValue.inputType = InputType.TYPE_CLASS_TEXT
                }
                R.id.rbReps -> {
                    binding.etExerciseValue.hint = "Reps (e.g., 15)"
                    binding.etExerciseValue.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }
        }
        return binding.root
    }
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.imageView.setImageURI(imageUri)
        }
    }

    private fun saveExercise(customId: String, name: String, value: String, youtubeId: String, photo: Blob?, steps: List<String>, isDuration: Boolean) {
        val exercise = Exercise(
            id = customId,
            name = name,
            duration = if (isDuration) value else "",
            reps = if (!isDuration) value else "",
            youtubeId = youtubeId,
            photo = photo,
            steps = steps
        )
        exerciseViewModel.addExercise(exercise)
    }
}