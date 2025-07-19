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
import android.os.Build;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.car.scalableui.metrics.MetricsHelper;
import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelTransaction;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;
import com.android.internal.jank.InteractionJankMonitor;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Manages the state of UI panels. This class is responsible for loading panel definitions,
 * handling events that trigger state transitions, and applying visual updates to panels
 * based on their current state.
 */
public class StateManager {
    private static final String TAG = StateManager.class.getSimpleName();
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;

    private static final StateManager sInstance = new StateManager();

    private final Map<String, PanelState> mPanelStates;

    private static final MetricsHelper sMetricsHelper = new MetricsHelper(
            InteractionJankMonitor.getInstance(),
            MetricsHelper.MetricsCujMapping.getInstance(PanelPool.getInstance()));
    private final ArraySet<PanelStateObserverData> mObservers = new ArraySet<>();

    private StateManager() {
        mPanelStates = new HashMap<>();
    }

    /** Clear all panel states. */
    public static void clearStates() {
        sInstance.mPanelStates.clear();
    }

    /**
     * Get shallow copy of the current PanelStates {@link StateManager#mPanelStates}
     * @return Snapshot of the current Map of PanelState.
     *
     */
    private static Map<String, PanelState> getCurrentPanelStatesCopy() {
        return sInstance.mPanelStates.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().clone()
                ));
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
    public static void addState(PanelState panelState) {
        if (sInstance.mPanelStates.put(panelState.getId(), panelState) != null) {
            if (DEBUG) {
                Log.w(TAG, "Previous PanelState with id=" + panelState.getId() + " got replaced");
            }
        }
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
        logIfDebuggable("handleEvent " + event);
        PanelTransaction.Builder panelTransactionBuilder = new PanelTransaction.Builder();
        HashSet<String> changedPanelIds = new HashSet<>();
        // Make a ShallowCopy of the currentPanelStates.
        Map<String, PanelState> currentPanelStatesCopy = getCurrentPanelStatesCopy();
        for (PanelState panelState : sInstance.mPanelStates.values()) {
            if (panelState == null) {
                Log.e(TAG, "panel state is null");
                continue;
            }
            Transition transition = panelState.getTransition(event);
            if (transition == null) {
                Log.e(TAG, "transition is null for " + panelState.getId());
                panelTransactionBuilder.addLockedPanelId(panelState.getId());
                continue;
            }
            Panel panel = PanelPool.getInstance().getPanel(panelState.getId());

            Variant toVariant = transition.getToVariant();
            Variant fromVariant = panelState.getCurrentVariant();

            if (fromVariant == null) {
                logIfDebuggable("fromVariant is null for " + panel.getPanelId());
                continue;
            } else if (toVariant == null) {
                // This should never happen, but observe if there is a bad config, add the check
                // for now and enforce in Transition later.
                Log.e(TAG, "toVariant is null for " + panel.getPanelId() + ", transition="
                        + toVariant);
                continue;
            } else if (Objects.equals(fromVariant, toVariant)
                    && !(toVariant instanceof KeyFrameVariant)) {
                // Fraction in KeyFrameVariant is not updated at this point, cannot use for
                // comparison.
                logIfDebuggable("FromVariant is the same as toVariant, " + panelState.getId());
                continue;
            }

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
                logIfDebuggable("add animator for " + panelState.getId());
                panelTransactionBuilder.addAnimator(panelState.getId(), animator);
                changedPanelIds.add(panelState.getId());
            } else if (!panelState.isAnimating()) {
                // Force apply the new state if there is no on going animation.
                logIfDebuggable("No animator for " + panelState.getId());
                panelState.setVariant(toVariant.getId(), event);
                applyState(panelState);
            }
            logIfDebuggable("add transition for " + panelState.getId());
            if (toVariant instanceof KeyFrameVariant) {
                panelTransactionBuilder.setHasWindowChanges(false);
            }
            panelTransactionBuilder.addPanelTransaction(panelState.getId(), transition);
        }
        if (!changedPanelIds.isEmpty()) {
            // Store copy of existing panel state in case it changes prior to callback
            Map<String, PanelState> panelStatesCopy = sInstance.createPanelStateCopy();
            panelTransactionBuilder.setAnimationStartCallbackRunnable(
                    sInstance.getBeforePanelStateChangeRunnable(changedPanelIds, panelStatesCopy));
            panelTransactionBuilder.setAnimationEndCallbackRunnable(
                    sInstance.getAfterPanelStateChangeRunnable(changedPanelIds, panelStatesCopy));
        }
        PanelTransaction panelTransaction = panelTransactionBuilder.build();
        if (sMetricsHelper != null) {
            sMetricsHelper.recordJankCuj(event, panelTransaction.getAnimators(),
                    currentPanelStatesCopy, sInstance.mPanelStates);
        }
        return panelTransactionBuilder.build();
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
        panel.setRole(panelState.getRole());
        panel.setBounds(variant.getBounds());
        panel.setVisibility(variant.isVisible());
        panel.setAlpha(variant.getAlpha());
        panel.setLayer(variant.getLayer());
        panel.setCanFocusOnTransition(variant.canFocusOnTransition());
        panel.setDisplayId(panelState.getDisplayId());
        panel.setInsets(variant.getInsets());
        panel.setCornerRadius(variant.getCornerRadius());
        // KeyFrameVariant might not have safe bounds.
        if (!(variant instanceof KeyFrameVariant)) {
            panel.setSafeBounds(variant.getSafeBounds());
        }
        panel.setPanelControllerMetadata(panelState.getPanelControllerMetadata());
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
     * Reloads {@link PanelState}.
     */
    public static void reloadPanelState(List<PanelState> panelStates) {
        for (PanelState panelState : panelStates) {
            if (sInstance.mPanelStates.put(panelState.getId(), panelState) != null) {
                if (DEBUG) {
                    Log.w(TAG, "PanelState with id=" + panelState.getId() + " got reloaded");
                }
            }
            applyState(panelState);
            handlePanelReset();
        }
    }

    /**
     * Retrieves a {@link PanelState} with the given id, or null if none is found.
     */
    @Nullable
    public static PanelState getPanelState(String id) {
        return getInstance().mPanelStates.getOrDefault(id, null);
    }

    /**
     * Dump all the current PanelStates to the output stream.
     */
    public static void dumpPanelStates(PrintWriter pw) {
        for (PanelState panelState : getInstance().mPanelStates.values()) {
            pw.println(panelState.toShortString());
        }
    }

    @VisibleForTesting
    Map<String, PanelState> getPanelStates() {
        return mPanelStates;
    }

    private static void logIfDebuggable(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * Add an observer to the panel state
     *
     * @param observer the observer
     * @param panelIds the panel ids to observe
     */
    public void addPanelStateObserver(PanelStateObserver observer, String... panelIds) {
        synchronized (mObservers) {
            removePanelStateObserver(observer);
            mObservers.add(new PanelStateObserverData(observer, panelIds));
        }
    }

    /**
     * Remove a panel state observer
     */
    public void removePanelStateObserver(PanelStateObserver observer) {
        synchronized (mObservers) {
            mObservers.removeIf(element -> element.mObserver == observer);
        }
    }

    private Runnable getBeforePanelStateChangeRunnable(Set<String> changedPanelIds,
            Map<String, PanelState> panelStates) {
        return () -> notifyPanelStateChange(changedPanelIds, panelStates, /* before= */ true);
    }

    private Runnable getAfterPanelStateChangeRunnable(Set<String> changedPanelIds,
            Map<String, PanelState> panelStates) {
        return () -> notifyPanelStateChange(changedPanelIds, panelStates, /* before= */ false);
    }

    private void notifyPanelStateChange(Set<String> changedPanelIds,
            Map<String, PanelState> panelStates, boolean before) {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.execute(() -> {
                synchronized (mObservers) {
                    for (PanelStateObserverData data : mObservers) {
                        if (data.observesPanel(changedPanelIds)) {
                            if (before) {
                                data.mObserver.onBeforePanelStateChanged(changedPanelIds,
                                        panelStates);
                            } else {
                                data.mObserver.onPanelStateChanged(changedPanelIds,
                                        panelStates);
                            }
                        }
                    }
                }
            });
        }
    }

    private Map<String, PanelState> createPanelStateCopy() {
        Map<String, PanelState> panelStatesCopy = new HashMap<>();
        mPanelStates.forEach((key, value) -> {
            panelStatesCopy.put(key, new PanelState(value));
        });
        return panelStatesCopy;
    }

    @VisibleForTesting
    void clearPanelStateObservers() {
        synchronized (mObservers) {
            mObservers.clear();
        }
    }

    public interface PanelStateObserver {
        /**
         * Notify of a panel state change that has just started
         *
         * @param changedPanelIds the panelIds that are changing
         * @param toPanelStates   the panel states from after the change
         */
        void onBeforePanelStateChanged(Set<String> changedPanelIds,
                Map<String, PanelState> toPanelStates);

        /**
         * Notify of a panel state change that has finished
         *
         * @param changedPanelIds the panelIds that have changed
         * @param toPanelStates   the panel states from after the change
         */
        void onPanelStateChanged(Set<String> changedPanelIds,
                Map<String, PanelState> toPanelStates);
    }

    private static class PanelStateObserverData {
        final PanelStateObserver mObserver;
        final ArraySet<String> mPanelIds = new ArraySet<>();

        PanelStateObserverData(PanelStateObserver observer, String... panelIds) {
            mObserver = observer;
            mPanelIds.addAll(Arrays.asList(panelIds));
        }

        boolean observesPanel(Set<String> changedPanelIds) {
            if (mPanelIds.isEmpty()) {
                return true;
            }
            if (changedPanelIds.isEmpty()) {
                return false;
            }
            return !Collections.disjoint(mPanelIds, changedPanelIds);
        }
    }
}
