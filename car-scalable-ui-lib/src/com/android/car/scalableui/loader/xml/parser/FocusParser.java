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

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Focus;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Focus} tag. */
public class FocusParser implements TagParser<Focus>, XmlChildParser<Variant.Builder> {
    public static final String FOCUS_TAG = "Focus";
    public static final String FOCUS_ON_TRANSITION_ATTRIBUTE = "onTransition";

    private static final AttributeMap<FocusBuilder> ATTRIBUTES =
            new AttributeMap.Builder<FocusBuilder>()
                    .addBoolean(
                            FOCUS_ON_TRANSITION_ATTRIBUTE,
                            (builder, value) -> builder.mFocusOnTransition = value)
                    .build();

    @NonNull
    @Override
    public Focus parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, FOCUS_TAG);

        FocusBuilder builder = new FocusBuilder();
        ATTRIBUTES.parse(env, parser, builder);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }

        return builder.build();
    }

    private static class FocusBuilder {
        boolean mFocusOnTransition = Focus.DEFAULT_FOCUS_ON_TRANSITION;

        Focus build() {
            return new Focus(mFocusOnTransition);
        }
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        Focus focus = parseTag(env, parser);
        builder.setCanFocusOnTransition(focus.canFocusOnTransition());
    }
}
