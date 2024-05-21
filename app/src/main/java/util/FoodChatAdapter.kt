package util

import nutrition.data.ChatMessage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.FoodMessageItemBinding

class FoodChatAdapter(private val chatMessages: List<ChatMessage>) : RecyclerView.Adapter<FoodChatAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FoodMessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FoodMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        if (chatMessage.role == "user") {
            holder.binding.tvUserMessage.text = chatMessage.content
            holder.binding.tvUserMessage.visibility = View.VISIBLE
            holder.binding.tvBotMessage.visibility = View.GONE
        } else {
            holder.binding.tvBotMessage.text = chatMessage.content
            holder.binding.tvBotMessage.visibility = View.VISIBLE
            holder.binding.tvUserMessage.visibility = View.GONE
        }
    }

    override fun getItemCount() = chatMessages.size
}

