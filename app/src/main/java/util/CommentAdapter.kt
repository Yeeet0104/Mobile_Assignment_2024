package util

import com.example.mobile_assignment.R
import Forum.data.Comment
import Forum.data.PostVM
import Login.data.AuthVM
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemForumPostBinding
import com.example.mobile_assignment.databinding.ItemForumUserResponseBinding
import java.sql.Blob

class CommentAdapter(
    private val comments: List<Comment>,
    private val postId: String,
    private val authVM: AuthVM,
    private val viewModel: PostVM,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    class ViewHolder(val binding: ItemForumUserResponseBinding) : RecyclerView.ViewHolder(binding.root){
        val btnDeleteComment: AppCompatImageButton = itemView.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForumUserResponseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.lblResponseContent.text = comment.content
        holder.binding.lblTimeResponse.text = DateUtils.getRelativeTimeSpanString(comment.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)

        // Fetch the user data associated with the comment
        viewModel.getUser(comment.userId).observe(lifecycleOwner) { user ->
            // Update the user image and name
            if (user?.photo != null) {
                val bitmap = blobToBitmap(user.photo)
                holder.binding.imageView4.setImageBitmap(bitmap)
            }
            if (user?.username != null) {
                holder.binding.lblNameResponse.text = user.username
            }
        }

        val currentUserId: String = authVM.getPreferences().getString("id", "") ?: ""
        if (currentUserId == comment.userId) {
            holder.btnDeleteComment.visibility = View.VISIBLE
        } else {
            holder.btnDeleteComment.visibility = View.GONE
        }

        holder.btnDeleteComment.setOnClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteComment(postId, comment)
                    Toast.makeText(it.context, "Comment deleted successfully.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Update the like and dislike count
        holder.binding.responseLikeCount.text = comment.likedBy?.size.toString()
        holder.binding.responseDislikeCount.text = comment.dislikedBy?.size.toString()

        // Update the like and dislike buttons
        holder.binding.commentLike.isChecked = comment.likedBy?.contains(currentUserId) ?: false
        holder.binding.commentDislike.isChecked = comment.dislikedBy?.contains(currentUserId) ?: false

        // Add listeners to the like and dislike buttons
        holder.binding.commentLike.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                comment.likedBy?.add(currentUserId)
            } else {
                comment.likedBy?.remove(currentUserId)
            }
            viewModel.updateComment(postId, comment)
            holder.binding.responseLikeCount.text = comment.likedBy?.size.toString()
        }

        holder.binding.commentDislike.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                comment.dislikedBy?.add(currentUserId)
            } else {
                comment.dislikedBy?.remove(currentUserId)
            }
            viewModel.updateComment(postId, comment)
            holder.binding.responseDislikeCount.text = comment.dislikedBy?.size.toString()
        }
    }

    override fun getItemCount() = comments.size

    private fun blobToBitmap(blob: com.google.firebase.firestore.Blob): Bitmap? {
        val bytes = blob.toBytes()
        return if (bytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }
}