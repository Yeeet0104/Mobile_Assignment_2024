package Login.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentEditProfileBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding


class EditProfileFragment : Fragment() {

    private lateinit var binding : FragmentEditProfileBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)



        return binding.root
    }


}