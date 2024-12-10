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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the state of UI panels. This class is responsible for loading panel definitions,
 * handling events that trigger state transitions, and applying visual updates to panels
 * based on their current state.
 */
public class StateManager {

    private static final StateManager sInstance = new StateManager();

    private StateManager() {}

    private final List<PanelState> mPanels = new ArrayList<>();

    /**
     * Adds a new panel state definition.
     *
     * @param panel The panel state to be added.
     */
    public static void addState(PanelState panel) {
        sInstance.mPanels.add(panel);
        applyState(panel);
    }

    /**
     * Resets the state manager by clearing all panel definitions.
     */
    public static void reset() {
        sInstance.mPanels.clear();
    }

    /**
     * Handles an event by triggering state transitions for panels with matching transitions.
     * This method iterates through all registered panel definitions, checks if any transitions
     * are defined for the given event, and applies the transition (including animations) if found.
     *
     * @param event The event to be handled.
     */
    static void handleEvent(Event event) {
        for (PanelState panelState : sInstance.mPanels) {
            Transition transition = panelState.getTransition(event);
            if (transition == null) {
                continue;
            }

            Panel panel = PanelPool.getInstance().getPanel(panelState.getId());
            Animator animator = transition.getAnimator(panel, panelState.getCurrentVariant());
            if (animator != null) {
                // Update the internal state to the new variant and show the transition animation
                panelState.onAnimationStart(animator);
                panelState.setVariant(transition.getToVariant().getId(), event.getPayload());
                animator.removeAllListeners();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        panelState.onAnimationEnd();
                        applyState(panelState);
                    }
                });
                animator.start();
            } else if (!panelState.isAnimating()) {
                // Force apply the new state if there is no on going animation.
                Variant toVariant = transition.getToVariant();
                panelState.setVariant(toVariant.getId(), event.getPayload());
                applyState(panelState);
            }
        }
    }

    /**
     * Applies the current state of a panel to the UI. This method updates the panel's
     * visual properties (bounds, visibility, alpha, layer) based on its current variant.
     *
     * @param panelState The panel data containing the current state information.
     */
    private static void applyState(PanelState panelState) {
        Variant variant = panelState.getCurrentVariant();
        String panelId = panelState.getId();
        Panel panel = PanelPool.getInstance().getPanel(panelId);
        panel.setRole(panelState.getRole().getValue());
        panel.setBounds(variant.getBounds());
        panel.setVisibility(variant.isVisible());
        panel.setAlpha(variant.getAlpha());
        panel.setLayer(variant.getLayer());
    }
}
