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

import android.annotation.NonNull;
import android.content.ComponentName;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.android.car.scalableui.model.PanelControllerMetadata;

import java.util.Set;

/**
 * An interface defining the contract for a controller that manages a task panel.
 *
 * <p>Implementations of this interface are responsible for providing information about the panel's
 * content, such as the default component to display and a set of persistent activities that should
 * always be considered. They also handle registration of a {@link TaskPanelHandler} to communicate
 * panel-related events.
 */
public interface TaskPanelController {
    /** Initializes the panel controller. */
    void init();

    /** Destroy the panel controller. */
    void destroy();

    /**
     * Gets the default component to be displayed on the panel.
     *
     * @return An {@link Intent} that can be used to start the default component, or {@code null} if
     *     no default component is defined.
     */
    @Nullable
    Intent getDefaultComponent();

    /**
     * Gets the set of persistent activities that should always be considered for display on the
     * panel, regardless of the current context or other dynamic factors.
     *
     * @return A {@link Set} of {@link ComponentName} representing the persistent activities. This
     *     set may be empty if there are no persistent activities for this panel.
     */
    @NonNull
    Set<ComponentName> getPersistentActivities();

    /**
     * Registers a {@link TaskPanelHandler} to receive callbacks for events related to this task
     * panel, such as changes in the set of available or relevant applications.
     *
     * @param taskPanelHandler The {@code TaskPanelHandler} to register. Implementations should
     *     store this handler and notify it when relevant events occur.
     */
    void registerTaskPanelHandler(TaskPanelHandler taskPanelHandler);

    /**
     * Checks if this panel controller handles the given component. This method can be used to
     * determine if a specific activity or service is relevant to and should be displayed or managed
     * by this panel.
     *
     * @param componentName The {@link ComponentName} to check.
     * @return {@code true} if this panel controller handles the component, {@code false} otherwise.
     */
    boolean handles(ComponentName componentName);

    /**
     * Factory for creating an implementation of the TaskPanelController.
     *
     * @param <T> the type of the TaskPanelController implementation
     */
    interface Factory<T extends TaskPanelController> {
        /**
         * Create an instance of the TaskPanelController implementation using the provided {@link
         * PanelControllerMetadata} for a given panelId.
         */
        T create(String panelId, PanelControllerMetadata metadata);
    }
}
