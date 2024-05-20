package Forum.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import Forum.data.Post
import com.example.mobile_assignment.databinding.FragmentForumCreatePostBinding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import Forum.data.PostVM
import com.example.mobile_assignment.R
import util.cropToBlobNullable
import util.errorDialog

class ForumCreatePost : Fragment() {

    private lateinit var binding : FragmentForumCreatePostBinding
    private val viewModel: PostVM by activityViewModels()
    private val nav by lazy { findNavController() }
    val dummyUserIds = listOf("user1", "user2", "user3", "user4", "user5")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForumCreatePostBinding.inflate(inflater, container, false)

        reset()
        //binding.btnReset.setOnClickListener  { reset() }
        binding.imgPost.setOnClickListener  { select() }
        binding.btnAddPost.setOnClickListener { submit() }
        binding.btnCamera.setOnClickListener { takePicture.launch(null) }
        return binding.root
    }

    private fun reset() {
        binding.edtPostTitle.text.clear()
        binding.edtPostContent.text.clear()
        binding.imgPost.setImageDrawable(null)
        binding.edtPostTitle.requestFocus()
    }

    // TODO: Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        binding.imgPost.setImageURI(it)
    }

    private fun select() {
        // TODO: Select file
        getContent.launch("image/*")
    }
    private fun submit() {
        val postImgBlob = binding.imgPost.cropToBlobNullable(300, 300) // Use nullable cropToBlob method
        val randomUserId = dummyUserIds.random()
        val p = Post(
            userId = "user1",
            postTitle = binding.edtPostTitle.text.toString().trim(),
            postContent = binding.edtPostContent.text.toString().trim(),
            username = "Dummy User",
            timePosted = System.currentTimeMillis(),
            postImg = postImgBlob
        )

        val e = viewModel.validate(p, false)
        if (e.isNotEmpty()) {
            errorDialog(e)
            return
        }

        viewModel.set(p)
        findNavController().navigate(R.id.forumHome)
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        binding.imgPost.setImageBitmap(bitmap)
    }
}