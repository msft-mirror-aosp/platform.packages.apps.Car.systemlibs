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

import static com.android.car.scalableui.model.KeyFrameVariant.KEY_FRAME_VARIANT_TAG;
import static com.android.car.scalableui.model.Transition.TRANSITION_TAG;
import static com.android.car.scalableui.model.Variant.VARIANT_TAG;

import android.animation.Animator;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.android.car.scalableui.manager.Event;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of a panel in the Scalable UI system.
 *
 * <p>A PanelState defines the different variants (layouts) that a panel can have, as well as the
 * transitions between those variants. It also manages the current variant and any running
 * animations.
 */
public class PanelState {
    private static final String PANEL_TAG = "Panel";
    private static final String ID_TAG = "id";
    private static final String DEFAULT_VARIANT_ATTRIBUTE = "defaultVariant";
    private static final String ROLE_ATTRIBUTE = "role";
    private static final String TRANSITIONS_TAG = "Transitions";
    private static final String DEFAULT_DURATION_ATTRIBUTE = "defaultDuration";
    private static final String DEFAULT_INTERPOLATOR_ATTRIBUTE = "defaultInterpolator";
    private static final int DEFAULT_TRANSITION_DURATION = 300;

    /**
     * Loads a PanelState from an XML resource.
     *
     * @param context    The context to use.
     * @param resourceId The ID of the XML resource.
     * @return The loaded PanelState.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    public static PanelState load(Context context, int resourceId) throws XmlPullParserException,
            IOException {
        XmlResourceParser parser = context.getResources().getXml(resourceId);
        while (true) {
            if (parser.next() == XmlPullParser.START_TAG) break;
        }
        return PanelState.create(context, parser);
    }

    private final String mId;
    private final Role mRole;
    private final List<Variant> mVariants = new ArrayList<>();
    private final List<Transition> mTransitions = new ArrayList<>();

    private Animator mRunningAnimator;
    private Variant mCurrentVariant;

    /**
     * Constructor for PanelState.
     *
     * @param id The ID of the panel.
     * @param role The role of the panel.
     */
    public PanelState(String id, Role role) {
        mId = id;
        mRole = role;
    }

    /**
     * Returns the ID of the panel.
     *
     * @return The ID of the panel.
     */
    public String getId() {
        return mId;
    }

    /**
     * Adds a variant to the panel.
     *
     * @param variant The variant to add.
     */
    public void addVariant(Variant variant) {
        mVariants.add(variant);
    }

    /**
     * Adds a transition to the panel.
     *
     * @param transition The transition to add.
     */
    public void addTransition(Transition transition) {
        mTransitions.add(transition);
    }

    /**
     * Returns the current variant of the panel.
     *
     * @return The current variant of the panel.
     */
    public Variant getCurrentVariant() {
        if (mCurrentVariant == null) {
            mCurrentVariant = mVariants.get(0);
        }
        return mCurrentVariant;
    }

    /**
     * Returns the variant with the given ID.
     *
     * @param id The ID of the variant.
     * @return The variant with the given ID, or null if not found.
     */
    public Variant getVariant(String id) {
        for (Variant variant : mVariants) {
            if (variant.getId().equals(id)) {
                return variant;
            }
        }
        return null;
    }

    /**
     * Sets the current variant to the variant with the given ID.
     *
     * @param id The ID of the variant.
     */
    public void setVariant(String id) {
        setVariant(id, null);
    }

    /**
     * Sets the current variant to the variant with the given ID and payload.
     *
     * @param id      The ID of the variant.
     * @param payload The payload to pass to the variant.
     */
    public void setVariant(String id, Object payload) {
        for (Variant variant : mVariants) {
            if (variant.getId().equals(id)) {
                mCurrentVariant = variant;
                if (mCurrentVariant instanceof KeyFrameVariant) {
                    ((KeyFrameVariant) mCurrentVariant).setPayload(payload);
                }
                return;
            }
        }
    }

    /**
     * Returns the role of the panel.
     *
     * @return The role of the panel.
     */
    public Role getRole() {
        return mRole;
    }

