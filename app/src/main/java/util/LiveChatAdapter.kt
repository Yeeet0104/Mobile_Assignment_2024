package util

import Forum.data.ChatMessage
import Login.data.AuthVM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.LiveChatReceiveBinding
import com.example.mobile_assignment.databinding.LiveChatSentBinding

class LiveChatAdapter(private val auth: AuthVM, private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    abstract class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class SentMessageHolder(private val binding: LiveChatSentBinding) : ChatViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.txtSentMessage.text = message.text
        }
    }

    class ReceivedMessageHolder(private val binding: LiveChatReceiveBinding) : ChatViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.txtReceiveMessage.text = message.text
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sharedPreferences = auth.getPreferences()
        val currentUserId = sharedPreferences.getString("id", "") ?: return VIEW_TYPE_RECEIVED
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = LiveChatSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageHolder(binding)
        } else {
            val binding = LiveChatReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size
}