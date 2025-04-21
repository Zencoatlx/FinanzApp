package com.finanzapp.util

object TransactionUtils {
    private val expenseCategories = mapOf(
        "Comida" to listOf("supermercado", "restaurant", "comida", "café", "pizza", "desayuno", "almuerzo", "cena", "mercado"),
        "Transporte" to listOf("gasolina", "uber", "taxi", "metro", "bus", "estacionamiento", "pasaje"),
        "Vivienda" to listOf("renta", "alquiler", "hipoteca", "agua", "luz", "gas", "internet"),
        "Salud" to listOf("médico", "doctor", "farmacia", "medicina", "consulta", "hospital"),
        "Ocio" to listOf("cine", "teatro", "concierto", "viaje", "vacaciones", "hotel", "juego"),
        "Educación" to listOf("colegio", "universidad", "curso", "libro", "clase"),
        "Ropa" to listOf("ropa", "zapatos", "vestido", "pantalón", "camisa"),
        "Servicios" to listOf("teléfono", "móvil", "suscripción", "streaming", "netflix", "spotify")
    )

    private val incomeCategories = mapOf(
        "Salario" to listOf("salario", "nómina", "sueldo", "pago"),
        "Inversiones" to listOf("dividendo", "interés", "inversión", "acción", "bolsa"),
        "Regalos" to listOf("regalo", "cumpleaños", "navidad"),
        "Ventas" to listOf("venta", "cliente", "producto", "servicio"),
        "Reembolsos" to listOf("reembolso", "devolución", "retorno")
    )

    fun suggestCategory(description: String, isIncome: Boolean): String {
        val categories = if (isIncome) incomeCategories else expenseCategories
        val descLower = description.lowercase()

        for ((category, keywords) in categories) {
            for (keyword in keywords) {
                if (descLower.contains(keyword)) {
                    return category
                }
            }
        }

        return if (isIncome) "Otro ingreso" else "Otro gasto"
    }
}