package Login.ui

import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentSplashScreenBinding
import kotlinx.coroutines.launch

class SplashScreenFragment : Fragment() {

    private lateinit var binding : FragmentSplashScreenBinding
    private val SPLASH_TIME: Long = 3000
    private val auth: AuthVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            val isLoggedIn = auth.loginFromPreferences()
            binding.root.postDelayed({
                if (isLoggedIn) {
                    // Navigate to profile fragment if logged in successfully
                    findNavController().navigate(R.id.action_splashScreenFragment_to_profileFragment)
                } else {
                    // Navigate to login fragment otherwise
                    findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
                }
            }, SPLASH_TIME)
        }

        return binding.root
    }

}

