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

package com.android.car.scalableui.designcompose;

import android.animation.Animator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

import com.android.car.scalableui.model.Alpha;
import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.KeyFrameVariant;
import com.android.car.scalableui.model.Layer;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Role;
import com.android.car.scalableui.model.Transition;
import com.android.car.scalableui.model.Variant;
import com.android.car.scalableui.model.Visibility;
import com.android.designcompose.definition.element.Event;
import com.android.designcompose.definition.element.Keyframe;
import com.android.designcompose.definition.element.KeyframeVariant;
import com.android.designcompose.definition.element.ScalableDimension;
import com.android.designcompose.definition.element.ScalableUIComponentSet;
import com.android.designcompose.definition.element.ScalableUiVariant;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class PanelStateDocLoader {
    public static final long DEFAULT_TRANSITION_DURATION = 300;
    private static final String TAG = "DC_" + PanelStateDocLoader.class.getSimpleName();

    @NonNull
    private Context mContext;

    public PanelStateDocLoader(@NonNull Context context) {
        mContext = context;
    }

    /** Loads the panel state */
    public List<PanelState> loadPanelStates(@NonNull InputStream fileStream, @NonNull String docId)
            throws DocLoadException {
        ScalableUiDoc doc = ScalableUiDocKt.loadScalableUiDoc(fileStream, docId);
        return loadPanelStates(doc);
    }

    private List<PanelState> loadPanelStates(ScalableUiDoc doc) throws DocLoadException {
        if (doc == null) throw new DocLoadException("Cannot load from null ScalableUiDoc");

        List<ScalableUIComponentSet> panels = doc.getPanels();
        if (panels == null || panels.isEmpty()) throw new DocLoadException("No panels found");

        ArrayList<PanelState> states = new ArrayList(panels.size());
        int index = 0;
        for (ScalableUIComponentSet setData : panels) {
            PanelState panel = createPanelStateFromDoc(setData, doc);
            states.add(panel);
            ++index;
        }
        return states;
    }

    private static boolean isInteger(@NonNull String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private PanelState createPanelStateFromDoc(
            @NonNull ScalableUIComponentSet setData,
            @NonNull ScalableUiDoc doc
    ) throws DocLoadException {
        String roleName = setData.getRole();

        Role.Builder roleBuilder = new Role.Builder();
        StringTokenizer tokenizer = new StringTokenizer(roleName, ";");
        if (tokenizer.countTokens() > 1) {
            // This is an array of component names
            while (tokenizer.hasMoreTokens()) {
                String componentName = tokenizer.nextToken();
                roleBuilder.addPersistentActivity(componentName);
                Log.i(TAG, "Panel " + setData.getName() + " ROLE array " + componentName);
            }
        } else if (isInteger(roleName)) {
            // This is a layout ID
            int roleId = Integer.parseInt(roleName);
            roleBuilder.setLayoutId(roleId);
            Log.i(TAG, "Panel " + setData.getName() + " ROLE Int " + roleId);
        } else if (PanelState.DEFAULT_ROLE.equals(roleName)) {
            // This is a default role
            roleBuilder.setIsDefault(true);
            Log.i(TAG, "Panel " + setData.getName() + " ROLE DEFAULT");
        } else {
            // This is a single string representing the persistent activity to use
            roleBuilder.addPersistentActivity(roleName);
            Log.i(TAG, "Panel " + setData.getName() + " ROLE activity " + roleName);
        }

        PanelState result = new PanelState(setData.getName());
        result.setRole(roleBuilder.build());

        // Add variants
        Log.i(TAG, "createPanel " + setData.getName() + ", default "
                + setData.getDefaultVariantName());
        int index = 0;
        for (String variantId : setData.getVariantIdsList()) {
            ScalableUiVariant scalableUiVariant = doc.getVariantById(variantId);
            if (scalableUiVariant == null) {
                throw new DocLoadException("No variant found with id " + variantId);
            }
            Variant variant = createVariantFromDoc(scalableUiVariant);
            result.addVariant(variant);
            ++index;
        }

        // Add keyframe variants
        for (KeyframeVariant kfv : setData.getKeyframeVariantsList()) {
            KeyFrameVariant keyFrameVariant = createKeyFrameVariantFromDoc(kfv, result);
            result.addVariant(keyFrameVariant);
        }

        // Add transitions
        long transitionDuration = DEFAULT_TRANSITION_DURATION; // TODO get this from doc
        Interpolator interpolator =
                new AccelerateDecelerateInterpolator(); // TODO get this from doc
        Log.i(TAG, "  addTransitions " + setData.getEventsList().size());
        for (com.android.designcompose.definition.element.Event event : setData.getEventsList()) {
            String transitionName = event.getEventName();
            if (event == null) {
                throw new DocLoadException("No event found from transition " + transitionName);
            }

            Transition transition =
                    createTransitionFromDoc(event, result, transitionDuration, interpolator);
            result.addTransition(transition);
        }

        result.setVariant(setData.getDefaultVariantName());
        return result;
    }

    private Variant createVariantFromDoc(
            @NonNull ScalableUiVariant variant
    ) throws DocLoadException {
        Log.i(TAG, "  createVariant " + variant.getName() + " layer " + variant.getLayer()
                + " visible " + variant.getIsVisible() + " alpha " + variant.getAlpha());
        Variant result =
                new Variant.Builder(variant.getName(), variant.getName())
                        .setLayer(new Layer.Builder().setLayer(
                                variant.getLayer()).build().getLayer())
                        .setVisibility(
                                new Visibility.Builder().setIsVisible(
                                        variant.getIsVisible()).build().isVisible())
                        .setAlpha(new Alpha.Builder().setAlpha(
                                variant.getAlpha()).build().getAlpha())
                        .setBounds(createBoundsFromDoc(variant).getRect())
                        .build();
        return result;
    }

    private Bounds createBoundsFromDoc(@NonNull ScalableUiVariant scalableUiVariant) {
        com.android.designcompose.definition.element.Bounds b = scalableUiVariant.getBounds();
        ScalableDimension dimLeft = b.getLeft();
        ScalableDimension dimTop = b.getTop();
        ScalableDimension dimWidth = b.getWidth();
        ScalableDimension dimHeight = b.getHeight();

        int left = getDimensionPixelSize(true, dimLeft);
        int top = getDimensionPixelSize(false, dimTop);
        int width = getDimensionPixelSize(true, dimWidth);
        int height = getDimensionPixelSize(false, dimHeight);
        Log.i(TAG, "    createBounds l " + left + " t " + top + " w " + width + " h " + height);

        return new Bounds.Builder().setLeft(left).setTop(top).setWidth(width).setHeight(
                height).build();
    }

    private int getDimensionPixelSize(boolean isHorizontal, @NonNull ScalableDimension dim) {
        if (dim.hasPoints()) {
            return (int) (dim.getPoints());
        } else {
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            if (isHorizontal) {
                return (int) (dim.getPercent() * displayMetrics.widthPixels / 100);
            } else {
                return (int) (dim.getPercent() * displayMetrics.heightPixels / 100);
            }
        }
    }

    private KeyFrameVariant createKeyFrameVariantFromDoc(
            @NonNull KeyframeVariant kfv,
            @NonNull PanelState panelState
    )
            throws DocLoadException {
        // No ID for KFV, so use name
        KeyFrameVariant.Builder result = new KeyFrameVariant.Builder(kfv.getName(), "");
        for (Keyframe kf : kfv.getKeyframesList()) {
            int frame = kf.getFrame();
            String variantName = kf.getVariantName();
            Variant panelVariant = panelState.getVariant(variantName);
            if (panelVariant == null) {
                throw new DocLoadException("Could not find variant with name " + variantName);
            }
            result.addKeyFrame(new KeyFrameVariant.KeyFrame(frame, panelVariant));
        }
        return result.build();
    }

    private Transition createTransitionFromDoc(
            @NonNull Event event,
            @NonNull PanelState panelState,
            long duration,
            @NonNull Interpolator interpolator
    ) throws DocLoadException {
        Variant fromVariant = panelState.getVariant(event.getFromVariantName());
        Variant toVariant = panelState.getVariant(event.getToVariantName());
        if (toVariant == null) {
            throw new DocLoadException(
                    "Could not find variant with name " + event.getToVariantName());
        }
        Animator animator = null; // TODO get from doc
        Log.i(TAG, "    addTransition " + event.getEventName() + ", " + event.getEventTokens()
                + " -> from " + (fromVariant != null ? fromVariant.getId() : "NULL") + " to "
                + toVariant.getId());
        return new Transition.Builder(fromVariant, toVariant)
                .setAnimator(animator)
                .setDefaultDuration(duration)
                .setDefaultInterpolator(interpolator)
                .setOnEvent(event.getEventName(), event.getEventTokens())
                .build();
    }
}
