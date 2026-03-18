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

import android.util.AttributeSet;
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

/** Parser for the &lt;BreakPoints&gt; tag in a {@link PanelControllerMetadata}. */
public class BreakPointsParser implements XmlChildParser<PanelControllerMetadata.Builder> {
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
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull PanelControllerMetadata.Builder builder)
            throws XmlPullParserException, IOException {
        BreakpointParsingState breakpointParsingState = new BreakpointParsingState();
        BREAKPOINTS_ATTRIBUTES.parse(env, parser, breakpointParsingState);
        builder.addBreakPoints(parseBreakPoints(env, parser, breakpointParsingState));
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
