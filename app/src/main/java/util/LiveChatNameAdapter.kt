package util

import Login.data.AuthVM
import Login.data.User
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemLiveChatBinding
import com.google.firebase.firestore.Blob

class LiveChatNameAdapter(
    private val auth: AuthVM,
    private val listener: OnUserClickListener
) : ListAdapter<User, LiveChatNameAdapter.ViewHolder>(UserDiffCallback()) {

    class ViewHolder(val binding: ItemLiveChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
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

        holder.binding.root.setOnClickListener {
            listener.onUserClick(user.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLiveChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

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

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}