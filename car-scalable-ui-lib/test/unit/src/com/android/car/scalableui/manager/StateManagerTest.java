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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelTransaction;
import com.android.car.scalableui.model.Role;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.Panel;
import com.android.car.scalableui.panel.PanelPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class StateManagerTest {
    private static final String TEST_PANEL_ID = "panel_id";
    private static final String TO_VARIANT_ID = "TO_VARIANT_ID";
    private static final String FROM_VARIANT_ID = "FROM_VARIANT_ID";
    private static final Event TEST_EVENT = new Event.Builder("TEST_EVENT").build();
    private static final Role DEFAULT_ROLE = new Role.Builder().setLayoutId(1).build();

    @Before
    public void setUp() {
        StateManager.clearStates();
        StateManager.getInstance().clearPanelStateObservers();
    }

    @After
    public void cleanUp() {
        PanelPool.getInstance().clearPanels();
    }

    @Test
    public void testHandleEvent_withTransition() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockFromVariant = mock(Variant.class);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        Variant mockToVariant = mock(Variant.class);
        when(mockToVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockToVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant.Builder(TO_VARIANT_ID).build());
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        Animator mockAnimator = mock(Animator.class);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(
                mockAnimator);

        PanelTransaction panelTransaction = StateManager.handleEvent(TEST_EVENT);

        verify(panelState).setVariant(TO_VARIANT_ID, TEST_EVENT);
        verify(panelState).onAnimationStart(mockAnimator);
        verify(mockAnimator).removeAllListeners();
        verify(mockAnimator).addListener(any(AnimatorListenerAdapter.class));
        assertThat(panelTransaction.getAnimators()).hasSize(/* expectedSize= */ 1);
        assertThat(panelTransaction.getPanelTransactionStates()).hasSize(/* expectedSize= */ 1);
    }

    @Test
    public void testHandleEvent_withoutTransition() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(null);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);

        StateManager.handleEvent(TEST_EVENT);

        // Verify that no state changes or animations are applied
        verify(panelState, never()).setVariant(any(String.class), any(Event.class));
        verify(mockPanel, never()).setBounds(any(Rect.class));
        verify(mockPanel, never()).setVisibility(any(Boolean.class));
        verify(mockPanel, never()).setAlpha(any(Float.class));
        verify(mockPanel, never()).setLayer(any(Integer.class));
    }

    @Test
    public void testHandleEvent_noVariantChange() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockFromVariant = mock(Variant.class);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        Variant mockToVariant = mock(Variant.class);
        when(mockToVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockToVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(mockFromVariant);
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        Animator mockAnimator = mock(Animator.class);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(
                mockAnimator);

        PanelTransaction panelTransaction = StateManager.handleEvent(TEST_EVENT);

        verify(panelState, never()).setVariant(TO_VARIANT_ID, TEST_EVENT);
        verify(panelState, never()).onAnimationStart(mockAnimator);
        verify(mockAnimator, never()).removeAllListeners();
        verify(mockAnimator, never()).addListener(any(AnimatorListenerAdapter.class));
        assertThat(panelTransaction.getAnimators()).hasSize(/* expectedSize= */ 0);
        assertThat(panelTransaction.getPanelTransactionStates()).hasSize(/* expectedSize= */ 0);
    }

    @Test
    public void testHandleEvent_keyFrameVariant() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockFromVariant = mock(KeyFrameVariant.class);
        when(mockFromVariant.getId()).thenReturn(FROM_VARIANT_ID);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        Variant mockToVariant = mock(Variant.class);
        when(mockToVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockToVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(mockFromVariant);
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(
                null);

        PanelTransaction panelTransaction = StateManager.handleEvent(TEST_EVENT);

        assertThat(panelTransaction.hasWindowChanges()).isFalse();
        assertThat(panelTransaction.getAnimators()).hasSize(/* expectedSize= */ 0);
        assertThat(panelTransaction.getPanelTransactionStates()).hasSize(/* expectedSize= */ 1);
    }

    @Test
    public void testHandleEvent_withTransitionWithoutAnimation() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        Variant mockFromVariant = mock(Variant.class);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockVariant = mock(Variant.class);
        when(mockVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant.Builder(TO_VARIANT_ID).build());
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(null);

        StateManager.handleEvent(TEST_EVENT);

        verify(panelState).setVariant(TO_VARIANT_ID, TEST_EVENT);
    }

    @Test
    public void testApplyState() {
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        Variant mockFromVariant = mock(Variant.class);
        when(mockFromVariant.getId()).thenReturn(FROM_VARIANT_ID);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        Variant mockVariant = mock(Variant.class);
        when(mockVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        Panel mockPanel = mock(Panel.class);
        when(delegate.createPanel(any())).thenReturn(mockPanel);

        StateManager.applyState(panelState);

        verify(mockPanel).setRole(DEFAULT_ROLE);
        verify(mockPanel).setBounds(mockVariant.getBounds());
        verify(mockPanel).setVisibility(mockVariant.isVisible());
        verify(mockPanel).setAlpha(mockVariant.getAlpha());
        verify(mockPanel).setLayer(mockVariant.getLayer());
        verify(mockPanel).setDisplayId(0);
        verify(mockPanel).setCornerRadius(mockVariant.getCornerRadius());
    }

    @Test
    public void testHandlePanelReset() {
        final String testPanel1 = "testPanel1";
        final String testPanel2 = "testPanel2";

        PanelState panelState1 = spy(new PanelState(testPanel1, DEFAULT_ROLE));
        PanelState panelState2 = spy(new PanelState(testPanel2, DEFAULT_ROLE));
        StateManager.getInstance().getPanelStates().put(testPanel1, panelState1);
        StateManager.getInstance().getPanelStates().put(testPanel2, panelState2);

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
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        PanelState retrievedPanelState = StateManager.getPanelState(TEST_PANEL_ID);

        assertThat(retrievedPanelState).isEqualTo(panelState);
    }

    @Test
    public void testPanelStateListener_withAnimation() {
        TestPanelStateObserver observer = new TestPanelStateObserver();
        StateManager.getInstance().addPanelStateObserver(observer);
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockFromVariant = mock(Variant.class);
        when(panelState.getCurrentVariant()).thenReturn(mockFromVariant);
        Variant mockVariant = mock(Variant.class);
        when(mockVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant.Builder(TO_VARIANT_ID).build());
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        Animator mockAnimator = mock(Animator.class);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(
                mockAnimator);

        PanelTransaction panelTransaction = StateManager.handleEvent(TEST_EVENT);
        assertThat(panelTransaction.getAnimationStartCallbackRunnable()).isNotNull();
        assertThat(panelTransaction.getAnimationEndCallbackRunnable()).isNotNull();

        panelTransaction.getAnimationStartCallbackRunnable().run();
        assertThat(observer.mOnBeforePanelStateChangedCalled).isTrue();
        assertThat(observer.mOnPanelStateChangedCalled).isFalse();

        panelTransaction.getAnimationEndCallbackRunnable().run();
        assertThat(observer.mOnPanelStateChangedCalled).isTrue();
    }

    @Test
    public void testPanelStateListener_withoutAnimation() {
        TestPanelStateObserver observer = new TestPanelStateObserver();
        StateManager.getInstance().addPanelStateObserver(observer);
        PanelState panelState = spy(new PanelState(TEST_PANEL_ID, DEFAULT_ROLE));
        when(panelState.getTransition(any(Event.class))).thenReturn(mock(Transition.class));
        Variant mockVariant = mock(Variant.class);
        when(mockVariant.getId()).thenReturn(TO_VARIANT_ID);
        panelState.addVariant(mockVariant);
        StateManager.getInstance().getPanelStates().put(TEST_PANEL_ID, panelState);

        Panel mockPanel = mock(Panel.class);
        PanelPool.PanelCreatorDelegate delegate = mock(PanelPool.PanelCreatorDelegate.class);
        PanelPool.getInstance().setDelegate(delegate);
        when(delegate.createPanel(any())).thenReturn(mockPanel);
        when(PanelPool.getInstance().getPanel(anyString())).thenReturn(mockPanel);
        Transition mockTransition = mock(Transition.class);
        when(mockTransition.getToVariant()).thenReturn(new Variant.Builder(TO_VARIANT_ID).build());
        when(panelState.getTransition(any(Event.class))).thenReturn(mockTransition);
        when(mockTransition.getAnimator(any(Panel.class), any(Variant.class))).thenReturn(null);

        PanelTransaction panelTransaction = StateManager.handleEvent(TEST_EVENT);

        assertThat(panelTransaction.getAnimationStartCallbackRunnable()).isNull();
        assertThat(panelTransaction.getAnimationEndCallbackRunnable()).isNull();
    }

    private static class TestPanelStateObserver implements StateManager.PanelStateObserver {
        private boolean mOnBeforePanelStateChangedCalled = false;
        private boolean mOnPanelStateChangedCalled = false;

        @Override
        public void onBeforePanelStateChanged(Set<String> changedPanelIds,
                Map<String, PanelState> panelStates) {
            mOnBeforePanelStateChangedCalled = true;
        }

        @Override
        public void onPanelStateChanged(Set<String> changedPanelIds,
                Map<String, PanelState> panelStates) {
            mOnPanelStateChangedCalled = true;
        }
    }
}
