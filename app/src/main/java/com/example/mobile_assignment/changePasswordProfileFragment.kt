package com.example.mobile_assignment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobile_assignment.databinding.FragmentChangePasswordProfileBinding
import com.example.mobile_assignment.databinding.FragmentLoginBinding
import com.example.mobile_assignment.databinding.FragmentProfileBinding


class changePasswordProfileFragment : Fragment() {

    private lateinit var binding : FragmentChangePasswordProfileBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordProfileBinding.inflate(inflater, container, false)


        return binding.root
    }

}