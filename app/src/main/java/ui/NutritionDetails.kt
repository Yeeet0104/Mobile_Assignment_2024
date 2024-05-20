package ui

import Data.NutritionVM
import Data.getDailyFoodReference
import Data.getDateReference
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionDetailsBinding
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
    private var userId = "A001"


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
                    val trackerItem = Data.TrackerItem(
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
                    val dateItem = Data.DateItem(date = date, caloriesTarget = 2000)
                    dateRef.set(dateItem)
                        .addOnSuccessListener {
                            // DateItem document successfully written or updated
                            val trackerItemRef = getDailyFoodReference(userId, date)
                            val trackerItem = Data.TrackerItem(
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
        nutritionViewModel.delete(foodId)
        toast("Food deleted from database")
        // Optionally, navigate back to the previous screen after deletion
        nav.navigateUp()
    }

}