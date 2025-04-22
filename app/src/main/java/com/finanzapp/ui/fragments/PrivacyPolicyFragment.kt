package com.finanzapp.ui.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.finanzapp.R

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewPrivacyPolicy = view.findViewById<TextView>(R.id.textViewPrivacyPolicy)

        // Cargar el texto HTML de la política de privacidad
        val policyText = getText(R.string.privacy_policy_text)
        textViewPrivacyPolicy.text = Html.fromHtml(policyText.toString(), Html.FROM_HTML_MODE_COMPACT)
    }
}