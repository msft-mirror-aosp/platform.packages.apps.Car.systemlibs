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

package com.android.car.scalableui.loader.xml.parser;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Interface for parsing a specific XML tag where the parent needs to receive the parsing result.
 *
 * <p>A {@code TagParser} is responsible for reading an XML tag and returning a Java object
 * representing that tag. The parent parser typically uses this when it needs to inspect the result,
 * perform validation, or execute custom logic before applying the data.
 *
 * <p>For example, a {@code SystemBar} parser might use a {@code TagParser<Bounds>} to parse a
 * {@code <Bounds>} tag, allowing it to validate that the bounds are appropriate for a system bar
 * before applying them to its model.
 *
 * <p><b>When to register as a TagParser ({@code registerParser}):</b>
 *
 * <ul>
 *   <li><b>Root elements:</b> Tags that form the base of a document (e.g., {@code <TaskPanel>},
 *       {@code <SystemBar>}).
 *   <li><b>Custom Parent Logic:</b> When a parent element needs to explicitly call {@code
 *       registry.getParser("TagName")} to get the {@code TagParser}, call {@code parseTag()}, and
 *       then do something specific with the returned object.
 * </ul>
 *
 * <p>If the parent doesn't need to see the result and is fine with the child directly updating the
 * parent's state (usually via a {@code Builder}), use {@link XmlChildParser} instead.
 *
 * @param <T> The type of the object being built or returned.
 */
public interface TagParser<T> {
    /**
     * Parses the current tag and returns the result.
     *
     * @param env The parsing context.
     * @param parser The XmlPullParser, positioned at the START_TAG.
     * @return The parsed object or builder.
     * @throws XmlPullParserException If an XML error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @NonNull
    T parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException;
}
