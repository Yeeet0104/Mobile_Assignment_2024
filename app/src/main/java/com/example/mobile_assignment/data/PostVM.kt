package com.example.mobile_assignment.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class PostVM : ViewModel() {
    val postList = MutableLiveData<List<Post>>()

    // Collection reference
    private val col = Firebase.firestore.collection("posts")
    init {
        // TODO: Add snapshot listener (real-time updates)
        col.addSnapshotListener { v, _ -> postList.value = v?.toObjects() }
    }

    fun get(id: String) = postList.value?.find { it.postId == id }

    fun delete(id: String) {
        // TODO: Delete record by id
        col.document(id).delete()
    }

    fun set(p: Post) {
        // If the post doesn't have an ID, generate one
        if (p.postId.isEmpty()) {
            val lastIdDoc = Firebase.firestore.collection("metadata").document("lastPostId")
            lastIdDoc.get().addOnSuccessListener { document ->
                val lastId = document?.getString("lastId")?.removePrefix("p")?.toInt() ?: 0
                val nextId = lastId + 1
                // Format the next ID as "p001", "p002", etc.
                p.postId = "p%03d".format(nextId)
                // Update the last used ID in the database
                lastIdDoc.set(mapOf("lastId" to p.postId))
                // Set record (insert or update)
                col.document(p.postId).set(p)
            }
        } else {
            // Set record (insert or update)
            col.document(p.postId).set(p)
        }
    }

    private fun idExists(id: String) = postList.value?.any { it.postId == id } ?: false

    fun validate(p: Post, insert: Boolean = true): String {
        val regexId = Regex("""^[0-9A-Z]{4}$""")
        var e = ""


        e += if (p.postTitle == "") "- Title is required.\n"
        else if (p.postTitle.length < 3) "- Title is too short (at least 3 letters).\n"
        else if (p.postTitle.length > 100) "- Title is too long (at most 100 letters).\n"
        else ""

        e += if (p.postContent == "") "- Content is required.\n"
        else if (p.postContent.length < 3) "- Content is too short (at least 3 letters).\n"
        else if (p.postContent.length > 100) "- Content is too long (at most 100 letters).\n"
        else ""

        return e
    }
}