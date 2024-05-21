package Forum.ui

import Forum.data.ChatMessage
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.mobile_assignment.databinding.ActivityLiveChatBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import util.LiveChatAdapter
import Login.data.AuthVM
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager

class LiveChatActivity : ComponentActivity() {
    lateinit var binding: ActivityLiveChatBinding
    private val TAG = "LiveChatActivity"
    private val auth: AuthVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLiveChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Firebase.firestore
        val recipientId = intent.getStringExtra("recipientId") ?: ""
        val sharedPreferences = auth.getPreferences()
        val senderId = sharedPreferences.getString("id", "") ?: return

        // Generate a unique chat room ID based on the sender and recipient IDs
        val chatRoomId = if (senderId < recipientId) "$senderId-$recipientId" else "$recipientId-$senderId"
        val messagesRef = db.collection("chat_rooms").document(chatRoomId).collection("messages")

        messagesRef.orderBy("timestamp").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val messages = snapshot?.map { document ->
                document.toObject(ChatMessage::class.java)
            }

            val liveChatAdapter = LiveChatAdapter(auth, messages ?: emptyList())
            binding.rvLiveChat.layoutManager = LinearLayoutManager(this)
            binding.rvLiveChat.adapter = liveChatAdapter
        }

        binding.send.setOnClickListener {
            val text = binding.messageBox.text.toString()
            Log.d(TAG, "messageBox text: $text")
            if (text.isNotEmpty()) {
                val timestamp = System.currentTimeMillis()

                // Create a ChatMessage object
                val message = ChatMessage(text, senderId, recipientId, timestamp)

                // Use the ChatMessage object when adding to Firestore
                messagesRef.add(message).addOnSuccessListener {
                    // Clear the message box after successfully sending the message
                    binding.messageBox.text.clear()
                    Log.d(TAG, "Message sent: $message") // Log the message that was sent
                }.addOnFailureListener { e ->
                    // Log the error if the message could not be sent
                    Log.w(TAG, "Error sending message", e)
                }
            }
        }
    }
}
