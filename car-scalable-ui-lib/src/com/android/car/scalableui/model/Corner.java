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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the corner of a {@code Panel}. This class provides methods for creating a Corner
 * object from an XML definition and retrieving the radius value.
 *
 * <p>The Corner class supports defining dimensions in the following formats:
 * <ul>
 *     <li><b>Absolute pixels:</b> e.g., <code>left="100"</code></li>
 *     <li><b>Density-independent pixels (dp):</b> e.g., <code>top="50dip"</code></li>
 *     <li><b>Resource references:</b> e.g., <code>bottom="@dimen/my_bottom_margin"</code></li>
 * </ul>
 */
public class Corner {
    static final int DEFAULT_RADIUS = 0;

    private final int mRadius;

    /**
     * Constructs a Corner object with the specified radius value.
     *
     * @param radius The radius value. 0 indicates a sharp corner.
     */
    Corner(int radius) {
        mRadius = radius;
    }

    /**
     * Returns the radius value.
     *
     * @return The Corner's radius value.
     */
    public int getRadius() {
        return mRadius;
    }

    /** Builder for {@link Corner} objects. */
    public static class Builder {
        @Nullable private Integer mRadius;

        public Builder() {}

        /** Sets layer */
        public Builder setRadius(int radius) {
            mRadius = radius;
            return this;
        }

        /** Returns the {@link Corner} instance */
        @NonNull
        public Corner build() {
            // Use default if not explicitly set
            int cornerValue = (mRadius != null) ? mRadius : DEFAULT_RADIUS;
            return new Corner(cornerValue);
        }
    }
}
