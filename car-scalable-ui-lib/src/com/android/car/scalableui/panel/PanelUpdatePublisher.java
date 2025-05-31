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
import android.graphics.Rect;

import com.android.car.scalableui.Flags;

/**
 * Interface for components that can publish updates about a panel's state.
 *
 * <p>This publisher is used by ScalableUI constructs, like {@link Panel}, to announce changes
 * that components outside of ScalableUI (e.g., SystemUI windows) might be interested in.
 */
public interface PanelUpdatePublisher {
    /**
     * Posts an update about a panel's bounds.
     *
     * <p>This method is called when a panel's dimensions or position have changed,
     * allowing interested observers to react to the new bounds.
     *
     * @param panelId The unique identifier of the panel whose bounds have changed.
     * @param bounds  A {@link android.graphics.Rect} object representing the new bounds of the
     *                panel.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EXT_PANEL_UPDATES)
    void postBounds(String panelId, Rect bounds);
}
