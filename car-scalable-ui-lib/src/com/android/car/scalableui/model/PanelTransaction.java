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
package com.android.car.scalableui.model;

import android.animation.Animator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Represents a set of transactions to be applied to panels. */
public class PanelTransaction {

    /** A map of panel IDs to panel {@link Transition}s. */
    private final HashMap<String, Transition> mTransactionMap;

    /** A map of panel IDs to panel {@link Animator}s. */
    private final HashMap<String, Animator> mAnimatorMap;

    private Runnable mAnimationStartCallbackRunnable;
    private Runnable mAnimationEndCallbackRunnable;

    public PanelTransaction() {
        mTransactionMap = new HashMap<>();
        mAnimatorMap = new HashMap<>();
    }

    /**
     * Adds a {@link Transition} for the panel with the specified ID.
     *
     * @param id         The ID of the panel.
     * @param transition The transition to apply to the panel.
     */
    void addPanelTransaction(@NonNull String id, @NonNull Transition transition) {
        mTransactionMap.put(id, transition);
    }

    /** Returns a set of entries representing the transactions in this object. */
    @NonNull
    public Set<Map.Entry<String, Transition>> getPanelTransactionStates() {
        return mTransactionMap.entrySet();
    }

    /**
     * Adds a {@link Animator} for the panel with the specified ID.
     *
     * @param id       The ID of the panel.
     * @param animator The animator to apply to the panel.
     */
    void addAnimator(@NonNull String id, @Nullable Animator animator) {
        mAnimatorMap.put(id, animator);
    }

    /** Returns a set of entries representing the Animation for given panel. */
    @NonNull
    public Set<Map.Entry<String, Animator>> getAnimators() {
        return mAnimatorMap.entrySet();
    }

    /**
     * Adds a {@link Runnable} to be executed when the animations are starting for this
     * transaction.
     */
    void setAnimationStartCallbackRunnable(@NonNull Runnable runnable) {
        mAnimationStartCallbackRunnable = runnable;
    }

    /**
     * Adds a {@link Runnable} to be executed when the animations have finished for this
     * transaction.
     */
    void setAnimationEndCallbackRunnable(@NonNull Runnable runnable) {
        mAnimationEndCallbackRunnable = runnable;
    }

    /**
     * Get the {@link Runnable} to be executed when the animations are starting for this
     * transaction.
     */
    @Nullable
    public Runnable getAnimationStartCallbackRunnable() {
        return mAnimationStartCallbackRunnable;
    }

    /**
     * Get the {@link Runnable} to be executed when the animations have finished for this
     * transaction.
     */
    @Nullable
    public Runnable getAnimationEndCallbackRunnable() {
        return mAnimationEndCallbackRunnable;
    }


    /**
     * Retrieves the {@link Transition} state associated with the given panel ID.
     *
     * @param id The ID of the panel.
     */
    @Nullable
    public Transition getPanelTransactionState(@NonNull String id) {
        return mTransactionMap.get(id);
    }

    /** Builder for {@link PanelTransaction}. */
    public static class Builder {
        private final PanelTransaction mPanelTransaction;

        public Builder() {
            mPanelTransaction = new PanelTransaction();
        }

        /**
         * Adds a {@link Transition} for the panel with the specified ID.
         *
         * @param id         The ID of the panel.
         * @param transition The transition to apply to the panel.
         * @return The builder instance.
         */
        @NonNull
        public Builder addPanelTransaction(@NonNull String id, @NonNull Transition transition) {
            mPanelTransaction.addPanelTransaction(id, transition);
            return this;
        }

        /**
         * Adds a {@link Animator} for the panel with the specified ID.
         *
         * @param id       The ID of the panel.
         * @param animator The animator to apply to the panel.
         * @return The builder instance.
         */
        @NonNull
        public Builder addAnimator(@NonNull String id, @Nullable Animator animator) {
            mPanelTransaction.addAnimator(id, animator);
            return this;
        }

        /**
         * Adds a {@link Runnable} to be executed when the animations are starting for this
         * transaction.
         */
        @NonNull
        public Builder setAnimationStartCallbackRunnable(@NonNull Runnable runnable) {
            mPanelTransaction.setAnimationStartCallbackRunnable(runnable);
            return this;
        }

        /**
         * Adds a {@link Runnable} to be executed when the animations have finished for this
         * transaction.
         */
        @NonNull
        public Builder setAnimationEndCallbackRunnable(@NonNull Runnable runnable) {
            mPanelTransaction.setAnimationEndCallbackRunnable(runnable);
            return this;
        }

        /**
         * Builds the {@link PanelTransaction} object.
         *
         * @return The built {@link PanelTransaction} object.
         */
        @NonNull
        public PanelTransaction build() {
            return mPanelTransaction;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ PanelTransaction:");
        for (Map.Entry<String, Transition> entry : mTransactionMap.entrySet()) {
            sb.append(" Transition: ").append(entry.getKey()).append("=").append(
                    entry.getValue()).append(", ");
        }
        for (Map.Entry<String, Animator> entry : mAnimatorMap.entrySet()) {
            sb.append(" Animator: ").append(entry.getKey()).append("=").append(
                    entry.getValue()).append(", ");
        }
        return sb.append("]").toString();
    }
}
