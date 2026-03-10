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
import com.android.car.scalableui.loader.xml.ParserEnv
import com.android.car.scalableui.loader.xml.XmlPullParserHelper
import com.android.car.scalableui.model.Action
import com.android.car.scalableui.model.ExternalConfig
import com.android.car.scalableui.model.PanelState
import java.io.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/**
 * Parser for the `<ExternalConfigs>` tag that holds multiple child root elements.
 */
class ExternalConfigsParser : TagParser<ExternalConfig> {

    companion object {
        const val EXTERNAL_CONFIGS_TAG = "ExternalConfigs"
        private val TAG = ExternalConfigsParser::class.java.simpleName
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parseTag(env: ParserEnv, parser: XmlPullParser): ExternalConfig {
        parser.require(XmlPullParser.START_TAG, null, EXTERNAL_CONFIGS_TAG)

        val config = ExternalConfig()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            val childTagName = parser.name
            val childParser = env.registry.getParser<Any>(childTagName)

            if (childParser != null) {
                val result = childParser.parseTag(env, parser)

                if (result is PanelState) {
                    config.panels.add(result)
                } else if (result is List<*>) {
                    for (item in result) {
                        if (item is Action) {
                            config.actions.add(item)
                        } else {
                            Log.w(
                                TAG,
                                "Unexpected list item type: ${item?.javaClass?.name ?: "null"}"
                            )
                        }
                    }
                } else {
                    Log.w(TAG, "Unknown parsed object type ignored: ${result.javaClass.name}")
                }
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser)
            }
        }

        return config
    }
}
