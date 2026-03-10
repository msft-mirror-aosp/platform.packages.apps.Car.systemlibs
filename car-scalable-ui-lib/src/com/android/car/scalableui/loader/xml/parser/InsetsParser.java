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

import android.graphics.Insets;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Insets} tag. */
public class InsetsParser implements XmlChildParser<Variant.Builder> {
    public static final String INSETS_TAG = "Insets";
    public static final String LEFT_ATTRIBUTE = "left";
    public static final String TOP_ATTRIBUTE = "top";
    public static final String RIGHT_ATTRIBUTE = "right";
    public static final String BOTTOM_ATTRIBUTE = "bottom";

    private static class InsetsParsingState {
        int mLeft;
        int mTop;
        int mRight;
        int mBottom;
    }

    private static final AttributeMap<InsetsParsingState> ATTRIBUTES =
            AttributeMap.<InsetsParsingState>builder()
                    .add(
                            LEFT_ATTRIBUTE,
                            (env, value, state) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    state.mLeft = v;
                                }
                            })
                    .add(
                            TOP_ATTRIBUTE,
                            (env, value, state) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    state.mTop = v;
                                }
                            })
                    .add(
                            RIGHT_ATTRIBUTE,
                            (env, value, state) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    state.mRight = v;
                                }
                            })
                    .add(
                            BOTTOM_ATTRIBUTE,
                            (env, value, state) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    state.mBottom = v;
                                }
                            })
                    .build();



    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, INSETS_TAG);

        InsetsParsingState state = new InsetsParsingState();
        ATTRIBUTES.parse(env, parser, state);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }

        builder.setInsets(Insets.of(state.mLeft, state.mTop, state.mRight, state.mBottom));
    }
}
