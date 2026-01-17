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
 * Defines the behavior of tasks within a panel.
 *
 * @property newTaskLaunchPolicy The policy to use when launching a new task.
 */
data class TaskBehavior(val taskProperties: String, val newTaskLaunchPolicy: String) {
    companion object {
        /**
         * The default task property, where tasks will behave normally on this panel.
         */
        const val TASK_PROPERTY_DEFAULT = "DEFAULT"

        /**
         * Panel task property that will cause all tasks on this panel to be untrimmable.
         */
        const val TASK_PROPERTY_UNTRIMMABLE = "UNTRIMMABLE"

        /**
         * The default policy, where the new task's behavior is determined by the system.
         */
        const val NEW_TASK_LAUNCH_POLICY_DEFAULT = "DEFAULT"

        /**
         * Policy indicating that the new task should remain in the source task's stack.
         */
        const val NEW_TASK_LAUNCH_POLICY_REMAIN_IN_SOURCE = "REMAIN_IN_SOURCE"

        /**
         * Policy indicating that the new task should be reparented to the source task's stack.
         */
        const val NEW_TASK_LAUNCH_POLICY_REPARENT_TO_SOURCE = "REPARENT_TO_SOURCE"
    }
}
