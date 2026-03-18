/*
 * Copyright (C) 2026 The Android Open Source Project
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

import android.os.Build;

import androidx.annotation.NonNull;

import com.android.car.scalableui.Flags;
import com.android.car.scalableui.loader.xml.parser.ActionParser;
import com.android.car.scalableui.loader.xml.parser.AlphaParser;
import com.android.car.scalableui.loader.xml.parser.BackgroundParser;
import com.android.car.scalableui.loader.xml.parser.BoundsParser;
import com.android.car.scalableui.loader.xml.parser.BreakPointsParser;
import com.android.car.scalableui.loader.xml.parser.ControllerConfigListParser;
import com.android.car.scalableui.loader.xml.parser.ControllerConfigParser;
import com.android.car.scalableui.loader.xml.parser.CornerIndividualParser;
import com.android.car.scalableui.loader.xml.parser.CornerParser;
import com.android.car.scalableui.loader.xml.parser.EventParser;
import com.android.car.scalableui.loader.xml.parser.ExternalConfigsParser;
import com.android.car.scalableui.loader.xml.parser.FocusParser;
import com.android.car.scalableui.loader.xml.parser.GravityParser;
import com.android.car.scalableui.loader.xml.parser.HunPanelParser;
import com.android.car.scalableui.loader.xml.parser.InsetsParser;
import com.android.car.scalableui.loader.xml.parser.KeyFrameVariantParser;
import com.android.car.scalableui.loader.xml.parser.LayerParser;
import com.android.car.scalableui.loader.xml.parser.PanelControllerParser;
import com.android.car.scalableui.loader.xml.parser.PanelParser;
import com.android.car.scalableui.loader.xml.parser.RestartParser;
import com.android.car.scalableui.loader.xml.parser.SystemBarParser;
import com.android.car.scalableui.loader.xml.parser.TaskBehaviorParser;
import com.android.car.scalableui.loader.xml.parser.TransitionParser;
import com.android.car.scalableui.loader.xml.parser.VariantParser;
import com.android.car.scalableui.loader.xml.parser.VisibilityParser;
import com.android.car.scalableui.loader.xml.parser.XmlChildParser;
import com.android.car.scalableui.model.GravityVariant;
import com.android.car.scalableui.model.PanelControllerMetadata;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Variant;

/**
 * Standard parser module that registers all core Scalable UI parsers.
 *
 * <p>This module registers parsers with the {@link XmlParserRegistry} based on how the parent
 * elements expect to interact with their children:
 *
 * <ul>
 *   <li><b>Standard Parsers ({@link TagParser}):</b> Registered via {@code registerParser()}. These
 *       are used when a parent parser wants to explicitly receive the parsed object (the result) to
 *       perform custom logic, validation, or conditional application. Root elements (like {@code
 *       <TaskPanel>}) are also registered this way.
 *   <li><b>Child Parsers ({@link XmlChildParser}):</b> Registered via {@code
 *       registerChildParser()}. These act as extensions that directly modify a parent object (like
 *       a {@code Builder}). The parent delegates the entire parsing of the child tag to the
 *       {@code XmlChildParser} without needing to inspect the result.
 * </ul>
 *
 * <p>Many parser implementations (like {@link AlphaParser}) implement both interfaces for
 * flexibility. However, they are typically only registered for the interface that represents their
 * primary role in the standard framework. For example, {@code <Alpha>} is registered as an
 * {@code XmlChildParser} for {@code Variant.Builder} because the variant parser doesn't need to
 * do anything custom with the alpha value; it just lets the {@code AlphaParser} update the
 * builder directly.
 *
 * <p>In cases where a tag is used in multiple ways, it may be registered as both. For example,
 * {@code <Bounds>} is registered as:
 *
 * <ul>
 *   <li>A {@link TagParser} for {@code registerParser()}: Used by {@code SystemBarParser} which
 *       needs to receive the {@code Bounds} object to perform custom validation specifically for
 *       system bars.
 *   <li>An {@link XmlChildParser} for {@code Variant.Builder}: Used when {@code <Bounds>} is a
 *       property of a generic {@code Variant}, where it can be applied directly to the builder.
 * </ul>
 */
public class CoreParserModule implements ParserModule {

