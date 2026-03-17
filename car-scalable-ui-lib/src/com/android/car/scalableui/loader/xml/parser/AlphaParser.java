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
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Alpha;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Alpha} tag. */
public class AlphaParser implements XmlChildParser<Variant.Builder> {

    public static final String ALPHA_TAG = "Alpha";
    public static final String ALPHA_VALUE_ATTRIBUTE = "alpha";

    private static final AttributeMap<Alpha.Builder> ATTRIBUTES =
            AttributeMap.<Alpha.Builder>builder()
                    .addFloat(ALPHA_VALUE_ATTRIBUTE, (builder, value) -> builder.setAlpha(value))
                    .build();

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, ALPHA_TAG);

        Alpha.Builder alphaBuilder = new Alpha.Builder();
        alphaBuilder.setAlpha(Alpha.DEFAULT_ALPHA);
        ATTRIBUTES.parse(env, parser, alphaBuilder);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }

        builder.setAlpha(alphaBuilder.build().getAlpha());
    }
}
