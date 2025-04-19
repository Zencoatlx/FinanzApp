package com.finanzapp.ui.fragments

import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.finanzapp.R
import com.finanzapp.data.entity.Transaction
import com.finanzapp.ui.viewmodel.TransactionViewModel
import com.finanzapp.util.ExportManager
import com.finanzapp.util.PremiumManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var premiumManager: PremiumManager
    private lateinit var exportManager: ExportManager
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var textTotalIncome: TextView
    private lateinit var textTotalExpenses: TextView
    private lateinit var textDailyAverage: TextView
    private lateinit var textLargestExpense: TextView
    private lateinit var spinnerTimeRange: Spinner
    private lateinit var buttonExport: Button

    private var currentTimeRange = "Este mes"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        // Inicializar Premium Manager
        premiumManager = PremiumManager(requireContext())

        // Inicializar Export Manager
        exportManager = ExportManager(requireContext())

        // Inicializar vistas
        barChart = view.findViewById(R.id.barChartIncomeExpense)
        pieChart = view.findViewById(R.id.pieChartExpenseDistribution)
        textTotalIncome = view.findViewById(R.id.textTotalIncome)
        textTotalExpenses = view.findViewById(R.id.textTotalExpenses)
        textDailyAverage = view.findViewById(R.id.textDailyAverage)
        textLargestExpense = view.findViewById(R.id.textLargestExpense)
        spinnerTimeRange = view.findViewById(R.id.spinnerTimeRange)
        buttonExport = view.findViewById(R.id.buttonExport)

        // Configurar spinner de rango de tiempo
        val timeRanges = arrayOf("Esta semana", "Este mes", "Este año", "Todo")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter
        spinnerTimeRange.setSelection(1) // Por defecto "Este mes"

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentTimeRange = timeRanges[position]
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configurar botón de exportar
        buttonExport.setOnClickListener {
            // Verificar si es usuario premium para exportar
            premiumManager.isPremium.observe(viewLifecycleOwner) { isPremium ->
                if (isPremium) {
                    exportData()
                } else {
                    showPremiumDialog("Exportar a Excel")
                }
            }
        }

        // Configurar gráficos
        setupBarChart()
        setupPieChart()

        // Cargar datos
        loadData()

        // Observar cambios en premium para habilitar/deshabilitar botones
        premiumManager.isPremium.observe(viewLifecycleOwner) { isPremium ->
            buttonExport.isEnabled = isPremium
            if (!isPremium) {
                buttonExport.text = "Exportar (Premium)"
            } else {
                buttonExport.text = "Exportar a Excel"
            }
        }
    }

    private fun showPremiumDialog(feature: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Función Premium")
            .setMessage("La función '$feature' está disponible solo para usuarios premium. ¿Deseas actualizar ahora?")
            .setPositiveButton("Ver opciones Premium") { _, _ ->
                findNavController().navigate(R.id.action_statsFragment_to_premiumFragment)
            }
            .setNegativeButton("Más tarde", null)
            .show()
    }

    private fun exportData() {
        val dateRange = getDateRange(currentTimeRange)
        viewModel.getTransactionsByDateRange(dateRange.first, dateRange.second)
            .observe(viewLifecycleOwner) { transactions ->
                if (transactions.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                if (exportManager.exportTransactionsToExcel(transactions)) {
                    Toast.makeText(requireContext(), "Archivo Excel generado con éxito", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Error al exportar datos", Toast.LENGTH_SHORT).show()
                    showPremiumDialog("Exportar a Excel")
                }
            }
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setScaleEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.isEnabled = true
        pieChart.holeRadius = 40f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Gastos"
    }

    private fun loadData() {
        val dateRange = getDateRange(currentTimeRange)
        val startDate = dateRange.first
        val endDate = dateRange.second

        viewModel.getTransactionsByDateRange(startDate, endDate).observe(viewLifecycleOwner) { transactions ->
            updateCharts(transactions)
            updateStats(transactions)
        }
    }

    private fun getDateRange(timeRange: String): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        when (timeRange) {
            "Esta semana" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "Este mes" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "Este año" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "Todo" -> {
                // Usar una fecha muy antigua
                calendar.set(2000, 0, 1)
            }
        }

        val startDate = calendar.time
        return Pair(startDate, endDate)
    }

    private fun updateCharts(transactions: List<Transaction>) {
        updateBarChart(transactions)
        updatePieChart(transactions)
    }

    private fun updateBarChart(transactions: List<Transaction>) {
        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        when (currentTimeRange) {
            "Esta semana" -> {
                // Agrupar por día de la semana
                val calendar = Calendar.getInstance()
                val daysOfWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

                for (i in 0..6) {
                    var totalIncome = 0f
                    var totalExpense = 0f

                    transactions.forEach { transaction ->
                        calendar.time = transaction.date
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-6 (Dom-Sáb)

                        if (dayOfWeek == i) {
                            if (transaction.isIncome) {
                                totalIncome += transaction.amount.toFloat()
                            } else {
                                totalExpense += transaction.amount.toFloat()
                            }
                        }
                    }

                    incomeEntries.add(BarEntry(i.toFloat(), totalIncome))
                    expenseEntries.add(BarEntry(i.toFloat(), totalExpense))
                    labels.add(daysOfWeek[i])
                }
            }
            "Este mes" -> {
                // Agrupar por semana del mes
                val weeksInMonth = 5

                for (i in 0 until weeksInMonth) {
                    var totalIncome = 0f
                    var totalExpense = 0f

                    transactions.forEach { transaction ->
                        val cal = Calendar.getInstance()
                        cal.time = transaction.date
                        val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH) - 1

                        if (weekOfMonth == i) {
                            if (transaction.isIncome) {
                                totalIncome += transaction.amount.toFloat()
                            } else {
                                totalExpense += transaction.amount.toFloat()
                            }
                        }
                    }

                    incomeEntries.add(BarEntry(i.toFloat(), totalIncome))
                    expenseEntries.add(BarEntry(i.toFloat(), totalExpense))
                    labels.add("Sem ${i + 1}")
                }
            }
            "Este año" -> {
                // Agrupar por mes
                val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

                for (i in 0..11) {
                    var totalIncome = 0f
                    var totalExpense = 0f

                    transactions.forEach { transaction ->
                        val cal = Calendar.getInstance()
                        cal.time = transaction.date
                        val month = cal.get(Calendar.MONTH)

                        if (month == i) {
                            if (transaction.isIncome) {
                                totalIncome += transaction.amount.toFloat()
                            } else {
                                totalExpense += transaction.amount.toFloat()
                            }
                        }
                    }

                    incomeEntries.add(BarEntry(i.toFloat(), totalIncome))
                    expenseEntries.add(BarEntry(i.toFloat(), totalExpense))
                    labels.add(months[i])
                }
            }
            "Todo" -> {
                // Agrupar por año
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val startYear = 2020 // Año inicial arbitrario

                for (i in startYear..currentYear) {
                    var totalIncome = 0f
                    var totalExpense = 0f

                    transactions.forEach { transaction ->
                        calendar.time = transaction.date
                        val year = calendar.get(Calendar.YEAR)

                        if (year == i) {
                            if (transaction.isIncome) {
                                totalIncome += transaction.amount.toFloat()
                            } else {
                                totalExpense += transaction.amount.toFloat()
                            }
                        }
                    }

                    incomeEntries.add(BarEntry((i - startYear).toFloat(), totalIncome))
                    expenseEntries.add(BarEntry((i - startYear).toFloat(), totalExpense))
                    labels.add(i.toString())
                }
            }
        }

        val incomeSet = BarDataSet(incomeEntries, "Ingresos")
        incomeSet.color = Color.GREEN

        val expenseSet = BarDataSet(expenseEntries, "Gastos")
        expenseSet.color = Color.RED

        val data = BarData(incomeSet, expenseSet)
        data.barWidth = 0.3f

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.labelCount = labels.size

        // Agrupar las barras
        val groupSpace = 0.4f
        val barSpace = 0.0f

        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.invalidate()
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        // Agrupar gastos por categoría
        val expensesByCategory = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            if (!transaction.isIncome) {
                val currentAmount = expensesByCategory[transaction.category] ?: 0f
                expensesByCategory[transaction.category] = currentAmount + transaction.amount.toFloat()
            }
        }

        val entries = ArrayList<PieEntry>()
        expensesByCategory.forEach { (category, amount) ->
            entries.add(PieEntry(amount, category))
        }

        val dataSet = PieDataSet(entries, "Categorías")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val format = NumberFormat.getPercentInstance(Locale("es", "MX"))
                return format.format(value / 100)
            }
        })

        pieChart.data = data
        pieChart.invalidate()
    }

    private fun updateStats(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpenses = 0.0
        var largestExpense = 0.0

        transactions.forEach { transaction ->
            if (transaction.isIncome) {
                totalIncome += transaction.amount
            } else {
                totalExpenses += transaction.amount
                if (transaction.amount > largestExpense) {
                    largestExpense = transaction.amount
                }
            }
        }

        // Calcular promedio diario de gastos
        val dateRange = getDateRange(currentTimeRange)
        val startDate = dateRange.first
        val endDate = dateRange.second
        val days = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        val dailyAverage = totalExpenses / days

        // Formatear valores
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        textTotalIncome.text = format.format(totalIncome)
        textTotalExpenses.text = format.format(totalExpenses)
        textDailyAverage.text = format.format(dailyAverage)
        textLargestExpense.text = format.format(largestExpense)
    }
}