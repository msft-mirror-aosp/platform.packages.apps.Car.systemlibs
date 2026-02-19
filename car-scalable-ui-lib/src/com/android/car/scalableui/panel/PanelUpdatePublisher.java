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
package com.android.car.scalableui.panel;

import android.annotation.FlaggedApi;
import android.graphics.Insets;
import android.graphics.Rect;
import android.view.Gravity;

import com.android.car.scalableui.Flags;
import com.android.car.scalableui.model.Corner;
import com.android.car.scalableui.model.PanelControllerMetadata;

/**
 * Interface for components that can publish updates about a panel's state.
 *
 * <p>This publisher is used by ScalableUI constructs, like {@link Panel}, to announce changes that
 * components outside of ScalableUI (e.g., SystemUI windows) might be interested in.
 */
public interface PanelUpdatePublisher {
    /**
     * Posts an update about a panel's bounds.
     *
     * @param panelId The unique identifier of the panel whose bounds has changed.
     * @param bounds A {@link android.graphics.Rect} object representing the new bounds of the
     *     panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postBounds(String panelId, Rect bounds);

    /**
     * Posts an update about a panel's alpha.
     *
     * @param panelId The unique identifier of the panel whose alpha has changed.
     * @param alpha Represent the new alpha of the panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postAlpha(String panelId, float alpha);

    /**
     * Posts an update about a panel's corner radius.
     *
     * @param panelId The unique identifier of the panel whose corner radius has changed.
     * @param radius Represents the new corner radius of the panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postCornerRadius(String panelId, Corner radius);

    /**
     * Posts an update about a panel's visibility.
     *
     * @param panelId The unique identifier of the panel whose visibility have changed.
     * @param isVisible Represents the new visibility of the panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postVisibility(String panelId, boolean isVisible);

    /**
     * Posts an update about a panel's insets.
     *
     * @param panelId The unique identifier of the panel whose insets has changed.
     * @param insets A {@link Insets} object representing the new insets of the panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postInsets(String panelId, Insets insets);

    /**
     * Posts an update about a panel's controller metadata.
     *
     * @param panelId The unique identifier of the panel whose controller metadata has changed.
     * @param metadata A {@link PanelControllerMetadata} object representing the new controller
     *     metadata of the panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postControllerMetadata(String panelId, PanelControllerMetadata metadata);

    /**
     * Posts an update for the panel's gravity.
     *
     * @param panelId The unique identifier of the panel.
     * @param gravity The new gravity value. See {@link Gravity}.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postGravity(String panelId, int gravity);
}
