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

import static com.android.car.scalableui.loader.xml.EventTagXmlParserKt.EVENT_TAG;
import static com.android.car.scalableui.loader.xml.EventTagXmlParserKt.parseEvent;
import static com.android.car.scalableui.model.Alpha.DEFAULT_ALPHA;
import static com.android.car.scalableui.model.Focus.DEFAULT_FOCUS_ON_TRANSITION;
import static com.android.car.scalableui.model.Layer.DEFAULT_LAYER;
import static com.android.car.scalableui.model.Transition.DEFAULT_DURATION;
import static com.android.car.scalableui.model.Visibility.DEFAULT_VISIBILITY;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Insets;
import android.hardware.display.DisplayManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.R;
import com.android.car.scalableui.model.Alpha;
import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.BreakPoint;
import com.android.car.scalableui.model.Corner;
import com.android.car.scalableui.model.Decor;
import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.Focus;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.PanelType;
import com.android.car.scalableui.model.Restart;
import com.android.car.scalableui.model.Role;
import com.android.car.scalableui.model.TaskBehavior;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.model.Visibility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that uses a {@link XmlPullParser} to create a {@link PanelState} object for
 * <Panel></Panel> block.
 */
public class PanelTagXmlParser {
    // --- Panel Tags ---
    public static final String TASK_PANEL_TAG = "TaskPanel";
    public static final String DECOR_PANEL_TAG = "DecorPanel";
    public static final String ID_ATTRIBUTE = "id";
    public static final String DEFAULT_VARIANT_ATTRIBUTE = "defaultVariant";
    public static final String ROLE_ATTRIBUTE = "role";
    public static final String ROLE_TYPE_STRING = "string";
    public static final String ROLE_TYPE_ARRAY = "array";
    public static final String ROLE_TYPE_LAYOUT = "layout";
    public static final String DISPLAY_ID = "displayId";
    public static final String DEFAULT_LAYER_ATTRIBUTE = "defaultLayer";
    public static final String CONTROLLER = "controller";
    // --- Restart Tags ---
    public static final String RESTART_TAG = "Restart";
    public static final String POLICY_ATTRIBUTE = "policy";
    public static final String MAX_RETRY_ATTRIBUTE = "maxRetry";
    // --- TaskBehavior Tags --
    public static final String TASK_BEHAVIOR_TAG = "TaskBehavior";
    public static final String NEW_TASK_LAUNCH_POLICY_ATTRIBUTE = "newTaskLaunchPolicy";
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
    public static final String DELAY_ATTRIBUTE = "delay";
    public static final String INTERPOLATOR_ATTRIBUTE = "interpolator";
    // --- Variant Tags ---
    public static final String VARIANT_TAG = "Variant";
    public static final String PARENT_ATTRIBUTE = "parent";
    // --- Background Tags ---
    public static final String BACKGROUND_TAG = "Background";
    public static final String BACKGROUND_COLOR_ATTRIBUTE = "color";
    public static final String BACKGROUND_DRAWABLE_ATTRIBUTE = "drawable";
    public static final String BACKGROUND_ALPHA_ATTRIBUTE = "alpha";
    // --- Visibility Tags ---
    public static final String VISIBILITY_TAG = "Visibility";
    public static final String IS_VISIBLE_ATTRIBUTE = "isVisible";
    // --- Alpha Tags ---
    public static final String ALPHA_TAG = "Alpha";
    public static final String ALPHA_VALUE_ATTRIBUTE = "alpha";
    // --- Layer Tags ---
    public static final String LAYER_TAG = "Layer";
    public static final String LAYER_VALUE_ATTRIBUTE = "layer";
    // --- Focus Tags ---
    public static final String FOCUS_TAG = "Focus";
    public static final String FOCUS_ON_TRANSITION_ATTRIBUTE = "onTransition";
    // --- Bounds Tags ---
    public static final String BOUNDS_TAG = "Bounds";
    public static final String SAFE_BOUNDS_TAG = "SafeBounds";
    public static final String TASK_TOOLBAR_BOUNDS_TAG = "TaskToolbarBounds";
    public static final String LEFT_ATTRIBUTE = "left";
    public static final String RIGHT_ATTRIBUTE = "right";
    public static final String TOP_ATTRIBUTE = "top";
    public static final String BOTTOM_ATTRIBUTE = "bottom";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";
    public static final String LEFT_OFFSET_ATTRIBUTE = "leftOffset";
    public static final String TOP_OFFSET_ATTRIBUTE = "topOffset";
    public static final String RIGHT_OFFSET_ATTRIBUTE = "rightOffset";
    public static final String BOTTOM_OFFSET_ATTRIBUTE = "bottomOffset";
    // --- Corner Tags ---
    public static final String CORNER_TAG = "Corner";
    public static final String RADIUS_ATTRIBUTE = "radius";
    public static final String TOP_LEFT_RADIUS_ATTRIBUTE = "topLeftRadius";
    public static final String TOP_RIGHT_RADIUS_ATTRIBUTE = "topRightRadius";
    public static final String BOTTOM_LEFT_RADIUS_ATTRIBUTE = "bottomLeftRadius";
    public static final String BOTTOM_RIGHT_RADIUS_ATTRIBUTE = "bottomRightRadius";
    // --- Insets Tags ---
    public static final String INSETS_TAG = "Insets";
    public static final String DIP = "dip";
    public static final String DP = "dp";
    public static final String PERCENT = "%";
    public static final String PIXEL = "px";
    // --- Controller Tags ---
    public static final String CONTROLLER_TAG = "Controller";
    // --- Controller Metadata Tags ---
    public static final String CONTROLLER_NAME_TAG = "ControllerName";
    public static final String VIEW_TAG = "View";
    public static final String EVENT_ID_TAG = "EventId";
    public static final String OVERLAY_PANEL_ID_TAG = "OverlayPanelId";
    public static final String BACKGROUND_COLOR_TAG = "BackgroundColor";
    public static final String ORIENTATION_TAG = "Orientation";
    public static final String SNAPTHREADHOLD_TAG = "SnapThreadhold";
    public static final String PERSISTENT_ACTIVITY_TAG = "PersistentActivity";
    public static final String PERSISTENT_PACKAGE_TAG = "PersistentPackage";
    public static final String DEFAULT_COMPONENT_TAG = "DefaultComponent";
    public static final String UPDATABLE_INTENT_FILTER_TAG = "UpdateIntentFilter";
    public static final String TASK_TOOLBAR_CONTROLLER_TAG = "TaskToolBarController";
    // --- BreakPoints Tags ---
    public static final String BREAKPOINTS_TAG = "BreakPoints";
    public static final String BREAKPOINT_TAG = "BreakPoint";
    public static final String BREAKPOINT_POINT_TAG = "point";
    public static final String BREAKPOINT_EVENT_ID_TAG = "eventId";
    // --- KeyFrameVariant Tags ---
    static final String KEY_FRAME_VARIANT_TAG = "KeyFrameVariant";
    private static final String TAG = PanelTagXmlParser.class.getSimpleName();
    private static final String KEY_FRAME_TAG = "KeyFrame";
    private static final String FRAME_ATTRIBUTE = "frame";
    private static final String VARIANT_ATTRIBUTE = "variant";

