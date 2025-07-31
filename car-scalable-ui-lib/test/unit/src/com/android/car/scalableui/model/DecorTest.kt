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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DecorTest {

    companion object {
        private const val TEST_ID = "testId123"
        private const val TEST_LAYER = 5
        private const val TEST_COLOR_RES = 12345
        private const val TEST_ALPHA = 0.75f
        private const val TEST_CONTENT_ID = 54321
    }

    @Test
    fun constructor_withAllValues_setsPropertiesCorrectly() {
        val decor = Decor(
            id = TEST_ID,
            layer = TEST_LAYER,
            colorRes = TEST_COLOR_RES,
            alpha = TEST_ALPHA,
            content = TEST_CONTENT_ID
        )

        assertThat(decor.id).isEqualTo(TEST_ID)
        assertThat(decor.layer).isEqualTo(TEST_LAYER)
        assertThat(decor.colorRes).isEqualTo(TEST_COLOR_RES)
        assertThat(decor.alpha).isEqualTo(TEST_ALPHA)
        assertThat(decor.content).isEqualTo(TEST_CONTENT_ID)
    }

    @Test
    fun constructor_withDefaultValues_usesDefaults() {
        val decor = Decor()

        assertThat(decor.id).isEqualTo("Default decor ID")
        assertThat(decor.layer).isEqualTo(-1)
        assertThat(decor.colorRes).isEqualTo(-1)
        assertThat(decor.alpha).isEqualTo(1f)
        assertThat(decor.content).isEqualTo(-1)
    }

    @Test
    fun equalsAndHashCode_forEqualObjects_areTrue() {
        val decor1 = Decor(
            id = TEST_ID,
            layer = TEST_LAYER,
            colorRes = TEST_COLOR_RES,
            alpha = TEST_ALPHA,
            content = TEST_CONTENT_ID
        )
        val decor2 = Decor(
            id = TEST_ID,
            layer = TEST_LAYER,
            colorRes = TEST_COLOR_RES,
            alpha = TEST_ALPHA,
            content = TEST_CONTENT_ID
        )

        assertThat(decor1).isEqualTo(decor2)
        assertThat(decor1.hashCode()).isEqualTo(decor2.hashCode())
    }

    @Test
    fun equalsAndHashCode_forDifferentObjects_areFalse() {
        val decor1 = Decor(id = "id1")
        val decor2 = Decor(id = "id2")

        assertThat(decor1).isNotEqualTo(decor2)
        assertThat(decor1.hashCode()).isNotEqualTo(decor2.hashCode())
    }

    @Test
    fun toString_containsAllProperties() {
        val decor = Decor(
            id = TEST_ID,
            layer = TEST_LAYER,
            colorRes = TEST_COLOR_RES,
            alpha = TEST_ALPHA,
            content = TEST_CONTENT_ID
        )
        val str = decor.toString()

        assertThat(str).contains("id=$TEST_ID")
        assertThat(str).contains("layer=$TEST_LAYER")
        assertThat(str).contains("colorRes=$TEST_COLOR_RES")
        assertThat(str).contains("alpha=$TEST_ALPHA")
        assertThat(str).contains("content=$TEST_CONTENT_ID")
    }
}
