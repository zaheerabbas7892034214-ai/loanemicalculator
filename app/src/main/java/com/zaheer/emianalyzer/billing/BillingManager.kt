package com.zaheer.emianalyzer.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.zaheer.emianalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) : PurchasesUpdatedListener {
    
    companion object {
        const val PRODUCT_ID_PRO = "emi_pro_unlock"
    }
    
    private var billingClient: BillingClient? = null
    
    private val _isPro = MutableStateFlow(preferencesManager.isProUnlocked())
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        object Success : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }
    
    init {
        setupBillingClient()
    }
    
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        connectToBillingService()
    }
    
    private fun connectToBillingService() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Query existing purchases
                    queryPurchases()
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Try to reconnect
                connectToBillingService()
            }
        })
    }
    
    fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }
    
    private fun handlePurchases(purchases: List<Purchase>) {
        val proPurchase = purchases.find { purchase ->
            purchase.products.contains(PRODUCT_ID_PRO) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        
        if (proPurchase != null) {
            // Acknowledge purchase if not already done
            if (!proPurchase.isAcknowledged) {
                acknowledgePurchase(proPurchase)
            }
            
            // Update Pro status
            preferencesManager.setProUnlocked(true)
            proPurchase.purchaseToken.let { 
                preferencesManager.savePurchaseToken(it)
            }
            _isPro.value = true
        } else {
            preferencesManager.setProUnlocked(false)
            _isPro.value = false
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Purchase acknowledged
            }
        }
    }
    
    fun launchPurchaseFlow(activity: Activity) {
        _purchaseState.value = PurchaseState.Loading
        
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                billingClient?.launchBillingFlow(activity, billingFlowParams)
            } else {
                _purchaseState.value = PurchaseState.Error("Product not found")
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    handlePurchases(purchases)
                    _purchaseState.value = PurchaseState.Success
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Idle
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(
                    billingResult.debugMessage ?: "Purchase failed"
                )
            }
        }
    }
    
    fun restorePurchases() {
        _purchaseState.value = PurchaseState.Loading
        queryPurchases()
        _purchaseState.value = PurchaseState.Idle
    }
    
    fun endConnection() {
        billingClient?.endConnection()
    }
}
