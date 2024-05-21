package util

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")  // Base URL for OpenAI API
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: OpenAIApi by lazy {
        retrofit.create(OpenAIApi::class.java)
    }
}
