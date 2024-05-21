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
import util.ForumAdapter

class ForumFragment : Fragment(), ForumAdapter.OnEditPostClickListener, ForumAdapter.OnCommentClickListener {
    private lateinit var binding: FragmentForumBinding
    private lateinit var forumAdapter: ForumAdapter
    private val viewModel: PostVM by activityViewModels()
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)

        // Initialize the adapter and RecyclerView
        forumAdapter = ForumAdapter(this, viewModel, this, this)
        binding.rvForum.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forumAdapter
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

        binding.txtNewPost.setOnClickListener {
            findNavController().navigate(R.id.forumCreatePost)
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