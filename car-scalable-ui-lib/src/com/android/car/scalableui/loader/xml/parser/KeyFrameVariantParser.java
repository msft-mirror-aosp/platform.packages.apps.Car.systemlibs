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

import static com.android.car.scalableui.loader.xml.parser.VariantParser.ID_ATTRIBUTE;

import android.util.AttributeSet;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link KeyFrameVariant} elements. */
public class KeyFrameVariantParser implements XmlChildParser<PanelState> {
    public static final String KEY_FRAME_VARIANT_TAG = "KeyFrameVariant";
    public static final String KEY_FRAME_TAG = "KeyFrame";
    private static final String FRAME_ATTRIBUTE = "frame";
    private static final String VARIANT_ATTRIBUTE = "variant";

    private static final AttributeMap<KeyFrameVariant.Builder> ATTRIBUTES =
            new AttributeMap.Builder<KeyFrameVariant.Builder>()
                    .add(
                            VariantParser.PARENT_ATTRIBUTE,
                            (env, value, builder) ->
                                    builder.setParentId(
                                            ParserUtils.getIdName(env.getContext(), value)))
                    .addFloat(AlphaParser.ALPHA_VALUE_ATTRIBUTE, Variant.Builder::setAlpha)
                    .addBoolean(
                            VisibilityParser.IS_VISIBLE_ATTRIBUTE, Variant.Builder::setVisibility)
                    .addInteger(LayerParser.LAYER_VALUE_ATTRIBUTE, Variant.Builder::setLayer)
                    .addBoolean(
                            FocusParser.FOCUS_ON_TRANSITION_ATTRIBUTE,
                            Variant.Builder::setCanFocusOnTransition)
                    .build();

    private static final AttributeMap<KeyFrameVariant.KeyFrame.Builder> KEY_FRAME_ATTRIBUTES =
            AttributeMap.<KeyFrameVariant.KeyFrame.Builder>builder()
                    .addInteger(FRAME_ATTRIBUTE, KeyFrameVariant.KeyFrame.Builder::setFrame)
                    .build();

    /** Parses a {@link KeyFrameVariant} from XML. */
    @Override
    public void parse(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, KEY_FRAME_VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String idName = ParserUtils.getIdName(env.getContext(), id);

        KeyFrameVariant.Builder builder = new KeyFrameVariant.Builder(id, idName);
        ATTRIBUTES.parse(env, parser, builder);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(KEY_FRAME_TAG)) {
                builder.addKeyFrame(parseKeyFrame(env, parser, parent));
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
        parent.addVariant(builder.build(parent));
    }

    /** Parses a {@link KeyFrameVariant.KeyFrame} from XML. */
    private KeyFrameVariant.KeyFrame parseKeyFrame(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, KEY_FRAME_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String variantId = attrs.getAttributeValue(null, VARIANT_ATTRIBUTE);
        String variantIdName = ParserUtils.getIdName(env.getContext(), variantId);

        KeyFrameVariant.KeyFrame.Builder builder = new KeyFrameVariant.KeyFrame.Builder();
        KEY_FRAME_ATTRIBUTES.parse(env, parser, builder);

        if (variantIdName != null) {
            Variant variant = parent.getVariant(variantIdName);
            if (variant != null) {
                builder.setVariant(variant);
            }
        }

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, KEY_FRAME_TAG);
        return builder.build();
    }
}
