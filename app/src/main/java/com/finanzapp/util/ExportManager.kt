package com.finanzapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.finanzapp.data.entity.Transaction
import com.finanzapp.data.entity.SavingGoal
import com.finanzapp.data.entity.Budget
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(private val context: Context) {

    fun exportTransactionsToExcel(transactions: List<Transaction>): Boolean {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transacciones")

            // Estilos
            val headerStyle = workbook.createCellStyle()
            headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

            val dateStyle = workbook.createCellStyle()
            val createHelper = workbook.creationHelper
            dateStyle.dataFormat = createHelper.createDataFormat().getFormat("dd/mm/yyyy")

            // Crear cabecera
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Descripción", "Monto", "Categoría", "Tipo", "Fecha", "Recurrente")

            for (i in headers.indices) {
                val cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
                cell.cellStyle = headerStyle
            }

            // Llenar datos
            for (i in transactions.indices) {
                val transaction = transactions[i]
                val row = sheet.createRow(i + 1)

                row.createCell(0).setCellValue(transaction.id.toDouble())
                row.createCell(1).setCellValue(transaction.description)
                row.createCell(2).setCellValue(transaction.amount)
                row.createCell(3).setCellValue(transaction.category)
                row.createCell(4).setCellValue(if (transaction.isIncome) "Ingreso" else "Gasto")

                val dateCell = row.createCell(5)
                dateCell.setCellValue(transaction.date)
                dateCell.cellStyle = dateStyle

                row.createCell(6).setCellValue(if (transaction.isRecurring) "Sí" else "No")
            }

            // Autoajustar columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Transacciones_${getCurrentDate()}.xlsx")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun exportSavingsToExcel(savingGoals: List<SavingGoal>): Boolean {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Metas de Ahorro")

            // Estilos
            val headerStyle = workbook.createCellStyle()
            headerStyle.fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

            val dateStyle = workbook.createCellStyle()
            val createHelper = workbook.creationHelper
            dateStyle.dataFormat = createHelper.createDataFormat().getFormat("dd/mm/yyyy")

            // Crear cabecera
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Nombre", "Monto Objetivo", "Monto Actual", "Progreso", "Fecha Límite", "Creado")

            for (i in headers.indices) {
                val cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
                cell.cellStyle = headerStyle
            }

            // Llenar datos
            for (i in savingGoals.indices) {
                val savingGoal = savingGoals[i]
                val row = sheet.createRow(i + 1)

                row.createCell(0).setCellValue(savingGoal.id.toDouble())
                row.createCell(1).setCellValue(savingGoal.name)
                row.createCell(2).setCellValue(savingGoal.targetAmount)
                row.createCell(3).setCellValue(savingGoal.currentAmount)

                val progress = if (savingGoal.targetAmount > 0)
                    savingGoal.currentAmount / savingGoal.targetAmount * 100
                else 0.0
                row.createCell(4).setCellValue("${"%.2f".format(progress)}%")

                if (savingGoal.deadline != null) {
                    val deadlineCell = row.createCell(5)
                    deadlineCell.setCellValue(savingGoal.deadline)
                    deadlineCell.cellStyle = dateStyle
                } else {
                    row.createCell(5).setCellValue("Sin fecha límite")
                }

                val createdCell = row.createCell(6)
                createdCell.setCellValue(savingGoal.createdAt)
                createdCell.cellStyle = dateStyle
            }

            // Autoajustar columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Metas_de_Ahorro_${getCurrentDate()}.xlsx")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun exportBudgetsToExcel(budgets: List<Budget>): Boolean {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Presupuestos")

            // Estilos
            val headerStyle = workbook.createCellStyle()
            headerStyle.fillForegroundColor = IndexedColors.LIGHT_ORANGE.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

            val dateStyle = workbook.createCellStyle()
            val createHelper = workbook.creationHelper
            dateStyle.dataFormat = createHelper.createDataFormat().getFormat("dd/mm/yyyy")

            // Crear cabecera
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "Nombre", "Monto Límite", "Gastado", "Progreso", "Categoría", "Creado")

            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
            }

            // Llenar datos
            for (i in budgets.indices) {
                val budget = budgets[i]
                val row = sheet.createRow(i + 1)

                row.createCell(0).setCellValue(budget.id.toDouble())
                row.createCell(1).setCellValue(budget.name)
                row.createCell(2).setCellValue(budget.amount)
                row.createCell(3).setCellValue(budget.spent)

                val progress = if (budget.amount > 0)
                    budget.spent / budget.amount * 100
                else 0.0
                row.createCell(4).setCellValue("${"%.2f".format(progress)}%")

                row.createCell(5).setCellValue(budget.category)

                val createdCell = row.createCell(6)
                createdCell.setCellValue(budget.createdAt)
                createdCell.cellStyle = dateStyle
            }

            // Autoajustar columnas
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            // Guardar archivo
            return saveExcelFile(workbook, "Presupuestos_${getCurrentDate()}.xlsx")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun saveExcelFile(workbook: XSSFWorkbook, fileName: String): Boolean {
        try {
            // Guardar en almacenamiento externo
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()

            // Compartir el archivo
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(Intent.createChooser(intent, "Abrir con..."))

            Toast.makeText(context, "Archivo guardado en Descargas", Toast.LENGTH_LONG).show()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}