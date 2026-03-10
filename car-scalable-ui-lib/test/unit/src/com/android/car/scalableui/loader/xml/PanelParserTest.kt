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

package com.android.car.scalableui.loader.xml

import android.content.Context
import android.platform.test.annotations.RequiresFlagsEnabled
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.scalableui.Flags
import com.android.car.scalableui.loader.xml.parser.PanelParser
import com.android.car.scalableui.loader.xml.parser.ResourceValueParser
import com.google.common.truth.Truth.assertThat
import java.io.StringReader
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

@RunWith(AndroidJUnit4::class)
class PanelParserTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseTaskPanel_withStringController_setsPendingControllerId() {
        val xml = """
            <TaskPanel id="panel_id" controller="my_controller_id">
            </TaskPanel>
        """.trimIndent()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        // Advance to START_TAG
        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()

        val parserContext = ParserEnv(context, valueParser, registry)
        val panelParser = PanelParser()
        val panelState = panelParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        assertThat(panelState.pendingControllerId).isEqualTo("my_controller_id")
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseTaskPanel_withStringResourceController_setsPendingControllerId() {
        val xml = """
            <TaskPanel id="panel_id" controller="@string/default_config">
            </TaskPanel>
        """.trimIndent()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        // Advance to START_TAG
        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()

        val parserContext = ParserEnv(context, valueParser, registry)
        val panelParser = PanelParser()
        val panelState = panelParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        // @string/default_config maps to "DEFAULT" in test/unit/res/values/strings.xml
        assertThat(panelState.pendingControllerId).isEqualTo("DEFAULT")
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseTaskPanel_withIdController_setsPendingControllerId() {
        val xml = """
            <TaskPanel id="panel_id" controller="@id/my_controller_id">
            </TaskPanel>
        """.trimIndent()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        // Advance to START_TAG
        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()

        val parserContext = ParserEnv(context, valueParser, registry)
        val panelParser = PanelParser()
        val panelState = panelParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        assertThat(panelState.pendingControllerId).isEqualTo("my_controller_id")
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseTaskPanel_withTaskBehavior_parsesBehaviors() {
        val xml = """
            <TaskPanel id="panel_id">
                <TaskBehavior taskProperties="testProp" newTaskLaunchPolicy="testPolicy" />
            </TaskPanel>
        """.trimIndent()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        // Advance to START_TAG
        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()
        com.android.car.scalableui.loader.xml.CoreParserModule().registerParsers(registry)

        val parserContext = ParserEnv(context, valueParser, registry)
        val panelParser = PanelParser()
        val panelState = panelParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        assertThat(panelState.taskBehavior).isNotNull()
        assertThat(panelState.taskBehavior?.taskProperties).isEqualTo("testProp")
        assertThat(panelState.taskBehavior?.newTaskLaunchPolicy).isEqualTo("testPolicy")
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseTaskPanel_withSafeBounds_parsesSafeBounds() {
        val xml = """
            <TaskPanel id="panel_id">
                <Variant id="var">
                    <SafeBounds left="10" top="20" right="30" bottom="40" />
                </Variant>
            </TaskPanel>
        """.trimIndent()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        // Advance to START_TAG
        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()
        com.android.car.scalableui.loader.xml.CoreParserModule().registerParsers(registry)

        val parserContext = ParserEnv(context, valueParser, registry)
        val panelParser = PanelParser()
        val panelState = panelParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        val variant = panelState.getVariant("var")
        assertThat(variant).isNotNull()
        assertThat(variant!!.safeBounds).isEqualTo(android.graphics.Rect(10, 20, 30, 40))
    }
}
