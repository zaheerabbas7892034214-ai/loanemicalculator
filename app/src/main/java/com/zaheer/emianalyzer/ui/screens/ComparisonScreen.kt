package com.zaheer.emianalyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaheer.emianalyzer.domain.EmiResult
import com.zaheer.emianalyzer.ui.viewmodel.EmiViewModel
import com.zaheer.emianalyzer.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    viewModel: EmiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    var principal1 by remember { mutableStateOf("") }
    var rate1 by remember { mutableStateOf("") }
    var tenure1 by remember { mutableStateOf("") }
    
    var principal2 by remember { mutableStateOf("") }
    var rate2 by remember { mutableStateOf("") }
    var tenure2 by remember { mutableStateOf("") }
    
    val comparison1 by viewModel.comparison1.collectAsState()
    val comparison2 by viewModel.comparison2.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isPro && comparison1 != null && comparison2 != null) {
                        IconButton(onClick = { viewModel.exportComparisonToPdf() }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!isPro) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upgrade to Pro for unlimited comparisons",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onNavigateToUpgrade) {
                                Text("Upgrade Now")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Option 1
            Text(
                text = "Loan Option 1",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LoanInputCard(
                principal = principal1,
                onPrincipalChange = { principal1 = it },
                rate = rate1,
                onRateChange = { rate1 = it },
                tenure = tenure1,
                onTenureChange = { tenure1 = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Option 2
            Text(
                text = "Loan Option 2",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LoanInputCard(
                principal = principal2,
                onPrincipalChange = { principal2 = it },
                rate = rate2,
                onRateChange = { rate2 = it },
                tenure = tenure2,
                onTenureChange = { tenure2 = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val p1 = principal1.toDoubleOrNull() ?: 0.0
                    val r1 = rate1.toDoubleOrNull() ?: 0.0
                    val t1 = tenure1.toIntOrNull() ?: 0
                    val p2 = principal2.toDoubleOrNull() ?: 0.0
                    val r2 = rate2.toDoubleOrNull() ?: 0.0
                    val t2 = tenure2.toIntOrNull() ?: 0
                    
                    if (p1 > 0 && r1 >= 0 && t1 > 0 && p2 > 0 && r2 >= 0 && t2 > 0) {
                        viewModel.calculateComparison(p1, r1, t1, p2, r2, t2)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compare Loans")
            }
            
            // Comparison Results
            if (comparison1 != null && comparison2 != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                ComparisonResultCard(comparison1!!, comparison2!!)
            }
        }
    }
}

@Composable
fun LoanInputCard(
    principal: String,
    onPrincipalChange: (String) -> Unit,
    rate: String,
    onRateChange: (String) -> Unit,
    tenure: String,
    onTenureChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = principal,
                onValueChange = onPrincipalChange,
                label = { Text("Loan Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = rate,
                onValueChange = onRateChange,
                label = { Text("Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = tenure,
                onValueChange = onTenureChange,
                label = { Text("Tenure (months)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ComparisonResultCard(result1: EmiResult, result2: EmiResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Comparison Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Option 1",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Option 2",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ComparisonRow("Monthly EMI", result1.emi, result2.emi)
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("Total Interest", result1.totalInterest, result2.totalInterest)
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("Total Payment", result1.totalPayment, result2.totalPayment)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recommendation
            val betterOption = if (result1.totalInterest < result2.totalInterest) 1 else 2
            val savings = kotlin.math.abs(result1.totalInterest - result2.totalInterest)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Recommendation",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Option $betterOption has lower total interest payment.",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "You save ${FormatUtils.formatCurrency(savings)} in interest.",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, value1: Double, value2: Double) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = FormatUtils.formatCurrency(value1),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value1 < value2) FontWeight.Bold else FontWeight.Normal,
            color = if (value1 < value2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = FormatUtils.formatCurrency(value2),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value2 < value1) FontWeight.Bold else FontWeight.Normal,
            color = if (value2 < value1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
