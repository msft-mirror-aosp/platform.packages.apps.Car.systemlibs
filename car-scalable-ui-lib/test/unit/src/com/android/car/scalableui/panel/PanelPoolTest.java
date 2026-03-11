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
package com.android.car.scalableui.panel;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.model.PanelType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class PanelPoolTest {

    private PanelPool mPanelPool;
    private PanelPool.PanelCreatorDelegate mMockDelegate;

    @Before
    public void setUp() {
        // PanelPool is a singleton, so we need to clear it before each test to ensure isolation.
        mPanelPool = PanelPool.getInstance();
        mPanelPool.clearPanels();
        mMockDelegate = mock(PanelPool.PanelCreatorDelegate.class);
        mPanelPool.setDelegate(mMockDelegate);
    }

    @Test
    public void getInstance_returnsSingleton() {
        PanelPool instance1 = PanelPool.getInstance();
        PanelPool instance2 = PanelPool.getInstance();
        assertThat(instance1).isSameInstanceAs(instance2);
    }

    @Test
    public void getOrCreatePanel_createsNewPanel_whenNotExists() {
        String panelId = "test_panel_1";
        int panelType = PanelType.TASK;
        Panel mockPanel = mock(Panel.class);
        when(mMockDelegate.createPanel(panelId, panelType)).thenReturn(mockPanel);

        Panel createdPanel = mPanelPool.getOrCreatePanel(panelId, panelType);

        assertThat(createdPanel).isEqualTo(mockPanel);
        verify(mMockDelegate).createPanel(panelId, panelType);
    }

    @Test
    public void getOrCreatePanel_returnsExistingPanel_whenExists() {
        String panelId = "test_panel_1";
        int panelType = PanelType.TASK;
        Panel mockPanel = mock(Panel.class);
        when(mMockDelegate.createPanel(panelId, panelType)).thenReturn(mockPanel);

        // First call creates the panel
        mPanelPool.getOrCreatePanel(panelId, panelType);
        // Second call should retrieve the existing one
        Panel retrievedPanel = mPanelPool.getOrCreatePanel(panelId, panelType);

        assertThat(retrievedPanel).isEqualTo(mockPanel);
        // Verify delegate was only called once
        verify(mMockDelegate, times(1)).createPanel(panelId, panelType);
    }

    @Test
    public void getPanel_byId_returnsCorrectPanel() {
        String panelId = "test_panel_1";
        Panel mockPanel = mock(Panel.class);
        when(mMockDelegate.createPanel(panelId, PanelType.TASK)).thenReturn(mockPanel);
        mPanelPool.getOrCreatePanel(panelId, PanelType.TASK);

        Panel foundPanel = mPanelPool.getPanel(panelId);

        assertThat(foundPanel).isEqualTo(mockPanel);
    }

    @Test
    public void getPanel_byId_returnsNull_whenNotExists() {
        Panel foundPanel = mPanelPool.getPanel("non_existent_panel");
        assertThat(foundPanel).isNull();
    }

    @Test
    public void getPanel_byPredicate_returnsFirstMatchingPanel() {
        Panel mockPanel1 = mock(Panel.class);
        when(mockPanel1.getPanelId()).thenReturn("panel1");
        when(mMockDelegate.createPanel("panel1", PanelType.TASK)).thenReturn(mockPanel1);
        mPanelPool.getOrCreatePanel("panel1", PanelType.TASK);

        Panel mockPanel2 = mock(Panel.class);
        when(mockPanel2.getPanelId()).thenReturn("panel2");
        when(mMockDelegate.createPanel("panel2", PanelType.DECOR)).thenReturn(mockPanel2);
        mPanelPool.getOrCreatePanel("panel2", PanelType.DECOR);

        Panel foundPanel = mPanelPool.getPanel(panel -> panel.getPanelId().equals("panel2"));

        assertThat(foundPanel).isEqualTo(mockPanel2);
    }

    @Test
    public void getPanel_byPredicate_returnsNull_whenNoMatch() {
        Panel mockPanel1 = mock(Panel.class);
        when(mockPanel1.getPanelId()).thenReturn("panel1");
        when(mMockDelegate.createPanel("panel1", PanelType.TASK)).thenReturn(mockPanel1);
        mPanelPool.getOrCreatePanel("panel1", PanelType.TASK);

        Panel foundPanel = mPanelPool.getPanel(panel -> panel.getPanelId().equals("no_match"));

        assertThat(foundPanel).isNull();
    }

    @Test
    public void clearPanels_removesAllPanelsAndCallsDestroy() {
        Panel mockPanel1 = mock(Panel.class);
        when(mMockDelegate.createPanel("panel1", PanelType.TASK)).thenReturn(mockPanel1);
        mPanelPool.getOrCreatePanel("panel1", PanelType.TASK);

        Panel mockPanel2 = mock(Panel.class);
        when(mMockDelegate.createPanel("panel2", PanelType.DECOR)).thenReturn(mockPanel2);
        mPanelPool.getOrCreatePanel("panel2", PanelType.DECOR);

        mPanelPool.clearPanels();

        assertThat(mPanelPool.getPanel("panel1")).isNull();
        assertThat(mPanelPool.getPanel("panel2")).isNull();
        verify(mockPanel1).destroy();
        verify(mockPanel2).destroy();
    }

    @Test
    public void removePanel_removesSpecificPanelAndCallsDestroy() {
        String idToRemove = "panel_to_remove";
        String idToKeep = "panel_to_keep";
        Panel mockPanelToRemove = mock(Panel.class);
        Panel mockPanelToKeep = mock(Panel.class);
        when(mMockDelegate.createPanel(idToRemove, PanelType.TASK)).thenReturn(mockPanelToRemove);
        when(mMockDelegate.createPanel(idToKeep, PanelType.TASK)).thenReturn(mockPanelToKeep);
        mPanelPool.getOrCreatePanel(idToRemove, PanelType.TASK);
        mPanelPool.getOrCreatePanel(idToKeep, PanelType.TASK);

        mPanelPool.executeOnPanel(idToRemove, Panel::destroy);

        assertThat(mPanelPool.getPanel(idToKeep)).isEqualTo(mockPanelToKeep);
        verify(mockPanelToRemove).destroy();
        verify(mockPanelToKeep, never()).destroy();
    }

    @Test
    public void forEach_executesConsumerOnAllPanels() {
        Panel mockPanel1 = mock(Panel.class);
        when(mMockDelegate.createPanel("panel1", PanelType.TASK)).thenReturn(mockPanel1);
        mPanelPool.getOrCreatePanel("panel1", PanelType.TASK);

        Panel mockPanel2 = mock(Panel.class);
        when(mMockDelegate.createPanel("panel2", PanelType.DECOR)).thenReturn(mockPanel2);
        mPanelPool.getOrCreatePanel("panel2", PanelType.DECOR);

        AtomicInteger count = new AtomicInteger(0);

        mPanelPool.forEach(
                panel -> {
                    count.incrementAndGet();
                    panel.getPanelId();
                });

        assertThat(count.get()).isEqualTo(2);
        verify(mockPanel1).getPanelId();
        verify(mockPanel2).getPanelId();
    }
}
