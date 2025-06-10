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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ActionTest {

    private Intent mTestIntent;

    @Before
    public void setUp() {
        // Initialize a simple Intent for testing
        mTestIntent = new Intent("TEST_ACTION");
        mTestIntent.setPackage("com.android.car.scalableui"); // Set package for explicit intent
        mTestIntent.putExtra("key", "value");
    }

    // --- Action.Builder Tests ---

    @Test
    public void builder_createsWithValidIntent() {
        Action.Builder builder = new Action.Builder(mTestIntent);
        assertNotNull(builder);
    }

    @Test
    public void builder_addTrigger_addsEvent() {
        Action.Builder builder = new Action.Builder(mTestIntent);
        Event mockEvent = mock(Event.class); // Mock an Event
        builder.addTrigger(mockEvent);

        Action action = builder.build();
        assertTrue(action.getEvents().contains(mockEvent));
    }

    @Test
    public void builder_build_returnsNonNullAction() {
        Action action = new Action.Builder(mTestIntent).build();
        assertNotNull(action);
    }

    @Test
    public void builder_build_actionHasCorrectIntent() {
        Action action = new Action.Builder(mTestIntent).build();
        assertEquals(mTestIntent, action.getIntent());
    }

    @Test
    public void builder_build_actionHasEmptyEventsIfNoneAdded() {
        Action action = new Action.Builder(mTestIntent).build();
        assertTrue(action.getEvents().isEmpty());
    }

    // --- Action Tests ---

    @Test
    public void getIntent_returnsCorrectIntent() {
        Action action = new Action(mTestIntent, Collections.emptyList());
        assertEquals(mTestIntent, action.getIntent());
    }

    @Test
    public void isTriggeredBy_noEvents_returnsFalse() {
        Action action = new Action(mTestIntent, Collections.emptyList());
        Event mockEvent = mock(Event.class);
        assertFalse(action.isTriggeredBy(mockEvent));
    }

    @Test
    public void isTriggeredBy_eventMatches_returnsTrue() {
        Event triggerEvent = mock(Event.class);
        Event checkEvent = mock(Event.class);
        when(triggerEvent.isMatch(checkEvent)).thenReturn(true); // Mock behavior of isMatch

        List<Event> events = new ArrayList<>();
        events.add(triggerEvent);

        Action action = new Action(mTestIntent, events);
        assertTrue(action.isTriggeredBy(checkEvent));
    }

    @Test
    public void isTriggeredBy_eventDoesNotMatch_returnsFalse() {
        Event triggerEvent = mock(Event.class);
        Event checkEvent = mock(Event.class);
        when(triggerEvent.isMatch(checkEvent)).thenReturn(false); // Mock behavior of isMatch

        List<Event> events = new ArrayList<>();
        events.add(triggerEvent);

        Action action = new Action(mTestIntent, events);
        assertFalse(action.isTriggeredBy(checkEvent));
    }

    @Test
    public void isTriggeredBy_multipleEvents_oneMatches_returnsTrue() {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class); // This one will match
        Event event3 = mock(Event.class);
        Event checkEvent = mock(Event.class);

        when(event1.isMatch(checkEvent)).thenReturn(false);
        when(event2.isMatch(checkEvent)).thenReturn(true); // This one matches
        when(event3.isMatch(checkEvent)).thenReturn(false);

        List<Event> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        events.add(event3);

        Action action = new Action(mTestIntent, events);
        assertTrue(action.isTriggeredBy(checkEvent));
    }

    @Test
    public void isTriggeredBy_multipleEvents_noneMatch_returnsFalse() {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        Event event3 = mock(Event.class);
        Event checkEvent = mock(Event.class);

        when(event1.isMatch(checkEvent)).thenReturn(false);
        when(event2.isMatch(checkEvent)).thenReturn(false);
        when(event3.isMatch(checkEvent)).thenReturn(false);

        List<Event> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        events.add(event3);

        Action action = new Action(mTestIntent, events);
        assertFalse(action.isTriggeredBy(checkEvent));
    }

    @Test
    public void constructor_eventsListIsCopied() {
        List<Event> originalEvents = new ArrayList<>();
        Event event1 = mock(Event.class);
        originalEvents.add(event1);

        Action action = new Action(mTestIntent, originalEvents);

        // Modify the original list AFTER constructing the Action
        Event event2 = mock(Event.class);
        originalEvents.add(event2);

        // Verify that the Action's internal list was a copy and wasn't affected
        assertEquals(1, action.getEvents().size());
        assertTrue(action.getEvents().contains(event1));
        assertFalse(action.getEvents().contains(event2));
    }
}
