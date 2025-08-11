/*
 * Copyright (C) 2025 The Android Open Source Project
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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.view.Display
import android.view.Gravity
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.BOUNDS_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.DEFAULT_VARIANT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.DISPLAY_ID
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.ID_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.PARENT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.TRANSITIONS_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.VARIANT_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.VISIBILITY_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getIdName
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantBoundsParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantVisibilityParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseTransitions
import com.android.car.scalableui.model.HunState
import com.android.car.scalableui.model.HunVariant
import com.android.car.scalableui.model.Variant
import java.io.IOException
import java.util.Locale
import java.util.StringTokenizer
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/**
 * A utility class for parsing Heads-Up Notification (Hun) tags from an XML file.
 * This parser is responsible for translating the `<HunPanel>` tag and its nested
 * properties into a [HunState] object.
 */
class HunTagXmlParser {

    companion object {
        const val HUN_TAG = "HunPanel"
        const val HUN_PANEL_ID = "_Hun_Panel"
        private val TAG = HunTagXmlParser::class.java.simpleName

        private const val SCRIM_TAG = "Scrim"
        private const val DRAWABLE_ATTRIBUTE = "drawable"
        private const val GRAVITY_TAG: String = "Gravity"
        private const val GRAVITY_VALUE_ATTRIBUTE: String = "value"
        private const val GRAVITY_SEPARATOR: String = "|"

        /**
         * Parses the `<HunPanel>` tag and its children from the provided XML parser.
         */
        @JvmStatic
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseHun(context: Context, parser: XmlPullParser): HunState {
            parser.require(XmlPullParser.START_TAG, null, HUN_TAG)
            val attrs = Xml.asAttributeSet(parser)
            val defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE)
            val displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID)
            val displayId = displayIdStr?.toInt() ?: Display.DEFAULT_DISPLAY

            val builder = HunState.Builder(HUN_PANEL_ID)
                .setDefaultVariant(defaultVariant)
                .setDisplayId(displayId)

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                when (parser.name) {
                    // A <Variant> tag within a <HunPanel> is parsed as a HunVariant.
                    VARIANT_TAG -> builder.addVariant(
                        parseHunVariant(
                            context,
                            builder.build(),
                            parser,
                            displayId
                        )
                    )
                    TRANSITIONS_TAG -> {
                        val transitions = parseTransitions(context, builder.build(), parser)
                        transitions.forEach { builder.addTransition(it) }
                    }
                    else -> XmlPullParserHelper.skip(parser)
                }
            }
            return builder.build()
        }

        /**
         * Parses a `<Variant>` tag within a `<HunPanel>` tag, creating a
         * [HunVariant] which includes custom Hun properties.
         */
        @Throws(IOException::class, XmlPullParserException::class)
        private fun parseHunVariant(
            context: Context,
            hunState: HunState,
            parser: XmlPullParser,
            displayId: Int
        ): HunVariant {
            parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG)
            val attrs = Xml.asAttributeSet(parser)
            val id = attrs.getAttributeValue(null, ID_ATTRIBUTE)
            val idName = getIdName(context, id)
            val parentVariantId = attrs.getAttributeValue(null, PARENT_ATTRIBUTE)
            val parentVariant = hunState.getVariant(parentVariantId)

            val variantBuilder = HunVariant.Builder(id, idName)
                .setParent(parentVariant)

            // Create a map of property parsers, including standard and custom ones.
            val parsers = mapOf<String, VariantPropertyParser>(
                VISIBILITY_TAG to getVariantVisibilityParser(),
                BOUNDS_TAG to getVariantBoundsParser(),
                SCRIM_TAG to getScrimParser(),
                GRAVITY_TAG to getGravityParser(),
            )

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                val name = parser.name
                parsers[name]?.let {
                    it.parse(context, parser, variantBuilder, displayId)
                } ?: run {
                    Log.w(TAG, "Unsupported Variant Tag in Hun: $name")
                    XmlPullParserHelper.skip(parser)
                }
            }
            return variantBuilder.build()
        }

        private fun getScrimParser(): VariantPropertyParser {
            return VariantPropertyParser { context: Context, parser: XmlPullParser,
                                           builder: Variant.Builder, displayId: Int ->
                (builder as HunVariant.Builder).setScrim(
                    parseScrim(context, parser)
                )
            }
        }

        @DrawableRes
        @Throws(XmlPullParserException::class, IOException::class)
        private fun parseScrim(context: Context, parser: XmlPullParser): Drawable? {
            parser.require(XmlPullParser.START_TAG, null, SCRIM_TAG)
            val attrs = Xml.asAttributeSet(parser)
            val drawableRes = attrs.getAttributeResourceValue(null, DRAWABLE_ATTRIBUTE, 0)
            // Skip to the end tag, consuming any nested content.
            while (parser.next() != XmlPullParser.END_TAG) {}
            return if (drawableRes != 0) context.getDrawable(drawableRes) else null
        }

        private fun getGravityParser(): VariantPropertyParser {
            return VariantPropertyParser { context: Context, parser: XmlPullParser,
                                           builder: Variant.Builder, displayId: Int ->
                (builder as HunVariant.Builder).setGravity(
                    parseGravity(parser)
                )
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        private fun parseGravity(@NonNull parser: XmlPullParser): Int {
            parser.require(XmlPullParser.START_TAG, null, GRAVITY_TAG)
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            val value: String = attrs.getAttributeValue(null, GRAVITY_VALUE_ATTRIBUTE)
            // Skip any nested content.
            while (parser.next() != XmlPullParser.END_TAG) { }
            var gravity: Int = Gravity.NO_GRAVITY
            val tokenizer = StringTokenizer(value, GRAVITY_SEPARATOR)
            while (tokenizer.hasMoreTokens()) {
                val token = tokenizer.nextToken().uppercase(Locale.getDefault())
                gravity = gravity or when (token) {
                    "TOP" -> Gravity.TOP
                    "BOTTOM" -> Gravity.BOTTOM
                    "LEFT" -> Gravity.LEFT
                    "RIGHT" -> Gravity.RIGHT
                    "CENTER" -> Gravity.CENTER
                    "CENTER_HORIZONTAL" -> Gravity.CENTER_HORIZONTAL
                    "CENTER_VERTICAL" -> Gravity.CENTER_VERTICAL
                    "START" -> Gravity.START
                    "END" -> Gravity.END
                    "CLIP_VERTICAL" -> Gravity.CLIP_VERTICAL
                    "CLIP_HORIZONTAL" -> Gravity.CLIP_HORIZONTAL
                    else -> Gravity.NO_GRAVITY
                }
            }
            return gravity
        }
    }
}
