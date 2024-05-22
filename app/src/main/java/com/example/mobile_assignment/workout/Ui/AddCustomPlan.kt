package com.example.mobile_assignment.workout.Ui

import Login.data.AuthVM
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentAddCustomPlanBinding
import com.example.mobile_assignment.workout.Data.CustomPlan
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.example.mobile_assignment.workout.MultiSelectSpinner
import com.example.mobile_assignment.workout.SelectedExerciseAdapter
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import util.cropToBlob
import util.toast
import java.util.Calendar
import com.google.zxing.Result


class addCustomPlan : Fragment() {
    private lateinit var binding: FragmentAddCustomPlanBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private val auth: AuthVM by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCustomPlanBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupImagePicker()
        restoreImage()

        binding.exercises.setOnClickListener {
            findNavController().navigate(R.id.addExercise)
        }

        binding.btnDone.setOnClickListener {
            saveCustomPlan()
        }

        binding.btnSelectDays.setOnClickListener {
            showDaysOfWeekDialog { selectedDays ->
                exerciseViewModel.setSelectedDays(selectedDays)
                binding.etDaysSelected.text = selectedDays.joinToString(", ")

            }
        }

        binding.btnImportQr.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                exerciseViewModel.setSelectedTime(time)
                binding.etTimeSelected.text = time
            }
        }

        // Adding MenuProvider to handle options menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        exerciseViewModel.clearSelectedExercises() // Clear selected exercises
                        exerciseViewModel.clearSelectedDaysAndTime() // Clear selected exercises
                        exerciseViewModel.clearSelectedImageUri() // Clear selected exercises
                        Log.d("WorkoutPlanDetailsWOI", "Navigating up")
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        return binding.root
    }


    private fun setupRecyclerView() {
        val adapter = SelectedExerciseAdapter(exerciseViewModel)
        binding.rvSelectedExercises.layoutManager = LinearLayoutManager(context)
        binding.rvSelectedExercises.adapter = adapter

        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            adapter.submitList(exercises)
            binding.tvNoExercises.visibility = if (exercises.isEmpty()) View.VISIBLE else View.GONE

        }
    }

    private fun setupObservers() {
        exerciseViewModel.selectedExercises.observe(viewLifecycleOwner) { exercises ->
            (binding.rvSelectedExercises.adapter as SelectedExerciseAdapter).submitList(exercises)
            binding.tvNoExercises.visibility = if (exercises.isEmpty()) View.VISIBLE else View.GONE
        }

        exerciseViewModel.selectedDays.observe(viewLifecycleOwner) { days ->
            binding.etDaysSelected.text = days.joinToString(", ")
        }

        exerciseViewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.etTimeSelected.text = time
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivCustomImage.setImageURI(it)
            exerciseViewModel.setSelectedImageUri(it)
        }
    }

    private fun selectImage() {
        getContent.launch("image/*")
    }

    private fun setupImagePicker() {
        binding.ivCustomImage.setOnClickListener {
            selectImage()
        }
    }
    private fun restoreImage() {
        exerciseViewModel.selectedImageUri.value?.let {
            binding.ivCustomImage.setImageURI(it)
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveCustomPlan() {
        val planName = binding.etPlanName.text.toString().trim()
        val targetedBodyPart = binding.etTargetedBody.text.toString().trim()
        val restDuration = binding.etRestDuration.text.toString().trim()
        val daysOfWeek = binding.etDaysSelected.text.toString().split(", ").filter { it.isNotEmpty() }
        val timeOfDay = binding.etTimeSelected.text.toString().trim()

        val selectedExercises = exerciseViewModel.selectedExercises.value ?: emptyList()
        val exerciseIds = selectedExercises.map { it.id }

        if (planName.isEmpty() || targetedBodyPart.isEmpty() || restDuration.isEmpty() || exerciseIds.isEmpty() || daysOfWeek.isEmpty() || timeOfDay.isEmpty()) {
            toast("Please fill all fields and select at least one exercise, day, and time")
            return
        }

        val userId = getCurrentUserId()

        val imageBlob = binding.ivCustomImage.cropToBlob(300, 300)

        // Check for duplicate plan name
        checkDuplicatePlanName(planName, userId) { isDuplicate ->
            if (isDuplicate) {
                toast("A workout plan with this name already exists. Please choose a different name.")
            } else {
                // No duplicate found, proceed to save the custom plan
                saveCustomPlanToDb(planName, targetedBodyPart, restDuration, exerciseIds, imageBlob, daysOfWeek, timeOfDay, userId)
                clearInputs()
                toast("Custom Plan Saved")
            }
        }
    }

    private fun checkDuplicatePlanName(planName: String, userId: String, callback: (Boolean) -> Unit) {
        db.collection("customPlans").document(userId).collection("plans").document(planName).get()
            .addOnSuccessListener { document ->
                Log.d("addCustomPlan", "Checking for duplicate plan name: ${userId}")
                Log.d("addCustomPlan", "Document exists: ${document.exists()}")
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                Log.d("addCustomPlan", "Error checking for duplicate plan name: ${e.message}")
                toast("Error checking for duplicate plan name: ${e.message}")
            }
    }
    private fun saveCustomPlanToDb(
        planName: String,
        targetedBodyPart: String,
        restDuration: String,
        exerciseIds: List<String>,
        imageBlob: Blob?,
        daysOfWeek: List<String>,
        timeOfDay: String,
        userId: String
    ) {
        val customPlan = CustomPlan(
            id = planName, // Use the plan name as the document ID
            name = planName,
            targetedBodyPart = targetedBodyPart,
            restDuration = restDuration,
            exerciseIds = exerciseIds,
            photo = imageBlob,
            daysOfWeek = daysOfWeek,
            timeOfDay = timeOfDay
        )

        exerciseViewModel.addCustomPlan(customPlan, userId,
            onSuccess = {
                toast("Custom Plan Saved")
                clearInputs()
//                scheduleWorkoutNotification(requireContext(), customPlan)
                findNavController().navigateUp()
            },
            onFailure = { e ->
                toast("Failed to save custom plan: ${e.message}")
            }
        )
    }

    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }

    private fun clearInputs() {
        binding.etPlanName.text.clear()
        binding.etTargetedBody.text.clear()
        binding.etRestDuration.text.clear()
        binding.etDaysSelected.text = ""
        binding.etTimeSelected.text = ""
        exerciseViewModel.clearSelectedImageUri()
        exerciseViewModel.clearSelectedExercises()
        exerciseViewModel.clearSelectedDaysAndTime()
        binding.ivCustomImage.setImageResource(R.drawable.outline_add_circle_outline_24)
    }

    private fun showTimePickerDialog(onTimeSet: (Int, Int) -> Unit) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun showDaysOfWeekDialog(onDaysSelected: (List<String>) -> Unit) {
        val daysOfWeek = MultiSelectSpinner.daysOfWeek
        val currentSelections = exerciseViewModel.selectedDays.value ?: emptyList()
        val selectedDays = BooleanArray(daysOfWeek.size) { index ->
            daysOfWeek[index] in currentSelections
        }

        AlertDialog.Builder(context)
            .setTitle("Select Days of the Week")
            .setMultiChoiceItems(daysOfWeek.toTypedArray(), selectedDays) { _, which, isChecked ->
                selectedDays[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selectedList = daysOfWeek.filterIndexed { index, _ -> selectedDays[index] }
                onDaysSelected(selectedList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    // import from qr code
    private val getContentQr = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            decodeQrCodeFromImage(it)
        }
    }
    private fun showImagePickerDialog() {
        val options = arrayOf("Take a Picture", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select an option")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        takePicture()
                    } else {
                        requestCameraPermission()
                    }
                }
                1 -> chooseFromGallery()
            }
        }
        builder.show()
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture()
        } else {
            toast("Camera permission is required to take pictures.")
        }
    }
    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            imageUri?.let {
                decodeQrCodeFromImage(it)
            }
        }
    }

