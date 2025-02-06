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

import android.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an event in the system. An event has an id and optionally tokens to match against
 * transitions.
 */
public class Event {
    /** Id string associated with this event. */
    private final String mId;
    /**
     * Token map for this event to be matched against. These tokens are in the format of key:value
     * strings.
     */
    private final Map<String, String> mTokens = new HashMap<>();

    /**
     * Constructs an Event.
     *
     * @param id A unique identifier associated with this event.
     */
    public Event(String id) {
        mId = id;
    }

    /**
     * Adds a token to this event to be matched against.
     */
    public final Event addToken(String tokenId, String tokenValue) {
        mTokens.put(tokenId, tokenValue);
        return this;
    }

    /**
     * Returns the id associated with this event.
     */
    public String getId() {
        return mId;
    }

    /**
     * Return the tokens associated with this event.
     */
    public Map<String, String> getTokens() {
        return mTokens;
    }

    /**
     * Whether the passed in parameters match this event.
     * @param transitionEvent the event from the transition to match against
     * @return true if this event matches the passed in parameters.
     */
    public boolean isMatch(@Nullable Event transitionEvent) {
        if (transitionEvent == null) {
            return false;
        }

        if (!TextUtils.equals(mId, transitionEvent.getId())) {
            // ids don't match
            return false;
        }

        Map<String, String> transitionTokens = transitionEvent.getTokens();
        if (transitionTokens == null || transitionTokens.isEmpty()) {
            // ids match and transition doesn't specify and additional tokens to match
            return true;
        }

        if (mTokens.isEmpty()) {
            // transition has tokens but event does not - not a match
            return false;
        }

        for (String key : transitionTokens.keySet()) {
            if (!mTokens.containsKey(key) || !TextUtils.equals(mTokens.get(key),
                    transitionTokens.get(key))) {
                // tokens don't match - not a match
                return false;
            }
        }
        // all specified transition tokens match the event
        return true;
    }

    @Override
    public String toString() {
        return "Event{" + "mId='" + mId + "' mTokens='" + mTokens + "'}";
    }
}
