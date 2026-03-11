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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the visibility of a Panel in the Scalable UI system.
 *
 * <p>This class encapsulates a boolean value indicating whether a panel is visible or not. It can
 * be created from an XML definition or directly using a boolean value.
 */
public class Visibility {
    public static final boolean DEFAULT_VISIBILITY = true;

    private final boolean mIsVisible;

    /**
     * Constructor for Visibility. Package-private; use the Builder.
     *
     * @param isVisible Whether the element is visible.
     */
    Visibility(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    public Visibility(Visibility original) {
        this.mIsVisible = original.isVisible();
    }

    /**
     * Returns whether the element is visible.
     *
     * @return True if the element is visible, false otherwise.
     */
    public boolean isVisible() {
        return mIsVisible;
    }

    /** Builder for {@link Visibility} objects. */
    public static class Builder {
        @Nullable private Boolean mIsVisible; // Use boxed type

        public Builder() {}

        /** Set visibility */
        public Builder setIsVisible(boolean isVisible) {
            mIsVisible = isVisible;
            return this;
        }

        /** Returns the {@link Visibility} instance */
        @NonNull
        public Visibility build() {
            // Use default if not explicitly set
            boolean visible = (mIsVisible != null) ? mIsVisible : DEFAULT_VISIBILITY;
            return new Visibility(visible);
        }
    }
}
