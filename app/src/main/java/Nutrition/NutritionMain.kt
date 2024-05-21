package Nutrition

import Nutrition.Data.NutritionVM
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
    private lateinit var nutritionVM: NutritionVM // Move declaration here

    //USER ID
    private lateinit var userId: String
    private lateinit var date: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionMainBinding.inflate(inflater, container, false)

        nutritionVM = activityViewModels<NutritionVM>().value
        userId = nutritionVM.getCurrentUserId()
        date = LocalDateTime.now().toLocalDate().toString()

        binding.searchView.setOnClickListener {
            nav.navigate(R.id.nutritionSearch)
        }

        nutritionVM.getDailyTrackerReference(userId, date)
            .observe(viewLifecycleOwner) { trackerItems ->
                // Calculate the sum of the calories of all trackerItems
                val totalCalories = trackerItems.sumOf { it.calories }

                // Get target calories synchronously
                // Observe target calories
                nutritionVM.getTargetCalories(userId, date)
                    .observe(viewLifecycleOwner) { targetCalories ->
                        // Set the max value and text of caloriesProgress
                        binding.caloriesProgress.max = targetCalories
                        binding.tvCalTarget.text = "$targetCalories kcal"

                        // Set the progress value and text of caloriesProgress
                        binding.tvCal.text = "$totalCalories kcal"
                        binding.caloriesProgress.progress = totalCalories
                    }

                // Set the adapter for the RecyclerView
                binding.rvMain.adapter = FoodRecordAdapter(
                    onDeleteClick = { position ->
                        // Implement deletion logic here using position
                        val itemToDelete = trackerItems[position]
                        nutritionVM.deleteTrackerItem(userId, date, itemToDelete)
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

        binding.btnAddFood.setOnClickListener {
            nav.navigate(R.id.nutritionAdd)
        }

        binding.btnChatbot.setOnClickListener {
            nav.navigate(R.id.nutritionChatbot)
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

                            // Get target calories synchronously
                            // Observe target calories
                            nutritionVM.getTargetCalories(userId, date)
                                .observe(viewLifecycleOwner) { targetCalories ->
                                    // Set the max value and text of caloriesProgress
                                    binding.caloriesProgress.max = targetCalories
                                    binding.tvCalTarget.text = "$targetCalories kcal"

                                    // Set the progress value and text of caloriesProgress
                                    binding.tvCal.text = "$totalCalories kcal"
                                    binding.caloriesProgress.progress = totalCalories
                                }

                            // Set the adapter for the RecyclerView
                            binding.rvMain.adapter = FoodRecordAdapter(
                                onDeleteClick = { position ->
                                    // Implement deletion logic here using position
                                    val itemToDelete = trackerItems[position]
                                    nutritionVM.deleteTrackerItem(userId, date, itemToDelete)
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

                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
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

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        nutritionVM.initializeTrackerForNewDay(userId, date)
        nutritionVM.initializePersonalFoodForNewUser(userId)
    }


}