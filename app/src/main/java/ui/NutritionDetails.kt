package ui

import Data.NutritionVM
import Data.getDailyFoodReference
import Data.getDateReference
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionDetailsBinding
import util.setImageBlob
import util.toast
import java.time.LocalDateTime

class NutritionDetails : Fragment() {
    private lateinit var binding: FragmentNutritionDetailsBinding

    private val nav by lazy { findNavController() }
    private val foodId by lazy { arguments?.getString("foodId") ?: "" }
    private val nutritionViewModel: NutritionVM by activityViewModels()

    //DATABASE ITEM TEST
    @RequiresApi(Build.VERSION_CODES.O)
    private var date = LocalDateTime.now()
    private var userId = "A001"
    private var caloriesTarget  = 2000


    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCalories() {
        val food = nutritionViewModel.get(foodId)
        if (food == null) {
            if (isAdded) {
                toast("Food not found")
            }
            return
        }

        val dateStr = date.toLocalDate().toString()

        // Ensure the DateItem document exists
        val dateItem = Data.DateItem(date = dateStr, caloriesTarget = caloriesTarget)
        val dateRef = getDateReference(userId, dateStr)
        dateRef.set(dateItem)
            .addOnSuccessListener {
                // DateItem document successfully written or updated
                val trackerItemRef = getDailyFoodReference(userId, dateStr)
                val trackerItem = Data.TrackerItem(foodId = food.foodId, foodName = food.foodName, calories = food.calories, image = food.image)

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
    private fun editCal() {
        // Navigate to the edit screen passing the foodId
        nav.navigate(R.id.nutritionEdit, bundleOf("foodId" to foodId))
    }

    private fun deleteCal() {
        nutritionViewModel.delete(foodId)
        toast("Food deleted from database")
        // Optionally, navigate back to the previous screen after deletion
        nav.navigateUp()
    }

}