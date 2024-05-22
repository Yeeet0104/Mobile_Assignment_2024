package Login.ui

import Login.data.AuthVM
import Login.data.UserVM
import Login.util.toBitmap
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.FragmentHomeBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding
import com.example.mobile_assignment.databinding.FragmentWorkoutHomeBinding

class   ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()
    private val userVM: UserVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadProfile()
        binding.btnEdtProfile.setOnClickListener { nav.navigate(R.id.action_profileFragment_to_editProfileFragment) }
        binding.btnChangePassword.setOnClickListener { nav.navigate(R.id.action_profileFragment_to_changePasswordProfileFragment) }
        binding.btnLogout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.lblDltAcc.setOnClickListener { showDeleteAccountConfirmationDialog() }


        return binding.root
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteAcc()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAcc(){
        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "") ?: return

        if (userId.isNotEmpty()) {
            userVM.delete(userId)
            auth.logout()
            nav.popBackStack(R.id.loginFragment, false)
            nav.navigate(R.id.loginFragment)
        }
    }

    private fun logout() {
        auth.logout()
        nav.popBackStack(R.id.loginFragment, false)
        nav.navigate(R.id.loginFragment)
    }

    private fun loadProfile() {
        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "") ?: return

        if (userId.isNotEmpty()) {
            userVM.get1(userId) { user ->
                user?.let {
                    binding.imgProfile.setImageBitmap(it.photo.toBitmap())
                    setTextView(binding.txtProfileUsername, it.username)
                    setTextView(binding.txtProfileEmail, it.email)
                    setTextView(binding.txtProfileGender, it.gender)
                    setTextView(binding.txtProfileHeight, if (it.height != 0) it.height.toString() else "")
                    setTextView(binding.txtProfileWeight, if (it.weight != 0) it.weight.toString() else "")
                } ?: run {
                    setTextView(binding.txtProfileUsername, "")
                    setTextView(binding.txtProfileEmail, "")
                    setTextView(binding.txtProfileGender, "")
                    setTextView(binding.txtProfileHeight, "")
                    setTextView(binding.txtProfileWeight, "")
                }
            }
        } else {
            setTextView(binding.txtProfileUsername, "")
            setTextView(binding.txtProfileEmail, "")
            setTextView(binding.txtProfileGender, "")
            setTextView(binding.txtProfileHeight, "")
            setTextView(binding.txtProfileWeight, "")
        }
    }

    //make null data to "-" and grey color
    private fun setTextView(textView: TextView, text: String) {
        if (text.isEmpty()) {
            textView.text = "-"
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        } else {
            textView.text = text
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

}
