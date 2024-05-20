package Login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentResetPasswordBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        binding.btnResetPassword.setOnClickListener { reset() }
        binding.lblRpToLogin.setOnClickListener { nav.navigate(R.id.action_resetPasswordFragment_to_loginFragment) }

        return binding.root
    }

    private fun reset(){
        nav.navigate(R.id.action_resetPasswordFragment_to_loginFragment)
    }


}