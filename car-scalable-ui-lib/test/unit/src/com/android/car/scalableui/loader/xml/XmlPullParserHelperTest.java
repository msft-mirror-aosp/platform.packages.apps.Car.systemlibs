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

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(AndroidJUnit4.class)
public class XmlPullParserHelperTest {

    private XmlPullParser mParser;

    @Before
    public void setUp() {
        mParser = mock(XmlPullParser.class);
    }

    @Test
    public void throwIfUnknownTag_notStartTag_throwsIllegalStateException() throws Exception {
        when(mParser.getEventType()).thenReturn(XmlPullParser.END_TAG);

        assertThrows(IllegalStateException.class,
                () -> XmlPullParserHelper.throwIfUnknownTag(mParser));
    }

    @Test
    public void throwIfUnknownTag_textEvent_throwsIllegalStateException() throws Exception {
        when(mParser.getEventType()).thenReturn(XmlPullParser.TEXT);

        assertThrows(IllegalStateException.class,
                () -> XmlPullParserHelper.throwIfUnknownTag(mParser));
    }

    @Test
    public void throwIfUnknownTag_startTag_throwsXmlPullParserException() throws Exception {
        when(mParser.getEventType()).thenReturn(XmlPullParser.START_TAG);
        when(mParser.getName()).thenReturn("unknownTag");

        XmlPullParserException e = assertThrows(XmlPullParserException.class,
                () -> XmlPullParserHelper.throwIfUnknownTag(mParser));

        assertThat(e).hasMessageThat().contains("Unknown or unhandled XML tag: <unknownTag>");
    }

    @Test
    public void readText_hasText_returnsTextAndAdvances() throws Exception {
        when(mParser.next()).thenReturn(XmlPullParser.TEXT);
        when(mParser.getText()).thenReturn("Sample Text");

        String result = XmlPullParserHelper.readText(mParser);

        assertThat(result).isEqualTo("Sample Text");
        verify(mParser).nextTag();
    }

    @Test
    public void readText_nextThrowsIOException_throwsIOException() throws Exception {
        when(mParser.next()).thenThrow(new java.io.IOException("Test IO Exception"));

        assertThrows(java.io.IOException.class, () -> XmlPullParserHelper.readText(mParser));
    }

    @Test
    public void readText_nextTagThrowsXmlPullParserException_throwsXmlPullParserException()
            throws Exception {
        when(mParser.next()).thenReturn(XmlPullParser.TEXT);
        when(mParser.getText()).thenReturn("Some Text");
        when(mParser.nextTag()).thenThrow(new XmlPullParserException("Test XML Exception"));

        assertThrows(XmlPullParserException.class, () -> XmlPullParserHelper.readText(mParser));
    }

    @Test
    public void readText_noText_returnsEmptyString() throws Exception {
        when(mParser.next()).thenReturn(XmlPullParser.END_TAG);

        String result = XmlPullParserHelper.readText(mParser);

        assertThat(result).isEmpty();
    }
}
