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

import android.util.Log;

public class DataSubscriptionConfig {
    public static final String TAG = DataSubscriptionConfig.class.getSimpleName();
    private final DataSubscriptionStatusType mType;

    //Decides the message will use a date or a time period (e.g, expiring messages)
    private final boolean mUseDate;
    private String mProactiveMessage;
    private String mReactiveMessage;

    public DataSubscriptionConfig(DataSubscriptionStatusType type, boolean useDate,
            String proactiveMessage, String reactiveMessage) {
        mType = type;
        mUseDate = useDate;
        mProactiveMessage = proactiveMessage;
        mReactiveMessage = reactiveMessage;
    }

    public DataSubscriptionStatusType getType() {
        return mType;
    }

    public boolean getUseDate() {
        return mUseDate;
    }

    public String getProactiveMessage() {
        return mProactiveMessage;
    }

    public String getReactiveMessage() {
        return mReactiveMessage;
    }

    public enum DataSubscriptionStatusType {
        /**
         * There is currently no persistent cellular network available
         */
        INACTIVE("inactive"),
        /**
         * There is currently a persistent cellular network available which is being
         * sponsored by the OEM as part of a trial program
         */
        TRIAL("trial"),
        /**
         * There is currently a persistent cellular network available being paid for
         * by the customer
         */
        PAID("paid"),
        /**
         * There is currently a  cellular network available but this will expire soon
         * for the customer
         */
        EXPIRING("expiring");

        private final String mValue;

        DataSubscriptionStatusType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }

        /**
         * Returns a DataSubscriptionStatusType corresponding to a string
         */
        public static DataSubscriptionStatusType fromValue(String value) {
            for (DataSubscriptionStatusType type : DataSubscriptionStatusType.values()) {
                if (type.getValue().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            Log.e(TAG, "Cannot find enum value");
            return null;
        }
    }
}
