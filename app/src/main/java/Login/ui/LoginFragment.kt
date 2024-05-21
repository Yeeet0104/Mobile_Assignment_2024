package Login.ui

import Login.data.AuthVM
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


class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

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
        val email    = binding.edtLoginEmail.text.toString().trim()
        val password = binding.edtLoginPassword.text.toString().trim()
        val remember = binding.chkRememberMe.isChecked

        //Login -> auth.login(...)
        //Clear navigation backstack
        lifecycleScope.launch {
            val success = auth.login(email, password, remember)
            if(success){
                nav.popBackStack(R.id.home2, false)
                nav.navigateUp()
            }else{
                errorDialog("Invalid Login Credentials.")
            }
        }

    }


}