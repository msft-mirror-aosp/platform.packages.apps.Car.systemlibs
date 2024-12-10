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
 * Represents the visibility of a Panel in the Scalable UI system.
 *
 * <p>This class encapsulates a boolean value indicating whether a panel is visible or not.
 * It can be created from an XML definition or directly using a boolean value.
 */
public class Visibility {
    static final String VISIBILITY_TAG = "Visibility";
    private static final String IS_VISIBLE_ATTRIBUTE = "isVisible";
    static final boolean DEFAULT_VISIBILITY = true;

    private final boolean mIsVisible;

    /**
     * Constructor for Visibility.
     *
     * @param isVisible Whether the element is visible.
     */
    public Visibility(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    /**
     * Copy constructor for Visibility.
     *
     * @param visibility The Visibility object to copy from.
     */
    public Visibility(Visibility visibility) {
        this(visibility.mIsVisible);
    }

    /**
     * Returns whether the element is visible.
     *
     * @return True if the element is visible, false otherwise.
     */
    public boolean isVisible() {
        return mIsVisible;
    }

    /**
     * Creates a Visibility object from an XML parser.
     *
     * @param parser The XML parser.
     * @return The created Visibility object.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    public static Visibility create(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, null, VISIBILITY_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        boolean isVisible = attrs.getAttributeBooleanValue(null, IS_VISIBLE_ATTRIBUTE,
                DEFAULT_VISIBILITY);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, VISIBILITY_TAG);
        return new Visibility(isVisible);
    }
}
