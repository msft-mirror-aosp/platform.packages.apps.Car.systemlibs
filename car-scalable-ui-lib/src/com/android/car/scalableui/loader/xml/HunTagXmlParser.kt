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
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.view.Display
import android.view.Gravity as ViewGravity
import androidx.annotation.NonNull
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.BOUNDS_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.DEFAULT_VARIANT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.DISPLAY_ID
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.ID_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.INSETS_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.PARENT_ATTRIBUTE
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.TRANSITIONS_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.VARIANT_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.VISIBILITY_TAG
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getIdName
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantBackgroundParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantBoundsParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantInsetsParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.getVariantVisibilityParser
import com.android.car.scalableui.loader.xml.PanelTagXmlParser.parseTransitions
import com.android.car.scalableui.model.GravityVariant
import com.android.car.scalableui.model.HunState
import com.android.car.scalableui.model.PanelType
import com.android.car.scalableui.model.Variant
import java.io.IOException
import java.util.Locale
import java.util.StringTokenizer
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

internal const val HUN_TAG = "HunPanel"
const val HUN_PANEL_ID = "_Hun_Panel"
private const val TAG = "HunTagXmlParser"
private const val BACKGROUND_TAG = "Background"
internal const val GRAVITY_TAG: String = "Gravity"
private const val GRAVITY_VALUE_ATTRIBUTE: String = "value"
private const val GRAVITY_SEPARATOR: String = "|"

/**
 * Parses the `<HunPanel>` tag and its children from the provided XML parser.
 */
@Throws(XmlPullParserException::class, IOException::class)
fun parseHun(context: Context, parser: XmlPullParser): HunState {
    parser.require(XmlPullParser.START_TAG, null, HUN_TAG)
    val attrs = Xml.asAttributeSet(parser)
    val defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE)
    val displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID)
    val displayId = displayIdStr?.toInt() ?: Display.DEFAULT_DISPLAY

    val builder = HunState.Builder(HUN_PANEL_ID, PanelType.HUN)
        .setDefaultVariant(defaultVariant)
        .setDisplayId(displayId)
    val hunState = builder.build()

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) continue
        when (parser.name) {
            // A <Variant> tag within a <HunPanel> is parsed as a GravityVariant.
            VARIANT_TAG -> hunState.addVariant(
                parseGravityVariant(
                    context,
                    hunState,
                    parser,
                    displayId
                )
            )
            TRANSITIONS_TAG -> {
                val transitions = parseTransitions(
                    context,
                    displayId,
                    hunState,
                    parser
                )
                transitions.forEach { hunState.addTransition(it) }
            }
            else -> XmlPullParserHelper.skip(parser)
        }
    }
    hunState.setVariant(defaultVariant) // Set the initial variant
    return hunState
}

/**
 * Parses a `<Variant>` tag within a `<HunPanel>` tag, creating a
 * [GravityVariant] which includes custom Hun properties.
 */
@Throws(IOException::class, XmlPullParserException::class)
private fun parseGravityVariant(
    context: Context,
    hunState: HunState,
    parser: XmlPullParser,
    displayId: Int
): GravityVariant {
    parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG)
    val attrs = Xml.asAttributeSet(parser)
    val id = attrs.getAttributeValue(null, ID_ATTRIBUTE)
    val idName = getIdName(context, id)
    val parentVariantId = attrs.getAttributeValue(null, PARENT_ATTRIBUTE)
    val parentVariant = hunState.getVariant(parentVariantId)

    val variantBuilder = GravityVariant.Builder(id, idName)
        .setParent(parentVariant)

    // Create a map of property parsers, including standard and custom ones.
    val parsers = mapOf<String, VariantPropertyParser>(
        VISIBILITY_TAG to getVariantVisibilityParser(),
        BOUNDS_TAG to getVariantBoundsParser(BOUNDS_TAG),
        BACKGROUND_TAG to getVariantBackgroundParser(HUN_PANEL_ID),
        GRAVITY_TAG to getVariantGravityParser(),
        INSETS_TAG to getVariantInsetsParser(),
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

internal fun getVariantGravityParser(): VariantPropertyParser {
    return VariantPropertyParser { _: Context, parser: XmlPullParser,
        builder: Variant.Builder, _: Int ->
        (builder as GravityVariant.Builder).setGravity(
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
    var gravity: Int = ViewGravity.NO_GRAVITY
    val tokenizer = StringTokenizer(value, GRAVITY_SEPARATOR)
    while (tokenizer.hasMoreTokens()) {
        val token = tokenizer.nextToken().uppercase(Locale.getDefault())
        gravity = gravity or when (token) {
            "TOP" -> ViewGravity.TOP
            "BOTTOM" -> ViewGravity.BOTTOM
            "LEFT" -> ViewGravity.LEFT
            "RIGHT" -> ViewGravity.RIGHT
            "CENTER" -> ViewGravity.CENTER
            "CENTER_HORIZONTAL" -> ViewGravity.CENTER_HORIZONTAL
            "CENTER_VERTICAL" -> ViewGravity.CENTER_VERTICAL
            "START" -> ViewGravity.START
            "END" -> ViewGravity.END
            "CLIP_VERTICAL" -> ViewGravity.CLIP_VERTICAL
            "CLIP_HORIZONTAL" -> ViewGravity.CLIP_HORIZONTAL
            else -> ViewGravity.NO_GRAVITY
        }
    }
    return gravity
}
