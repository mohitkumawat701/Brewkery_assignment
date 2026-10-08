<div align="center">

# ☕ Brewkery

### Coffee & Bakery Ordering App for Android

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2-48B983)
![Tests](https://img.shields.io/badge/Unit%20Tests-Passing-success)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-D9532F)

Built by **Mohit Kumawat** for the **Clickretina Android Developer** assessment.

</div>

---

## 📑 Table of Contents

- [App Walkthrough](#-app-walkthrough)
- [How I Worked with AI](#-how-i-worked-with-ai)
- [Tech Stack](#-tech-stack)
- [Engineering Decisions](#-engineering-decisions)
- [Unit Tests](#-unit-tests)
- [Build & Run](#-build--run)

---

## 📱 App Walkthrough

| # | Screen | Description |
|---|--------|-------------|
| 1 | **Home / Menu** | Header with live cart count, delivery banner ($2.50, 25 mins) or active order card, search bar, category chips (`All Items`, `☕ Hot Coffee`, `🧊 Cold Brews`, `🥐 Artisan Bakery`), catalog product cards with thumbnail, `BESTSELLER` / `SEASONAL` badges, rating, base price, `+ Customize` button, and normal scrolling cart summary card. |
| 2 | **Item Detail** | Hero image with favorite toggle, key ingredients, **Size** selection grid, **Milk** options, **Sugar** levels, quantity stepper, and live `Add to Cart • $X.XX` button that navigates directly to checkout without Toast popups. |
| 3 | **Cart & Checkout** | Itemized list with customization details, quantity steppers, `Clear Cart` option, compact bottom **Total Summary** card (Subtotal, Delivery Fee, 8% Tax, Total Payable), and gradient `🔒 Place Order Now` button. |
| 4 | **Order Status** | Order confirmation screen with generated ticket ID (e.g. `#BK-84920`), `PREPARING` badge, estimated wait time, item count, and `Back to Menu` action. |

---

## 🤖 How I Worked with AI

### 1. AI Tools Used
- **Google Antigravity / Claude 3.7 Sonnet**: Primary agentic AI coding assistant used for end-to-end architecture design, refactoring, window inset debugging, and Jetpack Compose layout tuning.
- **Android Studio IDE**: Code editing, Gradle builds, emulator verification, and linting.

---

### 2. Actual Prompts Sent

#### Prompt 1: Architecture & Money Scoping
> *"Architect a native Android coffee ordering app in Kotlin following Clean MVVM. Requirements: (1) Single-Activity with Navigation Compose; (2) Manual DI via an AppContainer singleton; (3) Retrofit 2 + Gson fetching from raw JSON endpoints; (4) Scope CartViewModel to the Activity so the in-memory cart and active order survive navigation; (5) Convert incoming Double prices into Long cents at DtoMappers.kt so no floating-point arithmetic touches the domain or UI."*

#### Prompt 2: Cart Card Height & Window Inset Fix
> *"Fix the cart card height/spacing on MenuScreen: wrap the card content naturally without fixed height or excessive vertical padding, move the system navigation bar padding outside the dark brown card container into the LazyColumn parent item, and keep it in the normal scrolling flow."*

#### Prompt 3: Direct Checkout Navigation & Toast Removal
> *"When the user taps '+ Customize' / Add to Cart and the item is added to cart, immediately navigate to the existing Order Checkout page. Remove the Toast message / Snackbar confirmation popup completely."*

---

### 3. What AI Got Right & What It Got Wrong

#### ✅ What AI Got Right
- **Clean Architecture & State Scoping**: Implemented single-Activity Navigation Compose with unidirectional `StateFlow` data streams. Activity-scoped ViewModels correctly preserve cart items and active orders across navigation.
- **Cents-Based Money Math**: Converting raw double values into `Long` cents at the network mapper level eliminated floating-point rounding errors across all subtotal, tax, and total calculations.

#### ❌ What AI Got Wrong & How I Fixed It
- **What AI Got Wrong**: When applying system navigation bar padding (`.navigationBarsPadding()`) to the bottom `CartCard` on the Menu screen, AI placed the padding modifier directly inside the inner `Row` of the card container. This injected bottom gesture inset height *inside* the dark brown card, creating a large unwanted empty gap below the dollar amount.
- **How I Fixed It**: Re-anchored `.navigationBarsPadding()` to an outer `Box` container within the parent `LazyColumn` in `MenuScreen.kt` while keeping `CartCard` using content-based height with clean internal padding. This ensured system gesture safety on Android without inflating the card's internal height.

---

## 🛠 Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 2.0.21 |
| UI Framework | Jetpack Compose (Material 3), Navigation Compose |
| Architecture | Clean MVVM + Repository Pattern |
| DI | Manual `AppContainer` Singleton |
| Network | Retrofit 2 + OkHttp 4 + Gson |
| State Management | Coroutines + `StateFlow` |
| Image Loading | Coil (`SubcomposeAsyncImage`) |

---

## 💡 Engineering Decisions

1. **All Money in `Long` Cents**: Double values from JSON are converted into `Long` cents in `DtoMappers.kt`. Dollar formatting (`$X.XX`) occurs only at UI render time via `PriceCalculator`.
2. **Compact Package Structure**: Project is organized into clean packages: `screens`, `network`, `model`, `storage`, `components`, and `theme`.
3. **Tax Calculation**: Tax is calculated using `BigDecimal` with `HALF_UP` rounding to match exact dollar totals.

---

## 🧪 Unit Tests

Run tests via Gradle:

```bash
./gradlew test
```

Unit tests in `PriceAndCartTests.kt` cover:
- Price conversion (`dollarsToCents`, `formatPriceAsDollars`)
- Regex extraction of sugar surcharges
- Multi-option unit price calculations
- 8% tax calculation with `HALF_UP` rounding
- Cart line merging by configuration key
- Ticket ID pattern generation (`#BK-XXXXX`)

---

## 🚀 Build & Run

Clone the repository:

```bash
git clone https://github.com/mohitkumawat701/Brewkery_assignment.git
cd Brewkery_assignment
```

Open the project in Android Studio and let Gradle sync.

Build/run the app using the normal Android Studio Run configuration or via Gradle:

```bash
# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```

Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`