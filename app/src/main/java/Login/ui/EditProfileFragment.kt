package Login.ui

import Login.data.AuthVM
import Login.data.User
import Login.data.UserVM
import Login.util.errorDialog
import Login.util.toBitmap
import Login.util.toBlob
import Login.util.toast
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentEditProfileBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import java.sql.Blob


class EditProfileFragment : Fragment() {

    private lateinit var binding : FragmentEditProfileBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private val userVM: UserVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        loadProfile()
        binding.btnEdtProfileSave.setOnClickListener { save() }
        binding.imageView3.setOnClickListener{ selectImg() }

        return binding.root
    }

    private fun loadProfile(){
        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "") ?: return

        userVM.get1(userId) { user ->
            if (user != null) {
                // Populate UI fields
                binding.imageView3.setImageBitmap(user.photo.toBitmap())
                binding.edtEdtProfileUsername.setText(user.username)
                binding.edtEdtEmail.setText(user.email)
                binding.edtEdtProfileWeight.setText(user.weight.toString())
                binding.edtEdtProfileHeight.setText(user.height.toString())

                // Set gender radio buttons
                if (user.gender == "Male") {
                    binding.radMale.isChecked = true
                } else if (user.gender == "Female") {
                    binding.radFemale.isChecked = true
                }
            }
        }

    }

    // Get-content launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.imageView3.setImageURI(it)
    }

    private fun selectImg(){
        // Select file
        getContent.launch("image/*")
    }

    private fun save() {
        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "") ?: return

        // Retrieve values from UI
        val username = binding.edtEdtProfileUsername.text.toString().trim()
        val email = binding.edtEdtEmail.text.toString().trim()
        val weight = binding.edtEdtProfileWeight.text.toString().trim().toIntOrNull() ?: 0
        val height = binding.edtEdtProfileHeight.text.toString().trim().toIntOrNull() ?: 0
        val gender = if (binding.radMale.isChecked) "Male" else if (binding.radFemale.isChecked) "Female" else ""

        // Validate input
        val errorMessage = validateInput(username, email, weight, height, gender)
        if (errorMessage.isNotEmpty()) {
            errorDialog(errorMessage)
            return
        }

        // Retrieve user's current data including photo
        userVM.get1(userId) { user ->
            if (user != null) {
                // Retrieve existing photo
                val currentPhoto = user.photo

                // Retrieve new photo from UI
                val newPhoto = binding.imageView3.drawable?.toBitmap()?.toBlob() ?: currentPhoto

                // Update user data
                val updatedUser = User(
                    id = userId,
                    username = username,
                    email = email,
                    weight = weight,
                    height = height,
                    gender = gender,
                    photo = newPhoto
                )

                // Update user in Firestore
                userVM.set(updatedUser)

                // Navigate back to ProfileFragment
                nav.navigateUp()
            }
        }
    }

    private fun validateInput(username: String, email: String, weight: Int, height: Int, gender: String): String {
        val errors = StringBuilder()

        if (username.isEmpty()) {
            errors.append("- Username cannot be empty.\n")
        }
        if (email.isEmpty()) {
            errors.append("- Email cannot be empty.\n")
        } else if (!isValidEmail(email)) {
            errors.append("- Email format is incorrect.\n")
        }
        if (weight <= 0) {
            errors.append("- Weight must be greater than 0.\n")
        }
        if (height <= 0) {
            errors.append("- Height must be greater than 0.\n")
        }
        if (gender.isEmpty()) {
            errors.append("- Please select a gender.\n")
        }

        return errors.toString()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }


}