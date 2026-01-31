package com.zaheer.emianalyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaheer.emianalyzer.domain.EmiResult
import com.zaheer.emianalyzer.ui.viewmodel.EmiViewModel
import com.zaheer.emianalyzer.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: EmiViewModel,
    onNavigateToAmortization: () -> Unit,
    onNavigateToComparison: () -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    val calculationResult by viewModel.calculationResult.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val pdfExportState by viewModel.pdfExportState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan EMI Analyzer") },
                actions = {
                    if (!isPro) {
                        TextButton(onClick = onNavigateToUpgrade) {
                            Icon(Icons.Default.Star, contentDescription = "Upgrade")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upgrade to Pro")
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
            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Loan Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = principal,
                        onValueChange = { principal = it },
                        label = { Text("Loan Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.AccountBalance, null) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        label = { Text("Interest Rate (% per annum)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Percent, null) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = tenureMonths,
                        onValueChange = { tenureMonths = it },
                        label = { Text("Tenure (months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = interestRate.toDoubleOrNull() ?: 0.0
                            val t = tenureMonths.toIntOrNull() ?: 0
                            if (p > 0 && r >= 0 && t > 0) {
                                viewModel.calculateEmi(p, r, t)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate EMI")
                    }
                }
            }
            
            // Results Section
            calculationResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                
                ResultCard(result)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToAmortization,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Schedule")
                    }
                    
                    Button(
                        onClick = onNavigateToComparison,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Compare")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.exportToPdf() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPro
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPro) "Export to PDF" else "Export to PDF (Pro)")
                }
            }
            
            // PDF Export Status
            when (val state = pdfExportState) {
                is EmiViewModel.PdfExportState.Success -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "PDF saved to: ${state.file.absolutePath}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.resetPdfExportState()
                    }
                }
                is EmiViewModel.PdfExportState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ResultCard(result: EmiResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EMI Calculation Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ResultRow("Monthly EMI", FormatUtils.formatCurrency(result.emi))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ResultRow("Total Interest", FormatUtils.formatCurrency(result.totalInterest))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ResultRow("Total Payment", FormatUtils.formatCurrency(result.totalPayment))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
