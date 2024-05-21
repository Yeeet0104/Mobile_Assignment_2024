package Forum.ui

import Forum.data.Comment
import Forum.data.PostVM
import Login.data.AuthVM
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentForumResponseBinding
import com.google.firebase.firestore.Blob
import util.CommentAdapter
import util.toBitmap
import util.toast

class ForumResponse : Fragment() {
    private var postId: String? = null
    private val nav by lazy { findNavController() }
    private lateinit var binding : FragmentForumResponseBinding
    private val viewModel: PostVM by activityViewModels()
    private val auth: AuthVM by activityViewModels()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForumResponseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedPreferences = auth.getPreferences()
        currentUserId = sharedPreferences.getString("id", "")

        arguments?.let {
            postId = it.getString("postId")
        }

        if (postId != null) {
            populateFields(postId!!)
        }

        binding.btnResponse.setOnClickListener { submitComment() }

        postId?.let {
            commentAdapter = CommentAdapter(emptyList(), it,auth, viewModel, viewLifecycleOwner)
        }

        binding.rvResponse.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
        // Display the username and picture of the currently logged in user
        val currentUserIdLocal = currentUserId
        if (currentUserIdLocal != null) {
            viewModel.getUser(currentUserIdLocal).observe(viewLifecycleOwner) { user ->
                if (user?.photo != null) {
                    val bitmap = blobToBitmap(user.photo)
                    binding.imageView5.setImageBitmap(bitmap)
                }
                if (user?.username != null) {
                    binding.nameWriteResponse.text = user.username
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId?.let { postId ->
            viewModel.getComments(postId).observe(viewLifecycleOwner) { comments: List<Comment> ->
                // Update the adapter
                commentAdapter = CommentAdapter(comments, postId, auth, viewModel,viewLifecycleOwner)
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

        // Display post image if it exists
        if (post?.postImg != null) {
            val postImageBitmap = blobToBitmap(post.postImg)
            if (postImageBitmap != null) {
                binding.imageView6.setImageBitmap(postImageBitmap)
                binding.imageView6.visibility = View.VISIBLE
            } else {
                binding.imageView6.visibility = View.GONE
            }
        } else {
            binding.imageView6.visibility = View.GONE
        }

        // Fetch the user data associated with the post
        post?.userId?.let { userId ->
            viewModel.getUser(userId).observe(viewLifecycleOwner) { user ->
                // Update the user image, name, and time
                if (user?.photo != null) {
                    val bitmap = user.photo.toBitmap()
                    binding.responseUserImg.setImageBitmap(bitmap)
                }
                binding.nameResponse.text = user?.username
                binding.timeResponse.text = post.timePosted?.let {
                    DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                }
            }
        }

        // Update the like and dislike count
        binding.responsesLikeCount.text = post?.likedBy?.size.toString()
        binding.responsesDislikeCount.text = post?.dislikedBy?.size.toString()

        // Update the like and dislike buttons
        binding.commentPostLike.isChecked = post?.likedBy?.contains(currentUserId) ?: false
        binding.commentPostDislike.isChecked = post?.dislikedBy?.contains(currentUserId) ?: false

        // Add listeners to the like and dislike buttons
        binding.commentPostLike.setOnCheckedChangeListener { _, isChecked ->
            currentUserId?.let { userId ->
                if (isChecked) {
                    post?.likedBy?.add(userId)
                } else {
                    post?.likedBy?.remove(userId)
                }
                post?.let { viewModel.updatePost(it) }
                binding.responsesLikeCount.text = post?.likedBy?.size.toString()
            }
        }

        binding.commentPostDislike.setOnCheckedChangeListener { _, isChecked ->
            currentUserId?.let { userId ->
                if (isChecked) {
                    post?.dislikedBy?.add(userId)
                } else {
                    post?.dislikedBy?.remove(userId)
                }
                post?.let { viewModel.updatePost(it) }
                binding.responsesDislikeCount.text = post?.dislikedBy?.size.toString()
            }
        }
    }
    private fun submitComment() {
        val commentContent = binding.lblWhatComment.text.toString()

        if (commentContent.isNotBlank()) {
            currentUserId?.let { userId ->
                val comment = Comment("", userId, commentContent, System.currentTimeMillis())

                    postId?.let {
                        viewModel.addComment(it, comment)
                        binding.lblWhatComment.setText("")

                        // Refresh comments list
                        viewModel.getComments(it).observe(viewLifecycleOwner) { comments: List<Comment> ->
                            // Update the adapter
                            commentAdapter = CommentAdapter(comments, it, auth, viewModel, viewLifecycleOwner)
                            binding.rvResponse.adapter = commentAdapter
                        }
                    }
            }
        }
        toast("Comment added.")
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