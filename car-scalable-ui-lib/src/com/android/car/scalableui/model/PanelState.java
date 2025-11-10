/*
 * Copyright (C) 2024 The Android Open Source Project.
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

import static android.view.Display.DEFAULT_DISPLAY;

import android.animation.Animator;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the state of a panel in the Scalable UI system.
 *
 * <p>A PanelState defines the different variants (layouts) that a panel can have, as well as the
 * transitions between those variants. It also manages the current variant and any running
 * animations.
 */
public class PanelState implements Cloneable {
    private static final String TAG = PanelState.class.getSimpleName();

    public static final String DEFAULT_ROLE = "DEFAULT";
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;

    private String mDefaultVariant;
    private int mDisplayId;

    private final String mId;
    @Nullable
    private Role mRole;
    private final List<Variant> mVariants = new ArrayList<>();
    private final List<Transition> mTransitions = new ArrayList<>();
    @Nullable
    private Restart mRestart;
    @Nullable
    private TaskBehavior mTaskBehavior;

    @Nullable
    private Animator mRunningAnimator;
    @Nullable
    private Variant mCurrentVariant;
    @Nullable
    private PanelControllerMetadata mPanelControllerMetadata;
    @PanelType
    private int mType;

    /**
     * Constructor for PanelState.
     *
     * @param id   The ID of the panel.
     * @param type The type of the panel.
     */
    public PanelState(@NonNull String id, @PanelType int type) {
        mId = id;
        mType = type;
        mDisplayId = DEFAULT_DISPLAY;
    }

    /**
     * Constructor to copy a PanelState
     */
    public PanelState(@NonNull PanelState other) {
        mId = other.mId;
        mType = other.mType;
        mRole = other.mRole;
        mDisplayId = other.mDisplayId;
        mDefaultVariant = other.mDefaultVariant;
        mVariants.addAll(other.mVariants);
        mTransitions.addAll(other.mTransitions);
        mRunningAnimator = other.mRunningAnimator;
        mCurrentVariant = other.mCurrentVariant;
    }

    /** Returns id */
    @NonNull
    public String getId() {
        return mId;
    }

    @PanelType
    public int getType() {
        return mType;
    }

    /** Adds variant */
    public void addVariant(@NonNull Variant variant) {
        mVariants.add(variant);
    }

    /** Adds transition */
    public void addTransition(@NonNull Transition transition) {
        mTransitions.add(transition);
    }

    /** Adds restart */
    public void addRestart(@NonNull Restart restart) {
        mRestart = restart;
    }

    /** Returns restart */
    @Nullable
    public Restart getRestart() {
        return mRestart;
    }

    /** Add task behavior */
    public void addTaskBehavior(@NonNull TaskBehavior taskBehavior) {
        mTaskBehavior = taskBehavior;
    }

    /** Returns task behavior */
    @Nullable
    public TaskBehavior getTaskBehavior() {
        return mTaskBehavior;
    }

    /** Returns current variant */
    @Nullable
    public Variant getCurrentVariant() {
        if (mCurrentVariant == null) {
            // Ensure mVariants is not empty before accessing
            if (!mVariants.isEmpty()) {
                mCurrentVariant = mVariants.get(0);
            }
        }
        return mCurrentVariant;
    }

    /** Returns variant with the given id */
    @Nullable
    public Variant getVariant(@NonNull String id) {
        for (Variant variant : mVariants) {
            if (variant.getId().equals(id)) {
                return variant;
            }
        }
        return null;
    }

    /** Returns variant with the given id name */
    @Nullable
    public Variant getVariantByName(@NonNull String name) {
        for (Variant variant : mVariants) {
            if (variant.getIdName().equals(name)) {
                return variant;
            }
        }
        return null;
    }

    /** Sets variant with the given id */
    public void setVariant(@NonNull String id) {
        setVariant(id, null);
    }

    /** Sets role. */
    public void setRole(@Nullable Role role) {
        mRole = role;
    }

    /** Resets to the default variant */
    public void resetVariant() {
        setVariant(mDefaultVariant);
    }

    /**
     * Sets variant
     *
     * @param id    The ID of the variant to set.
     * @param event The event that triggered the variant change.
     */
    public void setVariant(@NonNull String id, @Nullable Event event) {
        for (Variant variant : mVariants) {
            if (variant != null && variant.getId().equals(id)) {
                if (DEBUG) Log.d(TAG, "setVariant,  " + variant);
                mCurrentVariant = variant;
                if (event != null) {
                    mCurrentVariant.updateFromEvent(event);
                }
                return;
            }
        }
    }

    /** Returns the role */
    @Nullable
    public Role getRole() {
        return mRole;
    }

    /** Returns true if animating */
    public boolean isAnimating() {
        return mRunningAnimator != null && mRunningAnimator.isRunning();
    }

    /** Called on animation start */
    public void onAnimationStart(@NonNull Animator animator) {
        if (mRunningAnimator != null) {
            mRunningAnimator.pause();
            mRunningAnimator.removeAllListeners();
        }
        mRunningAnimator = animator;
    }

    /** Called on animation end */
    public void onAnimationEnd() {
        mRunningAnimator = null;
    }

    /** Returns transition for the given event */
    @Nullable
    public Transition getTransition(@Nullable Event event) {
        return getTransition(event, getCurrentVariant());
    }

