package ui

import Data.FoodItem
import Data.NutritionVM
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionAddBinding
import util.cropToBlob
import util.toast


class NutritionAdd : Fragment() {
    private lateinit var binding: FragmentNutritionAddBinding
    private val nav by lazy { findNavController() }
    private val nutritionVM: NutritionVM by activityViewModels()


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionAddBinding.inflate(inflater, container, false)

        reset()

        binding.photo.setOnClickListener { select() }
        binding.btnAdd.setOnClickListener { addFood() }
        binding.btnReset.setOnClickListener { reset() }

        binding.edtFat.addTextChangedListener { calculateTotalCalories() }
        binding.edtCarbs.addTextChangedListener { calculateTotalCalories() }
        binding.edtProtein.addTextChangedListener { calculateTotalCalories() }

        return binding.root
    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.photo.setImageURI(it)
    }

    private fun select() {
        // Select file
        getContent.launch("image/*")
    }

    @RequiresApi(Build.VERSION_CODES.R)
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

            if(description.isBlank()) {
                toast("Please enter a description.")
                return@generateCustomId
            }

            // Create a new Food object
            val food = FoodItem(foodId, name, calories, protein, carbs, fat, photo, description)

            // Add the food to the database
            nutritionVM.addFoodItem(food)

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
        binding.totalCalories.text = "0 kcalories"
        binding.photo.setImageDrawable(null)

        binding.edtFoodName.requestFocus()
    }

}