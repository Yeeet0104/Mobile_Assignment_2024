package ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentNutritionMainBinding


class NutritionMain : Fragment() {
    private lateinit var binding: FragmentNutritionMainBinding
    private val nav by lazy { findNavController() }

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

        return binding.root
    }

}