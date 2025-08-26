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

package com.android.car.datasubscription;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static com.android.car.datasubscription.DataSubscription.DATA_SUBSCRIPTION_ACTION;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.TaskStackListener;
import android.car.drivingstate.CarUxRestrictions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.android.car.ui.utils.CarUxRestrictionsUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller to display the data subscription messages
 */
public class DataSubscriptionController implements DataSubscription.DataSubscriptionChangeListener,
        DataSubscriptionViewActionListener {
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;
    private static final String TAG = DataSubscriptionController.class.toString();
    private static final String DATA_SUBSCRIPTION_SHARED_PREFERENCE_PATH =
            "com.android.car.systemui.car.qc.DataSubscriptionController";
    // Timeout for network callback in ms
    private static final int CALLBACK_TIMEOUT_MS = 1000;
    // Latch count for network callback
    private static final int NETWORK_CALLBACK_LATCH_COUNT = 1;
    private final Context mContext;
    private DataSubscription mSubscription;
    private final Intent mIntent;
    private ConnectivityManager mConnectivityManager;
    private DataSubscriptionNetworkCallback mNetworkCallback;
    private final Handler mMainHandler;
    private Executor mBackgroundExecutor;
    private Set<String> mActivitiesBlocklist;
    private Set<String> mPackagesBlocklist;
    private CountDownLatch mLatch;
    private boolean mIsNetworkCallbackRegistered;
    private DataSubscriptionMessageEventListener mDataSubscriptionMessageEventListener;
    private int mUserId = -1;
    private final TaskStackListener mTaskStackListener = new TaskStackListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) {
            if (mIsNetworkCallbackRegistered && mConnectivityManager != null) {
                mNetworkCallback.mNetwork = null;
                mNetworkCallback.mTopActivity = null;
                mNetworkCapabilities = null;
                mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
                mIsNetworkCallbackRegistered = false;
            }

            if (taskInfo.baseIntent.getAction() == DATA_SUBSCRIPTION_ACTION) {
                mDataSubscriptionStatsLogHelper.logButtonClicked();
            }
            if (taskInfo.topActivity == null || mConnectivityManager == null) {
                return;
            }
            if (mUserId == -1) {
                throw new IllegalArgumentException("User id is not set");
            }
            ComponentName topActivityComponent = taskInfo.topActivity;
            String topActivity;

            if (isMediaComponent(topActivityComponent)) {
                ComponentName mediaComponentName = getMediaComponentName(taskInfo);
                if (mediaComponentName == null) {
                    return;
                }
                mTopPackage = mediaComponentName.getPackageName();
                topActivity = mediaComponentName.flattenToString();
            } else {
                mTopPackage = taskInfo.topActivity.getPackageName();
                topActivity = taskInfo.topActivity.flattenToString();
            }

            if (mPackagesBlocklist.contains(mTopPackage)
                    || mActivitiesBlocklist.contains(topActivity)) {
                return;
            }

            PackageInfo packageInfo;
            ApplicationInfo appInfo;
            try {
                packageInfo = mContext.getPackageManager().getPackageInfoAsUser(mTopPackage,
                        PackageManager.GET_PERMISSIONS, mUserId);
                appInfo = mContext.getPackageManager().getApplicationInfoAsUser(
                        mTopPackage, 0, mUserId);
                if (packageInfo != null) {
                    String[] permissions = packageInfo.requestedPermissions;
                    boolean appReqInternet = Arrays.asList(permissions).contains(
                            ACCESS_NETWORK_STATE)
                            && Arrays.asList(permissions).contains(INTERNET);
                    if (!appReqInternet) {
                        mActivitiesBlocklist.add(topActivity);
                        return;
                    }
                }

                mTopLabel = appInfo.loadLabel(mContext.getPackageManager());
                int uid = appInfo.uid;
                mNetworkCallback.mTopActivity = topActivity;
                mLatch = new CountDownLatch(NETWORK_CALLBACK_LATCH_COUNT);
                mConnectivityManager.registerDefaultNetworkCallbackForUid(uid, mNetworkCallback,
                        mMainHandler);
                mIsNetworkCallbackRegistered = true;
                // since we don't have the option of using the synchronous call of getting the
                // default network by UID, we need to set a timeout period to make sure the network
                // from the callback is updated correctly before deciding to display the message
                //TODO: b/336869328 use the synchronous call to update network status
                mBackgroundExecutor.execute(() -> {
                    try {
                        mLatch.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "error updating network callback" + e);
                    } finally {
                        if (mNetworkCallback.mNetwork == null) {
                            mNetworkCapabilities = null;
                            updateShouldDisplayReactiveMessageForApp(mTopLabel, topActivity);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, mTopPackage + " not found : " + e);
            }
        }
    };

    private final CarUxRestrictionsUtil.OnUxRestrictionsChangedListener
            mUxRestrictionsChangedListener =
            new CarUxRestrictionsUtil.OnUxRestrictionsChangedListener() {
                @Override
                public void onRestrictionsChanged(@NonNull CarUxRestrictions carUxRestrictions) {
                    mIsDistractionOptimizationRequired =
                            carUxRestrictions.isRequiresDistractionOptimization();
                    if (mDataSubscriptionMessageCreator != null) {
                        mUxrPrompt = mDataSubscriptionMessageCreator.getUxrPrompt(
                            mIsDistractionOptimizationRequired);
                    }
                    if (mDataSubscriptionMessageEventListener != null) {
                        mDataSubscriptionMessageEventListener.onUxrChanged(
                                mIsDistractionOptimizationRequired, mUxrPrompt);
                    }
                }
            };
    private final DataSubscriptionMessageCreator mDataSubscriptionMessageCreator;
    // Determines whether a proactive message was already displayed
    private boolean mIsDistractionOptimizationRequired;
    private boolean mShouldDisplayProactiveMessage;

    private boolean mShouldDisplayReactiveMessage;
    private String mTopPackage;
    private CharSequence mTopLabel;
    private NetworkCapabilities mNetworkCapabilities;
    private boolean mIsUxRestrictionsListenerRegistered;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private int mCurrentInterval;
    private int mCurrentCycle;
    private int mCurrentActiveDays;
    private int mCurrentStatus;
    private String mUxrPrompt;
    private DataSubscriptionStatsLogHelper mDataSubscriptionStatsLogHelper;

    static final ComponentName CAR_MEDIA_ACTIVITY = new ComponentName(
            "com.android.car.media",
            "com.android.car.media.MediaActivity"
    );

    static final ComponentName CAR_MEDIA_DISPATCHER_ACTIVITY = new ComponentName(
            "com.android.car.media",
            "com.android.car.media.MediaDispatcherActivity"
    );

    static final String CAR_MEDIA_DATA_SCHEME = "custom";

    @VisibleForTesting
    static final String KEY_PREV_POPUP_DATE =
            "com.android.car.systemui.car.qc.PREV_DATE";
    @VisibleForTesting
    static final String KEY_PREV_POPUP_CYCLE =
            "com.android.car.systemui.car.qc.PREV_CYCLE";
    @VisibleForTesting
    static final String KEY_PREV_POPUP_ACTIVE_DAYS =
            "com.android.car.systemui.car.qc.PREV_ACTIVE_DAYS";
    @VisibleForTesting
    static final String KEY_PREV_POPUP_STATUS =
            "com.android.car.systemui.car.qc.PREV_STATUS";

    @SuppressLint("MissingPermission")
    public DataSubscriptionController(Context context,
            DataSubscriptionMessageCreator dataSubscriptionMessageCreator) {
        mContext = context;
        mSubscription = new DataSubscription(context);
        mMainHandler = new Handler(Looper.getMainLooper());
        mBackgroundExecutor = Executors.newSingleThreadScheduledExecutor();
        mIntent = new Intent(DATA_SUBSCRIPTION_ACTION);
        mIntent.setPackage(mContext.getString(
                R.string.connectivity_flow_app));
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mConnectivityManager = mContext.getSystemService(ConnectivityManager.class);
        mNetworkCallback = new DataSubscriptionNetworkCallback();
        mActivitiesBlocklist = new HashSet<>();
        mPackagesBlocklist = new HashSet<>();

        Resources res = mContext.getResources();
        String[] blockActivities = res.getStringArray(
                R.array.config_dataSubscriptionBlockedActivitiesList);
        mActivitiesBlocklist.addAll(List.of(blockActivities));
        String[] blockComponents = res.getStringArray(
                R.array.config_dataSubscriptionBlockedPackagesList);
        mPackagesBlocklist.addAll(List.of(blockComponents));
        mSharedPreferences = mContext.getSharedPreferences(
                DATA_SUBSCRIPTION_SHARED_PREFERENCE_PATH, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mDataSubscriptionMessageCreator = dataSubscriptionMessageCreator;
        mUxrPrompt = mDataSubscriptionMessageCreator.getUxrPrompt(
            CarUxRestrictionsUtil.getInstance(mContext).getCurrentRestrictions()
                .isRequiresDistractionOptimization());
        mDataSubscriptionStatsLogHelper = new DataSubscriptionStatsLogHelper();
    }

    void updateShouldDisplayProactiveMessage() {
        // Check if all the configs are valid for this drive cycle, this value should remain
        // the same until the next drive cycle
        mShouldDisplayProactiveMessage = isValidTimeInterval()
                && isValidCycle()
                && isValidActiveDays();
        if (mShouldDisplayProactiveMessage && mDataSubscriptionMessageEventListener != null) {
            String message = mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                    mCurrentStatus);
            boolean isMessageDisplayed =
                    mDataSubscriptionMessageEventListener.onDataSubscriptionStatusChanged(
                        mIsDistractionOptimizationRequired, message, mUxrPrompt);
            if (isMessageDisplayed) {
                writeLatestPopupDate();
                writeLatestPopupCycle();
                writeLatestPopupActiveDays();
                mDataSubscriptionStatsLogHelper.logProactiveMessageLaunched();
            }
        }
    }

    private void updateShouldDisplayReactiveMessageForApp(CharSequence appLabel,
                                                          String topActivity) {
        mShouldDisplayReactiveMessage = mNetworkCapabilities == null
                || (!isSuspendedNetwork() && !isValidNetwork());
        if (mShouldDisplayReactiveMessage && mDataSubscriptionMessageEventListener != null) {
            String message = mDataSubscriptionMessageCreator.getReactiveMessageForStatus(
                    mCurrentStatus, appLabel);
            boolean isMessageDisplayed = mDataSubscriptionMessageEventListener.onAppForegrounded(
                    mIsDistractionOptimizationRequired, message, mUxrPrompt);
            if (isMessageDisplayed) {
                mActivitiesBlocklist.add(topActivity);
                mDataSubscriptionStatsLogHelper.logReactiveMessageLaunched();
            }
        }
    }

    /** Register needed listeners */
    @Override
    public void registerListeners() {
        mSubscription.addDataSubscriptionListener(this);
        try {
            ActivityTaskManager.getService().registerTaskStackListener(mTaskStackListener);
        } catch (Exception e) {
            Log.e(TAG, "error while registering TaskStackListener " + e);
        }
        if (!mIsUxRestrictionsListenerRegistered) {
            CarUxRestrictionsUtil.getInstance(mContext).register(
                    mUxRestrictionsChangedListener);
            mIsUxRestrictionsListenerRegistered = true;
        }
        updateCurrentInterval();
        updateCurrentCycle();
        updateCurrentActiveDays();
        updateCurrentStatus();
        updateShouldDisplayProactiveMessage();
    }

    /** Unregister active listeners */
    @Override
    public void unregisterListeners() {
        mSubscription.removeDataSubscriptionListener();
        try {
            ActivityTaskManager.getService().unregisterTaskStackListener(mTaskStackListener);
        } catch (Exception e) {
            Log.e(TAG, "error while unregistering TaskStackListener " + e);
        }
        if (mIsUxRestrictionsListenerRegistered) {
            CarUxRestrictionsUtil.getInstance(mContext).unregister(
                    mUxRestrictionsChangedListener);
            mIsUxRestrictionsListenerRegistered = false;
        }
    }

    @Override
    public void setDataSubscriptionMessageEventListener(
            DataSubscriptionMessageEventListener dataSubscriptionMessageEventListener) {
        mDataSubscriptionMessageEventListener = dataSubscriptionMessageEventListener;
    }

    boolean isValidNetwork() {
        return mNetworkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    boolean isSuspendedNetwork() {
        return !mNetworkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED);
    }

    @Override
    public void onStatusChanged(int value) {
        updateCurrentStatus();
        // Check to display proactive message again. Since this is in the same cycle, we don't need
        // to check for other configs but we need to keep track of the configs' latest updates
        if (mShouldDisplayProactiveMessage && mDataSubscriptionMessageEventListener != null) {
            String message = mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                    mCurrentStatus);
            boolean isMessageDisplayed =
                    mDataSubscriptionMessageEventListener.onDataSubscriptionStatusChanged(
                            mIsDistractionOptimizationRequired, message, mUxrPrompt);
            if (isMessageDisplayed) {
                writeLatestPopupDate();
                writeLatestPopupCycle();
                writeLatestPopupActiveDays();
            }
        }
    }


    public class DataSubscriptionNetworkCallback extends ConnectivityManager.NetworkCallback {
        Network mNetwork;
        String mTopActivity;

        @Override
        public void onAvailable(@NonNull Network network) {
            if (DEBUG) {
                Log.d(TAG, "onAvailable " + network);
            }
            mNetwork = network;
            mLatch.countDown();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network,
                @NonNull NetworkCapabilities networkCapabilities) {
            if (DEBUG) {
                Log.d(TAG, "onCapabilitiesChanged " + network);
            }
            mNetwork = network;
            mNetworkCapabilities = networkCapabilities;
            updateShouldDisplayReactiveMessageForApp(mTopLabel, mTopActivity);
        }
    }

    @VisibleForTesting
    @Override
    public void setUserId(int userId) {
        mUserId = userId;
    }

    private boolean isValidTimeInterval() {
        return mCurrentInterval >= mContext.getResources().getInteger(
                R.integer.data_subscription_pop_up_frequency);
    }

    private boolean isValidCycle() {
        if (mCurrentCycle == 1) {
            return true;
        }
        return mCurrentCycle <= mContext.getResources().getInteger(
                R.integer.data_subscription_pop_up_startup_cycle_limit);
    }

    private boolean isValidActiveDays() {
        if (mCurrentActiveDays == 1) {
            return true;
        }
        return mCurrentActiveDays <= mContext.getResources().getInteger(
                R.integer.data_subscription_pop_up_active_days_limit);
    }

    @VisibleForTesting
    void updateCurrentStatus() {
        int prevStatus = mSharedPreferences.getInt(KEY_PREV_POPUP_STATUS, 0);
        mCurrentStatus = mSubscription.getDataSubscriptionStatus();
        // if the data subscription changes from inactive to paid, we want to reset the caches
        if (prevStatus != mCurrentStatus && !mSubscription.isDataSubscriptionInactive()) {
            mEditor.clear();
            mEditor.apply();
        }
        mEditor.putInt(KEY_PREV_POPUP_STATUS, mCurrentStatus);
        mEditor.apply();
    }

    private void updateCurrentInterval() {
        mCurrentInterval = mContext.getResources().getInteger(
                R.integer.data_subscription_pop_up_frequency);
        String prevDate = mSharedPreferences.getString(KEY_PREV_POPUP_DATE, /* defValue=*/ "");
        if (!TextUtils.isEmpty(prevDate)) {
            mCurrentInterval = (int) ChronoUnit.DAYS.between(LocalDate.parse(prevDate),
                    LocalDate.now(ZoneId.systemDefault()));
        }
    }

    private void updateCurrentCycle() {
        mCurrentCycle = mSharedPreferences.getInt(
                KEY_PREV_POPUP_CYCLE, /* defValue=*/ 0);
    }

    private void updateCurrentActiveDays() {
        mCurrentActiveDays = mSharedPreferences.getInt(
                KEY_PREV_POPUP_ACTIVE_DAYS, /* defValue=*/ 0);
    }

    private void writeLatestPopupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mContext.getString(
                R.string.config_dataSubscriptionDateTimeFormat));
        LocalDate newDate = LocalDate.now(ZoneId.systemDefault());
        String formattedNewDate = newDate.format(formatter);
        mEditor.putString(KEY_PREV_POPUP_DATE, formattedNewDate);
        mEditor.apply();
    }

    private void writeLatestPopupCycle() {
        mEditor.putInt(KEY_PREV_POPUP_CYCLE, mSharedPreferences.getInt(
                KEY_PREV_POPUP_CYCLE, /* defValue=*/ 1) + 1);
        mEditor.apply();
    }

    private void writeLatestPopupActiveDays() {
        mEditor.putInt(KEY_PREV_POPUP_ACTIVE_DAYS, mSharedPreferences.getInt(
                KEY_PREV_POPUP_ACTIVE_DAYS, /* defValue=*/ 1) + 1);
        mEditor.apply();
    }

    @VisibleForTesting
    void setSubscription(DataSubscription dataSubscription) {
        mSubscription = dataSubscription;
    }

    @VisibleForTesting
    boolean getShouldDisplayProactiveMessage() {
        return mShouldDisplayProactiveMessage;
    }

    @VisibleForTesting
    void setPackagesBlocklist(Set<String> list) {
        mPackagesBlocklist = list;
    }

    @VisibleForTesting
    void setActivitiesBlocklist(Set<String> list) {
        mActivitiesBlocklist = list;
    }

    @VisibleForTesting
    void setConnectivityManager(ConnectivityManager connectivityManager) {
        mConnectivityManager = connectivityManager;
    }

    @VisibleForTesting
    TaskStackListener getTaskStackListener() {
        return mTaskStackListener;
    }

    @VisibleForTesting
    boolean getShouldDisplayReactiveMessage() {
        return mShouldDisplayReactiveMessage;
    }

    @VisibleForTesting
    void setNetworkCallback(DataSubscriptionNetworkCallback callback) {
        mNetworkCallback = callback;
    }

    @VisibleForTesting
    void setIsCallbackRegistered(boolean value) {
        mIsNetworkCallbackRegistered = value;
    }

    @VisibleForTesting
    void setIsUxRestrictionsListenerRegistered(boolean value) {
        mIsUxRestrictionsListenerRegistered = value;
    }

    @VisibleForTesting
    void setSharedPreference(SharedPreferences sharedPreference) {
        mSharedPreferences = sharedPreference;
    }

    @VisibleForTesting
    void setCurrentInterval(int currentInterval) {
        mCurrentInterval = currentInterval;
    }

    @VisibleForTesting
    void setCurrentCycle(int cycle) {
        mCurrentCycle = cycle;
    }

    @VisibleForTesting
    void setCurrentActiveDays(int activeDays) {
        mCurrentActiveDays = activeDays;
    }

    @VisibleForTesting
    void setEditor(SharedPreferences.Editor editor) {
        mEditor = editor;
    }

    private ComponentName getMediaComponentName(ActivityManager.RunningTaskInfo taskInfo) {
        Uri data = taskInfo.baseIntent.getData();
        if (data == null) {
            if (DEBUG) Log.d(TAG, "No data attached to the base intent");
            return null;
        }
        if (!CAR_MEDIA_DATA_SCHEME.equals(data.getScheme())) {
            if (DEBUG) Log.d(TAG, "Data scheme doesn't match");
            return null;
        }
        // should drop the first backslash that is part of the schemeSpecificPart
        String ssp = data.getSchemeSpecificPart();
        String mediaComponentString = ssp.startsWith("/") ? ssp.substring(1) : ssp;
        ComponentName mediaComponent = ComponentName.unflattenFromString(mediaComponentString);

        if (DEBUG) Log.d(TAG, "Media component found: " + mediaComponent);
        return mediaComponent;
    }

    private boolean isMediaComponent(ComponentName component) {
        return CAR_MEDIA_ACTIVITY.equals(component)
                || CAR_MEDIA_DISPATCHER_ACTIVITY.equals(component);
    }
}
