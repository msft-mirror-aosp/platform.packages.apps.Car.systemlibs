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

public interface DataSubscriptionMessageEventListener {
    /**
     * Called when the car data subscription status changes.
     */
    boolean  onDataSubscriptionStatusChanged(boolean isUxrRequired,
            String proactiveMessage, String uxrPrompt);
    /**
     * Called when a task is moved to the front, indicating a change in the foreground app.
     */
    boolean onAppForegrounded(boolean isUxrRequired, String reactiveMessage,
            String uxrPrompt);
    /**
     * Called when there is a UXR change.
     */
    boolean onUxrChanged(boolean isUxrRequired, String uxrPrompt);
}
