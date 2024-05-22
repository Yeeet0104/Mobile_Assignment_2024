package com.example.mobile_assignment

import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentHomeBinding

class home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val nav by lazy { findNavController() }
    private val auth: AuthVM by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        // Access shared preferences from AuthVM
        val sharedPreferences = auth.getPreferences()

        // Get values from shared preferences
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        val id = sharedPreferences.getString("id", "")
        val role = sharedPreferences.getInt("role",0).toString()


        // Display values in textView
        binding.textView.text = "Email: $email\nPassword: $password\nID: $id\n" + "role: $role"


        return binding.root
    }
}