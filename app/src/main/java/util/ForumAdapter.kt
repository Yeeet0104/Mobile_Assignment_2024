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
import com.example.mobile_assignment.R
import com.example.mobile_assignment.data.Post
import com.example.mobile_assignment.databinding.ItemForumPostBinding
import com.google.firebase.firestore.Blob

class ForumAdapter(val fn: (RecyclerView.ViewHolder, Post) -> Unit = { _, _ -> }) : ListAdapter<Post, ForumAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(a: Post, b: Post) = a.postId == b.postId
        override fun areContentsTheSame(a: Post, b: Post) = a == b
    }

    class ViewHolder(val binding: ItemForumPostBinding) : RecyclerView.ViewHolder(binding.root)

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

        holder.binding.lblPostTitle.text = p.postTitle
        holder.binding.lblPostContent.text = p.postContent
        holder.binding.lblUsername.text = p.username
        holder.binding.lblTimePosted.text = DateUtils.getRelativeTimeSpanString(p.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        fn(holder, p)
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