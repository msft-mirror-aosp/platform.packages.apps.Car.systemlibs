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
public class BoundsTest {

    @Test
    public void testBoundsCreation() {
        Bounds bounds = new Bounds(10, 20, 30, 40);
        Rect rect = bounds.getRect();
        assertThat(rect.left).isEqualTo(10);
        assertThat(rect.top).isEqualTo(20);
        assertThat(rect.right).isEqualTo(30);
        assertThat(rect.bottom).isEqualTo(40);
    }
}
