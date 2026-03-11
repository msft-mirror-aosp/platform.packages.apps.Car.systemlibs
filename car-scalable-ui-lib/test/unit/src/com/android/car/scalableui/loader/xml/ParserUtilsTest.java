/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.car.scalableui.loader.xml;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.parser.ParserUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ParserUtilsTest {

    private Context mContext;
    private Resources mResources;
    private AttributeSet mAttrs;
    private DisplayMetrics mMetrics;

    @Before
    public void setUp() {
        mContext = mock(Context.class);
        mResources = mock(Resources.class);
        mAttrs = mock(AttributeSet.class);
        mMetrics = new DisplayMetrics();
        mMetrics.widthPixels = 1000;
        mMetrics.heightPixels = 2000;
        mMetrics.density = 2.0f;

        when(mContext.getResources()).thenReturn(mResources);
        when(mResources.getDisplayMetrics()).thenReturn(mMetrics);
        // Fallback for ParserUtils using default display
        DisplayManager displayManager = mock(DisplayManager.class);
        when(mContext.getSystemService(DisplayManager.class)).thenReturn(displayManager);
        when(displayManager.getDisplay(anyInt())).thenReturn(null);
    }

    @Test
    public void testGetDimensionPixelSize_literalPx() {
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("100px");
        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(100);
    }

    @Test
    public void testGetDimensionPixelSize_literalDp() {
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("50dp");
        // 50dp * 2.0 density = 100px
        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(100);
    }

    @Test
    public void testGetDimensionPixelSize_percentageWidth() {
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("50%");
        // 1000 * 0.5 = 500
        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(500);
    }

    @Test
    public void testGetDimensionPixelSize_percentageHeight() {
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("10%");
        // 2000 * 0.1 = 200
        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, false))
                .isEqualTo(200);
    }

    @Test
    public void testGetDimensionPixelSize_dimenResource() {
        int resId = 123;
        String resName = "dimen_res";
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("@" + resName);
        when(mAttrs.getAttributeResourceValue(null, "attr", 0)).thenReturn(resId);

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_DIMENSION;
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        when(mResources.getDimensionPixelSize(resId)).thenReturn(50);

        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(50);
    }

    @Test
    public void testGetDimensionPixelSize_integerResource() {
        int resId = 456;
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("@integer/val");
        when(mAttrs.getAttributeResourceValue(null, "attr", 0)).thenReturn(resId);
        when(mResources.getDimensionPixelSize(resId)).thenThrow(new Resources.NotFoundException());

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_INT_DEC;
                            tv.data = 100;
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(100);
    }

    @Test
    public void testGetDimensionPixelSize_stringResourceWithDp() {
        int resId = 789;
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("@string/val");
        when(mAttrs.getAttributeResourceValue(null, "attr", 0)).thenReturn(resId);

        when(mResources.getDimensionPixelSize(resId)).thenThrow(new Resources.NotFoundException());

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_STRING;
                            tv.string = "50dp";
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        // 50dp * 2.0 = 100px
        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(100);
    }

    @Test
    public void testGetDimensionPixelSize_stringResourceWithPercent() {
        int resId = 999;
        when(mAttrs.getAttributeValue(null, "attr")).thenReturn("@string/percent");
        when(mAttrs.getAttributeResourceValue(null, "attr", 0)).thenReturn(resId);

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_STRING;
                            tv.string = "50%"; // 500px (1000 * 0.5)
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        assertThat(ParserUtils.getDimensionPixelSize(mContext, mAttrs, "attr", 0, true))
                .isEqualTo(500);
    }
}
