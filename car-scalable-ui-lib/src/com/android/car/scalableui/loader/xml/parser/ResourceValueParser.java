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
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ValueParser that resolves Android resources (e.g., @string/foo). Extends {@link BaseValueParser}
 * to handle literals when not a reference.
 */
public class ResourceValueParser extends BaseValueParser {
    private static final String TAG = ResourceValueParser.class.getSimpleName();

    @Override
    public boolean parseBoolean(@NonNull Context context, @NonNull String value) {
        if (value.startsWith("@")) {
            int resId = getResourceId(context, value, "bool");
            if (resId != 0) {
                return context.getResources().getBoolean(resId);
            }
        }
        return super.parseBoolean(context, value);
    }

    @Override
    public boolean parseBoolean(
            @NonNull Context context, @Nullable String value, boolean defValue) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "bool");
            if (resId != 0) {
                return context.getResources().getBoolean(resId);
            }
        }
        return super.parseBoolean(context, value, defValue);
    }

    @Override
    public int parseColor(@NonNull Context context, @NonNull String value) {
        if (value.startsWith("@")) {
            int resId = getResourceId(context, value, "color");
            if (resId != 0) {
                return context.getResources().getColor(resId, context.getTheme());
            }
        }
        return super.parseColor(context, value);
    }

    @Override
    public String parseString(@NonNull Context context, @NonNull String value) {
        if (value.startsWith("@")) {
            int resId = getResourceId(context, value, "string");
            if (resId != 0) {
                // Verify that the resource is actually a string before calling getString to avoid
                // exceptions.
                try {
                    String type = context.getResources().getResourceTypeName(resId);
                    if ("string".equals(type)) {
                        return context.getString(resId);
                    }
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "String resource not found for: " + value, e);
                }
            }
        }
        return super.parseString(context, value);
    }

    /**
     * Parses a string array resource from the text content.
     *
     * @param context The context for accessing resources.
     * @param value The text content of the element, e.g. "@array/my_array".
     * @return A list of strings from the resolved array resource, or an empty list if not found.
     */
    @NonNull
    public List<String> parseStringArray(@NonNull Context context, @Nullable String value) {
        List<String> result = new ArrayList<>();
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "array");
            if (resId != 0) {
                try {
                    String[] array = context.getResources().getStringArray(resId);
                    if (array != null) {
                        return Arrays.asList(array);
                    }
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Array resource not found for: " + value, e);
                }
            } else {
                Log.w(TAG, "Could not find resource identifier for: " + value);
            }
        }
        return result;
    }

    @Override
    public int parseInteger(@NonNull Context context, @Nullable String value, int defValue) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "integer");
            if (resId != 0) {
                try {
                    return context.getResources().getInteger(resId);
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Integer resource not found for: " + value, e);
                }
            }
        }
        return super.parseInteger(context, value, defValue);
    }

    @Override
    public float parseFloat(@NonNull Context context, @Nullable String value, float defValue) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "dimen");
            if (resId != 0) {
                try {
                    // Use TypedValue for float resources
                    TypedValue outValue = new TypedValue();
                    context.getResources().getValue(resId, outValue, true);
                    if (outValue.type == TypedValue.TYPE_FLOAT) {
                        return outValue.getFloat();
                    }
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Float resource not found for: " + value, e);
                }
            }
        }
        return super.parseFloat(context, value, defValue);
    }

    @Override
    public int parseDimensionPixelSize(
            @NonNull Context context, @Nullable String value, int defValue) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "dimen");
            if (resId != 0) {
                TypedValue outValue = new TypedValue();
                context.getResources().getValue(resId, outValue, true);
                if (outValue.type == TypedValue.TYPE_DIMENSION) {
                    return context.getResources().getDimensionPixelSize(resId);
                } else if (outValue.type >= TypedValue.TYPE_FIRST_INT
                        && outValue.type <= TypedValue.TYPE_LAST_INT) {
                    return outValue.data;
                } else if (outValue.type == TypedValue.TYPE_STRING) {
                    return super.parseDimensionPixelSize(
                            context, outValue.string.toString(), defValue);
                }
            }
        }
        return super.parseDimensionPixelSize(context, value, defValue);
    }

    @Override
    public int parseResourceId(@NonNull Context context, @Nullable String value, int defValue) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, null);
            if (resId != 0) {
                return resId;
            }
        }
        return super.parseResourceId(context, value, defValue);
    }

    @Override
    public Drawable parseDrawable(@NonNull Context context, @Nullable String value) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "drawable");
            if (resId != 0) {
                return context.getDrawable(resId);
            }
        }
        return super.parseDrawable(context, value);
    }

    @Override
    public Interpolator parseInterpolator(@NonNull Context context, @Nullable String value) {
        if (value != null && value.startsWith("@")) {
            // interpolator resources don't have a standard defType like "interpolator" in
            // getIdentifier
            // often they are in "anim" or "interpolator" (if custom).
            // However, getResourceId tries defType then null.
            // Let's try "interpolator" as defType, or "anim".
            // Actually, for system resources they are often @android:interpolator/...
            int resId = getResourceId(context, value, "interpolator");
            if (resId != 0) {
                return AnimationUtils.loadInterpolator(context, resId);
            }
        }
        return super.parseInterpolator(context, value);
    }

    @Override
    public Animator parseAnimator(@NonNull Context context, @Nullable String value) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "animator");
            if (resId != 0) {
                return AnimatorInflater.loadAnimator(context, resId);
            }
        }
        return super.parseAnimator(context, value);
    }

    @Nullable
    @Override
    public XmlPullParser parseXml(@NonNull Context context, @Nullable String value) {
        if (value != null && value.startsWith("@")) {
            int resId = getResourceId(context, value, "xml");
            if (resId != 0) {
                try {
                    String type = context.getResources().getResourceTypeName(resId);
                    if ("xml".equals(type)) {
                        return context.getResources().getXml(resId);
                    }
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Xml resource not found for: " + value, e);
                }
            }
        }
        return super.parseXml(context, value);
    }

    private int getResourceId(Context context, String value, String defType) {
        if (value == null || value.length() < 2) {
            return 0;
        }
        try {
            // Remove the @ prefix
            String resName = value.substring(1);
            if (resName.startsWith("*")) {
                resName = resName.substring(1);
            }
            int resId =
                    context.getResources()
                            .getIdentifier(resName, defType, context.getPackageName());
            if (resId == 0 && defType != null) {
                // Also try without type if it failed
                resId =
                        context.getResources()
                                .getIdentifier(resName, null, context.getPackageName());
            }
            return resId;
        } catch (Exception e) {
            Log.w(TAG, "Failed to resolve resource: " + value, e);
            return 0;
        }
    }
}
