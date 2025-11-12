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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DataSubscriptionControllerTest {
    private Context mContext;
    private DataSubscriptionController mController;
    @Mock
    private DataSubscriptionMessageEventListener mDataSubscriptionMessageEventListener;
    @Mock
    private ConnectivityManager mConnectivityManager;
    @Mock
    private PackageManager mPackageManager;
    @Mock
    private DataSubscription mDataSubscription;
    @Mock
    private SharedPreferences mSharedPreferences;
    @Mock
    private SharedPreferences.Editor mEditor;
    @Mock
    private DataSubscriptionMessageCreator mDataSubscriptionMessageCreator;
    @Mock
    private ApplicationInfo mMockApplicationInfo;

    @Captor
    private ArgumentCaptor<ConnectivityManager.NetworkCallback> mCallbackCaptor;

    private ActivityManager.RunningTaskInfo mRunningTaskInfoMock;
    @Before
    public void setUp() throws PackageManager.NameNotFoundException {
        MockitoAnnotations.initMocks(this);
        mContext = spy(InstrumentationRegistry.getInstrumentation().getContext());
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        when(mContext.getSystemService(ConnectivityManager.class)).thenReturn(mConnectivityManager);

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = new String[]{ACCESS_NETWORK_STATE, INTERNET};
        mMockApplicationInfo.uid = 1000;

        when(mPackageManager.getPackageInfoAsUser(anyString(), anyInt(), anyInt()))
                .thenReturn(packageInfo);
        when(mPackageManager.getApplicationInfoAsUser(anyString(), anyInt(), anyInt()))
                .thenReturn(mMockApplicationInfo);
        when(mPackageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo);
        when(mPackageManager.getApplicationInfo(anyString(), anyInt()))
                .thenReturn(mMockApplicationInfo);
        when(mMockApplicationInfo.loadLabel(any(PackageManager.class))).thenReturn("Test App");


        mController = new DataSubscriptionController(mContext, mDataSubscriptionMessageCreator);
        mController.setSubscription(mDataSubscription);
        mController.setConnectivityManager(mConnectivityManager);
        mController.setSharedPreference(mSharedPreferences);
        mController.setDataSubscriptionMessageEventListener(
                mDataSubscriptionMessageEventListener);
        mController.setEditor(mEditor);
        mController.setUserId(1000);
        mRunningTaskInfoMock = new ActivityManager.RunningTaskInfo();
        mRunningTaskInfoMock.topActivity = new ComponentName("testPkgName", "testClassName");
        mRunningTaskInfoMock.taskId = 1;
        mRunningTaskInfoMock.baseIntent = new Intent();
        when(mSharedPreferences.edit()).thenReturn(mEditor);
        when(mEditor.putInt(anyString(), anyInt())).thenReturn(mEditor);
        when(mEditor.putString(anyString(), anyString())).thenReturn(mEditor);
        when(mEditor.clear()).thenReturn(mEditor);
    }

    @Test
    public void onTaskMovedToFront_TopPackageBlocked_popUpNotDisplay() throws RemoteException {
        HashSet<String> packagesBlocklist = new HashSet<>();
        packagesBlocklist.add(mRunningTaskInfoMock.topActivity.getPackageName());
        mController.setPackagesBlocklist(packagesBlocklist);

        mController.getTaskStackListener().onTaskMovedToFront(mRunningTaskInfoMock);

        assertFalse(mController.getShouldDisplayReactiveMessage());
    }

    @Test
    public void onTaskMovedToFront_TopActivityBlocked_popUpNotDisplay() throws RemoteException {
        HashSet<String> activitiesBlocklist = new HashSet<>();
        activitiesBlocklist.add(mRunningTaskInfoMock.topActivity.flattenToString());
        mController.setActivitiesBlocklist(activitiesBlocklist);

        mController.getTaskStackListener().onTaskMovedToFront(mRunningTaskInfoMock);

        assertFalse(mController.getShouldDisplayReactiveMessage());
    }

    @Test
    public void onTaskMovedToFront_AppNotRequireInternet_popUpNotDisplay()
            throws RemoteException, PackageManager.NameNotFoundException {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.requestedPermissions = new String[]{}; // No internet permission
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        when(mPackageManager.getPackageInfoAsUser(
                anyString(), anyInt(), anyInt())).thenReturn(packageInfo);

        mController.getTaskStackListener().onTaskMovedToFront(mRunningTaskInfoMock);

        assertFalse(mController.getShouldDisplayReactiveMessage());
    }

    @Test
    public void onTaskMovedToFront_AppRequiresInternetAndNotBlocked_registerCallback()
            throws RemoteException, PackageManager.NameNotFoundException {
        mController.getTaskStackListener().onTaskMovedToFront(mRunningTaskInfoMock);

        verify(mConnectivityManager).registerDefaultNetworkCallbackForUid(anyInt(), any(), any());
    }

    @Test
    public void onTaskMovedToFront_twoCalls_registersTwoSeparateCallbacks() throws RemoteException {
        // Arrange: Create two different task infos to simulate two different apps
        ActivityManager.RunningTaskInfo taskInfo1 = new ActivityManager.RunningTaskInfo();
        taskInfo1.topActivity = new ComponentName("com.app.one", ".MainActivity");
        taskInfo1.baseIntent = new Intent();

        ActivityManager.RunningTaskInfo taskInfo2 = new ActivityManager.RunningTaskInfo();
        taskInfo2.topActivity = new ComponentName("com.app.two", ".MainActivity");
        taskInfo2.baseIntent = new Intent();


        // Act: Simulate both apps moving to the front in quick succession
        mController.getTaskStackListener().onTaskMovedToFront(taskInfo1);
        mController.getTaskStackListener().onTaskMovedToFront(taskInfo2);

        // Assert
        // 1. Verify register was called twice
        verify(mConnectivityManager, times(2)).registerDefaultNetworkCallbackForUid(
                anyInt(), mCallbackCaptor.capture(), any(Handler.class));

        // 2. Get the captured callbacks
        List<ConnectivityManager.NetworkCallback> capturedCallbacks =
                mCallbackCaptor.getAllValues();

        // 3. Assert that we captured two distinct callbacks
        assertEquals(2, capturedCallbacks.size());
        assertNotSame("Callbacks should be different instances",
                capturedCallbacks.get(0), capturedCallbacks.get(1));
    }


    @Test
    public void updateShouldDisplayProactiveMessage_noCachedTimeInterval_popUpDisplay() {
        when(mSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("2025-01-15");
        when(mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                anyInt())).thenReturn("Valid Message");
        mController.setCurrentInterval(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_frequency) + 1);

        mController.updateShouldDisplayProactiveMessage();

        assertTrue(mController.getShouldDisplayProactiveMessage());
    }

    @Test
    public void updateShouldDisplayProactiveMessage_allConfigsAreValid_popUpDisplay() {
        when(mSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("2025-01-15");
        when(mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                anyInt())).thenReturn("Valid Message");

        mController.setCurrentInterval(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_frequency));
        mController.setCurrentCycle(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_startup_cycle_limit));
        mController.setCurrentActiveDays(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_active_days_limit));

        mController.updateShouldDisplayProactiveMessage();

        assertTrue(mController.getShouldDisplayProactiveMessage());
    }

    @Test
    public void updateShouldDisplayProactiveMessage_invalidTimeInterval_popUpNotDisplay() {
        when(mSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("2025-01-15");
        when(mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                anyInt())).thenReturn("");

        mController.setCurrentInterval(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_frequency) - 1);
        mController.setCurrentCycle(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_startup_cycle_limit));

        mController.updateShouldDisplayProactiveMessage();

        assertFalse(mController.getShouldDisplayProactiveMessage());
    }

    @Test
    public void updateShouldDisplayProactiveMessage_invalidCycle_popUpNotDisplay() {
        when(mSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("2025-01-15");
        when(mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                anyInt())).thenReturn("");

        mController.setCurrentInterval(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_frequency));
        mController.setCurrentCycle(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_startup_cycle_limit));
        mController.setCurrentCycle(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_startup_cycle_limit) + 1);

        mController.updateShouldDisplayProactiveMessage();

        assertFalse(mController.getShouldDisplayProactiveMessage());
    }

    @Test
    public void updateShouldDisplayProactiveMessage_invalidActiveDays_popUpNotDisplay() {
        when(mSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("2025-01-15");
        when(mDataSubscriptionMessageCreator.getProactiveMessageForStatus(
                anyInt())).thenReturn("");

        mController.setCurrentInterval(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_frequency));
        mController.setCurrentCycle(mContext.getResources()
                .getInteger(R.integer.data_subscription_pop_up_startup_cycle_limit) + 1);

        mController.updateShouldDisplayProactiveMessage();

        assertFalse(mController.getShouldDisplayProactiveMessage());
    }

    @Test
    public void updateShouldDisplayProactiveMessage_resetStatus_clearPreviousPreferences() {
        when(mSharedPreferences.getInt(anyString(), anyInt()))
                .thenReturn(0);
        when(mDataSubscription.getDataSubscriptionStatus())
                .thenReturn(1);

        mController.updateCurrentStatus();

        verify(mEditor).clear();
    }
}
