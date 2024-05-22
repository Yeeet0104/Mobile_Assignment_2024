package Forum.ui

import Login.data.AuthVM
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentLiveChatNameListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
                liveChatNameAdapter = LiveChatNameAdapter(auth, users, this)
                binding.rvChatNameList.adapter = liveChatNameAdapter
            }
    }
}