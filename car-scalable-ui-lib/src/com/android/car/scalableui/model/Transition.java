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
import android.animation.AnimatorInflater;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

import com.android.car.scalableui.panel.Panel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Represents a transition between two {@link Variant}s in the Scalable UI system.
 *
 * <p>A Transition defines the animation that should be used to transition from one variant to
 * another in response to an event. It can optionally specify a specific "from" variant, a "to"
 * variant, an event trigger, and a custom animator.
 */
public class Transition {
    public static final String TRANSITION_TAG = "Transition";
    private static final String FROM_VARIANT_ATTRIBUTE = "fromVariant";
    private static final String TO_VARIANT_ATTRIBUTE = "toVariant";
    private static final String ON_EVENT_ATTRIBUTE = "onEvent";
    private static final String ANIMATOR_ATTRIBUTE = "animator";
    private static final long DEFAULT_DURATION = 300;

    private final Variant mFromVariant;
    @NonNull
    private final Variant mToVariant;
    private final String mOnEvent;
    private final Animator mAnimator;
    private final Interpolator mDefaultInterpolator;
    private final long mDefaultDuration;

    /**
     * Constructor for Transition.
     *
     * @param fromVariant The variant to transition from (can be null).
     * @param toVariant The variant to transition to.
     * @param onEvent The event that triggers the transition.
     * @param animator A custom animator to use for the transition (can be null).
     * @param defaultDuration The default duration of the transition.
     * @param defaultInterpolator The default interpolator to use for the transition.
     */
    public Transition(Variant fromVariant, @NonNull Variant toVariant, String onEvent,
            Animator animator, long defaultDuration, Interpolator defaultInterpolator) {
        mFromVariant = fromVariant;
        mToVariant = toVariant;
        mAnimator = animator;
        mOnEvent = onEvent;
        mDefaultDuration = defaultDuration >= 0 ? defaultDuration : DEFAULT_DURATION;
        mDefaultInterpolator = defaultInterpolator != null
                ? defaultInterpolator
                : new AccelerateDecelerateInterpolator();
    }

    /**
     * Returns the "from" variant of the transition.
     *
     * @return The "from" variant, or null if not specified.
     */
    public Variant getFromVariant() {
        return mFromVariant;
    }

    /**
     * Returns the "to" variant of the transition.
     *
     * @return The "to" variant.
     */
    public @NonNull Variant getToVariant() {
        return mToVariant;
    }

    /**
     * Returns the animator for the transition.
     *
     * <p>If a custom animator was provided, it is cloned and returned. Otherwise, a default
     * animator will be created to transition from "from" variant to "to" variant with the default
     * duration and interpolator.
     *
     * @param panel The panel to apply the animation to.
     * @param fromVariant The actual "from" variant of the transition.
     * @return The animator for the transition.
     */
    public Animator getAnimator(Panel panel, @NonNull Variant fromVariant) {
        if (fromVariant.getId().equals(mToVariant.getId())) {
            return null;
        }

        if (mAnimator != null) {
            Animator animator = this.mAnimator.clone();
            animator.setTarget(panel);
            return animator;
        }
        return fromVariant.getAnimator(panel, mToVariant, mDefaultDuration, mDefaultInterpolator);
    }

    /**
     * Returns the event that triggers the transition.
     *
     * @return The event that triggers the transition.
     */
    public String getOnEvent() {
        return mOnEvent;
    }

    /**
     * Creates a Transition object from an XML parser.
     *
     * @param context The context to use.
     * @param panelState The panel state that this transition belongs to.
     * @param defaultDuration The default duration to use if not specified in the XML.
     * @param defaultInterpolator The default interpolator to use if not specified in the XML.
     * @param parser The XML parser.
     * @return The created Transition object.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    public static Transition create(Context context, PanelState panelState, long defaultDuration,
                                    Interpolator defaultInterpolator, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITION_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String from = attrs.getAttributeValue(null, FROM_VARIANT_ATTRIBUTE);
        String to = attrs.getAttributeValue(null, TO_VARIANT_ATTRIBUTE);
        String onEvent = attrs.getAttributeValue(null, ON_EVENT_ATTRIBUTE);
        int animatorId = attrs.getAttributeResourceValue(null, ANIMATOR_ATTRIBUTE, 0);
        Animator animator = animatorId == 0
                ? null
                : AnimatorInflater.loadAnimator(context, animatorId);
        Variant fromVariant = panelState.getVariant(from);
        Variant toVariant = panelState.getVariant(to);
        Transition result = new Transition(fromVariant, toVariant, onEvent, animator,
                defaultDuration, defaultInterpolator);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, TRANSITION_TAG);
        return result;
    }
}
