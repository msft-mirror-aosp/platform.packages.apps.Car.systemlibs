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

package com.android.car.tos

import android.car.settings.CarSettings.Secure.KEY_UNACCEPTED_TOS_DISABLED_APPS
import android.car.settings.CarSettings.Secure.KEY_USER_TOS_ACCEPTED
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import java.net.URISyntaxException
import java.util.Objects

/** Helper methods for terms of services (tos) restrictions **/
object TosHelper {
    private const val TAG = "TosHelper"

    // This value indicates if TOS is in uninitialized state
    const val TOS_UNINITIALIZED = "0"

    // This value indicates if TOS has not been accepted by the user
    const val TOS_NOT_ACCEPTED = "1"

    // This value indicates if TOS has been accepted by the user
    const val TOS_ACCEPTED = "2"
    private const val TOS_DISABLED_APPS_SEPARATOR = ","

    /**
     * Returns a set of packages that are disabled when terms of services are not accepted.
     *
     * @param context The application context
     * @param uid A user id for a particular user
     *
     * @return Set of packages disabled by tos
     */
    @JvmStatic
    @JvmOverloads
    fun getTosDisabledPackages(context: Context, uid: Int = UserHandle.myUserId()): Set<String> {
        val settingsValue = Settings.Secure.getStringForUser(
            context.contentResolver,
            KEY_UNACCEPTED_TOS_DISABLED_APPS,
            uid
        )
        return settingsValue?.split(TOS_DISABLED_APPS_SEPARATOR)?.toSet() ?: emptySet()
    }

    /**
     * Gets the intent for launching the terms of service acceptance flow.
     *
     * @param context The app context
     * @param id The desired resource identifier
     *
     * @return TOS intent, or null
     */
    @JvmStatic
    fun getIntentForTosAcceptanceFlow(context: Context, id: Int): Intent? {
        val tosIntentName = context.resources.getString(id)
        return try {
            Intent.parseUri(tosIntentName, Intent.URI_ANDROID_APP_SCHEME)
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid intent URI in user_tos_activity_intent", e)
            null
        }
    }

    /**
     * Replaces the [mapIntent] with an intent defined in the resources with [id] if terms of
     * services have not been accepted and the app defined by [mapIntent] is disabled.
     */
    @JvmStatic
    @JvmOverloads
    fun maybeReplaceWithTosMapIntent(
         context: Context,
         mapIntent: Intent,
         id: Int,
         uid: Int = UserHandle.myUserId()
     ): Intent {
         val packageName = mapIntent.component?.packageName
         val tosDisabledPackages = getTosDisabledPackages(context, uid)

        Log.i(TAG, "TOS disabled packages:$tosDisabledPackages")
        Log.i(TAG, "TOS accepted:" + tosAccepted(context))

        // Launch tos map intent when the user has not accepted tos and when the
        // default maps package is not available to package manager, or it's disabled by tos
        if (!tosAccepted(context) &&
            (packageName == null || tosDisabledPackages.contains(packageName))
        ) {
            Log.i(TAG, "Replacing default maps intent with tos map intent")
            return getIntentForTosAcceptanceFlow(context, id) ?: mapIntent
        }
        return mapIntent
    }

    /**
     * Returns true if tos is accepted or uninitialized, false otherwise.
     */
    @JvmStatic
    @JvmOverloads
    fun tosAccepted(context: Context, uid: Int = UserHandle.myUserId()): Boolean {
        val settingsValue = Settings.Secure.getStringForUser(
            context.contentResolver,
            KEY_USER_TOS_ACCEPTED,
            uid
        )
        // We consider an uninitialized state to be TOS accepted.
        return Objects.equals(settingsValue, TOS_ACCEPTED) || tosStatusUninitialized(context, uid)
    }

    /**
     * Returns true if tos is uninitialized, false otherwise.
     */
    @JvmStatic
    @JvmOverloads
    fun tosStatusUninitialized(context: Context, uid: Int = UserHandle.myUserId()): Boolean {
        val settingsValue = Settings.Secure.getStringForUser(
            context.contentResolver,
            KEY_USER_TOS_ACCEPTED,
            uid
        )
        return settingsValue == null || Objects.equals(settingsValue, TOS_UNINITIALIZED)
    }
}
