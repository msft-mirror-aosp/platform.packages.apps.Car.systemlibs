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

/**
 * Represents the role of a {@code Panel} within the system.
 *
 * <p>This class encapsulates an integer value that signifies the role of a UI element.
 * The specific meaning of the role value is determined by the system using it.
 */
public class Role {
    private final int mValue;

    /**
     * Constructor for Role.
     *
     * @param value The integer value representing the role.
     */
    public Role(int value) {
        mValue = value;
    }

    /**
     * Returns the integer value representing the role.
     *
     * @return The integer value of the role.
     */
    public int getValue() {
        return mValue;
    }
}
