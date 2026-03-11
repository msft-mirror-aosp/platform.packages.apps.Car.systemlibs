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

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an action, consisting of a required intent. This class provides methods for creating
 * an Action object and retrieving its intent.
 */
public class Action {

    @NonNull private final Intent mIntent;
    @NonNull private final List<Event> mEvents;

    /**
     * Constructs an Action object with the specified intent. Package-private constructor; use the
     * Builder.
     *
     * @param intent The intent string for this action.
     * @param events The list of events that can trigger this action.
     */
    Action(@NonNull Intent intent, @NonNull List<Event> events) {
        mEvents = new ArrayList<>(events);
        mIntent = intent;
    }

    /**
     * Returns the intent for this action.
     *
     * @return The intent.
     */
    @NonNull
    public Intent getIntent() {
        return mIntent;
    }

    /**
     * Returns whether this action is triggered by the given event.
     *
     * @param event The event that the action should be checked against.
     * @return True if the action should be triggered by the given event.
     */
    public boolean isTriggeredBy(Event event) {
        for (Event e : mEvents) {
            if (e.isMatch(event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of events for this action.
     *
     * @return The list of events.
     */
    @VisibleForTesting
    @NonNull
    public List<Event> getEvents() {
        return mEvents;
    }

    /**
     * Returns a string representation of the Action object. This includes the action's intent and
     * its associated trigger events.
     *
     * @return A string representation of the Action.
     */
    @Override
    @NonNull
    public String toString() {
        return "Action { "
                + "intent='"
                + mIntent.toUri(0)
                + "',"
                + "events=["
                + mEvents.stream().map(Event::toString).collect(Collectors.joining(", ", "[", "]"))
                + "}";
    }

    /** Builder for {@link Action} objects. */
    public static class Builder {
        @Nullable private Intent mIntent;
        @NonNull private List<Event> mEvents = new ArrayList<>();

        /**
         * Creates a builder with no intent set initially. Intent must be set via {@link #setIntent}
         * before building.
         */
        public Builder() {}

        /**
         * Creates a builder with given intent.
         *
         * @param intent The intent.
         */
        public Builder(@NonNull Intent intent) {
            mIntent = intent;
        }

        /**
         * Sets the intent for this action.
         *
         * @param intent The intent.
         * @return The Builder instance for chaining.
         */
        @NonNull
        public Builder setIntent(@NonNull Intent intent) {
            mIntent = intent;
            return this;
        }

        /**
         * Adds the optional event to the list of triggers for this action.
         *
         * @param event The trigger for this action.
         * @return The Builder instance for chaining.
         */
        @NonNull
        public Builder addTrigger(@NonNull Event event) {
            mEvents.add(event);
            return this;
        }

        /**
         * Returns the {@link Action} instance.
         *
         * @throws IllegalStateException if the intent has not been set.
         * @return The constructed Action object.
         */
        @NonNull
        public Action build() {
            if (mIntent == null) {
                throw new IllegalStateException("Intent must be set for an Action.");
            }

            return new Action(mIntent, mEvents);
        }
    }
}
