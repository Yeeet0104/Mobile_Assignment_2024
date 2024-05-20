package Login.ui

import Login.data.User
import Login.data.UserVM
import Login.util.errorDialog
import Login.util.toBlob
import Login.util.toast
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
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import com.example.mobile_assignment.databinding.FragmentSignUp1Binding
import kotlin.random.Random

class SignUp1Fragment : Fragment() {

    private lateinit var binding :FragmentSignUp1Binding
    private val nav by lazy { findNavController() }
    private val vm: UserVM by activityViewModels()

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
        val confPass = binding.edtSignUp1ConfPassword.text.toString()
        val pwd = binding.edtSignUp1Password.text.toString()
        val otpCode = Random.nextInt(1000, 9999)

        if(confPass == pwd){
            val userPhoto = BitmapFactory.decodeResource(resources, R.drawable.suzy)
            if (userPhoto == null) {
                toast("Failed to load user photo")
                return
            }

            // Insert user
            val user = User(
                id = autoGenerateID(),
                username = binding.edtSignUp1Username.text.toString().trim(),
                email    = binding.edtSignUp1Email.text.toString().trim(),
                password = binding.edtSignUp1Password.text.toString().trim(),
                otp = otpCode,
                role = 0,
                photo = userPhoto.toBlob()
            )

            val e = vm.validate(user)
            if (e != "") {
                errorDialog(e)
                return
            }

            vm.set(user)
            nav.navigateUp()
        }else{
            toast("Password and Confirm Password not same. Try again!")
        }

    }

    private fun autoGenerateID():String{
        val latestUserId = vm.getUsersLD().value?.lastOrNull()?.id

        // Extract the numeric part of the ID
        val lastIdNumeric = latestUserId?.substring(1)?.toIntOrNull() ?: 0
        val nextIdNumeric = lastIdNumeric + 1

        // Format the next ID to be in the "Uxxx" format
        val nextId = "U${nextIdNumeric.toString().padStart(3, '0')}"
        return nextId
    }

}