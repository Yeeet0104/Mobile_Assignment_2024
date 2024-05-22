package com.example.mobile_assignment

import Login.data.AuthVM
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_assignment.databinding.FragmentRunningHistoryBinding
import com.example.mobile_assignment.workout.Data.RunningSessionViewModel
import com.example.mobile_assignment.workout.RunningSessionAdapter


class running_history : Fragment() {

    private lateinit var binding : FragmentRunningHistoryBinding
    private lateinit var viewModel: RunningSessionViewModel
    private val auth: AuthVM by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunningHistoryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(RunningSessionViewModel::class.java)

        val adapter = RunningSessionAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.runningSessions.observe(viewLifecycleOwner) { runningSessions ->
            adapter.submitList(runningSessions)
        }

        viewModel.fetchRunningSessions(getCurrentUserId())

        return binding.root
    }


    private fun getCurrentUserId(): String {
        val sharedPreferences = auth.getPreferences()
        val userId = sharedPreferences.getString("id", "")
        return userId ?: "U001"
    }
}