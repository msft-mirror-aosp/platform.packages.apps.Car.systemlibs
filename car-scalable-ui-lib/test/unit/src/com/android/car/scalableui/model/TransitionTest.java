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
package com.android.car.scalableui.model;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.panel.Panel;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TransitionTest {

    private static final String TO_VARIANT_ID = "TO_VARIANT_ID";
    private static final String FROM_VARIANT_ID = "FROM_VARIANT_ID";
    private static final String TEST_EVENT = "TEST_EVENT";

    @Test
    public void testTransitionCreation() {
        Variant fromVariant = new Variant(FROM_VARIANT_ID, null);
        Variant toVariant = new Variant(TO_VARIANT_ID, null);
        Transition transition = new Transition(fromVariant, toVariant, TEST_EVENT, null, 500,
                new AccelerateDecelerateInterpolator());

        assertThat(transition.getFromVariant()).isEqualTo(fromVariant);
        assertThat(transition.getToVariant()).isEqualTo(toVariant);
        assertThat(transition.getOnEvent()).isEqualTo(TEST_EVENT);
    }

    @Test
    public void testGetAnimator_defaultAnimator() {
        Panel panel = mock(Panel.class);
        Variant fromVariant = new Variant(FROM_VARIANT_ID, null);
        Variant toVariant = new Variant(TO_VARIANT_ID, null);
        Transition transition = new Transition(fromVariant, toVariant, TEST_EVENT, null, 500,
                new AccelerateDecelerateInterpolator());

        Animator animator = transition.getAnimator(panel, fromVariant);

        assertThat(animator).isInstanceOf(ValueAnimator.class);
    }

    @Test
    public void testGetAnimator_sameFromAndToVariant() {
        Panel panel = mock(Panel.class);
        Variant variant = new Variant(FROM_VARIANT_ID, null);
        Transition transition = new Transition(variant, variant, TEST_EVENT, null, 500,
                new AccelerateDecelerateInterpolator());

        Animator animator = transition.getAnimator(panel, variant);

        assertThat(animator).isNull();
    }
}
