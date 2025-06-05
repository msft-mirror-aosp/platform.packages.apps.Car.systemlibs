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

import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.ALPHA_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.BOUNDS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.CORNER_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.DISPLAY_ID;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.DEFAULT_VARIANT_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.ID_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.INSETS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.KEY_FRAME_VARIANT_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.TRANSITIONS_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.VARIANT_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.VISIBILITY_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getDimensionPixelSize;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getDisplayMetricsForDisplay;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantAlphaParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantCornerParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantInsetsParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantVisibilityParser;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseKeyFrameVariant;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseTransitions;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseVariant;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Transition;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class that uses a {@link XmlPullParser} to create a {@link PanelState} object for
 * <SystemBar></SystemBar> block.
 */
public class SystemBarTagXmlParser {
    public static final String SIDE_ATTRIBUTE = "side";
    public static final String BAR_Z_ORDER_ATTRIBUTE = "barZOrder";
    public static final String GIRTH_ATTRIBUTE = "girth";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String SYSTEM_BAR_TAG = "SystemBar";
    private static final String TAG = SystemBarTagXmlParser.class.getSimpleName();
    private static final String SYSTEM_BAR_PANEL_ID_PREFIX = "_System_Bar_Panel_";
    private static final String SYSTEM_BAR_PANEL_TOP_ID_SUFFIX = "Top";
    public static final String SYSTEM_BAR_PANEL_TOP_ID =
            SYSTEM_BAR_PANEL_ID_PREFIX + SYSTEM_BAR_PANEL_TOP_ID_SUFFIX;
    private static final String SYSTEM_BAR_PANEL_BOTTOM_ID_SUFFIX = "Bottom";
    public static final String SYSTEM_BAR_PANEL_BOTTOM_ID =
            SYSTEM_BAR_PANEL_ID_PREFIX + SYSTEM_BAR_PANEL_BOTTOM_ID_SUFFIX;
    private static final String SYSTEM_BAR_PANEL_LEFT_ID_SUFFIX = "Left";
    public static final String SYSTEM_BAR_PANEL_LEFT_ID =
            SYSTEM_BAR_PANEL_ID_PREFIX + SYSTEM_BAR_PANEL_LEFT_ID_SUFFIX;
    private static final String SYSTEM_BAR_PANEL_RIGHT_ID_SUFFIX = "Right";
    public static final String SYSTEM_BAR_PANEL_RIGHT_ID =
            SYSTEM_BAR_PANEL_ID_PREFIX + SYSTEM_BAR_PANEL_RIGHT_ID_SUFFIX;

