package Login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentOtpBinding


class OtpFragment : Fragment() {

    private lateinit var binding : FragmentOtpBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOtpBinding.inflate(inflater, container, false)

        binding.btnOtpConfirm.setOnClickListener { confirm() }
        binding.lblOtpToLogin.setOnClickListener { nav.navigate(R.id.action_otpFragment_to_loginFragment) }

        return binding.root
    }

    private fun confirm(){


        nav.navigate(R.id.action_otpFragment_to_resetPasswordFragment)
    }

}