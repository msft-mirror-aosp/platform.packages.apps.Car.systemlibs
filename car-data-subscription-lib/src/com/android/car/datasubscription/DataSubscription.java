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


import static com.android.car.datasubscription.DataSubscriptionStatus.PAID;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;

import androidx.annotation.GuardedBy;

/**
 * This class provides a mechanism to indicate if a data plan is provided on a trial vs paid basis.
 */
public class DataSubscription {
    public static final String DATA_SUBSCRIPTION_ACTION =
            "android.intent.action.DATA_SUBSCRIPTION";
    private static final String SETTING = "car_data_subscription_status";
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 3;

    @DataSubscriptionStatus private static final int DEFAULT_VALUE = PAID;

    @GuardedBy("this")
    private DataSubscriptionChangeListener mDataSubscriptionChangeListener;
    private final Context mContext;
    private final ContentObserver mContentObserver =
            new ContentObserver(/*  handler= */ null) {
                @Override
                public void onChange(boolean selfChange) {
                    synchronized (DataSubscription.this) {
                        if (mDataSubscriptionChangeListener != null) {
                            mDataSubscriptionChangeListener.onChange(getDataSubscriptionStatus());
                        }
                    }
                }
            };

    public DataSubscription(Context context) {
        mContext = context;

    }

    /** Returns the data subscription status of the vehicle. */
    @DataSubscriptionStatus
    public int getDataSubscriptionStatus() {
        int subscriptionStatus =
                Settings.Global.getInt(mContext.getContentResolver(), SETTING, DEFAULT_VALUE);
        return (subscriptionStatus < MIN_VALUE || subscriptionStatus > MAX_VALUE)
                ? DEFAULT_VALUE
                : subscriptionStatus;
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
     * Interface to implement for listening to Data Subscription status changes.
     */
    public interface DataSubscriptionChangeListener {
        /**
         * Receive the Data Subscription status changes.
         */
        void onChange(@DataSubscriptionStatus int value);
    }
}
