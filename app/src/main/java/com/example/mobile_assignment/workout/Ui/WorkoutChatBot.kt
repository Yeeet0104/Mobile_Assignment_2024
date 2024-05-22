package com.example.mobile_assignment.workout.Ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ActivityWorkoutExpertChatBotBinding
import com.example.mobile_assignment.databinding.FragmentWorkoutChatBotBinding
import com.example.mobile_assignment.workout.workoutChatbot.WorkoutChatBotWM
import com.example.mobile_assignment.workout.workoutChatbot.WorkoutMessageAdapter

class workoutChatBot : Fragment() {
    private lateinit var binding: FragmentWorkoutChatBotBinding
    private lateinit var viewModel: WorkoutChatBotWM
    private lateinit var messageAdapter: WorkoutMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkoutChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(WorkoutChatBotWM::class.java)

        messageAdapter = WorkoutMessageAdapter(viewModel.messages.value ?: mutableListOf())
        binding.rvChat.adapter = messageAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(context)

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.notifyDataSetChanged()
            binding.rvChat.scrollToPosition(messages.size - 1) // Scroll to the latest message
        }

        binding.sendButton.setOnClickListener {
            val message = binding.userInput.text.toString()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.userInput.text.clear()
            }
        }
    }
}