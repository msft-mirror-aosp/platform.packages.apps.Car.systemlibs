/*
 * Copyright (C) 2025 The Android Open Source Project
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
package com.android.car.scalableui.loader.xml;

import static android.view.Display.DEFAULT_DISPLAY;

import static com.android.car.scalableui.loader.xml.HunTagXmlParserKt.GRAVITY_TAG;
import static com.android.car.scalableui.loader.xml.HunTagXmlParserKt.getVariantGravityParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.ALPHA_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.BOUNDS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.CORNER_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.DEFAULT_VARIANT_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.DISPLAY_ID;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.ID_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.INSETS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.KEY_FRAME_VARIANT_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.TRANSITIONS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.VARIANT_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.VISIBILITY_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getDisplayMetricsForDisplay;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getIdName;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantAlphaParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantCornerParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantInsetsParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantVisibilityParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseBounds;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseKeyFrameVariant;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseTransitions;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseVariant;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;
import com.android.car.scalableui.model.Transition;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A utility class that uses a {@link XmlPullParser} to create a {@link PanelState} object for
 * <SystemBar></SystemBar> block.
 */
public class SystemBarTagXmlParser {
    public static final String HIDE_FOR_KEYBOARD_ATTRIBUTE = "hideForKeyboard";
    public static final String BAR_Z_ORDER_ATTRIBUTE = "barZOrder";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String SYSTEM_BAR_TAG = "SystemBar";
    public static final String TYPE_STATUS = "status";
    public static final String TYPE_NAVIGATION = "navigation";
    private static final String TAG = SystemBarTagXmlParser.class.getSimpleName();

    private static VariantPropertyParser getVariantSystemBarBoundsParser() {
        return (context, parser, builder, displayId) -> {
            Bounds bounds = parseBounds(context, parser, displayId);
            DisplayMetrics displayMetrics = getDisplayMetricsForDisplay(context, displayId);
            Rect rect = bounds.getRect();
            if (rect.left == 0 || rect.top == 0 || rect.right == displayMetrics.widthPixels
                    || rect.bottom == displayMetrics.heightPixels) {
                return builder.setBounds(bounds.getRect());
            } else {
                throw new IllegalStateException(
                        "<SystemBar> <Variant> <Bounds> property must touch edge of display");
            }
        };
    }

    static PanelState parseSystemBar(@NonNull Context context, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, SYSTEM_BAR_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE);
        String displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID);
        int displayId = (displayIdStr == null) ? DEFAULT_DISPLAY : Integer.parseInt(displayIdStr);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        if (id == null) {
            throw new XmlPullParserException("<SystemBar> type property must have an id defined");
        }
        String idName = getIdName(context, id);

        String typeString = attrs.getAttributeValue(null, TYPE_ATTRIBUTE)
                .toLowerCase(Locale.ROOT);
        if (!TYPE_STATUS.equals(typeString) && !TYPE_NAVIGATION.equals(typeString)) {
            throw new XmlPullParserException(
                    "<SystemBar> type property must be status or navigation");
        }

        int zOrder = attrs.getAttributeIntValue(null, BAR_Z_ORDER_ATTRIBUTE, -1);
        if (zOrder < 0) {
            throw new XmlPullParserException(
                    "<SystemBar> barZOrder property must be a positive integer");
        }

        boolean hideForKeyboard = attrs.getAttributeBooleanValue(null, HIDE_FOR_KEYBOARD_ATTRIBUTE,
                false);
        Bundle bundle = new Bundle();
        bundle.putBoolean(HIDE_FOR_KEYBOARD_ATTRIBUTE, hideForKeyboard);
        bundle.putString(TYPE_ATTRIBUTE, typeString);

        for (int index = 0; index < attrs.getAttributeCount(); index++) {
            String name = attrs.getAttributeName(index);
            switch (name) {
                case ID_ATTRIBUTE, DEFAULT_VARIANT_ATTRIBUTE, HIDE_FOR_KEYBOARD_ATTRIBUTE,
                     TYPE_ATTRIBUTE -> {
                    // no-op
                }
                case BAR_Z_ORDER_ATTRIBUTE -> bundle.putInt(BAR_Z_ORDER_ATTRIBUTE, zOrder);
                default -> {
                    String value = attrs.getAttributeValue(index);
                    bundle.putString(name, value);
                }
            }
        }
        PanelControllerMetadata panelControllerMetaData = new PanelControllerMetadata(bundle);

        PanelState.Builder builder = new PanelState.Builder(idName, PanelType.SYSTEM_BAR);
        builder.setDisplayId(displayId);
        builder.setDefaultVariant(defaultVariant);
        builder.setPanelControllerMetadata(panelControllerMetaData);
        PanelState panelState = builder.build();

        Map<String, VariantPropertyParser> variantParserMap = Map.of(
                VISIBILITY_TAG, getVariantVisibilityParser(),
                ALPHA_TAG, getVariantAlphaParser(),
                BOUNDS_TAG, getVariantSystemBarBoundsParser(),
                CORNER_TAG, getVariantCornerParser(),
                INSETS_TAG, getVariantInsetsParser(),
                GRAVITY_TAG, getVariantGravityParser());

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VARIANT_TAG -> panelState.addVariant(parseVariant(context, panelState,
                        /* defaultLayer= */ null, parser, variantParserMap, displayId));
                case KEY_FRAME_VARIANT_TAG -> panelState.addVariant(
                        parseKeyFrameVariant(panelState, parser, context));
                case TRANSITIONS_TAG -> {
                    List<Transition> transitions = parseTransitions(context, displayId, panelState,
                            parser);
                    for (Transition transition : transitions) {
                        panelState.addTransition(transition);
                    }
                }
                default -> XmlPullParserHelper.skip(parser);
            }
        }
        panelState.setVariant(defaultVariant); // Set the initial variant
        return panelState;
    }
}
