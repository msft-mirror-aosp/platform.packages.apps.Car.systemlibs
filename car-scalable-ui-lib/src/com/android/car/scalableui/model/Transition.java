/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.car.scalableui.model;

import android.animation.Animator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.panel.Panel;

/**
 * Represents a transition between two {@link Variant}s in the Scalable UI system.
 *
 * <p>A Transition defines the animation that should be used to transition from one variant to
 * another in response to an event. It can optionally specify a specific "from" variant, a "to"
 * variant, an event trigger, and a custom animator.
 */
public class Transition {
    public static final long DEFAULT_DURATION = 300;

    @Nullable private final Variant mFromVariant;
    @NonNull private final Variant mToVariant;
    @Nullable private final Event mOnEvent;
    @Nullable private final Animator mAnimator;
    @NonNull private final Interpolator mDefaultInterpolator;
    private final long mDefaultDuration;

    /**
     * Constructor for Transition. Package-private; use the Builder.
     *
     * @param fromVariant The variant to transition from (can be null).
     * @param toVariant The variant to transition to.
     * @param onEvent The event that triggers the transition.
     * @param animator A custom animator to use for the transition (can be null).
     * @param defaultDuration The default duration of the transition.
     * @param defaultInterpolator The default interpolator to use for the transition.
     */
    Transition(
            @Nullable Variant fromVariant,
            @NonNull Variant toVariant,
            @Nullable Event onEvent,
            @Nullable Animator animator,
            long defaultDuration,
            @Nullable Interpolator defaultInterpolator) {
        mFromVariant = fromVariant;
        mToVariant = toVariant;
        mAnimator = animator;
        mOnEvent = onEvent;
        mDefaultDuration = defaultDuration >= 0 ? defaultDuration : DEFAULT_DURATION;
        mDefaultInterpolator =
                defaultInterpolator != null
                        ? defaultInterpolator
                        : new AccelerateDecelerateInterpolator();
    }

    /**
     * Returns the "from" variant of the transition.
     *
     * @return The "from" variant, or null if not specified.
     */
    @Nullable
    public Variant getFromVariant() {
        return mFromVariant;
    }

    /**
     * Returns the "to" variant of the transition.
     *
     * @return The "to" variant.
     */
    @NonNull
    public Variant getToVariant() {
        return mToVariant;
    }

    /**
     * Returns the animator for the transition.
     *
     * <p>If a custom animator was provided, it is cloned and returned. Otherwise, a default
     * animator will be created to transition from "from" variant to "to" variant with the default
     * duration and interpolator.
     *
     * @param panel The panel to apply the animation to.
     * @param fromVariant The actual "from" variant of the transition.
     * @return The animator for the transition.
     */
    @Nullable
    public Animator getAnimator(@NonNull Panel panel, @NonNull Variant fromVariant) {
        if (fromVariant.getId().equals(mToVariant.getId())) {
            return null;
        }

        if (mAnimator != null) {
            Animator animator = this.mAnimator.clone();
            animator.setTarget(panel);
            return animator;
        }
        return fromVariant.getAnimator(
                panel, mToVariant, mDefaultDuration, mDefaultInterpolator);
    }

    /**
     * Returns the event that triggers the transition.
     *
     * @return The event that triggers the transition.
     */
    @Nullable
    public Event getOnEvent() {
        return mOnEvent;
    }

    @Override
    @NonNull
    public String toString() {
        return "Transition{"
                + "mFromVariant=" + (mFromVariant != null ? mFromVariant : "null")
                + ", mToVariant=" + (mToVariant != null ? mToVariant : "null")
                + ", mOnEvent=" + mOnEvent
                + ", mAnimator=" + mAnimator
                + ", mDefaultInterpolator=" + mDefaultInterpolator
                + ", mDefaultDuration=" + mDefaultDuration
                + '}';
    }

    /** Builder for {@link Transition} objects. */
    public static class Builder {
        @Nullable private Variant mFromVariant; // Now nullable
        @NonNull private Variant mToVariant;
        @Nullable private Event mOnEvent;
        @Nullable private Animator mAnimator;
        @Nullable private Interpolator mDefaultInterpolator;
        @Nullable private Long mDefaultDuration; // Use boxed type Long

        public Builder(@Nullable Variant fromVariant, @NonNull Variant toVariant) {
            mFromVariant = fromVariant;
            mToVariant = toVariant;
        }

        /** Sets from variant */
        public Builder setFromVariant(@Nullable Variant fromVariant) {
            mFromVariant = fromVariant; // Accept null
            return this;
        }

        /** Sets to variant */
        public Builder setToVariant(@NonNull Variant toVariant) {
            mToVariant = toVariant;
            return this;
        }

        /** Sets onEvent */
        public Builder setOnEvent(@Nullable String eventId, @Nullable String eventTokens) {
            if (eventId == null) {
                mOnEvent = null;
            } else {
                mOnEvent = new Event.Builder(eventId)
                        .addTokensFromString(eventTokens)
                        .build();
            }
            return this;
        }

        /** Sets animator */
        public Builder setAnimator(@Nullable Animator animator) {
            mAnimator = animator;
            return this;
        }

        /** Sets default duration */
        public Builder setDefaultDuration(long duration) {
            mDefaultDuration = duration;
            return this;
        }

        /** Sets default interpolator */
        public Builder setDefaultInterpolator(@Nullable Interpolator interpolator) {
            mDefaultInterpolator = interpolator;
            return this;
        }

        /** Returns the {@link Transition} instance */
        @NonNull
        public Transition build() {
            return new Transition(
                    mFromVariant,
                    mToVariant,
                    mOnEvent,
                    mAnimator,
                    mDefaultDuration != null ? mDefaultDuration : DEFAULT_DURATION,
                    mDefaultInterpolator);
        }
    }
}
