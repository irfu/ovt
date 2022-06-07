/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2RawDataSourceTestEmulator.java,v $
 Date:      $Date: 2015/10/23 15:21:00 $
 Version:   $Revision: 1.00 $


 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P. G. Johansson, Fredrik Johansson)
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification is permitted provided that the following conditions are met:

 * No part of the software can be included in any commercial package without
 written consent from the OVT team.

 * Redistributions of the source or binary code must retain the above
 copyright notice, this list of conditions and the following disclaimer.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
 IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT OR
 INDIRECT DAMAGES  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.

 OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson

 =========================================================================*/
package ovt.mag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ovt.datatype.Time;
import ovt.mag.OMNI2Data.FieldID;
import ovt.util.Log;
import ovt.util.Utils;

/**
 * Alternative implementation of OMNI2RawDataSource for test purposes. OVT can
 * use this instead of the actual OMNI2 raw data source.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 */
// PROPOSAL: Rewrite into a more general-purpose emulator based on one instance of OMNI2Data?
//    PRO: Could be used by OMNI2DataSourceTest.
public class OMNI2RawDataSourceTestEmulator implements OMNI2RawDataSource {

    private final int yearMin;
    private final int yearMax;
    private final OMNI2Data data;
    private static final int IO_ERROR_FREQUENCY = 2;
    private static final int IO_ERROR_FIRST = 2;
    private int callCounter = 0;


    public OMNI2RawDataSourceTestEmulator() {
        data = createDefaultTestData();
        final double[] timeInterval_mjd = data.getInterval();
        yearMin = Time.getYear(timeInterval_mjd[0]);
        yearMax = Time.getYear(timeInterval_mjd[1]);
    }


    @Override
    public OMNI2Data getData_hourlyAvg(int year) throws IOException {
        Log.log(getClass().getSimpleName()+"#getData_hourlyAvg("+year+")");
        callCounter++;
        if ((IO_ERROR_FREQUENCY > 0) && (callCounter >= IO_ERROR_FIRST) && (callCounter % IO_ERROR_FREQUENCY == 0)) {
            throw new IOException(getClass().getSimpleName()+"#getData_hourlyAvg : TEST IOException");
        }

        final double beginMjd = Time.getMjd(year, 1, 1, 0, 0, 0);
        final double endMjd = Time.getMjd(year + 1, 1, 1, 0, 0, 0);

        if (beginMjd < endMjd) {
            return (OMNI2Data) data.createNew(beginMjd, endMjd);
        } else {
            return new OMNI2Data(beginMjd, endMjd);   // Return empty data.
        }
    }


    @Override
    public int[] getYearMinMax_hourlyAvg() {
        return new int[]{yearMin, yearMax};
    }


    private static double getTestDataScalar(FieldID fieldID, int i) {
        final int r = fieldID.ordinal();
        final double k = Math.sqrt(2) * r;
        final double x = Math.sin(i) + 1 / k * Math.sin(k * i);
        return x;
    }


    private static double[] getTestDataFieldArray(FieldID fieldID, int N) {
        final double[] y = new double[N];
        for (int i = 0; i < N; i++) {
            y[i] = getTestDataScalar(fieldID, i);
        }
        return y;
    }


    private static OMNI2Data createDefaultTestData() {
        final double timeMin_mjd = Time.getMjd(1990, 10, 01, 0, 0, 0);
        final double timeMax_mjd = Time.getMjd(2010, 10, 01, 0, 0, 0);
        final double[] time_mjd = Utils.newLinearArray(
                timeMin_mjd, timeMax_mjd - 1 / 24.0,
                (int) (timeMax_mjd - timeMin_mjd) * 24);  // NOTE: Must exclude end boundary since it is exclusive.

        final Map<OMNI2Data.FieldID, double[]> dataFields = new HashMap();

        // Create "data".
        final int N = time_mjd.length;
        for (FieldID ID : FieldID.values()) {
            if (ID == FieldID.time_mjd) {
                dataFields.put(ID, time_mjd);
            } else {
                dataFields.put(ID, getTestDataFieldArray(ID, N));
            }
        }

        return new OMNI2Data(timeMin_mjd, timeMax_mjd, dataFields);
    }

    // Test code
//    public static void main(String[] args) {
//        final OMNI2Data data = createDefaultTestData();
//    }
}
