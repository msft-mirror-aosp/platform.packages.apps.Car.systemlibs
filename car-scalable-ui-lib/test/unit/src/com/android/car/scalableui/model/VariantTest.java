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
package com.android.car.scalableui.model;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;

import android.animation.Animator;
import android.graphics.Rect;
import android.view.animation.Interpolator;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.panel.Panel;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VariantTest {
    private static final String VARIANT_ID = "VARIANT_ID";

    @Test
    public void testVariantCreation_withBaseVariant() {
        Variant base = new Variant(VARIANT_ID, null);
        base.setBounds(new Rect(10, 20, 30, 40));
        base.setVisibility(false);
        base.setLayer(5);
        base.setAlpha(0.5f);

        Variant variant = new Variant(VARIANT_ID, base);

        assertThat(variant.getId()).isEqualTo(VARIANT_ID);
        assertThat(variant.getBounds()).isEqualTo(new Rect(10, 20, 30, 40));
        assertThat(variant.isVisible()).isFalse();
        assertThat(variant.getLayer()).isEqualTo(5);
        assertThat(variant.getAlpha()).isEqualTo(0.5f);
    }

    @Test
    public void testVariantCreation_withoutBaseVariant() {
        Variant variant = new Variant(VARIANT_ID, null);

        assertThat(variant.getId()).isEqualTo(VARIANT_ID);
        assertThat(variant.getBounds()).isEqualTo(new Rect()); // Default Rect
        assertThat(variant.isVisible()).isTrue(); // Default Visibility
        assertThat(variant.getLayer()).isEqualTo(0); // Default Layer
        assertThat(variant.getAlpha()).isEqualTo(1.0f); // Default Alpha
    }

    @Test
    public void testGetAnimator() {
        final String toVariantId = "toVariantId";
        final String fromVariantId = "fromVariantId";
        Panel panel = mock(Panel.class);
        Variant fromVariant = new Variant(fromVariantId, null);
        Variant toVariant = new Variant(toVariantId, null);
        Interpolator interpolator = mock(Interpolator.class);

        Animator animator = fromVariant.getAnimator(panel, toVariant, 1000, interpolator);

        assertThat(animator).isNotNull();
    }
}
