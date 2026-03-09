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

/** Interface for modules that contribute parsers to the {@link XmlParserRegistry}. */
public interface ParserModule {
    /**
     * Registers parsers provided by this module.
     *
     * @param registry The registry to register parsers with.
     */
    void registerParsers(@NonNull XmlParserRegistry registry);
}
