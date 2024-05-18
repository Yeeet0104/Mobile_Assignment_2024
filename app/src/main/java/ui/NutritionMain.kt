package ui

import Data.NutritionVM
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionMainBinding
import util.FoodAdapter
import util.FoodRecordAdapter
import util.toast
import java.time.LocalDateTime


class NutritionMain : Fragment() {
    private lateinit var binding: FragmentNutritionMainBinding
    private val nav by lazy { findNavController() }
    private val nutritionVM by activityViewModels<NutritionVM>()

    //DATABASE ITEM TEST
    private var userId = "A001"
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

        nutritionVM.getDailyTrackerReference(userId, date).observe(viewLifecycleOwner) { trackerItems ->
            binding.rvMain.adapter = FoodRecordAdapter(
                onDeleteClick = { position ->
                    // Implement deletion logic here using position
                    // For example:
                    val itemToDelete = trackerItems[position]
                    nutritionVM.deleteTrackerItem(itemToDelete)
                    toast("Deleted ${itemToDelete.foodName}")
                }
            ) { holder, foodItem ->
                holder.binding.root.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("foodId", foodItem.foodId)
                    nav.navigate(R.id.nutritionDetails, bundle)
                }
            }

            (binding.rvMain.adapter as FoodRecordAdapter).submitList(trackerItems)
        }

        return binding.root
    }

}