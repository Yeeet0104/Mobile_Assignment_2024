package ui

import Data.NutritionVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionDetailsBinding
import util.setImageBlob
import util.toast

class NutritionDetails : Fragment() {
    private lateinit var binding: FragmentNutritionDetailsBinding

    private val nav by lazy { findNavController() }
    private val foodId by lazy { arguments?.getString("foodId") ?: "" }

    private val nutritionViewModel: NutritionVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionDetailsBinding.inflate(inflater, container, false)

        nutritionViewModel.getFoodById(foodId).observe(viewLifecycleOwner) { food ->
            if (food == null) {
                nav.navigateUp()
                return@observe
            }

            binding.ivFood.setImageBlob(food.image)
            binding.tvFoodName.text = food.foodName
            binding.tvCalories.text = "${food.calories} kcal"
            binding.tvProtein.text = "${food.protein} g"
            binding.tvCarbs.text = "${food.carbs} g"
            binding.tvFat.text = "${food.fat} g"
            binding.tvDescription.text = food.description
        }

        binding.btnAddCal.setOnClickListener {
            addCalories()
        }
        binding.btnEdit.setOnClickListener {
            editCal()
        }
        binding.btnDelete.setOnClickListener {
            deleteCal()
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

    fun addCalories() {
       // nutritionViewModel.addCalories(foodId)
    }
    fun editCal() {
        // Navigate to the edit screen passing the foodId
        nav.navigate(R.id.nutritionEdit, bundleOf("foodId" to foodId))
    }

    fun deleteCal() {
        nutritionViewModel.delete(foodId)
        toast("Food deleted from database")
        // Optionally, navigate back to the previous screen after deletion
        nav.navigateUp()
    }

}