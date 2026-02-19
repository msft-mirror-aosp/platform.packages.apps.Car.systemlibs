/*
 * Copyright (C) 2025 The Android Open Source Project.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.android.car.scalableui.model.Action;
import com.android.car.scalableui.model.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class ActionManagerTest {

    private static final String ACTION_PERMISSION =
            "com.android.car.scalableui.permission.ACTION_BROADCAST_PERMISSION";

    @Mock private Context mMockContext;

    @Before
    public void setUp() {
        // Clear any previous actions to ensure a clean state for each test
        ActionManager.setActions(Collections.emptyList());
    }

    @Test
    public void handleEvent_noActions_noBroadcastSent() {
        Event mockEvent = mock(Event.class);
        ActionManager.setActions(Collections.emptyList());

        ActionManager.handleEvent(mMockContext, mockEvent);

        // Verify that sendOrderedBroadcast was never called on the mMockContext
        verify(mMockContext, never()).sendOrderedBroadcast(any(Intent.class), any(String.class));
    }

    @Test
    public void handleEvent_actionsSetNoTrigger_noBroadcastSent() {
        Event mockEvent = mock(Event.class);

        Action mockAction1 = mock(Action.class);
        when(mockAction1.isTriggeredBy(mockEvent)).thenReturn(false); // Action does not trigger

        Action mockAction2 = mock(Action.class);
        when(mockAction2.isTriggeredBy(mockEvent)).thenReturn(false); // Action does not trigger

        ActionManager.setActions(Arrays.asList(mockAction1, mockAction2));

        ActionManager.handleEvent(mMockContext, mockEvent);

        // Verify that sendOrderedBroadcast was never called
        verify(mMockContext, never()).sendOrderedBroadcast(any(Intent.class), any(String.class));
    }

    @Test
    public void handleEvent_oneActionTriggers_broadcastSent() {
        Event mockEvent = mock(Event.class);
        Intent expectedIntent = new Intent("MOCKED_ACTION");

        Action mockAction = mock(Action.class);
        when(mockAction.isTriggeredBy(mockEvent)).thenReturn(true);
        when(mockAction.getIntent()).thenReturn(expectedIntent);

        ActionManager.setActions(Collections.singletonList(mockAction));

        ActionManager.handleEvent(mMockContext, mockEvent);

        // Verify that sendOrderedBroadcast was called exactly once with the correct intent and
        // permission
        verify(mMockContext, times(1))
                .sendOrderedBroadcast(eq(expectedIntent), eq(ACTION_PERMISSION));
    }

    @Test
    public void handleEvent_multipleActionsTrigger_multipleBroadcastsSent() {
        Event mockEvent = mock(Event.class);

        Intent expectedIntent1 = new Intent("MOCKED_ACTION_1");
        Action mockAction1 = mock(Action.class);
        when(mockAction1.isTriggeredBy(mockEvent)).thenReturn(true);
        when(mockAction1.getIntent()).thenReturn(expectedIntent1);

        Intent expectedIntent2 = new Intent("MOCKED_ACTION_2");
        Action mockAction2 = mock(Action.class);
        when(mockAction2.isTriggeredBy(mockEvent)).thenReturn(true);
        when(mockAction2.getIntent()).thenReturn(expectedIntent2);

        Action mockAction3 = mock(Action.class); // This one will not trigger
        when(mockAction3.isTriggeredBy(mockEvent)).thenReturn(false);

        ActionManager.setActions(Arrays.asList(mockAction1, mockAction2, mockAction3));

        ActionManager.handleEvent(mMockContext, mockEvent);

        // Verify that sendOrderedBroadcast was called for triggered actions, and not for
        // non-triggered.
        verify(mMockContext, times(1))
                .sendOrderedBroadcast(eq(expectedIntent1), eq(ACTION_PERMISSION));
        verify(mMockContext, times(1))
                .sendOrderedBroadcast(eq(expectedIntent2), eq(ACTION_PERMISSION));
        verify(mMockContext, times(2))
                .sendOrderedBroadcast(any(Intent.class), eq(ACTION_PERMISSION));
    }

    @Test
    public void handleEvent_securityException_doesNotCrash() {
        Event mockEvent = mock(Event.class);
        Intent problematicIntent = new Intent("PROBLEM_ACTION");

        Action mockAction = mock(Action.class);
        when(mockAction.isTriggeredBy(mockEvent)).thenReturn(true);
        when(mockAction.getIntent()).thenReturn(problematicIntent);

        // Configure mMockContext to throw SecurityException when sendOrderedBroadcast is called
        doThrow(new SecurityException("Test Security Exception"))
                .when(mMockContext)
                .sendOrderedBroadcast(eq(problematicIntent), eq(ACTION_PERMISSION));

        ActionManager.setActions(Collections.singletonList(mockAction));

        // Call handleEvent and assert that it does not throw an exception
        ActionManager.handleEvent(mMockContext, mockEvent);

        // Verify that sendOrderedBroadcast was still attempted (and thus threw the exception)
        verify(mMockContext, times(1))
                .sendOrderedBroadcast(eq(problematicIntent), eq(ACTION_PERMISSION));
    }
}
