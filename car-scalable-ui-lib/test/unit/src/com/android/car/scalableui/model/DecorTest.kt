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

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DecorTest {

    companion object {
        private const val TEST_ID = "testId123"
        private const val TEST_CONTENT = "This is test content."
        private val DEFAULT_VISUAL_DATA = VisualData.Builder().build()
        private val CUSTOM_VISUAL_DATA = VisualData.Builder()
            .setAlpha(0.75f)
            .setCornerRadius(15)
            .setIsVisible(false)
            .build()

        // Default values from Decor.Builder
        private const val BUILDER_DEFAULT_CONTENT = "Default content"
        // Note: The 'id' in Decor.Builder has a default "Default decor ID",
        // but the constructor `Builder(id: String)` immediately overwrites it.
    }

    @Test
    fun testBuilder_constructorSetsId_usesDefaultsForOthers() {
        val decor = Decor.Builder(TEST_ID).build()

        assertEquals(TEST_ID, decor.id)
        assertEquals(DEFAULT_VISUAL_DATA, decor.visualData)
        assertEquals(BUILDER_DEFAULT_CONTENT, decor.content)
    }

    @Test
    fun testBuilder_setAllValues() {
        val decor = Decor.Builder(TEST_ID)
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent(TEST_CONTENT)
            .build()

        assertEquals(TEST_ID, decor.id)
        assertEquals(CUSTOM_VISUAL_DATA, decor.visualData)
        assertEquals(TEST_CONTENT, decor.content)
    }

    @Test
    fun testBuilder_copyConstructor() {
        val originalDecor = Decor.Builder("originalId")
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent("Original Content")
            .build()

        val copiedDecor = Decor.Builder(originalDecor).build()

        assertEquals(originalDecor.id, copiedDecor.id)
        assertEquals(originalDecor.visualData, copiedDecor.visualData)
        assertEquals(originalDecor.content, copiedDecor.content)
        assertEquals(originalDecor, copiedDecor) // Should be equal in value

        // Ensure visualData is the same instance as VisualData is immutable
        assertTrue(originalDecor.visualData === copiedDecor.visualData)
    }

    @Test
    fun testBuilder_copyConstructor_thenModify() {
        val originalDecor = Decor.Builder("idToCopy")
            .setVisualData(DEFAULT_VISUAL_DATA)
            .setContent("Initial Content")
            .build()

        val newId = "newIdAfterCopy"
        val newContent = "New Content After Copy"
        val newVisualData = VisualData.Builder().setAlpha(0.1f).build()

        val modifiedDecor = Decor.Builder(originalDecor)
            // Note: Decor.Builder does not have setId, id is set via constructor.
            // To change id, one would typically create a new builder: Decor.Builder(newId)
            // and then copy other fields or set them.
            // For this test, we'll assume we are building a new Decor based on an old one
            // but with some new properties.
            // If we wanted to change the ID using the copy constructor, we'd need a setId in builder
            // or re-construct: Decor.Builder(newId).setVisualData(original.visualData)...
            .setVisualData(newVisualData)
            .setContent(newContent)
            .build()

        assertEquals(originalDecor.id, modifiedDecor.id) // ID remains from the copied Decor
        assertEquals(newVisualData, modifiedDecor.visualData)
        assertEquals(newContent, modifiedDecor.content)
        assertNotEquals(originalDecor, modifiedDecor)
    }

    @Test
    fun testEqualsAndHashCode() {
        val decor1 = Decor.Builder(TEST_ID)
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent(TEST_CONTENT)
            .build()

        val decor2 = Decor.Builder(TEST_ID)
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent(TEST_CONTENT)
            .build()

        val decor3_diffId = Decor.Builder("differentId")
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent(TEST_CONTENT)
            .build()

        val decor4_diffVisualData = Decor.Builder(TEST_ID)
            .setVisualData(DEFAULT_VISUAL_DATA) // Different VisualData
            .setContent(TEST_CONTENT)
            .build()

        val decor5_diffContent = Decor.Builder(TEST_ID)
            .setVisualData(CUSTOM_VISUAL_DATA)
            .setContent("Different Content")
            .build()

        assertEquals(decor1, decor2)
        assertEquals(decor1.hashCode(), decor2.hashCode())

        assertNotEquals(decor1, decor3_diffId)
        assertNotEquals(decor1.hashCode(), decor3_diffId.hashCode()) // Hashcodes likely different

        assertNotEquals(decor1, decor4_diffVisualData)
        assertNotEquals(decor1, decor5_diffContent)

        assertFalse(decor1.equals(null))
        assertFalse(decor1.equals("a string"))
    }

    @Test
    fun testToString() {
        val decor = Decor(TEST_ID, CUSTOM_VISUAL_DATA, TEST_CONTENT, float)
        val str = decor.toString()

        assertNotNull(str)
        assertTrue(str.contains("id=$TEST_ID"))
        assertTrue(str.contains("visualData=$CUSTOM_VISUAL_DATA"))
        assertTrue(str.contains("content=$TEST_CONTENT"))
    }
}
