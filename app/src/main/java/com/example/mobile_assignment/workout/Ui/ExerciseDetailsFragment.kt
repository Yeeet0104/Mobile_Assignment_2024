package com.example.mobile_assignment.workout.Ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mobile_assignment.databinding.FragmentExerciseDetailsBinding
import com.example.mobile_assignment.workout.Data.ExerciseViewModel


class ExerciseDetailsFragment : Fragment() {
    private lateinit var binding: FragmentExerciseDetailsBinding
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExerciseDetailsBinding.inflate(inflater, container, false)

        exerciseViewModel.selectedExercise.observe(viewLifecycleOwner) { exercise ->
            exercise?.let {
                binding.tvExerciseName.text = it.name
                binding.tvExerciseDuration.text =
                    if (it.duration != 0) "Duration: ${it.duration}" else "Reps: ${it.reps}"

                val stepsText = it.steps.joinToString(separator = "\n") { step -> "• $step" }
                binding.tvSteps.text = stepsText

                it.photo?.let { blob ->
                    val imageBytes = blob.toBytes()
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imageView.setImageBitmap(bitmap)
                }

                // Load YouTube video
                val youtubeUrl = "https://www.youtube.com/embed/${it.youtubeId}"
                binding.webView.apply {
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    loadUrl(youtubeUrl)
                }
            }
        }

        return binding.root
    }
}