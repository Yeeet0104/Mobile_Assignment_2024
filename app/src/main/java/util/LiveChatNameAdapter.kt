package util

import Login.data.AuthVM
import Login.data.User
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemLiveChatBinding
import com.google.firebase.firestore.Blob

class LiveChatNameAdapter(private val auth: AuthVM,
                          private val users: List<User>,
                          private val listener: OnUserClickListener
) : RecyclerView.Adapter<LiveChatNameAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLiveChatBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val sharedPreferences = auth.getPreferences()
        val currentUserId = sharedPreferences.getString("id", "") ?: return

        if (user.id != currentUserId) {
            val bitmap = blobToBitmap(user.photo)
            holder.binding.chatPic.setImageBitmap(bitmap)
            holder.binding.chatName.text = user.username
        } else {
            holder.binding.chatPic.visibility = View.GONE
            holder.binding.chatName.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener.onUserClick(user.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LiveChatNameAdapter.ViewHolder(ItemLiveChatBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false)
    )

    override fun getItemCount() = users.size

    interface OnUserClickListener {
        fun onUserClick(userId: String)
    }

    // Convert Blob to Bitmap
    private fun blobToBitmap(blob: Blob): Bitmap? {
        val bytes = blob.toBytes()
        return if (bytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }
}