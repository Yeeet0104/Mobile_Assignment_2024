package util

import Nutrition.Data.FoodItem
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.example.mobile_assignment.databinding.FoodRvSearchBinding


class FoodAdapter (
    val fn: (ViewHolder, FoodItem) -> Unit = { _, _ -> }
): ListAdapter<FoodItem, FoodAdapter.ViewHolder>(DiffCallback){

    companion object DiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(a: FoodItem, b: FoodItem) = a.foodId == b.foodId
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(a: FoodItem, b: FoodItem) = a == b
    }

    class ViewHolder(val binding: FoodRvSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(FoodRvSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodItem = getItem(position)

        // Bind the data to the views
        holder.binding.imageView.setImageBlob(foodItem.image)
        holder.binding.tvFoodId.text = foodItem.foodId
        holder.binding.tvSearchFoodName.text = foodItem.foodName
        holder.binding.tvSearchFoodCal.text = "${foodItem.calories} kcal"

        fn(holder, foodItem)
    }

}