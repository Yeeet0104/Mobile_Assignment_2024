package Login.ui

import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentHomeBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import com.example.mobile_assignment.databinding.FragmentWorkoutHomeBinding

class   ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)




        return binding.root
    }

}

//private lateinit var binding: FragmentHomeBinding
//private val nav by lazy { findNavController() }
//private val auth: AuthVM by activityViewModels()

//// Access shared preferences from AuthVM
//val sharedPreferences = auth.getPreferences()
//
//// Get values from shared preferences
//val email = sharedPreferences.getString("email", "")
//val password = sharedPreferences.getString("password", "")
//val id = sharedPreferences.getString("id", "")
//val role = sharedPreferences.getInt("role",0).toString()
//
//
//// Display values in textView
//binding.textView.text = "Email: $email\nPassword: $password\nID: $id\n" + "ID: $role"