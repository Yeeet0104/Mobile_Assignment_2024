package com.example.mobile_assignment

import Login.data.AuthVM
import Login.data.User
import Login.util.setImageBlob
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mobile_assignment.databinding.ActivityMainBinding
import com.example.mobile_assignment.databinding.Header1Binding
import com.example.mobile_assignment.databinding.HeaderBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }
    private lateinit var abc: AppBarConfiguration
    private val auth: AuthVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        abc = AppBarConfiguration(
            setOf(
                R.id.home2,
                R.id.workoutHome,
                R.id.forumHome,
                R.id.profileFragment,
                R.id.nutritionMain,
                R.id.loginFragment
            ),
            binding.root
        )

        nav.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.signUp1Fragment || destination.id == R.id.forgetPasswordFragment || destination.id == R.id.resetPasswordFragment || destination.id == R.id.otpFragment || destination.id == R.id.splashScreenFragment) {
                // Hide navigation components when on loginFragment
                supportActionBar?.hide()
                binding.bv.visibility = android.view.View.GONE
                binding.nv.visibility = android.view.View.GONE
            } else {
                // Show navigation components otherwise
                supportActionBar?.show()
                binding.bv.visibility = android.view.View.VISIBLE
                binding.nv.visibility = android.view.View.VISIBLE

                setupActionBarWithNavController(nav, abc)
                binding.bv.setupWithNavController(nav)
                binding.nv.setupWithNavController(nav)

//                // TODO(5): Observe login status -> userLiveData
//                auth.getUserLD().observe(this) { user ->
//                    // TODO(5A): Clear menu + remove header
//                    binding.nv.menu.clear()
//                    val h = binding.nv.getHeaderView(0)
//                    binding.nv.removeHeaderView(h)
//
//                    // TODO(5B): Inflate menu + header (based on login status)
//                    if (user == null) {
//                        binding.nv.inflateHeaderView(R.layout.header)
//                        nav.navigateUp()
//                    }
//                    else {
//                        binding.nv.inflateHeaderView(R.layout.header1)
//                        setHeader(user)
//                    }
//
//                }
//
//                // TODO(8): Auto login -> auth.loginFromPreferences(...)
//                lifecycleScope.launch{auth.loginFromPreferences()}
            }
        }

    }

//    private fun setHeader(user: User) {
//        val h = binding.nv.getHeaderView(0)
//        val b = HeaderBinding.bind(h)
//        b.imgPhoto.setImageBlob(user.photo)
//        b.txtName.text  = user.username
//        b.txtEmail.text = user.email
//    }

    override fun onSupportNavigateUp(): Boolean {
        return nav.navigateUp(abc)
    }
}