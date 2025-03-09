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

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Locale;

/**
 * Represents the corner of a {@code Panel}. This class provides methods for creating a Corner
 * object from an XML definition and retrieving the radius value.
 *
 * <p>The Corner class supports defining dimensions in the following formats:
 * <ul>
 *     <li><b>Absolute pixels:</b> e.g., <code>left="100"</code></li>
 *     <li><b>Density-independent pixels (dp):</b> e.g., <code>top="50dip"</code></li>
 *     <li><b>Resource references:</b> e.g., <code>bottom="@dimen/my_bottom_margin"</code></li>
 * </ul>
 */
class Corner {
    static final String CORNER_TAG = "Corner";
    private static final String RADIUS_ATTRIBUTE = "radius";
    private static final String DIP = "dip";
    private static final String DP = "dp";

    static final int DEFAULT_RADIUS = 0;

    private final int mRadius;

    /**
     * Constructs a Corner object with the specified radius value.
     *
     * @param radius The radius value. 0 indicates a sharp corner.
     */
    Corner(int radius) {
        mRadius = radius;
    }

    /**
     * Returns the radius value.
     *
     * @return The Corner's radius value.
     */
    public int getRadius() {
        return mRadius;
    }

    /**
     * Creates a Corner object from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Corner" and extracts the "radius"
     * attribute to create a Corner object. If the "radius" attribute is not specified,
     * it defaults to 0.
     *
     * @param context The application context.
     * @param parser The XML parser.
     * @return A Corner object with the parsed radius value.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static Corner create(Context context, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, null, CORNER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int radius = getDimensionPixelSize(context, attrs, RADIUS_ATTRIBUTE);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, CORNER_TAG);
        return new Corner(radius);
    }

    /**
     * Helper method to get a dimension pixel size from an attribute set.
     *
     * @param context The application context.
     * @param attrs The attribute set.
     * @param name The name of the attribute.
     * @return The dimension pixel size.
     */
    private static int getDimensionPixelSize(Context context, AttributeSet attrs, String name) {
        int resId = attrs.getAttributeResourceValue(null, name, 0);
        if (resId != 0) {
            return context.getResources().getDimensionPixelSize(resId);
        }
        String dimenStr = attrs.getAttributeValue(null, name);
        if (dimenStr == null) {
            return 0;
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(DP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        } else if (dimenStr.toLowerCase(Locale.ROOT).endsWith(DIP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DIP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        } else {
            return attrs.getAttributeIntValue(null, name, 0);
        }
    }
}
