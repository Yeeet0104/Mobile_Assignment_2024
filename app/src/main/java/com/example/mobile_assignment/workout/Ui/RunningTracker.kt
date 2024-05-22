package com.example.mobile_assignment.workout.Ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.mobile_assignment.R
import com.example.mobile_assignment.databinding.ActivityRunningTrackerBinding
import com.example.mobile_assignment.workout.Data.ExerciseViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class runningTracker : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRunningTrackerBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleMap: GoogleMap
    private var tracking = false
    private val pathPoints = mutableListOf<LatLng>()
    private var totalDistance = 0.0
    private var initialZoomDone = false
    private var startTime: Long = 0
    private var endTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private var elapsedTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunningTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Running Tracker"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.startButton.setOnClickListener {
            if (tracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        binding.btnHome.setOnClickListener {
            finish()
        }
        setupLocationCallback()

        // Prompt user to enable high accuracy location mode
        checkLocationSettings()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (tracking) {
                    for (location in locationResult.locations) {
                        // Use only high accuracy locations
                        if (location.accuracy <= 10) { // Adjust the accuracy threshold as needed
                            updateLocation(location)
                        }
                    }
                }
            }
        }
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        tracking = true
        startTime = System.currentTimeMillis()
        binding.startButton.text = "Stop"
        pathPoints.clear()
        totalDistance = 0.0
        elapsedTime = 0L
        handler.post(timerRunnable)
        binding.distanceTextView.text = "Distance: 0.0 m"
        initialZoomDone = false
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(1000)
                .setMinUpdateIntervalMillis(500)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            locationCallback,
            null
        )
    }

    private fun stopTracking() {
        tracking = false
        endTime = System.currentTimeMillis()
        binding.startButton.text = "Start"
        fusedLocationClient.removeLocationUpdates(locationCallback)
        binding.distanceTextView.text = "Distance: ${totalDistance.roundToInt()} m"
        handler.removeCallbacks(timerRunnable)

        // Calculate average speed
        val duration = (endTime - startTime) / 1000.0 // duration in seconds
        val averageSpeed = if (duration > 0) totalDistance / duration else 0.0 // speed in m/s

        // Save data to Firestore
        saveRunningSession(startTime, endTime, totalDistance, averageSpeed)
    }

    private fun updateLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (pathPoints.isNotEmpty()) {
            val lastLatLng = pathPoints.last()
            val results = FloatArray(1)
            Location.distanceBetween(
                lastLatLng.latitude,
                lastLatLng.longitude,
                latLng.latitude,
                latLng.longitude,
                results
            )
            totalDistance += results[0]
            val speed = location.speed // speed in meters/second

            // Display speed
            binding.speedTextView.text = "Speed: ${(speed * 3.6).roundToInt()} km/h" // converting m/s to km/h
        }
        pathPoints.add(latLng)

        // Update distance in real-time
        binding.distanceTextView.text = "Distance: ${totalDistance.roundToInt()} m"

        if (!initialZoomDone) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f)) // Zoom level 18 for closer view
            initialZoomDone = true
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        googleMap.addPolyline(
            PolylineOptions()
            .addAll(pathPoints)
            .color(android.graphics.Color.BLUE) // Change polyline color to blue
            .width(10f)) // Increase polyline width
    }

    private fun saveRunningSession(startTime: Long, endTime: Long, distance: Double, averageSpeed: Double) {

        // Get an instance of ExerciseViewModel
        val exerciseViewModel = ViewModelProvider(this).get(ExerciseViewModel::class.java)

        // Get user ID from shared preferences
        val userId = exerciseViewModel.getCurrentUserId()
        val firestore = FirebaseFirestore.getInstance()
        val runningSession = mapOf(
            "startTime" to startTime,
            "endTime" to endTime,
            "distance" to distance,
            "averageSpeed" to averageSpeed,
            "date" to System.currentTimeMillis() // Current date
        )

        firestore.collection("customPlans")
            .document(userId)
            .collection("runningSessions")
            .add(runningSession)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Session saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissions()
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(1000)
            .setMinUpdateIntervalMillis(500)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (!states!!.isLocationUsable) {
                // Prompt user to enable high accuracy mode
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime
            binding.timerTextView.text = formatElapsedTime(elapsedTime)
            handler.postDelayed(this, 1000)
        }
    }

    private fun formatElapsedTime(elapsedTime: Long): String {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }
}