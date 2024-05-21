package Login.ui

import Login.data.AuthVM
import Login.data.UserVM
import Login.util.SimpleEmail
import Login.util.errorDialog
import Login.util.hideKeyboard
import Login.util.snackbar
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentForgetPasswordBinding


class ForgetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentForgetPasswordBinding
    private val nav by lazy { findNavController() }
    private val vm: UserVM by activityViewModels()

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

        hideKeyboard()

        val email = binding.edtFpEmail.text.toString().trim()
        if (!isEmail(email)) {
            errorDialog("Invalid email.")
            binding.edtFpEmail.requestFocus()
            return
        }

        // Use the ViewModel to check if the email exists
        vm.getUsersLD().observe(viewLifecycleOwner, Observer { users ->
            val user = users.find { it.email == email }

            if (user == null) {
                errorDialog("You are not our member, please sign up.")
                binding.edtFpEmail.requestFocus()
                return@Observer
            }

            val userId = user.id
            val otpCode = user.otp.toString() // Retrieve the OTP from the user object

            val subject = "OTP Verification - $otpCode"
            val content = """
            <p>Please use the following one time password (OTP) to reset your password. Your <b>OTP</b> is:</p>
            <h1 style="color: red">$otpCode</h1>
            <p>Do not share this OTP with anyone.</p>
            <p>Thank you!</p>
        """.trimIndent()

            // send otp to email
            SimpleEmail()
                .to(email)
                .subject(subject)
                .content(content)
                .isHtml()
                .send{
                    snackbar("Email sent...")
                    binding.btnFp.isEnabled = true
                    binding.edtFpEmail.text.clear()

                    // SharePreferences
                    val sharedPref = requireActivity().getSharedPreferences("idForOtp", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("userId", userId)
                        apply()
                    }

                    nav.navigate(R.id.action_forgetPasswordFragment_to_otpFragment)
                }

            snackbar("Sending email...")
            binding.btnFp.isEnabled = false
        })
    }


    private fun isEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

}