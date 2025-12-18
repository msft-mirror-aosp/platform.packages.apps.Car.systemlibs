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
public class CornerTest {

    @Test
    public void testCornerCreation() {
        final int expectedRadiusTL = 1;
        final int expectedRadiusTR = 2;
        final int expectedRadiusBL = 3;
        final int expectedRadiusBR = 4;
        Corner corner = new Corner(expectedRadiusTL, expectedRadiusTR, expectedRadiusBL,
                expectedRadiusBR);
        assertThat(corner.getTopLeftRadius()).isEqualTo(expectedRadiusTL);
        assertThat(corner.getTopRightRadius()).isEqualTo(expectedRadiusTR);
        assertThat(corner.getBottomLeftRadius()).isEqualTo(expectedRadiusBL);
        assertThat(corner.getBottomRightRadius()).isEqualTo(expectedRadiusBR);
    }
}
