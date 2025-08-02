/*
 * Copyright (C) 2025 The Android Open Source Project.
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
package com.android.car.scalableui.model

/**
 * Represents the restart behavior of a panel.
 *
 * @property policy The restart policy.
 * @property maxRetry The maximum number of retries.
 */
data class Restart(val policy: String, val maxRetry: Int) {
    companion object {
        // Policy to launch the default task when restarted in a panel.
        const val RESTART_POLICY_DEFAULT = "DEFAULT"

        // Policy to start the last vanished task in a panel.
        const val RESTART_POLICY_LAST = "LAST"
    }
}
