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

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for parsing XML values. */
public final class ParserUtils {
    private static final String TAG = ParserUtils.class.getSimpleName();

    public static final String DIP = "dip";
    public static final String DP = "dp";
    public static final String PERCENT = "%";
    public static final String PIXEL = "px";

    private ParserUtils() {
        // No instantiation
    }

    /** Parses dimension pixel size from attribute value, supporting resources and percentages. */
    @Nullable
    public static Integer getDimensionPixelSize(
            @NonNull Context context,
            @NonNull AttributeSet attrs,
            @NonNull String attrName,
            int displayId,
            boolean isHorizontal) {
        int resourceId = attrs.getAttributeResourceValue(null, attrName, 0);
        if (resourceId != 0) {
            TypedValue outValue = new TypedValue();
            try {
                context.getResources().getValue(resourceId, outValue, true);
                if (outValue.type == TypedValue.TYPE_DIMENSION) {
                    return context.getResources().getDimensionPixelSize(resourceId);
                } else if (outValue.type >= TypedValue.TYPE_FIRST_INT
                        && outValue.type <= TypedValue.TYPE_LAST_INT) {
                    return outValue.data;
                } else if (outValue.type == TypedValue.TYPE_STRING) {
                    String value = outValue.string.toString();
                    return parseDimensionString(context, value, displayId, isHorizontal);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to resolve dimension resource: " + resourceId, e);
            }
        }

        String value = attrs.getAttributeValue(null, attrName);
        if (value == null) {
            return null;
        }
        return parseDimensionString(context, value, displayId, isHorizontal);
    }

    /** Parses dimension pixel size from string value, supporting resources and percentages. */
    @Nullable
    public static Integer parseDimensionPixelSize(
            @NonNull Context context,
            @Nullable ValueParser valueParser,
            @NonNull String value,
            int displayId,
            boolean isHorizontal) {
        if (value == null) {
            return null;
        }
        if (valueParser != null) {
            int resourceId = valueParser.parseResourceId(context, value, 0);
            if (resourceId != 0) {
                TypedValue outValue = new TypedValue();
                try {
                    context.getResources().getValue(resourceId, outValue, true);
                    if (outValue.type == TypedValue.TYPE_DIMENSION) {
                        return context.getResources().getDimensionPixelSize(resourceId);
                    } else if (outValue.type >= TypedValue.TYPE_FIRST_INT
                            && outValue.type <= TypedValue.TYPE_LAST_INT) {
                        return outValue.data;
                    } else if (outValue.type == TypedValue.TYPE_STRING) {
                        String strValue = outValue.string.toString();
                        return parseDimensionString(context, strValue, displayId, isHorizontal);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to resolve dimension resource: " + resourceId, e);
                }
            }
        }
        return parseDimensionString(context, value, displayId, isHorizontal);
    }

    private static int parseDimensionString(
            @NonNull Context context, @NonNull String value, int displayId, boolean isHorizontal) {
        DisplayMetrics metrics = getDisplayMetricsForDisplay(context, displayId);
        int baseSize;
        if (value.endsWith(PERCENT)) {
            float percent = Float.parseFloat(value.substring(0, value.length() - 1)) / 100f;
            baseSize = isHorizontal ? metrics.widthPixels : metrics.heightPixels;
            return Math.round(baseSize * percent);
        }
        if (value.endsWith(DIP) || value.endsWith(DP)) {
            String dpValue =
                    value.endsWith(DIP)
                            ? value.substring(0, value.length() - 3)
                            : value.substring(0, value.length() - 2);
            return (int)
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(dpValue), metrics);
        }
        if (value.endsWith(PIXEL)) {
            return (int) Float.parseFloat(value.substring(0, value.length() - 2));
        }
        try {
            return (int) Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid dimension format: " + value);
            return 0;
        }
    }

    /** Extracts the resource entry name from a resource ID string. */
    @NonNull
    public static String getIdName(@NonNull Context context, @NonNull String id) {
        if (id.startsWith("@+id/")) {
            return id.substring(5);
        } else if (id.startsWith("@id/")) {
            return id.substring(4);
        }

        Pattern pattern = Pattern.compile("^@(\\d+)$");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find() && matcher.groupCount() >= 1) {
            String idName = matcher.group(1);
            if (idName != null && !idName.isEmpty()) {
                try {
                    int resourceId = Integer.parseInt(idName);
                    return context.getResources().getResourceEntryName(resourceId);
                } catch (NumberFormatException
                        | Resources.NotFoundException e) {
                    Log.e(TAG, "invalid resource format for string " + id);
                }
            }
        }
        return id;
    }

    /** Helper to get DisplayMetrics for a given displayId. */
    @NonNull
    public static DisplayMetrics getDisplayMetricsForDisplay(
            @NonNull Context context, int displayId) {
        DisplayManager displayManager = context.getSystemService(DisplayManager.class);
        Display display = displayManager.getDisplay(displayId);
        DisplayMetrics metrics = new DisplayMetrics();
        if (display != null) {
            display.getRealMetrics(metrics);
        } else {
            context.getResources().getDisplayMetrics();
            return context.getResources().getDisplayMetrics();
        }
        return metrics;
    }
}
