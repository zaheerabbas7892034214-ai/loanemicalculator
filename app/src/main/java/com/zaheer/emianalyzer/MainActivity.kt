package com.zaheer.emianalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zaheer.emianalyzer.billing.BillingManager
import com.zaheer.emianalyzer.ui.screens.*
import com.zaheer.emianalyzer.ui.theme.LoanEMIAnalyzerTheme
import com.zaheer.emianalyzer.ui.viewmodel.EmiViewModel
import com.zaheer.emianalyzer.utils.PreferencesManager

class MainActivity : ComponentActivity() {
    
    private lateinit var billingManager: BillingManager
    private lateinit var viewModel: EmiViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        val preferencesManager = PreferencesManager(applicationContext)
        billingManager = BillingManager(applicationContext, preferencesManager)
        viewModel = EmiViewModel(applicationContext, billingManager)
        
        setContent {
            LoanEMIAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "calculator"
                    ) {
                        composable("calculator") {
                            CalculatorScreen(
                                viewModel = viewModel,
                                onNavigateToAmortization = {
                                    navController.navigate("amortization")
                                },
                                onNavigateToComparison = {
                                    navController.navigate("comparison")
                                },
                                onNavigateToUpgrade = {
                                    navController.navigate("upgrade")
                                }
                            )
                        }
                        
                        composable("amortization") {
                            AmortizationScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("comparison") {
                            ComparisonScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToUpgrade = {
                                    navController.navigate("upgrade")
                                }
                            )
                        }
                        
                        composable("upgrade") {
                            UpgradeScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}
