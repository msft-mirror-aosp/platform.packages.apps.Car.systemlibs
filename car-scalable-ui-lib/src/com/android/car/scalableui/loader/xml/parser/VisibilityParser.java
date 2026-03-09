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

import static com.android.car.scalableui.loader.xml.parser.VisibilityParser.IS_VISIBLE_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.parser.VisibilityParser.VISIBILITY_TAG;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.model.Visibility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Visibility} tag. */
public class VisibilityParser implements TagParser<Visibility>, XmlChildParser<Variant.Builder> {

    public static final String VISIBILITY_TAG = "Visibility";
    public static final String IS_VISIBLE_ATTRIBUTE = "isVisible";

    private static final AttributeMap<Visibility.Builder> ATTRIBUTES =
            AttributeMap.<Visibility.Builder>builder()
                    .add(
                            IS_VISIBLE_ATTRIBUTE,
                            (env, value, builder) -> {
                                boolean isVisible =
                                        env.getValueParser()
                                                .parseBoolean(
                                                        env.getContext(),
                                                        value,
                                                        Visibility.DEFAULT_VISIBILITY);
                                builder.setIsVisible(isVisible);
                            })
                    .build();

    @NonNull
    @Override
    public Visibility parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, VISIBILITY_TAG);

        Visibility.Builder builder = new Visibility.Builder();
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
        Visibility visibility = parseTag(env, parser);
        builder.setVisibility(visibility.isVisible());
    }
}
