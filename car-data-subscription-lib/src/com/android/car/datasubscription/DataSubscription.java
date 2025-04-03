/*
 * Copyright (C) 2024 The Android Open Source Project
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
import android.database.ContentObserver;
import android.provider.Settings;

import androidx.annotation.GuardedBy;

import com.android.car.datasubscription.DataSubscriptionConfig.DataSubscriptionStatusType;

import java.util.Map;

/**
 * This class provides a mechanism to indicate the current status of the data subscription
 */
public class DataSubscription {
    public static final String DATA_SUBSCRIPTION_ACTION =
            "android.intent.action.DATA_SUBSCRIPTION";
    public static int DATA_SUBSCRIPTION_INVALID_STATUS = -1;
    private static final String SETTING = "extended_car_data_subscription_status";

    @GuardedBy("this")
    private DataSubscriptionChangeListener mDataSubscriptionChangeListener;
    private final Context mContext;

    // A mapping from config id to the DataSubscriptionConfig in config.xml
    private final Map<Integer, DataSubscriptionConfig> mConfigData;

    private final ContentObserver mContentObserver =
            new ContentObserver(/*  handler= */ null) {
                @Override
                public void onChange(boolean selfChange) {
                    synchronized (DataSubscription.this) {
                        if (mDataSubscriptionChangeListener != null) {
                            mDataSubscriptionChangeListener.onStatusChanged(
                                    getDataSubscriptionStatus());
                        }
                    }
                }
            };

    public DataSubscription(Context context) {
        mContext = context;
        mConfigData = DataSubscriptionConfigParser.loadConfig(context);
    }

    /** Returns the data subscription status of the vehicle, -1
     * if the status is not specified or out of range */
    public int getDataSubscriptionStatus() {
        int status = Settings.Global.getInt(
                mContext.getContentResolver(), SETTING,
                /* def= */ DATA_SUBSCRIPTION_INVALID_STATUS);
        if (status == DATA_SUBSCRIPTION_INVALID_STATUS
                || status >= mConfigData.size()) {
            return DATA_SUBSCRIPTION_INVALID_STATUS;
        }
        return status;
    }

    /**
     * Registers a listener to data subscription status change updates. Supports registering
     * only one listener. Throws IllegalStateException if a listeners is already registered.
     */
    public synchronized void addDataSubscriptionListener(DataSubscriptionChangeListener listener) {
        if (mDataSubscriptionChangeListener != null) {
            throw new IllegalStateException(
                    "A listener is already registered. Multiple listeners are not supported.");
        }
        mDataSubscriptionChangeListener = listener;
        mContext
                .getContentResolver()
                .registerContentObserver(
                        Settings.Global.getUriFor(SETTING),
                        /* notifyForDescendants=  */ false,
                        mContentObserver);
    }

    /**
     * Unregisters the listener for data subscription status change updates. This method must be
     * called after registering a listener to avoid memory leaks. Throws IllegalStateException if no
     * listener is registered.
     */
    public synchronized void removeDataSubscriptionListener() {
        if (mDataSubscriptionChangeListener == null) {
            throw new IllegalStateException("No listener is registered.");
        }
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        mDataSubscriptionChangeListener = null;
    }

    /**
     * Checks if the data subscription status is inactive
     */
    public boolean isDataSubscriptionInactive() {
        int status = getDataSubscriptionStatus();
        if (status == DATA_SUBSCRIPTION_INVALID_STATUS) {
            return false;
        }
        DataSubscriptionStatusType statusType = mConfigData.get(status).getType();
        return statusType == DataSubscriptionStatusType.INACTIVE;
    }

    /**
     * Interface to implement for listening to Data Subscription status changes.
     */
    public interface DataSubscriptionChangeListener {
        /**
         * Receive the Data Subscription status changes.
         */
        void onStatusChanged(int value);
    }
}
