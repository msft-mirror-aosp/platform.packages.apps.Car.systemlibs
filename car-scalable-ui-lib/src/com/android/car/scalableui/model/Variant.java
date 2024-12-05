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

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.Interpolator;

import com.android.car.scalableui.panel.Panel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Represents a specific visual state or variant of a {@code Panel}.
 *
 * <p>This class defines the visual properties of a {@code Panel}, such as its bounds,
 * visibility, layer, and alpha. It also provides methods for creating animations
 * to transition between different variants.
 */
public class Variant {
    static final String VARIANT_TAG = "Variant";
    private static final String ID_ATTRIBUTE = "id";
    private static final String PARENT_ATTRIBUTE = "parent";

    private final FloatEvaluator mFloatEvaluator = new FloatEvaluator();
    private final RectEvaluator mRectEvaluator = new RectEvaluator();

    private final String mId;
    private float mAlpha;
    private boolean mIsVisible;
    private int mLayer;
    private Rect mBounds;

    /**
     * Constructs a Variant object with the specified ID and optional base variant.
     *
     * <p>If a base variant is provided, the new variant inherits its visual properties.
     *
     * @param id The ID of the variant.
     * @param base The optional base variant to inherit properties from.
     */
    public Variant(String id, Variant base) {
        this.mId = id;
        if (base != null) {
            mBounds = base.getBounds();
            mIsVisible = base.isVisible();
            mLayer = base.getLayer();
            mAlpha = base.getAlpha();
        } else {
            mBounds = new Rect();
            mIsVisible = Visibility.DEFAULT_VISIBILITY;
            mLayer = Layer.DEFAULT_LAYER;
            mAlpha = Alpha.DEFAULT_ALPHA;
        }
    }

    /**
     * Returns the ID of the variant.
     *
     * @return The ID of the variant.
     */
    public String getId() {
        return mId;
    }

    /**
     * Creates an animator to transition from the current state of a panel to this variant.
     *
     * @param panel The panel to animate.
     * @param toVariant The target variant to animate to.
     * @param duration The duration of the animation.
     * @param interpolator The interpolator to use for the animation.
     * @return An animator that animates the panel's properties to the target variant.
     */
    public Animator getAnimator(Panel panel, Variant toVariant, long duration,
            Interpolator interpolator) {
        if (toVariant instanceof KeyFrameVariant) {
            return null;
        } else {
            float fromAlpha = panel.getAlpha();
            float toAlpha = toVariant.getAlpha();
            Rect fromBounds = panel.getBounds();
            Rect toBounds = toVariant.getBounds();
            boolean isVisible = panel.isVisible() || toVariant.isVisible();
            int layer = toVariant.getLayer();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(duration);
            valueAnimator.setInterpolator(interpolator);
            valueAnimator.addUpdateListener(animator -> {
                panel.setVisibility(isVisible);
                panel.setLayer(layer);
                float fraction = animator.getAnimatedFraction();
                Rect bounds = mRectEvaluator.evaluate(fraction, fromBounds, toBounds);
                panel.setBounds(bounds);
                float alpha = mFloatEvaluator.evaluate(fraction, fromAlpha, toAlpha);
                panel.setAlpha(alpha);
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
        return mIsVisible;
    }

    /**
     * Sets the visibility of the variant.
     *
     * @param isVisible True if the variant should be visible, false otherwise.
     */
    public void setVisibility(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    /**
     * Returns the layer of the variant.
     *
     * @return The layer of the variant.
     */
    public int getLayer() {
        return mLayer;
    }

    /**
     * Returns the alpha of the variant.
     *
     * @return The alpha of the variant.
     */
    public float getAlpha() {
        return mAlpha;
    }

    /**
     * Sets the alpha of the variant.
     *
     * @param alpha The alpha value to set.
     */
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * Sets the layer of the variant.
     *
     * @param layer The layer value to set.
     */
    public void setLayer(int layer) {
        mLayer = layer;
    }

    /**
     * Returns the bounds of the variant.
     *
     * @return The bounds of the variant.
     */
    public Rect getBounds() {
        return mBounds;
    }

    /**
     * Sets the bounds of the variant.
     *
     * @param bounds The bounds to set.
     */
    public void setBounds(Rect bounds) {
        mBounds = bounds;
    }

    /**
     * Creates a Variant object from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Variant" and extracts its attributes
     * and child elements to create a Variant object.
     *
     * @param context The application context.
     * @param panelState The panel data associated with this variant.
     * @param parser The XML parser.
     * @return A Variant object with the parsed properties.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    static Variant create(Context context, PanelState panelState, XmlPullParser parser) throws
            XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String parentStr = attrs.getAttributeValue(null, PARENT_ATTRIBUTE);
        Variant parent = panelState.getVariant(parentStr);
        Variant result = new Variant(id, parent);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case Visibility.VISIBILITY_TAG:
                    result.setVisibility(Visibility.create(parser).isVisible());
                    break;
                case Alpha.ALPHA_TAG:
                    result.setAlpha(Alpha.create(parser).getAlpha());
                    break;
                case Layer.LAYER_TAG:
                    result.setLayer(Layer.create(parser).getLayer());
                    break;
                case Bounds.BOUNDS_TAG:
                    result.setBounds(Bounds.create(context, parser).getRect());
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
                    break;
            }
        }
        return result;
    }
}
