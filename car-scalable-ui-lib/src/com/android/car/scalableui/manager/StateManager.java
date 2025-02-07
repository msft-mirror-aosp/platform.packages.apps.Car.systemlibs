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
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the state of UI panels. This class is responsible for loading panel definitions,
 * handling events that trigger state transitions, and applying visual updates to panels
 * based on their current state.
 */
public class StateManager {

    private static final String TAG = StateManager.class.getSimpleName();

    private static final StateManager sInstance = new StateManager();
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;

    private final Map<String, PanelState> mPanelStates;
    private StateManager() {
        mPanelStates = new HashMap<>();
    }

    /** Clear all panel states. */
    public static void clearStates() {
        sInstance.mPanelStates.clear();
    }

    /**
     * Returns the singleton instance of the StateManager.
     *
     * @return The singleton instance of the StateManager.
     */
    public static StateManager getInstance() {
        return sInstance;
    }

    /**
     * Adds a new panel state definition.
     */
    public static void addState(Context context, int stateResId)
            throws XmlPullParserException, IOException {
        if (DEBUG) {
            Log.d(TAG, "addState: stateResId " + stateResId);
        }
        PanelState panelState = PanelState.load(context, stateResId);
        sInstance.mPanelStates.put(panelState.getId(), panelState);
        applyState(panelState);
        Panel panel = PanelPool.getInstance().getPanel(panelState.getId());
        panel.init();
    }

    /**
     * Handles an event by triggering state transitions for panels with matching transitions.
     * This method iterates through all registered panel definitions, checks if any transitions
     * are defined for the given event, and applies the transition (including animations) if found.
     *
     * @param event The event to be handled.
     */
    public static PanelTransaction handleEvent(Event event) {
        PanelTransaction panelTransaction = new PanelTransaction();
        for (PanelState panelState : sInstance.mPanelStates.values()) {
            Transition transition = panelState.getTransition(event);
            if (transition == null) {
                continue;
            }
            Panel panel = PanelPool.getInstance().getPanel(panelState.getId());

            Variant toVariant = transition.getToVariant();

            Animator animator = transition.getAnimator(panel, panelState.getCurrentVariant());
            if (animator != null) {
                // Update the internal state to the new variant and show the transition animation
                panelState.onAnimationStart(animator);
                animator.removeAllListeners();
                panelState.setVariant(toVariant.getId(), event);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        panelState.onAnimationEnd();
                        applyState(panelState);
                    }
                });
                Log.d(TAG, "add animator for " + panelState.getId());
                panelTransaction.setAnimator(panelState.getId(), animator);
            } else if (!panelState.isAnimating()) {
                // Force apply the new state if there is no on going animation.
                panelState.setVariant(toVariant.getId(), event);
                applyState(panelState);
            }
            Log.d(TAG, "add transition for " + panelState.getId());
            panelTransaction.setPanelTransaction(panelState.getId(), transition);
        }
        return panelTransaction;
    }

    /**
     * Applies the current state of a panel to the UI. This method updates the panel's
     * visual properties (bounds, visibility, alpha, layer) based on its current variant.
     *
     * @param panelState The panel data containing the current state information.
     */
    public static void applyState(PanelState panelState) {
        Variant variant = panelState.getCurrentVariant();
        String panelId = panelState.getId();
        Panel panel = PanelPool.getInstance().getPanel(panelId);
        panel.setRole(panelState.getRole().getValue());
        panel.setBounds(variant.getBounds());
        panel.setVisibility(variant.isVisible());
        panel.setAlpha(variant.getAlpha());
        panel.setLayer(variant.getLayer());
        panel.setDisplayId(panelState.getDisplayId());
    }

    //TODO(b/390006880): make this part of configuration.

    /**
     * Resets all the panels.
     */
    public static void handlePanelReset() {
        for (PanelState panelState : getInstance().mPanelStates.values()) {
            PanelPool.getInstance().getPanel(panelState.getId()).reset();
        }
    }

    /**
     * Retrieves a {@link PanelState} with the given id, or null if none is found.
     */
    @Nullable
    public static PanelState getPanelState(String id) {
        return getInstance().mPanelStates.getOrDefault(id, null);
    }

    @VisibleForTesting
    Map<String, PanelState> getPanelStates() {
        return mPanelStates;
    }
}