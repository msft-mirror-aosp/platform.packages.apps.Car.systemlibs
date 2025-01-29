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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VisibilityTest {

    @Test
    public void testVisibilityCreation_true() {
        Visibility visibility = new Visibility(true);
        assertThat(visibility.isVisible()).isTrue();
    }

    @Test
    public void testVisibilityCreation_false() {
        Visibility visibility = new Visibility(false);
        assertThat(visibility.isVisible()).isFalse();
    }

    @Test
    public void testVisibilityCopyConstructor() {
        Visibility original = new Visibility(true);
        Visibility copy = new Visibility(original);
        assertThat(copy.isVisible()).isTrue();
    }

    @Test
    public void testVisibilityConstants() {
        assertThat(Visibility.VISIBILITY_TAG).isEqualTo("Visibility");
        assertThat(Visibility.DEFAULT_VISIBILITY).isTrue();
    }
}
