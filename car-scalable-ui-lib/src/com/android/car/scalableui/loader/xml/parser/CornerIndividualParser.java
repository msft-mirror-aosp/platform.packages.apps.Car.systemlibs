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
import com.android.car.scalableui.model.Corner;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Corner} tag that supports individual corner radii. */
public class CornerIndividualParser implements TagParser<Corner>, XmlChildParser<Variant.Builder> {
    public static final String TOP_LEFT_RADIUS_ATTRIBUTE = "topLeftRadius";
    public static final String TOP_RIGHT_RADIUS_ATTRIBUTE = "topRightRadius";
    public static final String BOTTOM_LEFT_RADIUS_ATTRIBUTE = "bottomLeftRadius";
    public static final String BOTTOM_RIGHT_RADIUS_ATTRIBUTE = "bottomRightRadius";

    private static final AttributeMap<Corner.Builder> ATTRIBUTES =
            AttributeMap.<Corner.Builder>builder()
                    .add(
                            CornerParser.RADIUS_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer radius =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (radius != null) {
                                    builder.setRadius(radius);
                                }
                            })
                    .add(
                            TOP_LEFT_RADIUS_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer radius =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (radius != null) {
                                    builder.setTopLeftRadius(radius);
                                }
                            })
                    .add(
                            TOP_RIGHT_RADIUS_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer radius =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (radius != null) {
                                    builder.setTopRightRadius(radius);
                                }
                            })
                    .add(
                            BOTTOM_LEFT_RADIUS_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer radius =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (radius != null) {
                                    builder.setBottomLeftRadius(radius);
                                }
                            })
                    .add(
                            BOTTOM_RIGHT_RADIUS_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer radius =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (radius != null) {
                                    builder.setBottomRightRadius(radius);
                                }
                            })
                    .build();

    @NonNull
    @Override
    public Corner parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, CornerParser.CORNER_TAG);

        Corner.Builder builder = new Corner.Builder();
        ATTRIBUTES.parse(env, parser, builder);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }
        return builder.build();
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        Corner corner = parseTag(env, parser);
        builder.setCornerRadius(corner);
    }
}
