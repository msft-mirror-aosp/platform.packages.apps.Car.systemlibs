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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.model.PanelType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
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
         *
         * @param id given id for the panel.
         * @param type given {@link PanelType} for the panel.
         * @return the panel object.
         */
        Panel createPanel(String id, @PanelType int type);
    }


    private PanelPool() {
    }

    /**
     * Sets the {@link PanelCreatorDelegate} to be used for creating panel instances.
     *
     * @param delegate The delegate to set.
     */
    public void setDelegate(@NonNull PanelCreatorDelegate delegate) {
        mDelegate = delegate;
    }

    /**
     * Clears all panels from the pool.
     */
    public void clearPanels() {
        mPanels.forEach((id, panel) -> panel.destroy());
        mPanels.clear();
    }

    /**
     * Retrieves a panel with the given ID.
     *
     * <p>If a panel with the given ID already exists in the pool, it is returned. Otherwise,
     * return {@code null}
     *
     * @param id The ID of the panel to retrieve.
     * @return The panel with the given ID.
     */
    @NonNull
    public Panel getOrCreatePanel(@NonNull String id, @PanelType int type) {
        Panel panel = mPanels.get(id);
        if (panel == null) {
            panel = mDelegate.createPanel(id, type);
            mPanels.put(id, panel);
        }
        return panel;
    }

    /**
     * Retrieves a panel with the given ID.
     *
     * <p>If a panel with the given ID already exists in the pool, it is returned. Otherwise,
     * return {@code null}
     *
     * @param id The ID of the panel to retrieve.
     * @return The panel with the given ID.
     */
    @Nullable
    public Panel getPanel(@NonNull String id) {
        return mPanels.get(id);
    }

    /**
     * Retrieves a panel with the given {@link Predicate}.
     *
     * @param predicate A predicate that defines the criteria for selecting a panel.
     * @return The first panel matching the predicate, or null if none is found.
     */
    @Nullable
    public Panel getPanel(@NonNull Predicate<Panel> predicate) {
        for (Panel panel : mPanels.values()) {
            if (predicate.test(panel)) return panel;
        }
        return null;
    }

    /**
     * Retrieves a panels with the given {@link Predicate}.
     *
     * @param predicate A predicate that defines the criteria for selecting panels.
     * @return The panels matching the predicate, or empty list if none are found.
     */
    @NonNull
    public List<Panel> getPanels(@NonNull Predicate<Panel> predicate) {
        List<Panel> panels = new ArrayList<>();
        for (Panel panel : mPanels.values()) {
            if (predicate.test(panel)) {
                panels.add(panel);
            }
        }
        return panels;
    }

    /**
     * Executes a given {@link Consumer} on all the panels.
     */
    public void forEach(@NonNull Consumer<Panel> consumer) {
        mPanels.forEach((id, panel) -> consumer.accept(panel));
    }
}
