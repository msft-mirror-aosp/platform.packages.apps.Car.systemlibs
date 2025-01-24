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

import static org.mockito.Mockito.mock;

import android.animation.Animator;

import com.android.car.scalableui.model.Transition;

import org.junit.Test;

import java.util.Map;

public class PanelTransactionTest {

    private static final String TEST_PANEL_ID = "TEST_PANEL_ID";
    private static final String TEST_PANEL_ID_2 = "TEST_PANEL_ID_2";

    @Test
    public void testSetPanelTransaction() {
        PanelTransaction transaction = new PanelTransaction();
        Transition mockTransition = mock(Transition.class);
        transaction.setPanelTransaction(TEST_PANEL_ID, mockTransition);

        // Check if the transaction is added correctly
        assertThat(transaction.getPanelTransactionStates()).hasSize(1);
        Map.Entry<String, Transition> entry =
                transaction.getPanelTransactionStates().iterator().next();
        assertThat(entry.getKey()).isEqualTo(TEST_PANEL_ID);
        assertThat(entry.getValue()).isEqualTo(mockTransition);
    }

    @Test
    public void testGetPanelTransactionStates() {
        PanelTransaction transaction = new PanelTransaction();
        transaction.setPanelTransaction(TEST_PANEL_ID, mock(Transition.class));
        transaction.setPanelTransaction(TEST_PANEL_ID_2, mock(Transition.class));

        // Check if the correct number of transactions are returned
        assertThat(transaction.getPanelTransactionStates()).hasSize(/* expectedSize= */ 2);
    }

    @Test
    public void testSetAnimator() {
        PanelTransaction transaction = new PanelTransaction();
        Animator mockAnimator = mock(Animator.class);
        transaction.setAnimator(TEST_PANEL_ID, mockAnimator);

        // Check if the animator is added correctly
        assertThat(transaction.getAnimators()).hasSize(/* expectedSize= */ 1);
        Map.Entry<String, Animator> entry = transaction.getAnimators().iterator().next();
        assertThat(entry.getKey()).isEqualTo(TEST_PANEL_ID);
        assertThat(entry.getValue()).isEqualTo(mockAnimator);
    }

    @Test
    public void testGetAnimators() {
        PanelTransaction transaction = new PanelTransaction();
        transaction.setAnimator(TEST_PANEL_ID, mock(Animator.class));
        transaction.setAnimator(TEST_PANEL_ID_2, mock(Animator.class));

        // Check if the correct number of animators are returned
        assertThat(transaction.getAnimators()).hasSize(/* expectedSize= */ 2);
    }
}
