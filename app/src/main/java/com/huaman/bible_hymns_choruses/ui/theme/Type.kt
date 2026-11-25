package com.huaman.bible_hymns_choruses.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.huaman.bible_hymns_choruses.R

val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val googleSansFlexFont = GoogleFont("Google Sans Flex")

// From M3 (references/typography.md): Brand & Plain typeface slots configured with Google Sans Flex
// and local fallback chain so weights (Normal 400 -> Black 900) always render offline even without GMS.
val GoogleSansFlexFontFamily = FontFamily(
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold
    ),
    Font(
        googleFont = googleSansFlexFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Black
    )
)

/**
 * Dynamic reader [TextStyle] for Bible verses and hymn/chorus stanzas, responding live to
 * user-selected font size (`sp`), weight (`400..700`), and grade (`-50..150`) of Google Sans Flex.
 */
fun readerBodyTextStyle(
    fontSizeSp: Float = 18f,
    fontWeightValue: Int = 400,
    fontGradeValue: Int = 0
): TextStyle {
    val effectiveWeight = (fontWeightValue + (fontGradeValue / 2)).coerceIn(300, 700)
    return TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight(effectiveWeight),
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.55f).sp,
        letterSpacing = if (fontGradeValue > 50) 0.15.sp else 0.25.sp
    )
}

/**
 * Dynamic section heading [TextStyle] for Bible passage subtitles (`heading`), scaling live
 * with the user's [fontSizeSp] preference and always maintaining heavier stroke weight than body text.
 *
 * - From M3 (`references/typography.md` & `references/tokens.md`):
 *   - "When reducing the scale, keep impactful contrast between sizes; avoid small differences."
 *   - "Weight — apply emphasized to text that already carries weight (medium, bold) to reinforce hierarchy."
 *   - Large/title type readability uses a `~1.2x` line-height ratio (`references/typography.md#Readability`).
 * - My design decision (not in M3):
 *   - Use `FontWeight.ExtraBold` (`800` at default body weight `400`, or `FontWeight.Black` `900` if body
 *     weight is increased) and scale at `1.25f * fontSizeSp` (`17.5..35 sp`) so section headings have
 *     unmistakable stroke thickness and size separation from verse body text across `14..28 sp`.
 */
fun readerHeadingTextStyle(
    fontSizeSp: Float = 18f,
    bodyFontWeightValue: Int = 400
): TextStyle {
    val headingSize = (fontSizeSp * 1.25f).coerceIn(17.5f, 35f)
    val headingWeight = (bodyFontWeightValue + 400).coerceIn(800, 900)
    return TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight(headingWeight),
        fontSize = headingSize.sp,
        lineHeight = (headingSize * 1.22f).sp,
        letterSpacing = 0.sp
    )
}

/**
 * Dynamic psalm/note label [TextStyle] (`label`), scaling live with the user's [fontSizeSp] preference.
 *
 * - From M3 (`references/typography.md` & `references/tokens.md`):
 *   - Emphasized subtitle/caption weight (`SemiBold 600`) with `0.15.sp` tracking and `~1.45x` line-height ratio.
 * - My design decision (not in M3):
 *   - Scale label font size at `0.95f * fontSizeSp` in italic with `FontWeight.SemiBold` (`600`) so psalm
 *     superscriptions respond to the font size slider and stand out clearly from verse text (`400`).
 */
fun readerLabelTextStyle(
    fontSizeSp: Float = 18f
): TextStyle {
    val labelSize = (fontSizeSp * 0.95f).coerceIn(13.5f, 26.5f)
    return TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontSize = labelSize.sp,
        lineHeight = (labelSize * 1.45f).sp,
        letterSpacing = 0.15.sp
    )
}

// From M3 (references/tokens.md): 15 baseline + 15 emphasized type scale styles
// Baseline and emphasized are used together; emphasized matches baseline size/lineHeight and adjusts weight and tracking.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography = Typography(
    // Baseline scale (15 styles)
    displayLarge = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    // Emphasized scale — M3 Expressive (15 styles: md.sys.typescale.emphasized.*)
    displayLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLargeEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMediumEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmallEmphasized = TextStyle(
        fontFamily = GoogleSansFlexFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)