    /** Returns transition for the given event and from variant values */
    @Nullable
    public Transition getTransition(@Nullable Event event, @Nullable Variant fromVariant) {
        if (event == null) {
            return null;
        }
        // If both onEvent and fromVariant matches
        String currentVariantId = (fromVariant != null) ? fromVariant.getId() : null;
        Transition result = getTransitionInternal(event, currentVariantId);

        if (result != null) {
            return result;
        }
        // If only onEvent matches
        return getTransitionInternal(event);
    }

    @Nullable
    private Transition getTransitionInternal(@NonNull Event event, @Nullable String fromVariant) {
        for (Transition transition : mTransitions) {
            if (transition.isTriggeredBy(event)
                    && transition.getFromVariant() != null
                    && transition.getFromVariant().getId().equals(fromVariant)) {
                return transition;
            }
        }
        return null;
    }

    @Nullable
    private Transition getTransitionInternal(@NonNull Event event) {
        for (Transition transition : mTransitions) {
            if (transition.isTriggeredBy(event) && transition.getFromVariant() == null) {
                return transition;
            }
        }
        return null;
    }

    /**
     * Returns the ID of the display the panel is associated with.
     *
     * @return The display ID.
     */
    public int getDisplayId() {
        return mDisplayId;
    }

    void setDefaultVariant(@Nullable String defaultVariant) {
        mDefaultVariant = defaultVariant;
    }

    void setDisplayId(int displayId) {
        mDisplayId = displayId;
    }

    void setVariants(@NonNull List<Variant> variants) {
        mVariants.clear();
        mVariants.addAll(variants);
    }

    void setTransitions(@NonNull List<Transition> transitions) {
        mTransitions.clear();
        mTransitions.addAll(transitions);
    }

    @Nullable
    public PanelControllerMetadata getPanelControllerMetadata() {
        return mPanelControllerMetadata;
    }

    private void setPanelControllerMetadata(
            @Nullable PanelControllerMetadata panelControllerMetadata) {
        mPanelControllerMetadata = panelControllerMetadata;
    }

    @Override
    @NonNull
    public String toString() {
        return "PanelState{"
                + "mId='" + mId + '\''
                + ", mType=" + mType
                + ", mRole=" + mRole
                + ", mDefaultVariant='" + mDefaultVariant + '\''
                + ", mDisplayId=" + mDisplayId
                + ", mVariants=" + mVariants.stream()
                .map(Variant::toString)
                .collect(Collectors.joining(", ", "[", "]"))
                + ", mTransitions=" + mTransitions.stream()
                .map(Transition::toString)
                .collect(Collectors.joining(", ", "[", "]"))
                + ", mRunningAnimator=" + mRunningAnimator
                + ", mCurrentVariant="
                + (mCurrentVariant != null ? mCurrentVariant.getId() : "null")
                + '}';
    }

    /**
     * Shorter version of {@link #toString()}
     */
    @NonNull
    public String toShortString() {
        return "PanelState{"
                + "\n\tmId='" + mId + "'"
                + "\n\tmType='" + mType + "'"
                + "\n\tmDisplayId=" + mDisplayId
                + "\n\tmRunningAnimator=" + mRunningAnimator
                + "\n\tmCurrentVariant="
                + (mCurrentVariant == null ? "null" : mCurrentVariant.toString().replaceAll("\\R",
                "\n\t"))
                + '}';
    }

    @Override
    public PanelState clone() {
        try {
            // shallow copy is sufficient, mVariants and mTransitions might remain the same.
            return (PanelState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /** Builder for {@link PanelState} objects. */
    public static class Builder {
        private String mId;
        @PanelType
        private int mType;
        private Role mRole;
        private String mDefaultVariant;
        private Integer mDisplayId;
        private List<Variant> mVariants = new ArrayList<>();
        private List<Transition> mTransitions = new ArrayList<>();
        private PanelControllerMetadata mPanelControllerMetadata;

        public Builder(@NonNull String id, @PanelType int type) {
            mId = id;
            mType = type;
        }

        /** Sets role */
        public Builder setRole(@Nullable Role role) {
            mRole = role;
            return this;
        }

        /** Sets default variant */
        public Builder setDefaultVariant(@Nullable String defaultVariant) {
            mDefaultVariant = defaultVariant;
            return this;
        }

        /** Sets display id */
        public Builder setDisplayId(int displayId) {
            mDisplayId = displayId;
            return this;
        }

        /** Adds a variant */
        public Builder addVariant(@NonNull Variant variant) {
            mVariants.add(variant);
            return this;
        }

        /** Adds a transitions */
        public Builder addTransition(@NonNull Transition transition) {
            mTransitions.add(transition);
            return this;
        }

        /** Sets variants */
        public Builder setVariants(@NonNull List<Variant> variants) {
            mVariants = new ArrayList<>(variants); // Defensive copy
            return this;
        }

        /** Sets transitions */
        public Builder setTransitions(@NonNull List<Transition> transitions) {
            mTransitions = new ArrayList<>(transitions); // Defensive copy
            return this;
        }

        public void setPanelControllerMetadata(PanelControllerMetadata panelControllerMetaData) {
            mPanelControllerMetadata = panelControllerMetaData;
        }

        /** Returns the {@link PanelState} instance */
        @NonNull
        public PanelState build() {
            PanelState panelState = new PanelState(mId, mType);
            if (mRole != null) {
                panelState.setRole(mRole);
            }
            panelState.setDefaultVariant(mDefaultVariant);
            if (mDisplayId != null) {
                panelState.setDisplayId(mDisplayId);
            }
            panelState.setVariants(mVariants);
            panelState.setTransitions(mTransitions);
            panelState.setPanelControllerMetadata(mPanelControllerMetadata);
            return panelState;
        }
    }
}
