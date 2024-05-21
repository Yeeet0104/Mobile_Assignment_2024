package util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import Forum.data.Post
import Forum.data.PostVM
import Login.data.AuthVM
import android.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ItemForumPostBinding
import com.google.firebase.firestore.Blob

// ForumAdapter is responsible for displaying each item in the list
class ForumAdapter(
    private val fragment: Fragment,
    private val postVM: PostVM,
    private val auth: AuthVM,
    private val editPostClickListener: OnEditPostClickListener,
    private val commentClickListener: OnCommentClickListener,
    val fn: (RecyclerView.ViewHolder, Post) -> Unit = { _, _ -> }
) : ListAdapter<Post, ForumAdapter.ViewHolder>(Diff) {
    // Store comment counts for each post
    private val commentCounts = HashMap<String, Int>()

    // Define how to compare Post items for DiffUtil
    companion object Diff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(a: Post, b: Post) = a.postId == b.postId
        override fun areContentsTheSame(a: Post, b: Post) = a == b
    }

    // ViewHolder holds the views for each item in the list
    class ViewHolder(val binding: ItemForumPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val btnDeletePost: AppCompatImageButton = itemView.findViewById(R.id.btnDeletePost)
        val btnEditPost: AppCompatImageButton = itemView.findViewById(R.id.btnEditPost)
        val btnComment: AppCompatImageButton = itemView.findViewById(R.id.btnComment)
    }

    // Create new ViewHolder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemForumPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // Bind data to the views for each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = getItem(position)

        // Get currentUserId
        val sharedPreferences = auth.getPreferences()
        val currentUserId = sharedPreferences.getString("id", "") ?: return

        // Display user pic and username
        postVM.getUser(p.userId).observe(fragment.viewLifecycleOwner) { user ->
            if (user?.photo != null) {
                val bitmap = blobToBitmap(user.photo)
                holder.binding.imgUserProfilePost.setImageBitmap(bitmap)
            }
            if (user?.username != null) {
                holder.binding.lblUsername.text = user.username
            }
        }

        // Display post image if it exists
        if (p.postImg != null) {
            val postImageBitmap = blobToBitmap(p.postImg)
            if (postImageBitmap != null) {
                holder.binding.showPostImg.setImageBitmap(postImageBitmap)
                holder.binding.showPostImg.visibility = View.VISIBLE
            } else {
                holder.binding.showPostImg.visibility = View.GONE
            }
        } else {
            holder.binding.showPostImg.visibility = View.GONE
        }

        // Display comment count for each post
        postVM.getCommentCountLiveData(p.postId).observe(fragment.viewLifecycleOwner) { commentCount ->
            holder.binding.lblCommentCount.text = commentCount.toString()
        }

        // Display post title and content
        holder.binding.lblPostTitle.text = p.postTitle
        holder.binding.lblPostContent.text = p.postContent

        // Display time posted
        val timePostedText = if (p.isEdited) {
            "Edited" + DateUtils.getRelativeTimeSpanString(p.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        } else {
            DateUtils.getRelativeTimeSpanString(p.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
        holder.binding.lblTimePosted.text = timePostedText

        // Show delete and edit buttons only for posts created by current user
        if (p.userId == currentUserId) {
            holder.btnDeletePost.visibility = View.VISIBLE
            holder.btnEditPost.visibility = View.VISIBLE
        } else {
            holder.btnDeletePost.visibility = View.GONE
            holder.btnEditPost.visibility = View.GONE
        }

        // Call function passed in constructor
        fn(holder, p)

        // Set up click listeners for buttons
        holder.btnDeletePost.setOnClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { _, _ ->
                    postVM.delete(p)
                    fragment.toast("Post deleted successfully.")
                }
                .setNegativeButton("No", null)
                .show()
        }

        holder.btnEditPost.setOnClickListener {
            editPostClickListener.onEditPostClick(p.postId)
        }

        holder.btnComment.setOnClickListener {
            commentClickListener.onCommentClick(p.postId)
        }

        // Check if the current user has liked or disliked the post
        holder.binding.forumLike.isChecked = currentUserId in p.likedBy
        holder.binding.forumDislike.isChecked = currentUserId in p.dislikedBy

        // Display like and dislike count for each post
        holder.binding.forumLikeCount.text = p.likedBy.size.toString()
        holder.binding.forumDislikeCount.text = p.dislikedBy.size.toString()

        holder.binding.forumLike.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                p.likedBy.add(currentUserId)
            } else {
                p.likedBy.remove(currentUserId)
            }
            postVM.updatePost(p)
            holder.binding.forumLikeCount.text = p.likedBy.size.toString()
        }

        holder.binding.forumDislike.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                p.dislikedBy.add(currentUserId)
            } else {
                p.dislikedBy.remove(currentUserId)
            }
            postVM.updatePost(p)
            holder.binding.forumDislikeCount.text = p.dislikedBy.size.toString()
        }
    }

    // Interfaces for click listeners
    interface OnEditPostClickListener {
        fun onEditPostClick(postId: String)
    }

    interface OnCommentClickListener {
        fun onCommentClick(postId: String)
    }

    // Set comment count for a post
    fun setCommentCount(postId: String, commentCount: Int) {
        commentCounts[postId] = commentCount
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