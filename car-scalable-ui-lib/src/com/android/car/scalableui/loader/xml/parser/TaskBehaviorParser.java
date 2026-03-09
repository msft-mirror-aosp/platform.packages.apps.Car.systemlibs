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

import static com.android.car.scalableui.loader.xml.parser.TaskBehaviorParser.NEW_TASK_LAUNCH_POLICY_ATTRIBUTE;
import static com.android.car.scalableui.loader.xml.parser.TaskBehaviorParser.TASK_BEHAVIOR_TAG;
import static com.android.car.scalableui.loader.xml.parser.TaskBehaviorParser.TASK_PROPERTIES_ATTRIBUTE;

import androidx.annotation.NonNull;

import com.android.car.scalableui.loader.xml.ParserEnv;
import com.android.car.scalableui.loader.xml.XmlChildParser;
import com.android.car.scalableui.loader.xml.XmlPullParserHelper;
import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.TaskBehavior;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** Parser for {@link TaskBehavior} tag. */
public class TaskBehaviorParser implements XmlChildParser<PanelState> {

    public static final String TASK_BEHAVIOR_TAG = "TaskBehavior";
    public static final String TASK_PROPERTIES_ATTRIBUTE = "taskProperties";
    public static final String NEW_TASK_LAUNCH_POLICY_ATTRIBUTE = "newTaskLaunchPolicy";

    private static class TaskBehaviorParsingState {
        String mTaskProperties = TaskBehavior.TASK_PROPERTY_DEFAULT;
        String mNewTaskLaunchPolicy = TaskBehavior.NEW_TASK_LAUNCH_POLICY_DEFAULT;
    }

    private static final AttributeMap<TaskBehaviorParsingState> ATTRIBUTES =
            AttributeMap.<TaskBehaviorParsingState>builder()
                    .addString(
                            TASK_PROPERTIES_ATTRIBUTE,
                            (state, value) -> state.mTaskProperties = value)
                    .addString(
                            NEW_TASK_LAUNCH_POLICY_ATTRIBUTE,
                            (state, value) -> state.mNewTaskLaunchPolicy = value)
                    .build();

    @NonNull
    private TaskBehavior parseTaskBehavior(@NonNull ParserEnv env, @NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TASK_BEHAVIOR_TAG);

        TaskBehaviorParsingState state = new TaskBehaviorParsingState();
        ATTRIBUTES.parse(env, parser, state);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            XmlPullParserHelper.throwIfUnknownTag(parser);
        }
        return new TaskBehavior(state.mTaskProperties, state.mNewTaskLaunchPolicy);
    }

    @Override
    public void parse(
            @NonNull ParserEnv env, @NonNull XmlPullParser parser, @NonNull PanelState parent)
            throws XmlPullParserException, IOException {
        parent.addTaskBehavior(parseTaskBehavior(env, parser));
    }
}
