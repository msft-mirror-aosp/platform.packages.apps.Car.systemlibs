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

import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Represents the alpha (transparency) value of a UI element. This class provides methods for
 * creating an Alpha object from an XML definition and retrieving the alpha value.
 */
class Alpha {
    static final String ALPHA_TAG = "Alpha";
    private static final String ALPHA_ATTRIBUTE = "alpha";
    static final float DEFAULT_ALPHA = 1;

    private final float mAlpha;

    /**
     * Constructs an Alpha object with the specified alpha value.
     *
     * @param alpha The alpha value, between 0 (fully transparent) and 1 (fully opaque).
     */
    Alpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * Returns the alpha value.
     *
     * @return The alpha value.
     */
    public float getAlpha() {
        return mAlpha;
    }

    /**
     * Creates an Alpha object from an XML parser.
     *
     * This method parses an XML element with the tag "Alpha" and extracts the "alpha" attribute
     * to create an Alpha object. If the "alpha" attribute is not specified, it defaults to 1.0.
     *
     * @param parser The XML parser.
     * @return An Alpha object with the parsed alpha value.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static Alpha create(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, ALPHA_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        float alpha = attrs.getAttributeFloatValue(null, ALPHA_ATTRIBUTE, DEFAULT_ALPHA);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, ALPHA_TAG);
        return new Alpha(alpha);
    }
}
