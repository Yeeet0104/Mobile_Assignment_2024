package Nutrition.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NutritionVMFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionVM::class.java)) {
            return NutritionVM(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}