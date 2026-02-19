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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.parser.ResourceValueParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResourceValueParserTest {

    private ResourceValueParser mParser;
    private Context mContext;
    private Resources mResources;

    @Before
    public void setUp() {
        mParser = new ResourceValueParser();
        mContext = mock(Context.class);
        mResources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(mResources);
        when(mContext.getPackageName()).thenReturn("com.example.package");

        DisplayMetrics metrics = new DisplayMetrics();
        metrics.density = 1.0f;
        metrics.scaledDensity = 1.0f;
        when(mResources.getDisplayMetrics()).thenReturn(metrics);
    }

    @Test
    public void testParseBoolean_literal() {
        // Direct boolean parsing (delegated to super)
        assertThat(mParser.parseBoolean(mContext, "true")).isTrue();
    }

    @Test
    public void testParseBoolean_resource() {
        int resId = 123;
        String resName = "bool_res";
        when(mResources.getIdentifier(eq(resName), eq("bool"), anyString())).thenReturn(0);
        when(mResources.getIdentifier(eq(resName), eq("bool"), anyString())).thenReturn(0);
        when(mResources.getIdentifier(resName, "bool", "com.example.package")).thenReturn(resId);
        when(mResources.getBoolean(resId)).thenReturn(true);

        assertThat(mParser.parseBoolean(mContext, "@" + resName)).isTrue();
    }

    @Test
    public void testParseColor_literal() {
        assertThat(mParser.parseColor(mContext, "#FF0000")).isEqualTo(Color.RED);
    }

    @Test
    public void testParseColor_resource() {
        int resId = 456;
        String resName = "color_res";
        when(mResources.getIdentifier(resName, "color", "com.example.package")).thenReturn(resId);
        when(mResources.getColor(eq(resId), eq(null))).thenReturn(Color.BLUE);
        when(mContext.getTheme()).thenReturn(null);

        assertThat(mParser.parseColor(mContext, "@" + resName)).isEqualTo(Color.BLUE);
    }

    @Test
    public void testParseString_literal() {
        assertThat(mParser.parseString(mContext, "hello")).isEqualTo("hello");
    }

    @Test
    public void testParseString_resource() {
        int resId = 789;
        String resName = "string_res";
        when(mResources.getIdentifier(resName, "string", "com.example.package")).thenReturn(resId);
        when(mResources.getResourceTypeName(resId)).thenReturn("string");
        when(mContext.getString(resId)).thenReturn("resolved string");

        assertThat(mParser.parseString(mContext, "@" + resName)).isEqualTo("resolved string");
    }

    @Test
    public void testParseString_idResource_returnsLiteral() {
        int resId = 111;
        when(mResources.getIdentifier("not_a_string", "string", "com.example.package"))
                .thenReturn(resId);
        when(mResources.getResourceTypeName(resId)).thenReturn("id");

        assertThat(mParser.parseString(mContext, "@not_a_string")).isEqualTo("@not_a_string");
    }

    @Test
    public void testParseDimensionPixelSize_integerResource() {
        int resId = 321;
        String resName = "integer_res";
        when(mResources.getIdentifier(resName, null, "com.example.package")).thenReturn(resId);
        when(mResources.getIdentifier(resName, "dimen", "com.example.package")).thenReturn(0);

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_INT_DEC;
                            tv.data = 100;
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        when(mResources.getInteger(resId)).thenReturn(100);

        assertThat(mParser.parseDimensionPixelSize(mContext, "@" + resName, 0)).isEqualTo(100);
    }

    @Test
    public void testParseDimensionPixelSize_stringResource() {
        int resId = 654;
        String resName = "string_res";
        when(mResources.getIdentifier(resName, null, "com.example.package")).thenReturn(resId);
        when(mResources.getIdentifier(resName, "dimen", "com.example.package")).thenReturn(0);

        doAnswer(
                        invocation -> {
                            TypedValue tv = invocation.getArgument(1);
                            tv.type = TypedValue.TYPE_STRING;
                            tv.string = "50px";
                            return null;
                        })
                .when(mResources)
                .getValue(eq(resId), any(TypedValue.class), eq(true));

        when(mResources.getString(resId)).thenReturn("50px");

        assertThat(mParser.parseDimensionPixelSize(mContext, "@" + resName, 0)).isEqualTo(50);
    }

    @Test
    public void testParseInterpolator_explicitType_anim() {
        int resId = 135;
        String resValue = "@android:anim/decelerate_interpolator";
        String resName = "android:anim/decelerate_interpolator";

        when(mResources.getIdentifier(resName, "interpolator", "com.example.package"))
                .thenReturn(resId);

        XmlResourceParser systemParser = ApplicationProvider.getApplicationContext()
                .getResources().getAnimation(android.R.anim.linear_interpolator);
        when(mResources.getAnimation(resId)).thenReturn(systemParser);

        assertThat(mParser.parseInterpolator(mContext, resValue)).isNotNull();
    }

    @Test
    public void testParseInterpolator_implicitType_failsWithInterpolator() {
        String resValue = "@android:decelerate_interpolator";
        String resName = "android:decelerate_interpolator";

        when(mResources.getIdentifier(resName, "interpolator", "com.example.package"))
                .thenReturn(0);
        when(mResources.getIdentifier(resName, null, "com.example.package")).thenReturn(0);

        assertThat(mParser.parseInterpolator(mContext, resValue)).isNull();
    }

    @Test
    public void testParseBoolean_privateResource() {
        int resId = 321;
        String resName = "android:bool/config_hideNavBarForKeyboard";
        when(mResources.getIdentifier(resName, "bool", "com.example.package")).thenReturn(resId);
        when(mResources.getBoolean(resId)).thenReturn(true);

        assertThat(mParser.parseBoolean(mContext, "@*" + resName)).isTrue();
    }
}
