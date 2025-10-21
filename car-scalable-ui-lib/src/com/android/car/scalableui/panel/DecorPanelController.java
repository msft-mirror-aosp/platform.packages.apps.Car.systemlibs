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

import android.view.View;

import com.android.car.scalableui.model.PanelControllerMetadata;

/**
 * Interface for DecorPanel controller
 */
public interface DecorPanelController {
    /**
     * Returns a {@link View} to show in the decor panel.
     */
    View getView();

    /**
     * Refreshes the theme.
     */
    void refreshTheme();

    /**
     * Factory for creating an implementation of the DecorPanelController.
     * @param <T> the type of the DecorPanelController implementation
     */
    interface Factory<T extends DecorPanelController> {
        /**
         * Create an instance of the DecorPanelController implementation using the provided
         * {@link PanelControllerMetadata} for a given panelId.
         */
        T create(String panelId, PanelControllerMetadata metadata);
    }
}
