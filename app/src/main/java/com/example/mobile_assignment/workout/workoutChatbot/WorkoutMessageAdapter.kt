package com.example.mobile_assignment.workout.workoutChatbot

import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemWorkoutChatBotMessageBinding
import com.example.mobile_assignment.workout.Data.Message

class WorkoutMessageAdapter (private val messages: List<Message>) : RecyclerView.Adapter<WorkoutMessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: ItemWorkoutChatBotMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemWorkoutChatBotMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Customize appearance based on whether the message is from the user or the bot
        if (message.role == "user") {
            holder.binding.tvMessage.visibility = View.VISIBLE
            holder.binding.workoutProfilePic.visibility = View.VISIBLE
            holder.binding.tvMessageBot.visibility = View.GONE
            holder.binding.botPIc.visibility = View.GONE
            holder.binding.tvMessage.text = message.content
        } else {
            holder.binding.tvMessageBot.visibility = View.VISIBLE
            holder.binding.tvMessage.visibility = View.GONE
            holder.binding.workoutProfilePic.visibility = View.GONE
            holder.binding.tvMessageBot.text = message.content
            holder.binding.botPIc.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = messages.size
}