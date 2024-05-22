package com.example.mobile_assignment.workout.workoutChatbot

import com.example.mobile_assignment.workout.Data.OpenAIWorkoutRequest
import com.example.mobile_assignment.workout.Data.OpenAiWorkoutResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface WorkoutOpenAIApi {
    @Headers("Authorization: Bearer sk-proj-NFTqqQZxECpm5RfbNmrDT3BlbkFJCJ16DdXBW94ZA709CD5t")
    @POST("v1/chat/completions")
    fun sendMessage(@Body request: OpenAIWorkoutRequest): Call<OpenAiWorkoutResponse>
}