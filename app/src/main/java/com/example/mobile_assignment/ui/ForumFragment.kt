package com.example.mobile_assignment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.data.Post
import com.example.mobile_assignment.databinding.FragmentForumBinding
import com.example.mobile_assignment.R
import com.example.mobile_assignment.data.PostVM
import util.ForumAdapter

class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding
    private lateinit var forumAdapter: ForumAdapter
    private val viewModel: PostVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)

        // Initialize the adapter and RecyclerView
        forumAdapter = ForumAdapter()
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

        binding.txtNewPost.setOnClickListener {
            findNavController().navigate(R.id.action_forumHome_to_forumCreatePost)
        }
    }
}