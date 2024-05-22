package com.example.mobile_assignment.workout.workoutChatbot

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
object WorkoutRetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: WorkoutOpenAIApi by lazy {
        retrofit.create(WorkoutOpenAIApi::class.java)
    }
}