    /**
     * Returns true if the panel is currently animating.
     *
     * @return True if the panel is currently animating.
     */
    public boolean isAnimating() {
        return mRunningAnimator != null && mRunningAnimator.isRunning();
    }

    /**
     * Should be called when an animation starts.
     *
     * @param animator The animator that started.
     */
    public void onAnimationStart(Animator animator) {
        if (mRunningAnimator != null) {
            mRunningAnimator.pause();
            mRunningAnimator.removeAllListeners();
        }
        mRunningAnimator = animator;
    }

    /**
     * Should be Called when an animation ends.
     */
    public void onAnimationEnd() {
        mRunningAnimator = null;
    }

    /**
     * Returns the transition for the given event.
     *
     * @param event The event.
     * @return The transition for the given event, or null if not found.
     */
    public Transition getTransition(Event event) {
        // If both onEvent and fromVariant matches
        Transition result = getTransition(event.getId(), getCurrentVariant().getId());
        if (result != null) {
            return result;
        }
        // If only onEvent matches
        return getTransition(event.getId());
    }

    /**
     * Returns a transition that matches the given event ID and "from" variant.
     *
     * @param eventId The ID of the event to find a transition for.
     * @param fromVariant The ID of the variant the transition should start from.
     * @return The matching transition, or null if no such transition is found.
     */
    private Transition getTransition(String eventId, String fromVariant) {
        for (Transition transition : mTransitions) {
            if (eventId.equals(transition.getOnEvent())
                    && transition.getFromVariant() != null
                    && transition.getFromVariant().getId().equals(fromVariant)) {
                return transition;
            }
        }
        return null;
    }

    /**
     * Returns a transition that matches the given event ID and has no "from" variant specified.
     *
     * @param eventId The ID of the event to find a transition for.
     * @return The matching transition, or null if no such transition is found.
     */
    private Transition getTransition(String eventId) {
        for (Transition transition : mTransitions) {
            if (eventId.equals(transition.getOnEvent())
                    && transition.getFromVariant() == null) {
                return transition;
            }
        }
        return null;
    }

    /**
     * Creates a PanelState object from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Panel" and extracts its attributes
     * and child elements to create a Panel object.
     *
     * @param context The application context.
     * @param parser The XML parser.
     * @return A PanelState object with the parsed properties.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    private static PanelState create(Context context, XmlPullParser parser) throws
            XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, PANEL_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_TAG);
        String defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE);
        int roleValue = attrs.getAttributeResourceValue(null, ROLE_ATTRIBUTE, 0);
        PanelState result = new PanelState(id, new Role(roleValue));
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VARIANT_TAG:
                    Variant variant = Variant.create(context, result, parser);
                    result.addVariant(variant);
                    break;
                case KEY_FRAME_VARIANT_TAG:
                    KeyFrameVariant keyFrameVariant = KeyFrameVariant.create(result, parser);
                    result.addVariant(keyFrameVariant);
                    break;
                case TRANSITIONS_TAG:
                    List<Transition> transitions = readTransitions(context, result, parser);
                    for (Transition transition : transitions) {
                        result.addTransition(transition);
                    }
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
                    break;
            }
        }
        result.setVariant(defaultVariant);
        return result;
    }

    /**
     * Reads a list of Transition objects from an XML parser.
     *
     * <p>This method parses an XML element with the tag "Transitions" and extracts its attributes
     * and child transition elements.
     *
     * @param context The application context.
     * @param parser The XML parser.
     * @return A list of Transition objects with the parsed properties.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs while reading the XML.
     */
    private static List<Transition> readTransitions(Context context, PanelState panelState,
                                                    XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITIONS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int duration = attrs.getAttributeIntValue(null,
                DEFAULT_DURATION_ATTRIBUTE, DEFAULT_TRANSITION_DURATION);
        int interpolatorRef = attrs.getAttributeResourceValue(null,
                DEFAULT_INTERPOLATOR_ATTRIBUTE, 0);
        Interpolator interpolator = interpolatorRef == 0 ? null :
                AnimationUtils.loadInterpolator(context, interpolatorRef);

        List<Transition> result = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (parser.getName().equals(TRANSITION_TAG)) {
                result.add(Transition.create(context, panelState, duration, interpolator, parser));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }
        return result;
    }
}
