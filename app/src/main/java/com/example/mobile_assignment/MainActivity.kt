package com.example.mobile_assignment


import Login.data.AuthVM
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mobile_assignment.databinding.ActivityMainBinding
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.example.mobile_assignment.workout.WorkoutSharedViewModel
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }
    private lateinit var abc: AppBarConfiguration
    private val auth: AuthVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scheduleDailyProgressReset(this)
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

    private fun scheduleDailyProgressReset(context: Context) {
        val userId = getCurrentUserId()  // Get the user ID here
        val intent = Intent(context, ResetProgressReceiver::class.java).apply {
            putExtra("userId", userId)  // Add the user ID to the intent
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    private fun getCurrentUserId(): String {
        val sharedPref = getSharedPreferences("your_shared_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("userId", "U001") ?: "U001"
    }
}
class ResetProgressReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getStringExtra("userId") ?: return
        val viewModel = ViewModelProvider(context as MainActivity).get(WorkoutSharedViewModel::class.java)
        viewModel.resetProgress(userId)
    }
}
