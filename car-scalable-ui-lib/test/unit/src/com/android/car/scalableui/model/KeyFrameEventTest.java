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
package com.android.car.scalableui.model;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KeyFrameEventTest {

    @Test
    public void keyFrameEvent_toString_returnsCorrectFormat() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.5f)
                .addToken("key1", "value1")
                .build();
        String expectedString =
                "KeyFrameEvent{mId=testEvent, mTokens={key1=value1}, mFraction=0.5}";
        assertThat(keyFrameEvent.toString()).isEqualTo(expectedString);
    }

    @Test
    public void keyFrameEvent_getFraction_returnsCorrectValue() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.75f).build();
        assertThat(keyFrameEvent.getFraction()).isEqualTo(0.75f);
    }

    @Test
    public void keyFrameEventBuilder_addToken_addsTokenCorrectly() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.25f)
                .addToken("tokenKey", "tokenValue")
                .build();

        assertThat(keyFrameEvent.getTokens()).containsExactly("tokenKey", "tokenValue");
    }

    @Test
    public void keyFrameEventBuilder_addTokensFromString_addsMultipleTokensCorrectly() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.25f)
                .addTokensFromString("key1=value1;key2=value2")
                .build();

        assertThat(keyFrameEvent.getTokens()).containsExactly("key1", "value1", "key2", "value2");
    }

    @Test
    public void keyFrameEventBuilder_addTokensFromString_withEmptyString_addsNoToken() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.25f)
                .addTokensFromString("")
                .build();

        assertThat(keyFrameEvent.getTokens()).isEmpty();
    }

    @Test
    public void keyFrameEventBuilder_addTokensFromString_withNullString_addsNoToken() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.25f)
                .addTokensFromString(null)
                .build();

        assertThat(keyFrameEvent.getTokens()).isEmpty();
    }

    @Test
    public void keyFrameEventBuilder_addTokensFromString_withMalformedString_onlyAddsValidToken() {
        KeyFrameEvent keyFrameEvent = new KeyFrameEvent.Builder("testEvent", 0.25f)
                .addTokensFromString("key1=value1;key2;key3=value3")
                .build();

        assertThat(keyFrameEvent.getTokens()).containsExactly("key1", "value1", "key3", "value3");
    }

    @Test(expected = IllegalStateException.class)
    public void keyFrameEventBuilder_build_withInvalidFraction_throwsException() {
        new KeyFrameEvent.Builder("testEvent", -0.1f).build();
    }

    @Test(expected = IllegalStateException.class)
    public void keyFrameEventBuilder_build_withInvalidFraction_aboveOne_throwsException() {
        new KeyFrameEvent.Builder("testEvent", 1.1f).build();
    }
}
