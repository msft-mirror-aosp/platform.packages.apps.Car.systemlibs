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
    private static final String TEST_TOKEN_ID = "TEST_TOKEN_ID";
    private static final String TEST_TOKEN_VALUE = "TEST_TOKEN_VALUE";

    @Test
    public void testEventCreation_withoutTokens() {
        Event event = new Event(TEST_EVENT_ID);
        assertThat(event.getId()).isEqualTo(TEST_EVENT_ID);
        assertThat(event.getTokens()).isEmpty();
    }

    @Test
    public void testEventCreation_withTokens() {
        Event event = new Event(TEST_EVENT_ID);
        event.addToken(TEST_TOKEN_ID, TEST_TOKEN_VALUE);
        assertThat(event.getId()).isEqualTo(TEST_EVENT_ID);
        assertThat(event.getTokens()).isNotEmpty();
        assertThat(event.getTokens().get(TEST_TOKEN_ID)).isEqualTo(TEST_TOKEN_VALUE);
    }

    @Test
    public void testMatching_noIdMatch_isNotMatch() {
        Event event = new Event(TEST_EVENT_ID);
        Event event2 = new Event("OTHER_EVENT_ID");
        assertThat(event.isMatch(event2)).isFalse();
    }

    @Test
    public void testMatching_noTokenMatch_isNotMatch() {
        Event event = new Event(TEST_EVENT_ID);
        Event event2 = new Event(TEST_EVENT_ID);
        event2.addToken(TEST_TOKEN_ID, TEST_TOKEN_VALUE);
        assertThat(event.isMatch(event2)).isFalse();
    }

    @Test
    public void testMatching_isMatch() {
        Event event = new Event(TEST_EVENT_ID);
        event.addToken(TEST_TOKEN_ID, TEST_TOKEN_VALUE);
        Event event2 = new Event(TEST_EVENT_ID);
        event2.addToken(TEST_TOKEN_ID, TEST_TOKEN_VALUE);
        assertThat(event.isMatch(event2)).isTrue();
    }
}
