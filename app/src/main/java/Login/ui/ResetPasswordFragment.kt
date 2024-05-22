package Login.ui

import Login.data.UserVM
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentResetPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import util.errorDialog
import util.toast
import java.security.MessageDigest

class ResetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentResetPasswordBinding
    private val nav by lazy { findNavController() }
    private val auth: UserVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        binding.btnResetPassword.setOnClickListener { resetPassword() }
        binding.lblRpToLogin.setOnClickListener { nav.navigate(R.id.action_resetPasswordFragment_to_loginFragment) }

        return binding.root
    }

    private fun resetPassword(){

        val newPassword = binding.edtRpNewPassword.text.toString().trim()
        val confPassword = binding.edtRpConfPassword.text.toString().trim()

        if(newPassword == ""){
            errorDialog("New Password cannot be empty.")
            binding.edtRpConfPassword.text.clear()
            return
        }

        if (newPassword.length < 5) {
            errorDialog("Password must be at least 5 characters long.")
            binding.edtRpNewPassword.text.clear()
            binding.edtRpConfPassword.text.clear()
            binding.edtRpNewPassword.requestFocus()
            return
        }

        if(confPassword == ""){
            errorDialog("Confirm Password cannot be empty.")
            binding.edtRpNewPassword.text.clear()
            return
        }



        if (newPassword != confPassword) {
            errorDialog("New password and Confirm password not same. Please enter again.");
            binding.edtRpNewPassword.text.clear()
            binding.edtRpConfPassword.text.clear()
            binding.edtRpNewPassword.requestFocus()
            return
        }

        // Retrieve userId from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("idForOtp", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "")

        if (userId.isNullOrEmpty()) {
            errorDialog("User ID not found.")
            return
        }

        // Hash the new password
        val hashedPassword = hashPassword(newPassword)

        // Update password in Firestore
        auth.get1(userId) { user ->
            if (user != null) {
                user.password = hashedPassword
                auth.set(user)
                toast("Password Reset Successfully!")
                nav.navigate(R.id.action_resetPasswordFragment_to_loginFragment)
            } else {
                errorDialog("User not found.")
            }
        }
    }


    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

}