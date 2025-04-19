package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.finanzapp.R
import com.finanzapp.util.PremiumManager

class PremiumFragment : Fragment() {

    private lateinit var premiumManager: PremiumManager

    // Views que vamos a usar
    private lateinit var textTrialInfo: TextView
    private lateinit var buttonSubscribeMonthly: Button
    private lateinit var buttonSubscribeYearly: Button
    private lateinit var buttonSubscribeLifetime: Button
    private lateinit var buttonRestorePurchases: Button
    private lateinit var buttonCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el administrador de premium
        premiumManager = PremiumManager(requireContext())

        // Inicializar vistas
        textTrialInfo = view.findViewById(R.id.textTrialInfo)
        buttonSubscribeMonthly = view.findViewById(R.id.buttonSubscribeMonthly)
        buttonSubscribeYearly = view.findViewById(R.id.buttonSubscribeYearly)
        buttonSubscribeLifetime = view.findViewById(R.id.buttonSubscribeLifetime)
        buttonRestorePurchases = view.findViewById(R.id.buttonRestorePurchases)
        buttonCancel = view.findViewById(R.id.buttonCancel)

        // Observar cambios en el estado premium
        premiumManager.isPremium.observe(viewLifecycleOwner) { isPremium ->
            if (isPremium) {
                // Si ya es premium, mostrar mensaje y ocultar botones de compra
                textTrialInfo.text = "¡Ya tienes Premium! Disfruta de todas las funciones."
                textTrialInfo.visibility = View.VISIBLE
                hideSubscriptionButtons()
            }
        }

        // Observar período de prueba
        premiumManager.hasTrialPeriod.observe(viewLifecycleOwner) { hasTrial ->
            val daysRemaining = premiumManager.trialDaysRemaining.value ?: 0
            if (hasTrial && daysRemaining > 0) {
                textTrialInfo.text = "Disfruta de tu período de prueba gratuito: $daysRemaining días restantes"
            } else {
                textTrialInfo.text = "Elige tu plan Premium favorito"
            }
        }

        // Configurar botones
        buttonSubscribeMonthly.setOnClickListener {
            launchPremiumPurchase(PremiumManager.PRODUCT_ID_MONTHLY)
        }

        buttonSubscribeYearly.setOnClickListener {
            launchPremiumPurchase(PremiumManager.PRODUCT_ID_YEARLY)
        }

        buttonSubscribeLifetime.setOnClickListener {
            launchPremiumPurchase(PremiumManager.PRODUCT_ID_LIFETIME)
        }

        // Restaurar compras
        buttonRestorePurchases.setOnClickListener {
            Toast.makeText(requireContext(), "Verificando compras anteriores...", Toast.LENGTH_SHORT).show()
            premiumManager.queryPurchases()
        }

        // Volver atrás
        buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun launchPremiumPurchase(productId: String) {
        if (premiumManager.isFeatureSupported()) {
            premiumManager.launchBillingFlow(productId, requireActivity())
        } else {
            Toast.makeText(
                requireContext(),
                "Las compras dentro de la aplicación no están disponibles en este dispositivo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hideSubscriptionButtons() {
        buttonSubscribeMonthly.visibility = View.GONE
        buttonSubscribeYearly.visibility = View.GONE
        buttonSubscribeLifetime.visibility = View.GONE
    }
}