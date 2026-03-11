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
    private static final int TEST_LEFT = 10;
    private static final int TEST_TOP = 20;
    private static final int TEST_RIGHT = 30;
    private static final int TEST_BOTTOM = 40;
    private static final int TEST_WIDTH = 20;
    private static final int TEST_HEIGHT = 20;
    private static final int TEST_LEFT_OFFSET = 1;
    private static final int TEST_TOP_OFFSET = 2;
    private static final int TEST_RIGHT_OFFSET = 3;
    private static final int TEST_BOTTOM_OFFSET = 4;

    @Test
    public void testBoundsCreation_rect() {
        Bounds bounds =
                new Bounds.Builder()
                        .setLeft(TEST_LEFT)
                        .setTop(TEST_TOP)
                        .setRight(TEST_RIGHT)
                        .setBottom(TEST_BOTTOM)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT);
        assertThat(rect.top).isEqualTo(TEST_TOP);
        assertThat(rect.right).isEqualTo(TEST_RIGHT);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM);
    }

    @Test
    public void testBoundsCreation_width_withLeft() {
        Bounds bounds =
                new Bounds.Builder()
                        .setLeft(TEST_LEFT)
                        .setTop(TEST_TOP)
                        .setBottom(TEST_BOTTOM)
                        .setWidth(TEST_WIDTH)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT);
        assertThat(rect.top).isEqualTo(TEST_TOP);
        assertThat(rect.right).isEqualTo(TEST_RIGHT);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM);
    }

    @Test
    public void testBoundsCreation_width_withRight() {
        Bounds bounds =
                new Bounds.Builder()
                        .setRight(TEST_RIGHT)
                        .setTop(TEST_TOP)
                        .setBottom(TEST_BOTTOM)
                        .setWidth(TEST_WIDTH)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT);
        assertThat(rect.top).isEqualTo(TEST_TOP);
        assertThat(rect.right).isEqualTo(TEST_RIGHT);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM);
    }

    @Test
    public void testBoundsCreation_height_withTop() {
        Bounds bounds =
                new Bounds.Builder()
                        .setLeft(TEST_LEFT)
                        .setTop(TEST_TOP)
                        .setRight(TEST_RIGHT)
                        .setHeight(TEST_HEIGHT)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT);
        assertThat(rect.top).isEqualTo(TEST_TOP);
        assertThat(rect.right).isEqualTo(TEST_RIGHT);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM);
    }

    @Test
    public void testBoundsCreation_height_withBottom() {
        Bounds bounds =
                new Bounds.Builder()
                        .setLeft(TEST_LEFT)
                        .setRight(TEST_RIGHT)
                        .setBottom(TEST_BOTTOM)
                        .setHeight(TEST_HEIGHT)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT);
        assertThat(rect.top).isEqualTo(TEST_TOP);
        assertThat(rect.right).isEqualTo(TEST_RIGHT);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM);
    }

    @Test
    public void testBoundsCreation_withOffset() {
        Bounds bounds =
                new Bounds.Builder()
                        .setLeft(TEST_LEFT)
                        .setTop(TEST_TOP)
                        .setRight(TEST_RIGHT)
                        .setBottom(TEST_BOTTOM)
                        .setLeftOffset(TEST_LEFT_OFFSET)
                        .setTopOffset(TEST_TOP_OFFSET)
                        .setRightOffset(TEST_RIGHT_OFFSET)
                        .setBottomOffset(TEST_BOTTOM_OFFSET)
                        .build();

        Rect rect = bounds.getRect();

        assertThat(rect.left).isEqualTo(TEST_LEFT + TEST_LEFT_OFFSET);
        assertThat(rect.top).isEqualTo(TEST_TOP + TEST_TOP_OFFSET);
        assertThat(rect.right).isEqualTo(TEST_RIGHT - TEST_RIGHT_OFFSET);
        assertThat(rect.bottom).isEqualTo(TEST_BOTTOM - TEST_BOTTOM_OFFSET);
    }
}
