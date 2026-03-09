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

import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.util.Xml;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.loader.xml.CoreParserModule;
import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlParserRegistry;
import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.panel.PanelPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RunWith(AndroidJUnit4.class)
public class TransitionParserTest {

    private ParserEnv mContext;
    private TransitionParser mParser;
    private PanelState mPanelState;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        XmlParserRegistry registry = new XmlParserRegistry();
        new CoreParserModule().registerParsers(registry);
        mContext = new ParserEnv(context, new ResourceValueParser(), registry);
        mParser = new TransitionParser();
        mPanelState = new PanelState("test_panel", PanelType.TASK);
        PanelPool.getInstance().clearPanels();
    }

    @Test
    public void parse_missingToVariant_throwsIllegalStateException() throws Exception {
        String xml =
                """
                <Transitions>
                    <Transition>
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            mParser.parse(mContext, parser, mPanelState);
                        });
        assertThat(exception).hasMessageThat().contains("toVariant attribute is required");
    }

    @Test
    public void parse_toVariantNotFound_throwsDetailedException() throws Exception {
        String xml =
                """
                <Transitions>
                    <Transition toVariant="@id/unknown_variant">
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        mParser.parse(mContext, parser, mPanelState);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            mPanelState.resolvePendingTransitions();
                        });
        assertThat(exception).hasMessageThat().contains("Failed to resolve ToVariant");
    }

    @Test
    public void parse_validToVariant_succeeds() throws Exception {
        // Add variant to panel state first
        Variant variant = new Variant.Builder("variant1", "variant1").build();
        mPanelState.addVariant(variant);

        String xml =
                """
                <Transitions>
                    <Transition toVariant="variant1" onEvent="test_event">
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        mParser.parse(mContext, parser, mPanelState);
        mPanelState.resolvePendingTransitions();
        assertThat(mPanelState.getTransition(new Event.Builder("test_event").build())).isNotNull();
    }

    @Test
    public void parse_forwardReference_succeeds() throws Exception {
        // Variant "variant1" is NOT added to mPanelState yet

        String xml =
                """
                <Transitions>
                    <Transition toVariant="variant1" onEvent="test_event">
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        mParser.parse(mContext, parser, mPanelState);

        // Add variant LATER
        Variant variant = new Variant.Builder("variant1", "variant1").build();
        mPanelState.addVariant(variant);

        // Resolve
        mPanelState.resolvePendingTransitions();

        assertThat(mPanelState.getTransition(new Event.Builder("test_event").build())).isNotNull();
    }

    @Test
    public void parse_idMismatch_throwsDetailedException() throws Exception {
        // Variant uses @+id/variant1
        Variant variant = new Variant.Builder("@+id/variant1", "variant1").build();
        mPanelState.addVariant(variant);

        // Transition uses @id/variant2 which doesn't exist
        String xml =
                """
                <Transitions>
                    <Transition toVariant="@id/variant2" onEvent="test_event">
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        mParser.parse(mContext, parser, mPanelState);

        // This expects to fail because variant2 does not exist, and verifies the detailed message
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            mPanelState.resolvePendingTransitions();
                        });
        assertThat(exception).hasMessageThat().contains("Failed to resolve ToVariant: variant2");
    }

    @Test
    public void parse_withNestedEventTags_succeeds() throws Exception {
        Variant variant = new Variant.Builder("variant1", "variant1").build();
        mPanelState.addVariant(variant);

        String xml =
                """
                <Transitions>
                    <Transition toVariant="variant1">
                        <Event id="test_event1" panel="panel1"/>
                        <Event id="test_event2"/>
                    </Transition>
                </Transitions>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <Transitions>

        mParser.parse(mContext, parser, mPanelState);
        mPanelState.resolvePendingTransitions();

        assertThat(
                        mPanelState.getTransition(
                                new Event.Builder("test_event1").setPanelId("panel1").build()))
                .isNotNull();
        assertThat(mPanelState.getTransition(new Event.Builder("test_event2").build())).isNotNull();
    }

    @Test
    public void parse_systemBar_resolvesTransitionsAutomatically() throws Exception {
        String xml =
                """
                <SystemBar id="nav" defaultVariant="open" type="navigation">
                    <Variant id="open">
                        <Visibility isVisible="true"/>
                    </Variant>
                    <Variant id="closed">
                        <Visibility isVisible="false"/>
                    </Variant>
                    <Transitions>
                        <Transition onEvent="test_event" toVariant="closed"/>
                    </Transitions>
                </SystemBar>""";

        XmlPullParser parser = createParser(xml);
        parser.nextTag(); // START_TAG <SystemBar>

        SystemBarParser systemBarParser = new SystemBarParser();
        PanelState panelState = systemBarParser.parseTag(mContext, parser);

        // Verification: ensure that no manual call to resolvePendingTransitions is needed.
        assertThat(panelState.getTransition(new Event.Builder("test_event").build())).isNotNull();
        assertThat(panelState.getTransition(new Event.Builder("test_event").build())
                .getToVariant().getId()).isEqualTo("closed");
    }

    private XmlPullParser createParser(String xml) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        return parser;
    }
}
