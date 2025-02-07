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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A view based implementation of a {@link Panel}.
 */
public class PanelView extends FrameLayout implements Panel {
    private static final String DRAWABLE_RESOURCE_TYPE = "drawable";
    private static final String LAYOUT_RESOURCE_TYPE = "layout";

    private int mLayer = -1;
    private int mRole = 0;

    private int mImageHolderLayoutId;
    private int mImageId;

    public PanelView(@NonNull Context context) {
        super(context);
    }

    public PanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PanelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public Rect getBounds() {
        return new Rect(getLeft(), getTop(), getRight(), getBottom());
    }

    @Override
    public void setBounds(Rect bounds) {
        LayoutParams params = new LayoutParams(bounds.width(), bounds.height());
        params.topMargin = bounds.top;
        params.leftMargin = bounds.left;
        setLayoutParams(params);

        // Update left, right, top and bottom to make sure these values are correctly set before a
        // full round of re-layout.
        setLeft(bounds.left);
        setRight(bounds.right);
        setTop(bounds.top);
        setBottom(bounds.bottom);
    }

    public int getLayer() {
        return mLayer;
    }

    /**
     * Sets the z-order of the panel.
     * @param layer the required z-order.
     */
    public void setLayer(int layer) {
        if (this.mLayer == layer) return;
        this.mLayer = layer;
        ViewGroup parent = (ViewGroup) getParent();
        boolean isSorted = false;
        // Make sure all the sibling PanelViews have the correct relative z-order.
        while (!isSorted) {
            isSorted = true;
            int lastLayer = Integer.MAX_VALUE;
            for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                View child = parent.getChildAt(i);
                if (!(child instanceof PanelView panelView)) continue;
                if (panelView.getLayer() > lastLayer) {
                    panelView.bringToFront();
                    isSorted = false;
                    break;
                }
                lastLayer = panelView.getLayer();
            }
        }
    }

    @Override
    public int getX1() {
        return getLeft();
    }

    @Override
    public int getX2() {
        return getRight();
    }

    @Override
    public int getY1() {
        return getTop();
    }

    @Override
    public int getY2() {
        return getBottom();
    }

    @Override
    public void setX1(int x) {
        setBounds(new Rect(x, getTop(), getRight(), getBottom()));
    }

    @Override
    public void setX2(int x) {
        setBounds(new Rect(getLeft(), getTop(), x, getBottom()));
    }

    @Override
    public void setY1(int y) {
        setBounds(new Rect(getLeft(), y, getRight(), getBottom()));
    }

    @Override
    public void setY2(int y) {
        setBounds(new Rect(getLeft(), getTop(), getRight(), y));
    }

    /**
     * Checks if this panel is visible.
     *
     * @return True if the panel is visible, false otherwise.
     */
    public boolean isVisible() {
        return super.getVisibility() == VISIBLE;
    }

    /**
     * Sets the visibility of this panel.
     *
     * @param isVisible True if the panel should be visible, false otherwise.
     */
    public void setVisibility(boolean isVisible) {
        super.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }

    /**
     * Gets the alpha value of this panel.
     * The alpha value is a float between 0.0 (fully transparent) and 1.0 (fully opaque).
     *
     * @return The alpha value of this panel.
     */
    public float getAlpha() {
        return super.getAlpha();
    }

    /**
     * Sets the alpha value of this panel.
     *
     * @param alpha The new alpha value.
     */
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
    }

    public void setImageHolderLayoutId(int imageHolderLayoutId) {
        this.mImageHolderLayoutId = imageHolderLayoutId;
    }

    public void setImageId(int imageId) {
        this.mImageId = imageId;
    }

    /**
     * Sets the role of this panel.
     * The role of a panel can be used to identify its purpose or function.
     *
     * @param role The new role of this panel.
     */
    public void setRole(int role) {
        if (this.mRole == role) return;
        this.mRole = role;
        if (isDrawableRole(role)) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(mImageHolderLayoutId, this, true);
            ImageView imageView = view.findViewById(mImageId);
            if (imageView != null) {
                imageView.setImageResource(role);
            }
        } else if (isLayoutRole(role)) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            inflater.inflate(role, this, true);
        } else {
            throw new UnsupportedOperationException("Specified role is not supported");
        }
    }

    @Override
    public void setDisplayId(int displayId) {
        // no-op
    }

    @Override
    public void init() {
        // no-op
    }

    @Override
    public void reset() {
        // no-op
    }

    private boolean isDrawableRole(int role) {
        String resourceTypeName = getContext().getResources().getResourceTypeName(role);
        return DRAWABLE_RESOURCE_TYPE.equals(resourceTypeName);
    }

    private boolean isLayoutRole(int role) {
        String resourceTypeName = getContext().getResources().getResourceTypeName(role);
        return LAYOUT_RESOURCE_TYPE.equals(resourceTypeName);
    }
}
