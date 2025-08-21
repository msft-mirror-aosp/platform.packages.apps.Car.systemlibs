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

import androidx.annotation.NonNull

/**
 * A specialized version of [PanelState] for Heads-Up Notifications (Huns).
 *
 * This class uses a custom [HunVariant] to hold variant-specific attributes.
 */
class HunState(id: String, type: @PanelType Int) : PanelState(id, type) {

    /**
     * Builder for [HunState] objects.
     */
    class Builder(@NonNull private val id: String, private val type: @PanelType Int) {
        private var defaultVariant: String? = null
        private var displayId: Int? = null
        private val variants = mutableListOf<Variant>()
        private val transitions = mutableListOf<Transition>()

        /** Sets default variant */
        fun setDefaultVariant(defaultVariant: String?): Builder = apply {
            this.defaultVariant = defaultVariant
        }

        /** Sets display id */
        fun setDisplayId(displayId: Int): Builder = apply {
            this.displayId = displayId
        }

        /** Adds a variant */
        fun addVariant(variant: Variant): Builder = apply {
            variants.add(variant)
        }

        /** Adds a transitions */
        fun addTransition(transition: Transition): Builder = apply {
            transitions.add(transition)
        }

        /** Sets variants */
        fun setVariants(variants: List<Variant>): Builder = apply {
            this.variants.clear()
            this.variants.addAll(variants)
        }

        /** Sets transitions */
        fun setTransitions(transitions: List<Transition>): Builder = apply {
            this.transitions.clear()
            this.transitions.addAll(transitions)
        }

        /**
         * Builds and returns the configured [HunState] object.
         *
         * @return A new [HunState].
         */
        @NonNull
        fun build(): HunState {
            val hunState = HunState(id, type)
            hunState.setDefaultVariant(defaultVariant)
            displayId?.let { hunState.displayId = it }
            hunState.setVariants(variants)
            hunState.setTransitions(transitions)
            return hunState
        }
    }
}
