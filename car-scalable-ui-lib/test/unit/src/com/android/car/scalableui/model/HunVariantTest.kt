/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.scalableui.model

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class HunVariantTest {

    @Test
    fun builder_setsScrimCorrectly() {
        val scrimDrawable = mock(Drawable::class.java)
        val builder = HunVariant.Builder("testId", "testIdName")
            .setScrim(scrimDrawable)

        val hunVariant = builder.build()

        assertThat(hunVariant.scrim).isEqualTo(scrimDrawable)
    }

    @Test
    fun builder_setsGravityCorrectly() {
        val builder = HunVariant.Builder("testId", "testIdName")
            .setGravity(Gravity.CENTER)

        val hunVariant = builder.build()

        assertThat(hunVariant.gravity).isEqualTo(Gravity.CENTER)
    }

    @Test
    fun builder_setsInheritedPropertiesCorrectly() {
        val testBounds = Rect(10, 20, 30, 40)

        val builder = HunVariant.Builder("testId", "testIdName")
            .setBounds(testBounds)

        val hunVariant = builder.build()

        assertThat(hunVariant.bounds).isEqualTo(testBounds)
    }

    @Test
    fun builder_inheritsFromParent() {
        val parentBounds = Rect(0, 0, 100, 100)
        val parentVariant = Variant.Builder("parentId", "parentIdName")
            .setBounds(parentBounds)
            .build()

        val builder = HunVariant.Builder("childId", "childIdName")
            .setParent(parentVariant)

        val hunVariant = builder.build()

        assertThat(hunVariant.bounds).isEqualTo(parentBounds)
        assertThat(hunVariant.scrim).isNull()
        assertThat(hunVariant.gravity).isEqualTo(Gravity.NO_GRAVITY)
    }

    @Test
    fun builder_overridesParentProperties() {
        val parentBounds = Rect(0, 0, 100, 100)
        val childBounds = Rect(10, 10, 90, 90)
        val scrimDrawable = mock(Drawable::class.java)

        val parentVariant = Variant.Builder("parentId", "parentIdName")
            .setBounds(parentBounds)
            .build()

        val builder = HunVariant.Builder("childId", "childIdName")
            .setParent(parentVariant)
            .setBounds(childBounds)
            .setScrim(scrimDrawable)
            .setGravity(Gravity.CENTER)

        val hunVariant = builder.build()

        assertThat(hunVariant.bounds).isEqualTo(childBounds)
        assertThat(hunVariant.scrim).isEqualTo(scrimDrawable)
        assertThat(hunVariant.gravity).isEqualTo(Gravity.CENTER)
    }

    @Test
    fun builder_handlesNullParent() {
        val scrimDrawable = mock(Drawable::class.java)
        val builder = HunVariant.Builder("testId", "testIdName")
            .setParent(null)
            .setScrim(scrimDrawable)
            .setGravity(Gravity.CENTER)

        val hunVariant = builder.build()

        assertThat(hunVariant.scrim).isEqualTo(scrimDrawable)
        assertThat(hunVariant.gravity).isEqualTo(Gravity.CENTER)
    }

    @Test
    fun builder_chainingMethodsReturnCorrectType() {
        val builder = HunVariant.Builder("testId", "testIdName")
            .setVisibility(false)
            .setScrim(null)
            .setGravity(Gravity.CENTER)

        assertThat(builder).isInstanceOf(HunVariant.Builder::class.java)
    }
}
