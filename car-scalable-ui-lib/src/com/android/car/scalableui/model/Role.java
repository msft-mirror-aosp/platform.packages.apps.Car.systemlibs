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

import android.content.ComponentName;
import android.content.Context;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

/**
 * Represents the role of a {@code Panel} within the system.
 *
 * <p>This class encapsulates an integer value that signifies the role of a UI element. The specific
 * meaning of the role value is determined by the system using it.
 */
public class Role {
    public static Role DEFAULT_ROLE = new Builder().setLayoutId(-1).build();
    @LayoutRes private final int mLayoutId;
    private final boolean mIsDefault;
    @Nullable private final ComponentName[] mPersistedActivities;

    private Role(@LayoutRes int layoutId) {
        mLayoutId = layoutId;
        mIsDefault = false;
        mPersistedActivities = null;
    }

    private Role(boolean isDefault, @Nullable ComponentName[] persistedActivities) {
        mLayoutId = 0;
        mIsDefault = isDefault;
        mPersistedActivities = persistedActivities;
    }

    /** Returns if the role is the default role */
    public boolean isDefault() {
        return mIsDefault;
    }

    /** Inflates the view set in role attribute using the given context */
    @Nullable
    public View getView(@NonNull Context context) {
        if (mLayoutId == 0) {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(mLayoutId, null);
    }

    /** Returns the persisted activities associated with this role. */
    @Nullable
    public ComponentName[] getPersistedActivities() {
        return mPersistedActivities;
    }

    /** Builder for {@link Role} objects. */
    public static class Builder {
        @LayoutRes private int mLayoutId = 0;
        protected boolean mIsDefault = false;
        @NonNull protected ArraySet<ComponentName> mPersistedActivities = new ArraySet<>();

        public Builder() {}

        /** Sets isDefault value */
        public Builder setIsDefault(boolean isDefault) {
            mIsDefault = false;
            mPersistedActivities.clear();

            mIsDefault = isDefault;
            return this;
        }

        /** Adds a PersistentActivity */
        public Builder addPersistentActivity(@NonNull String activityComponent) {
            ComponentName componentName = ComponentName.unflattenFromString(activityComponent);
            return addPersistentActivity(componentName);
        }

        /** Adds a PersistentActivity */
        public Builder addPersistentActivity(@NonNull ComponentName componentName) {
            mLayoutId = 0;
            mIsDefault = false;
            mPersistedActivities.add(componentName);
            return this;
        }

        /** Add PersistentActivities */
        public Builder addPersistentActivities(@NonNull Collection<ComponentName> componentNames) {
            mLayoutId = 0;
            mIsDefault = false;
            mPersistedActivities.addAll(componentNames);
            return this;
        }

        /** Sets layout id to be inflated at runtime */
        public Builder setLayoutId(@LayoutRes int layoutId) {
            mIsDefault = false;
            mPersistedActivities.clear();

            mLayoutId = layoutId;
            return this;
        }

        /** Returns the {@link Role} instance */
        @NonNull
        public Role build() {
            if (mLayoutId == 0 && !mIsDefault && mPersistedActivities.isEmpty()) {
                return DEFAULT_ROLE;
            }

            if (mLayoutId != 0) {
                return new Role(mLayoutId);
            } else {
                return new Role(mIsDefault, mPersistedActivities.toArray(ComponentName[]::new));
            }
        }
    }
}
