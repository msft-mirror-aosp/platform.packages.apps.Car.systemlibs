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
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Layer} tag. */
public class LayerParser implements XmlChildParser<Variant.Builder> {
    public static final String LAYER_TAG = "Layer";
    public static final String LAYER_VALUE_ATTRIBUTE = "layer";

    private static final AttributeMap<Layer.Builder> ATTRIBUTES =
            AttributeMap.<Layer.Builder>builder()
                    .add(
                            LAYER_VALUE_ATTRIBUTE,
                            (env, value, builder) -> {
                                int layer =
                                        env.getValueParser()
                                                .parseInteger(
                                                        env.getContext(),
                                                        value,
                                                        Layer.DEFAULT_LAYER);
                                builder.setLayer(layer);
                            })
                    .build();

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, LAYER_TAG);

        Layer.Builder layerBuilder = new Layer.Builder();
        ATTRIBUTES.parse(env, parser, layerBuilder);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }

        builder.setLayer(layerBuilder.build().getLayer());
    }
}
