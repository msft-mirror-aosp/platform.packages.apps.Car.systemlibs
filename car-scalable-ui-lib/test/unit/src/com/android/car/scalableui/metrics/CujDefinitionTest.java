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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Variant;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RunWith(AndroidJUnit4.class)
public class CujDefinitionTest {

    private static final String PANEL_1 = "Panel1";
    private static final String PANEL_2 = "Panel2";

    private static final String TEST_EVENT_1 = "Event1";
    private static final String TEST_EVENT_2 = "Event2";

    @Test
    public void matches_allConditionsMet_returnsTrue() {
        // Arrange
        Predicate<Event> eventPredicate = event -> event.getId().equals(TEST_EVENT_1);
        Predicate<PanelState> preCondition = CujRegistry.IS_VISIBLE;
        Predicate<PanelState> postCondition = CujRegistry.IS_NOT_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Map.of(PANEL_1, preCondition),
                Map.of(PANEL_1, postCondition));

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Map.of(PANEL_1, createPanelState(PANEL_1, true));
        Map<String, PanelState> stateAfter = Map.of(PANEL_1, createPanelState(PANEL_1, false));

        // Act & Assert
        assertTrue(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_eventDoesNotMatch_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> event.getId().equals(TEST_EVENT_1);
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Collections.emptyMap(), Collections.emptyMap());

        Event event = new Event.Builder(TEST_EVENT_2).build(); // Different event type
        Map<String, PanelState> stateBefore = Collections.emptyMap();
        Map<String, PanelState> stateAfter = Collections.emptyMap();

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_preconditionNotMet_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> true;
        Predicate<PanelState> preCondition = CujRegistry.IS_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Map.of(PANEL_1, preCondition), Collections.emptyMap());

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Map.of(PANEL_1,
                createPanelState(PANEL_1, false)); // Precondition fails
        Map<String, PanelState> stateAfter = Collections.emptyMap();

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_postconditionNotMet_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> true;
        Predicate<PanelState> postCondition = CujRegistry.IS_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Collections.emptyMap(), Map.of(PANEL_1, postCondition));

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Collections.emptyMap();
        Map<String, PanelState> stateAfter = Map.of(PANEL_1,
                createPanelState(PANEL_1, false)); // Postcondition fails

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_preconditionPanelMissing_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> true;
        Predicate<PanelState> preCondition = CujRegistry.IS_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Map.of(PANEL_1, preCondition), Collections.emptyMap());

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Collections.emptyMap(); // Panel1 is missing
        Map<String, PanelState> stateAfter = Collections.emptyMap();

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }


    @Test
    public void matches_postconditionPanelMissing_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> true;
        Predicate<PanelState> postCondition = CujRegistry.IS_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Collections.emptyMap(), Map.of(PANEL_1, postCondition));

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Collections.emptyMap();
        Map<String, PanelState> stateAfter = Collections.emptyMap(); // Panel1 is missing

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_multipleConditions_allMet_returnsTrue() {
        // Arrange
        Predicate<Event> eventPredicate = event -> event.getId().equals(TEST_EVENT_1);
        Predicate<PanelState> pre1 = CujRegistry.IS_VISIBLE;
        Predicate<PanelState> pre2 = CujRegistry.IS_NOT_VISIBLE;
        Predicate<PanelState> post1 = CujRegistry.IS_NOT_VISIBLE;
        Predicate<PanelState> post2 = CujRegistry.IS_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Map.of(PANEL_1, pre1, PANEL_2, pre2),
                Map.of(PANEL_1, post1, PANEL_2, post2));

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Map.of(
                PANEL_1, createPanelState(PANEL_1, true),
                PANEL_2, createPanelState(PANEL_2, false)
        );
        Map<String, PanelState> stateAfter = Map.of(
                PANEL_1, createPanelState(PANEL_1, false),
                PANEL_2, createPanelState(PANEL_2, true)
        );

        // Act & Assert
        assertTrue(cuj.matches(event, stateBefore, stateAfter));
    }

    @Test
    public void matches_multipleConditions_onePreconditionFails_returnsFalse() {
        // Arrange
        Predicate<Event> eventPredicate = event -> event.getId().equals(TEST_EVENT_1);
        Predicate<PanelState> pre1 = CujRegistry.IS_VISIBLE;
        Predicate<PanelState> pre2 = CujRegistry.IS_NOT_VISIBLE;
        CujDefinition cuj = new CujDefinition(1, PANEL_1, eventPredicate,
                Map.of(PANEL_1, pre1, PANEL_2, pre2), Collections.emptyMap());

        Event event = new Event.Builder(TEST_EVENT_1).build();
        Map<String, PanelState> stateBefore = Map.of(
                PANEL_1, createPanelState(PANEL_1, true),
                PANEL_2, createPanelState(PANEL_2, true) // This fails pre2
        );
        Map<String, PanelState> stateAfter = Collections.emptyMap();

        // Act & Assert
        assertFalse(cuj.matches(event, stateBefore, stateAfter));
    }


    private PanelState createPanelState(String panelId, boolean isVisible) {
        List<Variant> listOfVariants = new ArrayList<Variant>();
        Variant currentVariant = new Variant.Builder(panelId, panelId).setVisibility(
                isVisible).build();
        listOfVariants.add(currentVariant);
        return new PanelState.Builder(panelId)
                .setVariants(listOfVariants)
                .setDefaultVariant(panelId)
                .build();
    }
}
