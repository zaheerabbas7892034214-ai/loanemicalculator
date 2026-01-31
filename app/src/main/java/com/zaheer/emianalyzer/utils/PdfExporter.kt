package com.zaheer.emianalyzer.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.zaheer.emianalyzer.domain.AmortizationEntry
import com.zaheer.emianalyzer.domain.EmiResult
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {
    
    private val currencyFormat = DecimalFormat("#,##,##0.00")
    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
    
    fun exportEmiCalculation(
        context: Context,
        emiResult: EmiResult,
        amortizationSchedule: List<AmortizationEntry>
    ): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "EMI_Report_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)
        
        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)
        
        // Title
        document.add(
            Paragraph("Loan EMI Analysis Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setBold()
        )
        
        document.add(
            Paragraph("Generated on: ${dateFormat.format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
        )
        
        document.add(Paragraph("\n"))
        
        // Loan Details
        document.add(Paragraph("Loan Details").setBold().setFontSize(14f))
        document.add(Paragraph("Principal Amount: ₹${currencyFormat.format(emiResult.loanDetails.principal)}"))
        document.add(Paragraph("Interest Rate: ${emiResult.loanDetails.annualInterestRate}% per annum"))
        document.add(Paragraph("Tenure: ${emiResult.loanDetails.tenureMonths} months"))
        
        document.add(Paragraph("\n"))
        
        // EMI Calculation
        document.add(Paragraph("EMI Calculation").setBold().setFontSize(14f))
        document.add(Paragraph("Monthly EMI: ₹${currencyFormat.format(emiResult.emi)}"))
        document.add(Paragraph("Total Interest: ₹${currencyFormat.format(emiResult.totalInterest)}"))
        document.add(Paragraph("Total Payment: ₹${currencyFormat.format(emiResult.totalPayment)}"))
        
        document.add(Paragraph("\n"))
        
        // Amortization Schedule
        if (amortizationSchedule.isNotEmpty()) {
            document.add(Paragraph("Amortization Schedule").setBold().setFontSize(14f))
            
            val table = Table(floatArrayOf(1f, 2f, 2f, 2f, 2f))
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100f))
            
            // Headers
            table.addHeaderCell("Month")
            table.addHeaderCell("EMI")
            table.addHeaderCell("Principal")
            table.addHeaderCell("Interest")
            table.addHeaderCell("Balance")
            
            // Data rows
            for (entry in amortizationSchedule) {
                table.addCell(entry.month.toString())
                table.addCell("₹${currencyFormat.format(entry.emi)}")
                table.addCell("₹${currencyFormat.format(entry.principalPaid)}")
                table.addCell("₹${currencyFormat.format(entry.interestPaid)}")
                table.addCell("₹${currencyFormat.format(entry.balance)}")
            }
            
            document.add(table)
        }
        
        document.close()
        
        return file
    }
    
    fun exportLoanComparison(
        context: Context,
        result1: EmiResult,
        result2: EmiResult
    ): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "Loan_Comparison_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)
        
        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)
        
        // Title
        document.add(
            Paragraph("Loan Comparison Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setBold()
        )
        
        document.add(
            Paragraph("Generated on: ${dateFormat.format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
        )
        
        document.add(Paragraph("\n"))
        
        // Comparison Table
        val table = Table(floatArrayOf(3f, 2f, 2f))
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100f))
        
        table.addHeaderCell("Parameter")
        table.addHeaderCell("Option 1")
        table.addHeaderCell("Option 2")
        
        table.addCell("Principal Amount")
        table.addCell("₹${currencyFormat.format(result1.loanDetails.principal)}")
        table.addCell("₹${currencyFormat.format(result2.loanDetails.principal)}")
        
        table.addCell("Interest Rate")
        table.addCell("${result1.loanDetails.annualInterestRate}%")
        table.addCell("${result2.loanDetails.annualInterestRate}%")
        
        table.addCell("Tenure (months)")
        table.addCell("${result1.loanDetails.tenureMonths}")
        table.addCell("${result2.loanDetails.tenureMonths}")
        
        table.addCell("Monthly EMI")
        table.addCell("₹${currencyFormat.format(result1.emi)}")
        table.addCell("₹${currencyFormat.format(result2.emi)}")
        
        table.addCell("Total Interest")
        table.addCell("₹${currencyFormat.format(result1.totalInterest)}")
        table.addCell("₹${currencyFormat.format(result2.totalInterest)}")
        
        table.addCell("Total Payment")
        table.addCell("₹${currencyFormat.format(result1.totalPayment)}")
        table.addCell("₹${currencyFormat.format(result2.totalPayment)}")
        
        document.add(table)
        
        document.add(Paragraph("\n"))
        
        // Recommendation
        val savings = kotlin.math.abs(result1.totalInterest - result2.totalInterest)
        val betterOption = if (result1.totalInterest < result2.totalInterest) "Option 1" else "Option 2"
        
        document.add(Paragraph("Recommendation:").setBold().setFontSize(14f))
        document.add(
            Paragraph("$betterOption has lower total interest payment.")
        )
        document.add(
            Paragraph("Interest savings: ₹${currencyFormat.format(savings)}")
        )
        
        document.close()
        
        return file
    }
}
