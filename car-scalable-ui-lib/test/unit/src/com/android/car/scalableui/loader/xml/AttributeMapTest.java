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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.parser.AttributeMap;
import com.android.car.scalableui.loader.xml.parser.ValueParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;

@RunWith(AndroidJUnit4.class)
public class AttributeMapTest {

    private ParserEnv mContext;
    private ValueParser mValueParser;
    private XmlPullParser mXmlPullParser;
    private Context mAndroidContext;

    @Before
    public void setUp() {
        mContext = mock(ParserEnv.class);
        mValueParser = mock(ValueParser.class);
        mXmlPullParser = mock(XmlPullParser.class);
        mAndroidContext = mock(Context.class);

        when(mContext.getValueParser()).thenReturn(mValueParser);
        when(mContext.getContext()).thenReturn(mAndroidContext);
    }

    @Test
    public void testParseString_success() throws Exception {
        String attrName = "testAttr";
        String attrValue = "testValue";
        String parsedValue = "parsedValue";

        when(mXmlPullParser.getAttributeCount()).thenReturn(1);
        when(mXmlPullParser.getAttributeName(0)).thenReturn(attrName);
        when(mXmlPullParser.getAttributeValue(0)).thenReturn(attrValue);
        when(mValueParser.parseString(any(), eq(attrValue))).thenReturn(parsedValue);

        TestTarget target = new TestTarget();
        AttributeMap<TestTarget> map =
                AttributeMap.<TestTarget>builder()
                        .addString(attrName, TestTarget::setStringField)
                        .build();

        map.parse(mContext, mXmlPullParser, target);

        assertThat(target.mStringField).isEqualTo(parsedValue);
    }

    @Test
    public void testParseInteger_success() throws Exception {
        String attrName = "intAttr";
        String attrValue = "123";
        int parsedValue = 123;

        when(mXmlPullParser.getAttributeCount()).thenReturn(1);
        when(mXmlPullParser.getAttributeName(0)).thenReturn(attrName);
        when(mXmlPullParser.getAttributeValue(0)).thenReturn(attrValue);
        when(mValueParser.parseInteger(any(), eq(attrValue), anyInt())).thenReturn(parsedValue);

        TestTarget target = new TestTarget();
        AttributeMap<TestTarget> map =
                AttributeMap.<TestTarget>builder()
                        .addInteger(attrName, TestTarget::setIntField)
                        .build();

        map.parse(mContext, mXmlPullParser, target);

        assertThat(target.mIntField).isEqualTo(parsedValue);
    }

    @Test
    public void testParse_ignoresUnknownAttributes() throws Exception {
        when(mXmlPullParser.getAttributeCount()).thenReturn(1);
        when(mXmlPullParser.getAttributeName(0)).thenReturn("unknown");
        when(mXmlPullParser.getAttributeValue(0)).thenReturn("value");

        TestTarget target = new TestTarget();
        AttributeMap<TestTarget> map =
                AttributeMap.<TestTarget>builder()
                        .addString("known", TestTarget::setStringField)
                        .build();

        map.parse(mContext, mXmlPullParser, target);

        assertThat(target.mStringField).isNull();
    }

    @Test
    public void testParse_multipleAttributes() throws Exception {
        when(mXmlPullParser.getAttributeCount()).thenReturn(2);
        when(mXmlPullParser.getAttributeName(0)).thenReturn("attr1");
        when(mXmlPullParser.getAttributeValue(0)).thenReturn("val1");
        when(mXmlPullParser.getAttributeName(1)).thenReturn("attr2");
        when(mXmlPullParser.getAttributeValue(1)).thenReturn("val2");

        when(mValueParser.parseString(any(), eq("val1"))).thenReturn("parsed1");
        when(mValueParser.parseString(any(), eq("val2"))).thenReturn("parsed2");

        TestTarget target = new TestTarget();
        AttributeMap<TestTarget> map =
                AttributeMap.<TestTarget>builder()
                        .addString("attr1", (t, v) -> t.mStringField = v)
                        .addString("attr2", (t, v) -> t.mOtherStringField = v)
                        .build();

        map.parse(mContext, mXmlPullParser, target);

        assertThat(target.mStringField).isEqualTo("parsed1");
        assertThat(target.mOtherStringField).isEqualTo("parsed2");
    }

    private static class TestTarget {
        String mStringField;
        String mOtherStringField;
        int mIntField;

        void setStringField(String val) {
            mStringField = val;
        }

        void setIntField(int val) {
            mIntField = val;
        }
    }
}
