/*
 * Copyright (C) 2025 The Android Open Source Project.
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

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.manager.Event;
import com.android.car.scalableui.unit.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class PanelStateTest {
    private static final String TEST_PANEL_ID = "TEST_PANEL_ID";
    private static final String VARIANT1 = "variant1";
    private static final String VARIANT2 = "variant2";
    private static final String TEST_EVENT = "TEST_EVENT";

    private Context mContext;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testPanelStateCreation() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, new Role(1));
        assertThat(panelState.getId()).isEqualTo(TEST_PANEL_ID);
        assertThat(panelState.getRole().getValue()).isEqualTo(1);
    }


    @Test
    public void testLoadFromXmlResource() throws XmlPullParserException, IOException {
        PanelState panelState = PanelState.load(mContext, R.xml.panel_test);

        assertThat(panelState.getId()).isEqualTo("panel_id");
        assertThat(panelState.getRole().getValue()).isEqualTo(
                R.string.default_config);
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo(VARIANT1);
    }


    @Test
    public void testAddVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, new Role(1));
        Variant variant = new Variant(VARIANT1, null);
        panelState.addVariant(variant);
        assertThat(panelState.getVariant(VARIANT1)).isEqualTo(variant);
    }

    @Test
    public void testAddTransition() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, new Role(1));
        Variant variant1 = new Variant(VARIANT1, null);
        Variant variant2 = new Variant(VARIANT2, null);
        Transition transition = new Transition(variant1, variant2, TEST_EVENT, null, 0, null);
        panelState.addTransition(transition);
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);
        panelState.setVariant(variant1.getId());

        assertThat(panelState.getTransition(new Event(TEST_EVENT))).isEqualTo(transition);
    }


    @Test
    public void testSetVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, new Role(1));
        Variant variant1 = new Variant(VARIANT1, null);
        Variant variant2 = new Variant(VARIANT2, null);
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);

        panelState.setVariant(VARIANT2);
        assertThat(panelState.getCurrentVariant()).isEqualTo(variant2);
    }

    @Test
    public void testResetVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, new Role(1));
        Variant variant1 = new Variant(VARIANT1, null);
        Variant variant2 = new Variant(VARIANT2, null);
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);
        panelState.setDefaultVariant(VARIANT1);

        panelState.setVariant(VARIANT2);
        panelState.resetVariant();
        assertThat(panelState.getCurrentVariant()).isEqualTo(variant1);
    }
}
