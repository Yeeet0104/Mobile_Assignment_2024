package util

import Nutrition.Data.OpenAIRequest
import Nutrition.Data.OpenAIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
interface OpenAIApi {
    @Headers("Authorization: Bearer sk-proj-ZBhCYEhfzKNsZ1BNg1p1T3BlbkFJOm1ntOLDgDSovEJy0b5v")
    @POST("v1/chat/completions")
    fun sendMessage(@Body request: OpenAIRequest): Call<OpenAIResponse>
}