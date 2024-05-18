package util

import Data.FoodItem
import Data.TrackerItem
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.FoodRvMainBinding
import com.example.mobile_assignment.databinding.FoodRvSearchBinding

class FoodRecordAdapter (
    private val onDeleteClick: (position: Int) -> Unit,
    val fn: (ViewHolder, TrackerItem) -> Unit = { _, _ -> }
): ListAdapter<TrackerItem, FoodRecordAdapter.ViewHolder>(DiffCallback){

    companion object DiffCallback : DiffUtil.ItemCallback<TrackerItem>() {
        override fun areItemsTheSame(a: TrackerItem, b: TrackerItem) = a.foodId == b.foodId
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(a: TrackerItem, b: TrackerItem) = a == b
    }

    class ViewHolder(val binding: FoodRvMainBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(FoodRvMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackerItem = getItem(position)

        // Bind the data to the views
        holder.binding.imageView.setImageBlob(trackerItem.image)
        holder.binding.tvMainFoodId.text = trackerItem.foodId
        holder.binding.tvMainFoodName.text = trackerItem.foodName
        holder.binding.tvSearchFoodCal.text = "${trackerItem.calories} kcal"

        holder.binding.ibDelete.setOnClickListener {
            onDeleteClick(position)
        }

        fn(holder, trackerItem)
    }

}