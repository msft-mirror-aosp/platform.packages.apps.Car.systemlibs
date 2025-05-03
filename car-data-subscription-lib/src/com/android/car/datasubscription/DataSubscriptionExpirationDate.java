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
import android.provider.Settings;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DataSubscriptionExpirationDate {
    private static final String CAR_DATA_SUBSCRIPTION_EXPIRATION_DATE =
            "car_data_subscription_expiration_date";
    public static final int EXPIRATION_DATE_NOT_SET = -1;
    private final Context mContext;

    public DataSubscriptionExpirationDate(Context context) {
        mContext = context;
    }

    /** @return The expiration date,
     * or null if the expiration date is not set.*/
    public String getExpirationDate() {
        String expirationDate = Settings.Global.getString(
                mContext.getContentResolver(), CAR_DATA_SUBSCRIPTION_EXPIRATION_DATE);
        if (expirationDate != null && validateExpirationDate(expirationDate)) {
            return expirationDate;
        }
        return null;
    }


    /** @return The time until the expiration time in days,
     * or -1 if the expiration date is not set.*/
    public int getExpirationTime() {
        String expirationDate = getExpirationDate();
        if (expirationDate != null && validateExpirationDate(expirationDate)) {
            return (int) ChronoUnit.DAYS.between(LocalDate.now(ZoneId.systemDefault()),
                    LocalDate.parse(expirationDate));
        }
        return EXPIRATION_DATE_NOT_SET;
    }

    private boolean validateExpirationDate(String date) {
        String expectedDateFormat = mContext.getResources().getString(
                R.string.config_dataSubscriptionDateTimeFormat);
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern(expectedDateFormat));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