    @Override
    public void registerParsers(@NonNull XmlParserRegistry registry) {
        if (Build.IS_DEBUGGABLE) {
            registry.registerParser(
                    ExternalConfigsParser.EXTERNAL_CONFIGS_TAG, new ExternalConfigsParser());
        }
        registry.registerParser(PanelParser.TASK_PANEL_TAG, new PanelParser());
        registry.registerParser(PanelParser.DECOR_PANEL_TAG, new PanelParser());
        if (Flags.enableExtPanelUpdates()) {
            registry.registerParser(SystemBarParser.SYSTEM_BAR_TAG, new SystemBarParser());
            registry.registerParser(HunPanelParser.HUN_PANEL_TAG, new HunPanelParser());
        }
        registry.registerParser(ActionParser.ACTIONS_TAG, new ActionParser());
        registry.registerParser(BoundsParser.BOUNDS_TAG, new BoundsParser());
        registry.registerParser(BackgroundParser.BACKGROUND_TAG, new BackgroundParser());
        registry.registerParser(EventParser.EVENT_TAG, new EventParser());
        registry.registerParser(PanelControllerParser.CONTROLLER_TAG, new PanelControllerParser());

        // Child Parsers
        registry.registerChildParser(
                PanelState.class, TransitionParser.TRANSITIONS_TAG, new TransitionParser());
        registry.registerChildParser(
                PanelState.class, RestartParser.RESTART_TAG, new RestartParser());
        registry.registerChildParser(
                PanelState.class, TaskBehaviorParser.TASK_BEHAVIOR_TAG, new TaskBehaviorParser());
        registry.registerChildParser(
                PanelState.class, VariantParser.VARIANT_TAG, new VariantParser());
        registry.registerChildParser(
                PanelState.class,
                KeyFrameVariantParser.KEY_FRAME_VARIANT_TAG,
                new KeyFrameVariantParser());

        registry.registerChildParser(
                Variant.Builder.class, VisibilityParser.VISIBILITY_TAG, new VisibilityParser());
        registry.registerChildParser(
                Variant.Builder.class, AlphaParser.ALPHA_TAG, new AlphaParser());
        registry.registerChildParser(
                Variant.Builder.class, LayerParser.LAYER_TAG, new LayerParser());
        registry.registerChildParser(
                Variant.Builder.class, FocusParser.FOCUS_TAG, new FocusParser());
        registry.registerChildParser(
                Variant.Builder.class,
                BoundsParser.BOUNDS_TAG,
                new BoundsParser(BoundsParser.BOUNDS_TAG));
        registry.registerChildParser(
                Variant.Builder.class,
                BoundsParser.SAFE_BOUNDS_TAG,
                new BoundsParser(BoundsParser.SAFE_BOUNDS_TAG));
        registry.registerChildParser(
                Variant.Builder.class,
                BoundsParser.TASK_TOOLBAR_BOUNDS_TAG,
                new BoundsParser(BoundsParser.TASK_TOOLBAR_BOUNDS_TAG));
        if (com.android.graphics.surfaceflinger.flags.Flags.setClientDrawnCornerRadii()) {
            registry.registerChildParser(
                    Variant.Builder.class, CornerParser.CORNER_TAG, new CornerIndividualParser());
        } else {
            registry.registerChildParser(
                    Variant.Builder.class, CornerParser.CORNER_TAG, new CornerParser());
        }
        registry.registerChildParser(
                Variant.Builder.class, InsetsParser.INSETS_TAG, new InsetsParser());
        registry.registerChildParser(
                Variant.Builder.class, BackgroundParser.BACKGROUND_TAG, new BackgroundParser());

        // Child parsers for PanelControllerParser
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.CONTROLLER_NAME_TAG,
                new ControllerConfigParser(PanelControllerParser.CONTROLLER_NAME_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.VIEW_TAG,
                new ControllerConfigParser(PanelControllerParser.VIEW_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.EVENT_ID_TAG,
                new ControllerConfigParser(PanelControllerParser.EVENT_ID_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.OVERLAY_PANEL_ID_TAG,
                new ControllerConfigParser(PanelControllerParser.OVERLAY_PANEL_ID_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.BACKGROUND_COLOR_TAG,
                new ControllerConfigParser(PanelControllerParser.BACKGROUND_COLOR_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.ORIENTATION_TAG,
                new ControllerConfigParser(PanelControllerParser.ORIENTATION_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.SNAPTHREADHOLD_TAG,
                new ControllerConfigParser(PanelControllerParser.SNAPTHREADHOLD_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.PERSISTENT_ACTIVITY_TAG,
                new ControllerConfigParser(PanelControllerParser.PERSISTENT_ACTIVITY_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.DEFAULT_COMPONENT_TAG,
                new ControllerConfigParser(PanelControllerParser.DEFAULT_COMPONENT_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.DEFAULT_INTENT_TAG,
                new ControllerConfigParser(PanelControllerParser.DEFAULT_INTENT_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.UPDATABLE_INTENT_FILTER_TAG,
                new ControllerConfigParser(PanelControllerParser.UPDATABLE_INTENT_FILTER_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.TASK_TOOLBAR_CONTROLLER_TAG,
                new ControllerConfigParser(PanelControllerParser.TASK_TOOLBAR_CONTROLLER_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.PERSISTENT_PACKAGE_TAG,
                new ControllerConfigParser(PanelControllerParser.PERSISTENT_PACKAGE_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                PanelControllerParser.PERSISTENT_ACTIVITY_LIST_TAG,
                new ControllerConfigListParser(PanelControllerParser.PERSISTENT_ACTIVITY_TAG));
        registry.registerChildParser(
                PanelControllerMetadata.Builder.class,
                BreakPointsParser.BREAKPOINTS_TAG,
                new BreakPointsParser());

        // SystemBar specific property parser overrides or additions
        registry.registerChildParser(
                GravityVariant.Builder.class, GravityParser.GRAVITY_TAG, new GravityParser());
    }
}
