package com.example.mobile_assignment.workout.workoutChatbot

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_assignment.workout.Data.Message
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class WorkoutChatBotWM (application: Application) : AndroidViewModel(application) {
    private val repository = ChatBotRepository()

    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> get() = _messages

    fun sendMessage(prompt: String) {
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(Message(role = "user", content = prompt)) // User's message
        _messages.value = currentMessages

        repository.sendMessage(prompt) { reply ->
            currentMessages.add(reply) // Bot's response
            _messages.postValue(currentMessages)
        }
    }
}