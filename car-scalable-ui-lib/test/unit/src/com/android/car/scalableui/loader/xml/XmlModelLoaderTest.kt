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

package com.android.car.scalableui.loader.xml

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.scalableui.loader.xml.parser.TagParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class XmlModelLoaderTest {

    private lateinit var context: Context
    private lateinit var loader: XmlModelLoader
    private lateinit var mockParser: XmlResourceParser
    private lateinit var mockRegistry: XmlParserRegistry
    private lateinit var mockParserContext: ParserEnv

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun parseXml_withCommentAtStart_skipsCommentAndSucceeds() {
        loader = XmlModelLoader(context)
        val parseXmlMethod = XmlModelLoader::class.java.getDeclaredMethod(
            "parseXml",
            XmlResourceParser::class.java,
            ParserEnv::class.java
        )
        parseXmlMethod.isAccessible = true

        mockParser = mock(XmlResourceParser::class.java)

        // Initial state
        `when`(mockParser.eventType).thenReturn(XmlPullParser.START_DOCUMENT)

        // Sequence of next() calls
        `when`(mockParser.next())
            .thenReturn(XmlPullParser.COMMENT) // First next() returns COMMENT
            .thenReturn(XmlPullParser.START_TAG) // Second next() returns START_TAG

        `when`(mockParser.name).thenReturn("Actions")

        val mockParserModule = mock(ParserModule::class.java)
        loader = XmlModelLoader(context, mockParserModule)

        val registryField = XmlModelLoader::class.java.getDeclaredField("mRegistry")
        registryField.isAccessible = true
        val registry = registryField.get(loader) as XmlParserRegistry

        val mockTagParser = mock(TagParser::class.java)
        registry.registerParser("Actions", mockTagParser)

        parseXmlMethod.invoke(loader, mockParser, mock(ParserEnv::class.java))

        // Verify parseTag was called
        verify(mockTagParser).parseTag(any(), eq(mockParser))
    }
}
