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

import android.graphics.Insets
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.annotation.NonNull
import androidx.annotation.Nullable

/**
 * A specialized version of [Variant] for Heads-Up Notifications (Huns).
 *
 * This class extends the base [Variant] to include Hun-specific properties like a scrim drawable.
 */
open class HunVariant : Variant {
    @get:Nullable
    val scrim: Drawable?
    val gravity: Int
    internal constructor(
        id: String,
        idName: String,
        scrim: Drawable?,
        gravity: Int
    ) : super(id, idName) {
        this.scrim = scrim
        this.gravity = gravity
    }

    internal constructor(
        id: String,
        base: Variant,
        idName: String,
        scrim: Drawable?,
        gravity: Int
    ) : super(id, base, idName) {
        this.scrim = scrim
        this.gravity = gravity
    }

    /** Builder for [HunVariant] objects. */
    class Builder(id: String, idName: String) : Variant.Builder(id, idName) {
        private var scrim: Drawable? = null
        private var gravity: Int = Gravity.NO_GRAVITY

        /** Sets the scrim for this variant. */
        fun setScrim(scrim: Drawable?): Builder = apply { this.scrim = scrim }

        /** Sets the gravity for this variant. */
        fun setGravity(gravity: Int): Builder = apply { this.gravity = gravity }

        // Override parent methods to return the correct builder type for chaining.
        override fun setParent(parent: Variant?): Builder = apply { super.setParent(parent) }
        override fun setAlpha(alpha: Float?): Builder = apply { super.setAlpha(alpha) }
        override fun setVisibility(isVisible: Boolean?): Builder =
            apply { super.setVisibility(isVisible) }
        override fun setLayer(layer: Int?): Builder = apply { super.setLayer(layer) }
        override fun setCanFocusOnTransition(canFocusOnTransition: Boolean?): Builder =
            apply { super.setCanFocusOnTransition(canFocusOnTransition) }
        override fun setBounds(bounds: Rect): Builder = apply { super.setBounds(bounds) }
        override fun setSafeBounds(safeBounds: Rect): Builder =
            apply { super.setSafeBounds(safeBounds) }
        override fun setCornerRadius(cornerRadius: Int): Builder =
            apply { super.setCornerRadius(cornerRadius) }
        override fun setInsets(insets: Insets): Builder = apply { super.setInsets(insets) }

        /** Returns the [HunVariant] instance. */
        @NonNull
        override fun build(): HunVariant {
            // Create a local, immutable copy of mParent to avoid smart cast errors.
            val parentVariant = mParent
            val variant = parentVariant?.let { HunVariant(mId, it, mIdName, scrim, gravity) }
                ?: HunVariant(mId, mIdName, scrim, gravity)

            // Apply properties from the builder to the new variant instance
            mAlpha?.let { variant.alpha = it }
            mIsVisible?.let { variant.setVisibility(it) }
            mLayer?.let { variant.layer = it }
            mCanFocusOnTransition?.let { variant.setCanFocusOnTransition(it) }
            mBounds?.let { variant.bounds = Rect(it) }
            mSafeBounds?.let { variant.safeBounds = Rect(it) }
            mCornerRadius?.let { variant.cornerRadius = it }
            mInsets?.let { variant.insets = Insets.of(it.left, it.top, it.right, it.bottom) }

            return variant
        }
    }
}
