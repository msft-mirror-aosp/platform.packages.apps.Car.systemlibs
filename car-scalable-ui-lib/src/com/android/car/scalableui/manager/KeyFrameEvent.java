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
package com.android.car.scalableui.manager;

/**
 * Describes a KeyframeEvent in the system. This is the same as a standard {@link Event} but
 * includes a fraction value that represents the keyframe event progress.
 */
public class KeyFrameEvent extends Event {
    private final float mFraction;

    /**
     * Constructs a KeyframeEvent.
     *
     * @param id       A unique identifier associated with this event.
     * @param fraction A fraction value (between 0 and 1).
     */
    public KeyFrameEvent(String id, float fraction) {
        super(id);
        mFraction = fraction;
    }

    /**
     * Returns the fraction associated with this event.
     *
     * @return The fraction progress value of this event (between 0 and 1).
     */
    public float getFraction() {
        return mFraction;
    }
}
