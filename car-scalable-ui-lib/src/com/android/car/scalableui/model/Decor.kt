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

import android.content.Context
import android.view.LayoutInflater
import android.view.View

/**
 * Represents a decorative element.
 *
 * This data class holds the configuration for a single decor item, including its unique identifier,
 * layering order, color, transparency, and the layout resource for its content.
 *
 * @property id A unique identifier for this decor. Defaults to "Default decor ID".
 * @property layer The layer level for this decor, used for ordering multiple decors. Defaults to -1.
 * @property colorRes The color resource to be used as the background color. Defaults to -1.
 * @property alpha The transparency of the decor, ranging from 0.0 (fully transparent) to 1.0 (fully opaque). Defaults to 1f.
 * @property content The layout resource ID for the decor's view content. Defaults to -1.
 */
data class Decor(
    val id: String = "Default decor ID",
    val layer: Int = -1,
    val colorRes: Int = -1,
    val alpha: Float = 1f,
    val content: Int = -1
) {

    /**
     * Inflates and returns the [View] for this decor.
     *
     * This function uses the provided [content] layout resource to create a view and applies the
     * specified [colorRes] as its background color.
     *
     * @param context The [Context] used for inflating the layout and resolving the color resource.
     * @return The inflated [View] with the background color set.
     */
    fun getView(context: Context): View {
        val view = LayoutInflater.from(context).inflate(content, null)
        view.setBackgroundColor(context.getColor(colorRes))
        return view
    }
}
