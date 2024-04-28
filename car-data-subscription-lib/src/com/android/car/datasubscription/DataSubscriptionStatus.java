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

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Specifies a boolean value or a no-op. */
@Retention(RetentionPolicy.SOURCE)

@IntDef({
        DataSubscriptionStatus.INACTIVE,
        DataSubscriptionStatus.TRIAL,
        DataSubscriptionStatus.PAID
})

public @interface DataSubscriptionStatus {
    /**  There is currently no persistent cellular network available */
    int INACTIVE = 1;
    /** There is currently a persistent cellular network available
     * which is being sponsored by the OEM as part of a trial program */
    int TRIAL = 2;
    /** There is currently a persistent cellular network available
     * being paid for by the customer */
    int PAID = 3;
}
