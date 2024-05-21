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
import Login.data.AuthVM
import androidx.lifecycle.lifecycleScope
import android.Manifest
import com.example.mobile_assignment.R
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch
import util.cropToBlobNullable
import util.errorDialog
import util.toast

class ForumCreatePost : Fragment() {

    private lateinit var binding : FragmentForumCreatePostBinding
    private val viewModel: PostVM by activityViewModels()
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        binding.imgPost.setImageBitmap(bitmap)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForumCreatePostBinding.inflate(inflater, container, false)

        // Request camera permission
        val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, launch the camera
                takePicture.launch(null)
            } else {
                // Permission denied, show a message to the user
                toast("Camera permission is required to take a photo")
            }
        }

        reset()
        binding.btnReset.setOnClickListener  { reset() }
        binding.imgPost.setOnClickListener  { select() }
        binding.btnAddPost.setOnClickListener { submit() }
        binding.btnCamera.setOnClickListener { requestCameraPermission.launch(Manifest.permission.CAMERA) }
        binding.selectBtn.setOnClickListener { selectImage() }
        return binding.root
    }

    private fun reset() {
        binding.edtPostTitle.text.clear()
        binding.edtPostContent.text.clear()
        binding.imgPost.setImageDrawable(null)
        binding.edtPostTitle.requestFocus()
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        binding.imgPost.setImageURI(it)
    }

    private fun select() {
        getContent.launch("image/*")
    }
    private fun submit() {
        lifecycleScope.launch {
            val postImgBlob = binding.imgPost.cropToBlobNullable(800, 750)
            val sharedPreferences = auth.getPreferences()
            val currentUserId= sharedPreferences.getString("id", "")
            val currentUsername = sharedPreferences.getString("username", "")
            val currentUserPhotoBlob = auth.getUserPhotoBlob() ?: Blob.fromBytes(ByteArray(0))

            val p = Post(
                userId = currentUserId ?: "",
                postUsername = currentUsername ?: "",
                postTitle = binding.edtPostTitle.text.toString().trim(),
                postContent = binding.edtPostContent.text.toString().trim(),
                timePosted = System.currentTimeMillis(),
                postImg = postImgBlob,
                userProfilePic = currentUserPhotoBlob
            )

            val e = viewModel.validate(p, false)
            if (e.isNotEmpty()) {
                errorDialog(e)
                return@launch
            }

            viewModel.set(p)
            findNavController().navigate(R.id.forumHome)
            toast("New post created")
        }
    }

    private fun selectImage() {
        getContent.launch("image/*")
    }
}