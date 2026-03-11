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

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.scalableui.model.PanelState;
import com.android.car.scalableui.model.Role;

/** Parser for {@link Role}. */
public class RoleParser {
    private static final String TAG = RoleParser.class.getSimpleName();
    public static final String ROLE_TYPE_STRING = "string";
    public static final String ROLE_TYPE_ARRAY = "array";
    public static final String ROLE_TYPE_LAYOUT = "layout";

    /** Parses {@link Role} from a resource ID. */
    @Nullable
    public static Role parseRole(@NonNull Context context, int roleValue, String panelId) {
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
}
