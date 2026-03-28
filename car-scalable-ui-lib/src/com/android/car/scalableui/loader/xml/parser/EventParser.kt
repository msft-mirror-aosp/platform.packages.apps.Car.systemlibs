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
package com.android.car.scalableui.loader.xml.parser

import android.util.Xml
import android.view.Display
import com.android.car.scalableui.loader.xml.ParserEnv
import com.android.car.scalableui.loader.xml.XmlPullParserHelper
import com.android.car.scalableui.loader.xml.parser.ParserUtils.getIdName
import com.android.car.scalableui.loader.xml.parser.VariantParser.ID_ATTRIBUTE
import com.android.car.scalableui.model.Event
import java.io.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

class EventParser : TagParser<Event> {

    companion object {
        const val EVENT_TAG = "Event"
        private const val PANEL_ATTRIBUTE = "panel"
        private const val PANEL_ID_ATTRIBUTE = "panelId"
        private const val COMPONENT_NAME_ATTRIBUTE = "componentName"
        private const val PACKAGE_NAME_ATTRIBUTE = "packageName"
        // Attribute to specify the endVariant for the animation completion event.
        private const val END_VARIANT_ATTRIBUTE = "endVariant"
        private const val TOKENS_ATTRIBUTE = "tokens"

        private val ATTRIBUTES = AttributeMap.builder<Event.Builder>()
            .add(PANEL_ATTRIBUTE) { context, value, builder ->
                builder.setPanelId(getIdName(context.context, value))
            }
            .add(PANEL_ID_ATTRIBUTE) { context, value, builder ->
                builder.setPanelId(getIdName(context.context, value))
            }
            .addString(
                COMPONENT_NAME_ATTRIBUTE
            ) { builder, value -> builder.setComponentName(value) }
            .addString(PACKAGE_NAME_ATTRIBUTE) { builder, value -> builder.setPackageName(value) }
            .addString(END_VARIANT_ATTRIBUTE) { builder, value -> builder.setToVariantId(value) }
            .addString(TOKENS_ATTRIBUTE) { builder, value -> builder.addTokensFromString(value) }
            .build()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parseTag(env: ParserEnv, parser: XmlPullParser): Event {
        parser.require(XmlPullParser.START_TAG, null, EVENT_TAG)
        val attrs = Xml.asAttributeSet(parser)
        val id = attrs.getAttributeValue(null, ID_ATTRIBUTE)

        val builder = Event.Builder(id)
        val displayId = env.displayId
        if (displayId != Display.INVALID_DISPLAY) {
            builder.addApplicableDisplay(displayId)
        }

        ATTRIBUTES.parse(env, parser, builder)

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser) // Skip any nested tags
        }
        return builder.build()
    }
}
