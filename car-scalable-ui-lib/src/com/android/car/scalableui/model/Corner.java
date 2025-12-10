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

/**
 * Represents the corner of a {@code Panel}.
 *
 * <p>A {@code Corner} object holds the radius for each of the four corners: top-left, top-right,
 * bottom-left, and bottom-right. This allows for creating panels with rounded corners, where each
 * corner can have a different radius.
 *
 * <p>The radius can be defined in absolute pixels or as a resource reference.
 */
public class Corner {
    private static final int DEFAULT_RADIUS = 0;
    public static final Corner DEFAULT_CORNER = new Builder().build();

    private final int mRadiusTL;
    private final int mRadiusTR;
    private final int mRadiusBL;
    private final int mRadiusBR;

    Corner(int radiusTL, int radiusTR, int radiusBL, int radiusBR) {
        mRadiusTL = radiusTL;
        mRadiusTR = radiusTR;
        mRadiusBL = radiusBL;
        mRadiusBR = radiusBR;
    }

    /** Returns the top-left corner radius. */
    public int getTopLeftRadius() {
        return mRadiusTL;
    }

    /** Returns the top-right corner radius. */
    public int getTopRightRadius() {
        return mRadiusTR;
    }

    /** Returns the bottom-left corner radius. */
    public int getBottomLeftRadius() {
        return mRadiusBL;
    }

    /** Returns the bottom-right corner radius. */
    public int getBottomRightRadius() {
        return mRadiusBR;
    }

    @Override
    public String toString() {
        return "Corner{"
                + "mRadiusTL=" + mRadiusTL
                + ", mRadiusTR=" + mRadiusTR
                + ", mRadiusBL=" + mRadiusBL
                + ", mRadiusBR=" + mRadiusBR
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Corner other)) {
            return false;
        }
        return mRadiusTL == other.mRadiusTL
                && mRadiusTR == other.mRadiusTR
                && mRadiusBL == other.mRadiusBL
                && mRadiusBR == other.mRadiusBR;
    }

    @Override
    public int hashCode() {
        // Start with the first field
        int result = mRadiusTL;
        // Multiply by 31 (a standard prime) and add the next field
        result = 31 * result + mRadiusTR;
        result = 31 * result + mRadiusBL;
        result = 31 * result + mRadiusBR;
        return result;
    }

    /** Builder for {@link Corner} objects. */
    public static class Builder {
        private int mRadiusTL = DEFAULT_RADIUS;
        private int mRadiusTR = DEFAULT_RADIUS;
        private int mRadiusBL = DEFAULT_RADIUS;
        private int mRadiusBR = DEFAULT_RADIUS;

        public Builder() {
        }

        /** Sets the radius for all corners. */
        public Builder setRadius(Integer radius) {
            if (radius == null) {
                return this;
            }
            mRadiusTL = radius;
            mRadiusTR = radius;
            mRadiusBL = radius;
            mRadiusBR = radius;
            return this;
        }

        /** Sets the top-left corner radius. */
        public Builder setTopLeftRadius(Integer radiusTL) {
            if (radiusTL == null) {
                return this;
            }
            mRadiusTL = radiusTL;
            return this;
        }

        /** Sets the top-right corner radius. */
        public Builder setTopRightRadius(Integer radiusTR) {
            if (radiusTR == null) {
                return this;
            }
            mRadiusTR = radiusTR;
            return this;
        }

        /** Sets the bottom-left corner radius. */
        public Builder setBottomLeftRadius(Integer radiusBL) {
            if (radiusBL == null) {
                return this;
            }
            mRadiusBL = radiusBL;
            return this;
        }

        /** Sets the bottom-right corner radius. */
        public Builder setBottomRightRadius(Integer radiusBR) {
            if (radiusBR == null) {
                return this;
            }
            mRadiusBR = radiusBR;
            return this;
        }

        /** Returns the {@link Corner} instance. */
        @NonNull
        public Corner build() {
            return new Corner(mRadiusTL, mRadiusTR, mRadiusBL, mRadiusBR);
        }
    }
}
