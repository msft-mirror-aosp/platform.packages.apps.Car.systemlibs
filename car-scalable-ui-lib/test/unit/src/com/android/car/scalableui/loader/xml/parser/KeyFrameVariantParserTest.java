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

package com.android.car.scalableui.loader.xml.parser;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Rect;
import android.util.Xml;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlParserRegistry;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;
import com.android.car.scalableui.model.Variant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RunWith(AndroidJUnit4.class)
public class KeyFrameVariantParserTest {
    private Context mContext;
    private ParserEnv mParserEnv;
    private KeyFrameVariantParser mParser;
    private PanelState mPanelState;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mParserEnv = new ParserEnv(mContext, new BaseValueParser(), new XmlParserRegistry());
        mParser = new KeyFrameVariantParser();
        mPanelState = new PanelState("testPanel", PanelType.SYSTEM_BAR);
    }

    @Test
    public void testParseKeyFrameVariant() throws XmlPullParserException, IOException {
        String xml =
                "<KeyFrameVariant id=\"kfv1\" parent=\"baseVariant\" alpha=\"0.5\">"
                        + "<KeyFrame frame=\"0\" variant=\"v1\" />"
                        + "<KeyFrame frame=\"100\" variant=\"v2\" />"
                        + "</KeyFrameVariant>";

        // Pre-populate variants referenced by KeyFrames and parent
        Variant baseVariant = new Variant.Builder("baseVariant", "baseVariant").build();
        Variant v1 = new Variant.Builder("v1", "v1").build();
        Variant v2 = new Variant.Builder("v2", "v2").build();
        mPanelState.addVariant(baseVariant);
        mPanelState.addVariant(v1);
        mPanelState.addVariant(v2);

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        parser.nextTag(); // Advance to KeyFrameVariant tag

        mParser.parse(mParserEnv, parser, mPanelState);

        KeyFrameVariant kfv = (KeyFrameVariant) mPanelState.getVariant("kfv1");
        assertThat(kfv).isNotNull();
        // KeyFrameVariant interpolates between keyframes. At fraction 0 (default), it uses v1's
        // alpha (1.0).
        // The 'alpha' attribute on KeyFrameVariant tag is ignored when keyframes are present.
        assertThat(kfv.getAlpha()).isEqualTo(1.0f);
        // Note: KeyFrameVariant inherits from baseVariant, but we can't easily check parent
        // directly without access to mParent field or behavior.
        // However, we can check if it interpolated correctly or if properties are set.

        // We can't access keyframes directly easily, but we can check behavior if we set fraction.
        kfv.setFraction(0f);
        assertThat(kfv.getLayer())
                .isEqualTo(v1.getLayer()); // Default layer should match v1 if set, or default.
        // v1 and v2 have default values.
    }

    @Test
    public void testDeferredParentResolution() throws XmlPullParserException, IOException {
        String xml =
                "<KeyFrameVariant id=\"kfv_deferred\" parent=\"futureVariant\">"
                        + "</KeyFrameVariant>";

        // Parent "futureVariant" does NOT exist yet.

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        parser.nextTag();

        mParser.parse(mParserEnv, parser, mPanelState);

        // NOW add the parent.
        Variant futureVariant =
                new Variant.Builder("futureVariant", "futureVariant").setAlpha(0.8f).build();
        mPanelState.addVariant(futureVariant);
        KeyFrameVariant kfv = (KeyFrameVariant) mPanelState.getVariant("kfv_deferred");
        assertThat(kfv).isNotNull();
    }

    @Test
    public void testAttributeMapParsing() throws XmlPullParserException, IOException {
        String xml = "<KeyFrameVariant id=\"test_attr\" alpha=\"0.123\" isVisible=\"false\" />";
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        parser.nextTag();

        mParser.parse(mParserEnv, parser, mPanelState);

        KeyFrameVariant kfv = (KeyFrameVariant) mPanelState.getVariant("test_attr");
        assertThat(kfv.getAlpha()).isWithin(0.001f).of(0.123f);
        assertThat(kfv.isVisible()).isFalse();
    }

    @Test
    public void testSafeBoundsFallback() {
        // Test that safeBounds defaults to bounds if not specified
        Rect boundary = new Rect(0, 0, 100, 100);
        KeyFrameVariant.Builder builder =
                new KeyFrameVariant.Builder("test_safe_bounds", "test_safe_bounds");
        builder.setBounds(boundary);
        KeyFrameVariant variant = builder.build();

        assertThat(variant.getSafeBounds()).isEqualTo(boundary);
    }

    @Test
    public void testParseKeyFrameVariant_withResourceId()
            throws XmlPullParserException, IOException {
        String xml =
                "<KeyFrameVariant id=\"@id/kfv1\" parent=\"@id/baseVariant\">"
                        + "<KeyFrame frame=\"100\" variant=\"@+id/v2\" />"
                        + "</KeyFrameVariant>";

        Variant baseVariant = new Variant.Builder("baseVariant", "baseVariant").build();
        Variant v2 = new Variant.Builder("v2", "v2").build();
        mPanelState.addVariant(baseVariant);
        mPanelState.addVariant(v2);

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        parser.nextTag(); // Advance to KeyFrameVariant tag

        mParser.parse(mParserEnv, parser, mPanelState);

        KeyFrameVariant kfv = (KeyFrameVariant) mPanelState.getVariant("kfv1");
        assertThat(kfv).isNotNull();
        // Just verify we could parse and find the keyframe
        kfv.setFraction(1.0f);
        // We know it didn't throw an exception resolving parent and the keyframe variant.
    }
}
