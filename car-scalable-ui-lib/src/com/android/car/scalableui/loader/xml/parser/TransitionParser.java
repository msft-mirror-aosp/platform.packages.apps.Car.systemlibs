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

package com.android.car.scalableui.loader.xml.parser;

import static com.android.car.scalableui.loader.xml.parser.EventParser.EVENT_TAG;
import static com.android.car.scalableui.loader.xml.parser.ParserUtils.getIdName;

import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Event;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Transition;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Transition} elements. */
public class TransitionParser implements XmlChildParser<PanelState> {
    public static final String TRANSITIONS_TAG = "Transitions";
    public static final String TRANSITION_TAG = "Transition";

    private static final String DEFAULT_DURATION_ATTRIBUTE = "defaultDuration";
    private static final String DEFAULT_INTERPOLATOR_ATTRIBUTE = "defaultInterpolator";
    private static final String FROM_VARIANT_ATTRIBUTE = "fromVariant";
    private static final String TO_VARIANT_ATTRIBUTE = "toVariant";
    private static final String ON_EVENT_ATTRIBUTE = "onEvent";
    private static final String ON_EVENT_TOKENS_ATTRIBUTE = "onEventTokens";
    private static final String ANIMATOR_ATTRIBUTE = "animator";
    private static final String DURATION_ATTRIBUTE = "duration";
    private static final String DELAY_ATTRIBUTE = "delay";
    private static final String INTERPOLATOR_ATTRIBUTE = "interpolator";

    private static class TransitionDefaults {
        int mDuration = (int) Transition.DEFAULT_DURATION;
        Interpolator mInterpolator;
    }

    private static final AttributeMap<TransitionDefaults> TRANSITIONS_ATTRIBUTES =
            AttributeMap.<TransitionDefaults>builder()
                    .addInteger(
                            DEFAULT_DURATION_ATTRIBUTE,
                            (defaults, value) -> defaults.mDuration = value)
                    .add(
                            DEFAULT_INTERPOLATOR_ATTRIBUTE,
                            (env, value, defaults) ->
                                    defaults.mInterpolator =
                                            env.getValueParser()
                                                    .parseInterpolator(env.getContext(), value))
                    .build();

    private static class TransitionParsingState {
        final Transition.Builder mBuilder = new Transition.Builder();
        String mOnEvent;
        String mOnEventTokens;
        String mToVariantId;
        String mFromVariantId;
    }

    private static final AttributeMap<TransitionParsingState> TRANSITION_ATTRIBUTES =
            AttributeMap.<TransitionParsingState>builder()
                    .add(
                            FROM_VARIANT_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mFromVariantId = getIdName(env.getContext(), value))
                    .add(
                            TO_VARIANT_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mToVariantId = getIdName(env.getContext(), value))
                    .addString(ON_EVENT_ATTRIBUTE, (state, value) -> state.mOnEvent = value)
                    .addString(
                            ON_EVENT_TOKENS_ATTRIBUTE,
                            (state, value) -> state.mOnEventTokens = value)
                    .add(
                            ANIMATOR_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBuilder.setAnimator(
                                            env.getValueParser()
                                                    .parseAnimator(env.getContext(), value)))
                    .addInteger(
                            DURATION_ATTRIBUTE,
                            (state, value) -> state.mBuilder.setDefaultDuration(value))
                    .addInteger(DELAY_ATTRIBUTE, (state, value) -> state.mBuilder.setDelay(value))
                    .add(
                            INTERPOLATOR_ATTRIBUTE,
                            (env, value, state) ->
                                    state.mBuilder.setDefaultInterpolator(
                                            env.getValueParser()
                                                    .parseInterpolator(env.getContext(), value)))
                    .build();

    /** Parses a list of {@link Transition} from XML. */
    @Override
    public void parse(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws XmlPullParserException, IOException {
        int displayId = parent.getDisplayId();
        parser.require(XmlPullParser.START_TAG, null, TRANSITIONS_TAG);

        TransitionDefaults defaults = new TransitionDefaults();
        TRANSITIONS_ATTRIBUTES.parse(env, parser, defaults);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName().equals(TRANSITION_TAG)) {
                parseTransition(env, displayId, parent, defaults, parser);
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }
    }

    /** Parses a {@link Transition} from XML. */
    private void parseTransition(
            @NonNull ParserEnv env,
            int displayId,
            @NonNull PanelState panelState,
            @NonNull TransitionDefaults defaults,
            @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TRANSITION_TAG);

        TransitionParsingState state = new TransitionParsingState();
        // Set defaults
        state.mBuilder.setDefaultDuration(defaults.mDuration);
        state.mBuilder.setDefaultInterpolator(defaults.mInterpolator);

        TRANSITION_ATTRIBUTES.parse(env, parser, state);

        if (state.mOnEvent != null) {
            Event.Builder builder =
                    new Event.Builder(state.mOnEvent).addApplicableDisplay(displayId);
            if (state.mOnEventTokens != null) {
                builder.addTokensFromString(state.mOnEventTokens);
            }
            state.mBuilder.addEvent(builder.build());
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName().equals(EVENT_TAG)) {
                TagParser<Event> eventParser = env.getRegistry().getParser(EVENT_TAG);
                if (eventParser != null) {
                    state.mBuilder.addEvent(eventParser.parseTag(env, parser));
                } else {
                    XmlPullParserHelper.throwIfUnknownTag(parser);
                }
            } else {
                XmlPullParserHelper.throwIfUnknownTag(parser);
            }
        }

        if (state.mToVariantId == null) {
            throw new IllegalStateException("toVariant attribute is required for Transition");
        }

        panelState.addPendingTransition(state.mBuilder, state.mToVariantId, state.mFromVariantId);
    }
}
