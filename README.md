# Loan EMI Analyzer – India

A complete Android application for calculating loan EMI (Equated Monthly Installments) with advanced features.

## Features

### Core Features
- **EMI Calculator** - Calculate monthly EMI using the formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)
  - Input: Loan amount, Interest rate (% annual), Tenure (months)
  - Output: Monthly EMI, Total interest, Total payment
- **Amortization Schedule** - View detailed month-by-month payment breakdown
- **Loan Comparison** - Compare two loan options side-by-side
- **PDF Export** - Export calculations and comparisons as PDF documents (Pro feature)

### Pro Features (In-App Purchase)
- PDF Export functionality
- Unlimited loan comparisons
- Restore purchases across devices

## Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **State Management:** StateFlow
- **Navigation:** Navigation Compose
- **Billing:** Google Play Billing Library (billing-ktx v7.x)
- **PDF Generation:** iText 7
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## Project Structure

```
app/src/main/java/com/zaheer/emianalyzer/
├── MainActivity.kt                    # Main activity with navigation
├── billing/
│   └── BillingManager.kt             # Google Play Billing integration
├── domain/
│   └── EmiCalculator.kt              # Business logic for EMI calculations
├── ui/
│   ├── screens/
│   │   ├── CalculatorScreen.kt       # Main EMI calculator screen
│   │   ├── AmortizationScreen.kt     # Amortization schedule display
│   │   ├── ComparisonScreen.kt       # Loan comparison screen
│   │   └── UpgradeScreen.kt          # Pro upgrade screen
│   ├── theme/
│   │   ├── Color.kt                  # App colors
│   │   ├── Theme.kt                  # Material 3 theme
│   │   └── Type.kt                   # Typography
│   └── viewmodel/
│       └── EmiViewModel.kt           # ViewModel with StateFlow
└── utils/
    ├── FormatUtils.kt                # Formatting utilities
    ├── PdfExporter.kt                # PDF export functionality
    └── PreferencesManager.kt         # SharedPreferences manager
```

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

```bash
./gradlew assembleDebug
```

## In-App Purchase Setup

To enable in-app purchases:
1. Create a product in Google Play Console with ID: `emi_pro_unlock`
2. Set it as a one-time managed product (INAPP)
3. Configure pricing and availability

## EMI Calculation Formula

The app uses the standard EMI calculation formula:

```
EMI = P × r × (1+r)^n / ((1+r)^n - 1)

Where:
P = Principal loan amount
r = Monthly interest rate (annual rate / 12 / 100)
n = Loan tenure in months
```

## License

This project is created for demonstration purposes.

## Package Information

- **Package Name:** com.zaheer.emianalyzer
- **App Name:** Loan EMI Analyzer – India
