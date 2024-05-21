package Forum.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    var postId : String = "",
    val postUsername: String = "",
    val userId: String = "",
    val userProfilePic: Blob = Blob.fromBytes(ByteArray(0)),
    val timePosted: Long = 0,
    val postTitle: String = "",
    val postContent: String = "",
    val postImg: Blob? = null,
    val isEdited: Boolean = false,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false,
    var likeCount: Int = 0,
    var dislikeCount: Int = 0,
    var likedBy: MutableList<String> = mutableListOf(),
    var dislikedBy: MutableList<String> = mutableListOf()
)

data class Comment(
    @DocumentId
    var commentId: String = "",
    val userId: String = "",
    val content: String = "",
    val timePosted: Long = 0,
    var likedBy: MutableList<String> = mutableListOf(),
    var dislikedBy: MutableList<String> = mutableListOf()
)

data class ChatMessage(
    val text: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val timestamp: Long = 0
)