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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Represents a set of transactions to be applied to panels. */
public class PanelTransaction {

    /** A map of panel IDs to panel {@link Transition}s. */
    private final HashMap<String, Transition> mTransactionMap;

    /** A map of panel IDs to panel {@link Animator}s. */
    private final HashMap<String, Animator> mAnimatorMap;
    private final HashSet<String> mUnchangedPanelIdSet;
    private boolean mHasWindowChanges;

    private Runnable mAnimationStartCallbackRunnable;
    private Runnable mAnimationEndCallbackRunnable;

    public PanelTransaction(Map<String, Transition> transactionMap,
            Map<String, Animator> animatorMap, Set<String> unchangedPanelIdSet) {
        mTransactionMap = new HashMap<>(transactionMap);
        mAnimatorMap = new HashMap<>(animatorMap);
        mUnchangedPanelIdSet = new HashSet<>(unchangedPanelIdSet);
    }

    /** Returns a set of entries representing the transactions in this object. */
    @NonNull
    public Set<Map.Entry<String, Transition>> getPanelTransactionStates() {
        return mTransactionMap.entrySet();
    }

    /** Returns a set of entries representing the Animation for given panel. */
    @NonNull
    public Set<Map.Entry<String, Animator>> getAnimators() {
        return mAnimatorMap.entrySet();
    }

    @NonNull
    public Set<String> getUnchangedPanelIdSet() {
        return mUnchangedPanelIdSet;
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

    /**
     * Return if this Panel transaction contains any window change.
     */
    public boolean hasWindowChanges() {
        return mHasWindowChanges;
    }

    private void setHasWindowChanges(boolean hasWindowChanges) {
        mHasWindowChanges = hasWindowChanges;
    }

    /** Builder for {@link PanelTransaction}. */
    public static class Builder {
        private final HashMap<String, Transition> mTransactionMap;
        private final HashMap<String, Animator> mAnimatorMap;
        private Runnable mAnimationStartCallbackRunnable;
        private Runnable mAnimationEndCallbackRunnable;
        private boolean mHasWindowChanges = true;
        private Set<String> mUnchangedPanelIdSet;

        public Builder() {
            mTransactionMap = new HashMap<>();
            mAnimatorMap = new HashMap<>();
            mUnchangedPanelIdSet = new HashSet<>();
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
            mTransactionMap.put(id, transition);
            return this;
        }

        /**
         * Sets a flag indicating whether the object under construction has window changes.
         */
        @NonNull
        public Builder setHasWindowChanges(boolean hasWindowChanges) {
            mHasWindowChanges = hasWindowChanges;
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
            mAnimatorMap.put(id, animator);
            return this;
        }

        /**
         * Adds a {@link Runnable} to be executed when the animations are starting for this
         * transaction.
         */
        @NonNull
        public Builder setAnimationStartCallbackRunnable(@NonNull Runnable runnable) {
            mAnimationStartCallbackRunnable = runnable;
            return this;
        }

        /**
         * Adds a {@link Runnable} to be executed when the animations have finished for this
         * transaction.
         */
        @NonNull
        public Builder setAnimationEndCallbackRunnable(@NonNull Runnable runnable) {
            mAnimationEndCallbackRunnable = runnable;
            return this;
        }

        /**
         * Adds the ID of a panel that should remain unchanged during this transaction.
         */
        @NonNull
        public Builder addUnchangedPanelId(@NonNull String id) {
            mUnchangedPanelIdSet.add(id);
            return this;
        }

        /**
         * Builds the {@link PanelTransaction} object.
         *
         * @return The built {@link PanelTransaction} object.
         */
        @NonNull
        public PanelTransaction build() {
            PanelTransaction panelTransaction = new PanelTransaction(mTransactionMap, mAnimatorMap,
                    mUnchangedPanelIdSet);
            panelTransaction.setHasWindowChanges(mHasWindowChanges);
            if (mAnimationStartCallbackRunnable != null) {
                panelTransaction.setAnimationStartCallbackRunnable(mAnimationStartCallbackRunnable);
            }
            if (mAnimationEndCallbackRunnable != null) {
                panelTransaction.setAnimationEndCallbackRunnable(mAnimationEndCallbackRunnable);
            }
            return panelTransaction;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[PanelTransaction:");

        if (!mTransactionMap.isEmpty()) {
            sb.append(" Transitions={");
            sb.append(" HasWindowChange= ").append(hasWindowChanges()).append(", ");
            boolean firstTransition = true;
            for (Map.Entry<String, Transition> entry : mTransactionMap.entrySet()) {
                if (!firstTransition) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                firstTransition = false;
            }
            sb.append("}");
        }

        if (!mAnimatorMap.isEmpty()) {
            if (!mTransactionMap.isEmpty()) {
                sb.append(", ");
            }
            sb.append(" Animators={");
            boolean firstAnimator = true;
            for (Map.Entry<String, Animator> entry : mAnimatorMap.entrySet()) {
                if (!firstAnimator) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                firstAnimator = false;
            }
            sb.append("}");
        }

        sb.append("]");
        return sb.toString();
    }
}
