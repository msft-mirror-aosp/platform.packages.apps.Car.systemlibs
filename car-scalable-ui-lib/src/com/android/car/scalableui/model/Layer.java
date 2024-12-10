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
 * Represents the layer of a {@code Panel}. This class provides methods for creating a Layer object
 * from an XML definition and retrieving the layer value.
 */
class Layer {
    static final String LAYER_TAG = "Layer";
    private static final String LAYER_ATTRIBUTE = "layer";

    static final int DEFAULT_LAYER = 0;

    private final int mLayer;

    /**
     * Constructs a Layer object with the specified layer value.
     *
     * @param layer The layer value. Higher values indicate that the element should be drawn on top
     *              of elements with lower layer values.
     */
    Layer(int layer) {
        mLayer = layer;
    }

    /**
     * Returns the layer value.
     *
     * @return The layer value.
     */
    public int getLayer() {
        return mLayer;
    }

    /**
     * Creates a Layer object from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Layer" and extracts the "layer" attribute
     * to create a Layer object. If the "layer" attribute is not specified, it defaults to 0.
     *
     * @param parser The XML parser.
     * @return A Layer object with the parsed layer value.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static Layer create(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, LAYER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int layer = attrs.getAttributeIntValue(null, LAYER_ATTRIBUTE, DEFAULT_LAYER);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, LAYER_TAG);
        return new Layer(layer);
    }
}