    static PanelState parsePanel(@NonNull Context context, @NonNull XmlPullParser parser,
            @PanelType int type) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, type == PanelType.DECOR ? DECOR_PANEL_TAG
                : TASK_PANEL_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String displayIdStr = attrs.getAttributeValue(null, DISPLAY_ID);
        int displayId = (displayIdStr == null) ? DEFAULT_DISPLAY : Integer.parseInt(displayIdStr);
        String defaultVariant = attrs.getAttributeValue(null, DEFAULT_VARIANT_ATTRIBUTE);
        int roleValue = attrs.getAttributeResourceValue(null, ROLE_ATTRIBUTE, View.NO_ID);

        Integer defaultLayer = null;
        if (attrs.getAttributeValue(null, DEFAULT_LAYER_ATTRIBUTE) != null) {
            int resId = attrs.getAttributeResourceValue(null, DEFAULT_LAYER_ATTRIBUTE, 0);
            if (resId != 0) {
                defaultLayer = context.getResources().getInteger(resId);
            } else {
                defaultLayer = attrs.getAttributeIntValue(null, DEFAULT_LAYER_ATTRIBUTE,
                        DEFAULT_LAYER);
            }
        }
        PanelControllerMetadata panelControllerMetaData = null;
        if (attrs.getAttributeValue(null, CONTROLLER) != null) {
            int xmlId = attrs.getAttributeResourceValue(null, CONTROLLER, 0);
            if (xmlId != 0) {
                try {
                    panelControllerMetaData = createController(context, xmlId, displayId);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "error inflate" + id + " with controller " + xmlId);
                }
            }
        }

        PanelState.Builder builder = new PanelState.Builder(id, type);
        if (roleValue != View.NO_ID) {
            builder.setRole(parseRole(context, roleValue, id));
        }
        builder.setDisplayId(displayId);
        builder.setDefaultVariant(defaultVariant);
        builder.setPanelControllerMetadata(panelControllerMetaData);
        PanelState panelState = builder.build();

        Map<String, VariantPropertyParser> variantParserMap = Map.of(
                VISIBILITY_TAG, getVariantVisibilityParser(),
                ALPHA_TAG, getVariantAlphaParser(),
                LAYER_TAG, getVariantLayerParser(),
                FOCUS_TAG, getVariantFocusParser(),
                BOUNDS_TAG, getVariantBoundsParser(BOUNDS_TAG),
                SAFE_BOUNDS_TAG, getVariantBoundsParser(SAFE_BOUNDS_TAG),
                TASK_TOOLBAR_BOUNDS_TAG, getVariantBoundsParser(TASK_TOOLBAR_BOUNDS_TAG),
                CORNER_TAG, getVariantCornerParser(),
                INSETS_TAG, getVariantInsetsParser(),
                BACKGROUND_TAG, getVariantBackgroundParser(id));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            switch (name) {
                case VARIANT_TAG -> panelState.addVariant(
                        parseVariant(context, panelState, defaultLayer, parser,
                                variantParserMap, displayId));
                case KEY_FRAME_VARIANT_TAG -> panelState.addVariant(
                        parseKeyFrameVariant(panelState, parser, context));
                case TRANSITIONS_TAG -> {
                    List<Transition> transitions = parseTransitions(context, displayId, panelState,
                            parser);
                    for (Transition transition : transitions) {
                        panelState.addTransition(transition);
                    }
                }
                case RESTART_TAG -> panelState.addRestart(parseRestart(parser));
                case TASK_BEHAVIOR_TAG -> panelState.addTaskBehavior(parseTaskBehavior(parser));
                default -> XmlPullParserHelper.skip(parser);
            }
        }
        panelState.setVariant(defaultVariant); // Set the initial variant
        return panelState;
    }

    @Nullable
    private static Role parseRole(@NonNull Context context, int roleValue, String panelId) {
        try {
            Role.Builder roleBuilder = new Role.Builder();
            String roleTypeName = context.getResources().getResourceTypeName(roleValue);
            switch (roleTypeName) {
                case ROLE_TYPE_STRING -> {
                    String roleString = context.getResources().getString(roleValue);
                    if (PanelState.DEFAULT_ROLE.equals(roleString)) {
                        roleBuilder.setIsDefault(true);
                    } else {
                        roleBuilder.addPersistentActivity(roleString);
                    }
                }
                case ROLE_TYPE_ARRAY -> {
                    String[] componentNames = context.getResources().getStringArray(roleValue);
                    for (String componentName : componentNames) {
                        roleBuilder.addPersistentActivity(componentName);
                    }
                }
                case ROLE_TYPE_LAYOUT -> roleBuilder.setLayoutId(roleValue);
                default -> Log.e(TAG, "Role type is not supported " + roleTypeName);
            }
            return roleBuilder.build();
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "role resource not found for " + panelId + ", roleValue: " + roleValue);
            return null;
        }
    }

    private static Restart parseRestart(@NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, RESTART_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String policy = attrs.getAttributeValue(null, POLICY_ATTRIBUTE);
        int maxRetry = attrs.getAttributeIntValue(null, MAX_RETRY_ATTRIBUTE, 0);
        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }
        return new Restart(policy, maxRetry);
    }

    private static TaskBehavior parseTaskBehavior(@NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TASK_BEHAVIOR_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String policy = attrs.getAttributeValue(null, NEW_TASK_LAUNCH_POLICY_ATTRIBUTE);
        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }
        return new TaskBehavior(policy);
    }

    static PanelControllerMetadata createController(@NonNull Context context, int xmlId,
            int displayId) throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlId);
        return parseController(context, parser, displayId);

    }

    private static PanelControllerMetadata parseController(@NonNull Context context,
            @NonNull XmlResourceParser parser, int displayId)
            throws IOException, XmlPullParserException {
        // Consume any START_DOCUMENT or whitespace events
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT || (eventType == XmlPullParser.TEXT
                && parser.isWhitespace())) {
            eventType = parser.next();
        }
        if (eventType != XmlPullParser.START_TAG || !parser.getName().equals(CONTROLLER_TAG)) {
            throw new XmlPullParserException(
                    "Expected <Controller> tag at the beginning but " + parser.getName());
        }

        parser.require(XmlPullParser.START_TAG, null, CONTROLLER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        PanelControllerMetadata.Builder builder = PanelControllerMetadata.builder(id);
        Log.e(TAG, "init " + id);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                break;
            }
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName() == null) {
                parser.next();
                continue;
            }

            String name = parser.getName();
            switch (name) {
                case BREAKPOINTS_TAG:
                    builder.addBreakPoints(parseBreakPoints(context, parser, displayId));
                    break;
                case CONTROLLER_NAME_TAG:
                case VIEW_TAG:
                case EVENT_ID_TAG:
                case OVERLAY_PANEL_ID_TAG:
                case BACKGROUND_COLOR_TAG:
                case ORIENTATION_TAG:
                case SNAPTHREADHOLD_TAG:
                case PERSISTENT_ACTIVITY_TAG:
                case PERSISTENT_PACKAGE_TAG:
                case DEFAULT_COMPONENT_TAG:
                case UPDATABLE_INTENT_FILTER_TAG:
                case TASK_TOOLBAR_CONTROLLER_TAG:
                    String value = XmlPullParserHelper.readText(parser);
                    if (value != null) {
                        builder.addConfiguration(name, value);
                    } else {
                        Log.e(TAG, "No value for Controller Tag: " + name);
                    }
                    break;
                default:
                    Log.w(TAG, "Unsupported Controller Tag: " + name);
                    XmlPullParserHelper.skip(parser);
            }
        }
        return builder.build();
    }

    private static List<BreakPoint> parseBreakPoints(Context context, XmlResourceParser parser,
            int displayId) throws IOException, XmlPullParserException {
        List<BreakPoint> points = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if (name.equals(BREAKPOINT_TAG)) {
                points.add(parseBreakPoint(context, parser, displayId));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }
        // We are now at the END_TAG for <BreakPoints>
        return points;
    }

    private static BreakPoint parseBreakPoint(@NonNull Context context,
            @NonNull XmlPullParser parser, int displayId)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, BREAKPOINT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Integer point = getDimensionPixelSize(context, attrs, BREAKPOINT_POINT_TAG, displayId,
                false);
        String eventId = attrs.getAttributeValue(null, BREAKPOINT_EVENT_ID_TAG);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, BREAKPOINT_TAG);

        return new BreakPoint.Builder(point, eventId).build();
    }

    @NonNull
    static Variant parseKeyFrameVariant(@NonNull PanelState panelState,
            @NonNull XmlPullParser parser, Context context)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, KEY_FRAME_VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String idName = getIdName(context, id);
        String parentStr = attrs.getAttributeValue(null, PARENT_ATTRIBUTE);
        Variant parent = panelState.getVariant(parentStr);
        KeyFrameVariant.Builder builder = new KeyFrameVariant.Builder(id, idName);
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

    private static KeyFrameVariant.KeyFrame parseKeyFrame(@NonNull PanelState panelState,
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
    static Variant parseVariant(@NonNull Context context, @NonNull PanelState panelState,
            @Nullable Integer defaultLayer, @NonNull XmlPullParser parser,
            @NonNull Map<String, VariantPropertyParser> supportedTags,
            int displayId)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, VARIANT_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String id = attrs.getAttributeValue(null, ID_ATTRIBUTE);
        String idName = getIdName(context, id);
        String parentVariantId = attrs.getAttributeValue(null, PARENT_ATTRIBUTE);
        Variant parentVariant = panelState.getVariant(parentVariantId);

        Variant.Builder variantBuilder = new Variant.Builder(id, idName);
        variantBuilder.setLayer(defaultLayer);
        variantBuilder.setParent(parentVariant);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if (supportedTags.containsKey(name)) {
                variantBuilder = supportedTags.get(name).parse(context, parser, variantBuilder,
                        displayId);
            } else {
                Log.w(TAG, "Unsupported Variant Tag: " + name);
                XmlPullParserHelper.skip(parser); // Skip other nested tags
            }
        }
        return variantBuilder.build();
    }

    /**
     * Attempt to get the String name from a resource id.
     *
     * @return the resource string or the passed in id param if the string could not be parsed
     */
    @NonNull
    static String getIdName(@NonNull Context context, @NonNull String id) {
        Pattern pattern = Pattern.compile("^@(\\d+)$");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find() && matcher.groupCount() >= 1) {
            String idName = matcher.group(1); // Group 1 is resource id number
            if (idName != null && !idName.isEmpty()) {
                try {
                    int resourceId = Integer.parseInt(idName);
                    return context.getResources().getResourceEntryName(
                            resourceId);
                } catch (NumberFormatException | Resources.NotFoundException e) {
                    Log.e(TAG, "invalid resource format for string " + id);
                }
            }
        }
        // If not an integer (after the @) or the res id is not valid, fallback to id string
        return id;
    }

    static VariantPropertyParser getVariantBackgroundParser(@NonNull String id) {
        return (context, parser, builder, displayId) -> builder.addDecor(
                parseBackground(context, parser, id));
    }

    @NonNull
    private static Decor parseBackground(@NonNull Context context, @NonNull XmlPullParser parser,
            @NonNull String id) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, BACKGROUND_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        String decorId = id + "_" + BACKGROUND_TAG;
        int colorRes = attrs.getAttributeResourceValue(/* namespace= */null,
                BACKGROUND_COLOR_ATTRIBUTE, /* defaultValue= */-1);
        // TODO(b/441580484): The drawableRes might be from a different package if it's only
        // defined in an RRO. Add defensive mechanisms to handle cases where the resource ID
        // is valid but not resolvable within the current package context.
        int drawableRes = attrs.getAttributeResourceValue(/* namespace= */null,
                BACKGROUND_DRAWABLE_ATTRIBUTE, /* defaultValue= */-1);
        float alpha = 1f;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if (Objects.equals(name, BACKGROUND_ALPHA_ATTRIBUTE)) {
                alpha = parseAlpha(context, parser).getAlpha();
            }
        }

        return new Decor(decorId, /* layer= */ -1, colorRes, drawableRes, alpha,
                R.layout.background_layout);
    }

    static VariantPropertyParser getVariantVisibilityParser() {
        return (context, parser, builder, displayId) -> builder.setVisibility(
                parseVisibility(parser).isVisible());
    }

    @NonNull
    private static Visibility parseVisibility(@NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, VISIBILITY_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        boolean isVisible = attrs.getAttributeBooleanValue(null, IS_VISIBLE_ATTRIBUTE,
                DEFAULT_VISIBILITY);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Visibility.Builder().setIsVisible(isVisible).build();
    }

    static VariantPropertyParser getVariantAlphaParser() {
        return (context, parser, builder, displayId) -> builder.setAlpha(
                parseAlpha(context, parser).getAlpha());
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

    static VariantPropertyParser getVariantLayerParser() {
        return (context, parser, builder, displayId) -> builder.setLayer(
                parseLayer(context, parser).getLayer());
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

    static VariantPropertyParser getVariantFocusParser() {
        return (context, parser, builder, displayId) -> builder.setCanFocusOnTransition(
                parseFocus(context, parser).getCanFocusOnTransition());
    }

    @NonNull
    private static Focus parseFocus(@NonNull Context context, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, FOCUS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        boolean focusOnTransition = DEFAULT_FOCUS_ON_TRANSITION;
        int resId = attrs.getAttributeResourceValue(null, FOCUS_ON_TRANSITION_ATTRIBUTE, 0);
        if (resId != 0) {
            focusOnTransition = context.getResources().getBoolean(resId);
        } else {
            focusOnTransition = attrs.getAttributeBooleanValue(null, FOCUS_ON_TRANSITION_ATTRIBUTE,
                    DEFAULT_FOCUS_ON_TRANSITION);
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Focus(focusOnTransition);
    }

    /**
     * Returns a parser that extracts bounds from XML and applies them to the correct property
     * in the Variant.Builder based on the provided tag.
     *
     * @param tag The XML tag (e.g., "Bounds", "SafeBounds", "TaskToolbarBounds") that
     *            determines which property on the builder is set.
     * @return A {@link VariantPropertyParser} for handling bounds attributes.
     */
    static VariantPropertyParser getVariantBoundsParser(String tag) {
        return (context, parser, builder, displayId) -> switch (tag) {
            case BOUNDS_TAG -> builder.setBounds(
                    parseBounds(context, parser, displayId).getRect());
            case SAFE_BOUNDS_TAG -> builder.setSafeBounds(
                    parseBounds(context, parser, displayId).getRect());
            case TASK_TOOLBAR_BOUNDS_TAG -> builder.setTaskToolbarBounds(
                    parseBounds(context, parser, displayId).getRect());
            default -> throw new IllegalStateException("Unknown bounds tag: " + tag);
        };
    }

    private static boolean isSupportedBoundsTag(@NonNull String tag) {
        return BOUNDS_TAG.equals(tag) || SAFE_BOUNDS_TAG.equals(tag)
                || TASK_TOOLBAR_BOUNDS_TAG.equals(tag);
    }

    @NonNull
    static Bounds parseBounds(@NonNull Context context, @NonNull XmlPullParser parser,
            int displayId) throws IOException, XmlPullParserException {
        if (XmlPullParser.START_TAG != parser.getEventType() || !isSupportedBoundsTag(
                parser.getName())) {
            throw new XmlPullParserException(
                    "parseBounds called with wrong parser event type: " + parser.getEventType()
                            + " or name: " + parser.getName());
        }
        AttributeSet attrs = Xml.asAttributeSet(parser);

        Integer left = getDimensionPixelSize(context, attrs, LEFT_ATTRIBUTE, displayId,
                /* isHorizontal= */ true);
        Integer top = getDimensionPixelSize(context, attrs, TOP_ATTRIBUTE, displayId,
                /* isHorizontal= */ false);
        Integer right = getDimensionPixelSize(context, attrs, RIGHT_ATTRIBUTE, displayId,
                /* isHorizontal= */ true);
        Integer bottom = getDimensionPixelSize(context, attrs, BOTTOM_ATTRIBUTE, displayId,
                /* isHorizontal= */ false);

        Integer width = getDimensionPixelSize(context, attrs, WIDTH_ATTRIBUTE, displayId,
                /* isHorizontal= */ true);
        Integer height = getDimensionPixelSize(context, attrs, HEIGHT_ATTRIBUTE, displayId,
                /* isHorizontal= */ false);

        Integer leftOffset = getDimensionPixelSize(context, attrs, LEFT_OFFSET_ATTRIBUTE,
                displayId, /* isHorizontal= */ true);
        Integer topOffset = getDimensionPixelSize(context, attrs, TOP_OFFSET_ATTRIBUTE,
                displayId, /* isHorizontal= */ false);
        Integer rightOffset = getDimensionPixelSize(context, attrs, RIGHT_OFFSET_ATTRIBUTE,
                displayId, /* isHorizontal= */ true);
        Integer bottomOffset = getDimensionPixelSize(context, attrs, BOTTOM_OFFSET_ATTRIBUTE,
                displayId, /* isHorizontal= */ false);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return new Bounds.Builder().setLeft(left).setTop(top).setRight(right).setBottom(
                bottom).setWidth(width).setHeight(height).setLeftOffset(leftOffset).setTopOffset(
                topOffset).setRightOffset(rightOffset).setBottomOffset(bottomOffset).build();
    }

    static VariantPropertyParser getVariantCornerParser() {
        return (context, parser, builder, displayId) -> builder.setCornerRadius(
                parseCorner(context, parser, displayId));
    }

    @NonNull
    private static Corner parseCorner(Context context, XmlPullParser parser, int displayId)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, CORNER_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        Corner.Builder builder = new Corner.Builder();
        builder.setRadius(
                getDimensionPixelSize(context, attrs, RADIUS_ATTRIBUTE, displayId, false));
        if (!com.android.graphics.surfaceflinger.flags.Flags.setClientDrawnCornerRadii() && (
                attrs.getAttributeValue(null, TOP_LEFT_RADIUS_ATTRIBUTE) != null
                        || attrs.getAttributeValue(null, TOP_RIGHT_RADIUS_ATTRIBUTE) != null
                        || attrs.getAttributeValue(null, BOTTOM_LEFT_RADIUS_ATTRIBUTE) != null
                        || attrs.getAttributeValue(null, BOTTOM_RIGHT_RADIUS_ATTRIBUTE) != null)) {
            Log.w(TAG, "Individual corner radius attributes are defined. These are only supported "
                    + "when the set_client_drawn_corner_radii flag is enabled.");
        } else if (com.android.graphics.surfaceflinger.flags.Flags.setClientDrawnCornerRadii()) {
            builder.setTopLeftRadius(
                    getDimensionPixelSize(context, attrs, TOP_LEFT_RADIUS_ATTRIBUTE, displayId,
                            false));
            builder.setTopRightRadius(
                    getDimensionPixelSize(context, attrs, TOP_RIGHT_RADIUS_ATTRIBUTE, displayId,
                            false));
            builder.setBottomLeftRadius(
                    getDimensionPixelSize(context, attrs, BOTTOM_LEFT_RADIUS_ATTRIBUTE, displayId,
                            false));
            builder.setBottomRightRadius(
                    getDimensionPixelSize(context, attrs, BOTTOM_RIGHT_RADIUS_ATTRIBUTE, displayId,
                            false));
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }
        return builder.build();
    }

    static VariantPropertyParser getVariantInsetsParser() {
        return (context, parser, builder, displayId) -> builder.setInsets(
                parseInsets(context, parser, displayId));
    }

    private static Insets parseInsets(@NonNull Context context, @NonNull XmlPullParser parser,
            int displayId) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, null, INSETS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        Integer left = getDimensionPixelSize(context, attrs, LEFT_ATTRIBUTE, displayId, true);
        Integer top = getDimensionPixelSize(context, attrs, TOP_ATTRIBUTE, displayId, false);
        Integer right = getDimensionPixelSize(context, attrs, RIGHT_ATTRIBUTE, displayId, true);
        Integer bottom = getDimensionPixelSize(context, attrs, BOTTOM_ATTRIBUTE, displayId, false);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.skip(parser); // Skip any nested tags
        }

        return Insets.of(left, top, right, bottom);
    }

    @NonNull
    static List<Transition> parseTransitions(@NonNull Context context, int displayId,
            @NonNull PanelState panelState, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITIONS_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        // possible lossy conversion from long to int. we're assuming the default duration can be
        // converted to int safely.
        int duration = attrs.getAttributeIntValue(null, DEFAULT_DURATION_ATTRIBUTE,
                (int) DEFAULT_DURATION);
        int interpolatorRef = attrs.getAttributeResourceValue(null, DEFAULT_INTERPOLATOR_ATTRIBUTE,
                0);
        Interpolator interpolator = interpolatorRef == 0 ? null : AnimationUtils.loadInterpolator(
                context, interpolatorRef);

        List<Transition> result = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (parser.getName().equals(TRANSITION_TAG)) {
                result.add(parseTransition(context, displayId, panelState, duration, interpolator,
                        parser));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }
        return result;
    }

    @NonNull
    private static Transition parseTransition(@NonNull Context context, int displayId,
            @NonNull PanelState panelState, long defaultDuration,
            @Nullable Interpolator defaultInterpolator, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITION_TAG);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        String from = attrs.getAttributeValue(null, FROM_VARIANT_ATTRIBUTE);
        String to = attrs.getAttributeValue(null, TO_VARIANT_ATTRIBUTE);
        String onEvent = attrs.getAttributeValue(null, ON_EVENT_ATTRIBUTE);
        String onEventTokens = attrs.getAttributeValue(null, ON_EVENT_TOKENS_ATTRIBUTE);
        List<Event> events = new ArrayList<>();
        if (onEvent != null) {
            Event.Builder builder = new Event.Builder(onEvent)
                    .addTokensFromString(onEventTokens)
                    .addApplicableDisplay(displayId);
            events.add(builder.build());
        }
        int animatorId = attrs.getAttributeResourceValue(null, ANIMATOR_ATTRIBUTE, 0);
        Animator animator = animatorId == 0 ? null : AnimatorInflater.loadAnimator(context,
                animatorId);
        int duration = attrs.getAttributeIntValue(null, DURATION_ATTRIBUTE, (int) defaultDuration);
        int delay = attrs.getAttributeIntValue(null, DELAY_ATTRIBUTE, 0);
        int interpolatorRef = attrs.getAttributeResourceValue(null, INTERPOLATOR_ATTRIBUTE, 0);
        Interpolator interpolator =
                interpolatorRef == 0 ? defaultInterpolator : AnimationUtils.loadInterpolator(
                        context, interpolatorRef);
        Variant fromVariant = panelState.getVariant(from);
        Variant toVariant = panelState.getVariant(to);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (parser.getName().equals(EVENT_TAG)) {
                events.add(parseEvent(parser, displayId));
            } else {
                XmlPullParserHelper.skip(parser);
            }
        }

        return new Transition.Builder(fromVariant, toVariant)
                .addEvents(events)
                .setAnimator(animator).setDefaultDuration(duration)
                .setDelay(delay).setDefaultInterpolator(interpolator).build();
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
    static Integer getDimensionPixelSize(@NonNull Context context, @NonNull AttributeSet attrs,
            @NonNull String name, int displayId, boolean isHorizontal) {
        int resId = attrs.getAttributeResourceValue(null, name, 0);
        String dimenStr;
        if (resId != 0) {
            // Attempt to resolve resource - supports dimen, integer, fraction, and string types
            String resType = context.getResources().getResourceTypeName(resId);
            // dimen and integer values will be used directly as the pixel size
            if (resType.equals("dimen")) {
                return context.getResources().getDimensionPixelSize(resId);
            }
            if (resType.equals("integer")) {
                return context.getResources().getInteger(resId);
            }
            if (resType.equals("attr")) {
                TypedValue typedValue = new TypedValue();
                if (context.getTheme().resolveAttribute(resId, typedValue, true)) {
                    if (typedValue.type == TypedValue.TYPE_DIMENSION) {
                        return TypedValue.complexToDimensionPixelSize(
                                typedValue.data,
                                context.getResources().getDisplayMetrics()
                        );
                    }
                }
            }

            // fraction and string types will be used as string to be parsed
            dimenStr = switch (resType) {
                case "fraction" -> String.format("%f%%",
                        context.getResources().getFraction(resId, 100, 100));
                case "string" -> context.getResources().getString(resId);
                default -> throw new IllegalArgumentException("Invalid res type " + resType);
            };
        } else {
            dimenStr = attrs.getAttributeValue(null, name);
        }

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
            return (int) (value * getDisplayMetricsForDisplay(context, displayId).density);
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(DIP)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - DIP.length());
            float value = Float.parseFloat(valueStr);
            return (int) (value * getDisplayMetricsForDisplay(context, displayId).density);
        }
        if (dimenStr.toLowerCase(Locale.ROOT).endsWith(PERCENT)) {
            String valueStr = dimenStr.substring(0, dimenStr.length() - PERCENT.length());
            float value = Float.parseFloat(valueStr);
            DisplayMetrics displayMetrics = getDisplayMetricsForDisplay(context, displayId);
            if (isHorizontal) {
                return (int) (value * displayMetrics.widthPixels / 100);
            }
            return (int) (value * displayMetrics.heightPixels / 100);
        }
        // The default value is never returned because `attrs.getAttributeValue` is not null.
        return attrs.getAttributeIntValue(null, name, 0);
    }

    @NonNull
    static DisplayMetrics getDisplayMetricsForDisplay(@NonNull Context context,
            int displayId) {
        DisplayManager displayManager = context.getSystemService(DisplayManager.class);
        if (displayManager == null) {
            throw new IllegalStateException("Cannot obtain display manager");
        }
        Display display = displayManager.getDisplay(displayId);
        if (display == null) {
            throw new IllegalArgumentException("Cannot find display " + displayId);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }
}
