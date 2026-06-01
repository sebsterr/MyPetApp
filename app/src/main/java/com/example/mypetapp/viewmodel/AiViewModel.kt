package com.example.mypetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class AiViewModel : ViewModel() {

    private val apiKey = "AIzaSyDxWadZmhnXvF3bAHC4GWK8QL3PiBeovMg"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    private var chatSession = generativeModel.startChat()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun initializeChatWithPet(completeInstruction: String) {
        chatSession = generativeModel.startChat(
            history = listOf(
                content(role = "user") {
                    text(completeInstruction)
                },
                content(role = "model") {
                    text("Hello! I am PetAI. I now remember your pet's profile and I am ready for a prompt conversation.")
                }
            )
        )

        _messages.value = listOf(
            ChatMessage(
                text = "Hello! I am now connected to your pet's profile. How can I help you today?",
                isUser = false
            )
        )
    }

    fun askGemini(userQuestion: String) {
        if (userQuestion.isBlank()) return

        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage(text = userQuestion, isUser = true))
        _messages.value = currentList

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chatSession.sendMessage(userQuestion)
                val aiText = response.text ?: "Could not generate a response."

                val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage(text = aiText, isUser = false))
                _messages.value = updatedList

            } catch (e: Exception) {
                val errorList = _messages.value.toMutableList()
                errorList.add(ChatMessage(text = "Error: ${e.localizedMessage}", isUser = false))
                _messages.value = errorList
            } finally {
                _isLoading.value = false
            }
        }
    }
}