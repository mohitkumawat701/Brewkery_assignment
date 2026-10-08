<div align="center">

# ☕ Brewkery

### Coffee & Bakery Ordering App for Android

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2-48B983)
![Tests](https://img.shields.io/badge/Unit%20Tests-11%20passing-success)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-D9532F)

Built by **Mohit Kumawat** for the **Clickretina Android Developer** take-home assessment.

[Live Prototype](https://vivekshah138.github.io/Brewkery/) · [Build & Run](#-build--run) · [How I Used AI](#-how-i-used-ai)

</div>

---

## 📑 Table of Contents

- [App Walkthrough](#-app-walkthrough)
- [How I Used AI](#-how-i-used-ai)
- [Tech Stack](#-tech-stack)
- [Engineering Decisions](#-engineering-decisions)
- [Unit Tests](#-unit-tests)
- [Build & Run](#-build--run)
- [Submission Details](#-submission-details)

---

## 📱 App Walkthrough

| # | Screen | What it does |
|---|--------|--------------|
| 1 | **Home / Menu** | `BK` header with live cart count, delivery banner ($2.50, 20–30 mins) that becomes an **Active Order Tracking Card** after ordering, real-time search, category pills (`All Items`, `☕ Hot Coffee`, `🧊 Cold Brews`, `🥐 Artisan Bakery`), catalog cards with thumbnail, `BESTSELLER` / `SEASONAL` badge, rating, base price and `+ Customize`, plus a sticky `View Your Cart • $X.XX` bar. |
| 2 | **Item Detail** | 160dp hero image with favorite heart, description and Key Ingredients, **Size** grid (`Tall` / `Grande` / `Venti`), **Milk** rows with price deltas, **Sugar** chips, and a `[ − qty + ]` stepper with live `Add to Cart • $X.XX`. |
| 3 | **Cart & Checkout** | Itemized list with options (`Size • Milk • Sugar`), steppers, `Clear Cart`, bill card (Subtotal, $2.50 delivery, 8% tax, Total) and a gradient `🔒 Place Order Now` button. |
| 4 | **Order Status** | Mug badge, generated ticket ID (e.g. `#BK-84920`), amber `PREPARING` badge, estimated wait, item count, and `Back to Menu`. |

---

## 🤖 How I Used AI

### Tools

| Tool | Used for |
|------|----------|
| **Google Antigravity & Claude** | Architecture planning, drafting Compose screens, debugging window insets |
| **Cursor / GitHub Copilot** | Autocomplete, DTOs, scaffolding unit tests |

### Prompts I Sent

I gave AI explicit constraints, design tokens and architectural boundaries instead of generic requests.

<details>
<summary><b>1. Architecture, state scoping & money representation</b></summary>

> **Why:** Sets up the foundation upfront, avoids floating-point currency errors at the network boundary, and keeps state alive across navigation without heavy frameworks.
>
> *"Let's architect a native Android coffee ordering app in Kotlin following Clean MVVM. Requirements: (1) Single-Activity with Navigation Compose; (2) Manual DI via an AppContainer singleton, no Hilt/Dagger; (3) Retrofit 2 + Gson fetching from raw JSON endpoints; (4) Scope CartViewModel to the Activity so the in-memory cart and active order survive navigation; (5) Convert incoming Double prices into Long cents at DtoMappers.kt so no floating-point arithmetic touches the domain or UI. Scaffold the core layers, Repositories, and ViewModels."*

</details>

<details>
<summary><b>2. Reverse-engineering the prototype UI in Compose</b></summary>

> **Why:** Gives exact visual specs from the live prototype so the generated Compose code matches closely.
>
> *"I'm inspecting the Brewkery web prototype. Match it 1:1 in Jetpack Compose Material 3: (1) Color tokens: Terracotta #D9532F, Espresso #140B07, Crema #FDFAF7, Amber #F59E0B, Border #EBD8CB, MutedBrown #786457; (2) Catalog cards with 56dp rounded thumbnails, amber-100 badges, star ratings and a terracotta '+ Customize' button; (3) Item Detail with a 3-column size grid, full-width milk rows and dark Espresso sugar chips; (4) Gradient-terracotta bottom CTA beside a bordered quantity stepper. Write modular composables."*

</details>

<details>
<summary><b>3. API edge cases & accounting-grade financial math</b></summary>

> **Why:** Surfaces a hidden data quirk and enforces rounding rules verified against the reference bill.
>
> *"In data.json, 'sizes' and 'milk_options' have numeric extra_price, but 'sugar_levels' are plain strings like 'Light Wildflower Honey (+0.40)'. Write a pure Kotlin PriceCalculator that: (1) uses regex to extract the sugar surcharge, defaulting to 0; (2) keeps everything in Long cents; (3) computes 8% tax with BigDecimal HALF_UP to match the reference bill ($9.40 + $2.50 + $0.75 = $12.65); (4) includes JUnit 4 tests for every calculation and edge case."*

</details>

<details>
<summary><b>4. Edge-to-edge window insets on a real device</b></summary>

> **Why:** Diagnoses a defect found on a physical phone and points the AI at the root cause, not a hack.
>
> *"With enableEdgeToEdge(), the bottom 'Add to Cart' bar leaves 30dp transparent because the outer Surface has navigationBarsPadding(), so the LazyColumn shows through underneath. How should Scaffold and insets be structured so the Surface paints solid to the screen edge while buttons stay above the gesture bar, and the last list item stays scrollable?"*

</details>

### ✅ What AI Got Right

- **Clean MVVM** with unidirectional `StateFlow`; Activity-scoped ViewModels kept cart and order state intact across screens.
- **Money in cents:** mapping everything to `Long` cents in `DtoMappers.kt` prevents bugs like `0.1 + 0.2 = 0.30000000000000004`.
- **Unit tests:** generated 11 `PriceCalculator` tests quickly, which sped up validation.

### ❌ What AI Got Wrong, and How I Fixed It

**1. Schema assumption on sugar levels**

AI wrote DTOs assuming `sugar_levels` were objects with `extra_price`. Checking the raw `data.json` showed they are plain strings like `"Light Wildflower Honey (+0.40)"`. I fixed it with a regex extractor:

```kotlin
private val SUGAR_EXTRA_REGEX = Regex("""\(\+(\d+\.\d+)\)""")
fun parseSugarExtraCents(sugarLevel: String): Long {
    val match = SUGAR_EXTRA_REGEX.find(sugarLevel) ?: return 0L
    return dollarsToCents(match.groupValues[1].toDoubleOrNull() ?: 0.0)
}
```

**2. Bottom bar leak on real devices**

AI put `navigationBarsPadding()` on the outer `Surface`, so on gesture-nav phones the white surface stopped above the gesture bar and the list peeked through. I kept the `Surface` full-bleed and moved the padding to the inner `Row`:

```kotlin
Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // keeps buttons above the gesture pill
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) { ... }
}
```

I also increased the `LazyColumn` bottom padding so the last card scrolls fully into view.

---

## 🛠 Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3, Navigation Compose (single Activity) |
| Networking | Retrofit 2 + Gson + OkHttp |
| Async & state | Coroutines + `StateFlow` |
| Images | Coil (`AsyncImage`) |
| Architecture | MVVM + Repository |
| DI | Manual `AppContainer` singleton (no Hilt/KAPT overhead) |
| Storage | None, in-memory cart resets on app close (per assignment) |

---

## 💡 Engineering Decisions

1. **All money in `Long` cents.** `Double` is converted once at `DtoMappers.kt`; formatting to `$X.XX` happens only at display time.
2. **Tax with `BigDecimal` `HALF_UP`.** Reference bill: `$9.40` subtotal → `$0.75` tax → `$12.65` total.
   ```kotlin
   BigDecimal(subtotalCents).multiply(BigDecimal(0.08)).setScale(0, RoundingMode.HALF_UP).toLong()
   ```
3. **Cart line merging.** Key `CartLineKey(itemId, sizeId, milkOptionId, sugarLevel)`; identical items increase quantity instead of duplicating.

---

## 🧪 Unit Tests

```bash
./gradlew test
```

All 11 tests in `PriceAndCartTests.kt` pass:

- [x] Price conversion (`dollarsToCents`, `formatPrice`)
- [x] Regex extraction of sugar surcharges
- [x] Base + size + milk + sugar addition
- [x] 8% tax with `HALF_UP` rounding
- [x] Full bill: **$9.40 + $2.50 + $0.75 = $12.65**
- [x] Cart key hashing and quantity merging

---

## 🚀 Build & Run

```bash
git clone <your-github-repo-url>
cd <repo-folder>
```

| Step | Command |
|------|---------|
| Run tests | `./gradlew test` |
| Build debug APK | `./gradlew assembleDebug` |
| Install on device / emulator | `./gradlew installDebug` |

APK output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 👤 Submission Details

| | |
|---|---|
| **Candidate** | Mohit Kumawat |
| **GitHub** | `<your-github-repo-url>` |
| **Submitted to** | Hr@clickretina.co.in |