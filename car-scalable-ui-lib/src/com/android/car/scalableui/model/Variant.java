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

import static com.android.car.scalableui.Flags.enableVisualData;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.panel.Panel;

/**
 * Represents a specific visual state or variant of a {@code Panel}.
 *
 * <p>This class defines the visual properties of a {@code Panel}, such as its bounds, visibility,
 * layer, and alpha. It also provides methods for creating animations to transition between
 * different variants.
 */
public class Variant {
    private static final String TAG = Variant.class.getSimpleName();
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;
    private final FloatEvaluator mFloatEvaluator = new FloatEvaluator();
    private final RectEvaluator mRectEvaluator = new RectEvaluator();
    private final IntEvaluator mIntEvaluator = new IntEvaluator();
    @NonNull
    protected final String mId;
    private float mAlpha;
    private boolean mIsVisible;
    private int mLayer;
    private int mCornerRadius;
    @NonNull
    private Rect mBounds;
    @NonNull
    private Rect mSafeBounds;
    @NonNull
    private Insets mInsets;
    @NonNull
    private VisualData mVisualData;

    /**
     * Constructs a Variant object with the specified ID. This constructor is package-private and is
     * intended to be used by the VariantBuilder.
     *
     * @param id The ID of the variant.
     */
    Variant(@NonNull String id) {
        this.mId = id;
        // Initialize with default values
        mBounds = new Rect();
        mSafeBounds = new Rect();
        mIsVisible = Visibility.DEFAULT_VISIBILITY;
        mLayer = Layer.DEFAULT_LAYER;
        mAlpha = Alpha.DEFAULT_ALPHA;
        mCornerRadius = Corner.DEFAULT_RADIUS;
        mInsets = Insets.NONE;
        mVisualData = new VisualData.Builder().build();
    }

    /**
     * Constructs a Variant object with the specified ID and base variant. Package private
     * constructor, designed to be invoked by the builder.
     *
     * <p>If a base variant is provided, the new variant inherits its visual properties.
     *
     * @param id   The ID of the variant.
     * @param base The optional base variant to inherit properties from.
     */
    Variant(@NonNull String id, @NonNull Variant base) {
        this(id);
        mBounds = new Rect(base.getBounds());
        mSafeBounds = new Rect(base.getSafeBounds());
        mIsVisible = base.isVisible();
        mLayer = base.getLayer();
        mAlpha = base.getAlpha();
        mCornerRadius = base.getCornerRadius();
        mInsets = base.getInsets();
    }

    protected void setVisualData(@NonNull VisualData visualData) {
        if (!enableVisualData()) {
            return;
        }
        mVisualData = visualData;
    }

    /**
     * Returns the ID of the variant.
     *
     * @return The ID of the variant.
     */
    @NonNull
    public String getId() {
        return mId;
    }

    /**
     * Creates an animator to transition from the current state of a panel to this variant.
     *
     * @param panel        The panel to animate.
     * @param toVariant    The target variant to animate to.
     * @param duration     The duration of the animation.
     * @param interpolator The interpolator to use for the animation.
     * @return An animator that animates the panel's properties to the target variant.
     */
    @Nullable
    public Animator getAnimator(
            @NonNull Panel panel,
            @NonNull Variant toVariant,
            long duration,
            @Nullable Interpolator interpolator) {
        if (toVariant instanceof KeyFrameVariant) {
            return null;
        } else {
            float fromAlpha = panel.getAlpha();
            float toAlpha = toVariant.getAlpha();
            int fromCornerRadius = panel.getCornerRadius();
            int toCornerRadius = toVariant.getCornerRadius();
            Rect fromBounds = new Rect(panel.getBounds());
            Rect toBounds = new Rect(toVariant.getBounds());
            boolean isVisible = panel.isVisible() || toVariant.isVisible();
            int layer = toVariant.getLayer();
            Rect fromInsets = panel.getInsets().toRect();
            Rect toInsets = toVariant.getInsets().toRect();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(duration);
            valueAnimator.setInterpolator(interpolator);
            valueAnimator.addUpdateListener(
                    animator -> {
                        panel.setVisibility(isVisible);
                        panel.setLayer(layer);
                        float fraction = animator.getAnimatedFraction();
                        Rect bounds = mRectEvaluator.evaluate(fraction, fromBounds, toBounds);
                        panel.setBounds(bounds);
                        float alpha = mFloatEvaluator.evaluate(fraction, fromAlpha, toAlpha);
                        panel.setAlpha(alpha);
                        int radius = mIntEvaluator.evaluate(fraction, fromCornerRadius,
                                toCornerRadius);
                        panel.setCornerRadius(radius);
                        Rect insets = mRectEvaluator.evaluate(fraction, fromInsets,
                                toInsets);
                        panel.setInsets(Insets.of(insets));
                        if (DEBUG) {
                            Log.d(TAG, "Panel updated: " + panel);
                        }
                    });
            return valueAnimator;
        }
    }

