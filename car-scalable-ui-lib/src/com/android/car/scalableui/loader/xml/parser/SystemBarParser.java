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

import static android.view.Display.DEFAULT_DISPLAY;

import static com.android.car.scalableui.loader.xml.parser.BoundsParser.BOUNDS_TAG;
import static com.android.car.scalableui.loader.xml.parser.PanelParser.DEFAULT_VARIANT_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.parser.PanelParser.DISPLAY_ID;
import static com.android.car.scalableui.loader.xml.parser.ParserUtils.getIdName;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.GravityVariant;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Parsers {@link PanelState} from XML for SystemBar tags. */
public class SystemBarParser implements TagParser<PanelState> {
    public static final String BAR_Z_ORDER_ATTRIBUTE = "barZOrder";
    public static final String HIDE_FOR_KEYBOARD_ATTRIBUTE = "hideForKeyboard";
    public static final String SYSTEM_BAR_TAG = "SystemBar";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String TYPE_NAVIGATION = "navigation";
    public static final String TYPE_STATUS = "status";

    private static class SystemBarParsingState {
        final PanelState.Builder mBuilder;
        final Bundle mBundle;
        Integer mDefaultLayer;

        SystemBarParsingState(PanelState.Builder builder, Bundle bundle) {
            mBuilder = builder;
            mBundle = bundle;
        }
    }

    private static final AttributeMap<SystemBarParsingState> ATTRIBUTES =
            AttributeMap.<SystemBarParsingState>builder()
                    .add(
                            DEFAULT_VARIANT_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBuilder.setDefaultVariant(
                                            getIdName(env.getContext(), value)))
                    .add(
                            PanelParser.DEFAULT_LAYER_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mDefaultLayer =
                                            env.getValueParser()
                                                    .parseInteger(
                                                            env.getContext(),
                                                            value,
                                                            Layer.DEFAULT_LAYER))
                    .addBoolean(
                            HIDE_FOR_KEYBOARD_ATTRIBUTE,
                            (state, value) ->
                                    state.mBundle.putBoolean(HIDE_FOR_KEYBOARD_ATTRIBUTE, value))
                    .add(
                            BAR_Z_ORDER_ATTRIBUTE,
                            (env, value, state) -> {
                                int zOrder =
                                        env.getValueParser()
                                                .parseInteger(env.getContext(), value, -1);
                                if (zOrder < 0) {
                                    throw new XmlPullParserException(
                                            "<SystemBar> barZOrder must be a positive integer");
                                }
                                state.mBundle.putInt(BAR_Z_ORDER_ATTRIBUTE, zOrder);
                            })
                    .addString(
                            TYPE_ATTRIBUTE,
                            (state, value) -> {
                                String type = value != null ? value.toLowerCase(Locale.ROOT) : null;
                                if (!TYPE_STATUS.equals(type) && !TYPE_NAVIGATION.equals(type)) {
                                    throw new IllegalArgumentException(
                                            "<SystemBar> type property must be status or"
                                                    + " navigation");
                                }
                                state.mBundle.putString(TYPE_ATTRIBUTE, type);
                            })
                    // Ignore ID and DISPLAY_ID as they are handled before map parsing
                    .add(VariantParser.ID_ATTRIBUTE, (env, value, state) -> {})
                    .add(DISPLAY_ID, (env, value, state) -> {})
                    .build();

    @NonNull
    @Override
    public PanelState parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Context androidContext = env.getContext();
        parser.require(XmlPullParser.START_TAG, null, SYSTEM_BAR_TAG);

        String id =
                env.getValueParser()
                        .parseString(
                                androidContext,
                                parser.getAttributeValue(null, VariantParser.ID_ATTRIBUTE));
        if (id == null) {
            throw new XmlPullParserException("<SystemBar> type property must have an id defined");
        }
        String idName = getIdName(androidContext, id);

        String displayIdStr = parser.getAttributeValue(null, DISPLAY_ID);
        int displayId =
                env.getValueParser().parseInteger(androidContext, displayIdStr, DEFAULT_DISPLAY);

        // Update context with the parsed displayId.
        env = env.withDisplayId(displayId);

        PanelState.Builder builder = new PanelState.Builder(idName, PanelType.SYSTEM_BAR);
        builder.setDisplayId(displayId);

        Bundle bundle = new Bundle();
        SystemBarParsingState state = new SystemBarParsingState(builder, bundle);

        ATTRIBUTES.parse(env, parser, state);

        builder.setPanelControllerMetadata(new PanelControllerMetadata(state.mBundle));

        PanelState panelState = builder.build();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (VariantParser.VARIANT_TAG.equals(name)) {
                panelState.addVariant(
                        parseSystemBarVariant(env, parser, panelState, state.mDefaultLayer));
                continue;
            }

            XmlChildParser<PanelState> childParser =
                    env.getRegistry().getChildParser(PanelState.class, name);
            if (childParser != null) {
                childParser.parse(env, parser, panelState);
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
        panelState.resolvePendingTransitions();
        panelState.resetVariant();
        return panelState;
    }

    private Variant parseSystemBarVariant(
            ParserEnv env, XmlPullParser parser, PanelState panelState, Integer defaultLayer)
            throws XmlPullParserException, IOException {
        int displayId = panelState.getDisplayId();

        Map<String, XmlChildParser<Variant.Builder>> customParsers = new HashMap<>();
        customParsers.put(
                BOUNDS_TAG,
                (childEnv, childParser, builder) -> {
                    TagParser<Bounds> boundsParser =
                            childEnv.getRegistry().<Bounds>getParser(BOUNDS_TAG);
                    Bounds bounds = boundsParser.parseTag(childEnv, childParser);
                    DisplayMetrics displayMetrics =
                            ParserUtils.getDisplayMetricsForDisplay(
                                    childEnv.getContext(), childEnv.getDisplayId());
                    Rect rect = bounds.getRect();
                    if (rect.left == 0
                            || rect.top == 0
                            || rect.right == displayMetrics.widthPixels
                            || rect.bottom == displayMetrics.heightPixels) {
                        builder.setBounds(bounds.getRect());
                    } else {
                        throw new IllegalStateException(
                                "<SystemBar> <Variant> <Bounds> property must touch edge of"
                                    + " display");
                    }
                });

        return VariantParser.parseVariant(
                env,
                panelState,
                defaultLayer,
                parser,
                displayId,
                (id, idName) -> new GravityVariant.Builder(id, idName),
                customParsers);
    }
}
