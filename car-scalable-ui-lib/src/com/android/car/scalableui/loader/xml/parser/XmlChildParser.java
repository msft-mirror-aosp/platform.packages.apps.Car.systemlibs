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
 * Interface for parsers that parse an XML tag and apply the parsed data directly to a parent
 * object being built.
 *
 * <p>Unlike a {@link TagParser}, an {@code XmlChildParser} exists to modify a parent object (the
 * parent) rather than returning a result to the parent. It acts as an extension point, allowing a
 * parent parser to support new child tags without needing to know how to parse them or manage the
 * results.
 *
 * <p>For example, a {@code Variant.Builder} doesn't need to know how to parse a {@code <Alpha>} or
 * {@code <Visibility>} tag. Instead, it delegates to an {@code XmlChildParser} which parses the
 * tag and directly updates the builder.
 *
 * <p><b>When to register as an XmlChildParser ({@code registerChildParser}):</b>
 *
 * <ul>
 *   <li><b>Automatic Property Updates:</b> When the parent doesn't need to inspect or validate the
 *       parsed data before it is applied, and is comfortable with the child directly updating
 *       its state.
 *   <li><b>Dynamic Extension:</b> When you want to add new properties or modifiers to a parent
 *       component without hardcoding knowledge of the new tag into the parent parser.
 * </ul>
 *
 * <p>If the parent needs to receive the parsing result to perform custom logic, use
 * {@link TagParser} instead.
 *
 * @param <P> The type of the parent object that this parser modifies.
 */
public interface XmlChildParser<P> {

    /**
     * Parses the current tag and updates the parent object.
     *
     * @param env The parsing context.
     * @param parser The XML pull parser, positioned at the start tag of the child element.
     * @param parent The parent object to update.
     * @throws XmlPullParserException If an XML parsing error occurs.
     * @throws IOException If an I/O error occurs.
     */
    void parse(@NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull P parent)
            throws XmlPullParserException, IOException;
}
