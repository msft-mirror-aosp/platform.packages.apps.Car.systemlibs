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
 * Represents the alpha (transparency) value of a UI element. This class provides methods for
 * creating an Alpha object from an XML definition and retrieving the alpha value.
 */
public class Alpha {
    public static final float DEFAULT_ALPHA = 1;

    private final float mAlpha;

    /**
     * Constructs an Alpha object with the specified alpha value. Package-private constructor; use
     * the Builder.
     *
     * @param alpha The alpha value, between 0 (fully transparent) and 1 (fully opaque).
     */
    Alpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * Returns the alpha value.
     *
     * @return The alpha value.
     */
    public float getAlpha() {
        return mAlpha;
    }

    /** Builder for {@link Alpha} objects. */
    public static class Builder {
        @Nullable private Float mAlpha;

        public Builder() {}

        /** Sets alpha */
        public Builder setAlpha(float alpha) {
            if (alpha > 1) {
                alpha = 1f;
            } else if (alpha < 0) {
                alpha = 0f;
            } else {
                mAlpha = alpha;
            }
            return this;
        }

        /** Returns the {@link Alpha} instance */
        @NonNull
        public Alpha build() {
            // Use the default if alpha wasn't set explicitly
            float alphaValue = (mAlpha != null) ? mAlpha : DEFAULT_ALPHA;
            return new Alpha(alphaValue);
        }
    }
}
