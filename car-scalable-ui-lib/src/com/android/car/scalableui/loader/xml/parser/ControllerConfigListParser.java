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
import com.android.car.scalableui.model.PanelControllerMetadata;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/** Parser for generic list configurations in a {@link PanelControllerMetadata}. */
public class ControllerConfigListParser implements XmlChildParser<PanelControllerMetadata.Builder> {

    private final String mConfigurationKey;

    public ControllerConfigListParser(@NonNull String configurationKey) {
        mConfigurationKey = configurationKey;
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull PanelControllerMetadata.Builder builder)
            throws XmlPullParserException, IOException {
        ValueParser valueParser = env.getValueParser();
        String rawList = XmlPullParserHelper.readText(parser);
        List<String> list = valueParser.parseStringArray(env.getContext(), rawList);
        if (list != null) {
            for (String stringValue : list) {
                builder.addConfiguration(mConfigurationKey, stringValue);
            }
        }
    }
}
