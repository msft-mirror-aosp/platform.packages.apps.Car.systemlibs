/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.car.scalableui.model

import android.graphics.Insets
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualDataTest {

    companion object {
        private const val TEST_ALPHA = 0.5f
        private const val TEST_IS_VISIBLE = false
        private const val TEST_LAYER = 5
        private const val TEST_CORNER_RADIUS = 10
        private val TEST_BOUNDS = Rect(10, 20, 100, 200)
        private val TEST_SAFE_BOUNDS = Rect(15, 25, 95, 195)
        private val TEST_INSETS = Insets.of(1, 2, 3, 4)
    }

    @Test
    fun testBuilder_defaultValues() {
        val visualData = VisualData.Builder().build()

        assertEquals(Alpha.DEFAULT_ALPHA, visualData.alpha, 0.0f)
        assertEquals(Visibility.DEFAULT_VISIBILITY, visualData.isVisible)
        assertEquals(Layer.DEFAULT_LAYER, visualData.layer)
        assertEquals(Corner.DEFAULT_RADIUS, visualData.cornerRadius)
        assertEquals(Rect(), visualData.bounds)
        assertEquals(Rect(), visualData.safeBounds) // Defaults to empty if bounds is empty
        assertEquals(Insets.NONE, visualData.insets)
    }

    @Test
    fun testBuilder_setAllValues() {
        val visualData = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)
            .build()

        assertEquals(TEST_ALPHA, visualData.alpha, 0.0f)
        assertEquals(TEST_IS_VISIBLE, visualData.isVisible)
        assertEquals(TEST_LAYER, visualData.layer)
        assertEquals(TEST_CORNER_RADIUS, visualData.cornerRadius)
        assertEquals(TEST_BOUNDS, visualData.bounds)
        assertEquals(TEST_SAFE_BOUNDS, visualData.safeBounds)
        assertEquals(TEST_INSETS, visualData.insets)

        // Verify defensive copies for Rect and Insets
        assertNotEquals(
            System.identityHashCode(TEST_BOUNDS),
            System.identityHashCode(visualData.bounds)
        )
        assertNotEquals(
            System.identityHashCode(TEST_SAFE_BOUNDS),
            System.identityHashCode(visualData.safeBounds)
        )
        // Insets.of creates a new instance, so identity will be different for insets too
    }

    @Test
    fun testBuilder_setAlpha_clampsValues() {
        val visualDataLow = VisualData.Builder().setAlpha(-1.0f).build()
        assertEquals(0.0f, visualDataLow.alpha, 0.0f)

        val visualDataHigh = VisualData.Builder().setAlpha(2.0f).build()
        assertEquals(1.0f, visualDataHigh.alpha, 0.0f)
    }

    @Test
    fun testBuilder_setCornerRadius_clampsValue() {
        val visualData = VisualData.Builder().setCornerRadius(-10).build()
        assertEquals(0, visualData.cornerRadius)
    }

    @Test
    fun testBuilder_safeBoundsDefaultsToBounds_whenNotSet() {
        val visualData = VisualData.Builder()
            .setBounds(TEST_BOUNDS)
            .build()
        assertEquals(TEST_BOUNDS, visualData.safeBounds)
    }

    @Test
    fun testBuilder_safeBoundsDoesNotDefaultsToBounds_whenSetExplicitlyEmpty() {
        val emptyRect = Rect()
        val visualData = VisualData.Builder()
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(emptyRect) // Explicitly set to empty
            .build()
        assertEquals(TEST_BOUNDS, visualData.safeBounds)
    }

    @Test
    fun testBuilder_copyConstructor() {
        val originalVisualData = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)
            .build()

        val copiedVisualData = VisualData.Builder(originalVisualData).build()

        assertEquals(originalVisualData.alpha, copiedVisualData.alpha, 0.0f)
        assertEquals(originalVisualData.isVisible, copiedVisualData.isVisible)
        assertEquals(originalVisualData.layer, copiedVisualData.layer)
        assertEquals(originalVisualData.cornerRadius, copiedVisualData.cornerRadius)
        assertEquals(originalVisualData.bounds, copiedVisualData.bounds)
        assertEquals(originalVisualData.safeBounds, copiedVisualData.safeBounds)
        assertEquals(originalVisualData.insets, copiedVisualData.insets)

        // Ensure it's a deep copy for mutable fields (Rects)
        assertNotEquals(
            System.identityHashCode(originalVisualData.bounds),
            System.identityHashCode(copiedVisualData.bounds)
        )
        assertNotEquals(
            System.identityHashCode(originalVisualData.safeBounds),
            System.identityHashCode(copiedVisualData.safeBounds)
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val visualData1 = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)
            .build()

        val visualData2 = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(Rect(TEST_BOUNDS)) // New Rect instance with same values
            .setSafeBounds(Rect(TEST_SAFE_BOUNDS)) // New Rect instance
            .setInsets(
                Insets.of(
                    TEST_INSETS.left,
                    TEST_INSETS.top,
                    TEST_INSETS.right,
                    TEST_INSETS.bottom
                )
            ) // New Insets instance
            .build()

        val visualData3 = VisualData.Builder()
            .setAlpha(0.8f) // Different alpha
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)
            .build()

        // Reflexive
        assertEquals(visualData1, visualData1)

        // Symmetric
        assertEquals(visualData1, visualData2)
        assertEquals(visualData2, visualData1)

        // Consistent
        assertEquals(visualData1, visualData2)

        // Not equal to null
        assertFalse(visualData1.equals(null))

        // Not equal to different type
        assertFalse(visualData1.equals("a string"))

        // Different objects
        assertNotEquals(visualData1, visualData3)
        assertNotEquals(visualData2, visualData3)

        // HashCode
        assertEquals(visualData1.hashCode(), visualData2.hashCode())
        assertNotEquals(visualData1.hashCode(), visualData3.hashCode())
    }

    @Test
    fun testEquals_differentFields() {
        val baseBuilder = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)

        val base = baseBuilder.build()

        // Create a new builder for each modification to avoid state leakage from previous sets
        val diffAlpha = VisualData.Builder(base).setAlpha(0.1f).build()
        val diffVisible = VisualData.Builder(base).setIsVisible(!TEST_IS_VISIBLE).build()
        val diffLayer = VisualData.Builder(base).setLayer(TEST_LAYER + 1).build()
        val diffCorner = VisualData.Builder(base).setCornerRadius(TEST_CORNER_RADIUS + 1).build()
        val diffBounds = VisualData.Builder(base).setBounds(Rect(0, 0, 1, 1)).build()
        val diffSafeBounds = VisualData.Builder(base).setSafeBounds(Rect(0, 0, 1, 1)).build()
        val diffInsets = VisualData.Builder(base).setInsets(Insets.of(5, 5, 5, 5)).build()

        assertNotEquals(base, diffAlpha)
        assertNotEquals(base, diffVisible)
        assertNotEquals(base, diffLayer)
        assertNotEquals(base, diffCorner)
        assertNotEquals(base, diffBounds)
        assertNotEquals(base, diffSafeBounds)
        assertNotEquals(base, diffInsets)
    }

    @Test
    fun testToString() {
        val visualData = VisualData.Builder()
            .setAlpha(TEST_ALPHA)
            .setIsVisible(TEST_IS_VISIBLE)
            .setLayer(TEST_LAYER)
            .setCornerRadius(TEST_CORNER_RADIUS)
            .setBounds(TEST_BOUNDS)
            .setSafeBounds(TEST_SAFE_BOUNDS)
            .setInsets(TEST_INSETS)
            .build()

        val str = visualData.toString()
        assertNotNull(str)
        assertFalse(str.isEmpty())
        // Kotlin data class toString includes property names from primary constructor
        assertTrue(str.contains("alpha=$TEST_ALPHA"))
        assertTrue(str.contains("isVisible=$TEST_IS_VISIBLE"))
        assertTrue(str.contains("layer=$TEST_LAYER"))
        assertTrue(str.contains("cornerRadius=$TEST_CORNER_RADIUS"))
        assertTrue(str.contains("_bounds=$TEST_BOUNDS"))
        assertTrue(str.contains("_safeBounds=$TEST_SAFE_BOUNDS"))
        assertTrue(str.contains("_insets=$TEST_INSETS"))
    }

    @Test
    fun testGetters_defensiveCopies() {
        val originalBounds = Rect(10, 10, 20, 20)
        val originalSafeBounds = Rect(12, 12, 18, 18)
        val originalInsets = Insets.of(1, 1, 1, 1)

        val visualData = VisualData.Builder()
            .setBounds(originalBounds)
            .setSafeBounds(originalSafeBounds)
            .setInsets(originalInsets)
            .build()

        val retrievedBounds = visualData.bounds
        val retrievedSafeBounds = visualData.safeBounds
        val retrievedInsets = visualData.insets

        // Check they are equal in value
        assertEquals(originalBounds, retrievedBounds)
        assertEquals(originalSafeBounds, retrievedSafeBounds)
        assertEquals(originalInsets, retrievedInsets)

        // Modify the retrieved objects
        retrievedBounds.set(0, 0, 0, 0)
        retrievedSafeBounds.set(1, 1, 1, 1)
        // Insets are immutable, but the getter returns a new instance.

        // Check that the original values in VisualData are unchanged
        assertEquals(originalBounds, visualData.bounds)
        assertEquals(originalSafeBounds, visualData.safeBounds)
        assertEquals(originalInsets, visualData.insets)

        // Also check that the builder makes defensive copies on set
        val builder = VisualData.Builder()
        val mutableBoundsForSet = Rect(50, 50, 60, 60)
        val mutableSafeBoundsForSet = Rect(52, 52, 58, 58)
        val mutableInsetsForSet = Insets.of(2, 2, 2, 2) // Though Insets are immutable

        builder.setBounds(mutableBoundsForSet)
        builder.setSafeBounds(mutableSafeBoundsForSet)
        builder.setInsets(mutableInsetsForSet)

        mutableBoundsForSet.set(100, 100, 100, 100)
        mutableSafeBoundsForSet.set(101, 101, 101, 101)
        // No modification needed for Insets as it's immutable

        val builtData = builder.build()
        assertNotEquals(mutableBoundsForSet, builtData.bounds)
        assertEquals(Rect(50, 50, 60, 60), builtData.bounds)
        assertNotEquals(mutableSafeBoundsForSet, builtData.safeBounds)
        assertEquals(Rect(52, 52, 58, 58), builtData.safeBounds)
        assertEquals(Insets.of(2, 2, 2, 2), builtData.insets)
    }
}
