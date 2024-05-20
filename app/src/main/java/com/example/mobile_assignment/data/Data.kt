package com.example.mobile_assignment.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    var postId : String = "",
    val username: String = "",
    val userProfilePic: Blob = Blob.fromBytes(ByteArray(0)),
    val timePosted: Long = 0,
    val postTitle: String = "",
    val postContent: String = "",
    val postImg: Blob? = null
)