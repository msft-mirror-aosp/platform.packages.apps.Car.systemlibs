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

package com.android.car.scalableui.metrics;

import com.android.car.scalableui.model.PanelState;
import com.android.internal.jank.Cuj;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A central registry that holds all CUJ definitions for the application. TODO(b/431796250):
 * Evaluate a plan to create XML based definitions.
 */
public class CujRegistry {

    // Common predicates for panel states to make definitions clean and readable.
    public static final Predicate<PanelState> IS_VISIBLE =
            s -> s != null && s.getCurrentVariant() != null && s.getCurrentVariant().isVisible();
    public static final Predicate<PanelState> IS_NOT_VISIBLE = IS_VISIBLE.negate();

    private static final String PANEL_APP_GRID = "panel_app_grid";
    private static final String APP_PANEL = "app_panel";
    private static final String PANEL_CALM_MODE = "panel_calm_mode";

    private static final List<CujDefinition> DEFINITIONS =
            List.of(
                    // CUJ: Close App Grid to Home via Home event.
                    new CujDefinition(
                            Cuj.CUJ_LAUNCHER_APP_CLOSE_TO_HOME,
                            PANEL_APP_GRID,
                            event -> "_System_OnHomeEvent".equals(event.getId()),
                            Map.of(PANEL_APP_GRID, IS_VISIBLE),
                            Map.of(PANEL_APP_GRID, IS_NOT_VISIBLE)),

                    // CUJ: Open App Panel from Home state.
                    new CujDefinition(
                            Cuj.CUJ_LAUNCHER_OPEN_ALL_APPS,
                            PANEL_APP_GRID,
                            event -> "_System_TaskOpenEvent".equals(event.getId()),
                            Map.of(PANEL_APP_GRID, IS_NOT_VISIBLE),
                            Map.of(PANEL_APP_GRID, IS_VISIBLE)),

                    // CUJ: Launch an app from the app grid or dock/pinned app.
                    new CujDefinition(
                            Cuj.CUJ_LAUNCHER_APP_LAUNCH_FROM_ICON,
                            APP_PANEL,
                            event -> "_System_TaskOpenEvent".equals(event.getId()),
                            Map.of(APP_PANEL, IS_NOT_VISIBLE),
                            Map.of(APP_PANEL, IS_VISIBLE, PANEL_APP_GRID, IS_NOT_VISIBLE)),
                    // CUJ: Close All Apps panel by pressing AppGrid button in the taskbar
                    new CujDefinition(
                            Cuj.CUJ_LAUNCHER_TASKBAR_ALL_APPS_CLOSE_BACK,
                            PANEL_APP_GRID,
                            event -> "close_app_grid".equals(event.getId()),
                            Map.of(PANEL_APP_GRID, IS_VISIBLE),
                            Map.of(PANEL_APP_GRID, IS_NOT_VISIBLE, APP_PANEL, IS_NOT_VISIBLE)),
                    // CUJ: Show Calm mode
                    new CujDefinition(
                            Cuj.CUJ_KEYGUARD_TRANSITION_GONE_TO_AOD,
                            PANEL_CALM_MODE,
                            event -> "_System_TaskOpenEvent".equals(event.getId()),
                            Map.of(PANEL_CALM_MODE, IS_NOT_VISIBLE),
                            Map.of(PANEL_CALM_MODE, IS_VISIBLE))

                    // *** ADD MORE CUJ DEFINITIONS HERE AS NEEDED ***
                    );

    public static List<CujDefinition> getDefinitions() {
        return DEFINITIONS;
    }
}
