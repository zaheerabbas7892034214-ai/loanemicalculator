package com.zaheer.emianalyzer.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.emianalyzer.billing.BillingManager
import com.zaheer.emianalyzer.domain.AmortizationEntry
import com.zaheer.emianalyzer.domain.EmiCalculator
import com.zaheer.emianalyzer.domain.EmiResult
import com.zaheer.emianalyzer.domain.LoanDetails
import com.zaheer.emianalyzer.utils.PdfExporter
import com.zaheer.emianalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class EmiViewModel(
    private val context: Context,
    private val billingManager: BillingManager
) : ViewModel() {
    
    private val _calculationResult = MutableStateFlow<EmiResult?>(null)
    val calculationResult: StateFlow<EmiResult?> = _calculationResult.asStateFlow()
    
    private val _amortizationSchedule = MutableStateFlow<List<AmortizationEntry>>(emptyList())
    val amortizationSchedule: StateFlow<List<AmortizationEntry>> = _amortizationSchedule.asStateFlow()
    
    private val _comparison1 = MutableStateFlow<EmiResult?>(null)
    val comparison1: StateFlow<EmiResult?> = _comparison1.asStateFlow()
    
    private val _comparison2 = MutableStateFlow<EmiResult?>(null)
    val comparison2: StateFlow<EmiResult?> = _comparison2.asStateFlow()
    
    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()
    
    val isPro: StateFlow<Boolean> = billingManager.isPro
    val purchaseState: StateFlow<BillingManager.PurchaseState> = billingManager.purchaseState
    
    sealed class PdfExportState {
        object Idle : PdfExportState()
        object Loading : PdfExportState()
        data class Success(val file: File) : PdfExportState()
        data class Error(val message: String) : PdfExportState()
    }
    
    fun calculateEmi(principal: Double, interestRate: Double, tenureMonths: Int) {
        viewModelScope.launch {
            val loanDetails = LoanDetails(principal, interestRate, tenureMonths)
            val result = EmiCalculator.calculateEmi(loanDetails)
            _calculationResult.value = result
            
            // Also generate amortization schedule
            val schedule = EmiCalculator.generateAmortizationSchedule(loanDetails)
            _amortizationSchedule.value = schedule
        }
    }
    
    fun calculateComparison(
        principal1: Double, rate1: Double, tenure1: Int,
        principal2: Double, rate2: Double, tenure2: Int
    ) {
        viewModelScope.launch {
            val loan1 = LoanDetails(principal1, rate1, tenure1)
            val loan2 = LoanDetails(principal2, rate2, tenure2)
            
            val (result1, result2) = EmiCalculator.compareLoanOptions(loan1, loan2)
            _comparison1.value = result1
            _comparison2.value = result2
        }
    }
    
    fun exportToPdf() {
        if (!isPro.value) {
            _pdfExportState.value = PdfExportState.Error("PDF export requires Pro version")
            return
        }
        
        viewModelScope.launch {
            try {
                _pdfExportState.value = PdfExportState.Loading
                
                val result = _calculationResult.value
                val schedule = _amortizationSchedule.value
                
                if (result != null) {
                    val file = PdfExporter.exportEmiCalculation(context, result, schedule)
                    _pdfExportState.value = PdfExportState.Success(file)
                } else {
                    _pdfExportState.value = PdfExportState.Error("No calculation to export")
                }
            } catch (e: Exception) {
                _pdfExportState.value = PdfExportState.Error(e.message ?: "Export failed")
            }
        }
    }
    
    fun exportComparisonToPdf() {
        if (!isPro.value) {
            _pdfExportState.value = PdfExportState.Error("PDF export requires Pro version")
            return
        }
        
        viewModelScope.launch {
            try {
                _pdfExportState.value = PdfExportState.Loading
                
                val result1 = _comparison1.value
                val result2 = _comparison2.value
                
                if (result1 != null && result2 != null) {
                    val file = PdfExporter.exportLoanComparison(context, result1, result2)
                    _pdfExportState.value = PdfExportState.Success(file)
                } else {
                    _pdfExportState.value = PdfExportState.Error("No comparison to export")
                }
            } catch (e: Exception) {
                _pdfExportState.value = PdfExportState.Error(e.message ?: "Export failed")
            }
        }
    }
    
    fun resetPdfExportState() {
        _pdfExportState.value = PdfExportState.Idle
    }
    
    fun launchPurchaseFlow(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }
    
    fun restorePurchases() {
        billingManager.restorePurchases()
    }
    
    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
