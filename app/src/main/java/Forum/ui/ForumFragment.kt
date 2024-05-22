package Forum.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import Forum.data.Post
import com.example.mobile_assignment.databinding.FragmentForumBinding
import com.example.mobile_assignment.R
import Forum.data.PostVM
import Login.data.AuthVM
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.AdapterView
import android.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import util.ForumAdapter

class ForumFragment : Fragment(), ForumAdapter.OnEditPostClickListener, ForumAdapter.OnCommentClickListener {
    private lateinit var binding: FragmentForumBinding
    private lateinit var forumAdapter: ForumAdapter
    private val viewModel: PostVM by activityViewModels()
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)

        // Initialize the adapter and RecyclerView
        forumAdapter = ForumAdapter(this, viewModel, auth,this, this)
        binding.rvForum.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forumAdapter
        }

        viewModel.getPostLD().observe(viewLifecycleOwner) { posts ->
            // Update the adapter
            forumAdapter.submitList(posts)
        }

        binding.searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = true
            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.search(newText)
                return true
            }
        })

        binding.filterPost.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.sort(selectedItem)
                viewModel.updateResult()
                binding.rvForum.layoutManager?.scrollToPosition(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        viewModel.getResultLD().observe(viewLifecycleOwner) { posts ->
            // Update the list in the adapter
            forumAdapter.submitList(posts)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe the data in the ViewModel
        viewModel.postList.observe(viewLifecycleOwner) { posts: List<Post> ->
            // Update the adapter
            forumAdapter.submitList(posts)
        }

        // Observe the comment count for each post
        viewModel.postList.value?.forEach { post ->
            viewModel.getCommentCountLiveData(post.postId).observe(viewLifecycleOwner) { commentCount ->
                forumAdapter.setCommentCount(post.postId, commentCount)
            }
        }

        displayUserPic()
        binding.btnChat.setOnClickListener {
            nav.navigate(R.id.liveChatNameList)
        }

        binding.txtNewPost.setOnClickListener {
            findNavController().navigate(R.id.forumCreatePost)
        }
    }
    private fun displayUserPic(){
        val sharedPreferences = auth.getPreferences()
        val currentUserId= sharedPreferences.getString("id", "")

        if (currentUserId != null) {
            viewModel.getUser(currentUserId).observe(viewLifecycleOwner) { user ->
                if (user?.photo != null) {
                    val bitmap = blobToBitmap(user.photo)
                    binding.imgUserProfileNew.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun blobToBitmap(blob: Blob): Bitmap? {
        val bytes = blob.toBytes()
        return if (bytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }
    override fun onEditPostClick(postId: String) {
        val bundle = Bundle().apply {
            putString("postId", postId)
        }
        findNavController().navigate(R.id.forumEditPost, bundle)
    }

    override fun onCommentClick(postId: String) {
        val bundle = Bundle().apply {
            putString("postId", postId)
        }
        findNavController().navigate(R.id.forumResponse, bundle)
    }
}