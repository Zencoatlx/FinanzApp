package com.finanzapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.finanzapp.data.entity.Budget
import com.finanzapp.data.entity.SavingGoal
import com.finanzapp.data.entity.Transaction
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase para manejar la exportación de datos a Excel.
 */
class ExportManager(private val context: Context) {

    companion object {
        private const val TAG = "ExportManager"
    }

    /**
     * Exporta las transacciones a un archivo Excel.
     */
    fun exportTransactionsToExcel(transactions: List<Transaction>): Boolean {
        try {
            // Crear libro de trabajo
            val workbook: Workbook = HSSFWorkbook()

            // Crear hoja
            val sheet: Sheet = workbook.createSheet("Transacciones")

            // Crear encabezados
            val headerRow: Row = sheet.createRow(0)

            // Definir encabezados
            val headers = arrayOf("Fecha", "Categoría", "Descripción", "Monto", "Tipo")

            for (i in headers.indices) {
                val cell: Cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            // Llenar datos
            for (i in transactions.indices) {
                val row: Row = sheet.createRow(i + 1)
                val transaction = transactions[i]

                // Formatear fecha
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
                row.createCell(0).setCellValue(dateFormat.format(transaction.date))

                // Categoría
                row.createCell(1).setCellValue(transaction.category)

                // Descripción
                row.createCell(2).setCellValue(transaction.description)

                // Monto
                row.createCell(3).setCellValue(transaction.amount)

                // Tipo
                row.createCell(4).setCellValue(if (transaction.isIncome) "Ingreso" else "Gasto")
            }

            // Ajustar ancho de columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Transacciones_${getCurrentDateFormatted()}.xls")

        } catch (e: Exception) {
            Log.e(TAG, "Error exportando transacciones: ${e.message}")
            return false
        }
    }

    /**
     * Exporta las metas de ahorro a un archivo Excel.
     */
    fun exportSavingGoalsToExcel(savingGoals: List<SavingGoal>): Boolean {
        try {
            // Crear libro de trabajo
            val workbook: Workbook = HSSFWorkbook()
            val sheet: Sheet = workbook.createSheet("Metas de Ahorro")

            // Crear encabezados
            val headerRow: Row = sheet.createRow(0)

            // Definir encabezados
            val headers = arrayOf("Nombre", "Monto Objetivo", "Monto Actual", "Progreso", "Fecha Límite", "Fecha Creación")

            for (i in headers.indices) {
                val cell: Cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            // Llenar datos
            for (i in savingGoals.indices) {
                val row: Row = sheet.createRow(i + 1)
                val goal = savingGoals[i]

                // Nombre
                row.createCell(0).setCellValue(goal.name)

                // Monto Objetivo
                row.createCell(1).setCellValue(goal.targetAmount)

                // Monto Actual
                row.createCell(2).setCellValue(goal.currentAmount)

                // Progreso (porcentaje)
                val progress = if (goal.targetAmount > 0) goal.currentAmount / goal.targetAmount * 100 else 0.0
                row.createCell(3).setCellValue("${String.format("%.1f", progress)}%")

                // Fecha Límite
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
                row.createCell(4).setCellValue(goal.deadline?.let { dateFormat.format(it) } ?: "Sin fecha")

                // Fecha Creación
                row.createCell(5).setCellValue(dateFormat.format(goal.createdAt))
            }

            // Ajustar ancho de columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Metas_Ahorro_${getCurrentDateFormatted()}.xls")

        } catch (e: Exception) {
            Log.e(TAG, "Error exportando metas de ahorro: ${e.message}")
            return false
        }
    }

    /**
     * Exporta los presupuestos a un archivo Excel.
     */
    fun exportBudgetsToExcel(budgets: List<Budget>): Boolean {
        try {
            // Crear libro de trabajo
            val workbook: Workbook = HSSFWorkbook()
            val sheet: Sheet = workbook.createSheet("Presupuestos")

            // Crear encabezados
            val headerRow: Row = sheet.createRow(0)

            // Definir encabezados
            val headers = arrayOf("Categoría", "Fecha", "Monto Presupuestado", "Monto Gastado", "Porcentaje Usado", "Duración (meses)")

            for (i in headers.indices) {
                val cell: Cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            // Llenar datos
            for (i in budgets.indices) {
                val row: Row = sheet.createRow(i + 1)
                val budget = budgets[i]

                // Categoría
                row.createCell(0).setCellValue(budget.category)

                // Fecha
                val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
                row.createCell(1).setCellValue(dateFormat.format(budget.date))

                // Monto Presupuestado
                row.createCell(2).setCellValue(budget.amount)

                // Monto Gastado
                val spent = budget.spent ?: 0.0
                row.createCell(3).setCellValue(spent)

                // Porcentaje Usado
                val percentage = if (budget.amount > 0) spent / budget.amount * 100 else 0.0
                row.createCell(4).setCellValue("${String.format("%.1f", percentage)}%")

                // Duración
                cell.setCellValue(value.toString())
            }

            // Ajustar ancho de columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Presupuestos_${getCurrentDateFormatted()}.xls")

        } catch (e: Exception) {
            Log.e(TAG, "Error exportando presupuestos: ${e.message}")
            return false
        }
    }

    /**
     * Guarda un libro de Excel en el almacenamiento y comparte el archivo.
     */
    private fun saveExcelFile(workbook: Workbook, fileName: String): Boolean {
        try {
            // Crear directorio de archivos de la aplicación si no existe
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FinanzApp")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Crear archivo
            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)

            // Escribir datos y cerrar streams
            workbook.write(outputStream)
            outputStream.flush()
            outputStream.close()

            // Compartir archivo
            shareFile(file)

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando archivo Excel: ${e.message}")
            return false
        }
    }

    /**
     * Comparte un archivo utilizando una intent.
     */
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/vnd.ms-excel"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Iniciar la actividad de compartir
        val chooser = Intent.createChooser(intent, "Compartir Excel")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Obtiene la fecha actual formateada para nombres de archivo.
     */
    private fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}