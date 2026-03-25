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

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.Display;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parsers for TaskPanel and DecorPanel. */
public class PanelParser implements TagParser<PanelState> {

    private static final String TAG = "PanelParser";
    public static final String TASK_PANEL_TAG = "TaskPanel";
    public static final String DECOR_PANEL_TAG = "DecorPanel";
    public static final String DEFAULT_VARIANT_ATTRIBUTE = "defaultVariant";
    public static final String ROLE_ATTRIBUTE = "role";
    public static final String DISPLAY_ID = "displayId";
    public static final String DEFAULT_LAYER_ATTRIBUTE = "defaultLayer";
    public static final String CONTROLLER = "controller";

    public PanelParser() {}

    private static class PanelParsingState {
        final PanelState.Builder mBuilder;
        Integer mDefaultLayer;

        PanelParsingState(PanelState.Builder builder) {
            mBuilder = builder;
        }
    }

    private static final AttributeMap<PanelParsingState> ATTRIBUTES =
            AttributeMap.<PanelParsingState>builder()
                    .add(
                            DEFAULT_VARIANT_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBuilder.setDefaultVariant(
                                            getIdName(env.getContext(), value)))
                    .add(
                            DEFAULT_LAYER_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mDefaultLayer =
                                            env.getValueParser()
                                                    .parseInteger(
                                                            env.getContext(),
                                                            value,
                                                            Layer.DEFAULT_LAYER))
                    .add(
                            ROLE_ATTRIBUTE,
                            (env, value, state) -> {
                                int roleValue =
                                        env.getValueParser()
                                                .parseResourceId(
                                                        env.getContext(), value, View.NO_ID);
                                if (roleValue != View.NO_ID) {
                                    state.mBuilder.setRole(
                                            RoleParser.parseRole(
                                                    env.getContext(),
                                                    roleValue,
                                                    state.mBuilder.getId()));
                                }
                            })
                    .add(
                            CONTROLLER,
                            (env, value, state) -> {
                                XmlPullParser xmlParser =
                                        env.getValueParser().parseXml(env.getContext(), value);

                                if (xmlParser != null) {
                                    TagParser<PanelControllerMetadata> parser = env.getRegistry()
                                            .getParser(PanelControllerParser.CONTROLLER_TAG);
                                    try {
                                        state.mBuilder.setPanelControllerMetadata(
                                                parser.parseTag(env, xmlParser));
                                    } catch (XmlPullParserException | IOException e) {
                                        throw new RuntimeException("Failed to parse controller", e);
                                    } finally {
                                        if (xmlParser instanceof XmlResourceParser) {
                                            ((XmlResourceParser) xmlParser).close();
                                        }
                                    }
                                } else {
                                    String controllerStr =
                                            env.getValueParser()
                                                    .parseString(env.getContext(), value);
                                    if (controllerStr != null) {
                                        state.mBuilder.setPendingControllerId(
                                                getIdName(env.getContext(), controllerStr));
                                    }
                                }
                            })
                    .build();

    @NonNull
    @Override
    public PanelState parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String tagName = parser.getName();
        int panelType;
        if (TASK_PANEL_TAG.equals(tagName)) {
            panelType = PanelType.TASK;
        } else if (DECOR_PANEL_TAG.equals(tagName)) {
            panelType = PanelType.DECOR;
        } else {
            throw new XmlPullParserException("Invalid tag: " + tagName);
        }

        Context androidContext = env.getContext();

        // ID is required for Builder
        String id =
                env.getValueParser()
                        .parseString(
                                androidContext,
                                parser.getAttributeValue(null, VariantParser.ID_ATTRIBUTE));
        if (id == null) {
            throw new XmlPullParserException("<" + tagName + "> must have an id defined");
        }
        String idName = getIdName(androidContext, id);

        // Display ID is required for Context update
        String displayIdStr = parser.getAttributeValue(null, DISPLAY_ID);
        int displayId =
                env.getValueParser()
                        .parseInteger(androidContext, displayIdStr, Display.DEFAULT_DISPLAY);

        // Update context with the parsed displayId.
        env = env.withDisplayId(displayId);

        PanelState.Builder builder = new PanelState.Builder(idName, panelType);
        builder.setDisplayId(displayId);

        PanelParsingState state = new PanelParsingState(builder);
        ATTRIBUTES.parse(env, parser, state);

        PanelState panelState = builder.build();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (VariantParser.VARIANT_TAG.equals(name)) {
                panelState.addVariant(
                        VariantParser.parseVariant(
                                env,
                                panelState,
                                state.mDefaultLayer,
                                parser,
                                displayId));
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
}
