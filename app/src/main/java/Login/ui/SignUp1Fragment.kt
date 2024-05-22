package Login.ui

import Login.data.User
import Login.data.UserVM
import Login.util.toBlob
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentSignUp1Binding
import com.google.firebase.firestore.FirebaseFirestore
import util.errorDialog
import util.toast
import java.security.MessageDigest
import kotlin.random.Random

class SignUp1Fragment : Fragment() {

    private lateinit var binding :FragmentSignUp1Binding
    private val nav by lazy { findNavController() }
    private val vm: UserVM by activityViewModels()
    private var roleForSignUp: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUp1Binding.inflate(inflater, container, false)

        reset()
        binding.btnSignUp1Next.setOnClickListener { register() }
        binding.lblSignUp1ToLogin.setOnClickListener { nav.navigate(R.id.action_signUp1Fragment_to_loginFragment) }

        return binding.root
    }

    private fun reset() {
        binding.edtSignUp1Username.text.clear()
        binding.edtSignUp1Email.text.clear()
        binding.edtSignUp1Password.text.clear()
        binding.edtSignUp1ConfPassword.text.clear()

        binding.edtSignUp1Username.requestFocus()
    }

    private fun register() {
        val userName = binding.edtSignUp1Username.text.toString().trim()
        val userEmail = binding.edtSignUp1Email.text.toString().trim()
        val confPass = binding.edtSignUp1ConfPassword.text.toString()
        val pwd = binding.edtSignUp1Password.text.toString()
        val otpCode = Random.nextInt(1000, 9999)

        // Get the roleForSignUp from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        roleForSignUp = sharedPreferences.getInt("roleForSignUp", 0)

        //validation
        when {
            userName.isEmpty() -> {
                errorDialog("Username cannot be empty")
                return
            }
            userName.length < 3 -> {
                errorDialog("Username must be at least 3 characters")
                return
            }
            userEmail.isEmpty() -> {
                errorDialog("Email cannot be empty")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                errorDialog("Invalid email format")
                return
            }
            pwd.isEmpty() -> {
                errorDialog("Password cannot be empty")
                return
            }
            pwd.length < 5 -> {
                errorDialog("Password must be at least 5 characters")
                return
            }
            confPass.isEmpty() -> {
                errorDialog("Confirm Password cannot be empty")
                return
            }
            confPass != pwd -> {
                errorDialog("Password and Confirm Password do not match")
                return
            }
        }

        val userPhoto = BitmapFactory.decodeResource(resources, R.drawable.suzy)
        if (userPhoto == null) {
            toast("Failed to load user photo")
            return
        }

        // Check if email already exists
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Hash the password
                    val hashedPassword = hashPassword(pwd)

                    // Generate ID asynchronously
                    autoGenerateID { generatedId ->
                        val user = User(
                            id = generatedId,
                            username = userName,
                            email = userEmail,
                            password = hashedPassword, // Store hashed password
                            otp = otpCode,
                            role = roleForSignUp,
                            photo = userPhoto.toBlob()
                        )

                        val e = vm.validate(user)
                        if (e != "") {
                            errorDialog(e)
                            return@autoGenerateID
                        }

                        vm.set(user)
                        toast("Successfully registered! Login now!")
                        nav.navigateUp()
                    }
                } else {
                    errorDialog("Email duplicated.")
                }
            }
            .addOnFailureListener { exception ->
                errorDialog("Error checking email: ${exception.message}")
            }
    }

    private fun autoGenerateID(callback: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                val idList = result.documents.map { it.id }
                var newId = "U001"
                var idNumber = 1
                while (idList.contains(newId)) {
                    idNumber++
                    newId = "U" + String.format("%03d", idNumber)
                }
                callback(newId)
            }
            .addOnFailureListener {
                callback("U001") // Default ID in case of error
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

}