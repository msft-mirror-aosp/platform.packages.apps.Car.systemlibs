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

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Insets
import android.graphics.Rect
import android.view.Gravity
import android.view.animation.Interpolator
import androidx.annotation.NonNull
import com.android.car.scalableui.panel.Panel

/**
 * A specialized version of [Variant] that includes a gravity property.
 */
open class GravityVariant : Variant {
    var gravity: Int

    internal constructor(
        id: String,
        idName: String,
        gravity: Int
    ) : super(id, idName) {
        this.gravity = gravity
    }

    internal constructor(
        id: String,
        base: Variant,
        idName: String
    ) : super(id, base, idName) {
        this.gravity = if (base is GravityVariant) base.gravity else Gravity.NO_GRAVITY
    }

    override fun getAnimator(
        panel: Panel,
        toVariant: Variant,
        duration: Long,
        delay: Long,
        interpolator: Interpolator?
    ): Animator? {
        val animator = super.getAnimator(panel, toVariant, duration, delay, interpolator)
        if (toVariant is GravityVariant && animator is ValueAnimator) {
            animator.addUpdateListener {
                panel.gravity = toVariant.gravity
            }
        }
        return animator
    }

    /** Builder for [GravityVariant] objects. */
    open class Builder(id: String, idName: String) : Variant.Builder(id, idName) {
        protected var gravity: Int = Gravity.NO_GRAVITY

        /** Sets the gravity for this variant. */
        open fun setGravity(gravity: Int): Builder = apply { this.gravity = gravity }

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
        override fun setCornerRadius(cornerRadius: Corner): Builder =
            apply { super.setCornerRadius(cornerRadius) }
        override fun setInsets(insets: Insets): Builder = apply { super.setInsets(insets) }
        override fun addDecor(decor: Decor): Builder = apply { super.addDecor(decor) }

        /** Returns the [GravityVariant] instance. */
        @NonNull
        override fun build(): GravityVariant {
            val localParent = mParent
            // Let the constructor handle inheritance.
            val variant = if (localParent != null) {
                GravityVariant(mId, localParent, mIdName)
            } else {
                GravityVariant(mId, mIdName, gravity)
            }

            // Apply this builder's specific overrides.
            mAlpha?.let { variant.alpha = it }
            mIsVisible?.let { variant.setVisibility(it) }
            mLayer?.let { variant.layer = it }
            mCanFocusOnTransition?.let { variant.setCanFocusOnTransition(it) }
            mBounds?.let { variant.bounds = Rect(it) }
            mSafeBounds?.let { variant.safeBounds = Rect(it) }
            mCornerRadius?.let { variant.cornerRadius = it }
            mInsets?.let { variant.insets = Insets.of(it.left, it.top, it.right, it.bottom) }
            if (mDecors.isNotEmpty()) {
                variant.setDecors(mDecors)
            }
            // Override gravity only if it was explicitly set in this builder.
            if (gravity != Gravity.NO_GRAVITY) {
                variant.gravity = gravity
            }

            return variant
        }
    }

    @NonNull
    override fun toString(): String {
        val decorString = if (decors.isEmpty()) {
            "empty"
        } else {
            decors.entries.joinToString(" , ") { (key, value) -> "$key=$value" }
        }

        return "GravityVariant{" +
          " mIdName=" + idName +
          " mAlpha=" + alpha +
          " mIsVisible=" + isVisible +
          " mLayer=" + layer +
          " mCanFocusOnTransition=" + canFocusOnTransition() +
          " mBounds=" + bounds +
          " mSafeBounds=" + safeBounds +
          " mCornerRadius=" + cornerRadius +
          " mInsets=" + insets +
          " mDecors=" + decorString +
          " mGravity=" + gravity +
          '}'
    }
}
