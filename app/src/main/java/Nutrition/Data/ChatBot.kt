package Nutrition.Data

data class ChatMessage(val role: String, val content: String)

data class OpenAIRequest(val model: String, val messages: List<ChatMessage>)

data class OpenAIResponse(val id: String, val choices: List<Choice>)

data class Choice(val message: ChatMessage)

