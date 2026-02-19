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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;
import com.android.internal.jank.Cuj;
import com.android.internal.jank.InteractionJankMonitor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Helper class to facilitate recording jank metrics for Critical User Journeys (CUJs) using {@link
 * InteractionJankMonitor}. It coordinates the beginning and ending/canceling of jank monitoring
 * based on UI events and associated animations.
 */
public class MetricsHelper {

    public static final String TAG = MetricsHelper.class.getSimpleName();
    private final InteractionJankMonitor mInteractionJankMonitor;
    private final MetricsCujMapping mMetricsCujMapping;

    /**
     * Constructs a new MetricsHelper.
     *
     * @param interactionJankMonitor The {@link InteractionJankMonitor} instance used to record
     *     jank.
     * @param metricsCujMapping The {@link MetricsCujMapping} instance used to map events and states
     *     to specific CUJs.
     */
    public MetricsHelper(
            InteractionJankMonitor interactionJankMonitor, MetricsCujMapping metricsCujMapping) {
        mInteractionJankMonitor = interactionJankMonitor;
        mMetricsCujMapping = metricsCujMapping;
    }

    /**
     * Records a Critical User Journey (CUJ) for jank monitoring based on the triggering event and
     * the animations involved.
     *
     * <p>This method attempts to map the given event and initial panel state to a specific CUJ
     * using {@link MetricsCujMapping}. If a mapping is found, it begins monitoring using {@link
     * InteractionJankMonitor#begin}.
     *
     * @param events The list of {@link Event} that triggered the potential CUJ.
     * @param panelAnimators A Set of PanelId {@link String} to Animator {@link Animator} pairs
     *     associated with the UI transition for this event.
     * @param stateBefore A collection representing the {@link PanelState} of relevant UI panels
     *     *before* the event occurred. Used for CUJ mapping.
     * @param stateAfter A collection representing the {@link PanelState} of relevant UI panels
     *     *after* the event occurred. Used for CUJ mapping.
     */
    public void recordJankCuj(
            List<Event> events,
            Set<Map.Entry<String, Animator>> panelAnimators,
            Map<String, PanelState> stateBefore,
            Map<String, PanelState> stateAfter) {
        Pair<Panel, Integer> cuj = mMetricsCujMapping.getMappedCuj(events, stateBefore, stateAfter);
        if (cuj == null || cuj.first.getLeash() == null) {
            // No CUJ found
            return;
        }
        mInteractionJankMonitor.begin(
                cuj.first.getLeash(),
                cuj.first.getContext(),
                cuj.first.getContext().getMainThreadHandler(),
                cuj.second);
        // Total count of the animations involved in this cuj.
        int panelAnimatorCount = panelAnimators.size();
        final AtomicInteger totalEndedAnimation = new AtomicInteger(0);
        final AtomicInteger totalCancelledAnimation = new AtomicInteger(0);

        panelAnimators.forEach(
                new Consumer<>() {
                    @Override
                    public void accept(Map.Entry<String, Animator> animatorEntry) {
                        animatorEntry
                                .getValue()
                                .addListener(
                                        new AnimatorListenerAdapter() {

                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                                super.onAnimationCancel(animation);
                                                // Increment and check if this is the last cancelled
                                                // animation.
                                                if (panelAnimatorCount
                                                        == totalCancelledAnimation
                                                                .incrementAndGet()) {
                                                    mInteractionJankMonitor.cancel(cuj.second);
                                                }
                                                // Increment endAnimation, as one of the animations
                                                // have been cancelled
                                                totalEndedAnimation.incrementAndGet();
                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                // Increment and check if this is the last ended
                                                // animation.
                                                if (panelAnimatorCount
                                                        == totalEndedAnimation.incrementAndGet()) {
                                                    mInteractionJankMonitor.end(cuj.second);
                                                }
                                            }
                                        });
                    }
                });
    }

    /**
     * Manages the mapping between UI events/states and specific Interaction Jank Monitor CUJs.
     *
     * <p>This class follows the Singleton pattern and requires a {@link PanelPool} to retrieve
     * panel information needed for mapping. TODO (b/409561895) : Improve the mapping functionality
     * as more CUJ use cases are defined.
     */
    public static class MetricsCujMapping {

        private static MetricsCujMapping sInstance;
        private final PanelPool mPanelPool;

        /**
         * Gets the Singleton instance of the MetricsCujMapping. Creates the instance on the first
         * call.
         *
         * @param panelPool The {@link PanelPool} used to retrieve {@link Panel} instances. Required
         *     for mapping CUJs to specific panels.
         * @return The Singleton instance of {@link MetricsCujMapping}.
         */
        public static MetricsCujMapping getInstance(PanelPool panelPool) {
            if (sInstance == null) {
                sInstance = new MetricsCujMapping(panelPool);
            }
            return sInstance;
        }

        /**
         * Private constructor for Singleton pattern.
         *
         * @param panelPool The {@link PanelPool} instance.
         */
        private MetricsCujMapping(PanelPool panelPool) {
            mPanelPool = panelPool;
        }

        /**
         * Gets the associated integer identifier for a CUJ by matching the event and state changes
         * against a central registry of CUJ definitions.
         *
         * @param events The list of {@link Event}s that triggered the UI change.
         * @param stateBefore A map of Panel ID to {@link PanelState} before the event.
         * @param stateAfter A map of Panel ID to {@link PanelState} after the event.
         * @return A {@link Pair} containing the relevant {@link Panel} and the integer {@link
         *     Cuj.CujType} identifier if a match is found; otherwise, {@code null}.
         */
        @Nullable
        Pair<Panel, Integer> getMappedCuj(
                List<Event> events,
                Map<String, PanelState> stateBefore,
                Map<String, PanelState> stateAfter) {
            // Iterate through all known CUJ definitions.
            for (CujDefinition definition : CujRegistry.getDefinitions()) {
                // Iterate all provided events
                for (Event event : events) {
                    if (definition.matches(event, stateBefore, stateAfter)) {
                        // We found a matching CUJ.
                        Panel relevantPanel = mPanelPool.getPanel(definition.getRelevantPanelId());
                        if (relevantPanel != null) {
                            return Pair.create(relevantPanel, definition.getCujType());
                        }
                    }
                }
            }
            // No CUJ definition matched the current situation.
            return null;
        }
    }
}
