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

package com.android.car.scalableui.loader.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * This class provides helper methods for working with XmlPullParser.
 */
public class XmlPullParserHelper {
    /**
     * Skips an XML tag and all its contents.
     *
     * @param parser The XML parser.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) throw new IllegalStateException();
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Reads the text content from the current XML element.
     *
     * <p>This helper function assumes the parser is currently positioned on a START_TAG. It reads
     * the text from the subsequent TEXT event and consumes the corresponding END_TAG to advance
     * the parser.
     *
     * @param parser The XmlPullParser instance, positioned at a START_TAG.
     * @return The text content of the element, or an empty string if the element is empty.
     * @throws IOException if an I/O error occurs.
     * @throws XmlPullParserException if the parser encounters an unexpected event type.
     */
    static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
