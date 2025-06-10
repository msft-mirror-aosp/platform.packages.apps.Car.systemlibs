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
package com.android.car.scalableui.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.car.scalableui.model.Action;
import com.android.car.scalableui.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages and dispatches {@link Action} objects based on incoming {@link Event}s.
 * This class maintains a list of registered actions and provides a mechanism to
 * trigger these actions via broadcast intents when a matching event occurs.
 *
 * <p>Actions are expected to be defined externally and loaded into the manager
 * using the {@link #setActions(List)} method. When an event is handled,
 * the manager iterates through the registered actions, checks if an action's
 * trigger condition is met by the event, and if so, sends an ordered broadcast
 * with the intent associated with that action.
 */
public class ActionManager {
    private static final String TAG = ActionManager.class.getSimpleName();
    private static final String ACTION_PERMISSION =
            "com.android.car.scalableui.permission.ACTION_BROADCAST_PERMISSION";

    private static final ActionManager sInstance = new ActionManager();

    private final List<Action> mActions = new ArrayList<>();

    private ActionManager() {
    }

    /**
     * Handles an event by triggering relevant actions.
     * This method iterates through all registered action definitions, checks if any actions
     * are a match for the given event, and triggers such an event by broadcasting an intent.
     *
     * @param event The event to be handled.
     */
    public static void handleEvent(Context context, Event event) {
        for (Action action : sInstance.mActions) {
            if (!action.isTriggeredBy(event)) {
                continue;
            }

            Intent intent = action.getIntent();
            try {
                context.sendOrderedBroadcast(intent, ACTION_PERMISSION);
            } catch (SecurityException e) {
                Log.w(TAG, "Action broadcast could not be sent.", e);
            }
        }
    }

    /**
     * Reloads {@link Action} list.
     */
    public static void setActions(List<Action> actions) {
        sInstance.mActions.clear();
        sInstance.mActions.addAll(actions);
    }
}