//    private fun takePicture() {
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, "QR_${System.currentTimeMillis()}.jpg")
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        }
//        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        cameraLauncher.launch(imageUri)
//    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val result: Result? = decodeBitmap(bitmap)
            result?.let { qrResult ->
                handleScannedData(qrResult.text)
            } ?: run {
                toast("No QR code found in the taken picture.")
            }
        }
    }

    private fun takePicture() {
        takePictureLauncher.launch(null)
    }
    private fun chooseFromGallery() {
        getContentQr.launch("image/*")
    }

    private fun decodeQrCodeFromImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            Log.d("addCustomPlan", "Bitmap decoded successfully")
            val result: Result? = decodeBitmap(bitmap)
            result?.let { qrResult ->
                handleScannedData(qrResult.text)
            } ?: run {
                toast("No QR code found in the selected image.")
            }
        } catch (e: Exception) {
            Log.d("addCustomPlan", "Error decoding bitmap or QR code: ${e.message}")
        }
    }
    private fun decodeBitmap(bitmap: Bitmap): Result? {
        try {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            return MultiFormatReader().decode(binaryBitmap)
        } catch (e: Exception) {
            Log.d("addCustomPlan", "Error decoding QR code: ${e.message}")
            return null
        }
    }

    private fun handleScannedData(data: String) {
        if (!data.contains(",") || !data.contains(":")) {
            toast("Invalid QR code data.")
            Log.d("addCustomPlan", "Invalid QR code data: $data")
            return
        }
        val userId = getCurrentUserId()
        val planInfo = data.split(",").map { it.split(":")[1] }

        // Check if planInfo has at least two elements
        if (planInfo.size < 2) {
            toast("Invalid QR code data.")
            return
        }

        val scannedUserId = planInfo[0]
        val workoutPlanId = planInfo[1]

        if (userId == scannedUserId) {
            toast("You cannot add your own workout plan.")
            return
        }
        Log.d("addCustomPlan", "Scanned user ID: $scannedUserId")
        Log.d("addCustomPlan", "Scanned user ID: $workoutPlanId")
        // Check for duplication and add the workout plan to the user's list
        checkAndAddWorkoutPlan(scannedUserId, workoutPlanId)
    }

    private fun checkAndAddWorkoutPlan(userId: String, workoutPlanId: String) {
        exerciseViewModel.fetchCustomPlanByOtherId(workoutPlanId,userId, onSuccess = { plan ->
            if (plan != null) {
                exerciseViewModel.checkAndAddCustomPlan(getCurrentUserId(), plan,
                    onSuccess = {
                        toast("Workout plan added successfully.")
                        findNavController().navigateUp()
                    },
                    onFailure = {
                        toast("Workout plan already exists.")
                    }
                )
            } else {
                toast("Invalid workout plan.")
            }
        }, onFailure = {
            toast("Failed to fetch workout plan.")
        })
    }
}