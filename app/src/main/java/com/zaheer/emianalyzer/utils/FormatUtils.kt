package com.zaheer.emianalyzer.utils

import java.text.DecimalFormat

object FormatUtils {
    
    private val currencyFormat = DecimalFormat("#,##,##0.00")
    private val percentageFormat = DecimalFormat("0.00")
    
    fun formatCurrency(amount: Double): String {
        return "₹${currencyFormat.format(amount)}"
    }
    
    fun formatPercentage(value: Double): String {
        return "${percentageFormat.format(value)}%"
    }
    
    fun formatMonths(months: Int): String {
        val years = months / 12
        val remainingMonths = months % 12
        
        return when {
            years == 0 -> "$months months"
            remainingMonths == 0 -> "$years year${if (years > 1) "s" else ""}"
            else -> "$years year${if (years > 1) "s" else ""} $remainingMonths month${if (remainingMonths > 1) "s" else ""}"
        }
    }
}
