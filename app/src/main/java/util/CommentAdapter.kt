package util

import com.example.mobile_assignment.R
import Forum.data.Comment
import Forum.data.PostVM
import android.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_assignment.databinding.ItemForumPostBinding
import com.example.mobile_assignment.databinding.ItemForumUserResponseBinding

class CommentAdapter(private val comments: List<Comment>, private val postId: String, private val viewModel: PostVM) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemForumUserResponseBinding) : RecyclerView.ViewHolder(binding.root){
        val btnDeleteComment: AppCompatImageButton = itemView.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForumUserResponseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.lblNameResponse.text = comment.userId
        holder.binding.lblResponseContent.text = comment.content
        holder.binding.lblTimeResponse.text = DateUtils.getRelativeTimeSpanString(comment.timePosted, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)

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
    }

    override fun getItemCount() = comments.size

}