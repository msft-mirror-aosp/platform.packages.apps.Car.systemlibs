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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents immutable metadata associated with a panel controller.
 * <p>
 * This class acts as a data holder for configuration details, breakpoints, and
 * identification for a specific controller. It is designed to be immutable after
 * construction, ensuring that its state cannot change once created.
 * <p>
 * Instances are typically created using the {@link Builder} pattern via the
 * static factory method {@link #builder(String)}.
 */
public final class PanelControllerMetadata {
    public static final String TAG = PanelControllerMetadata.class.getSimpleName();

    public static final String CONTROLLER_NAME_TAG = "ControllerName";
    public static final String VIEW_TAG = "View";
    public static final String EVENT_ID_TAG = "EventId";
    public static final String ORIENTATION_TAG = "Orientation";
    public static final String SNAPTHREADHOLD_TAG = "SnapThreadhold";

    @NonNull
    private Map<String, String> mConfigurations;
    @NonNull
    private List<BreakPoint> mBreakPoints;
    @NonNull
    private final String mId;

    public PanelControllerMetadata(String id, List<BreakPoint> breakPoints,
            Map<String, String> configuration) {
        mId = id;
        mBreakPoints = new ArrayList<>(breakPoints);
        mConfigurations = new HashMap<>(configuration);
    }

    /**
     * Retrieves a configuration value associated with the specified key.
     */
    @Nullable
    public String getConfiguration(@NonNull String key) {
        return mConfigurations.getOrDefault(key, null);
    }

    /**
     * Gets the list of breakpoints associated with this controller metadata.
     */
    @NonNull
    public List<BreakPoint> getBreakPoints() {
        return mBreakPoints;
    }

    /**
     * Gets the unique identifier for this metadata instance.
     */
    @NonNull
    public String getId() {
        return mId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append("[");
        sb.append("mId='").append(mId).append('\'');
        sb.append(", mConfigurations=");
        mConfigurations.forEach((key, value) ->
                sb.append(key).append("=").append(value).append(";"));

        sb.append(", mBreakPoints=");
        mBreakPoints.forEach(breakPoint -> sb.append(breakPoint));

        sb.append("]");
        return sb.toString();
    }

    @NonNull
    public String getControllerName() {
        return getConfiguration(CONTROLLER_NAME_TAG);
    }

    /** Returns a {@link PanelControllerMetadata.Builder} objects. */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Builder for {@link PanelControllerMetadata} objects. */
    public static class Builder {
        private final String mId;
        protected Map<String, String> mConfigurations = new HashMap<>();
        private List<BreakPoint> mBreakPoints = new ArrayList<>();

        private Builder(String id) {
            mId = id;
        }

        /**
         * Adds or replaces a key-value pair in the configuration map for the metadata.
         *
         * @param key   The non-null configuration key.
         * @param value The non-null configuration value.
         * @return This {@link Builder} instance for fluent chaining.
         */
        public Builder addConfiguration(@NonNull String key, @NonNull String value) {
            mConfigurations.put(key, value);
            return this;
        }

        /**
         * Sets the list of {@link BreakPoint} for the metadata being built.
         * <p>
         * This method <b>replaces</b> any previously added breakpoints. It clears the
         * current internal list and adds all elements from the provided list. A defensive
         * copy of the provided list's elements is made.
         *
         * @param breakPoints The list of {@link BreakPoint} objects to set. If null,
         * the internal list will be cleared. (Consider disallowing null?)
         * @return This {@link Builder} instance for fluent chaining.
         */
        public Builder addBreakPoints(List<BreakPoint> breakPoints) {
            mBreakPoints.clear();
            mBreakPoints.addAll(breakPoints);
            return this;
        }

        /** Returns the {@link PanelControllerMetadata} instance */
        public PanelControllerMetadata build() {
            if (!mConfigurations.containsKey(CONTROLLER_NAME_TAG)
                    || !mConfigurations.containsKey(VIEW_TAG)) {
                Log.e(TAG, "Controller name or view name cannot be empty"
                        + " controller = " + mConfigurations.getOrDefault(CONTROLLER_NAME_TAG, null)
                        + " view = " + mConfigurations.getOrDefault(VIEW_TAG, null));
            }
            if (mBreakPoints != null) {
                mBreakPoints.sort(Comparator.comparing(
                        BreakPoint::getPoint));

            }
            return new PanelControllerMetadata(mId, mBreakPoints, mConfigurations);
        }
    }
}
