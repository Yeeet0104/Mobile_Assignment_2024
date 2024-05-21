package Nutrition

import Nutrition.Data.ChatMessage
import Nutrition.Data.OpenAIRequest
import Nutrition.Data.OpenAIResponse
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentNutritionChatbotBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import util.FoodChatAdapter
import util.RetrofitInstance

class NutritionChatbot : Fragment() {
    private lateinit var binding: FragmentNutritionChatbotBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private val adapter = FoodChatAdapter(chatMessages)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionChatbotBinding.inflate(inflater, container, false)
        binding.rvChat.layoutManager = LinearLayoutManager(context)
        binding.rvChat.adapter = adapter

        binding.ibSubmit.setOnClickListener {
            val userMessage = binding.edtQuestion.text.toString()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, true)
                sendMessageToBot(userMessage)
                binding.edtQuestion.text.clear()
            }
        }

        return binding.root
    }

    private fun addMessage(message: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(if (isUser) "user" else "bot", message))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChat.scrollToPosition(chatMessages.size - 1)
    }

    private fun sendMessageToBot(message: String) {
        val openAIRequest = OpenAIRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(ChatMessage("user", message))
        )

        RetrofitInstance.api.sendMessage(openAIRequest).enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                if (response.isSuccessful) {
                    response.body()?.choices?.get(0)?.message?.content?.let { botMessage ->
                        addMessage(botMessage, false)
                    }
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}
