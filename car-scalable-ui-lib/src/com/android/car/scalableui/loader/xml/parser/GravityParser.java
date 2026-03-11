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

import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.model.GravityVariant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Locale;

/** Parsers {@link Integer} gravity value from XML. */
public class GravityParser implements TagParser<Integer>, XmlChildParser<GravityVariant.Builder> {

    public static final String GRAVITY_TAG = "Gravity";
    private static final String TAG = GravityParser.class.getSimpleName();
    private static final String GRAVITY_VALUE_ATTRIBUTE = "value";
    private static final String GRAVITY_SEPARATOR = "\\|";

    private static final AttributeMap<GravityParsingState> ATTRIBUTES =
            AttributeMap.<GravityParsingState>builder()
                    .add(
                            GRAVITY_VALUE_ATTRIBUTE,
                            (env, value, state) -> state.mGravity = parseGravity(value))
                    .build();

    private static class GravityParsingState {
        int mGravity = Gravity.NO_GRAVITY;
    }

    @NonNull
    @Override
    public Integer parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, GRAVITY_TAG);

        GravityParsingState state = new GravityParsingState();
        ATTRIBUTES.parse(env, parser, state);

        // Skip any nested content.
        while (parser.next() != XmlPullParser.END_TAG) {
            // No nested tags expected.
        }

        return state.mGravity;
    }

    private static int parseGravity(@NonNull String value) {
        int gravity = Gravity.NO_GRAVITY;
        String[] tokens = value.split(GRAVITY_SEPARATOR);
        for (String token : tokens) {
            String sanitizedToken = token.trim().toUpperCase(Locale.ROOT);
            switch (sanitizedToken) {
                case "TOP" -> gravity |= Gravity.TOP;
                case "BOTTOM" -> gravity |= Gravity.BOTTOM;
                case "LEFT" -> gravity |= Gravity.LEFT;
                case "RIGHT" -> gravity |= Gravity.RIGHT;
                case "CENTER" -> gravity |= Gravity.CENTER;
                case "CENTER_HORIZONTAL" -> gravity |= Gravity.CENTER_HORIZONTAL;
                case "CENTER_VERTICAL" -> gravity |= Gravity.CENTER_VERTICAL;
                case "START" -> gravity |= Gravity.START;
                case "END" -> gravity |= Gravity.END;
                case "CLIP_VERTICAL" -> gravity |= Gravity.CLIP_VERTICAL;
                case "CLIP_HORIZONTAL" -> gravity |= Gravity.CLIP_HORIZONTAL;
                default -> Log.w(TAG, "Unknown gravity value: " + token);
            }
        }
        return gravity;
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull GravityVariant.Builder builder)
            throws XmlPullParserException, IOException {
        Integer gravity = parseTag(env, parser);
        builder.setGravity(gravity);
    }
}
