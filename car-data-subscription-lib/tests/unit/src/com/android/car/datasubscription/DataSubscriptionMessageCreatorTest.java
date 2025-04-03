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

package com.android.car.datasubscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class DataSubscriptionMessageCreatorTest {
    private static final int TEST_EXPIRATION_TIME = 4;
    private static final String TEST_EXPIRATION_DATE = "test date";

    private Context mContext;
    private DataSubscriptionMessageCreator mMessageCreator;
    private Map<Integer, DataSubscriptionConfig> mConfigData;
    @Mock
    private DataSubscriptionExpirationDate mDataSubscriptionExpirationDate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(InstrumentationRegistry.getInstrumentation().getContext());
        mMessageCreator = new DataSubscriptionMessageCreator(mContext);
        mMessageCreator.setDataSubscriptionExpirationDate(mDataSubscriptionExpirationDate);
        mConfigData = DataSubscriptionConfigParser.loadConfig(mContext);
        when(mDataSubscriptionExpirationDate.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);
        when(mDataSubscriptionExpirationDate.getExpirationDate()).thenReturn(TEST_EXPIRATION_DATE);

    }

    @Test
    public void getProactiveMessageForStatus_statusInactive_validMessage() {
        String message = mMessageCreator.getProactiveMessageForStatus(0);

        assertNotNull(message);
        assertEquals(message, mConfigData.get(0).getProactiveMessage());
    }

    @Test
    public void getProactiveMessageForStatus_statusTrial_noMessage() {
        String message = mMessageCreator.getProactiveMessageForStatus(1);

        assertNull(message);
    }

    @Test
    public void getProactiveMessageForStatus_statusActive_noMessage() {
        String message = mMessageCreator.getProactiveMessageForStatus(2);

        assertNull(message);
    }

    @Test
    public void getProactiveMessageForStatus_statusExpiringSoonUseDate_validMessage() {
        String message = mMessageCreator.getProactiveMessageForStatus(3);

        assertNotNull(message);
        assertEquals(message, String.format(mConfigData.get(3).getProactiveMessage(),
                TEST_EXPIRATION_DATE));
    }

    @Test
    public void getProactiveMessageForStatus_statusExpiringSoonAndNotUseDate_validMessage() {
        String message = mMessageCreator.getProactiveMessageForStatus(4);

        assertNotNull(message);
        assertEquals(message, String.format(mConfigData.get(4).getProactiveMessage(),
                TEST_EXPIRATION_TIME));
    }

    @Test
    public void getReactiveMessageForStatus_statusInactive_validMessage() {
        String message = mMessageCreator.getReactiveMessageForStatus(0, "App 1");

        assertNotNull(message);
        assertEquals(message, String.format(mConfigData.get(0)
                    .getReactiveMessage(), "App 1"));

    }

    @Test
    public void getReactiveMessageForStatus_statusTrial_noMessage() {
        String message = mMessageCreator.getReactiveMessageForStatus(1,
                "App 1");

        assertNull(message);
    }

    @Test
    public void getReactiveMessageForStatus_statusActive_noMessage() {
        String message = mMessageCreator.getReactiveMessageForStatus(2,
                "App 1");

        assertNull(message);
    }

    @Test
    public void getReactiveMessageForStatus_statusExpiringSoonAndUseDate_validMessage() {
        String message = mMessageCreator.getReactiveMessageForStatus(3, "App 1");

        assertNotNull(message);
        assertEquals(message, String.format(mConfigData.get(3).getReactiveMessage(),
                "App 1", TEST_EXPIRATION_DATE));
    }

    @Test
    public void getReactiveMessageForStatus_statusExpiringSoonAndNotUseDate_validMessage() {
        String message = mMessageCreator.getReactiveMessageForStatus(4, "App 1");

        assertNotNull(message);
        assertEquals(message, String.format(mConfigData.get(4).getReactiveMessage(),
                "App 1", TEST_EXPIRATION_TIME));
    }

    @Test
    public void getUxrPrompt_uxrRequired_validMessage() {
        String message = mMessageCreator.getUxrPrompt(true);

        assertNotNull(message);
        assertEquals(message, mContext.getResources()
                .getString(R.string.data_subscription_uxr_prompt));
    }

    @Test
    public void getUxrPrompt_uxrNotRequired_validMessage() {
        String message = mMessageCreator.getUxrPrompt(false);

        assertNotNull(message);
        assertEquals(message, mContext.getResources()
                .getString(R.string.data_subscription_non_uxr_prompt));
    }
}
