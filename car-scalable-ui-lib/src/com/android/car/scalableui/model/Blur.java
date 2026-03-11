/*
 * Copyright (C) 2025 The Android Open Source Project.
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

/**
 * Represents configuration for a blur effect. Instances are immutable and created using the {@link
 * Builder}.
 */
public final class Blur {

    private final float mCornerRadius;
    private final int mBlurRadius;
    private final int mBackgroundColor;
    private final boolean mEnableVail;

    private Blur(Builder builder) {
        this.mCornerRadius = builder.mCornerRadius;
        this.mBlurRadius = builder.mBlurRadius;
        this.mBackgroundColor = builder.mBackgroundColor;
        this.mEnableVail = builder.mEnableVail;
    }

    /**
     * Gets the corner radius for the blur area.
     *
     * @return The corner radius.
     */
    public float getCornerRadius() {
        return mCornerRadius;
    }

    /**
     * Gets the blur radius intensity.
     *
     * @return The blur radius.
     */
    public int getBlurRadius() {
        return mBlurRadius;
    }

    /**
     * Gets the background color to apply behind the blur effect. Often represented as an ARGB
     * integer (e.g., 0xAARRGGBB).
     *
     * @return The background color as an integer.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public boolean isVailEnabled() {
        return mEnableVail;
    }

    /**
     * Creates a new builder instance.
     *
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder class for constructing {@link Blur} instances. */
    public static class Builder {
        private float mCornerRadius = 0.0f;
        private int mBlurRadius = 0;
        private int mBackgroundColor = 0x00000000; // Default value (transparent)
        private boolean mEnableVail;

        public Builder() {}

        /**
         * Sets the corner radius for the blur area.
         *
         * @param cornerRadius The desired corner radius.
         * @return This builder instance for chaining.
         */
        public Builder setCornerRadius(float cornerRadius) {
            this.mCornerRadius = cornerRadius;
            return this;
        }

        /**
         * Sets the blur radius intensity.
         *
         * @param blurRadius The desired blur radius.
         * @return This builder instance for chaining.
         */
        public Builder setBlurRadius(int blurRadius) {
            this.mBlurRadius = blurRadius;
            return this;
        }

        /**
         * Sets the background color behind the blur effect.
         *
         * @param backgroundColor The desired background color (e.g., ARGB int).
         * @return This builder instance for chaining.
         */
        public Builder setBackgroundColor(int backgroundColor) {
            this.mBackgroundColor = backgroundColor;
            return this;
        }

        /**
         * If enabled the overlay vioew would add a vail for the task that is being overlaid.
         *
         * @param enableVail true to add a vail
         * @return This builder instance for chaining.
         */
        public Builder setEnableVail(boolean enableVail) {
            this.mEnableVail = enableVail;
            return this;
        }

        /**
         * Builds and returns an immutable {@link Blur} instance with the configured properties.
         *
         * @return A new {@link Blur} instance.
         */
        public Blur build() {
            return new Blur(this);
        }
    }

    @Override
    public String toString() {
        return "Blur{"
                + "cornerRadius="
                + mCornerRadius
                + ", blurRadius="
                + mBlurRadius
                + ", backgroundColor="
                + String.format("0x%08X", mBackgroundColor)
                + '}';
    }
}
