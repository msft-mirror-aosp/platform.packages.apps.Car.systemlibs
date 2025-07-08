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

package com.android.car.scalableui.metrics;

import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Defines the conditions for a specific Critical User Journey (CUJ).
 * This class acts as a "recipe" for identifying a CUJ based on the
 * triggering event and the state of UI panels before and after.
 */
public class CujDefinition {

    private final int mCujType;
    private final String mRelevantPanelId; // The primary panel associated with this CUJ.
    private final Predicate<Event> mEventPredicate;
    private final Map<String, Predicate<PanelState>> mPreconditions; // Panel ID -> State Condition
    private final Map<String, Predicate<PanelState>> mPostconditions; // Panel ID -> State Condition

    /**
     * Constructs a CujDefinition.
     *
     * @param cujType         The integer identifier for the CUJ.
     * @param relevantPanelId The ID of the panel that is the main subject of this CUJ.
     * @param eventPredicate  A predicate to match the triggering event.
     * @param preconditions   A map of panel IDs to predicates defining the required state BEFORE
     *                        the event.
     * @param postconditions  A map of panel IDs to predicates defining the required state AFTER the
     *                        event.
     */
    public CujDefinition(int cujType, String relevantPanelId, Predicate<Event> eventPredicate,
            Map<String, Predicate<PanelState>> preconditions,
            Map<String, Predicate<PanelState>> postconditions) {
        this.mCujType = cujType;
        this.mRelevantPanelId = relevantPanelId;
        this.mEventPredicate = eventPredicate;
        this.mPreconditions = preconditions;
        this.mPostconditions = postconditions;
    }

    public int getCujType() {
        return mCujType;
    }

    public String getRelevantPanelId() {
        return mRelevantPanelId;
    }

    /**
     * Checks if this CUJ definition matches the given event and panel states.
     *
     * @param event       The event that occurred.
     * @param stateBefore A map of panel IDs to their states before the event.
     * @param stateAfter  A map of panel IDs to their states after the event.
     * @return {@code true} if all conditions of this CUJ definition are met.
     */
    public boolean matches(Event event, Map<String, PanelState> stateBefore,
            Map<String, PanelState> stateAfter) {
        // 1. Check if the event matches.
        if (!mEventPredicate.test(event)) {
            return false;
        }
        // 2. Check all preconditions are met.
        for (Map.Entry<String, Predicate<PanelState>> condition : mPreconditions.entrySet()) {
            PanelState panelState = stateBefore.get(condition.getKey());
            // The panel must exist and its state must match the condition.
            if (panelState == null || !condition.getValue().test(panelState)) {
                return false;
            }
        }

        // 3. Check all postconditions are met.
        for (Map.Entry<String, Predicate<PanelState>> condition : mPostconditions.entrySet()) {
            PanelState panelState = stateAfter.get(condition.getKey());
            // The panel must exist and its state must match the condition.
            if (panelState == null || !condition.getValue().test(panelState)) {
                return false;
            }
        }
        return true;
    }
}
