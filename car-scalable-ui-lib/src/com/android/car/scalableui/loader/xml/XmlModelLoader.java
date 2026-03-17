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

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.loader.xml.parser.ResourceValueParser;
import com.android.car.scalableui.loader.xml.parser.TagParser;
import com.android.car.scalableui.loader.xml.parser.ValueParser;
import com.android.car.scalableui.model.Action;
import com.android.car.scalableui.model.ExternalConfig;
import com.android.car.scalableui.model.PanelState;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/** Loads {@link PanelState} from an xml resource. */
public class XmlModelLoader {
    private static final String TAG = XmlModelLoader.class.getSimpleName();

    private final Context mContext;
    private final XmlParserRegistry mRegistry;
    private final ParserEnv mParserEnv;

    public XmlModelLoader(Context context) {
        this(context, new CoreParserModule());
    }

    public XmlModelLoader(Context context, ParserModule... modules) {
        this(context, new ResourceValueParser(), modules);
    }

    public XmlModelLoader(Context context, ValueParser valueParser, ParserModule... modules) {
        mContext = context;
        mRegistry = new XmlParserRegistry();
        registerParsers(modules);
        mParserEnv = new ParserEnv(mContext, valueParser, mRegistry);
    }

    private void registerParsers(ParserModule... modules) {
        for (ParserModule module : modules) {
            module.registerParsers(mRegistry);
        }
    }

    /** Creates a {@link PanelState} using the given xml resource */
    @Nullable
    public PanelState createPanelState(int resourceId) {
        try (XmlResourceParser parser = mContext.getResources().getXml(resourceId)) {
            Object result = parseXml(parser, mParserEnv);
            if (result instanceof PanelState) {
                return (PanelState) result;
            } else {
                Log.e(TAG, "Parsed object is not a PanelState: " + result);
                return null;
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing xml", e);
            return null;
        }
    }

    /** Creates a list of {@link Action}s using the given xml resource */
    @Nullable
    @SuppressWarnings("unchecked")
    public List<Action> createActions(int resourceId) {
        try (XmlResourceParser parser = mContext.getResources().getXml(resourceId)) {
            Object result = parseXml(parser, mParserEnv);
            if (result instanceof List) {
                return (List<Action>) result;
            } else {
                Log.e(TAG, "Parsed object is not a List<Action>: " + result);
                return null;
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing xml", e);
            return null;
        }
    }

    /** Creates an {@link ExternalConfig} using the given xml resource */
    @Nullable
    public ExternalConfig createExternalConfig(int resourceId) {
        try (XmlResourceParser parser = mContext.getResources().getXml(resourceId)) {
            Object result = parseXml(parser, mParserEnv);
            if (result instanceof ExternalConfig) {
                return (ExternalConfig) result;
            } else {
                Log.e(TAG, "Parsed object is not an ExternalConfig: " + result);
                return null;
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing xml", e);
            return null;
        }
    }

    @Nullable
    private Object parseXml(@NonNull XmlResourceParser parser, @NonNull ParserEnv env)
            throws XmlPullParserException, IOException {
        // Consume any START_DOCUMENT, COMMENT, or whitespace events
        int eventType = parser.getEventType();
        while (eventType == XmlPullParser.START_DOCUMENT
                || eventType == XmlPullParser.COMMENT
                || (eventType == XmlPullParser.TEXT && parser.isWhitespace())) {
            eventType = parser.next();
        }

        if (eventType != XmlPullParser.START_TAG) {
            throw new XmlPullParserException(
                    "Unrecognized tag at the beginning: " + parser.getName());
        }

        String tagName = parser.getName();
        TagParser<?> tagParser = mRegistry.getParser(tagName);
        if (tagParser != null) {
            return tagParser.parseTag(env, parser);
        } else {
            throw new XmlPullParserException("No parser registered for tag: " + tagName);
        }
    }
}
