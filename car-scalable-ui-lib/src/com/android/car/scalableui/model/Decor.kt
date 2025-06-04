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
 * Represents a decor element with an identifier, visual properties, and content.
 *
 * @property id The unique identifier for the decor element.
 * @property colorRes The visual properties of the decor element.
 * @property layer The string content associated with the decor element.
 */
data class Decor(
    val id: String,
    val layer: Int,
    val colorRes: Int,
    val alpha: Float,
    val content: Int
) {

    fun getView(context: Context): View {
        val view = LayoutInflater.from(context).inflate(content, null)
        view.setBackgroundColor(context.getColor(colorRes))
        return view
    }

    /** Builder for [Decor] objects. */
    class Builder {
        private var id: String = "Default decor ID"
        private var colorRes: Int = -1
        private var layer: Int = -1
        private var alpha: Float = 1f
        private var content: Int = -1

        /**
         * Creates a Builder.
         */
        constructor(id: String) {
            this.id = id
        }

        /**
         * Creates a Builder initialized with values from an existing [Decor] object.
         *
         * @param decor The Decor instance to copy values from.
         */
        constructor(decor: Decor) {
            this.id = decor.id
            this.colorRes = decor.colorRes // VisualData is immutable
            this.layer = decor.layer
            this.alpha = decor.alpha
            this.content = decor.content
        }

        /** Sets the visual properties of the decor element. */
        fun setColor(colorRes: Int) = apply { this.colorRes = colorRes }

        /** Sets the visual properties of the decor element. */
        fun setAlpha(alpha: Float) = apply { this.alpha = alpha }

        /** Sets the string content associated with the decor element. */
        fun setLayer(layer: Int) = apply { this.layer = layer }

        /** Sets the string content associated with the decor element. */
        fun setContent(contentRes: Int) = apply { this.content = contentRes }

        /** Builds the [Decor] instance. */
        fun build(): Decor {
            return Decor(
                id = id,
                layer = layer,
                colorRes = colorRes,
                alpha = alpha,
                content = content
            )
        }
    }
}
