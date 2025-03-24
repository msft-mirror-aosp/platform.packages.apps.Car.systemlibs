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
package com.android.car.scalableui.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Describes an event in the system. An event has an id and optionally tokens to match against
 * transitions.
 */
public class Event {

    /** Id string associated with this event. */
    @NonNull
    protected final String mId;

    /**
     * Token map for this event to be matched against. These tokens are in the format of key:value
     * strings.
     */
    protected final Map<String, String> mTokens = new HashMap<>();

    /**
     * Constructs an Event.  Package-private; use the Builder.
     *
     * @param id A unique identifier associated with this event.
     */
    Event(@NonNull String id) {
        mId = id;
    }

    protected Event(@NonNull String id, @NonNull Map<String, String> tokens) {
        mId = id;
        mTokens.putAll(tokens); // Defensive copy
    }

    /** Adds a token to this event to be matched against. */
    public final Event addToken(String tokenId, String tokenValue) {
        mTokens.put(tokenId, tokenValue);
        return this;
    }

    /** Returns the id associated with this event. */
    @NonNull
    public String getId() {
        return mId;
    }

    /** Return the tokens associated with this event. */
    @NonNull
    public Map<String, String> getTokens() {
        // Return a copy to prevent external modification
        return new HashMap<>(mTokens);
    }

    /**
     * Whether the passed in parameters match this event.
     *
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
            if (!mTokens.containsKey(key)
                    || !TextUtils.equals(mTokens.get(key), transitionTokens.get(key))) {
                // tokens don't match - not a match
                return false;
            }
        }
        // all specified transition tokens match the event
        return true;
    }

    @Override
    @NonNull
    public String toString() {
        return "Event{" + "mId='" + mId + "' mTokens='" + mTokens + "'}";
    }

    /** Builder for {@link Event} objects. */
    public static class Builder {
        protected String mId;
        protected Map<String, String> mTokens = new HashMap<>();

        public Builder(@NonNull String id) {
            mId = id;
        }

        /** Adds token */
        public Builder addToken(String key, String value) {
            mTokens.put(key, value);
            return this;
        }

        /** Sets token */
        public Builder addTokensFromString(@Nullable String eventTokens) {
            if (!TextUtils.isEmpty(eventTokens)) {
                StringTokenizer tokenizer = new StringTokenizer(eventTokens, ";");
                while (tokenizer.hasMoreTokens()) {
                    String pair = tokenizer.nextToken();
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        mTokens.put(keyValue[0], keyValue[1]);
                    } // else:  Ignore malformed tokens.
                }
            }
            return this;
        }

        /** Returns the {@link Event} instance */
        @NonNull
        public Event build() {
            if (mId == null) {
                throw new IllegalStateException("Event ID must be set.");
            }
            return new Event(mId, mTokens);
        }
    }
}
