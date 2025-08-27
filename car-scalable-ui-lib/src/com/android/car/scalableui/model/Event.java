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
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Describes an event in the system. An event has an id and optionally tokens to match against
 * transitions.
 */
public class Event {
    private static final String TAG = Event.class.getSimpleName();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final String PANEL_ID_TOKEN_ID = "panelId";
    private static final String COMPONENT_NAME_TOKEN_ID = "component";
    private static final String PACKAGE_NAME_TOKEN_ID = "package";
    private static final String TO_VARIANT_ID_TOKEN_ID = "panelToVariantId";

    /** Id string associated with this event. */
    @NonNull
    protected final String mId;

    /**
     * Token map for this event to be matched against. These tokens are in the format of key:value
     * strings.
     */
    protected final Map<String, String> mTokens = new HashMap<>();

    /**
     * Set of display ids that this event is applicable to. This may include more than just the
     * display context that triggered the event.
     */
    @NonNull
    protected final Set<Integer> mApplicableDisplays = new ArraySet<>();

    /**
     * Constructs an Event.  Package-private; use the Builder.
     *
     * @param id A unique identifier associated with this event.
     */
    Event(@NonNull String id) {
        mId = id;
    }

    protected Event(@NonNull String id, @NonNull Map<String, String> tokens,
            @NonNull Set<Integer> applicableDisplays) {
        mId = id;
        mTokens.putAll(tokens); // Defensive copy
        mApplicableDisplays.addAll(applicableDisplays); // Defensive copy
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

    /** Return the panel id associated with this event or null if none exists. */
    @Nullable
    public String getPanelId() {
        return mTokens.get(PANEL_ID_TOKEN_ID);
    }

    /**
     * Whether the passed in parameters match this event.
     *
     * @param transitionEvent the event from the transition to match against
     * @return true if this event matches the passed in parameters.
     */
    public boolean isMatch(@Nullable Event transitionEvent) {
        logIfDebuggable("Match event " + transitionEvent + ", with " + this);
        if (transitionEvent == null) {
            return false;
        }

        if (!TextUtils.equals(mId, transitionEvent.getId())) {
            // ids don't match
            logIfDebuggable("Event id doesn't match" + mId + " vs " + transitionEvent.getId());
            return false;
        }

        if (!mApplicableDisplays.isEmpty() && mApplicableDisplays.stream().noneMatch(
                transitionEvent.mApplicableDisplays::contains)) {
            // This event contains a display specification while the event specified in the
            // <Transition> does not - this is not a match.
            logIfDebuggable("Event displays do not match");
            return false;
        }

        Map<String, String> transitionTokens = transitionEvent.getTokens();
        if (transitionTokens.isEmpty()) {
            // ids match and transition doesn't specify and additional tokens to match
            return true;
        }

        if (mTokens.isEmpty()) {
            // transition has tokens but event does not - not a match
            logIfDebuggable("transition has tokens but event does not - not a match");
            return false;
        }

        for (String key : transitionTokens.keySet()) {
            if (!mTokens.containsKey(key)
                    || !TextUtils.equals(mTokens.get(key), transitionTokens.get(key))) {
                // tokens don't match - not a match
                logIfDebuggable("Token don't match " + key);
                return false;
            }
        }
        // all specified transition tokens match the event
        return true;
    }

    private static void logIfDebuggable(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /** Creates a string representation of the Event. */
    @Override
    @NonNull
    public String toString() {
        String tokenString = mTokens.isEmpty()
                ? "empty"
                : mTokens.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(" , "));
        String applicableDisplayString = mApplicableDisplays.isEmpty()
                ? "empty"
                : mApplicableDisplays.stream().map(Object::toString).collect(
                        Collectors.joining(", "));
        return "Event{" + "mId='" + mId + "' mTokens='" + tokenString
                + "' mApplicableDisplays='" + applicableDisplayString + "'}";
    }

    /** Builder for {@link Event} objects. */
    public static class Builder {
        protected String mId;
        protected Map<String, String> mTokens = new HashMap<>();
        protected Set<Integer> mApplicableDisplays = new ArraySet<>();

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

        /** Add an applicable display to this event */
        public Builder addApplicableDisplay(int displayId) {
            mApplicableDisplays.add(displayId);
            return this;
        }

        /**
         * Add a list of applicable displays to this event.
         */
        public Builder addApplicableDisplays(Collection<Integer> displayIds) {
            mApplicableDisplays.addAll(displayIds);
            return this;
        }

        /** Sets a token for panelId. */
        public Builder setPanelId(@NonNull String panelId) {
            mTokens.put(PANEL_ID_TOKEN_ID, panelId);
            return this;
        }

        /** Sets a token for package name. */
        public Builder setPackageName(@NonNull String packageName) {
            mTokens.put(PACKAGE_NAME_TOKEN_ID, packageName);
            return this;
        }

        /** Sets a token for component name. */
        public Builder setComponentName(@NonNull String componentName) {
            mTokens.put(COMPONENT_NAME_TOKEN_ID, componentName);
            return this;
        }

        /** Sets a token for toVariant. */
        public Builder setToVariantId(@NonNull String toVariantId) {
            mTokens.put(TO_VARIANT_ID_TOKEN_ID, toVariantId);
            return this;
        }

        /** Returns the {@link Event} instance */
        @NonNull
        public Event build() {
            if (mId == null) {
                throw new IllegalStateException("Event ID must be set.");
            }
            return new Event(mId, mTokens, mApplicableDisplays);
        }
    }
}
