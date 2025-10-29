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
package com.android.car.scalableui.loader.xml;

import static com.android.car.scalableui.loader.xml.EventTagXmlParserKt.EVENT_TAG;
import static com.android.car.scalableui.loader.xml.EventTagXmlParserKt.parseEvent;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.android.car.scalableui.model.Action;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that uses a {@link XmlPullParser} to create a list of {@link Action} objects.
 */
public class ActionXmlParser {
    private static final String TAG = ActionXmlParser.class.getSimpleName();

    // --- Action Tags ---
    public static final String ACTIONS_TAG = "Actions";
    public static final String ACTION_TAG = "Action";
    public static final String INTENT_ATTRIBUTE = "intent";

    @NonNull
    static List<Action> parse(@NonNull Context context, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException, URISyntaxException {

        // Consume any START_DOCUMENT or whitespace events
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT
                || (eventType == XmlPullParser.TEXT && parser.isWhitespace())) {
            eventType = parser.next();
        }
        if (eventType != XmlPullParser.START_TAG || !parser.getName().equals(ACTIONS_TAG)) {
            throw new XmlPullParserException("Expected <Actions> tag at the beginning but "
                    + parser.getName());
        }

        parser.require(XmlPullParser.START_TAG, null, ACTIONS_TAG);
        List<Action> actions = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case ACTION_TAG:
                    actions.add(parseAction(context, parser));
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
            }
        }
        return actions;
    }

    @NonNull
    private static Action parseAction(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException, URISyntaxException {
        parser.require(XmlPullParser.START_TAG, null, ACTION_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String intentString = attrs.getAttributeValue(null, INTENT_ATTRIBUTE);
        Intent intent = Intent.parseUri(intentString, Intent.URI_INTENT_SCHEME);
        Action.Builder builder = new Action.Builder(intent);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case EVENT_TAG:
                    builder.addTrigger(parseEvent(parser));
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
            }
        }

        return builder.build();
    }
}
