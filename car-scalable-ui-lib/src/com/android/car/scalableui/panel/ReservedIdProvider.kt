/*
 * Copyright (C) 2025 The Android Open Source Project
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
package com.android.car.scalableui.panel

import com.android.car.scalableui.loader.xml.SystemBarTagXmlParser
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * A class that provides reserved panel IDs.
 */
data object ReservedIdProvider {
    /**
     * Reserved System Bar IDs
     */
    @JvmField val SYSTEM_BAR_IDS: Set<String> = java.util.Set.copyOf(
        listOf(
            SystemBarTagXmlParser.SYSTEM_BAR_PANEL_LEFT_ID,
            SystemBarTagXmlParser.SYSTEM_BAR_PANEL_TOP_ID,
            SystemBarTagXmlParser.SYSTEM_BAR_PANEL_RIGHT_ID,
            SystemBarTagXmlParser.SYSTEM_BAR_PANEL_BOTTOM_ID
        )
    )

    /**
     * Reserved System Panel IDs
     */
    @JvmField val SYSTEM_PANEL_IDS: Set<String> = Stream.of(SYSTEM_BAR_IDS)
        .flatMap { obj: Set<String> -> obj.stream() }
        .collect(Collectors.toUnmodifiableSet())
}
