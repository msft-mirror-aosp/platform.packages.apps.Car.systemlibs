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

import static com.android.car.scalableui.loader.xml.parser.ParserUtils.getIdName;

import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** Parser for {@link Variant} elements. */
public class VariantParser implements XmlChildParser<PanelState> {
    private static final String TAG = VariantParser.class.getSimpleName();
    public static final String VARIANT_TAG = "Variant";
    public static final String ID_ATTRIBUTE = "id";
    public static final String PARENT_ATTRIBUTE = "parent";

    private static final AttributeMap<Variant.Builder> ATTRIBUTES =
            new AttributeMap.Builder<Variant.Builder>()
                    .add(
                            PARENT_ATTRIBUTE,
                            (env, value, builder) ->
                                    builder.setParentId(getIdName(env.getContext(), value)))
                    .addFloat(AlphaParser.ALPHA_VALUE_ATTRIBUTE, Variant.Builder::setAlpha)
                    .addBoolean(
                            VisibilityParser.IS_VISIBLE_ATTRIBUTE, Variant.Builder::setVisibility)
                    .addInteger(LayerParser.LAYER_VALUE_ATTRIBUTE, Variant.Builder::setLayer)
                    .addBoolean(
                            FocusParser.FOCUS_ON_TRANSITION_ATTRIBUTE,
                            Variant.Builder::setCanFocusOnTransition)
                    .build();

    /** Parses a {@link Variant} from XML using default builder. */
    @Override
    public void parse(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws XmlPullParserException, IOException {
        parent.addVariant(
                parseVariant(
                        env,
                        parent,
                        Layer.DEFAULT_LAYER,
                        parser,
                        parent.getDisplayId(),
                        Variant.Builder::new,
                        Collections.emptyMap()));
    }

    /** Parses a {@link Variant} from XML using default builder. */
    @NonNull
    public static Variant parseVariant(
            @NonNull ParserEnv env,
            @NonNull PanelState panelState,
            @Nullable Integer defaultLayer,
            @NonNull XmlPullParser parser,
            int displayId)
            throws IOException, XmlPullParserException {
        return parseVariant(
                env,
                panelState,
                defaultLayer,
                parser,
                displayId,
                Variant.Builder::new,
                Collections.emptyMap());
    }

    /** Parses a {@link Variant} from XML using a factory. */
    @NonNull
    @SuppressWarnings("unchecked")
    public static Variant parseVariant(
            @NonNull ParserEnv env,
            @NonNull PanelState panelState,
            @Nullable Integer defaultLayer,
            @NonNull XmlPullParser parser,
            int displayId,
            @NonNull VariantBuilderFactory variantBuilderFactory,
            @NonNull Map<String, XmlChildParser<Variant.Builder>> customParsers)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String idName = ParserUtils.getIdName(env.getContext(), id);

        Variant.Builder variantBuilder = variantBuilderFactory.create(id, idName);
        variantBuilder.setLayer(defaultLayer);
        variantBuilder.setPanelId(panelState.getId());

        ATTRIBUTES.parse(env, parser, variantBuilder);

        ParserEnv childEnv = env.withDisplayId(displayId);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            XmlChildParser customParser = customParsers.get(name);
            if (customParser != null) {
                ((XmlChildParser<Variant.Builder>) customParser)
                        .parse(childEnv, parser, variantBuilder);
            } else {
                XmlChildParser childParser =
                        childEnv.getRegistry().getChildParser(variantBuilder.getClass(), name);
                if (childParser != null) {
                    ((XmlChildParser<Variant.Builder>) childParser)
                            .parse(childEnv, parser, variantBuilder);
                } else {
                    Log.w(TAG, "Unsupported Variant Tag: " + name);
                    XmlPullParserHelper.throwIfUnknownTag(parser);
                }
            }
        }
        return variantBuilder.build(panelState);
    }
}
