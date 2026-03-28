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

import static com.android.car.scalableui.loader.xml.parser.PanelControllerParser.CONTROLLER_NAME_TAG;
import static com.android.car.scalableui.loader.xml.parser.PanelControllerParser.TASK_TOOLBAR_CONTROLLER_TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents immutable metadata associated with a panel controller.
 *
 * <p>This class acts as a data holder for configuration details, breakpoints, and identification
 * for a specific controller. It is designed to be immutable after construction, ensuring that its
 * state cannot change once created.
 *
 * <p>Instances are typically created using the {@link Builder} pattern via the static factory
 * method {@link #builder(String)}.
 */
public final class PanelControllerMetadata {
    public static final String TAG = PanelControllerMetadata.class.getSimpleName();

    @NonNull private final Bundle mConfigurations;
    @NonNull private final List<BreakPoint> mBreakPoints;
    @NonNull private final String mId;

    public PanelControllerMetadata(String id, List<BreakPoint> breakPoints, Bundle bundle) {
        mId = id;
        mBreakPoints = new ArrayList<>(breakPoints);
        mConfigurations = new Bundle(bundle);
    }

    public PanelControllerMetadata(Bundle bundle) {
        this(/* id= */ "", new ArrayList<>(), bundle);
    }

    /**
     * Retrieves a String configuration value associated with the specified key or null if no
     * mapping.
     */
    @Nullable
    public String getStringConfiguration(@NonNull String key) {
        return mConfigurations.getString(key);
    }

    /**
     * Retrieves a list configuration value associated with the specified key or null if no mapping.
     */
    @Nullable
    public List<String> getListConfiguration(@NonNull String key) {
        Object value = mConfigurations.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        } else if (value instanceof String) {
            ArrayList<String> list = new ArrayList<>();
            list.add((String) value);
            return list;
        }
        return null;
    }

    /** Returns the bundle containing all configurations. */
    @NonNull
    public Bundle getConfigurations() {
        return mConfigurations;
    }

    /**
     * Checks if a configuration with the given name exists.
     *
     * @param configName The name of the configuration to check for.
     * @return {@code true} if the configuration exists, {@code false} otherwise.
     */
    public boolean hasConfiguration(String configName) {
        return mConfigurations.containsKey(configName);
    }

    /** Gets the list of breakpoints associated with this controller metadata. */
    @NonNull
    public List<BreakPoint> getBreakPoints() {
        return mBreakPoints;
    }

    /** Gets the unique identifier for this metadata instance. */
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
        sb.append(", mConfigurations=").append(mConfigurations);
        sb.append(", mBreakPoints=");
        mBreakPoints.forEach(breakPoint -> sb.append(breakPoint));

        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelControllerMetadata that = (PanelControllerMetadata) o;
        return Objects.equals(getConfigurations(), that.getConfigurations())
                && Objects.equals(getBreakPoints(), that.getBreakPoints())
                && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConfigurations(), getBreakPoints(), getId());
    }

    @Nullable
    public String getControllerName() {
        return getStringConfiguration(CONTROLLER_NAME_TAG);
    }

    @Nullable
    public String getTaskToolBarControllerName() {
        return getStringConfiguration(TASK_TOOLBAR_CONTROLLER_TAG);
    }

    /** Returns a {@link PanelControllerMetadata.Builder} objects. */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Builder for {@link PanelControllerMetadata} objects. */
    public static class Builder {
        private final String mId;
        protected Bundle mConfigurations = new Bundle();
        private List<BreakPoint> mBreakPoints = new ArrayList<>();

        private Builder(String id) {
            mId = id;
        }

        /**
         * Adds or replaces a key-value pair in the configuration map for the metadata.
         *
         * @param key The non-null configuration key.
         * @param value The non-null configuration value.
         * @return This {@link Builder} instance for fluent chaining.
         */
        public Builder addConfiguration(@NonNull String key, @NonNull String value) {
            if (mConfigurations.containsKey(key)) {
                Object existingValue = mConfigurations.get(key);
                ArrayList<String> list;
                if (existingValue instanceof ArrayList) {
                    list = (ArrayList<String>) existingValue;
                } else {
                    list = new ArrayList<>();
                    if (existingValue instanceof String) {
                        list.add((String) existingValue);
                    }
                }
                list.add(value);
                mConfigurations.putStringArrayList(key, list);
            } else {
                mConfigurations.putString(key, value);
            }
            return this;
        }

        /**
         * Sets the list of {@link BreakPoint} for the metadata being built.
         *
         * <p>This method <b>replaces</b> any previously added breakpoints. It clears the current
         * internal list and adds all elements from the provided list. A defensive copy of the
         * provided list's elements is made.
         *
         * @param breakPoints The list of {@link BreakPoint} objects to set. If null, the internal
         *     list will be cleared. (Consider disallowing null?)
         * @return This {@link Builder} instance for fluent chaining.
         */
        public Builder addBreakPoints(List<BreakPoint> breakPoints) {
            mBreakPoints.clear();
            mBreakPoints.addAll(breakPoints);
            return this;
        }

        /** Returns the {@link PanelControllerMetadata} instance */
        public PanelControllerMetadata build() {
            if (!mConfigurations.containsKey(CONTROLLER_NAME_TAG)) {
                Log.e(TAG, "Controller name cannot be empty");
            }
            if (mBreakPoints != null) {
                mBreakPoints.sort(Comparator.comparing(BreakPoint::getPoint));
            }
            return new PanelControllerMetadata(mId, mBreakPoints, mConfigurations);
        }
    }
}
