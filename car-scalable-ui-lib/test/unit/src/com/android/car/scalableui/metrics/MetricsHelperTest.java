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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.util.Pair;
import android.view.SurfaceControl;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.panel.Panel;
import com.android.internal.jank.Cuj;
import com.android.internal.jank.InteractionJankMonitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class MetricsHelperTest {
    private MetricsHelper.MetricsCujMapping mMetricsCujMapping;
    private InteractionJankMonitor mInteractionJankMonitor;
    private MetricsHelper mMetricsHelper;
    private Panel mPanel;
    private Context mContext;
    private Handler mHandler;
    private SurfaceControl mSurfaceControl;


    @Before
    public void setUp() {
        mInteractionJankMonitor = mock(InteractionJankMonitor.class);
        mMetricsCujMapping = mock(MetricsHelper.MetricsCujMapping.class);
        mMetricsHelper = new MetricsHelper(mInteractionJankMonitor, mMetricsCujMapping);
        mPanel = mock(Panel.class);
        mContext = mock(Context.class);
        mHandler = mock(Handler.class);
        mSurfaceControl = mock(SurfaceControl.class);
        when(mPanel.getLeash()).thenReturn(mSurfaceControl);
        when(mPanel.getContext()).thenReturn(mContext);
        when(mContext.getMainThreadHandler()).thenReturn(mHandler);
    }

    @Test
    public void recordJankCuj_noCujMapping_doesNothing() {
        Event event = new Event.Builder("test_event_id").build();
        Set<Map.Entry<String, Animator>> panelAnimators = Collections.emptySet();
        Map<String, PanelState> stateBefore = new HashMap<>();
        Map<String, PanelState> stateAfter = new HashMap<>();

        when(mMetricsCujMapping.getMappedCuj(anyList(), any(), any()))
                .thenReturn(null);

        mMetricsHelper.recordJankCuj(Collections.singletonList(event), panelAnimators, stateBefore,
                stateAfter);

        verify(mInteractionJankMonitor, never()).begin(any(), any(), any(), anyInt());
        verify(mInteractionJankMonitor, never()).end(anyInt());
        verify(mInteractionJankMonitor, never()).cancel(anyInt());
    }

    @Test
    public void recordJankCuj_singleAnimator_animationEnds_jankMonitorEnds() {
        Event event = new Event.Builder("test_event_id").build();
        Animator animator = mock(Animator.class);
        Set<Map.Entry<String, Animator>> panelAnimators = new HashSet<>();
        panelAnimators.add(Map.entry("test_panel_id_1", animator));
        Map<String, PanelState> stateBefore = new HashMap<>();
        Map<String, PanelState> stateAfter = new HashMap<>();

        Pair<Panel, Integer> cuj = Pair.create(mPanel, Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);
        when(mMetricsCujMapping.getMappedCuj(anyList(), any(), any()))
                .thenReturn(cuj);

        mMetricsHelper.recordJankCuj(Collections.singletonList(event), panelAnimators, stateBefore,
                stateAfter);

        verify(mInteractionJankMonitor).begin(mSurfaceControl, mContext, mHandler,
                Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);

        ArgumentCaptor<Animator.AnimatorListener> listenerCaptor =
                ArgumentCaptor.forClass(Animator.AnimatorListener.class);
        verify(animator).addListener(listenerCaptor.capture());

        listenerCaptor.getValue().onAnimationEnd(animator);

        verify(mInteractionJankMonitor).end(Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);
        verify(mInteractionJankMonitor, never()).cancel(anyInt());
    }

    @Test
    public void recordJankCuj_singleAnimator_animationCancels_jankMonitorCancels() {
        Event event = new Event.Builder("test_event_id").build();
        Animator animator = mock(Animator.class);
        Set<Map.Entry<String, Animator>> panelAnimators = new HashSet<>();
        panelAnimators.add(Map.entry("test_panel_id_1", animator));
        Map<String, PanelState> stateBefore = new HashMap<>();
        Map<String, PanelState> stateAfter = new HashMap<>();

        Pair<Panel, Integer> cuj = Pair.create(mPanel, Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);
        when(mMetricsCujMapping.getMappedCuj(anyList(), any(), any()))
                .thenReturn(cuj);

        mMetricsHelper.recordJankCuj(Collections.singletonList(event), panelAnimators, stateBefore,
                stateAfter);

        verify(mInteractionJankMonitor).begin(mSurfaceControl, mContext, mHandler,
                Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);

        ArgumentCaptor<Animator.AnimatorListener> listenerCaptor =
                ArgumentCaptor.forClass(Animator.AnimatorListener.class);
        verify(animator).addListener(listenerCaptor.capture());

        listenerCaptor.getValue().onAnimationCancel(animator);

        verify(mInteractionJankMonitor).cancel(Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME);
        verify(mInteractionJankMonitor, never()).end(anyInt());
    }
}
