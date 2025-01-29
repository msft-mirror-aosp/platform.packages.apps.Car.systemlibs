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

package com.android.car.scalableui.manager;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class EventTest {

    private static final String TEST_EVENT_ID = "TEST_EVENT_ID";
    private static final String TEST_PAYLOAD = "TEST_PAYLOAD";

    @Test
    public void testEventCreation_withoutPayload() {
        Event event = new Event(TEST_EVENT_ID);
        assertThat(event.getId()).isEqualTo(TEST_EVENT_ID);
        assertThat(event.getPayload()).isNull();
    }

    @Test
    public void testEventCreation_withPayload() {
        Object payload = new Object();
        Event event = new Event(TEST_EVENT_ID, payload);
        assertThat(event.getId()).isEqualTo(TEST_EVENT_ID);
        assertThat(event.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testGetPayload() {
        Event event = new Event(TEST_EVENT_ID, TEST_PAYLOAD);
        assertThat(event.getPayload()).isEqualTo(TEST_PAYLOAD);
    }
}
