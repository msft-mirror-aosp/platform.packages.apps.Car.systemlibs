/*
 * Copyright (C) 2024 The Android Open Source Project.
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

import android.graphics.Rect;

/**
 * Represents a rectangular panel that can be displayed on the screen.
 * Panels have properties such as bounds, layer, visibility, and alpha.
 */
public interface Panel {
    /**
     * Gets the bounding rectangle of this panel.
     *
     * @return The bounding rectangle.
     */
    Rect getBounds();

    /**
     * Sets the bounding rectangle of this panel.
     *
     * @param bounds The new bounding rectangle.
     */
    void setBounds(Rect bounds);

    /**
     * Gets the layer of this panel.
     * Panels with higher layer values are drawn on top of panels with lower layer values.
     *
     * @return The layer of this panel.
     */
    int getLayer();

    /**
     * Sets the layer of this panel.
     *
     * @param layer The new layer of this panel.
     */
    void setLayer(int layer);

    /**
     * Gets the x-coordinate of the left edge of this panel.
     *
     * @return The x-coordinate of the left edge.
     */
    int getX1();

    /**
     * Sets the x-coordinate of the left edge of this panel.
     *
     * @param x The new x-coordinate of the left edge.
     */
    void setX1(int x);

    /**
     * Gets the x-coordinate of the right edge of this panel.
     *
     * @return The x-coordinate of the right edge.
     */
    int getX2();

    /**
     * Sets the x-coordinate of the right edge of this panel.
     *
     * @param x The new x-coordinate of the right edge.
     */
    void setX2(int x);

    /**
     * Gets the y-coordinate of the top edge of this panel.
     *
     * @return The y-coordinate of the top edge.
     */
    int getY1();

    /**
     * Sets the y-coordinate of the top edge of this panel.
     *
     * @param y The new y-coordinate of the top edge.
     */
    void setY1(int y);

    /**
     * Gets the y-coordinate of the bottom edge of this panel.
     *
     * @return The y-coordinate of the bottom edge.
     */
    int getY2();

    /**
     * Sets the y-coordinate of the bottom edge of this panel.
     *
     * @param y The new y-coordinate of the bottom edge.
     */
    void setY2(int y);

    /**
     * Gets the alpha value of this panel.
     * The alpha value is a float between 0.0 (fully transparent) and 1.0 (fully opaque).
     *
     * @return The alpha value of this panel.
     */
    float getAlpha();

    /**
     * Sets the alpha value of this panel.
     *
     * @param alpha The new alpha value.
     */
    void setAlpha(float alpha);

    /**
     * Sets the visibility of this panel.
     *
     * @param isVisible True if the panel should be visible, false otherwise.
     */
    void setVisibility(boolean isVisible);

    /**
     * Checks if this panel is visible.
     *
     * @return True if the panel is visible, false otherwise.
     */
    boolean isVisible();

    /**
     * Sets the role of this panel.
     * The role of a panel can be used to identify its purpose or function.
     *
     * @param role The new role of this panel.
     */
    void setRole(int role);

    /**
     * Set the rootTask of the panel to be launch root task.
     * TODO(b/388021504):This api should move to role
     */
    void setLaunchRoot(boolean isLaunchRoot);

    /**
     * Sets the display ID of the panel.
     * TODO(b/388021504):This api should move to role
     */
    void setDisplayId(int displayId);

    /**
     * Initializes the panel.
     */
    void init();

    /**
     * Reset the panel.
     */
    void reset();
}
