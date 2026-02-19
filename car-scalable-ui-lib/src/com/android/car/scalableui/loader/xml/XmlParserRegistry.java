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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.loader.xml.parser.TagParser;

import java.util.HashMap;
import java.util.Map;

/** Registry to map XML tags (and namespaces) to {@link TagParser} instances. */
public class XmlParserRegistry {

    // Key: Namespace URI + Tag Name (or just Tag Name if namespace is empty)
    private final Map<String, Object> mParsers = new HashMap<>();

    /**
     * Registers a parser for a specific tag name in the default (empty) namespace.
     *
     * @param tagName The tag name.
     * @param parser The parser to handle this tag.
     */
    public void registerParser(@NonNull String tagName, @NonNull TagParser<?> parser) {
        registerParser("", tagName, parser);
    }

    /**
     * Registers a parser for a specific namespace and tag name.
     *
     * @param namespaceUri The XML namespace URI (can be empty).
     * @param tagName The tag name.
     * @param parser The parser to handle this tag.
     */
    public void registerParser(
            @NonNull String namespaceUri, @NonNull String tagName, @NonNull TagParser<?> parser) {
        String key = makeKey(namespaceUri, tagName);
        mParsers.put(key, parser);
    }

    /**
     * Retrieves the parser for the given namespace and tag name.
     *
     * @param namespaceUri The XML namespace URI.
     * @param tagName The tag name.
     * @return The registered parser, or null if not found.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> TagParser<T> getParser(@Nullable String namespaceUri, @NonNull String tagName) {
        String key = makeKey(namespaceUri, tagName);
        return (TagParser<T>) mParsers.get(key);
    }

    /**
     * Retrieves the parser for the given tag name in the default (empty) namespace.
     *
     * @param tagName The tag name.
     * @return The registered parser, or null if not found.
     */
    @Nullable
    public <T> TagParser<T> getParser(@NonNull String tagName) {
        return getParser("", tagName);
    }

    /**
     * Registers a child parser for a specific parent type and tag name.
     *
     * @param parentType The class of the parent object.
     * @param tagName The tag name of the child element.
     * @param parser The parser to handle this child tag.
     */
    public <P> void registerChildParser(
            @NonNull Class<P> parentType,
            @NonNull String tagName,
            @NonNull XmlChildParser<P> parser) {
        String key = makeChildParserKey(parentType, tagName);
        mParsers.put(key, parser);
    }

    /**
     * Retrieves the child parser for the given parent type and tag name.
     *
     * @param parentType The class of the parent object.
     * @param tagName The tag name of the child element.
     * @return The registered child parser, or null if not found.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <P> XmlChildParser<P> getChildParser(
            @NonNull Class<P> parentType, @NonNull String tagName) {
        Class<?> currentType = parentType;
        while (currentType != null && currentType != Object.class) {
            String key = makeChildParserKey(currentType, tagName);
            Object parser = mParsers.get(key);
            if (parser instanceof XmlChildParser) {
                return (XmlChildParser<P>) parser;
            }
            currentType = currentType.getSuperclass();
        }
        return null;
    }

    private String makeKey(@Nullable String namespaceUri, @NonNull String tagName) {
        if (namespaceUri == null || namespaceUri.isEmpty()) {
            return tagName;
        }
        return "{" + namespaceUri + "}" + tagName;
    }

    private String makeChildParserKey(@NonNull Class<?> parentType, @NonNull String tagName) {
        return parentType.getName() + "#" + tagName;
    }
}
