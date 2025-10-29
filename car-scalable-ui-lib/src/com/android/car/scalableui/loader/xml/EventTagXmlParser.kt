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

import android.util.Xml
import com.android.car.scalableui.model.Event
import java.io.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

const val EVENT_TAG = "Event"
private const val ID_ATTRIBUTE = "id"
private const val PANEL_ATTRIBUTE = "panel"
private const val PANEL_ID_ATTRIBUTE = "panelId"
private const val COMPONENT_NAME_ATTRIBUTE = "componentName"
private const val PACKAGE_NAME_ATTRIBUTE = "packageName"
private const val TOKENS_ATTRIBUTE = "tokens"

@Throws(XmlPullParserException::class, IOException::class)
@JvmOverloads
fun parseEvent(parser: XmlPullParser, displayId: Int? = null): Event {
    parser.require(XmlPullParser.START_TAG, null, EVENT_TAG)
    val attrs = Xml.asAttributeSet(parser)
    val id = attrs.getAttributeValue(null, ID_ATTRIBUTE)
    val panel = attrs.getAttributeValue(null, PANEL_ATTRIBUTE)
        ?: attrs.getAttributeValue(null, PANEL_ID_ATTRIBUTE)
    val componentName = attrs.getAttributeValue(null, COMPONENT_NAME_ATTRIBUTE)
    val packageName = attrs.getAttributeValue(null, PACKAGE_NAME_ATTRIBUTE)
    val tokens = attrs.getAttributeValue(null, TOKENS_ATTRIBUTE)

    val builder = Event.Builder(id)
    builder.apply {
        panel?.let {
            setPanelId(it)
        }
        componentName?.let {
            setComponentName(it)
        }
        packageName?.let {
            setPackageName(it)
        }
        displayId?.let {
            addApplicableDisplay(it)
        }
        tokens?.let {
            addTokensFromString(it)
        }
    }

    while (parser.next() != XmlPullParser.END_TAG) {
        XmlPullParserHelper.skip(parser) // Skip any nested tags
    }
    return builder.build()
}
