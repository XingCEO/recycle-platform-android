package com.recycle.user.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---- Brand palette : premium eco -------------------------------------------
// Emerald primary, teal secondary, lime/amber tertiary, warm refined neutrals.

val Emerald900 = Color(0xFF064E3B)
val Emerald700 = Color(0xFF047857)
val Emerald600 = Color(0xFF059669)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald50 = Color(0xFFECFDF5)
val GoogleGreen = Color(0xFF0F9D58)

val Teal700 = Color(0xFF0F766E)
val Teal600 = Color(0xFF0D9488)
val Teal500 = Color(0xFF14B8A6)
val Teal100 = Color(0xFFCCFBF1)

val Lime500 = Color(0xFF84CC16)
val Lime400 = Color(0xFFA3E635)
val Amber500 = Color(0xFFF59E0B)
val Amber400 = Color(0xFFFBBF24)
val Amber100 = Color(0xFFFEF3C7)
val Amber700 = Color(0xFFB45309)

// Refined warm neutrals
val Ink900 = Color(0xFF0B1F17)   // near-black with green undertone
val Ink700 = Color(0xFF1F2D27)
val Slate600 = Color(0xFF52635B)
val Slate500 = Color(0xFF6B7C74)
val Surface0 = Color(0xFFFBFDF9)  // warm off-white
val Surface1 = Color(0xFFF4F8F2)
val Surface2 = Color(0xFFEAF1E9)
val OutlineSoft = Color(0xFFD4E0D2)

// Status accents
val StatusGood = Emerald600
val StatusWarn = Color(0xFFE11D48)   // rose for "不可回收"
val StatusPending = Amber500
val StatusDone = Slate500

val DangerRed = Color(0xFFDC2626)

// ---- Gradients -------------------------------------------------------------

val HeroGradient = listOf(Emerald600, Teal600)                 // hero header / points
val HeroGradientDeep = listOf(Emerald700, Teal700, Emerald900) // login backdrop
val ButtonGradient = listOf(Emerald500, Teal500)               // primary CTA
val PointsGradient = listOf(Emerald500, GoogleGreen, Teal600)  // points card
val BadgeGradient = listOf(Emerald400, Teal500)                // logo / icon chips
val AmberGradient = listOf(Amber400, Amber500)                 // points pills

fun verticalBrush(colors: List<Color>) = Brush.verticalGradient(colors)
fun horizontalBrush(colors: List<Color>) = Brush.horizontalGradient(colors)
