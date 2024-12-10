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

package com.android.car.scalableui.manager;

/**
 * A utility class for dispatching events. This class provides methods for dispatching events with
 * or without payloads. All events are handled by the {@link StateManager}.
 */
public class EventDispatcher {

    /**
     * Dispatches an event without a payload.
     *
     * @param eventId The id of the event that needs to be dispatched.
     */
    public static void dispatch(String eventId) {
        dispatch(eventId, null);
    }

    /**
     * Dispatches an event with a given payload.
     *
     * @param eventId The id of the event that needs to be dispatched.
     * @param payload The payload associated with the event. Can be any Java object.
     */
    public static void dispatch(String eventId, Object payload) {
        dispatch(new Event(eventId, payload));
    }

    /**
     * Dispatches a given event.
     *
     * @param event The event object to be dispatched.
     */
    public static void dispatch(Event event) {
        StateManager.handleEvent(event);
    }
}
