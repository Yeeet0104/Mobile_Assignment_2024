package Login.ui

import Login.data.AuthVM
import Login.data.UserVM
import Login.util.errorDialog
import Login.util.toast
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentChangePasswordProfileBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import java.security.MessageDigest


class ChangePasswordProfileFragment : Fragment() {

    private lateinit var binding : FragmentChangePasswordProfileBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private val userVM: UserVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordProfileBinding.inflate(inflater, container, false)

        binding.btnCpSave.setOnClickListener { save() }

        return binding.root
    }

//    private fun save(){
//        // Access shared preferences from AuthVM
//        val sharedPreferences = auth.getPreferences()
//        val userId = sharedPreferences.getString("id", "") ?: return
//
//        val newPassword : String = binding.edtCpNewPassword.text.toString().trim()
//        val confPassword : String = binding.edtCpConfPassword.text.toString().trim()
//
//        if(newPassword == ""){
//            errorDialog("New Password cannot be empty.")
//            binding.edtCpConfPassword.text.clear()
//            return
//        }
//
//        if(confPassword == ""){
//            errorDialog("Confirm Password cannot be empty.")
//            binding.edtCpNewPassword.text.clear()
//            return
//        }
//
//        if (newPassword.length < 5) {
//            errorDialog("Password must be at least 5 characters long.")
//            binding.edtCpNewPassword.text.clear()
//            binding.edtCpConfPassword.text.clear()
//            binding.edtCpNewPassword.requestFocus()
//            return
//        }
//
//        if (newPassword == confPassword) {
//            userVM.get1(userId) { user ->
//                user?.let {
//                    it.password = newPassword
//                    userVM.set(it)
//                    toast("Password changed successfully!")
//                    nav.popBackStack()
//                } ?: run {
//                    // Handle user not found
//                }
//            }
//        } else {
//            // Handle passwords do not match
//            binding.edtCpNewPassword.text.clear()
//            binding.edtCpConfPassword.text.clear()
//            binding.edtCpNewPassword.requestFocus()
//            errorDialog("New Password and Confirm Password are not same. Enter again.")
//        }
//
//    }

    private fun save(){
        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "") ?: return

        val newPassword : String = binding.edtCpNewPassword.text.toString().trim()
        val confPassword : String = binding.edtCpConfPassword.text.toString().trim()

        if(newPassword == ""){
            errorDialog("New Password cannot be empty.")
            binding.edtCpConfPassword.text.clear()
            return
        }

        if(confPassword == ""){
            errorDialog("Confirm Password cannot be empty.")
            binding.edtCpNewPassword.text.clear()
            return
        }

        if (newPassword.length < 5) {
            errorDialog("Password must be at least 5 characters long.")
            binding.edtCpNewPassword.text.clear()
            binding.edtCpConfPassword.text.clear()
            binding.edtCpNewPassword.requestFocus()
            return
        }

        if (newPassword == confPassword) {
            // Hash the new password
            val hashedPassword = hashPassword(newPassword)

            userVM.get1(userId) { user ->
                user?.let {
                    it.password = hashedPassword
                    userVM.set(it)
                    toast("Password changed successfully!")
                    nav.popBackStack()
                } ?: run {
                    // Handle user not found
                }
            }
        } else {
            // Handle passwords do not match
            binding.edtCpNewPassword.text.clear()
            binding.edtCpConfPassword.text.clear()
            binding.edtCpNewPassword.requestFocus()
            errorDialog("New Password and Confirm Password are not same. Enter again.")
        }

    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

}