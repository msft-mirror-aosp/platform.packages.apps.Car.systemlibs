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

package com.android.car.scalableui.loader.xml.parser

import android.content.Context
import android.util.Xml
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.scalableui.loader.xml.ParserEnv
import com.android.car.scalableui.loader.xml.XmlParserRegistry
import com.android.car.scalableui.model.PanelType
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@RunWith(AndroidJUnit4::class)
class ExternalConfigsParserTest {

    private lateinit var env: ParserEnv
    private lateinit var parser: ExternalConfigsParser

    @Before
    fun setUp() {
        val androidContext = ApplicationProvider.getApplicationContext<Context>()
        val registry = XmlParserRegistry()

        registry.registerParser(PanelParser.TASK_PANEL_TAG, PanelParser())
        registry.registerParser(PanelParser.DECOR_PANEL_TAG, PanelParser())
        registry.registerParser(ActionParser.ACTIONS_TAG, ActionParser())
        registry.registerParser(EventParser.EVENT_TAG, EventParser())

        env = ParserEnv(androidContext, ResourceValueParser(), registry)
        parser = ExternalConfigsParser()
    }

    @Test
    fun parseTag_parsesPanelsAndActions() {
        val xml = """
            <ExternalConfigs>
                <TaskPanel id="test_panel_1" />
                <DecorPanel id="test_panel_2" />
                <Actions>
                    <Action intent="intent:#Intent;action=android.intent.action.MAIN;end" />
                </Actions>
            </ExternalConfigs>
        """.trimIndent()

        val xmlParser = createParser(xml)
        xmlParser.nextTag() // START_TAG <ExternalConfigs>

        val config = parser.parseTag(env, xmlParser)

        assertThat(config.panels).hasSize(2)
        assertThat(config.panels[0].id).isEqualTo("test_panel_1")
        assertThat(config.panels[0].type).isEqualTo(PanelType.TASK)

        assertThat(config.panels[1].id).isEqualTo("test_panel_2")
        assertThat(config.panels[1].type).isEqualTo(PanelType.DECOR)

        assertThat(config.actions).hasSize(1)
        assertThat(config.actions[0].intent).isNotNull()
    }

    @Test(expected = XmlPullParserException::class)
    fun parseTag_throwsOnUnknownTag() {
        val xml = """
            <ExternalConfigs>
                <UnknownTag />
            </ExternalConfigs>
        """.trimIndent()

        val xmlParser = createParser(xml)
        xmlParser.nextTag()

        parser.parseTag(env, xmlParser)
    }

    private fun createParser(xml: String): XmlPullParser {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8)), null)
        return parser
    }
}
