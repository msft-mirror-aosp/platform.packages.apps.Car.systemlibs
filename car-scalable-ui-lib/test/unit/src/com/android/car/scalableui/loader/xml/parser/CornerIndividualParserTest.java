/*
 * Copyright (C) 2026 The Android Open Source Project.
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
package com.android.car.scalableui.loader.xml.parser;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.view.Display;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.model.Corner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

@RunWith(AndroidJUnit4.class)
public class CornerIndividualParserTest {

    @Mock private ParserEnv mContext;
    @Mock private ValueParser mValueParser;
    @Mock private Context mAndroidContext;
    @Mock private Resources mResources;

    private CornerIndividualParser mParser;
    private DisplayMetrics mMetrics;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mParser = new CornerIndividualParser();
        mMetrics = new DisplayMetrics();
        mMetrics.density = 1.0f;

        when(mContext.getValueParser()).thenReturn(mValueParser);
        when(mContext.getContext()).thenReturn(mAndroidContext);
        when(mAndroidContext.getResources()).thenReturn(mResources);
        when(mResources.getDisplayMetrics()).thenReturn(mMetrics);

        DisplayManager displayManager = mock(DisplayManager.class);
        Display display = mock(Display.class);
        when(mAndroidContext.getSystemService(DisplayManager.class)).thenReturn(displayManager);
        when(displayManager.getDisplay(anyInt())).thenReturn(display);
        when(displayManager.getDisplay(anyInt())).thenReturn(null);
    }

    @Test
    public void testParseIndividualCorners() throws Exception {
        String xml = "<Corner radius=\"10\" topLeftRadius=\"5\" bottomRightRadius=\"15\" />";
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(new StringReader(xml));
        parser.nextTag();

        Corner corner = mParser.parseTag(mContext, parser);

        assertThat(corner.getTopLeftRadius()).isEqualTo(5);
        assertThat(corner.getTopRightRadius()).isEqualTo(10); // Inherited from radius
        assertThat(corner.getBottomLeftRadius()).isEqualTo(10); // Inherited from radius
        assertThat(corner.getBottomRightRadius()).isEqualTo(15);
    }
}
