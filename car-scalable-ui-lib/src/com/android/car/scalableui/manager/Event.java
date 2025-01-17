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
 * Describes an event in the system. An event can optionally carry a payload object.
 */
public class Event {
    private final String mId;
    private final Object mPayload;

    /**
     * Constructs an Event without a payload.
     *
     * @param id A unique identifier associated with this event.
     */
    public Event(String id) {
        this(id, null);
    }

    /**
     * Constructs an Event with an optional payload.
     *
     * @param id A unique identifier associated with this event.
     * @param payload An optional payload associated with this event.
     */
    public Event(String id, Object payload) {
        mId = id;
        mPayload = payload;
    }

    /**
     * Returns the event identifier.
     *
     * @return The event identifier.
     */
    public String getId() {
        return mId;
    }

    /**
     * Returns the payload associated with this event.
     *
     * @return The payload of the event, or null if no payload is associated.
     */
    public Object getPayload() {
        return mPayload;
    }

    @Override
    public String toString() {
        return "Event{"
                + "mId='" + mId + '\''
                + ", mPayload=" + mPayload
                + '}';
    }
}
