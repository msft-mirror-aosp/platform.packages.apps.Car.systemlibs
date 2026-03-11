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

import static org.mockito.Mockito.mock;

import android.content.Context;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.parser.BaseValueParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BaseValueParserTest {

    private BaseValueParser mParser;
    private Context mContext;

    @Before
    public void setUp() {
        mParser = new BaseValueParser();
        mContext = mock(Context.class);
    }

    @Test
    public void testParseBoolean_true() {
        assertThat(mParser.parseBoolean(mContext, "true")).isTrue();
        assertThat(mParser.parseBoolean(mContext, "TRUE")).isTrue();
    }

    @Test
    public void testParseBoolean_false() {
        assertThat(mParser.parseBoolean(mContext, "false")).isFalse();
        assertThat(mParser.parseBoolean(mContext, "FALSE")).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBoolean_invalid() {
        mParser.parseBoolean(mContext, "not_boolean");
    }

    @Test
    public void testParseColor_hex() {
        assertThat(mParser.parseColor(mContext, "#FF0000")).isEqualTo(Color.RED);
        assertThat(mParser.parseColor(mContext, "#00FF00")).isEqualTo(Color.GREEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseColor_resource_throws_exception() {
        mParser.parseColor(mContext, "@color/red");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseColor_invalid() {
        mParser.parseColor(mContext, "not_a_color");
    }

    @Test
    public void testParseString() {
        assertThat(mParser.parseString(mContext, "hello")).isEqualTo("hello");
    }

    @Test
    public void testParseInteger_decimal() {
        assertThat(mParser.parseInteger(mContext, "123", 0)).isEqualTo(123);
        assertThat(mParser.parseInteger(mContext, "-456", 0)).isEqualTo(-456);
    }

    @Test
    public void testParseInteger_hex() {
        assertThat(mParser.parseInteger(mContext, "0x1A", 0)).isEqualTo(26);
        assertThat(mParser.parseInteger(mContext, "0X1A", 0)).isEqualTo(26);
        assertThat(mParser.parseInteger(mContext, "#1A", 0)).isEqualTo(26);
    }

    @Test
    public void testParseInteger_octal() {
        assertThat(mParser.parseInteger(mContext, "012", 0)).isEqualTo(10);
    }

    @Test
    public void testParseInteger_whitespace() {
        assertThat(mParser.parseInteger(mContext, "  123  ", 0)).isEqualTo(123);
        assertThat(mParser.parseInteger(mContext, "  0x1A  ", 0)).isEqualTo(26);
    }

    @Test
    public void testParseInteger_invalid_returnsDefault() {
        assertThat(mParser.parseInteger(mContext, "not_an_int", 999)).isEqualTo(999);
        assertThat(mParser.parseInteger(mContext, "12.34", 999)).isEqualTo(999);
    }

    @Test
    public void testParseInteger_nullOrEmpty_returnsDefault() {
        assertThat(mParser.parseInteger(mContext, null, 999)).isEqualTo(999);
        assertThat(mParser.parseInteger(mContext, "", 999)).isEqualTo(999);
    }
}
