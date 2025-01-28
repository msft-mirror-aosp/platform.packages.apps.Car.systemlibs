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

import android.graphics.Rect;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KeyFrameVariantTest {
    @Test
    public void testGetBounds() {
        KeyFrameVariant variant = createKeyFrameVariant();
        variant.setFraction(0.5f); // Interpolate halfway

        Rect bounds = variant.getBounds();

        // Expected bounds are the average of the two keyframe bounds
        assertThat(bounds).isEqualTo(new Rect(5, 10, 15, 20));
    }

    @Test
    public void testGetVisibility() {
        KeyFrameVariant variant = createKeyFrameVariant();

        variant.setFraction(0.25f); // Before the visible keyframe
        assertThat(variant.isVisible()).isFalse();

        variant.setFraction(0.75f); // After the visible keyframe
        assertThat(variant.isVisible()).isTrue();
    }

    @Test
    public void testGetAlpha() {
        KeyFrameVariant variant = createKeyFrameVariant();
        variant.setFraction(0.5f); // Interpolate halfway
        assertThat(variant.getAlpha()).isEqualTo(0.5f); // Expected alpha is the average
    }

    private KeyFrameVariant createKeyFrameVariant() {
        final String keyFrameVariantId = "keyFrameVariantId";
        final String variantId1 = "variantId1";
        final String variantId2 = "variantId2";

        KeyFrameVariant variant = new KeyFrameVariant(keyFrameVariantId, null);
        Variant variant1 = new Variant(variantId1, null);
        variant1.setBounds(new Rect(0, 0, 10, 10));
        variant1.setVisibility(false);
        variant1.setAlpha(0.0f);

        Variant variant2 = new Variant(variantId2, null);
        variant2.setBounds(new Rect(10, 20, 20, 30));
        variant2.setVisibility(true);
        variant2.setAlpha(1.0f);

        variant.addKeyFrame(new KeyFrameVariant.KeyFrame(25, variant1));
        variant.addKeyFrame(new KeyFrameVariant.KeyFrame(75, variant2));
        return variant;
    }
}
