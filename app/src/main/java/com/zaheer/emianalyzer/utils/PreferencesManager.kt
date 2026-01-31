package com.zaheer.emianalyzer.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "emi_analyzer_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_PRO_UNLOCKED = "pro_unlocked"
        private const val KEY_PURCHASE_TOKEN = "purchase_token"
    }
    
    fun setProUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PRO_UNLOCKED, unlocked).apply()
    }
    
    fun isProUnlocked(): Boolean {
        return prefs.getBoolean(KEY_PRO_UNLOCKED, false)
    }
    
    fun savePurchaseToken(token: String) {
        prefs.edit().putString(KEY_PURCHASE_TOKEN, token).apply()
    }
    
    fun getPurchaseToken(): String? {
        return prefs.getString(KEY_PURCHASE_TOKEN, null)
    }
    
    fun clearPurchaseData() {
        prefs.edit()
            .remove(KEY_PRO_UNLOCKED)
            .remove(KEY_PURCHASE_TOKEN)
            .apply()
    }
}
