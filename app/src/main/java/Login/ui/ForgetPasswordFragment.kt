package Login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentForgetPasswordBinding


class ForgetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentForgetPasswordBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)

        binding.btnFp.setOnClickListener { sendOtp() }
        binding.lblFpToLogin.setOnClickListener { nav.navigate(R.id.action_forgetPasswordFragment_to_loginFragment) }


        return binding.root
    }

    private fun sendOtp(){


        nav.navigate(R.id.action_forgetPasswordFragment_to_otpFragment)
    }

}