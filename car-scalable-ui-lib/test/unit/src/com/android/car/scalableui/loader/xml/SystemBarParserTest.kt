/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.android.car.scalableui.loader.xml.parser.BackgroundParser
import com.android.car.scalableui.loader.xml.parser.BoundsParser
import com.android.car.scalableui.loader.xml.parser.GravityParser
import com.android.car.scalableui.loader.xml.parser.InsetsParser
import com.android.car.scalableui.loader.xml.parser.ResourceValueParser
import com.android.car.scalableui.loader.xml.parser.SystemBarParser
import com.android.car.scalableui.loader.xml.parser.TransitionParser
import com.android.car.scalableui.loader.xml.parser.VisibilityParser
import com.android.car.scalableui.model.PanelState
import com.android.car.scalableui.model.Variant
import com.android.car.scalableui.unit.R
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@RunWith(AndroidJUnit4::class)
class SystemBarParserTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseSystemBar_withNegativeZOrder_throwsException() {
        val parser = context.resources.getXml(R.xml.system_bar_invalid_z_order)
        var eventType: Int = parser.eventType
        while (eventType == XmlPullParser.START_DOCUMENT || (eventType == XmlPullParser.TEXT &&
                    parser.isWhitespace())
        ) {
            eventType = parser.next()
        }
        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()
        registry.registerParser(VisibilityParser.VISIBILITY_TAG, VisibilityParser())
        registry.registerParser(BoundsParser.BOUNDS_TAG, BoundsParser())
        registry.registerParser(BackgroundParser.BACKGROUND_TAG, BackgroundParser())
        registry.registerParser(InsetsParser.INSETS_TAG, InsetsParser())
        registry.registerParser(GravityParser.GRAVITY_TAG, GravityParser())

        val parserContext = ParserEnv(context, valueParser, registry)
        val systemBarParser = SystemBarParser()

        val exception = assertThrows(XmlPullParserException::class.java) {
            systemBarParser.parseTag(parserContext, parser)
        }

        assertThat(exception.message).contains("barZOrder must be a positive integer")
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseSystemBar_setsContextDisplayId_forChildren() {
        val displayId = 2
        val parser = context.resources.getXml(R.xml.system_bar_top_test)
        var eventType: Int = parser.eventType
        while (eventType == XmlPullParser.START_DOCUMENT || (eventType == XmlPullParser.TEXT &&
                    parser.isWhitespace())
        ) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()
        CoreParserModule().registerParsers(registry)

        var capturedDisplayId = -1
        val childParser = object : XmlChildParser<PanelState> {
            override fun parse(
                context: ParserEnv,
                parser: XmlPullParser,
                parent: PanelState
            ) {
                capturedDisplayId = context.displayId
                var depth = 1
                while (depth > 0) {
                    when (parser.next()) {
                        org.xmlpull.v1.XmlPullParser.END_TAG -> depth--
                        org.xmlpull.v1.XmlPullParser.START_TAG -> depth++
                    }
                }
            }
        }

        val parserContext = ParserEnv(context, valueParser, registry)
        val systemBarParser = SystemBarParser()

        registry.registerChildParser(
            PanelState::class.java,
            TransitionParser.TRANSITIONS_TAG,
            childParser
        )

        systemBarParser.parseTag(parserContext, parser)

        assertThat(capturedDisplayId).isEqualTo(displayId)
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    fun parseSystemBar_withResourceId_setsDefaultVariantIdName() {
        val xml = """
            <SystemBar id="top" defaultVariant="@id/variant1">
            </SystemBar>
        """.trimIndent()

        val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(java.io.StringReader(xml))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG) {
            eventType = parser.next()
        }

        val valueParser = ResourceValueParser()
        val registry = XmlParserRegistry()

        val parserContext = ParserEnv(context, valueParser, registry)
        val systemBarParser = SystemBarParser()
        val panelState = systemBarParser.parseTag(parserContext, parser)

        assertThat(panelState).isNotNull()
        panelState.addVariant(
            Variant.Builder("variant1", "variant1").build()
        )
        panelState.resetVariant()
        assertThat(panelState.currentVariant?.idName).isEqualTo("variant1")
    }
}
