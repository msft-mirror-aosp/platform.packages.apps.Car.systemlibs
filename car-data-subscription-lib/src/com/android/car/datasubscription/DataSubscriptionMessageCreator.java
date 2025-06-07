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

import androidx.annotation.VisibleForTesting;

import com.android.car.datasubscription.DataSubscriptionConfig.DataSubscriptionStatusType;

import java.util.Map;


public class DataSubscriptionMessageCreator {
    private Context mContext;
    private DataSubscriptionExpirationDate mDataSubscriptionExpirationDate;
    private final Map<Integer, DataSubscriptionConfig> mConfigData;

    public DataSubscriptionMessageCreator(Context context) {
        mContext = context;
        mDataSubscriptionExpirationDate = new DataSubscriptionExpirationDate(context);
        mConfigData  = DataSubscriptionConfigParser.loadConfig(context);
    }

    /**
     * Returns a proactive msg for a certain status
     */
    public String getProactiveMessageForStatus(int status) {
        if (status == DataSubscription.DATA_SUBSCRIPTION_INVALID_STATUS) {
            return null;
        }
        DataSubscriptionConfig config = mConfigData.get(status);
        String proactiveMessage = config.getProactiveMessage();
        if (proactiveMessage == null || proactiveMessage.isEmpty()) {
            return null;
        }
        DataSubscriptionStatusType statusType = config.getType();
        if (statusType == DataSubscriptionStatusType.EXPIRING) {
            return String.format(proactiveMessage, config.getUseDate()
                    ? mDataSubscriptionExpirationDate.getExpirationDate()
                    : mDataSubscriptionExpirationDate.getExpirationTime());
        }
        return proactiveMessage;
    }

    /**
     * Returns a reactive msg for a certain status
     */
    public String getReactiveMessageForStatus(int status, CharSequence appLabel) {
        if (status == DataSubscription.DATA_SUBSCRIPTION_INVALID_STATUS) {
            return null;
        }
        DataSubscriptionConfig config = mConfigData.get(status);
        String reactiveMessage = config.getReactiveMessage();
        if (reactiveMessage == null || reactiveMessage.isEmpty()) {
            return null;
        }
        DataSubscriptionStatusType statusType = config.getType();
        if (statusType == DataSubscriptionStatusType.EXPIRING) {
            return String.format(reactiveMessage,
                    appLabel, config.getUseDate()
                        ? mDataSubscriptionExpirationDate.getExpirationDate()
                        : mDataSubscriptionExpirationDate.getExpirationTime());
        }
        return String.format(reactiveMessage, appLabel);
    }

    /**
     * Returns a UXR prompt based on the current UXR
     */
    public String getUxrPrompt(boolean isUxrRequired) {
        if (isUxrRequired) {
            return mContext.getResources().getString(R.string.data_subscription_uxr_prompt);
        }
        return mContext.getResources().getString(R.string.data_subscription_non_uxr_prompt);
    }

    @VisibleForTesting
    void setDataSubscriptionExpirationDate(
            DataSubscriptionExpirationDate dataSubscriptionExpirationDate) {
        mDataSubscriptionExpirationDate = dataSubscriptionExpirationDate;
    }
}
