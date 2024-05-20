package Forum.ui

import Forum.data.Comment
import Forum.data.PostVM
import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentForumEditPostBinding
import com.example.mobile_assignment.databinding.FragmentForumResponseBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import util.CommentAdapter
import util.toBitmap

class ForumResponse : Fragment() {
    private var postId: String? = null
    private val nav by lazy { findNavController() }
    private lateinit var binding : FragmentForumResponseBinding
    private val viewModel: PostVM by activityViewModels()
    private val currentUserId = "user1"
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForumResponseBinding.inflate(inflater, container, false)

        arguments?.let {
            postId = it.getString("postId")
        }

        if (postId != null) {
            populateFields(postId!!)
        }

        binding.btnResponse.setOnClickListener { submitComment() }

        postId?.let {
            commentAdapter = CommentAdapter(emptyList(), it, viewModel)
        }

        binding.rvResponse.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId?.let { postId ->
            viewModel.getComments(postId).observe(viewLifecycleOwner) { comments: List<Comment> ->
                // Update the adapter
                commentAdapter = CommentAdapter(comments, postId, viewModel)
                binding.rvResponse.adapter = commentAdapter
                binding.responsesCount.text = comments.size.toString()
            }
        }
    }

    private fun populateFields(postId: String) {
        val post = viewModel.getPost(postId)
        binding.lblResponseTitle.text = post?.postTitle
        binding.responseContent.text = post?.postContent
        binding.timeResponse.text = post?.timePosted?.let {
            DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
        if (post?.postImg != null) {
            binding.imageView6.setImageBitmap(post.postImg.toBitmap())
        } else {
            binding.imageView6.visibility = View.GONE
        }

        val currentUsername = viewModel.currentUserId
        binding.nameWriteResponse.text = currentUsername
    }

    private fun submitComment() {
        val commentContent = binding.lblWhatComment.text.toString()

        if (commentContent.isNotBlank()) {
            val comment = Comment("", currentUserId, commentContent, System.currentTimeMillis())
            if (postId == null) {
                Log.d("ForumResponse", "postId is null, not calling addComment")
            } else {
                postId?.let {
                    viewModel.addComment(it, comment)
                    binding.lblWhatComment.setText("")

                    // Refresh comments list
                    viewModel.getComments(it).observe(viewLifecycleOwner) { comments: List<Comment> ->
                        // Update the adapter
                        commentAdapter = CommentAdapter(comments, it,  viewModel)
                        binding.rvResponse.adapter = commentAdapter
                    }
                }
            }
        }
    }
}