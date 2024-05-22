package Login.ui

import Login.data.AuthVM
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
import com.example.mobile_assignment.databinding.FragmentOtpBinding
import util.errorDialog
import util.toast
import kotlin.random.Random


class OtpFragment : Fragment() {

    private lateinit var binding : FragmentOtpBinding
    private val nav by lazy { findNavController() }
    private val auth: UserVM by activityViewModels()

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

        val otpCode: Int = binding.edtOtp.text.toString().toIntOrNull() ?: 0

        val sharedPref = requireActivity().getSharedPreferences("idForOtp", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "")

        if (userId.isNullOrBlank()) {
            errorDialog("User ID not found.")
            return
        }

        auth.getOTP(userId) { otpFromDatabase ->
            if (otpFromDatabase == otpCode) {
                toast("Verification is successful. Reset your password now.")

                //generate again otp to replace otp in database
                // Generate a new OTP
                val newOtpCode = Random.nextInt(1000, 9999)

                // Update the new OTP in the database
                auth.updateOTP(userId, newOtpCode) { success ->
                    if (success) {
                        nav.navigate(R.id.action_otpFragment_to_resetPasswordFragment)
                    } else {
                        errorDialog("Failed to generate new OTP. Please try again.")
                    }
                }

            } else {
                errorDialog("OTP does not match. Please re-enter.")
            }
        }
    }

}