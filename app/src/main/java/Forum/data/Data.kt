package Forum.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    var postId : String = "",
    val username: String = "",
    val userId: String = "",
    val userProfilePic: Blob = Blob.fromBytes(ByteArray(0)),
    val timePosted: Long = 0,
    val postTitle: String = "",
    val postContent: String = "",
    val postImg: Blob? = null,
    val isEdited: Boolean = false,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false
)

data class Comment(
    @DocumentId
    var commentId: String = "",
    val userId: String = "",
    val content: String = "",
    val timePosted: Long = 0
)