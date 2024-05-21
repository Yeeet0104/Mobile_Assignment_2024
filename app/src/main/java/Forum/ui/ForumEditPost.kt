package Forum.ui

import Forum.data.Post
import Forum.data.PostVM
import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentForumEditPostBinding
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch
import util.cropToBlobNullable
import util.errorDialog
import util.toBitmap
import util.toast

class ForumEditPost : Fragment() {
    private var postId: String? = null
    private lateinit var binding : FragmentForumEditPostBinding
    private val viewModel: PostVM by activityViewModels()
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (postId != null) {
            // Fetch the Post object from database using postId
            val post = viewModel.getPost(postId!!)

            // Populate the fields with the data from the Post object
            binding.updtPostTitle.setText(post?.postTitle)
            binding.updtPostContent.setText(post?.postContent)
            post?.postImg?.let {
                binding.imgUpdatePic.setImageBitmap(it.toBitmap())
            }
        }
        binding.btnUpdatePost.setOnClickListener { update() }
        binding.imgUpdatePic.setOnClickListener { selectImage() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumEditPostBinding.inflate(inflater, container, false)

        binding.btnUpdatePost.setOnClickListener { update() }
        binding.imgUpdatePic.setOnClickListener { selectImage() }

        return binding.root
    }

    private fun update() {
        lifecycleScope.launch {
            val postImgBlob = binding.imgUpdatePic.cropToBlobNullable(800, 750)
            val sharedPreferences = auth.getPreferences()
            val currentUserId = sharedPreferences.getString("id", "")
            val currentUsername = sharedPreferences.getString("username", "")
            val currentUserPhotoBlob = auth.getUserPhotoBlob() ?: Blob.fromBytes(ByteArray(0))

            val p = Post(
                postId = postId!!,
                userId = currentUserId ?: "",
                postUsername = currentUsername ?: "",
                postTitle = binding.updtPostTitle.text.toString().trim(),
                postContent = binding.updtPostContent.text.toString().trim(),
                timePosted = System.currentTimeMillis(),
                postImg = postImgBlob,
                isEdited = true,
                userProfilePic = currentUserPhotoBlob
            )

            val e = viewModel.validate(p, false)
            if (e.isNotEmpty()) {
                errorDialog(e)
                return@launch
            }

            viewModel.set(p)
            nav.navigate(R.id.forumHome)
            toast("Post updated")
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        binding.imgUpdatePic.setImageURI(it)
    }

    private fun selectImage() {
        getContent.launch("image/*")
    }

}