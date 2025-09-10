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

import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.PANEL_TAG;
import static com.android.car.scalableui.loader.xml.PanelTagXmlParser.parsePanel;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.SYSTEM_BAR_TAG;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.parseSystemBar;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.car.scalableui.model.PanelState;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A utility class that uses a {@link XmlPullParser} to create a {@link PanelState} object.
 */
public class PanelStateXmlParser {
    private static final String TAG = PanelStateXmlParser.class.getSimpleName();

    @NonNull
    static PanelState parse(@NonNull Context context, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Consume any START_DOCUMENT or whitespace events
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT || (eventType == XmlPullParser.TEXT
                && parser.isWhitespace())) {
            eventType = parser.next();
        }

        if (eventType != XmlPullParser.START_TAG) {
            throw new XmlPullParserException(
                    "Unrecognized tag at the beginning: " + parser.getName());
        } else if (parser.getName().equals(PANEL_TAG)) {
            return parsePanel(context, parser);
        } else if (parser.getName().equals(SYSTEM_BAR_TAG)) {
            return parseSystemBar(context, parser);
        } else {
            throw new XmlPullParserException(
                    "Unrecognized tag at the beginning: " + parser.getName());
        }
    }
}
