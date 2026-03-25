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
package com.android.car.scalableui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PanelControllerMetadataTest {

    @Test
    public void builder_addConfiguration_multipleValuesYieldsList() {
        PanelControllerMetadata.Builder builder = PanelControllerMetadata.builder("testId");
        builder.addConfiguration("ControllerName", "com.test.Controller");

        builder.addConfiguration("TEST_KEY", "value1");
        builder.addConfiguration("TEST_KEY", "value2");
        builder.addConfiguration("TEST_KEY", "value3");

        PanelControllerMetadata metadata = builder.build();

        List<String> list = metadata.getListConfiguration("TEST_KEY");
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("value1", list.get(0));
        assertEquals("value2", list.get(1));
        assertEquals("value3", list.get(2));
    }

    @Test
    public void builder_addConfiguration_singleValueYieldsStringButListIsStillAccessible() {
        PanelControllerMetadata.Builder builder = PanelControllerMetadata.builder("testId");
        builder.addConfiguration("ControllerName", "com.test.Controller");
        builder.addConfiguration("TEST_KEY", "value1");

        PanelControllerMetadata metadata = builder.build();

        assertEquals("value1", metadata.getStringConfiguration("TEST_KEY"));

        List<String> list = metadata.getListConfiguration("TEST_KEY");
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("value1", list.get(0));
    }
}
