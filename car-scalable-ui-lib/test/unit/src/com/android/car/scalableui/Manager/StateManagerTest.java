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

package com.android.car.scalableui.manager;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Role;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StateManagerTest {
    private static final String TEST_PANEL_ID = "panel_id";
    private static final String TO_VARIANT_ID = "TO_VARIANT_ID";
    private static final String TEST_EVENT = "TEST_EVENT";

    @Before
    public void setUp() {
        StateManager.clearStates();
    }

    @Test
    public void testHandleEvent_withTransition() {
        PanelState mockPanelState = mock(PanelState.class);
        when(mockPanelState.getId()).thenReturn(TEST_PANEL_ID);
        when(mockPanelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        when(mockPanelState.getCurrentVariant()).thenReturn(mock(Variant.class));
        when(mockPanelState.isAnimating()).thenReturn(false);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, mockPanelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant(TO_VARIANT_ID, null));
        when(mockPanelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        Animator mockAnimator = mock(Animator.class);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(
                mockAnimator);

        PanelTransaction panelTransaction = StateManager.handleEvent(new Event(TEST_EVENT));

        verify(mockPanelState).setVariant(TO_VARIANT_ID, /* payload= */ null);
        verify(mockPanelState).onAnimationStart(mockAnimator);
        verify(mockAnimator).removeAllListeners();
        verify(mockAnimator).addListener(any(AnimatorListenerAdapter.class));
        assertThat(panelTransaction.getAnimators()).hasSize(/* expectedSize= */ 1);
        assertThat(panelTransaction.getPanelTransactionStates()).hasSize(/* expectedSize= */ 1);
    }

    @Test
    public void testHandleEvent_withoutTransition() {
        PanelState mockPanelState = mock(PanelState.class);
        when(mockPanelState.getId()).thenReturn(TEST_PANEL_ID);
        when(mockPanelState.getTransition(any(Event.class))).thenReturn(null);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, mockPanelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);

        StateManager.handleEvent(new Event(TEST_EVENT));

        // Verify that no state changes or animations are applied
        verify(mockPanelState, never()).setVariant(any(String.class), any());
        verify(mockPanel, never()).setBounds(any(Rect.class));
        verify(mockPanel, never()).setVisibility(any(Boolean.class));
        verify(mockPanel, never()).setAlpha(any(Float.class));
        verify(mockPanel, never()).setLayer(any(Integer.class));
    }

    @Test
    public void testHandleEvent_withTransitionWithoutAnimation() {
        PanelState mockPanelState = mock(PanelState.class);
        when(mockPanelState.getId()).thenReturn(TEST_PANEL_ID);
        when(mockPanelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        when(mockPanelState.getCurrentVariant()).thenReturn(mock(Variant.class));
        when(mockPanelState.isAnimating()).thenReturn(false);
        when(mockPanelState.getRole()).thenReturn(new Role(0));
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, mockPanelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        when(PanelPool.getInstance().getPanel(anyString())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant(TO_VARIANT_ID, null));
        when(mockPanelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(null);

        StateManager.handleEvent(new Event(TEST_EVENT));

        verify(mockPanelState).setVariant(TO_VARIANT_ID, /* payload= */null);
    }

    @Test
    public void testApplyState() {
        int roleValue = 1;
        PanelState mockPanelState = mock(PanelState.class);
        when(mockPanelState.getId()).thenReturn(TEST_PANEL_ID);
        when(mockPanelState.getRole()).thenReturn(new Role(roleValue));
        when(mockPanelState.isLaunchRoot()).thenReturn(false);
        when(mockPanelState.getDisplayId()).thenReturn(0);
        Variant mockVariant = mock(Variant.class);
        when(mockPanelState.getCurrentVariant()).thenReturn(mockVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, mockPanelState);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        Panel mockPanel = mock(Panel.class);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        when(PanelPool.getInstance().getPanel(TEST_PANEL_ID)).thenReturn(mockPanel);

        StateManager.applyState(mockPanelState);

        verify(mockPanel).setRole(roleValue);
        verify(mockPanel).setBounds(mockVariant.getBounds());
        verify(mockPanel).setVisibility(mockVariant.isVisible());
        verify(mockPanel).setAlpha(mockVariant.getAlpha());
        verify(mockPanel).setLayer(mockVariant.getLayer());
        verify(mockPanel).setLaunchRoot(false);
        verify(mockPanel).setDisplayId(0);
    }

    @Test
    public void testHandlePanelReset() {
        final String testPanel1 = "testPanel1";
        final String testPanel2 = "testPanel2";

        PanelState mockPanelState1 = mock(PanelState.class);
        when(mockPanelState1.getId()).thenReturn(testPanel1);
        PanelState mockPanelState2 = mock(PanelState.class);
        when(mockPanelState2.getId()).thenReturn(testPanel2);
        StateManager.getInstance().getPanelStates().put(testPanel1, mockPanelState1);
        StateManager.getInstance().getPanelStates().put(testPanel2, mockPanelState2);

        Panel mockPanel1 = mock(Panel.class);
        Panel mockPanel2 = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(testPanel1)).thenReturn(mockPanel1);
        when(delegate.createPanel(testPanel2)).thenReturn(mockPanel2);
        when(PanelPool.getInstance().getPanel(testPanel1)).thenReturn(mockPanel1);
        when(PanelPool.getInstance().getPanel(testPanel2)).thenReturn(mockPanel2);

        StateManager.handlePanelReset();

        verify(mockPanel1, times(/*wantedNumberOfInvocations=*/ 1)).reset();
        verify(mockPanel2, times(/*wantedNumberOfInvocations=*/ 1)).reset();
    }

    @Test
    public void testGetPanelState() {
        PanelState mockPanelState = mock(PanelState.class);
        when(mockPanelState.getId()).thenReturn(TEST_PANEL_ID);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, mockPanelState);

        PanelState retrievedPanelState = StateManager.getPanelState(TEST_PANEL_ID);

        assertThat(retrievedPanelState).isEqualTo(mockPanelState);
    }
}
