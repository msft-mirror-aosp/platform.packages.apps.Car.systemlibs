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
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.panel.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a transition between two {@link Variant}s in the Scalable UI system.
 *
 * <p>A Transition defines the animation that should be used to transition from one variant to
 * another in response to an event. It can optionally specify a specific "from" variant, a "to"
 * variant, an event trigger, and a custom animator.
 */
public class Transition {
    public static final long DEFAULT_DURATION = 300;
    private static final String TAG = Transition.class.getSimpleName();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);

    @Nullable private final Variant mFromVariant;
    @NonNull private final Variant mToVariant;
    @NonNull private final List<Event> mEvents;
    @Nullable private final Animator mAnimator;
    @NonNull private final Interpolator mDefaultInterpolator;
    private final long mDefaultDuration;
    private final long mDelay;

    /**
     * Constructor for Transition. Package-private; use the Builder.
     *
     * @param fromVariant The variant to transition from (can be null).
     * @param toVariant The variant to transition to.
     * @param events The events that trigger the transition.
     * @param animator A custom animator to use for the transition (can be null).
     * @param defaultDuration The default duration of the transition.
     * @param defaultInterpolator The default interpolator to use for the transition.
     */
    Transition(
            @Nullable Variant fromVariant,
            @NonNull Variant toVariant,
            @NonNull List<Event> events,
            @Nullable Animator animator,
            long defaultDuration,
            long delay,
            @Nullable Interpolator defaultInterpolator) {
        mFromVariant = fromVariant;
        mToVariant = toVariant;
        mAnimator = animator;
        mEvents = events;
        mDelay = delay;
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
        if (DEBUG) {
            Log.d(TAG, "panel=" + panel.getPanelId() + "fromVariant=" + fromVariant + ", toVariant"
                    + mToVariant);
        }
        if (Objects.equals(fromVariant.getId(), mToVariant.getId())) {
            return null;
        }

        if (mAnimator != null) {
            Animator animator = this.mAnimator.clone();
            animator.setTarget(panel);
            return animator;
        }
        return fromVariant.getAnimator(
                panel, mToVariant, mDefaultDuration, mDelay, mDefaultInterpolator);
    }

    /**
     * Returns true if the event triggers the transition.
     *
     * @return The event that is checked against the transition.
     */
    public boolean isTriggeredBy(Event event) {
        for (Event e : mEvents) {
            if (event.isMatch(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NonNull
    public String toString() {
        return "Transition{"
                + "mFromVariant=" + (mFromVariant != null ? mFromVariant : "null")
                + ", mToVariant=" + (mToVariant != null ? mToVariant : "null")
                + ", mEvents=" + mEvents
                + ", mAnimator=" + mAnimator
                + ", mDefaultInterpolator=" + mDefaultInterpolator
                + ", mDefaultDuration=" + mDefaultDuration
                + ", mDelay=" + mDelay
                + '}';
    }

    /** Builder for {@link Transition} objects. */
    public static class Builder {
        @Nullable private Variant mFromVariant; // Now nullable
        @NonNull private Variant mToVariant;
        @NonNull private List<Event> mEvents;
        @Nullable private Animator mAnimator;
        @Nullable private Interpolator mDefaultInterpolator;
        @Nullable private Long mDefaultDuration; // Use boxed type Long
        private long mDelay;

        public Builder(@Nullable Variant fromVariant, @NonNull Variant toVariant) {
            mFromVariant = fromVariant;
            mToVariant = toVariant;
            mEvents = new ArrayList<>();
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

        /** Adds to the list of events that can trigger this transition */
        public Builder addEvents(@NonNull List<Event> events) {
            mEvents.addAll(events);
            return this;
        }

        /** Adds an event that can trigger this transition */
        public Builder addEvent(@NonNull Event event) {
            mEvents.add(event);
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

        /** Sets delay */
        public Builder setDelay(long delay) {
            mDelay = delay;
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
                    mEvents,
                    mAnimator,
                    mDefaultDuration != null ? mDefaultDuration : DEFAULT_DURATION,
                    mDelay,
                    mDefaultInterpolator);
        }
    }
}
