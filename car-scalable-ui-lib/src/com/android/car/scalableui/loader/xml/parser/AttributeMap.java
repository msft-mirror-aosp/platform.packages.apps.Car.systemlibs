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

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to map XML attributes to builder setters in a declarative way.
 *
 * @param <T> The type of the builder (or object) being populated.
 */
public class AttributeMap<T> {

    private final Map<String, AttributeHandler<T>> mHandlers;

    private AttributeMap(Map<String, AttributeHandler<T>> handlers) {
        mHandlers = handlers;
    }

    /**
     * Parses attributes from the current tag in the XmlPullParser and applies them to the target.
     *
     * @param env The parser context.
     * @param parser The XmlPullParser positioned at a start tag.
     * @param target The target object (e.g., a Builder) to populate.
     * @throws XmlPullParserException If an XML parsing error occurs.
     * @throws IOException If an I/O error occurs.
     */
    public void parse(@NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull T target)
            throws XmlPullParserException, IOException {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            AttributeHandler<T> handler = mHandlers.get(name);
            String value = parser.getAttributeValue(i);
            if (handler != null) {
                handler.handle(env, value, target);
            }
        }
    }

    /** Creates a new Builder for AttributeMap. */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder for AttributeMap.
     *
     * @param <T> The type of the state object being populated.
     */
    public static class Builder<T> {
        private final Map<String, AttributeHandler<T>> mHandlers = new HashMap<>();

        /** Generic add method. */
        public Builder<T> add(String attributeName, AttributeHandler<T> handler) {
            mHandlers.put(attributeName, handler);
            return this;
        }

        /** Adds a mapping for a String attribute. */
        public Builder<T> addString(String attributeName, AttributeSetter<T, String> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        String parsed = env.getValueParser().parseString(env.getContext(), value);
                        setter.set(target, parsed);
                    });
        }

        /** Adds a mapping for an Integer attribute (using parseInteger). */
        public Builder<T> addInteger(String attributeName, AttributeSetter<T, Integer> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        int parsed = env.getValueParser().parseInteger(env.getContext(), value, 0);
                        setter.set(target, parsed);
                    });
        }

        /** Adds a mapping for a Boolean attribute. */
        public Builder<T> addBoolean(String attributeName, AttributeSetter<T, Boolean> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        boolean parsed =
                                env.getValueParser().parseBoolean(env.getContext(), value, false);
                        setter.set(target, parsed);
                    });
        }

        /** Adds a mapping for a Resource ID attribute. */
        public Builder<T> addResourceId(String attributeName, AttributeSetter<T, Integer> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        int parsed =
                                env.getValueParser().parseResourceId(env.getContext(), value, 0);
                        setter.set(target, parsed);
                    });
        }

        /** Adds a mapping for a Float attribute. */
        public Builder<T> addFloat(String attributeName, AttributeSetter<T, Float> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        float parsed = env.getValueParser().parseFloat(env.getContext(), value, 0f);
                        setter.set(target, parsed);
                    });
        }

        /**
         * Adds a generic mapping using a ValueParser method reference. Example: .add("prop",
         * ValueParser::parseFloat, Builder::setProp)
         */
        public <V> Builder<T> add(
                String attributeName,
                ValueParserMethod<V> parserMethod,
                AttributeSetter<T, V> setter) {
            return add(
                    attributeName,
                    (env, value, target) -> {
                        V parsed =
                                parserMethod.parse(env.getValueParser(), env.getContext(), value);
                        setter.set(target, parsed);
                    });
        }

        /** Builds the AttributeMap. */
        public AttributeMap<T> build() {
            return new AttributeMap<>(new HashMap<>(mHandlers));
        }
    }

    /**
     * Interface for handling a single attribute.
     *
     * @param <T> The type of the state object.
     */
    public interface AttributeHandler<T> {
        /** Handles the attribute. */
        void handle(ParserEnv env, String value, T target)
                throws XmlPullParserException, IOException;
    }

    /**
     * Interface for setting a value on the target, allowing checked exceptions.
     *
     * @param <T> The type of the state object.
     * @param <V> The type of the value being set.
     */
    public interface AttributeSetter<T, V> {
        /** Sets the value on the target. */
        void set(T target, V value) throws XmlPullParserException, IOException;
    }

    /**
     * Interface for ValueParser methods that take (Context, String).
     *
     * @param <V> The return type of the parsing method.
     */
    public interface ValueParserMethod<V> {
        /** Parses specific value. */
        V parse(ValueParser parser, Context context, String value);
    }
}
