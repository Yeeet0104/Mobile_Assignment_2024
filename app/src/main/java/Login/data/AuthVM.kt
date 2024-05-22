package Login.data

import android.app.Application
import android.content.Context
import android.location.GnssAntennaInfo.Listener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthVM (val app: Application) : AndroidViewModel(app) {

    private val USERS = Firebase.firestore.collection("users")
    private val userLD = MutableLiveData<User?>()
    private var listener: ListenerRegistration? = null

    init {
        userLD.value = null
    }

    // ---------------------------------------------------------------------------------------------

    fun init() = Unit

    fun getUserLD() = userLD

    fun getUser() = userLD.value

    // TODO(1): Login
    suspend fun login(email: String, password: String, remember: Boolean = false) : Boolean {
        // TODO(1A): Get the user record with matching email + password
        //           Return false is no matching found
        if (email == "" || password == "") return false

        val user = USERS
            .whereEqualTo("email",email)
            .whereEqualTo("password", password)
            .get()
            .await()
            .toObjects<User>()
            .firstOrNull() ?: return false

        // TODO(1B): Setup snapshot listener
        //           Update live data -> user
        listener?.remove()
        listener = USERS.document(user.id).addSnapshotListener{snap, _ ->
            userLD.value = snap?.toObject<User>()
        }

        // TODO(6A): Handle remember-me -> add shared preferences
        if(remember){
            getPreferences()
                .edit()
                .putString("id",user.id)
                .putString("email", email)
                .putString("password", password)
                .putInt("role", user.role)
                .apply()
        }

        return true
    }

     //TODO(2): Logout
    fun logout() {
        // TODO(2A): Remove snapshot listener
        //           Update live data -> null
        listener?.remove()
        userLD.value = null

        // TODO(6B): Handle remember-me -> clear shared preferences
    getPreferences().edit().remove("email").remove("password").remove("id").remove("role").apply()

    // [OR] getPreferences().edit().clear().apply()
    // [OR] app.deleteSharedPreferences("AUTH")

    }


    // TODO(6): Get shared preferences
    public fun getPreferences() = app.getSharedPreferences("AUTH", Context.MODE_PRIVATE)

//    // TODO(7): Auto login from shared preferences
//    suspend fun loginFromPreferences() {
//        val email = getPreferences().getString("email",null)
//        val password = getPreferences().getString("password", null)
//
//        if(email != null && password != null){
//            login(email,password)
//        }
//    }

    // Assuming your existing code for AuthVM is fine, ensure loginFromPreferences is implemented.
    suspend fun loginFromPreferences(): Boolean {
        val email = getPreferences().getString("email", null)
        val password = getPreferences().getString("password", null)

        return if (email != null && password != null) {
            login(email, password, true)
        } else {
            false
        }
    }

    suspend fun getUserPhotoBlob(): Blob? {
        val currentUserId = getPreferences().getString("id", null)
        if (currentUserId != null) {
            val user = USERS
                .document(currentUserId)
                .get()
                .await()
                .toObject<User>()
            return user?.photo
        }
        return null
    }

}

