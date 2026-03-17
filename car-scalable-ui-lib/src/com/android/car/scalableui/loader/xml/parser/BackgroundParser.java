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

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Background;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Background} tag. */
public class BackgroundParser implements TagParser<Background>, XmlChildParser<Variant.Builder> {

    public static final String BACKGROUND_TAG = "Background";
    public static final String BACKGROUND_COLOR_ATTRIBUTE = "color";
    public static final String BACKGROUND_DRAWABLE_ATTRIBUTE = "drawable";

    private static final String TAG = BackgroundParser.class.getSimpleName();

    private static class BackgroundParsingState {
        int mBackgroundColor = 0;
        Drawable mBackgroundDrawable;
        float mAlpha = 1.0f;
    }

    private static final AttributeMap<BackgroundParsingState> ATTRIBUTES =
            AttributeMap.<BackgroundParsingState>builder()
                    .add(
                            BACKGROUND_COLOR_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBackgroundColor =
                                            env.getValueParser()
                                                    .parseColor(env.getContext(), value))
                    .add(
                            BACKGROUND_DRAWABLE_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBackgroundDrawable =
                                            env.getValueParser()
                                                    .parseDrawable(env.getContext(), value))
                    .addFloat(
                            AlphaParser.ALPHA_VALUE_ATTRIBUTE,
                            (state, value) -> state.mAlpha = value)
                    .build();


    @NonNull
    @Override
    public Background parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, BACKGROUND_TAG);

        BackgroundParsingState state = new BackgroundParsingState();
        ATTRIBUTES.parse(env, parser, state);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }
        return new Background(
                state.mBackgroundColor,
                state.mBackgroundDrawable,
                state.mAlpha);
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        Background bg = parseTag(env, parser);
        if (builder.getPanelId() != null) {
            builder.addDecor(bg.toDecor(builder.getPanelId()));
        }
    }
}
