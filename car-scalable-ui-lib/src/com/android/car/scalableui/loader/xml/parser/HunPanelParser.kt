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

import android.util.Log
import android.util.Xml
import com.android.car.scalableui.loader.xml.ParserEnv
import com.android.car.scalableui.loader.xml.XmlPullParserHelper
import com.android.car.scalableui.loader.xml.parser.BackgroundParser.BACKGROUND_TAG
import com.android.car.scalableui.loader.xml.parser.PanelParser.DEFAULT_VARIANT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.parser.PanelParser.DISPLAY_ID
import com.android.car.scalableui.loader.xml.parser.ParserUtils.getIdName
import com.android.car.scalableui.loader.xml.parser.VariantParser.ID_ATTRIBUTE
import com.android.car.scalableui.loader.xml.parser.VariantParser.PARENT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.parser.VariantParser.VARIANT_TAG
import com.android.car.scalableui.model.Background
import com.android.car.scalableui.model.GravityVariant
import com.android.car.scalableui.model.HunState
import com.android.car.scalableui.model.PanelState
import com.android.car.scalableui.model.PanelType
import com.android.car.scalableui.model.Variant
import java.io.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/**
 * Parsers [PanelState] from XML for HunPanel tags.
 */
class HunPanelParser : TagParser<PanelState> {

    companion object {
        public const val HUN_PANEL_ID = "_Hun_Panel"
        private const val TAG = "HunPanelParser"
        const val HUN_PANEL_TAG = "HunPanel"
    }

    private val attributes = AttributeMap.builder<HunState.Builder>()
        .add(DEFAULT_VARIANT_ATTRIBUTE) { context, value, builder ->
            builder.setDefaultVariant(getIdName(context.context, value))
        }
        .addInteger(DISPLAY_ID) { builder, value ->
            builder.setDisplayId(value)
        }
        .build()

    override fun parseTag(env: ParserEnv, parser: XmlPullParser): PanelState {
        parser.require(XmlPullParser.START_TAG, null, HUN_PANEL_TAG)

        val builder = HunState.Builder(HUN_PANEL_ID, PanelType.HUN)
        attributes.parse(env, parser, builder)

        val hunState = builder.build()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                // A <Variant> tag within a <HunPanel> is parsed as a GravityVariant.
                VARIANT_TAG -> hunState.addVariant(
                    parseGravityVariant(
                        env,
                        hunState,
                        parser,
                        env.displayId
                    )
                )
                else -> {
                    val childParser = env.registry.getChildParser(
                        PanelState::class.java,
                        parser.name
                    )
                    if (childParser != null) {
                        childParser.parse(env, parser, hunState)
                    } else {
                        XmlPullParserHelper.throwIfUnknownTag(parser)
                    }
                }
            }
        }
        hunState.resolvePendingTransitions()
        hunState.resetVariant() // Set the initial variant

        return hunState
    }

    /**
     * Parses a `<Variant>` tag within a `<HunPanel>` tag, creating a
     * [GravityVariant] which includes custom Hun properties.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseGravityVariant(
        env: ParserEnv,
        hunState: HunState,
        parser: XmlPullParser,
        displayId: Int
    ): GravityVariant {
        parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG)
        val attrs = Xml.asAttributeSet(parser)
        val valueParser = env.valueParser

        val idStr = attrs.getAttributeValue(null, ID_ATTRIBUTE)
        val id = valueParser.parseString(env.context, idStr ?: "")
        val idName = getIdName(env.context, id)

        val parentVariantIdStr = attrs.getAttributeValue(null, PARENT_ATTRIBUTE)

        val variantBuilder = GravityVariant.Builder(id, idName)
            .setParentId(parentVariantIdStr)

        // Set the panel ID so child parsers can access it
        variantBuilder.setPanelId(HUN_PANEL_ID)

        val childEnv = env.withDisplayId(displayId)
        val customParsers = mapOf(
            BACKGROUND_TAG to object : XmlChildParser<Variant.Builder> {
                override fun parse(env: ParserEnv, parser: XmlPullParser, b: Variant.Builder) {
                    val tagParser = env.registry.getParser<Background>(BACKGROUND_TAG)
                        ?: throw XmlPullParserException("No parser registered for $BACKGROUND_TAG")
                    val bg = tagParser.parseTag(env, parser)
                    b.addDecor(bg.toDecor(HUN_PANEL_ID))
                }
            }
        )

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            val name = parser.name

            val customParser = customParsers[name]
            if (customParser != null) {
                @Suppress("UNCHECKED_CAST")
                (customParser as XmlChildParser<Variant.Builder>).parse(
                    childEnv,
                    parser,
                    variantBuilder
                )
            } else {
                val childParser = childEnv.registry.getChildParser(
                    variantBuilder.javaClass,
                    name
                )
                if (childParser != null) {
                    @Suppress("UNCHECKED_CAST")
                    (childParser as XmlChildParser<Variant.Builder>).parse(
                        childEnv,
                        parser,
                        variantBuilder
                    )
                } else {
                    Log.w(TAG, "Unsupported Variant Tag in Hun: $name")
                    XmlPullParserHelper.throwIfUnknownTag(parser)
                }
            }
        }
        // Deferred resolution of parent variant using build(PanelState)
        return variantBuilder.build(hunState) as GravityVariant
    }
}
