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

import android.animation.FloatEvaluator;
import android.animation.RectEvaluator;
import android.graphics.Insets;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Variant} that interpolates between different variants based on a fraction value.
 *
 * <p>This class defines a series of keyframes, each associated with a {@link Variant} and a frame
 * position. The {@link #setFraction(float)} method sets the current fraction, which determines the
 * interpolation between keyframes.</p>
 *
 * <p>KeyFrameVariant allows for smooth transitions between different panel states by interpolating
 * properties such as bounds, visibility, and alpha.
 */
public class KeyFrameVariant extends Variant {
    private static final String TAG = KeyFrameVariant.class.getSimpleName();
    private float mFraction;
    private final RectEvaluator mRectEvaluator = new RectEvaluator();
    private final FloatEvaluator mFloatEvaluator = new FloatEvaluator();

    /** Represents a single keyframe in a {@link KeyFrameVariant}. */
    public static class KeyFrame {
        int mFramePosition;
        Variant mVariant;

        /**
         * Constructor for KeyFrame.
         *
         * @param framePosition The position of the keyframe (0-100).
         * @param variant       The variant associated with this keyframe.
         */
        public KeyFrame(int framePosition, @NonNull Variant variant) {
            mFramePosition = framePosition;
            mVariant = variant;
        }

        /** Builder for {@link KeyFrameVariant} objects. */
        public static class Builder {
            private final int mFramePosition;
            private final Variant mVariant;

            public Builder(int framePosition, @NonNull Variant variant) {
                mVariant = variant;
                mFramePosition = framePosition;
            }

            /** Returns the {@link KeyFrameVariant} instance */
            public KeyFrame build() {
                if (mVariant == null) {
                    throw new IllegalStateException("Variant must be set for KeyFrame");
                }
                return new KeyFrame(mFramePosition, mVariant);
            }
        }

        @Override
        public String toString() {
            return "KeyFrame{"
                    + "mFramePosition=" + mFramePosition
                    + ", mVariant=" + mVariant
                    + '}';
        }
    }

    private final List<KeyFrame> mKeyFrames = new ArrayList<>();

    /**
     * Constructor for KeyFrameVariant. Package-private, use the Builder.
     *
     * @param id   The ID of this variant.
     * @param base The base variant to inherit properties from.
     */
    KeyFrameVariant(@NonNull String id, @NonNull Variant base) {
        super(id, base);
    }

    /**
     * Constructor for KeyFrameVariant. Package-private, use the Builder.
     *
     * @param id The ID of this variant.
     */
    KeyFrameVariant(@NonNull String id) {
        super(id);
    }

    /**
     * Adds a keyframe to this variant.
     *
     * @param keyFrame The keyframe to add.
     */
    public void addKeyFrame(@NonNull KeyFrame keyFrame) {
        mKeyFrames.add(keyFrame);
        mKeyFrames.sort(Comparator.comparingInt(o -> o.mFramePosition));
    }

    /**
     * Sets the current fraction for interpolation.
     *
     * @param fraction The fraction value (between 0 and 1).
     */
    public void setFraction(float fraction) {
        mFraction = fraction;
    }

    /**
     * Returns the interpolated bounds for the current fraction.
     *
     * @return The interpolated bounds.
     */
    @Override
    @NonNull
    public Rect getBounds() {
        return getBounds(mFraction);
    }

    /**
     * Returns the interpolated visibility for the current fraction.
     *
     * @return The interpolated visibility.
     */
    @Override
    public boolean isVisible() {
        return getVisibility(mFraction);
    }

    /**
     * Returns the interpolated alpha for the current fraction.
     *
     * @return The interpolated alpha.
     */
    @Override
    public float getAlpha() {
        return getAlpha(mFraction);
    }

    @Override
    public void updateFromEvent(@Nullable Event event) {
        if (event instanceof KeyFrameEvent keyFrameEvent) {
            setFraction(keyFrameEvent.getFraction());
        }
    }

    /**
     * Finds the keyframe immediately before the given fraction.
     *
     * <p>This method iterates through the list of keyframes and returns the keyframe that is
     * immediately before the given fraction. If the fraction is smaller than the first keyframe's
     * position, the first keyframe is returned. If the fraction is larger than the last keyframe's
     * position, the last keyframe is returned.
     *
     * @param fraction The fraction value (between 0 and 1).
     * @return The keyframe before the given fraction, or null if there are no keyframes.
     */
    @Nullable
    private KeyFrame before(float fraction) {
        if (mKeyFrames.isEmpty()) return null;
        KeyFrame current = mKeyFrames.getFirst();
        for (KeyFrame keyFrame : mKeyFrames) {
            if (keyFrame.mFramePosition >= fraction * 100) {
                return current;
            }
            current = keyFrame;
        }
        return current;
    }

    /**
     * Returns the key frame after the fraction
     *
     * @param fraction The fraction value (between 0 and 1).
     * @return The key frame
     */
    @Nullable
    private KeyFrame after(float fraction) {
        if (mKeyFrames.isEmpty()) return null;
        for (KeyFrame keyFrame : mKeyFrames) {
            if (keyFrame.mFramePosition >= fraction * 100) {
                return keyFrame;
            }
        }
        return mKeyFrames.get(mKeyFrames.size() - 1);
    }

    /**
     * Calculates the fraction between two keyframes based on the given overall fraction.
     *
     * <p>This method takes two frame positions (representing keyframes) and an overall fraction
     * value (between 0 and 1). It calculates the fraction between the two keyframes, effectively
     * normalizing the overall fraction to the range between the keyframes.
     *
     * <p>For example, if framePosition1 is 0, framePosition2 is 80, and fraction is 0.5, the
     * result will be 0.75, because 0.5 lies at 62.5% of the range between 0 and 80.
     *
     * @param framePosition1 The position of the first keyframe (0-100).
     * @param framePosition2 The position of the second keyframe (0-100).
     * @param fraction       The overall fraction value (between 0 and 1).
     * @return The fraction between the two keyframes.
     */
    private float getKeyFrameFraction(int framePosition1, int framePosition2, float fraction) {
        fraction = fraction * 100;
        if (fraction <= framePosition1) return 0;
        if (fraction >= framePosition2) return 1;
        return (fraction - framePosition1) / (framePosition2 - framePosition1);
    }

    /**
     * Returns the interpolated bounds for the given fraction.
     *
     * @param fraction The fraction value (between 0 and 1).
     * @return The interpolated bounds.
     */
    @NonNull
    private Rect getBounds(float fraction) {
        if (mKeyFrames.isEmpty()) return new Rect();
        KeyFrame keyFrame1 = before(fraction);
        Rect bounds1 = Objects.requireNonNull(keyFrame1).mVariant.getBounds();
        KeyFrame keyFrame2 = after(fraction);
        Rect bounds2 = Objects.requireNonNull(keyFrame2).mVariant.getBounds();
        float fractionInBetween =
                getKeyFrameFraction(
                        keyFrame1.mFramePosition, keyFrame2.mFramePosition, fraction);
        Rect rect = mRectEvaluator.evaluate(fractionInBetween, bounds1, bounds2);
        return new Rect(rect.left, rect.top, rect.right, rect.bottom);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KeyFrameVariant{ mid=")
                .append(mId)
                .append(", mFraction=")
                .append(mFraction);
        for (KeyFrame keyFrame : mKeyFrames) {
            sb.append(", keyFrame=").append(keyFrame);
        }
        sb.append(", layer=").append(getLayer());
        sb.append(", visibility=").append(isVisible());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns the interpolated visibility for the given fraction.
     *
     * @param fraction The fraction value (between 0 and 1).
     * @return The interpolated visibility.
     */
    private boolean getVisibility(float fraction) {
        if (mKeyFrames.isEmpty()) return false;
        KeyFrame keyFrame1 = before(fraction);
        boolean isVisible1 = Objects.requireNonNull(keyFrame1).mVariant.isVisible();
        KeyFrame keyFrame2 = after(fraction);
        boolean isVisible2 = Objects.requireNonNull(keyFrame2).mVariant.isVisible();
        return isVisible1 || isVisible2;
    }

    /**
     * Returns the interpolated alpha for the given fraction.
     *
     * @param fraction The fraction value (between 0 and 1).
     * @return The interpolated alpha.
     */
    private float getAlpha(float fraction) {
        if (mKeyFrames.isEmpty()) return 1;
        KeyFrame keyFrame1 = before(fraction);
        float alpha1 = (Objects.requireNonNull(keyFrame1).mVariant.getAlpha());
        KeyFrame keyFrame2 = after(fraction);
        float alpha2 = (Objects.requireNonNull(keyFrame2).mVariant.getAlpha());
        return mFloatEvaluator.evaluate(fraction, alpha1, alpha2);
    }

    /** Builder for {@link KeyFrameVariant} objects. */
    public static class Builder extends Variant.Builder {
        private List<KeyFrame> mKeyFrames = new ArrayList<>();

        public Builder(@NonNull String id) {
            super(id);
        }

        /** Adds keyframe */
        public Builder addKeyFrame(@NonNull KeyFrame keyFrame) {
            mKeyFrames.add(keyFrame);
            return this;
        }

        /** Sets keyframes */
        public Builder setKeyFrames(@NonNull List<KeyFrame> keyFrames) {
            mKeyFrames = new ArrayList<>(keyFrames); // Defensive copy
            return this;
        }

        /** Returns the {@link KeyFrameVariant} instance */
        @Override
        @NonNull
        public KeyFrameVariant build() {
            KeyFrameVariant variant;
            if (mParent != null) {
                variant = new KeyFrameVariant(mId, mParent);
            } else {
                variant = new KeyFrameVariant(mId);
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
            if (mCornerRadius != null) {
                variant.setCornerRadius(mCornerRadius);
            }
            variant.setSafeBounds(new Rect((mSafeBounds != null) ? mSafeBounds : mBounds));
            if (mInsets != null) {
                variant.setInsets(
                        Insets.of(mInsets.left, mInsets.top, mInsets.right, mInsets.bottom));
            }

            // Sort keyframes by frame position after adding them all.
            mKeyFrames.sort(Comparator.comparingInt(o -> o.mFramePosition));
            for (KeyFrame keyFrame : mKeyFrames) {
                variant.addKeyFrame(keyFrame);
            }
            return variant;
        }
    }
}