    /**
     * Returns whether the variant is visible.
     *
     * @return True if the variant is visible, false otherwise.
     */
    public boolean isVisible() {
        if (!enableVisualData()) {
            return mIsVisible;
        }
        return mVisualData.isVisible();
    }

    /**
     * Sets the visibility of the variant.
     *
     * @param isVisible True if the variant should be visible, false otherwise.
     */
    protected void setVisibility(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    /**
     * Returns the layer of the variant.
     *
     * @return The layer of the variant.
     */
    public int getLayer() {
        if (!enableVisualData()) {
            return mLayer;
        }
        return mVisualData.getLayer();
    }

    /**
     * Sets the layer of the variant.
     *
     * @param layer The layer value to set.
     */
    protected void setLayer(int layer) {
        mLayer = layer;
    }

    /**
     * Returns the alpha of the variant.
     *
     * @return The alpha of the variant.
     */
    public float getAlpha() {
        if (!enableVisualData()) {
            return mAlpha;
        }
        return mVisualData.getAlpha();
    }

    /**
     * Sets the alpha of the variant.
     *
     * @param alpha The alpha value to set.
     */
    protected void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * Returns the bounds of the variant.
     *
     * @return The bounds of the variant.
     */
    @NonNull
    public Rect getBounds() {
        if (!enableVisualData()) {
            return mBounds;
        }
        return mVisualData.getBounds();
    }

    /**
     * Sets the bounds of the variant.
     *
     * @param bounds The bounds to set.
     */
    protected void setBounds(@NonNull Rect bounds) {
        mBounds = bounds;
    }

    /**
     * Returns the safe bounds of the variant.
     *
     * @return The safe bounds of the variant.
     */
    @NonNull
    public Rect getSafeBounds() {
        if (!enableVisualData()) {
            return mSafeBounds;
        }
        return mVisualData.getSafeBounds();
    }

    /**
     * Sets the safe bounds of the variant.
     *
     * @param safeBounds The bounds to set.
     */
    protected void setSafeBounds(@NonNull Rect safeBounds) {
        mSafeBounds = safeBounds;
    }

    /**
     * Returns the corner radius of the variant.
     *
     * @return The corner radius of the variant.
     */
    public int getCornerRadius() {
        if (!enableVisualData()) {
            return mCornerRadius;
        }
        return mVisualData.getCornerRadius();
    }

    /**
     * Sets the corner radius of the variant.
     *
     * @param radius The corner radius to set.
     */
    protected void setCornerRadius(int radius) {
        mCornerRadius = radius;
    }

    /**
     * Update the variant with data from an event.
     *
     * @param event the event that was executed.
     */
    protected void updateFromEvent(@Nullable Event event) {
        // no-op
    }

    /**
     * @return {@link Insets}.
     */
    @NonNull
    public Insets getInsets() {
        if (!enableVisualData()) {
            return mInsets;
        }
        return mVisualData.getInsets();
    }

    /**
     * Sets insets.
     * This is essentially the panel's insets.
     */
    protected void setInsets(@NonNull Insets insets) {
        mInsets = insets;
    }

    @Override
    @NonNull
    public String toString() {
        if (!enableVisualData()) {
            return "Variant{"
                    + "mId='" + mId
                    + ", mAlpha=" + mAlpha
                    + ", mIsVisible=" + mIsVisible
                    + ", mLayer=" + mLayer
                    + ", mBounds=" + mBounds
                    + ", mSafeBounds=" + mSafeBounds
                    + ", mCornerRadius=" + mCornerRadius
                    + ", mInsets=" + mInsets
                    + '}';
        }
        return "Variant{"
                + "mId='" + mId
                + ", mVisualData" + mVisualData;
    }

    /** Builder for {@link Variant} objects. */
    public static class Builder {
        @NonNull
        protected String mId;
        @Nullable
        protected Float mAlpha;
        @Nullable
        protected Boolean mIsVisible;
        @Nullable
        protected Integer mLayer;
        @Nullable
        protected Rect mBounds;
        @Nullable
        protected Rect mSafeBounds;
        @Nullable
        protected Integer mCornerRadius;
        @Nullable
        protected Insets mInsets;
        @Nullable
        protected Variant mParent;

        public Builder(@NonNull String id) {
            mId = id;
        }

        /** Sets alpha */
        public Builder setAlpha(@Nullable Float alpha) {
            mAlpha = alpha;
            return this;
        }

        /** Sets visibility */
        public Builder setVisibility(@Nullable Boolean isVisible) {
            mIsVisible = isVisible;
            return this;
        }

        /** Sets layer */
        public Builder setLayer(@Nullable Integer layer) {
            mLayer = layer;
            return this;
        }

        /** Sets bounds */
        public Builder setBounds(@NonNull Rect bounds) {
            mBounds = bounds;
            return this;
        }

        /**
         * Sets safe bounds. This is an area generally not overlapped by display cutouts or insets
         * for display compatibility apps to be drawn within.
         */
        public Builder setSafeBounds(@NonNull Rect safeBounds) {
            mSafeBounds = safeBounds;
            return this;
        }

        /** Sets corner radius */
        public Builder setCornerRadius(@NonNull Integer cornerRadius) {
            mCornerRadius = cornerRadius;
            return this;
        }

        /** Sets insets */
        public Builder setInsets(@NonNull Insets insets) {
            mInsets = insets;
            return this;
        }

        /** Sets parent */
        public Builder setParent(@Nullable Variant parent) {
            mParent = parent;
            return this;
        }

        /** Returns the {@link Variant} instance */
        @NonNull
        public Variant build() {
            if (!enableVisualData()) {
                return buildLegacy();
            }
            Variant variant = new Variant(mId);
            VisualData.Builder visualDataBuilder;
            if (mParent != null) {
                visualDataBuilder = new VisualData.Builder(mParent.mVisualData);
            } else {
                visualDataBuilder = new VisualData.Builder();
            }
            if (mBounds != null) {
                visualDataBuilder.setBounds(mBounds);
            }
            if (mAlpha != null) {
                visualDataBuilder.setAlpha(mAlpha);
            }
            if (mInsets != null) {
                visualDataBuilder.setInsets(mInsets);
            }
            if (mIsVisible != null) {
                visualDataBuilder.setIsVisible(mIsVisible);
            }
            if (mCornerRadius != null) {
                visualDataBuilder.setCornerRadius(mCornerRadius);
            }
            if (mLayer != null) {
                visualDataBuilder.setLayer(mLayer);
            }
            if (mSafeBounds != null) {
                visualDataBuilder.setSafeBounds(mSafeBounds);
            }
            variant.setVisualData(visualDataBuilder.build());
            return variant;
        }

        private Variant buildLegacy() {
            Variant variant;
            if (mParent != null) {
                variant = new Variant(mId, mParent);
            } else {
                variant = new Variant(mId);
            }
            if (mAlpha != null) {
                variant.setAlpha(mAlpha);
            }
            if (mIsVisible != null) {
                variant.setVisibility(mIsVisible);
            }
            if (mLayer != null) {
                variant.setLayer(mLayer);
            }
            if (mBounds != null) {
                variant.setBounds(new Rect(mBounds)); // Defensive copy
            }
            if (mSafeBounds != null) {
                variant.setSafeBounds(new Rect(mSafeBounds)); // Defensive copy
            } else if (mBounds != null) {
                variant.setSafeBounds(new Rect(mBounds)); // Defensive copy
            }
            if (mCornerRadius != null) {
                variant.setCornerRadius(mCornerRadius);
            }
            if (mInsets != null) {
                variant.setInsets(
                        Insets.of(mInsets.left, mInsets.top, mInsets.right, mInsets.bottom));
            }
            return variant;
        }
    }
}
