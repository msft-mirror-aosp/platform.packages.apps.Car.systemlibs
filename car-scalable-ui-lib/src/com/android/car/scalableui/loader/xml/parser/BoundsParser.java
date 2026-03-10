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

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.Bounds;
import com.android.car.scalableui.model.Variant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link Bounds} tag. */
public class BoundsParser implements TagParser<Bounds>, XmlChildParser<Variant.Builder> {
    public static final String BOUNDS_TAG = "Bounds";
    public static final String SAFE_BOUNDS_TAG = "SafeBounds";
    public static final String TASK_TOOLBAR_BOUNDS_TAG = "TaskToolbarBounds";
    public static final String LEFT_ATTRIBUTE = "left";
    public static final String TOP_ATTRIBUTE = "top";
    public static final String RIGHT_ATTRIBUTE = "right";
    public static final String BOTTOM_ATTRIBUTE = "bottom";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";
    public static final String LEFT_OFFSET_ATTRIBUTE = "leftOffset";
    public static final String TOP_OFFSET_ATTRIBUTE = "topOffset";
    public static final String RIGHT_OFFSET_ATTRIBUTE = "rightOffset";
    public static final String BOTTOM_OFFSET_ATTRIBUTE = "bottomOffset";

    private final String mTagName;

    public BoundsParser(@NonNull String tagName) {
        mTagName = tagName;
    }

    public BoundsParser() {
        this(BOUNDS_TAG);
    }

    /** Returns true if the tag is a supported bounds tag. */
    public static boolean isSupportedBoundsTag(@NonNull String tag) {
        return BOUNDS_TAG.equals(tag)
                || SAFE_BOUNDS_TAG.equals(tag)
                || TASK_TOOLBAR_BOUNDS_TAG.equals(tag);
    }

    private static final AttributeMap<Bounds.Builder> ATTRIBUTES =
            AttributeMap.<Bounds.Builder>builder()
                    .add(
                            LEFT_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    builder.setLeft(v);
                                }
                            })
                    .add(
                            TOP_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    builder.setTop(v);
                                }
                            })
                    .add(
                            RIGHT_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    builder.setRight(v);
                                }
                            })
                    .add(
                            BOTTOM_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    builder.setBottom(v);
                                }
                            })
                    .add(
                            WIDTH_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    builder.setWidth(v);
                                }
                            })
                    .add(
                            HEIGHT_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    builder.setHeight(v);
                                }
                            })
                    .add(
                            LEFT_OFFSET_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    builder.setLeftOffset(v);
                                }
                            })
                    .add(
                            TOP_OFFSET_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    builder.setTopOffset(v);
                                }
                            })
                    .add(
                            RIGHT_OFFSET_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                true);
                                if (v != null) {
                                    builder.setRightOffset(v);
                                }
                            })
                    .add(
                            BOTTOM_OFFSET_ATTRIBUTE,
                            (env, value, builder) -> {
                                Integer v =
                                        ParserUtils.parseDimensionPixelSize(
                                                env.getContext(),
                                                env.getValueParser(),
                                                value,
                                                env.getDisplayId(),
                                                false);
                                if (v != null) {
                                    builder.setBottomOffset(v);
                                }
                            })
                    .build();

    @NonNull
    @Override
    public Bounds parseTag(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, mTagName);

        Bounds.Builder builder = new Bounds.Builder();
        ATTRIBUTES.parse(env, parser, builder);

        while (parser.next() != XmlPullParser.END_TAG) {
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }

        return builder.build();
    }

    @Override
    public void parse(
            @NonNull ParserEnv env,
            @NonNull XmlPullParser parser,
            @NonNull Variant.Builder builder)
            throws XmlPullParserException, IOException {
        Bounds bounds = parseTag(env, parser);
        if (SAFE_BOUNDS_TAG.equals(mTagName)) {
            builder.setSafeBounds(bounds.getRect());
        } else if (TASK_TOOLBAR_BOUNDS_TAG.equals(mTagName)) {
            builder.setTaskToolbarBounds(bounds.getRect());
        } else {
            builder.setBounds(bounds.getRect());
        }
    }
}
