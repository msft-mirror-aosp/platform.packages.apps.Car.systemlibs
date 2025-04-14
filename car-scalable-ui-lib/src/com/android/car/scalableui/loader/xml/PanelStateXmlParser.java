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
package com.android.car.scalableui.loader.xml;

import static android.view.Display.DEFAULT_DISPLAY;

import static com.android.car.scalableui.model.Alpha.DEFAULT_ALPHA;
import static com.android.car.scalableui.model.Layer.DEFAULT_LAYER;
import static com.android.car.scalableui.model.Transition.DEFAULT_DURATION;
import static com.android.car.scalableui.model.Visibility.DEFAULT_VISIBILITY;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Insets;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.model.Alpha;
import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.Corner;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Role;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.model.Visibility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A utility class that uses a {@link XmlPullParser} to create a {@link PanelState} object.
 */
public class PanelStateXmlParser {
    private static final String TAG = PanelStateXmlParser.class.getSimpleName();

    // --- Panel Tags ---
    public static final String PANEL_TAG = "Panel";
    public static final String ID_ATTRIBUTE = "id";
    public static final String DEFAULT_VARIANT_ATTRIBUTE = "defaultVariant";
    public static final String ROLE_ATTRIBUTE = "role";
    public static final String DISPLAY_ID = "displayId";
    public static final String DEFAULT_LAYER_ATTRIBUTE = "defaultLayer";

    // --- Transitions Tags ---
    public static final String TRANSITIONS_TAG = "Transitions";
    public static final String DEFAULT_DURATION_ATTRIBUTE = "defaultDuration";
    public static final String DEFAULT_INTERPOLATOR_ATTRIBUTE = "defaultInterpolator";

    // --- Transition Tags ---
    public static final String TRANSITION_TAG = "Transition";
    public static final String FROM_VARIANT_ATTRIBUTE = "fromVariant";
    public static final String TO_VARIANT_ATTRIBUTE = "toVariant";
    public static final String ON_EVENT_ATTRIBUTE = "onEvent";
    public static final String ON_EVENT_TOKENS_ATTRIBUTE = "onEventTokens";
    public static final String ANIMATOR_ATTRIBUTE = "animator";
    public static final String DURATION_ATTRIBUTE = "duration";
    public static final String INTERPOLATOR_ATTRIBUTE = "interpolator";

    // --- Variant Tags ---
    public static final String VARIANT_TAG = "Variant";
    public static final String PARENT_ATTRIBUTE = "parent";

    // --- KeyFrameVariant Tags ---
    static final String KEY_FRAME_VARIANT_TAG = "KeyFrameVariant";
    private static final String KEY_FRAME_TAG = "KeyFrame";
    private static final String FRAME_ATTRIBUTE = "frame";
    private static final String VARIANT_ATTRIBUTE = "variant";

    // --- Visibility Tags ---
    public static final String VISIBILITY_TAG = "Visibility";
    public static final String IS_VISIBLE_ATTRIBUTE = "isVisible";

    // --- Alpha Tags ---
    public static final String ALPHA_TAG = "Alpha";
    public static final String ALPHA_VALUE_ATTRIBUTE = "alpha";

    // --- Layer Tags ---
    public static final String LAYER_TAG = "Layer";
    public static final String LAYER_VALUE_ATTRIBUTE = "layer";

    // --- Bounds Tags ---
    public static final String BOUNDS_TAG = "Bounds";
    public static final String SAFE_BOUNDS_TAG = "SafeBounds";
    public static final String LEFT_ATTRIBUTE = "left";
    public static final String RIGHT_ATTRIBUTE = "right";
    public static final String TOP_ATTRIBUTE = "top";
    public static final String BOTTOM_ATTRIBUTE = "bottom";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";

    // --- Corner Tags ---
    public static final String CORNER_TAG = "Corner";
    public static final String RADIUS_ATTRIBUTE = "radius";

    // --- Insets Tags ---
    public static final String INSETS_TAG = "Insets";

    public static final String DIP = "dip";
    public static final String DP = "dp";
    public static final String PERCENT = "%";
    public static final String PIXEL = "px";

