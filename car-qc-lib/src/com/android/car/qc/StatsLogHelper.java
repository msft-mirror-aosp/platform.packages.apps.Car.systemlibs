/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.car.qc;

import static com.android.car.qc.QCItem.QC_ACTION_SLIDER_VALUE;
import static com.android.car.qc.QCItem.QC_ACTION_TOGGLE_STATE;
import static com.android.car.qc.QCItem.QC_TYPE_ACTION_SWITCH;
import static com.android.car.qc.QCItem.QC_TYPE_ACTION_TOGGLE;
import static com.android.car.qc.QCItem.QC_TYPE_ROW;
import static com.android.car.qc.QCItem.QC_TYPE_SLIDER;
import static com.android.car.qc.QCItem.QC_TYPE_TILE;

import android.annotation.IntDef;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class StatsLogHelper {
    private static final String TAG = StatsLogHelper.class.getSimpleName();
    private static StatsLogHelper sInstance;
    private static final int DEFAULT_VALUE = -1;
    private static final boolean DEFAULT_STATE = false;


    /**
     * IntDef representing enum values of CarQcLibEventReported.element_type.
     */
    @IntDef({
        QcElementType.UNSPECIFIED_ELEMENT_TYPE,
        QcElementType.QC_TYPE_LIST,
        QcElementType.QC_TYPE_ROW,
        QcElementType.QC_TYPE_TILE,
        QcElementType.QC_TYPE_SLIDER,
        QcElementType.QC_TYPE_ACTION_SWITCH,
        QcElementType.QC_TYPE_ACTION_TOGGLE,
    })

    public @interface QcElementType {
        int UNSPECIFIED_ELEMENT_TYPE =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__UNSPECIFIED_ELEMENT_TYPE;
        int QC_TYPE_LIST =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_LIST;
        int QC_TYPE_ROW =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ROW;
        int QC_TYPE_TILE =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_TILE;
        int QC_TYPE_SLIDER =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_SLIDER;
        int QC_TYPE_ACTION_SWITCH =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ACTION_SWITCH;
        int QC_TYPE_ACTION_TOGGLE =
                CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ACTION_TOGGLE;
    }

    /**
     * Returns the current logging instance of StatsLogHelper to write this devices'
     * CarQcLibStatsModule.
     *
     * @return the logging instance of StatsLogHelper.
     */
    public static StatsLogHelper getInstance() {
        if (sInstance == null) {
            sInstance = new StatsLogHelper();
        }
        return sInstance;
    }

    /**
     * Writes to CarQcLibEvent atom with all the optional fields filled.
     *
     * @param qcHashedTag         the tag of the QC
     * @param qcElementType one of {@link QcElementType}
     * @param qcValue         the current value of the QC element
     * @param qcState         the current state of the QC element
     */
    private void writeCarQcLibEventReported(int packageUid, String qcHashedTag, int qcElementType,
            int qcValue, boolean qcState) {
        if (Build.isDebuggable()) {
            Log.v(TAG, "writing CAR_QC_LIB_EVENT_REPORTED. packageUid=" + packageUid
                    + ", qcHashedTag=" + qcHashedTag + ", qcElementType= " + qcElementType
                    + ", qcValue=" + qcValue + ", qcState=" + qcState);
        }
        CarQcLibStatsLog.write(
            /* atomId */ CarQcLibStatsLog.CAR_QC_LIB_EVENT_REPORTED,
            /* packageUid */ packageUid,
            /* qcHashedTag */ qcHashedTag,
            /* qcElementType */ qcElementType,
            /* qcValue */ qcValue,
            /* qcState */ qcState);
    }

    /**
     * Logs that there is an interaction on QC elements
     */
    public void logMetrics(QCItem item, Intent intent) {
        // if we can't find package uid or tag, we don't need any metrics
        if (item.getPackageUid() == 0 || item.getTag().isEmpty()) {
            return;
        }
        int value = DEFAULT_VALUE;
        boolean state = DEFAULT_STATE;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String type = item.getType();
                if (type.equals(QC_TYPE_ACTION_SWITCH)
                        || type.equals(QC_TYPE_TILE)
                        || type.equals(QC_TYPE_ACTION_TOGGLE)) {
                    state = bundle.getBoolean(QC_ACTION_TOGGLE_STATE);
                } else if (item.getType().equals(QC_TYPE_SLIDER)) {
                    int i = bundle.getInt(QC_ACTION_SLIDER_VALUE);
                    value = ((QCSlider) item).getSliderValueInPercentage(i);
                }
            }
            writeCarQcLibEventReported(item.getPackageUid(), item.getTag(),
                    convertStringToIntQcType(item.getType()),
                    /* value= */ value, /* state= */ state);
            return;
        }
        if (item.getType().equals(QC_TYPE_ROW) || item.getType().equals(QC_TYPE_TILE)) {
            if (item.getDisabledClickAction() != null || item.getPrimaryAction() != null) {
                writeCarQcLibEventReported(item.getPackageUid(), item.getTag(),
                        convertStringToIntQcType(item.getType()),
                        /* value= */ DEFAULT_VALUE, /* state= */ DEFAULT_STATE);
            }
        }
    }

    private int convertStringToIntQcType(String qcType) {
        switch (qcType) {
            case QCItem.QC_TYPE_LIST -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_LIST;
            }
            case QC_TYPE_ROW -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ROW;
            }
            case QCItem.QC_TYPE_TILE -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_TILE;
            }
            case QCItem.QC_TYPE_SLIDER -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_SLIDER;
            }
            case QCItem.QC_TYPE_ACTION_SWITCH -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ACTION_SWITCH;
            }
            case QCItem.QC_TYPE_ACTION_TOGGLE -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__QC_TYPE_ACTION_TOGGLE;
            }
            default -> {
                return CarQcLibStatsLog
                    .CAR_QC_LIB_EVENT_REPORTED__ELEMENT_TYPE__UNSPECIFIED_ELEMENT_TYPE;
            }
        }
    }
}
