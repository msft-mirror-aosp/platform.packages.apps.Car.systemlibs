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

package com.android.car.scalableui.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the layer of a {@code Panel}. This class provides methods for creating a Layer object
 * from an XML definition and retrieving the layer value.
 */
public class Layer {

    public static final int DEFAULT_LAYER = 0;

    private final int mLayer;

    /**
     * Constructs a Layer object. Package-private; use the Builder.
     *
     * @param layer The layer value.
     */
    Layer(int layer) {
        mLayer = layer;
    }

    /**
     * Returns the layer value.
     *
     * @return The layer value.
     */
    public int getLayer() {
        return mLayer;
    }

    /** Builder for {@link Layer} objects. */
    public static class Builder {
        @Nullable private Integer mLayer;

        public Builder() {}

        /** Sets layer */
        public Builder setLayer(int layer) {
            mLayer = layer;
            return this;
        }

        /** Returns the {@link Layer} instance */
        @NonNull
        public Layer build() {
            // Use default if not explicitly set
            int layerValue = (mLayer != null) ? mLayer : DEFAULT_LAYER;
            return new Layer(layerValue);
        }
    }
}
