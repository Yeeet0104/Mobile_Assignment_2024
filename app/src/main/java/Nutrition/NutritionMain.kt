package Nutrition

import Nutrition.Data.NutritionVM
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionMainBinding
import util.FoodRecordAdapter
import util.toast
import java.time.LocalDate
import java.time.LocalDateTime


class NutritionMain : Fragment() {
    private lateinit var binding: FragmentNutritionMainBinding
    private val nav by lazy { findNavController() }
    private val nutritionVM by activityViewModels<NutritionVM>()

    //DATABASE ITEM TEST
    // TODO: Replace with actual user ID
    private var userId = "U001"

    @RequiresApi(Build.VERSION_CODES.O)
    private var date = LocalDateTime.now().toLocalDate().toString()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionMainBinding.inflate(inflater, container, false)

        binding.searchView.setOnClickListener {
            nav.navigate(R.id.nutritionSearch)
        }

        binding.btnAddFood.setOnClickListener {
            nav.navigate(R.id.nutritionAdd)
        }

        binding.tvDate.text = date

        binding.ibCalender.setOnClickListener {
            val current = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    date = selectedDate.toString()
                    binding.tvDate.text = date

                    nutritionVM.initializeTrackerForNewDay(userId, date)

                    nutritionVM.getDailyTrackerReference(userId, date)
                        .observe(viewLifecycleOwner) { trackerItems ->
                            // Calculate the sum of the calories of all trackerItems
                            val totalCalories = trackerItems.sumOf { it.calories }

                            // Set the progress value of caloriesProgress
                            binding.caloriesProgress.progress = totalCalories
                            binding.tvCal.text = "$totalCalories kcal"

                            // Set the adapter for the RecyclerView
                            binding.rvMain.adapter = FoodRecordAdapter(
                                onDeleteClick = { position ->
                                    // Implement deletion logic here using position
                                    // For example:
                                    val itemToDelete = trackerItems[position]
                                    nutritionVM.deleteTrackerItem(date, itemToDelete)
                                    toast("Deleted ${itemToDelete.foodName}")
                                }
                            ) { holder, foodItem ->
                                holder.binding.root.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("foodId", foodItem.foodId)
                                    nav.navigate(R.id.nutritionDetails, bundle)
                                }
                            }

                            // Submit the list to the adapter
                            (binding.rvMain.adapter as FoodRecordAdapter).submitList(trackerItems)
                        }

                    // Observe the targetCalories from Firestore
                    nutritionVM.getTargetCalories(userId, date)
                        .observe(viewLifecycleOwner) { targetCalories ->
                            binding.caloriesProgress.max = targetCalories
                            binding.tvCalTarget.text = "$targetCalories kcal"
                        }

                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        nutritionVM.getDailyTrackerReference(userId, date)
            .observe(viewLifecycleOwner) { trackerItems ->
                // Calculate the sum of the calories of all trackerItems
                val totalCalories = trackerItems.sumOf { it.calories }

                // Set the progress value of caloriesProgress
                binding.caloriesProgress.progress = totalCalories
                binding.tvCal.text = "$totalCalories kcal"

                // Set the adapter for the RecyclerView
                binding.rvMain.adapter = FoodRecordAdapter(
                    onDeleteClick = { position ->
                        // Implement deletion logic here using position
                        // For example:
                        val itemToDelete = trackerItems[position]
                        nutritionVM.deleteTrackerItem(date, itemToDelete)
                        toast("Deleted ${itemToDelete.foodName}")
                    }
                ) { holder, foodItem ->
                    holder.binding.root.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("foodId", foodItem.foodId)
                        nav.navigate(R.id.nutritionDetails, bundle)
                    }
                }

                // Submit the list to the adapter
                (binding.rvMain.adapter as FoodRecordAdapter).submitList(trackerItems)
            }

        binding.btnEditTarget.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_target, null)

            // Create the AlertDialog
            val dialog = AlertDialog.Builder(context)
                .setTitle("Edit target calories")
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    // Get the input from the EditText
                    val etTargetCalories = dialogView.findViewById<EditText>(R.id.edtTargetCalories)
                    val targetCaloriesInput = etTargetCalories.text.toString()

                    if (targetCaloriesInput.isEmpty()) {
                        toast("Target calories cannot be empty")
                        return@setPositiveButton
                    }

                    val newTargetCalories = etTargetCalories.text.toString().toInt()

                    nutritionVM.updateTargetCalories(userId, date, newTargetCalories)

                    toast("Updated target calories to $newTargetCalories")
                }
                .setNegativeButton("Cancel", null)
                .create()

            // Show the dialog
            dialog.show()
        }

        // Observe the targetCalories from Firestore
        nutritionVM.getTargetCalories(userId, date).observe(viewLifecycleOwner) { targetCalories ->
            binding.caloriesProgress.max = targetCalories
            binding.tvCalTarget.text = "$targetCalories kcal"
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        nutritionVM.initializeTrackerForNewDay(userId, date)
        nutritionVM.initializePersonalFoodForNewUser(userId)
    }


}