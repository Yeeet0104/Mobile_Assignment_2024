package Login.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentFirstPageBinding
import com.example.mobile_assignment.databinding.FragmentForgetPasswordBinding

class FirstPageFragment : Fragment() {

    private lateinit var binding : FragmentFirstPageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val nav by lazy { findNavController() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFirstPageBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        binding.btnUser.setOnClickListener { user() }
        binding.btnAdmin.setOnClickListener { admin() }


        return binding.root
    }

    private fun user(){
        sharedPreferences.edit().putInt("roleForSignUp", 0).apply()
        nav.navigate(R.id.action_firstPageFragment_to_loginFragment)
    }

    private fun admin(){
        sharedPreferences.edit().putInt("roleForSignUp", 1).apply()
        nav.navigate(R.id.action_firstPageFragment_to_loginFragment)
    }

}