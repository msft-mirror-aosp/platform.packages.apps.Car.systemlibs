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

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides helper methods for working with XmlPullParser.
 */
public class XmlPullParserHelper {
    private static final String TAG = XmlPullParserHelper.class.getSimpleName();

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

    /**
     * Reads the text from the current XML element and resolves it if it is a string resource like
     * {@code @string/some_string}. If it's not a resource or the resource cannot be found, the
     * plain text is returned.
     *
     * @param context The context for accessing resources.
     * @param parser  The XmlPullParser instance, positioned at a START_TAG.
     * @return The resolved string resource, or the plain text content if it's not a resource.
     * @throws IOException if an I/O error occurs.
     * @throws XmlPullParserException if the parser encounters an unexpected event type.
     */
    @Nullable
    static String readStringResource(@NonNull Context context,
            @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String text = readText(parser);

        if (text != null && text.startsWith("@string/")) {
            int resId = context.getResources().getIdentifier(text.substring(1), null,
                    context.getPackageName());
            if (resId != 0) {
                try {
                    return context.getResources().getString(resId);
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "String resource not found for: "
                            + text, e);
                }
            } else {
                Log.w(TAG, "Could not find resource identifier "
                        + "for: " + text);
            }
        }
        return text;
    }

    /**
     * Reads an array resource value from the current XML element, such as
     * {@code <PersistentActivityList>@array/activity_list</PersistentActivityList>}.
     *
     * @param context The context for accessing resources.
     * @param parser  The XmlPullParser instance, positioned at a START_TAG.
     * @return A list of strings from the resolved array resource, or {@code null} if the resource
     * is not found or the tag's content is not a valid array resource identifier.
     * @throws IOException if an I/O error occurs.
     * @throws XmlPullParserException if the parser encounters an unexpected event type.
     */
    @NonNull
    static List<String> readArrayResource(@NonNull Context context,
            @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        List<String> result = new ArrayList<>();
        String text = readText(parser);

        if (text != null && text.startsWith("@array/")) {
            int resId = context.getResources().getIdentifier(text.substring(1), null,
                    context.getPackageName());
            if (resId != 0) {
                try {
                    return Arrays.asList(context.getResources().getStringArray(resId));
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Array resource not found for: " + text, e);
                }
            } else {
                Log.w(TAG, "Could not find resource identifier for: " + text);
            }
        }
        return result;
    }
}
