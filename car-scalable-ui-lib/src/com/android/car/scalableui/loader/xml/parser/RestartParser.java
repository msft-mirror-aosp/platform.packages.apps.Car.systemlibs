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

import static com.android.car.scalableui.loader.xml.parser.RestartParser.MAX_RETRY_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.parser.RestartParser.POLICY_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.parser.RestartParser.RESTART_TAG;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Restart;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Restart} tag. */
public class RestartParser implements XmlChildParser<PanelState> {

    public static final String RESTART_TAG = "Restart";
    public static final String POLICY_ATTRIBUTE = "policy";
    public static final String MAX_RETRY_ATTRIBUTE = "maxRetry";

    private static class RestartParsingState {
        String mPolicy;
        int mMaxRetry;
    }

    private static final AttributeMap<RestartParsingState> ATTRIBUTES =
            AttributeMap.<RestartParsingState>builder()
                    .addString(POLICY_ATTRIBUTE, (state, value) -> state.mPolicy = value)
                    .addInteger(MAX_RETRY_ATTRIBUTE, (state, value) -> state.mMaxRetry = value)
                    .build();

    @NonNull
    private Restart parseRestart(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, RESTART_TAG);

        RestartParsingState state = new RestartParsingState();
        ATTRIBUTES.parse(env, parser, state);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }
        return new Restart(state.mPolicy, state.mMaxRetry);
    }

    @Override
    public void parse(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws XmlPullParserException, IOException {
        parent.addRestart(parseRestart(env, parser));
    }
}
