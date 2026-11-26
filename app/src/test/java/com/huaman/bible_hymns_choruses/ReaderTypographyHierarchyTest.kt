package com.huaman.bible_hymns_choruses

import com.huaman.bible_hymns_choruses.ui.theme.readerBodyTextStyle
import com.huaman.bible_hymns_choruses.ui.theme.readerHeadingTextStyle
import com.huaman.bible_hymns_choruses.ui.theme.readerLabelTextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying that Bible section subtitles (`heading`), psalm superscriptions (`label`),
 * and verse body text (`body`) dynamically scale across the entire `14..28 sp` personalization
 * slider range while maintaining strict stroke-weight and size hierarchy.
 */
class ReaderTypographyHierarchyTest {

    private val sliderFontSizes = listOf(14f, 16f, 18f, 20f, 22f, 24f, 26f, 28f)
    private val bodyFontWeights = listOf(400, 500, 600, 700)

    @Test
    fun sectionHeadings_scaleWithFontSize_andAreAlwaysLargerThanVerseBody() {
        var previousHeadingSize = 0f

        for (sizeSp in sliderFontSizes) {
            val bodyStyle = readerBodyTextStyle(fontSizeSp = sizeSp)
            val headingStyle = readerHeadingTextStyle(fontSizeSp = sizeSp)
            val labelStyle = readerLabelTextStyle(fontSizeSp = sizeSp)

            val bodySize = bodyStyle.fontSize.value
            val headingSize = headingStyle.fontSize.value
            val labelSize = labelStyle.fontSize.value

            // 1. Heading size must strictly increase as slider increases
            assertTrue(
                "Expected heading size ($headingSize) at ${sizeSp}sp to be greater than previous ($previousHeadingSize)",
                headingSize > previousHeadingSize
            )
            previousHeadingSize = headingSize

            // 2. Heading size must always be at least 25% larger than body size
            assertTrue(
                "Expected heading size ($headingSize) to be >= 1.25x body size ($bodySize) at ${sizeSp}sp",
                headingSize >= bodySize * 1.25f - 0.01f
            )

            // 3. Label size must scale proportionally between 13.5sp and headingSize
            assertTrue(
                "Expected label size ($labelSize) to be smaller than heading ($headingSize) at ${sizeSp}sp",
                labelSize < headingSize
            )
        }
    }

    @Test
    fun sectionHeadings_haveSignificantlyThickerFontWeightThanVerseBody_atAllWeights() {
        for (sizeSp in sliderFontSizes) {
            for (bodyWeight in bodyFontWeights) {
                val bodyStyle = readerBodyTextStyle(
                    fontSizeSp = sizeSp,
                    fontWeightValue = bodyWeight
                )
                val headingStyle = readerHeadingTextStyle(
                    fontSizeSp = sizeSp,
                    bodyFontWeightValue = bodyWeight
                )
                val labelStyle = readerLabelTextStyle(fontSizeSp = sizeSp)

                val actualBodyWeight = bodyStyle.fontWeight?.weight ?: 0
                val actualHeadingWeight = headingStyle.fontWeight?.weight ?: 0
                val actualLabelWeight = labelStyle.fontWeight?.weight ?: 0

                assertNotNull(headingStyle.fontWeight)

                // Heading must always be ExtraBold (800) or Black (900)
                assertTrue(
                    "Expected heading weight ($actualHeadingWeight) >= 800 (ExtraBold), found $actualHeadingWeight",
                    actualHeadingWeight >= 800
                )

                // Heading must always be strictly heavier than body text (even if body is Bold 700)
                assertTrue(
                    "Expected heading weight ($actualHeadingWeight) > body weight ($actualBodyWeight)",
                    actualHeadingWeight > actualBodyWeight
                )

                // At default body weight (400 Normal), heading weight is 800 (ExtraBold = +400 delta)
                if (bodyWeight == 400) {
                    assertEquals(800, actualHeadingWeight)
                    assertEquals(600, actualLabelWeight)
                }
            }
        }
    }
}
