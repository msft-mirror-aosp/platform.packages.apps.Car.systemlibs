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
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

/**
 * Interface for parsing values (references, colors, booleans, etc.) from XML attributes. Abstracts
 * the difference between standard Android resource parsing and strict external parsing.
 */
public interface ValueParser {

    /**
     * Parses a boolean value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @return The parsed boolean.
     */
    boolean parseBoolean(@NonNull Context context, @NonNull String value);

    /**
     * Parses a boolean value with a default.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @param defValue The default value if parsing fails or value is null.
     * @return The parsed boolean.
     */
    boolean parseBoolean(@NonNull Context context, @Nullable String value, boolean defValue);

    /**
     * Parses a color value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @return The parsed color int.
     */
    int parseColor(@NonNull Context context, @NonNull String value);

    /**
     * Parses a string value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @return The parsed string.
     */
    String parseString(@NonNull Context context, @NonNull String value);

    /**
     * Parses a string array value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @return The parsed list of strings.
     */
    List<String> parseStringArray(@NonNull Context context, @Nullable String value);

    /**
     * Parses an integer value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @param defValue The default value if parsing fails or value is null/empty.
     * @return The parsed integer.
     */
    int parseInteger(@NonNull Context context, @Nullable String value, int defValue);

    /**
     * Parses a float value.
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value.
     * @param defValue The default value if parsing fails.
     * @return The parsed float.
     */
    float parseFloat(@NonNull Context context, @Nullable String value, float defValue);

    /**
     * Parses a dimension value to pixel size (integer).
     *
     * @param context The context used for resource resolution (if applicable).
     * @param value The raw string value (e.g., "10dp", "16px").
     * @param defValue The default value if parsing fails.
     * @return The dimension in pixels.
     */
    int parseDimensionPixelSize(@NonNull Context context, @Nullable String value, int defValue);

    /**
     * Parses a resource identifier (e.g., @string/foo) and returns the resId. If the value is not a
     * reference, returns 0 (or defValue?).
     */
    int parseResourceId(@NonNull Context context, @Nullable String value, int defValue);

    /**
     * Parses a drawable.
     *
     * @param context The context used for resource resolution.
     * @param value The raw string value.
     * @return The parsed Drawable, or null if parsing fails.
     */
    @Nullable
    Drawable parseDrawable(@NonNull Context context, @Nullable String value);

    /**
     * Parses an interpolator.
     *
     * @param context The context used for resource resolution.
     * @param value The raw string value.
     * @return The parsed Interpolator, or null if parsing fails.
     */
    @Nullable
    Interpolator parseInterpolator(@NonNull Context context, @Nullable String value);

    /**
     * Parses an animator.
     *
     * @param context The context used for resource resolution.
     * @param value The raw string value.
     * @return The parsed Animator, or null if parsing fails.
     */
    @Nullable
    Animator parseAnimator(@NonNull Context context, @Nullable String value);

    /**
     * Parses an XML resource into an XmlPullParser.
     *
     * @param context The context used for resource resolution.
     * @param value The raw string value.
     * @return The XmlPullParser, or null if parsing fails.
     */
    @Nullable
    XmlPullParser parseXml(@NonNull Context context, @Nullable String value);
}
