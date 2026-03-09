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


import static com.android.car.scalableui.loader.xml.parser.EventParser.EVENT_TAG;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Action;
import com.android.car.scalableui.model.Event;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Parsers a list of {@link Action} objects from XML. */
public class ActionParser implements TagParser<List<Action>> {

    public static final String ACTIONS_TAG = "Actions";
    private static final String ACTION_TAG = "Action";
    private static final String INTENT_ATTRIBUTE = "intent";

    private static final AttributeMap<Action.Builder> ATTRIBUTES =
            AttributeMap.<Action.Builder>builder()
                    .addString(
                            INTENT_ATTRIBUTE,
                            (builder, value) -> {
                                try {
                                    Intent intent =
                                            Intent.parseUri(value, Intent.URI_INTENT_SCHEME);
                                    builder.setIntent(intent);
                                } catch (URISyntaxException e) {
                                    throw new IOException("Invalid URI syntax in Action", e);
                                }
                            })
                    .build();

    @NonNull
    @Override
    public List<Action> parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Expected start tag");
        }

        String tagName = parser.getName();
        if (!ACTIONS_TAG.equals(tagName)) {
            throw new XmlPullParserException("Expected <" + ACTIONS_TAG + "> but found " + tagName);
        }

        List<Action> actions = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (ACTION_TAG.equals(name)) {
                actions.add(parseAction(env, parser));
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
        return actions;
    }

    @NonNull
    private Action parseAction(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, ACTION_TAG);

        Action.Builder builder = new Action.Builder();
        ATTRIBUTES.parse(env, parser, builder);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (EVENT_TAG.equals(name)) {
                TagParser<Event> eventParser = env.getRegistry().getParser(EVENT_TAG);
                if (eventParser != null) {
                    builder.addTrigger(eventParser.parseTag(env, parser));
                } else {
                    XmlPullParserHelper.throwIfUnknownTag(parser);
                }
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
        return builder.build();
    }
}