    @NonNull
    static PanelState parse(@NonNull Context context, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Consume any START_DOCUMENT or whitespace events
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT
                || (eventType == XmlPullParser.TEXT && parser.isWhitespace())) {
            eventType = parser.next();
        }
        if (eventType != XmlPullParser.START_TAG || !parser.getName().equals("Panel")) {
            throw new XmlPullParserException("Expected <Panel> tag at the beginning but "
                    + parser.getName());
        }

        parser.require(XmlPullParser.START_TAG, null, PANEL_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID);
        int displayId = (displayIdStr == null) ? DEFAULT_DISPLAY : Integer.parseInt(displayIdStr);
        String defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE);
        int roleValue = attrs.getAttributeResourceValue(null, ROLE_ATTRIBUTE, 0);

        Integer defaultLayer = null;
        if (attrs.getAttributeValue(null, DEFAULT_LAYER_ATTRIBUTE) != null) {
            int resId = attrs.getAttributeResourceValue(null, DEFAULT_LAYER_ATTRIBUTE, 0);
            if (resId != 0) {
                defaultLayer = context.getResources().getInteger(resId);
            } else {
                defaultLayer =
                        attrs.getAttributeIntValue(null, DEFAULT_LAYER_ATTRIBUTE, DEFAULT_LAYER);
            }
        }

        PanelState.Builder builder = new PanelState.Builder(id, new Role(roleValue));
        builder.setDisplayId(displayId);
        builder.setDefaultVariant(defaultVariant);
        PanelState panelState = builder.build();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VARIANT_TAG:
                    panelState.addVariant(
                            parseVariant(context, panelState, defaultLayer, parser));
                    break;
                case KEY_FRAME_VARIANT_TAG:
                    panelState.addVariant(parseKeyFrameVariant(panelState, parser));
                    break;
                case TRANSITIONS_TAG:
                    List<Transition> transitions = parseTransitions(context, panelState, parser);
                    for (Transition transition : transitions) {
                        panelState.addTransition(transition);
                    }
                    break;
                default:
                    XmlPullParserHelper.skip(parser);
            }
        }
        panelState.setVariant(defaultVariant); // Set the initial variant
        return panelState;
    }

    @NonNull
    private static Variant parseKeyFrameVariant(
            @NonNull PanelState panelState,
            @NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, KEY_FRAME_VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String parentStr = attrs.getAttributeValue(null, PARENT_ATTRIBUTE);
        Variant parent = panelState.getVariant(parentStr);
        KeyFrameVariant.Builder builder = new KeyFrameVariant.Builder(id);
        if (parent != null) {
            builder.setParent(parent);
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if (name.equals(KEY_FRAME_TAG)) {
                builder.addKeyFrame(parseKeyFrame(panelState, parser));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }
        return builder.build();
    }

    private static KeyFrameVariant.KeyFrame parseKeyFrame(
            @NonNull PanelState panelState,
            @NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, KEY_FRAME_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int frame = attrs.getAttributeIntValue(null, FRAME_ATTRIBUTE, 0);
        String variant = attrs.getAttributeValue(null, VARIANT_ATTRIBUTE);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, KEY_FRAME_TAG);
        Variant panelVariant = panelState.getVariant(variant);
        if (panelVariant == null) {
            throw new XmlPullParserException("Variant not found: " + variant);
        }
        return new KeyFrameVariant.KeyFrame.Builder(frame, panelVariant).build();
    }

    @NonNull
    private static Variant parseVariant(
            @NonNull Context context,
            @NonNull PanelState panelState,
            @Nullable Integer defaultLayer,
            @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String parentVariantId = attrs.getAttributeValue(null, PARENT_ATTRIBUTE);
        Variant parentVariant = panelState.getVariant(parentVariantId);

        Variant.Builder variantBuilder = new Variant.Builder(id);
        variantBuilder.setLayer(defaultLayer);
        variantBuilder.setParent(parentVariant);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VISIBILITY_TAG:
                    variantBuilder.setVisibility(parseVisibility(parser).isVisible());
                    break;
                case ALPHA_TAG:
                    variantBuilder.setAlpha(parseAlpha(context, parser).getAlpha());
                    break;
                case LAYER_TAG:
                    variantBuilder.setLayer(parseLayer(context, parser).getLayer());
                    break;
                case BOUNDS_TAG:
                    variantBuilder.setBounds(parseBounds(context, parser).getRect());
                    break;
                case SAFE_BOUNDS_TAG:
                    variantBuilder.setSafeBounds(parseBounds(context, parser).getRect());
                    break;
                case CORNER_TAG:
                    variantBuilder.setCornerRadius(parseCorner(context, parser).getRadius());
                    break;
                case INSETS_TAG:
                    variantBuilder.setInsets(parseInsets(context, parser));
                    break;
                default:
                    XmlPullParserHelper.skip(parser); // Skip other nested tags
            }
        }
        return variantBuilder.build();
    }

    @NonNull
    private static Visibility parseVisibility(@NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, VISIBILITY_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        boolean isVisible =
                attrs.getAttributeBooleanValue(null, IS_VISIBLE_ATTRIBUTE, DEFAULT_VISIBILITY);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Visibility.Builder().setIsVisible(isVisible).build();
    }

    @NonNull
    private static Alpha parseAlpha(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, ALPHA_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        float alpha = DEFAULT_ALPHA;
        int resId = attrs.getAttributeResourceValue(null, ALPHA_VALUE_ATTRIBUTE, 0);
        if (resId != 0) {
            alpha = context.getResources().getFloat(resId);
        } else {
            alpha = attrs.getAttributeFloatValue(null, ALPHA_VALUE_ATTRIBUTE, DEFAULT_ALPHA);
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Alpha.Builder().setAlpha(alpha).build();
    }

    @NonNull
    private static Layer parseLayer(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, LAYER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int layer = DEFAULT_LAYER;
        int resId = attrs.getAttributeResourceValue(null, LAYER_VALUE_ATTRIBUTE, 0);
        if (resId != 0) {
            layer = context.getResources().getInteger(resId);
        } else {
            layer = attrs.getAttributeIntValue(null, LAYER_VALUE_ATTRIBUTE, DEFAULT_LAYER);
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Layer.Builder().setLayer(layer).build();
    }

    @NonNull
    private static Bounds parseBounds(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        if (XmlPullParser.START_TAG != parser.getEventType()
                || !(BOUNDS_TAG.equals(parser.getName())
                || SAFE_BOUNDS_TAG.equals(parser.getName()))) {
            throw new XmlPullParserException(
                    "parseBounds called with wrong parser event type: " + parser.getEventType()
                            + " or name: " + parser.getName());
        }
        AttributeSet attrs = Xml.asAttributeSet(parser);

        Integer left = getDimensionPixelSize(context, attrs, LEFT_ATTRIBUTE, true);
        Integer top = getDimensionPixelSize(context, attrs, TOP_ATTRIBUTE, false);
        Integer right = getDimensionPixelSize(context, attrs, RIGHT_ATTRIBUTE, true);
        Integer bottom = getDimensionPixelSize(context, attrs, BOTTOM_ATTRIBUTE, false);

        Integer width = getDimensionPixelSize(context, attrs, WIDTH_ATTRIBUTE, true);
        Integer height = getDimensionPixelSize(context, attrs, HEIGHT_ATTRIBUTE, false);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Bounds.Builder()
                .setLeft(left)
                .setTop(top)
                .setRight(right)
                .setBottom(bottom)
                .setWidth(width)
                .setHeight(height)
                .build();
    }

    @NonNull
    private static Corner parseCorner(Context context, XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, CORNER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Integer radius = getDimensionPixelSize(context, attrs, RADIUS_ATTRIBUTE, false);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Corner.Builder()
                .setRadius(radius)
                .build();
    }

    private static Insets parseInsets(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, null, INSETS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        Integer left = getDimensionPixelSize(context, attrs, LEFT_ATTRIBUTE, true);
        Integer top = getDimensionPixelSize(context, attrs, TOP_ATTRIBUTE, false);
        Integer right = getDimensionPixelSize(context, attrs, RIGHT_ATTRIBUTE, true);
        Integer bottom = getDimensionPixelSize(context, attrs, BOTTOM_ATTRIBUTE, false);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return Insets.of(left, top, right, bottom);
    }

    @NonNull
    private static List<Transition> parseTransitions(
            @NonNull Context context, @NonNull PanelState panelState, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITIONS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        // possible lossy conversion from long to int. we're assuming the default duration can be
        // convereted to int safely.
        int duration = attrs.getAttributeIntValue(null, DEFAULT_DURATION_ATTRIBUTE,
                (int) DEFAULT_DURATION);
        int interpolatorRef =
                attrs.getAttributeResourceValue(null, DEFAULT_INTERPOLATOR_ATTRIBUTE, 0);
        Interpolator interpolator =
                interpolatorRef == 0
                        ? null
                        : AnimationUtils.loadInterpolator(context, interpolatorRef);

        List<Transition> result = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (parser.getName().equals(TRANSITION_TAG)) {
                result.add(
                        parseTransition(context, panelState, duration, interpolator, parser));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }
        return result;
    }

    @NonNull
    private static Transition parseTransition(
            @NonNull Context context,
            @NonNull PanelState panelState,
            long defaultDuration,
            @Nullable Interpolator defaultInterpolator,
            @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITION_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String from = attrs.getAttributeValue(null, FROM_VARIANT_ATTRIBUTE);
        String to = attrs.getAttributeValue(null, TO_VARIANT_ATTRIBUTE);
        String onEvent = attrs.getAttributeValue(null, ON_EVENT_ATTRIBUTE);
        String onEventTokens = attrs.getAttributeValue(null, ON_EVENT_TOKENS_ATTRIBUTE);
        int animatorId = attrs.getAttributeResourceValue(null, ANIMATOR_ATTRIBUTE, 0);
        Animator animator =
                animatorId == 0 ? null : AnimatorInflater.loadAnimator(context, animatorId);
        int duration = attrs.getAttributeIntValue(null, DURATION_ATTRIBUTE, (int) defaultDuration);
        int interpolatorRef = attrs.getAttributeResourceValue(null, INTERPOLATOR_ATTRIBUTE, 0);
        Interpolator interpolator =
                interpolatorRef == 0
                        ? defaultInterpolator
                        : AnimationUtils.loadInterpolator(context, interpolatorRef);
        Variant fromVariant = panelState.getVariant(from);
        Variant toVariant = panelState.getVariant(to);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Should be no nested tags.
        }

        return new Transition.Builder(fromVariant, toVariant)
                .setOnEvent(onEvent, onEventTokens)
                .setAnimator(animator)
                .setDefaultDuration(duration)
                .setDefaultInterpolator(interpolator)
                .build();
    }

    /**
     * Helper method to get a dimension pixel size from an attribute set.
     *
     * @param context      The application context.
     * @param attrs        The attribute set.
     * @param name         The name of the attribute.
     * @param isHorizontal Whether the dimension is horizontal (width) or vertical (height).
     * @return The dimension pixel size.
     */
    @Nullable
    private static Integer getDimensionPixelSize(@NonNull Context context,
            @NonNull AttributeSet attrs, @NonNull String name, boolean isHorizontal) {
        int resId = attrs.getAttributeResourceValue(null, name, 0);
        if (resId != 0) {
            return context.getResources().getDimensionPixelSize(resId);
        }
        String dimenStr = attrs.getAttributeValue(null, name);
        if (dimenStr == null) {
            return null;
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(PIXEL)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - PIXEL.length());
            return (int) Float.parseFloat(valueStr);
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(DP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(DIP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DIP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * Resources.getSystem().getDisplayMetrics().density);
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(PERCENT)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - PERCENT.length());
            float value = Float.parseFloat(valueStr);
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            if (isHorizontal) {
                return (int) (value * displayMetrics.widthPixels / 100);
            }
            return (int) (value * displayMetrics.heightPixels / 100);
        }
        // The default value is never returned because `attrs.getAttributeValue` is not null.
        return attrs.getAttributeIntValue(null, name, 0);
    }
}
