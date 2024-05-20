package ui

import Data.NutritionVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionSearchBinding
import util.FoodAdapter

class NutritionSearch : Fragment() {
    private lateinit var binding: FragmentNutritionSearchBinding
    private val nav by lazy { findNavController() }
    private val nutritionViewModel: NutritionVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionSearchBinding.inflate(inflater, container, false)

        binding.searchView.requestFocus()

        val adapter = FoodAdapter { holder, foodItem ->
            holder.binding.root.setOnClickListener {
                detail(foodItem.foodId)
            }
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        nutritionViewModel.getFoodLD().observe(viewLifecycleOwner) { foodItems ->
            adapter.submitList(foodItems)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = true
            override fun onQueryTextChange(newText: String): Boolean {
                nutritionViewModel.search(newText)
                return false
            }
        })

        nutritionViewModel.getResultLD().observe(viewLifecycleOwner) { filteredFoodItems ->
            adapter.submitList(filteredFoodItems)
        }


        return binding.root
    }


    override fun onResume() {
        super.onResume()
        // Reset search when fragment is resumed
        binding.searchView.setQuery("", false)
        nutritionViewModel.search("")
    }



    private fun detail(foodId: String) {
        nav.navigate(R.id.nutritionDetails, bundleOf(
            "foodId" to foodId
        ))
    }
}