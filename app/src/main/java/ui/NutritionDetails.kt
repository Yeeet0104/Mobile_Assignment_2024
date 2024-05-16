package ui

import Data.NutritionVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionDetailsBinding
import util.setImageBlob

class NutritionDetails : Fragment() {
    private lateinit var binding: FragmentNutritionDetailsBinding

    private val foodId by lazy { arguments?.getString("foodId") ?: "" }

    private val nutritionViewModel: NutritionVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionDetailsBinding.inflate(inflater, container, false)

        val food = nutritionViewModel.get(foodId)
        if (food == null) {
            requireActivity().onBackPressed()
            return null
        }

        binding.ivFood.setImageBlob(food.image)
        binding.tvFoodName.text = food.foodName
        binding.tvCalories.text = "${food.calories} kcal"
        binding.tvProtein.text = "${food.protein} g"
        binding.tvFat.text = "${food.fat} g"

        return binding.root
    }
}