/*
 * Copyright (C) 2026 The Android Open Source Project
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

import android.graphics.drawable.Drawable
import com.android.car.scalableui.R

data class Background(
    val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    val backgroundAlpha: Float = 1.0f
) {
    /**
     * Converts this Background to a Decor.
     */
    fun toDecor(panelId: String): Decor {
        // ID is "${panelId}_Background" to identify it and avoid conflicts.
        return Decor(
            id = "${panelId}_Background",
            layer = -1,
            colorRes = -1,
            drawableRes = -1,
            alpha = backgroundAlpha,
            content = R.layout.background_layout,
            color = backgroundColor,
            drawable = backgroundDrawable
        )
    }
}
