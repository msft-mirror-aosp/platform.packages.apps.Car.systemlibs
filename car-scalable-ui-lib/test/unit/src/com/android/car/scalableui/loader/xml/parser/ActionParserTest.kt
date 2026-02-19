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
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class ActionParserTest {

    private lateinit var context: ParserEnv
    private lateinit var parser: ActionParser

    @Before
    fun setUp() {
        val androidContext = ApplicationProvider.getApplicationContext<Context>()
        val registry = XmlParserRegistry()
        registry.registerParser(EventParser.EVENT_TAG, EventParser())
        context = ParserEnv(androidContext, ResourceValueParser(), registry)
        parser = ActionParser()
    }

    @Test
    fun parse_withNestedEventTags_succeeds() {
        val xml = """
            <Actions>
                <Action intent="intent:#Intent;action=android.intent.action.MAIN;end">
                    <Event id="test_event1" panel="panel1"/>
                    <Event id="test_event2"/>
                </Action>
            </Actions>
        """.trimIndent()

        val xmlParser = createParser(xml)
        xmlParser.nextTag() // START_TAG <Actions>

        val actions = parser.parseTag(context, xmlParser)

        assertThat(actions).hasSize(1)
        val action = actions[0]
        assertThat(action.intent).isNotNull()
        assertThat(action.events).hasSize(2)
        assertThat(action.events[0].id).isEqualTo("test_event1")
        assertThat(action.events[0].panelId).isEqualTo("panel1")
        assertThat(action.events[1].id).isEqualTo("test_event2")
        assertThat(action.events[1].panelId).isNull()
    }

    private fun createParser(xml: String): XmlPullParser {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8)), null)
        return parser
    }
}
