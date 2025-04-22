package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.finanzapp.R
import com.finanzapp.ui.adapters.OnboardingAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonNext: Button
    private lateinit var buttonSkip: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView

    private val onboardingItems = listOf(
        OnboardingItem(
            "Controla tus Finanzas",
            "Registra ingresos y gastos fácilmente para llevar un control detallado de tu dinero."
        ),
        OnboardingItem(
            "Crea Presupuestos",
            "Establece límites para diferentes categorías y evita gastos innecesarios."
        ),
        OnboardingItem(
            "Fija Metas de Ahorro",
            "Define objetivos de ahorro y haz seguimiento de tu progreso hasta alcanzarlos."
        ),
        OnboardingItem(
            "Analiza tus Estadísticas",
            "Visualiza gráficos de tus gastos e ingresos para tomar mejores decisiones financieras."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        viewPager = view.findViewById(R.id.viewPager)
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonSkip = view.findViewById(R.id.buttonSkip)
        tabLayout = view.findViewById(R.id.tabLayout)
        textTitle = view.findViewById(R.id.textTitle)
        textDescription = view.findViewById(R.id.textDescription)

        // Configurar adaptador
        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        // Configurar indicadores de página
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Mostrar primer ítem
        updateContent(0)

        // Configurar cambio de página
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateContent(position)

                // Cambiar texto del botón en la última página
                if (position == onboardingItems.size - 1) {
                    buttonNext.text = "Comenzar"
                } else {
                    buttonNext.text = "Siguiente"
                }
            }
        })

        // Configurar botón Siguiente/Comenzar
        buttonNext.setOnClickListener {
            val currentPosition = viewPager.currentItem

            if (currentPosition < onboardingItems.size - 1) {
                // Ir a la siguiente página
                viewPager.currentItem = currentPosition + 1
            } else {
                // Finalizar onboarding y navegar a Dashboard
                finishOnboarding()
            }
        }

        // Configurar botón Saltar
        buttonSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateContent(position: Int) {
        val item = onboardingItems[position]
        textTitle.text = item.title
        textDescription.text = item.description
    }

    private fun finishOnboarding() {
        // Guardar en SharedPreferences que el onboarding ya fue mostrado
        val sharedPrefs = requireActivity().getSharedPreferences("FinanzAppPrefs", 0)
        sharedPrefs.edit().putBoolean("onboarding_shown", true).apply()

        // Navegar al Dashboard
        findNavController().navigate(R.id.action_onboardingFragment_to_dashboardFragment)
    }

    data class OnboardingItem(val title: String, val description: String)
}