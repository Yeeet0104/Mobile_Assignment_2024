package Nutrition

import Nutrition.Data.NutritionVM
import Nutrition.Data.getDailyFoodReference
import Nutrition.Data.getDateReference
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionDetailsBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.journeyapps.barcodescanner.BarcodeEncoder
import util.setImageBlob
import util.toast
import java.time.LocalDate
import java.time.LocalDateTime

class NutritionDetails : Fragment() {
    private lateinit var binding: FragmentNutritionDetailsBinding

    private val nav by lazy { findNavController() }
    private val foodId by lazy { arguments?.getString("foodId") ?: "" }
    private val nutritionViewModel: NutritionVM by activityViewModels()

    //DATABASE ITEM TEST
    // TODO: Replace with actual user ID and calories target
    @RequiresApi(Build.VERSION_CODES.O)
    private var date = LocalDateTime.now().toLocalDate().toString()
    private var userId = "U001"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionDetailsBinding.inflate(inflater, container, false)

        nutritionViewModel.getFoodById(foodId).observe(viewLifecycleOwner) { food ->
            if (food == null) {
                nav.navigateUp()
                toast("Food not found")
                return@observe
            }

            binding.ivFood.setImageBlob(food!!.image)
            binding.tvFoodName.text = food.foodName
            binding.tvCalories.text = "${food.calories} kcal"
            binding.tvProtein.text = "${food.protein} g"
            binding.tvCarbs.text = "${food.carbs} g"
            binding.tvFat.text = "${food.fat} g"
            binding.tvDescription.text = food.description
        }

        binding.btnExport.setOnClickListener {
            // Get the current food object
            val food = nutritionViewModel.get(foodId)
            if (food != null) {
                // Create a new object with only the desired fields
                val foodData = mapOf(
                    "foodId" to food.foodId,
                    "userId" to userId
                )

                // Convert the new object to a JSON string
                val gson = Gson()
                val foodJson = gson.toJson(foodData)

                // Generate a QR code from the JSON string with increased size
                val hints = mapOf(
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L
                )

                try {
                    val qrCodeWriter = QRCodeWriter()
                    // Increase the size to 1000x1000 pixels
                    val bitMatrix = qrCodeWriter.encode(foodJson, BarcodeFormat.QR_CODE, 1000, 1000, hints)
                    val barcodeEncoder = BarcodeEncoder()
                    val qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix)

                    // Save the QR code as an image file
                    val filename = "${food.foodName}.png"
                    val resolver = context?.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = resolver?.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    resolver?.openOutputStream(uri ?: return@setOnClickListener)?.use { outputStream ->
                        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    // Show a message to the user
                    toast("QR code saved as $filename in the Downloads directory")
                } catch (e: WriterException) {
                    // Handle exception
                    e.printStackTrace()
                }
            }
        }

        binding.btnAddCal.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_calories_date, null)
            val ibAddCalender = dialogView.findViewById<ImageButton>(R.id.ibAddCalender)
            val tvAddDate = dialogView.findViewById<TextView>(R.id.tvAddDate)

            tvAddDate.text = date

            ibAddCalender.setOnClickListener {
                val current = LocalDateTime.now()
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        date = selectedDate.toString()
                        tvAddDate.text = date
                    },
                    current.year,
                    current.monthValue - 1,
                    current.dayOfMonth
                )
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete Food Item from database?")
                .setView(dialogView)
                .setPositiveButton("Yes") { _, _ ->
                    addCalories(date)
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.btnEdit.setOnClickListener {
            editCal()
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete Food Item from database?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteCal()
                }
                .setNegativeButton("No", null)
                .show()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val food = nutritionViewModel.get(foodId)
        if (food == null) {
            nav.navigateUp()
            return
        }

        binding.ivFood.setImageBlob(food.image)
        binding.tvFoodName.text = food.foodName
        binding.tvCalories.text = "${food.calories} kcal"
        binding.tvProtein.text = "${food.protein} g"
        binding.tvCarbs.text = "${food.carbs} g"
        binding.tvFat.text = "${food.fat} g"
        binding.tvDescription.text = food.description
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCalories(date: String) {
        val food = nutritionViewModel.get(foodId)
        if (food == null) {
            if (isAdded) {
                toast("Food not found")
            }
            return
        }

        // Ensure the DateItem document exists
        val dateRef = getDateReference(userId, date)

        dateRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // DateItem document exists, no need to create a new one
                    val trackerItemRef = getDailyFoodReference(userId, date)
                    val trackerItem = Nutrition.Data.TrackerItem(
                        foodId = food.foodId,
                        foodName = food.foodName,
                        calories = food.calories,
                        image = food.image
                    )

                    trackerItemRef.add(trackerItem)
                        .addOnSuccessListener {
                            // TrackerItem successfully added
                            toast("Food added to tracker")
                            nav.navigateUp()
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to add TrackerItem
                            e.printStackTrace()
                            toast("Failed to add food to tracker")
                        }

                } else {
                    // DateItem document doesn't exist, create a new one with caloriesTarget set to 2000
                    val dateItem = Nutrition.Data.DateItem(date = date, caloriesTarget = 2000)
                    dateRef.set(dateItem)
                        .addOnSuccessListener {
                            // DateItem document successfully written or updated
                            val trackerItemRef = getDailyFoodReference(userId, date)
                            val trackerItem = Nutrition.Data.TrackerItem(
                                foodId = food.foodId,
                                foodName = food.foodName,
                                calories = food.calories,
                                image = food.image
                            )

                            trackerItemRef.add(trackerItem)
                                .addOnSuccessListener {
                                    // TrackerItem successfully added
                                    toast("Food added to tracker")
                                    nav.navigateUp()
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure to add TrackerItem
                                    e.printStackTrace()
                                    toast("Failed to add food to tracker")
                                }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to set DateItem
                            e.printStackTrace()
                            toast("Failed to update tracker date")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to get DateItem
                e.printStackTrace()
                toast("Failed to get tracker date")
            }
    }

    private fun editCal() {
        // Navigate to the edit screen passing the foodId
        nav.navigate(R.id.nutritionEdit, bundleOf("foodId" to foodId))
    }

    private fun deleteCal() {
        nutritionViewModel.delete(userId, foodId)
        toast("Food deleted from database")
        // Optionally, navigate back to the previous screen after deletion
        nav.navigateUp()
    }

}