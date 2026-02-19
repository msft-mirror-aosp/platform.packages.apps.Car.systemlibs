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
 * Represents a specific point (e.g., a position, value, or time) associated with an event
 * identifier.
 *
 * <p>This class is immutable; its state cannot be changed after creation. Instances are typically
 * created using the {@link Builder}.
 */
public class BreakPoint {

    private final int mPoint;
    private final String mEventId;

    /** Gets the event Id. */
    public String getEventId() {
        return mEventId;
    }

    /** Gets the point. */
    public int getPoint() {
        return mPoint;
    }

    BreakPoint(int point, String eventId) {
        mPoint = point;
        mEventId = eventId;
    }

    /** Builder for {@link BreakPoint} objects. */
    public static class Builder {
        private final int mPoint;
        private final String mEventId;

        public Builder(Integer point, @NonNull String eventId) {
            if (point == null) {
                throw new RuntimeException("point should not be null");
            }
            mPoint = point;
            mEventId = eventId;
        }

        /** Returns the {@link BreakPoint} instance */
        @NonNull
        public BreakPoint build() {
            // Use the default if alpha wasn't set explicitly
            return new BreakPoint(mPoint, mEventId);
        }
    }

    @Override
    public String toString() {
        return "BreakPoint{" + "mPoint=" + mPoint + ", mEventId='" + mEventId + '}';
    }
}
