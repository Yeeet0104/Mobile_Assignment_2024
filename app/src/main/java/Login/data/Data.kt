package Login.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId
    val id :String = "",
    val email: String = "",
    val gender: String = "",
    val height: Int = 0,
    val otp: Int = 0,
    var password: String = "",
    val role: Int = 0,
    val username: String = "",
    val weight: Int = 0,
    val photo: Blob = Blob.fromBytes(ByteArray(0))
)
