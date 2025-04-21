package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.finanzapp.R
import com.finanzapp.util.PremiumManager

class PremiumFragment : Fragment() {

    private lateinit var textWelcome: TextView
    private lateinit var textTrialInfo: TextView
    private lateinit var buttonMonthly: Button
    private lateinit var buttonYearly: Button
    private lateinit var buttonLifetime: Button
    private lateinit var premiumManager: PremiumManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        premiumManager = PremiumManager.getInstance(requireContext())

        // Inicializar vistas
        textWelcome = view.findViewById(R.id.textWelcome)
        textTrialInfo = view.findViewById(R.id.textTrialInfo)
        buttonMonthly = view.findViewById(R.id.buttonMonthly)
        buttonYearly = view.findViewById(R.id.buttonYearly)
        buttonLifetime = view.findViewById(R.id.buttonLifetime)

        // Mostrar información de prueba si está disponible
        if (premiumManager.hasTrialPeriod) {
            textTrialInfo.visibility = View.VISIBLE
            textTrialInfo.text = "Tienes un periodo de prueba gratuito de ${premiumManager.trialDaysRemaining} días."
        } else {
            textTrialInfo.visibility = View.GONE
        }

        // Observar cambios en los precios
        premiumManager.monthlyPrice.observe(viewLifecycleOwner) { price ->
            buttonMonthly.text = "Mensual: $price/mes"
        }

        premiumManager.yearlyPrice.observe(viewLifecycleOwner) { price ->
            buttonYearly.text = "Anual: $price/año"
        }

        premiumManager.lifetimePrice.observe(viewLifecycleOwner) { price ->
            buttonLifetime.text = "De por vida: $price"
        }

        // Configurar botones de compra
        buttonMonthly.setOnClickListener {
            if (premiumManager.isFeatureSupported(PremiumManager.PRODUCT_ID_MONTHLY)) {
                premiumManager.launchBillingFlow(requireActivity(), PremiumManager.PRODUCT_ID_MONTHLY)
            }
        }

        buttonYearly.setOnClickListener {
            if (premiumManager.isFeatureSupported(PremiumManager.PRODUCT_ID_YEARLY)) {
                premiumManager.launchBillingFlow(requireActivity(), PremiumManager.PRODUCT_ID_YEARLY)
            }
        }

        buttonLifetime.setOnClickListener {
            if (premiumManager.isFeatureSupported(PremiumManager.PRODUCT_ID_LIFETIME)) {
                premiumManager.launchBillingFlow(requireActivity(), PremiumManager.PRODUCT_ID_LIFETIME)
            }
        }

        // Observar cambios en el estado premium
        premiumManager.isPremium.observe(viewLifecycleOwner) { isPremium ->
            if (isPremium) {
                textWelcome.text = "¡Gracias por ser Premium!"
                buttonMonthly.visibility = View.GONE
                buttonYearly.visibility = View.GONE
                buttonLifetime.visibility = View.GONE
                textTrialInfo.visibility = View.GONE
            } else {
                textWelcome.text = "Mejora a Premium"
            }
        }
    }
}