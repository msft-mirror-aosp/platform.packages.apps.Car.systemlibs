/*
 * Copyright (C) 2024 The Android Open Source Project
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

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * A pool for managing {@link Panel} instances.
 *
 * <p>This class provides a centralized mechanism for creating and retrieving panels, ensuring that
 * only one instance of a panel with a given ID exists at a time. It uses a
 * {@link PanelCreatorDelegate} to handle the actual creation of panel instances.
 */
public class PanelPool {
    private static final PanelPool sInstance = new PanelPool();

    private final HashMap<String, Panel> mPanels = new HashMap<>();
    private PanelCreatorDelegate mDelegate;

    /**
     * An instance of the {@link PanelPool}.
     */
    public static PanelPool getInstance() {
        return sInstance;
    }

    /**
     * A delegate interface for creating {@link Panel} instances.
     */
    public interface PanelCreatorDelegate {
        /**
         * Creates a panel object.
         * @param id given identifier for the panel.
         * @return the panel object.
         */
        Panel createPanel(String id);
    }


    private PanelPool() {}

    /**
     * Sets the {@link PanelCreatorDelegate} to be used for creating panel instances.
     *
     * @param delegate The delegate to set.
     */
    public void setDelegate(PanelCreatorDelegate delegate) {
        mDelegate = delegate;
    }

    /**
     * Clears all panels from the pool.
     */
    public void clearPanels() {
        mPanels.clear();
    }

    /**
     * Retrieves a panel with the given ID.
     *
     * <p>If a panel with the given ID already exists in the pool, it is returned. Otherwise, a new
     * panel is created using the {@link PanelCreatorDelegate}, added to the pool, and returned.
     *
     * @param id The ID of the panel to retrieve.
     * @return The panel with the given ID.
     */
    public Panel getPanel(String id) {
        Panel panel = mPanels.get(id);
        if (panel == null) {
            panel = mDelegate.createPanel(id);
            mPanels.put(id, panel);
        }
        return mPanels.get(id);
    }

    /**
     * Retrieves a panel with the given {@link Predicate}.
     *
     * @param predicate A predicate that defines the criteria for selecting a panel.
     * @return The first panel matching the predicate, or null if none is found.
     */
    @Nullable
    public Panel getPanel(Predicate<Panel> predicate) {
        for (Panel panel : mPanels.values()) {
            if (predicate.test(panel)) return panel;
        }
        return null;
    }
}
