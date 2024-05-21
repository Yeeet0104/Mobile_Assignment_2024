package Nutrition

import Nutrition.Data.FoodItem
import Nutrition.Data.NutritionVM
import Nutrition.Data.QRCodeData
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentNutritionAddBinding
import com.google.gson.Gson
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import util.cropToBlob
import util.setImageBlob
import util.toast


class NutritionAdd : Fragment() {
    private lateinit var nutritionVM: NutritionVM // Move declaration here

    //USER ID
    private lateinit var userId: String

    private lateinit var binding: FragmentNutritionAddBinding
    private val nav by lazy { findNavController() }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionAddBinding.inflate(inflater, container, false)
        nutritionVM = activityViewModels<NutritionVM>().value
        userId = nutritionVM.getCurrentUserId()


        reset()

        binding.photo.setOnClickListener { select() }
        binding.btnAdd.setOnClickListener { addFood() }
        binding.btnReset.setOnClickListener { reset() }
        binding.btnImport.setOnClickListener { importQRCode() }

        binding.edtFat.addTextChangedListener { calculateTotalCalories() }
        binding.edtCarbs.addTextChangedListener { calculateTotalCalories() }
        binding.edtProtein.addTextChangedListener { calculateTotalCalories() }

        return binding.root
    }

    private fun importQRCode() {
        val options = arrayOf("Scan QR Code", "Select QR Code Image")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Import QR Code")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> scanQRCode() // Scan QR Code using camera
                    1 -> selectQRCodeFromFile() // Select QR Code image from file
                }
            }
        builder.show()
    }

    private fun scanQRCode() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("Scan QR Code\n")
            .setBeepEnabled(true)
        getResult.launch(options)
    }

    private val getResult = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val qrCodeResult = com.google.zxing.Result(result.contents, null, null, null)
            handleQRCodeResult(qrCodeResult)
        }
    }


    private val selectQRCodeLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    val source = RGBLuminanceSource(width, height, pixels)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val reader = MultiFormatReader()
                    val result = reader.decode(binaryBitmap)
                    handleQRCodeResult(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("QR code is invalid or expired.")
                }
            }
        }

    private fun selectQRCodeFromFile() {
        selectQRCodeLauncher.launch("image/*")
    }

    private fun handleQRCodeResult(result: com.google.zxing.Result) {
        val qrCodeData = Gson().fromJson(result.text, QRCodeData::class.java)
        val foodRef = nutritionVM.getImportFoodReference(qrCodeData.userId, qrCodeData.foodId)

        foodRef.get().addOnSuccessListener { documentSnapshot ->
            val foodItem = documentSnapshot.toObject(FoodItem::class.java)
            if (foodItem != null) {
                binding.edtFoodName.setText(foodItem.foodName)
                binding.edtFat.setText(foodItem.fat.toString())
                binding.edtCarbs.setText(foodItem.carbs.toString())
                binding.edtProtein.setText(foodItem.protein.toString())
                binding.edtDescription.setText(foodItem.description)
                binding.photo.setImageBlob(foodItem.image)
            } else {
                toast("QR code expired or invalid.")
            }
        }.addOnFailureListener { exception ->
            toast("Failed to get food item: ${exception.message}")
        }
    }

    // Existing methods: select(), addFood(), calculateTotalCalories(), reset()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.photo.setImageURI(it)
    }

    private fun select() {
        // Select file
        getContent.launch("image/*")
    }


    private fun addFood() {
        // Get the input from the fields
        nutritionVM.generateCustomId { foodId ->
            val name = binding.edtFoodName.text.toString().trim()
            val fat = binding.edtFat.text.toString().toIntOrNull()
            val carbs = binding.edtCarbs.text.toString().toIntOrNull()
            val protein = binding.edtProtein.text.toString().toIntOrNull()
            val calories = (4 * (protein ?: 0) + 4 * (carbs ?: 0) + 9 * (fat ?: 0))
            val photo = binding.photo.cropToBlob(300, 300)
            val description = binding.edtDescription.text.toString().trim()

            // Check if the name is valid
            if (name.isBlank()) {
                toast("Please enter a name.")
                return@generateCustomId
            }

            // Check if the fat is valid
            if (fat == null) {
                toast("Please enter a valid number for fat.")
                return@generateCustomId
            }

            // Check if the carbs is valid
            if (carbs == null) {
                toast("Please enter a valid number for carbs.")
                return@generateCustomId
            }

            // Check if the protein is valid
            if (protein == null) {
                toast("Please enter a valid number for protein.")
                return@generateCustomId
            }

            if (photo.toBytes().isEmpty()) {
                toast("Please select a photo.")
                return@generateCustomId
            }

            if (description.isBlank()) {
                toast("Please enter a description.")
                return@generateCustomId
            }

            // Create a new Food object
            val food = FoodItem(foodId, name, calories, protein, carbs, fat, photo, description)

            // Add the food to the database
            nutritionVM.addFoodItem(userId, food)

            // Clear the input fields
            reset()

            // Show a success message
            toast("Food added into database successfully.")

            nav.navigateUp()
        }
    }

    private fun calculateTotalCalories() {
        val fat = binding.edtFat.text.toString().toIntOrNull() ?: 0
        val carbs = binding.edtCarbs.text.toString().toIntOrNull() ?: 0
        val protein = binding.edtProtein.text.toString().toIntOrNull() ?: 0

        val totalCalories = (4 * protein + 4 * carbs + 9 * fat)
        binding.totalCalories.text = "$totalCalories kcalories"
    }

    private fun reset() {
        binding.edtFat.text.clear()
        binding.edtCarbs.text.clear()
        binding.edtProtein.text.clear()
        binding.edtFoodName.text.clear()
        binding.edtDescription.text.clear()
        binding.totalCalories.text = "0 kcalories"
        binding.photo.setImageDrawable(null)

        binding.edtFoodName.requestFocus()
    }

}