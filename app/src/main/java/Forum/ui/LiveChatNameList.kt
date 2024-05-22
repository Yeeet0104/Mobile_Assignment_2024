package Forum.ui

import Login.data.AuthVM
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentLiveChatNameListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import util.LiveChatNameAdapter

class LiveChatNameList : Fragment(),
    LiveChatNameAdapter.OnUserClickListener{
    private lateinit var binding: FragmentLiveChatNameListBinding
    private lateinit var liveChatNameAdapter: LiveChatNameAdapter
    private val auth: AuthVM by activityViewModels()

    override fun onUserClick(userId: String) {
        val intent = Intent(context, LiveChatActivity::class.java)
        intent.putExtra("recipientId", userId)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLiveChatNameListBinding.inflate(inflater, container, false)

        // Initialize liveChatNameAdapter before setting up the observer
        liveChatNameAdapter = LiveChatNameAdapter(auth, this)
        binding.rvChatNameList.adapter = liveChatNameAdapter

        auth.filteredUsers.observe(viewLifecycleOwner) { users ->
            // Update your RecyclerView adapter here
            liveChatNameAdapter.submitList(users)
        }

// Set up the SearchView to call viewModel.searchUser whenever the query text changes
        binding.svName.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewLifecycleOwner.lifecycleScope.launch {
                    auth.searchUser(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewLifecycleOwner.lifecycleScope.launch {
                    auth.searchUser(newText)
                }
                return false
            }

        })
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the layout manager for the RecyclerView
        binding.rvChatNameList.layoutManager = LinearLayoutManager(context)

        val sharedPreferences = auth.getPreferences()
        val currentUserId = sharedPreferences.getString("id", "")

        // Fetch the list of users from Firebase Firestore
        val db = Firebase.firestore
        val usersRef = db.collection("users")
        usersRef.get()
            .addOnSuccessListener { result ->
                val users = result.mapNotNull { document ->
                    val user = document.toObject(Login.data.User::class.java)
                    if (user.id != currentUserId) user else null // Filter out the current user
                }
                liveChatNameAdapter.submitList(users)
            }
    }
}
