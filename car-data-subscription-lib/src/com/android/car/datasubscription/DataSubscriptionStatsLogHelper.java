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

import android.annotation.IntDef;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

public class DataSubscriptionStatsLogHelper {
    private static final String TAG = DataSubscriptionStatsLogHelper.class.getSimpleName();
    private long mSessionId;

    /**
     * IntDef representing enum values of CarSystemUiDataSubscriptionEventReported.event_type.
     */
    @IntDef({
            DataSubscriptionEventType.UNSPECIFIED_EVENT_TYPE,
            DataSubscriptionEventType.PROACTIVE_MESSAGE_LAUNCHED,
            DataSubscriptionEventType.REACTIVE_MESSAGE_LAUNCHED,
            DataSubscriptionEventType.BUTTON_CLICKED,
    })

    public @interface DataSubscriptionEventType {
        int UNSPECIFIED_EVENT_TYPE =
                CarDataSubscriptionStatsLog
                        .CAR_DATA_SUBSCRIPTION_EVENT_REPORTED__EVENT_TYPE__UNSPECIFIED_EVENT_TYPE;

        int PROACTIVE_MESSAGE_LAUNCHED =
                CarDataSubscriptionStatsLog
                        .CAR_DATA_SUBSCRIPTION_EVENT_REPORTED__EVENT_TYPE__PROACTIVE_MESSAGE_LAUNCHED;

        int REACTIVE_MESSAGE_LAUNCHED =
                CarDataSubscriptionStatsLog
                        .CAR_DATA_SUBSCRIPTION_EVENT_REPORTED__EVENT_TYPE__REACTIVE_MESSAGE_LAUNCHED;
        int BUTTON_CLICKED =
                CarDataSubscriptionStatsLog
                        .CAR_DATA_SUBSCRIPTION_EVENT_REPORTED__EVENT_TYPE__BUTTON_CLICKED;
    }


    /**
     * Construct logging instance of DataSubscriptionStatsLogHelper.
     */
    public DataSubscriptionStatsLogHelper() {}

    /**
     * Logs that a proactive message is launched.
     */
    public void logProactiveMessageLaunched() {
        mSessionId = UUID.randomUUID().getMostSignificantBits();
        writeDataSubscriptionEventReported(DataSubscriptionEventType.PROACTIVE_MESSAGE_LAUNCHED);
    }

    /**
     * Logs that a reactive message is launched.
     */
    public void logReactiveMessageLaunched() {
        mSessionId = UUID.randomUUID().getMostSignificantBits();
        writeDataSubscriptionEventReported(DataSubscriptionEventType.REACTIVE_MESSAGE_LAUNCHED);
    }

    /**
     * Logs that the "See plans" button is clicked.
     */
    public void logButtonClicked() {
        writeDataSubscriptionEventReported(DataSubscriptionEventType.BUTTON_CLICKED);
    }

    /**
     * Writes to CarDataSubscriptionEventReported atom with all the optional fields filled.
     *
     * @param eventType   one of {@link DataSubscriptionEventType}
     */
    private void writeDataSubscriptionEventReported(int eventType) {
        if (Build.isDebuggable()) {
            Log.d(TAG, "writing CAR_DATA_SUBSCRIPTION_EVENT_REPORTED. sessionId="
                    + mSessionId + ", eventType= " + eventType);
        }
        CarDataSubscriptionStatsLog.write(
                /* atomId */ CarDataSubscriptionStatsLog.CAR_DATA_SUBSCRIPTION_EVENT_REPORTED,
                /* sessionId */ mSessionId,
                /* eventType */ eventType);
    }
}
