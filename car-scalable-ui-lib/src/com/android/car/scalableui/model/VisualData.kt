/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may
 * obtain a copy of the License at
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
import kotlin.math.max

/**
 * Represents the visual properties of a UI element.
 *
 * This data class is a holder for properties like alpha, visibility, layer, corner radius,
 * bounds, safe bounds, and insets.
 *
 * @property alpha The alpha value (0.0f to 1.0f).
 * @property isVisible True if the element is visible, false otherwise.
 * @property layer The rendering layer.
 * @property cornerRadius The corner radius in pixels.
 * @property bounds The bounds of the element. A defensive copy is returned by the getter.
 * @property safeBounds The safe bounds of the element, considering system intrusions. A defensive
 *   copy is returned by the getter.
 * @property insets The insets of the element. A defensive copy is returned by the getter.
 */
data class VisualData(
    val alpha: Float,
    val isVisible: Boolean,
    val layer: Int,
    val cornerRadius: Int,
    private val _bounds: Rect, // Private backing field for defensive copying
    private val _safeBounds: Rect, // Private backing field for defensive copying
    private val _insets: Insets // Private backing field for defensive copying
) {

    // Custom getter for bounds to ensure defensive copying
    val bounds: Rect
        get() = Rect(_bounds)

    // Custom getter for safeBounds to ensure defensive copying
    val safeBounds: Rect
        get() = Rect(_safeBounds)

    // Custom getter for insets to ensure defensive copying
    val insets: Insets
        get() = Insets.of(_insets.left, _insets.top, _insets.right, _insets.bottom)

    /** Builder for [VisualData] objects. */
    class Builder {
        private var alpha: Float = Alpha.DEFAULT_ALPHA
        private var isVisible: Boolean = Visibility.DEFAULT_VISIBILITY
        private var layer: Int = Layer.DEFAULT_LAYER
        private var cornerRadius: Int = Corner.DEFAULT_RADIUS
        private var bounds: Rect = Bounds.DEFAULT_BOUNDS
        private var safeBounds: Rect = Bounds.DEFAULT_BOUNDS
        private var insets: Insets = Insets.NONE

        constructor()

        /**
         * Creates a Builder initialized with values from an existing [VisualData] object.
         *
         * @param visualData The VisualData instance to copy values from.
         */
        constructor(visualData: VisualData) {
            this.alpha = visualData.alpha
            this.isVisible = visualData.isVisible
            this.layer = visualData.layer
            this.cornerRadius = visualData.cornerRadius
            this.bounds = visualData.bounds // Uses the getter which returns a copy
            this.safeBounds = visualData.safeBounds // Uses the getter which returns a copy
            this.insets = visualData.insets // Uses the getter which returns a copy
        }

        /** Sets the alpha value (0.0f to 1.0f). */
        fun setAlpha(alpha: Float) = apply {
            this.alpha = when {
                alpha < 0.0f -> 0.0f
                alpha > 1.0f -> 1.0f
                else -> alpha
            }
        }

        /** Sets whether the element is visible. */
        fun setIsVisible(isVisible: Boolean) = apply { this.isVisible = isVisible }

        /** Sets the rendering layer. */
        fun setLayer(layer: Int) = apply { this.layer = layer }

        /** Sets the corner radius in pixels. */
        fun setCornerRadius(cornerRadius: Int) = apply {
            this.cornerRadius = max(0, cornerRadius)
        }

        /** Sets the bounds of the element. A defensive copy is made. */
        fun setBounds(bounds: Rect) = apply { this.bounds = Rect(bounds) }

        /**
         * Sets the safe bounds of the element. A defensive copy is made. These bounds should
         * account for system intrusions.
         */
        fun setSafeBounds(safeBounds: Rect) = apply { this.safeBounds = Rect(safeBounds) }

        /** Sets the insets of the element. A defensive copy is made. */
        fun setInsets(insets: Insets) = apply {
            this.insets = Insets.of(insets.left, insets.top, insets.right, insets.bottom)
        }

        /** Builds the [VisualData] instance. */
        fun build(): VisualData {
            val finalSafeBounds =
                if (safeBounds.isEmpty && !bounds.isEmpty &&
                    safeBounds.left == 0 && safeBounds.top == 0 &&
                    safeBounds.right == 0 && safeBounds.bottom == 0
                ) {
                    Rect(bounds)
                } else {
                    safeBounds
                }
            return VisualData(
                alpha = alpha,
                isVisible = isVisible,
                layer = layer,
                cornerRadius = cornerRadius,
                _bounds = bounds, // Pass the builder's (copied) Rect
                _safeBounds = finalSafeBounds, // Pass the builder's (copied or defaulted) Rect
                _insets = insets // Pass the builder's (copied) Insets
            )
        }
    }
}
