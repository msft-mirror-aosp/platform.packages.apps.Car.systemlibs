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

import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.BAR_Z_ORDER_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.HIDE_FOR_KEYBOARD_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.SYSTEM_BAR_PANEL_BOTTOM_ID;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.SYSTEM_BAR_PANEL_LEFT_ID;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.SYSTEM_BAR_PANEL_RIGHT_ID;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.SYSTEM_BAR_PANEL_TOP_ID;
import static com.android.car.scalableui.loader.xml.SystemBarTagXmlParser.TYPE_ATTRIBUTE;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.Resources;
import android.platform.test.annotations.RequiresFlagsEnabled;
import android.platform.test.flag.junit.CheckFlagsRule;
import android.platform.test.flag.junit.DeviceFlagsValueProvider;
import android.util.DisplayMetrics;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.scalableui.Flags;
import com.android.car.scalableui.loader.xml.XmlModelLoader;
import com.android.car.scalableui.unit.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PanelStateTest {
    @Rule
    public final CheckFlagsRule mCheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule();
    private static final String TEST_PANEL_ID = "TEST_PANEL_ID";
    private static final String VARIANT1 = "variant1";
    private static final String VARIANT2 = "variant2";
    private static final Event TEST_EVENT = new Event("TEST_EVENT");
    private static final Role DEFAULT_ROLE = new Role.Builder().setLayoutId(1).build();

    private Context mContext;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testPanelStateCreation() {
        Role defaultRole = new Role.Builder().setIsDefault(true).build();
        PanelState panelState = new PanelState(TEST_PANEL_ID, defaultRole);
        assertThat(panelState.getId()).isEqualTo(TEST_PANEL_ID);
        assertTrue(panelState.getRole().isDefault());
    }

    @Test
    public void testLoadFromXmlResource_panel() {
        XmlModelLoader loader = new XmlModelLoader(mContext);
        PanelState panelState = loader.createPanelState(R.xml.panel_test);

        assertThat(panelState.getId()).isEqualTo("panel_id");
        assertTrue(panelState.getRole().isDefault());
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo("@" + R.id.variant1);
        Variant variant2 = panelState.getVariant("@" + R.id.variant2);
        assertThat(variant2.getLayer()).isEqualTo(100);
        assertThat(variant2.getAlpha()).isEqualTo(0.8f);
        assertThat(variant2.getInsets()).isNotNull();
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    public void testLoadFromXmlResource_systemBar_top() {
        XmlModelLoader loader = new XmlModelLoader(mContext);
        PanelState panelState = loader.createPanelState(R.xml.system_bar_top_test);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        assertThat(panelState.getId()).isEqualTo(SYSTEM_BAR_PANEL_TOP_ID);
        assertThat(panelState.getRole()).isNull();
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                BAR_Z_ORDER_ATTRIBUTE)).isEqualTo(11);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                TYPE_ATTRIBUTE)).isEqualTo(3);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getBoolean(
                HIDE_FOR_KEYBOARD_ATTRIBUTE)).isFalse();
        assertThat(panelState.getCurrentVariant().getBounds().top).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().left).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().right).isEqualTo(
                displayMetrics.widthPixels);
        assertThat(panelState.getCurrentVariant().getBounds().bottom).isEqualTo(50);
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo("@" + R.id.variant1);
        Variant variant2 = panelState.getVariant("@" + R.id.variant2);
        assertThat(variant2.getAlpha()).isEqualTo(1.0f);
        assertThat(variant2.getInsets()).isNotNull();
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    public void testLoadFromXmlResource_systemBar_bottom() {
        XmlModelLoader loader = new XmlModelLoader(mContext);
        PanelState panelState = loader.createPanelState(R.xml.system_bar_bottom_test);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        assertThat(panelState.getId()).isEqualTo(SYSTEM_BAR_PANEL_BOTTOM_ID);
        assertThat(panelState.getRole()).isNull();
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                BAR_Z_ORDER_ATTRIBUTE)).isEqualTo(0);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                TYPE_ATTRIBUTE)).isEqualTo(0);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getBoolean(
                HIDE_FOR_KEYBOARD_ATTRIBUTE)).isTrue();
        assertThat(panelState.getCurrentVariant().getBounds().top).isEqualTo(
                displayMetrics.heightPixels - 50);
        assertThat(panelState.getCurrentVariant().getBounds().left).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().right).isEqualTo(
                displayMetrics.widthPixels);
        assertThat(panelState.getCurrentVariant().getBounds().bottom).isEqualTo(
                displayMetrics.heightPixels);
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo("@" + R.id.variant1);
        Variant variant2 = panelState.getVariant("@" + R.id.variant2);
        assertThat(variant2.getAlpha()).isEqualTo(1.0f);
        assertThat(variant2.getInsets()).isNotNull();
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    public void testLoadFromXmlResource_systemBar_left() {
        XmlModelLoader loader = new XmlModelLoader(mContext);
        PanelState panelState = loader.createPanelState(R.xml.system_bar_left_test);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        assertThat(panelState.getId()).isEqualTo(SYSTEM_BAR_PANEL_LEFT_ID);
        assertThat(panelState.getRole()).isNull();
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                BAR_Z_ORDER_ATTRIBUTE)).isEqualTo(10);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                TYPE_ATTRIBUTE)).isEqualTo(1);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getBoolean(
                HIDE_FOR_KEYBOARD_ATTRIBUTE)).isFalse();
        assertThat(panelState.getCurrentVariant().getBounds().top).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().left).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().right).isEqualTo(50);
        assertThat(panelState.getCurrentVariant().getBounds().bottom).isEqualTo(
                displayMetrics.heightPixels);
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo("@" + R.id.variant1);
        Variant variant2 = panelState.getVariant("@" + R.id.variant2);
        assertThat(variant2.getAlpha()).isEqualTo(1.0f);
        assertThat(variant2.getInsets()).isNotNull();
    }

    @Test
    @RequiresFlagsEnabled(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    public void testLoadFromXmlResource_systemBar_right() {
        XmlModelLoader loader = new XmlModelLoader(mContext);
        PanelState panelState = loader.createPanelState(R.xml.system_bar_right_test);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        assertThat(panelState.getId()).isEqualTo(SYSTEM_BAR_PANEL_RIGHT_ID);
        assertThat(panelState.getRole()).isNull();
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                BAR_Z_ORDER_ATTRIBUTE)).isEqualTo(2);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getInt(
                TYPE_ATTRIBUTE)).isEqualTo(2);
        assertThat(panelState.getPanelControllerMetadata().getConfigurations().getBoolean(
                HIDE_FOR_KEYBOARD_ATTRIBUTE)).isFalse();
        assertThat(panelState.getCurrentVariant().getBounds().top).isEqualTo(0);
        assertThat(panelState.getCurrentVariant().getBounds().left).isEqualTo(
                displayMetrics.widthPixels - 50);
        assertThat(panelState.getCurrentVariant().getBounds().right).isEqualTo(
                displayMetrics.widthPixels);
        assertThat(panelState.getCurrentVariant().getBounds().bottom).isEqualTo(
                displayMetrics.heightPixels);
        assertThat(panelState.getCurrentVariant().getId()).isEqualTo("@" + R.id.variant1);
        Variant variant2 = panelState.getVariant("@" + R.id.variant2);
        assertThat(variant2.getAlpha()).isEqualTo(1.0f);
        assertThat(variant2.getInsets()).isNotNull();
    }

    @Test
    public void testAddVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, DEFAULT_ROLE);
        Variant variant = new Variant(VARIANT1, "");
        panelState.addVariant(variant);
        assertThat(panelState.getVariant(VARIANT1)).isEqualTo(variant);
    }

    @Test
    public void testAddTransition() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, DEFAULT_ROLE);
        Variant variant1 = new Variant(VARIANT1, "");
        Variant variant2 = new Variant(VARIANT2, "");
        Transition transition = new Transition(variant1, variant2, TEST_EVENT, null, 0, 0, null);
        panelState.addTransition(transition);
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);
        panelState.setVariant(variant1.getId());

        assertThat(panelState.getTransition(TEST_EVENT)).isEqualTo(transition);
    }

    @Test
    public void testSetVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, DEFAULT_ROLE);
        Variant variant1 = new Variant(VARIANT1, "");
        Variant variant2 = new Variant(VARIANT2, "");
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);

        panelState.setVariant(VARIANT2);
        assertThat(panelState.getCurrentVariant()).isEqualTo(variant2);
    }

    @Test
    public void testResetVariant() {
        PanelState panelState = new PanelState(TEST_PANEL_ID, DEFAULT_ROLE);
        Variant variant1 = new Variant(VARIANT1, "");
        Variant variant2 = new Variant(VARIANT2, "");
        panelState.addVariant(variant1);
        panelState.addVariant(variant2);
        panelState.setDefaultVariant(VARIANT1);

        panelState.setVariant(VARIANT2);
        panelState.resetVariant();
        assertThat(panelState.getCurrentVariant()).isEqualTo(variant1);
    }
}