    static PanelState parseSystemBar(@NonNull Context context, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, SYSTEM_BAR_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID);
        int displayId = (displayIdStr == null) ? DEFAULT_DISPLAY : Integer.parseInt(displayIdStr);
        String defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE);
        String side = attrs.getAttributeValue(null, SIDE_ATTRIBUTE);
        int type = attrs.getAttributeIntValue(null, TYPE_ATTRIBUTE, -1);
        if (type < 0 || type > 3) {
            throw new XmlPullParserException("<SystemBar> type property must be between 0 and 3");
        }
        int zOrder = attrs.getAttributeIntValue(null, BAR_Z_ORDER_ATTRIBUTE, -1);
        if (zOrder < 0) {
            throw new XmlPullParserException(
                    "<SystemBar> barZOrder property must be a positive integer");
        }
        String id = getIdForSide(side);
        Bundle bundle = new Bundle();

        for (int index = 0; index < attrs.getAttributeCount(); index++) {
            String name = attrs.getAttributeName(index);
            switch (name) {
                case ID_ATTRIBUTE -> throw new XmlPullParserException(
                        "<SystemBar> does not support attribute: " + ID_ATTRIBUTE);
                case DEFAULT_VARIANT_ATTRIBUTE -> {
                    // no-op
                }
                case TYPE_ATTRIBUTE -> {
                    bundle.putInt(TYPE_ATTRIBUTE, type);
                }
                case BAR_Z_ORDER_ATTRIBUTE -> {
                    bundle.putInt(BAR_Z_ORDER_ATTRIBUTE, zOrder);
                }
                default -> {
                    String value = attrs.getAttributeValue(index);
                    bundle.putString(name, value);
                }
            }
        }
        PanelControllerMetadata panelControllerMetaData = new PanelControllerMetadata(bundle);

        PanelState.Builder builder = new PanelState.Builder(id);
        builder.setDisplayId(displayId);
        builder.setDefaultVariant(defaultVariant);
        builder.setPanelControllerMetadata(panelControllerMetaData);
        PanelState panelState = builder.build();

        Map<String, VariantPropertyParser> variantParserMap = new HashMap<>();
        variantParserMap.put(VISIBILITY_TAG, getVariantVisibilityParser());
        variantParserMap.put(ALPHA_TAG, getVariantAlphaParser());
        variantParserMap.put(BOUNDS_TAG, getVariantSystemBarBoundsParser(id, displayId));
        variantParserMap.put(CORNER_TAG, getVariantCornerParser(displayId));
        variantParserMap.put(INSETS_TAG, getVariantInsetsParser(displayId));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VARIANT_TAG:
                    panelState.addVariant(parseVariant(context, panelState,
                            /* defaultLayer= */ null, parser, variantParserMap));
                    break;
                case KEY_FRAME_VARIANT_TAG:
                    panelState.addVariant(parseKeyFrameVariant(panelState, parser));
                    break;
                case TRANSITIONS_TAG:
                    List<Transition> transitions = parseTransitions(context, displayId, panelState,
                            parser);
                    for (Transition transition : transitions) {
                        panelState.addTransition(transition);
                    }
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
            }
        }
        panelState.setVariant(defaultVariant); // Set the initial variant
        return panelState;
    }

    @NonNull
    private static String getIdForSide(String side) throws XmlPullParserException {
        String id;
        if (side == null) {
            throw new XmlPullParserException("<SystemBar> requires attribute side");
        } else if (side.equalsIgnoreCase(SYSTEM_BAR_PANEL_TOP_ID_SUFFIX)) {
            id = SYSTEM_BAR_PANEL_TOP_ID;
        } else if (side.equalsIgnoreCase(SYSTEM_BAR_PANEL_BOTTOM_ID_SUFFIX)) {
            id = SYSTEM_BAR_PANEL_BOTTOM_ID;
        } else if (side.equalsIgnoreCase(SYSTEM_BAR_PANEL_LEFT_ID_SUFFIX)) {
            id = SYSTEM_BAR_PANEL_LEFT_ID;
        } else if (side.equalsIgnoreCase(SYSTEM_BAR_PANEL_RIGHT_ID_SUFFIX)) {
            id = SYSTEM_BAR_PANEL_RIGHT_ID;
        } else {
            throw new XmlPullParserException(
                    "<SystemBar>'s side attribute supports top|bottom|left|right values");
        }
        return id;
    }

    private static int getLeftForId(String id, DisplayMetrics displayMetrics, Integer girth) {
        // Since right system bar encapsulates right edge of screen, its left value depends on girth
        if (id.equals(SYSTEM_BAR_PANEL_RIGHT_ID)) {
            return displayMetrics.widthPixels - girth;
        } else {
            return 0;
        }
    }

    private static Integer getRightForId(String id, DisplayMetrics displayMetrics, Integer girth) {
        // Since left system bar encapsulates left edge of screen, its right value depends on girth
        if (id.equals(SYSTEM_BAR_PANEL_LEFT_ID)) {
            return girth;
        } else {
            return displayMetrics.widthPixels;
        }
    }

    private static Integer getTopForId(String id, DisplayMetrics displayMetrics, Integer girth) {
        // Since bottom system bar encapsulates bottom edge of screen, its top value depends on
        // girth
        if (id.equals(SYSTEM_BAR_PANEL_BOTTOM_ID)) {
            return displayMetrics.heightPixels - girth;
        } else {
            return 0;
        }
    }

    private static Integer getBottomForId(String id, DisplayMetrics displayMetrics, Integer girth) {
        // Since top system bar encapsulates top edge of screen, its bottom value depends on girth
        if (id.equals(SYSTEM_BAR_PANEL_TOP_ID)) {
            return girth;
        } else {
            return displayMetrics.heightPixels;
        }
    }

    private static VariantPropertyParser getVariantSystemBarBoundsParser(String id, int displayId) {
        return (context, parser, builder) -> builder.setBounds(
                parseSystemBarBounds(context, parser, id, displayId).getRect());
    }

    @NonNull
    private static Bounds parseSystemBarBounds(@NonNull Context context,
            @NonNull XmlPullParser parser, @NonNull String id, int displayId)
            throws IOException, XmlPullParserException {
        if (XmlPullParser.START_TAG != parser.getEventType() || !BOUNDS_TAG.equals(
                parser.getName())) {
            throw new XmlPullParserException(
                    "parseSystemBarBounds called with wrong parser event type: "
                            + parser.getEventType() + " or name: " + parser.getName());
        }
        AttributeSet attrs = Xml.asAttributeSet(parser);
        DisplayMetrics displayMetrics = getDisplayMetricsForDisplay(context, displayId);

        Integer girth = getDimensionPixelSize(context, attrs, GIRTH_ATTRIBUTE, displayId,
                id.equals(SYSTEM_BAR_PANEL_TOP_ID) || id.equals(SYSTEM_BAR_PANEL_BOTTOM_ID));
        if (girth == null) {
            throw new XmlPullParserException(
                    "<SystemBar> <Variant> <Bounds> must have girth defined");
        }

        Integer left = getLeftForId(id, displayMetrics, girth);
        Integer top = getTopForId(id, displayMetrics, girth);
        Integer right = getRightForId(id, displayMetrics, girth);
        Integer bottom = getBottomForId(id, displayMetrics, girth);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Bounds.Builder()
                .setLeft(left)
                .setTop(top)
                .setRight(right)
                .setBottom(bottom)
                .build();
    }
}
