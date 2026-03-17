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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.BreakPoint;
import com.android.car.scalableui.model.PanelControllerMetadata;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Parser for {@link PanelControllerMetadata} elements. */
public class PanelControllerParser implements TagParser<PanelControllerMetadata> {
    private static final String TAG = PanelControllerParser.class.getSimpleName();

    public static final String CONTROLLER_TAG = "Controller";
    public static final String CONTROLLER_NAME_TAG = "ControllerName";
    public static final String VIEW_TAG = "View";
    public static final String EVENT_ID_TAG = "EventId";
    public static final String OVERLAY_PANEL_ID_TAG = "OverlayPanelId";
    public static final String BACKGROUND_COLOR_TAG = "BackgroundColor";
    public static final String ORIENTATION_TAG = "Orientation";
    public static final String SNAPTHREADHOLD_TAG = "SnapThreadhold";
    public static final String PERSISTENT_ACTIVITY_TAG = "PersistentActivity";
    public static final String PERSISTENT_ACTIVITY_LIST_TAG = "PersistentActivityList";
    public static final String PERSISTENT_PACKAGE_TAG = "PersistentPackage";
    public static final String DEFAULT_COMPONENT_TAG = "DefaultComponent";
    public static final String DEFAULT_INTENT_TAG = "DefaultIntent";
    public static final String UPDATABLE_INTENT_FILTER_TAG = "UpdateIntentFilter";
    public static final String TASK_TOOLBAR_CONTROLLER_TAG = "TaskToolBarController";

    public static final String BREAKPOINTS_TAG = "BreakPoints";
    public static final String BREAKPOINTS_ORIENTATION_TAG = "orientation";
    public static final String BREAKPOINT_TAG = "BreakPoint";
    public static final String BREAKPOINT_POINT_TAG = "point";
    public static final String BREAKPOINT_EVENT_ID_TAG = "eventId";

    private static class BreakpointParsingState {
        int mOrientation;
    }

    private static final AttributeMap<BreakpointParsingState> BREAKPOINTS_ATTRIBUTES =
            AttributeMap.<BreakpointParsingState>builder()
                    .addInteger(
                            BREAKPOINTS_ORIENTATION_TAG,
                            (state, value) -> state.mOrientation = value)
                    .build();

    @Override
    public PanelControllerMetadata parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return parseController(env, parser);
    }

    /** Parses a {@link PanelControllerMetadata} from a XML parser. */
    private static PanelControllerMetadata parseController(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT
                || (eventType == XmlPullParser.TEXT && parser.isWhitespace())) {
            eventType = parser.next();
        }
        if (eventType != XmlPullParser.START_TAG || !parser.getName().equals(CONTROLLER_TAG)) {
            throw new XmlPullParserException(
                    "Expected <Controller> tag at the beginning but " + parser.getName());
        }

        parser.require(XmlPullParser.START_TAG, null, CONTROLLER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, VariantParser.ID_ATTRIBUTE);
        PanelControllerMetadata.Builder builder = PanelControllerMetadata.builder(id);
        ValueParser valueParser = env.getValueParser();
        Context androidContext = env.getContext();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            String value;
            BreakpointParsingState breakpointParsingState = new BreakpointParsingState();
            switch (name) {
                case BREAKPOINTS_TAG -> {
                    BREAKPOINTS_ATTRIBUTES.parse(env, parser, breakpointParsingState);
                    builder.addBreakPoints(parseBreakPoints(env, parser, breakpointParsingState));
                }
                case CONTROLLER_NAME_TAG,
                        VIEW_TAG,
                        EVENT_ID_TAG,
                        OVERLAY_PANEL_ID_TAG,
                        BACKGROUND_COLOR_TAG,
                        ORIENTATION_TAG,
                        SNAPTHREADHOLD_TAG,
                        PERSISTENT_PACKAGE_TAG,
                        UPDATABLE_INTENT_FILTER_TAG,
                        TASK_TOOLBAR_CONTROLLER_TAG,
                        DEFAULT_COMPONENT_TAG,
                        DEFAULT_INTENT_TAG,
                        PERSISTENT_ACTIVITY_TAG -> {
                    String rawValue = XmlPullParserHelper.readText(parser);
                    value = valueParser.parseString(androidContext, rawValue);
                    if (value != null) {
                        builder.addConfiguration(name, value);
                    } else {
                        Log.e(TAG, "No value for Controller Tag: " + name);
                    }
                }
                case PERSISTENT_ACTIVITY_LIST_TAG -> {
                    String rawList = XmlPullParserHelper.readText(parser);
                    List<String> list = valueParser.parseStringArray(androidContext, rawList);
                    for (String stringValue : list) {
                        builder.addConfiguration(PERSISTENT_ACTIVITY_TAG, stringValue);
                    }
                }
                default -> {
                    Log.w(TAG, "Unsupported Controller Tag: " + name);
                    XmlPullParserHelper.throwIfUnknownTag(parser);
                }
            }
        }
        return builder.build();
    }

    /** Parses a list of {@link BreakPoint} from XML. */
    private static List<BreakPoint> parseBreakPoints(ParserEnv env, XmlPullParser parser,
            BreakpointParsingState state)
            throws IOException, XmlPullParserException {
        List<BreakPoint> points = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(BREAKPOINT_TAG)) {
                points.add(parseBreakPoint(env, parser, state));
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
        return points;
    }

    /** Parses a {@link BreakPoint} from XML. */
    private static BreakPoint parseBreakPoint(@NonNull ParserEnv env, @NonNull XmlPullParser parser,
            BreakpointParsingState state)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, BREAKPOINT_TAG);
        boolean isHorizontal = state.mOrientation == 1;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Integer point =
                ParserUtils.getDimensionPixelSize(
                        env.getContext(), attrs, BREAKPOINT_POINT_TAG, env.getDisplayId(),
                        isHorizontal);
        String eventId = attrs.getAttributeValue(null, BREAKPOINT_EVENT_ID_TAG);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, BREAKPOINT_TAG);

        return new BreakPoint.Builder(point, eventId).build();
    }
}
