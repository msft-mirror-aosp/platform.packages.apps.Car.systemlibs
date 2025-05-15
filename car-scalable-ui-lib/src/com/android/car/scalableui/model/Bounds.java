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

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the bounds of a UI element. This class provides methods for creating a Bounds object
 * from an XML definition and retrieving the bounds as a {@link Rect}.
 *
 * <p>The Bounds class supports defining dimensions in the following formats:
 *
 * <ul>
 *   <li><b>Absolute pixels:</b> e.g., <code>left="100"</code></li>
 *   <li><b>Density-independent pixels (dp):</b> e.g., <code>top="50dip"</code></li>
 *   <li><b>Percentage of screen width/height:</b> e.g., <code>right="80%"</code></li>
 *   <li><b>Resource references:</b> e.g., <code>bottom="@dimen/my_bottom_margin"</code></li>
 * </ul>
 *
 * <p>It also allows defining either the left and right positions, or the left position and width.
 * Similarly, it allows defining either the top and bottom positions, or the top position and
 * height.
 */
public class Bounds {
    public static final Rect DEFAULT_BOUNDS = new Rect();

    private final int mLeft;
    private final int mTop;
    private final int mRight;
    private final int mBottom;

    /**
     * Constructs a Bounds object. Package-private constructor; use the Builder.
     *
     * @param left   The left position in pixels.
     * @param top    The top position in pixels.
     * @param right  The right position in pixels.
     * @param bottom The bottom position in pixels.
     */
    Bounds(int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    /**
     * Returns the bounds as a {@link Rect} object.
     *
     * @return A Rect object representing the bounds.
     */
    @NonNull
    public Rect getRect() {
        return new Rect(mLeft, mTop, mRight, mBottom);
    }

    /** Builder for {@link Bounds} objects. */
    public static class Builder {
        @Nullable
        private Integer mLeft;
        @Nullable
        private Integer mTop;
        @Nullable
        private Integer mRight;
        @Nullable
        private Integer mBottom;
        @Nullable
        private Integer mWidth;
        @Nullable
        private Integer mHeight;
        @Nullable
        private Integer mLeftOffset;
        @Nullable
        private Integer mTopOffset;
        @Nullable
        private Integer mRightOffset;
        @Nullable
        private Integer mBottomOffset;

        public Builder() {
        }

        /** Sets left */
        public Builder setLeft(@Nullable Integer left) {
            mLeft = left;
            return this;
        }

        /** Sets top */
        public Builder setTop(@Nullable Integer top) {
            mTop = top;
            return this;
        }

        /** Sets right */
        public Builder setRight(@Nullable Integer right) {
            mRight = right;
            return this;
        }

        /** Sets bottom */
        public Builder setBottom(@Nullable Integer bottom) {
            mBottom = bottom;
            return this;
        }

        /** Sets width */
        public Builder setWidth(@Nullable Integer width) {
            mWidth = width;
            return this;
        }

        /** Sets height */
        public Builder setHeight(@Nullable Integer height) {
            mHeight = height;
            return this;
        }

        /** Sets leftOffset */
        public Builder setLeftOffset(@Nullable Integer offset) {
            mLeftOffset = offset;
            return this;
        }

        /** Sets topOffset */
        public Builder setTopOffset(@Nullable Integer offset) {
            mTopOffset = offset;
            return this;
        }

        /** Sets rightOffset */
        public Builder setRightOffset(@Nullable Integer offset) {
            mRightOffset = offset;
            return this;
        }

        /** Sets bottomOffset */
        public Builder setBottomOffset(@Nullable Integer offset) {
            mBottomOffset = offset;
            return this;
        }

        /** Sets rect */
        public Builder setRect(@NonNull Rect rect) {
            mLeft = rect.left;
            mTop = rect.top;
            mRight = rect.right;
            mBottom = rect.bottom;
            return this;
        }

        /** Returns the {@link Bounds} instance */
        @NonNull
        public Bounds build() {
            // Default values and logic to ensure a valid Rect.
            int left = (mLeft != null) ? mLeft : 0;
            int top = (mTop != null) ? mTop : 0;
            int right = (mRight != null) ? mRight : 0;
            int bottom = (mBottom != null) ? mBottom : 0;
            int width = (mWidth != null) ? mWidth : 0;
            int height = (mHeight != null) ? mHeight : 0;

            // Handle width/height combinations, prioritizing explicit left/right/top/bottom
            if (mRight == null && mWidth != null) {
                right = left + width;
            } else if (mLeft == null && mWidth != null) {
                left = right - width;
            }
            if (mBottom == null && mHeight != null) {
                bottom = top + height;
            } else if (mTop == null && mHeight != null) {
                top = bottom - height;
            }

            // Handle offsets
            if (mLeftOffset != null) {
                left += mLeftOffset;
            }
            if (mTopOffset != null) {
                top += mTopOffset;
            }
            if (mRightOffset != null) {
                right -= mRightOffset;
            }
            if (mBottomOffset != null) {
                bottom -= mBottomOffset;
            }

            return new Bounds(left, top, right, bottom);
        }
    }
}
