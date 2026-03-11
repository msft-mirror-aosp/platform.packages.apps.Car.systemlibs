/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.car.scalableui.loader.xml.parser;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Base implementation of {@link ValueParser} that only parses literal values. Does NOT support
 * resource references (e.g., @string/foo).
 */
public class BaseValueParser implements ValueParser {

    @Override
    public boolean parseBoolean(@NonNull Context context, @NonNull String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }

    @Override
    public boolean parseBoolean(
            @NonNull Context context, @Nullable String value, boolean defValue) {
        if (value == null) {
            return defValue;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return defValue;
    }

    @Override
    public int parseColor(@NonNull Context context, @NonNull String value) {
        try {
            return Color.parseColor(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid color value: " + value, e);
        }
    }

    @Override
    public String parseString(@NonNull Context context, @NonNull String value) {
        return value;
    }

    @Override
    public List<String> parseStringArray(@NonNull Context context, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("\\s*,\\s*"));
    }

    @Override
    public int parseInteger(@NonNull Context context, @Nullable String value, int defValue) {
        if (value == null || value.isEmpty()) {
            return defValue;
        }
        try {
            return Integer.decode(value.trim());
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    @Override
    public float parseFloat(@NonNull Context context, @Nullable String value, float defValue) {
        if (value == null) {
            return defValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    @Override
    public int parseDimensionPixelSize(
            @NonNull Context context, @Nullable String value, int defValue) {
        if (value == null) {
            return defValue;
        }

        float density = context.getResources().getDisplayMetrics().density;
        String lower = value.toLowerCase(Locale.ROOT);
        try {
            if (lower.endsWith("px")) {
                return (int) Float.parseFloat(value.substring(0, value.length() - 2));
            } else if (lower.endsWith("dp") || lower.endsWith("dip")) {
                int index = lower.lastIndexOf('d');
                float dp = Float.parseFloat(value.substring(0, index));
                return (int) (dp * density + 0.5f);
            } else if (lower.endsWith("sp")) {
                float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
                float sp = Float.parseFloat(value.substring(0, value.length() - 2));
                return (int) (sp * scaledDensity + 0.5f);
            } else {
                return (int) Float.parseFloat(value);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return defValue;
        }
    }

    @Override
    public int parseResourceId(@NonNull Context context, @Nullable String value, int defValue) {
        return defValue;
    }

    @Nullable
    @Override
    public Drawable parseDrawable(@NonNull Context context, @Nullable String value) {
        return null;
    }

    @Nullable
    @Override
    public Interpolator parseInterpolator(@NonNull Context context, @Nullable String value) {
        return null;
    }

    @Nullable
    @Override
    public Animator parseAnimator(@NonNull Context context, @Nullable String value) {
        return null;
    }
}
