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
class PanelControllerParserTest {

    private lateinit var context: ParserEnv
    private lateinit var parser: PanelControllerParser

    @Before
    fun setUp() {
        val androidContext = ApplicationProvider.getApplicationContext<Context>()
        val registry = XmlParserRegistry()
        context = ParserEnv(androidContext, ResourceValueParser(), registry)
        parser = PanelControllerParser()
    }

    @Test
    fun parse_withNestedController_doesNotConsumeBeyondEndTag() {
        val xml = """
            <SomeParent>
                <Controller id="test_controller">
                    <ControllerName>com.android.car.test.ControllerName</ControllerName>
                </Controller>
                <NextElement/>
            </SomeParent>
        """.trimIndent()

        val xmlParser = createParser(xml)
        xmlParser.nextTag() // START_TAG <SomeParent>
        xmlParser.nextTag() // START_TAG <Controller>

        val controller = parser.parseTag(context, xmlParser)

        assertThat(controller).isNotNull()
        assertThat(controller.id).isEqualTo("test_controller")

        // Ensure the parser is now right after the </Controller> end tag,
        // and next() should give us <NextElement> or whitespace
        xmlParser.nextTag()
        assertThat(xmlParser.eventType).isEqualTo(XmlPullParser.START_TAG)
        assertThat(xmlParser.name).isEqualTo("NextElement")
    }

    private fun createParser(xml: String): XmlPullParser {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8)), null)
        return parser
    }
}
