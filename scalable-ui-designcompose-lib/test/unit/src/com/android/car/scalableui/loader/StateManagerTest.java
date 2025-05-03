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

package com.android.car.scalableui.designcompose;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import android.content.Context;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.car.scalableui.manager.StateManager;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Variant;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StateManagerTest {
  private static final String TAG = StateManagerTest.class.getSimpleName();

  private Context mContext;

  @Before
  public void setUp() {
    StateManager.clearStates();
    mContext = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testLoadDcfFile() {
    Log.i(TAG, "testLoadDcfFile");
    String docId = "1OqNt1MCCc4E8KXS0OVcxS";
    String filename = "ScalableUiTest_" + docId + ".dcf";
    URL dcfFile = this.getClass().getClassLoader().getResource("src/resources/" + filename);
    assertWithMessage("DCF file " + filename).that(dcfFile).isNotNull();

    String dcfPath = dcfFile.getPath();
    try {
      InputStream is = dcfFile.openStream();

      // Check that panels can be loaded from the document
      PanelStateDocLoader loader = new PanelStateDocLoader(mContext);
      List<PanelState> states = loader.loadPanelStates(is, docId);
      assertWithMessage("Failed to load panel states").that(states).isNotNull();
      assertThat(states.size()).isEqualTo(11);

      // Check that the PanelApp exists, and it has the appropriate variants
      Optional<PanelState> element =
          states.stream().filter(e -> e.getId().equals("PanelApp")).findFirst();
      Assert.assertTrue(element.isPresent());
      PanelState panelApp = element.get();
      assertThat(panelApp).isNotNull();
      assertThat(panelApp.getVariant("#panel-app=open1")).isNotNull();
      assertThat(panelApp.getVariant("#panel-app=open2")).isNotNull();
      assertThat(panelApp.getVariant("#panel-app=open3")).isNotNull();
      assertThat(panelApp.getVariant("#panel-app=full")).isNotNull();

      // Check that the current variant is the open3 variant
      Variant open3 = panelApp.getVariant("#panel-app=open3");
      assertThat(open3).isEqualTo(panelApp.getCurrentVariant());

      // Check the bounds of one of the variants
      float density = mContext.getResources().getDisplayMetrics().density;
      float width = open3.getBounds().width();
      Assert.assertEquals(500.0f, width / density, 1f);
      Assert.assertEquals(690.0f, open3.getBounds().height() / density, 1f);
      Assert.assertEquals(150.0f, open3.getBounds().left / density, 1f);
      Assert.assertEquals(0.0f, open3.getBounds().top / density, 1f);

      // Check other properties of the variant
      Assert.assertTrue(open3.isVisible());
      Assert.assertEquals(1.0f, open3.getAlpha(), .01f);
      Assert.assertEquals(0, open3.getLayer());
    } catch (DocLoadException | IOException e) {
      Assert.fail("Failed to load doc: " + e);
    }
  }
}
