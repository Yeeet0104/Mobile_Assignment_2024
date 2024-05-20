package Login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import com.example.mobile_assignment.databinding.FragmentSignUp1Binding
import com.example.mobile_assignment.databinding.FragmentSignUp2Binding

class SignUp2Fragment : Fragment() {

    private lateinit var binding :FragmentSignUp2Binding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUp2Binding.inflate(inflater, container, false)

        return binding.root
    }

}