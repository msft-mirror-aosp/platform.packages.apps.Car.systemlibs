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

package com.android.car.datasubscription;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.android.car.datasubscription.DataSubscriptionConfig.DataSubscriptionStatusType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Config parser for data subscription statuses and messages
 */
public class DataSubscriptionConfigParser {
    private static final String TAG = "DataSubscriptionConfigParser";
    private static final String DATA_SUBSCRIPTION_STATUS = "status";
    private static final String DATA_SUBSCRIPTION_ID = "id";
    private static final String DATA_SUBSCRIPTION_TYPE = "type";
    private static final String DATA_SUBSCRIPTION_PROACTIVE_MESSAGE = "proactiveMessage";
    private static final String DATA_SUBSCRIPTION_REACTIVE_MESSAGE = "reactiveMessage";
    private static final String DATA_SUBSCRIPTION_USE_DATE = "useDate";


    /**
     * Returns a map for the all data subscription status configs
     */
    public static Map<Integer, DataSubscriptionConfig> parseConfig(XmlResourceParser parser) {
        Map<Integer, DataSubscriptionConfig> configData = new HashMap<>();
        try (parser) {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG
                            && DATA_SUBSCRIPTION_STATUS.equals(tagName)) {
                    int id = Integer.parseInt(parser.getAttributeValue(/* namespace = */ null,
                            DATA_SUBSCRIPTION_ID));
                    DataSubscriptionStatusType type = DataSubscriptionStatusType.fromValue(
                            parser.getAttributeValue(/* namespace = */ null,
                                DATA_SUBSCRIPTION_TYPE));
                    boolean useDate = Boolean.parseBoolean(parser.getAttributeValue(null,
                            DATA_SUBSCRIPTION_USE_DATE));
                    String proactiveMessage = parser.getAttributeValue(/* namespace = */ null,
                            DATA_SUBSCRIPTION_PROACTIVE_MESSAGE);
                    String reactiveMessage = parser.getAttributeValue(/* namespace = */ null,
                            DATA_SUBSCRIPTION_REACTIVE_MESSAGE);
                    DataSubscriptionConfig dataSubscriptionConfig = new DataSubscriptionConfig(type,
                            useDate, proactiveMessage, reactiveMessage);
                    configData.put(id, dataSubscriptionConfig);
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Cannot parse data subscription config XML: "
                    + e.getMessage());
        }
        return configData;
    }

    /**
     * Loads the config data from config.xml
     */
    public static Map<Integer, DataSubscriptionConfig> loadConfig(Context context) {
        XmlResourceParser xrp = context.getResources().getXml(R.xml.config);
        return parseConfig(xrp);
    }
}
