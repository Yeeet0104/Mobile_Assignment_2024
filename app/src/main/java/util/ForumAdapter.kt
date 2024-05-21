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
import android.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ItemForumPostBinding
import com.google.firebase.firestore.Blob

class ForumAdapter(private val fragment: Fragment,
                   private val postVM:PostVM,
                   private val editPostClickListener: OnEditPostClickListener,
                   private val commentClickListener: OnCommentClickListener,
                   val fn: (RecyclerView.ViewHolder, Post) -> Unit = { _, _ -> }) : ListAdapter<Post, ForumAdapter.ViewHolder>(Diff){
    private val currentUserId = "user1"
    private val commentCounts = HashMap<String, Int>()
    companion object Diff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(a: Post, b: Post) = a.postId == b.postId
        override fun areContentsTheSame(a: Post, b: Post) = a == b
    }

    class ViewHolder(val binding: ItemForumPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val btnDeletePost: AppCompatImageButton = itemView.findViewById(R.id.btnDeletePost)
        val btnEditPost: AppCompatImageButton = itemView.findViewById(R.id.btnEditPost)
        val btnComment: AppCompatImageButton = itemView.findViewById(R.id.btnComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemForumPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = getItem(position)

        holder.binding.imgUserProfilePost.setImageBitmap(blobToBitmap(p.userProfilePic))

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

        postVM.getCommentCountLiveData(p.postId).observe(fragment.viewLifecycleOwner) { commentCount ->
            holder.binding.lblCommentCount.text = commentCount.toString()
        }


        holder.binding.lblPostTitle.text = p.postTitle
        holder.binding.lblPostContent.text = p.postContent
        holder.binding.lblUsername.text = p.username

        val timePostedText = if (p.isEdited) {
            "Edited ${DateUtils.getRelativeTimeSpanString(p.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
        } else {
            DateUtils.getRelativeTimeSpanString(p.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
        holder.binding.lblTimePosted.text = timePostedText

        if (p.userId == currentUserId) {
            holder.btnDeletePost.visibility = View.VISIBLE
            holder.btnEditPost.visibility = View.VISIBLE
        } else {
            holder.btnDeletePost.visibility = View.GONE
            holder.btnEditPost.visibility = View.GONE
        }

        fn(holder, p)

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

        holder.binding.forumLike.isChecked = p.isLiked
        holder.binding.forumDislike.isChecked = p.isDisliked

        holder.binding.forumLike.setOnCheckedChangeListener { _, isChecked ->
            p.isLiked = isChecked
            // code to update the like count in your database...
        }

        holder.binding.forumDislike.setOnCheckedChangeListener { _, isChecked ->
            p.isDisliked = isChecked
            // code to update the dislike count in your database...
        }
    }

    interface OnEditPostClickListener {
        fun onEditPostClick(postId: String)
    }

    interface OnCommentClickListener {
        fun onCommentClick(postId: String)
    }

    fun setCommentCount(postId: String, commentCount: Int) {
        commentCounts[postId] = commentCount
    }

    private fun blobToBitmap(blob: Blob): Bitmap? {
        val bytes = blob.toBytes()
        return if (bytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }
}