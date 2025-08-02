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

import com.android.car.scalableui.model.Action;
import com.android.car.scalableui.model.PanelState;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Loads {@link PanelState} from an xml resource.
 */
public class XmlModelLoader {
    private static final String TAG = XmlModelLoader.class.getSimpleName();

    private Context mContext;

    public XmlModelLoader(Context context) {
        mContext = context;
    }

    /** Creates a {@link PanelState} using the given xml resource */
    public PanelState createPanelState(int resourceId) {
        try (XmlResourceParser parser = mContext.getResources().getXml(resourceId)) {
            PanelState ps = PanelStateXmlParser.parse(mContext, parser);
            return ps;
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing xml", e);
            return null;
        }
    }

    /** Creates a list of {@link actions} using the given xml resource */
    public List<Action> createActions(int resourceId) {
        try (XmlResourceParser parser = mContext.getResources().getXml(resourceId)) {
            List<Action> actions = ActionXmlParser.parse(mContext, parser);
            return actions;
        } catch (XmlPullParserException | IOException | URISyntaxException e) {
            Log.e(TAG, "Error parsing xml", e);
            return null;
        }
    }
}
