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

import android.platform.test.annotations.RequiresFlagsEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.scalableui.Flags
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class HunStateTest {

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun builder_buildsCorrectly() {
        val panelId = "_Hun_Panel"
        val defaultVariant = "closed"
        val displayId = 1

        val variant1 = mock(GravityVariant::class.java)
        val variant2 = mock(GravityVariant::class.java)
        val transition1 = mock(Transition::class.java)

        val builder = HunState.Builder(panelId, PanelType.HUN)
        builder.setDefaultVariant(defaultVariant)
            .setDisplayId(displayId)
            .addVariant(variant1)
            .addVariant(variant2)
            .addTransition(transition1)

        val hunState = builder.build()

        assertEquals(panelId, hunState.id)
        assertEquals(displayId, hunState.displayId)
        assertEquals(variant1, hunState.currentVariant)
    }
}
