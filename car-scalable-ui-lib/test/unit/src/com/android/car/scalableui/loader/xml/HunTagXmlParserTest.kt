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
import android.platform.test.annotations.RequiresFlagsEnabled
import android.view.Gravity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.scalableui.Flags
import com.android.car.scalableui.model.Event
import com.android.car.scalableui.model.GravityVariant
import com.android.car.scalableui.unit.R
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@RunWith(AndroidJUnit4::class)
class HunTagXmlParserTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    @Throws(IOException::class, XmlPullParserException::class)
    fun parseHun_fromXmlResource_parsesCorrectly() {
        val parser = context.resources.getXml(R.xml.hun_panel_test)
        var eventType: Int = parser.getEventType()
        while (eventType == XmlPullParser.START_DOCUMENT || (eventType == XmlPullParser.TEXT &&
                    parser.isWhitespace())
        ) {
            eventType = parser.next()
        }
        val hunState = parseHun(context, parser)

        assertThat(hunState).isNotNull()
        assertThat(hunState.id).isEqualTo("_Hun_Panel")
        assertThat(hunState.currentVariant?.idName).isEqualTo("base")

        val variant1 = hunState.getVariant("@" + R.id.variant1) as GravityVariant?
        assertThat(variant1).isNotNull()
        assertThat(variant1?.isVisible).isTrue()
        assertThat(variant1?.gravity).isEqualTo(Gravity.TOP)
        assertThat(variant1?.decors).isNotEmpty()

        val variant2 = hunState.getVariant("@" + R.id.variant2) as GravityVariant?
        assertThat(variant2).isNotNull()
        assertThat(variant2?.isVisible).isFalse()
        assertThat(variant2?.gravity).isEqualTo(Gravity.TOP)
        assertThat(variant2?.decors).isEmpty()

        val event1 = Event.Builder("event1").build()
        val transition1 = hunState.getTransition(event1)
        assertThat(transition1).isNotNull()
        assertThat(transition1?.toVariant?.idName).isEqualTo("variant1")

        val event2 = Event.Builder("event2").build()
        val transition2 = hunState.getTransition(event2)
        assertThat(transition2).isNotNull()
        assertThat(transition2?.toVariant?.idName).isEqualTo("variant2")
    }
}
