package com.finanzapp.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.finanzapp.R

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esperar 2 segundos y luego comprobar si se debe mostrar el onboarding
        Handler(Looper.getMainLooper()).postDelayed({
            checkOnboardingStatus()
        }, 2000)
    }

    private fun checkOnboardingStatus() {
        // Comprobar si el onboarding ya se mostró anteriormente
        val sharedPrefs = requireActivity().getSharedPreferences("FinanzAppPrefs", 0)
        val onboardingShown = sharedPrefs.getBoolean("onboarding_shown", false)

        // Navegar según corresponda
        if (onboardingShown) {
            // Si ya se mostró el onboarding, ir directamente al dashboard
            findNavController().navigate(R.id.action_splashFragment_to_dashboardFragment)
        } else {
            // Si es la primera vez, mostrar el onboarding
            findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
        }
    }
}