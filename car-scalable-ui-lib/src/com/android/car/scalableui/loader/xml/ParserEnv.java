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

package com.android.car.scalableui.loader.xml;

import android.content.Context;
import android.view.Display;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.parser.ValueParser;

/** Context for strict XML parsing, holding the Android Context and ValueParser. */
public class ParserEnv {
    private final Context mContext;
    private final ValueParser mValueParser;
    private final XmlParserRegistry mRegistry;

    private final int mDisplayId;

    /**
     * Creates a new parser context.
     *
     * @param context The Android Context.
     * @param valueParser The strategy for parsing values.
     * @param registry The registry of parsers.
     */
    public ParserEnv(
            @NonNull Context context,
            @NonNull ValueParser valueParser,
            @NonNull XmlParserRegistry registry) {
        this(context, valueParser, registry, Display.DEFAULT_DISPLAY);
    }

    /**
     * Creates a new parser context with a specific display ID.
     *
     * @param context The Android Context.
     * @param valueParser The strategy for parsing values.
     * @param registry The registry of parsers.
     * @param displayId The display ID to use for parsing dimensions.
     */
    public ParserEnv(
            @NonNull Context context,
            @NonNull ValueParser valueParser,
            @NonNull XmlParserRegistry registry,
            int displayId) {
        mContext = context;
        mValueParser = valueParser;
        mRegistry = registry;
        mDisplayId = displayId;
    }

    /** Returns the Android Context. */
    @NonNull
    public Context getContext() {
        return mContext;
    }

    /** Returns the ValueParser strategy. */
    @NonNull
    public ValueParser getValueParser() {
        return mValueParser;
    }

    /** Returns the Parser Registry. */
    @NonNull
    public XmlParserRegistry getRegistry() {
        return mRegistry;
    }

    /** Returns the display ID used for parsing. */
    public int getDisplayId() {
        return mDisplayId;
    }

    /** Creates a new context with the given display ID. */
    @NonNull
    public ParserEnv withDisplayId(int displayId) {
        return new ParserEnv(mContext, mValueParser, mRegistry, displayId);
    }
}
