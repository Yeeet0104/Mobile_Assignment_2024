package com.example.mobile_assignment.workout.workoutChatbot

import com.example.mobile_assignment.workout.Data.Message
import com.example.mobile_assignment.workout.Data.OpenAIWorkoutRequest
import com.example.mobile_assignment.workout.Data.OpenAiWorkoutResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatBotRepository {
    private val api = WorkoutRetrofitInstance.api

    fun sendMessage(prompt: String, callback: (Message) -> Unit) {
        val initialContext = Message(role = "system", content = "You are a fitness expert. Provide workout and fitness advice.")
        val userMessage = Message(role = "user", content = prompt)
        val request = OpenAIWorkoutRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(initialContext, userMessage)
        )
        api.sendMessage(request).enqueue(object : Callback<OpenAiWorkoutResponse> {
            override fun onResponse(call: Call<OpenAiWorkoutResponse>, response: Response<OpenAiWorkoutResponse>) {
                if (response.isSuccessful) {
                    val botMessageContent = response.body()?.choices?.firstOrNull()?.message?.content ?: "No response"
                    callback(Message(role = "assistant", content = botMessageContent))
                } else {
                    callback(Message(role = "assistant", content = "Error: ${response.errorBody()?.string()}"))
                }
            }

            override fun onFailure(call: Call<OpenAiWorkoutResponse>, t: Throwable) {
                callback(Message(role = "assistant", content = "Exception: ${t.message ?: t.toString()}"))
            }
        })
    }
}