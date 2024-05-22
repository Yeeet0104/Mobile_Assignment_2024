package Login.ui

import Login.data.AuthVM
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import util.errorDialog
import util.toast
import java.security.MessageDigest


class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private var roleForSignUp: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Get the roleForSignUp from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        roleForSignUp = sharedPreferences.getInt("roleForSignUp", 0)

        // Set visibility of lbl_admin_login based on roleForSignUp
        binding.lblAdminLogin.visibility = if (roleForSignUp == 1) View.VISIBLE else View.GONE

        reset()
        binding.btnLogin.setOnClickListener { login() }
        binding.lblForgetPassword.setOnClickListener { nav.navigate(R.id.action_loginFragment_to_forgetPasswordFragment) }
        binding.lblLoginToSignUp1.setOnClickListener { nav.navigate(R.id.action_loginFragment_to_signUp1Fragment) }

        return binding.root
    }

    private fun reset(){
        binding.edtLoginEmail.text.clear()
        binding.edtLoginPassword.text.clear()
        binding.chkRememberMe.isChecked = false

        binding.edtLoginEmail.requestFocus()
    }

    private fun login() {
        val email = binding.edtLoginEmail.text.toString().trim()
        val password = binding.edtLoginPassword.text.toString().trim()
        val remember = binding.chkRememberMe.isChecked

        if(email == ""){
            errorDialog("Email cannot be empty.")
            return
        }

        if(password == ""){
            errorDialog("Password cannot be empty.")
            return
        }

        // Check if Remember Me is checked
        if (!binding.chkRememberMe.isChecked) {
            errorDialog("Please check the Remember Me checkbox to login.")
            return
        }

        // Hash the entered password
        val hashedPassword = hashPassword(password)

        // Login -> auth.login(...)
        // Clear navigation backstack
        lifecycleScope.launch {
            val success = auth.login(email, hashedPassword, remember)
            if (success) {
                toast("Login Successfully!")
                nav.navigate(R.id.profileFragment)
            } else {
                errorDialog("Invalid Login Credentials.")
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