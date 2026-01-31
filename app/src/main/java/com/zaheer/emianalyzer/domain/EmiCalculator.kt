package com.zaheer.emianalyzer.domain

import kotlin.math.pow

data class LoanDetails(
    val principal: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int
)

data class EmiResult(
    val emi: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val loanDetails: LoanDetails
)

data class AmortizationEntry(
    val month: Int,
    val emi: Double,
    val principalPaid: Double,
    val interestPaid: Double,
    val balance: Double
)

object EmiCalculator {
    
    /**
     * Calculate EMI using the formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)
     * where:
     * P = Principal loan amount
     * r = Monthly interest rate (annual rate / 12 / 100)
     * n = Loan tenure in months
     */
    fun calculateEmi(loanDetails: LoanDetails): EmiResult {
        val principal = loanDetails.principal
        val annualRate = loanDetails.annualInterestRate
        val months = loanDetails.tenureMonths
        
        // Handle edge case: 0% interest
        if (annualRate == 0.0) {
            val emi = principal / months
            return EmiResult(
                emi = emi,
                totalInterest = 0.0,
                totalPayment = principal,
                loanDetails = loanDetails
            )
        }
        
        // Calculate monthly interest rate
        val monthlyRate = annualRate / 12 / 100
        
        // EMI = P × r × (1+r)^n / ((1+r)^n - 1)
        val factor = (1 + monthlyRate).pow(months)
        val emi = principal * monthlyRate * factor / (factor - 1)
        
        val totalPayment = emi * months
        val totalInterest = totalPayment - principal
        
        return EmiResult(
            emi = emi,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            loanDetails = loanDetails
        )
    }
    
    /**
     * Generate amortization schedule
     */
    fun generateAmortizationSchedule(loanDetails: LoanDetails): List<AmortizationEntry> {
        val emiResult = calculateEmi(loanDetails)
        val emi = emiResult.emi
        val monthlyRate = loanDetails.annualInterestRate / 12 / 100
        
        val schedule = mutableListOf<AmortizationEntry>()
        var balance = loanDetails.principal
        
        for (month in 1..loanDetails.tenureMonths) {
            val interestPaid = balance * monthlyRate
            val principalPaid = emi - interestPaid
            balance -= principalPaid
            
            // Handle rounding errors in last month
            val actualBalance = if (month == loanDetails.tenureMonths) 0.0 else balance
            
            schedule.add(
                AmortizationEntry(
                    month = month,
                    emi = emi,
                    principalPaid = principalPaid,
                    interestPaid = interestPaid,
                    balance = actualBalance.coerceAtLeast(0.0)
                )
            )
        }
        
        return schedule
    }
    
    /**
     * Compare two loan options
     */
    fun compareLoanOptions(loan1: LoanDetails, loan2: LoanDetails): Pair<EmiResult, EmiResult> {
        return Pair(calculateEmi(loan1), calculateEmi(loan2))
    }
}
