package com.example.mobile_assignment.workout.Ui

import Login.data.AuthVM
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentWorkoutPlanDetailsBinding
import com.example.mobile_assignment.workout.Data.Exercise
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.example.mobile_assignment.workout.SelectedExerciseAdapter
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import util.showConfirmationDialog
import util.toBitmap
import util.toast
import java.io.IOException
import com.google.zxing.BarcodeFormat

class fragment_workout_plan_details : Fragment() {

    private lateinit var binding: FragmentWorkoutPlanDetailsBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val auth: AuthVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkoutPlanDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        setupRecyclerView()
        displayWorkoutPlanDetails()

//        binding.btnStartWorkout.setOnClickListener {
//            // Handle start workout logic here
//        }
        binding.btnEditWorkoutPlan.setOnClickListener {
            findNavController().navigate(R.id.editCustomWorkoutPlan)
        }
        binding.btnExportQr.setOnClickListener {
            exportWorkoutPlanAsQR()
        }
        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                         exerciseViewModel.clearSelectedExercises() // Clear selected exercises
                        Log.d("WorkoutPlanDetailsWOI", "Navigating up")
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.btnDeleteWorkoutPlan.setOnClickListener {
            showConfirmationDialog(
                message = "Are you sure you want to delete this workout plan?",
                positiveText = "Delete",
                negativeText = "Cancel",
                onPositiveClick = { deleteWorkoutPlan() },
                onNegativeClick = { /* Do nothing */ }
            )
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = SelectedExerciseAdapter(exerciseViewModel)

        binding.rvExercises.layoutManager = LinearLayoutManager(context)
        binding.rvExercises.adapter = adapter

        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            Log.d("WorkoutPlanDetails", "Selected exercises: $exercises")
            updateRecyclerView(exercises)
        }
    }
    private fun updateRecyclerView(exercises: List<Exercise>) {
        val adapter = binding.rvExercises.adapter as SelectedExerciseAdapter
        adapter.submitList(exercises)
    }
    override fun onResume() {
        super.onResume()
        // Refresh custom plan when returning to this fragment
        val customPlan = exerciseViewModel.selectedCustomPlan.value
        val userId = getCurrentUserId()
        customPlan?.id?.let { exerciseViewModel.refreshSelectedCustomPlan(it, userId) }
    }
    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }
    private fun displayWorkoutPlanDetails() {
        exerciseViewModel.selectedCustomPlan.observe(viewLifecycleOwner) { customPlan ->
            customPlan?.let {
                binding.tvPlanName.text = it.name
                binding.tvPlanDetails.text =
                    "${it.exerciseIds.size} Exercises"

                it.photo?.let { blob ->
                    binding.ivPlanImage.setImageBitmap(blob.toBitmap())
                }

                exerciseViewModel.updateSelectedExercisesByIds(it.exerciseIds)
            }
        }
    }

    private fun deleteWorkoutPlan() {
        val customPlan = exerciseViewModel.selectedCustomPlan.value
        val userId = getCurrentUserId()

        customPlan?.let {
            exerciseViewModel.deleteCustomPlan(it.id, userId,
                onSuccess = {
                    toast("Workout Plan Deleted")
                    exerciseViewModel.clearSelectedExercises() // Clear selected exercises
                    findNavController().navigateUp() // Navigate back
                },
                onFailure = { e ->
                    toast("Failed to delete workout plan: ${e.message}")
                }
            )
        }
    }
    private fun exportWorkoutPlanAsQR() {
        val customPlan = exerciseViewModel.selectedCustomPlan.value
        val userId = getCurrentUserId()

        if (customPlan != null) {
            val qrContent = "userId:${userId},workoutPlanId:${customPlan.id}"
            val qrBitmap = generateQRCode(qrContent)
            if (qrBitmap != null) {
                saveImage(requireContext(), qrBitmap)
            } else {
                toast("Failed to generate QR code.")
            }
        }
    }
    private fun generateQRCode(content: String): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
    private fun saveImage(context: Context, bitmap: Bitmap) {
        val filename = "QR_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri == null) {
            toast("Failed to create new MediaStore record.")
            return
        }

        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    toast("Failed to save bitmap.")
                } else {
                    toast("Qr Code generated and saved to gallery.")
                }
            }
        } catch (e: IOException) {
            toast("Failed to write bitmap: ${e.message}")
        }
    }
}