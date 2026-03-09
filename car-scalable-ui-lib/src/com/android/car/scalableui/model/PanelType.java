/*
 * Copyright (C) 2024 The Android Open Source Project.
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
package com.android.car.scalableui.model;

import static com.android.car.scalableui.model.PanelType.DECOR;
import static com.android.car.scalableui.model.PanelType.HUN;
import static com.android.car.scalableui.model.PanelType.SYSTEM_BAR;
import static com.android.car.scalableui.model.PanelType.TASK;

import android.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Defines the type of a panel in Scalable UI. */
@IntDef(value = {TASK, DECOR, SYSTEM_BAR, HUN})
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.SOURCE)
public @interface PanelType {
    /** A panel that hosts a task. */
    int TASK = 0;

    /** A panel that hosts decorative elements. */
    int DECOR = 1;

    /** A panel that hosts system bars. */
    int SYSTEM_BAR = 2;

    /** A panel that hosts Heads-up notifications. */
    int HUN = 3;
}
