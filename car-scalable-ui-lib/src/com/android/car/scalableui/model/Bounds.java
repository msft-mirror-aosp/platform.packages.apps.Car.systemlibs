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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Represents the bounds of a UI element. This class provides methods for creating a Bounds object
 * from an XML definition and retrieving the bounds as a {@link Rect}.
 *
 * <p>The Bounds class supports defining dimensions in the following formats:
 * <ul>
 *     <li><b>Absolute pixels:</b> e.g., <code>left="100"</code></li>
 *     <li><b>Density-independent pixels (dp):</b> e.g., <code>top="50dip"</code></li>
 *     <li><b>Percentage of screen width/height:</b> e.g., <code>right="80%"</code></li>
 *     <li><b>Resource references:</b> e.g., <code>bottom="@dimen/my_bottom_margin"</code></li>
 * </ul>
 *
 * <p>It also allows defining either the left and right positions, or the left position and width.
 * Similarly, it allows defining either the top and bottom positions, or the top position and
 * height.
 */
class Bounds {
    static final String BOUNDS_TAG = "Bounds";
    private static final String LEFT_ATTRIBUTE = "left";
    private static final String RIGHT_ATTRIBUTE = "right";
    private static final String TOP_ATTRIBUTE = "top";
    private static final String BOTTOM_ATTRIBUTE = "bottom";
    private static final String WIDTH_ATTRIBUTE = "width";
    private static final String HEIGHT_ATTRIBUTE = "height";
    private static final String DIP = "dip";
    private static final String DP = "dp";
    private static final String PERCENT = "%";
    private final int mLeft;
    private final int mTop;
    private final int mRight;
    private final int mBottom;

    /**
     * Constructs a Bounds object with the specified left, top, right, and bottom positions.
     *
     * @param left The left position in pixels.
     * @param top The top position in pixels.
     * @param right The right position in pixels.
     * @param bottom The bottom position in pixels.
     */
    Bounds(int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    /**
     * Returns the bounds as a {@link Rect} object.
     *
     * @return A Rect object representing the bounds.
     */
    public Rect getRect() {
        return new Rect(mLeft, mTop, mRight, mBottom);
    }

    /**
     * Creates a Bounds object from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Bounds" and extracts the "left", "top",
     * "right", and "bottom" attributes (or equivalent width/height combinations) to create a
     * Bounds object.
     *
     * @param context The application context.
     * @param parser The XML parser.
     * @return A Bounds object with the parsed bounds.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static Bounds create(Context context, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, null, BOUNDS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int left = getDimensionPixelSize(context, attrs, LEFT_ATTRIBUTE, true);
        int top = getDimensionPixelSize(context, attrs,  TOP_ATTRIBUTE, false);
        int right = getDimensionPixelSize(context, attrs, RIGHT_ATTRIBUTE, true);
        int bottom = getDimensionPixelSize(context, attrs, BOTTOM_ATTRIBUTE, false);

        int width = getDimensionPixelSize(context, attrs, WIDTH_ATTRIBUTE, true);
        int height = getDimensionPixelSize(context, attrs, HEIGHT_ATTRIBUTE, false);
        if (attrs.getAttributeValue(null, RIGHT_ATTRIBUTE) == null) {
            right = left + width;
        } else if (attrs.getAttributeValue(null, LEFT_ATTRIBUTE) == null) {
            left = right - width;
        }
        if (attrs.getAttributeValue(null, BOTTOM_ATTRIBUTE) == null) {
            bottom = top + height;
        } else if (attrs.getAttributeValue(null, TOP_ATTRIBUTE) == null) {
            top = bottom - height;
        }

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, BOUNDS_TAG);
        return new Bounds(left, top, right, bottom);
    }

    /**
     * Helper method to get a dimension pixel size from an attribute set.
     *
     * @param context The application context.
     * @param attrs The attribute set.
     * @param name The name of the attribute.
     * @param isHorizontal Whether the dimension is horizontal (width) or vertical (height).
     * @return The dimension pixel size.
     */
    private static int getDimensionPixelSize(Context context, AttributeSet attrs, String name,
            boolean isHorizontal) {
        int resId = attrs.getAttributeResourceValue(null, name, 0);
        if (resId != 0) {
            return context.getResources().getDimensionPixelSize(resId);
        }
        String dimenStr = attrs.getAttributeValue(null, name);
        if (dimenStr == null) {
            return 0;
        }
        if (dimenStr.toLowerCase().endsWith(DP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        } else if (dimenStr.toLowerCase().endsWith(DIP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DIP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        } else if (dimenStr.toLowerCase().endsWith(PERCENT)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - PERCENT.length());
            float value = Float.parseFloat(valueStr);
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            if (isHorizontal) {
                return (int) (value * displayMetrics.widthPixels / 100);
            } else {
                return (int) (value * displayMetrics.heightPixels / 100);
            }
        } else {
            return attrs.getAttributeIntValue(null, name, 0);
        }
    }
}
