package ui

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
import com.example.mobile_assignment.databinding.FragmentNutritionEditBinding
import util.cropToBlob
import util.setImageBlob
import util.toast

class NutritionEdit : Fragment() {
    private val nav by lazy { findNavController() }
    private val nutritionVM: NutritionVM by activityViewModels()
    private lateinit var binding: FragmentNutritionEditBinding
    private val foodId by lazy { arguments?.getString("foodId") ?: "" }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionEditBinding.inflate(inflater, container, false)

        val food = nutritionVM.get(foodId)
        if (food == null) {
            nav.navigateUp()
            return null
        }

        binding.photo.setOnClickListener { select() }

        binding.photo.setImageBlob(food.image)
        binding.edtFoodName.setText(food.foodName)
        binding.edtProtein.setText(food.protein.toString())
        binding.edtFat.setText(food.fat.toString())
        binding.edtDescription.setText(food.description)
        binding.edtCarbs.setText(food.carbs.toString())
        binding.totalCalories.text = "${food.calories} kcal"

        binding.edtFat.addTextChangedListener { calculateTotalCalories() }
        binding.edtCarbs.addTextChangedListener { calculateTotalCalories() }
        binding.edtProtein.addTextChangedListener { calculateTotalCalories() }

        binding.btnConfirmEdit.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { cancel() }

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
    private fun save() {
        val foodName = binding.edtFoodName.text.toString()
        val carbs = binding.edtProtein.text.toString().toIntOrNull() ?: 0
        val protein = binding.edtProtein.text.toString().toIntOrNull() ?: 0
        val fat = binding.edtFat.text.toString().toIntOrNull() ?: 0
        val description = binding.edtDescription.text.toString()
        val photo = binding.photo.cropToBlob(300, 300)
        val calories = (4 * protein + 4 * carbs + 9 * fat)

        // Check if the name is valid
        if (foodName.isBlank()) {
            toast("Please enter a name.")
            return
        }

        // Check if the fat is valid
        if (fat == null) {
            toast("Please enter a valid number for fat.")
            return
        }

        // Check if the carbs is valid
        if (carbs == null) {
            toast("Please enter a valid number for carbs.")
            return
        }

        // Check if the protein is valid
        if (protein == null) {
            toast("Please enter a valid number for protein.")
            return
        }

        if (photo.toBytes().isEmpty()) {
            toast("Please select a photo.")
            return
        }

        if(description.isBlank()) {
            toast("Please enter a description.")
            return
        }

        nutritionVM.edit(foodId, foodName, calories, carbs, protein, fat, description)
        nav.navigateUp()
    }

    private fun cancel() {
        nav.navigateUp()
    }

    private fun calculateTotalCalories() {
        val fat = binding.edtFat.text.toString().toIntOrNull() ?: 0
        val carbs = binding.edtCarbs.text.toString().toIntOrNull() ?: 0
        val protein = binding.edtProtein.text.toString().toIntOrNull() ?: 0

        val totalCalories = (4 * protein + 4 * carbs + 9 * fat)
        binding.totalCalories.text = "$totalCalories kcalories"
    }
}