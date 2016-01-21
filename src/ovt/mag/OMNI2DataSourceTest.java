/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2DataSourceTest.java $
 Date:      $Date: 2015/10/16 13:16:00 $
 Version:   $Revision: 1.0 $
 
 
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
 
 OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
 =========================================================================*/
package ovt.mag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ovt.datatype.Time;
import ovt.mag.OMNI2Data.FieldID;
import ovt.util.Log;

/**
 * Test code for OMNI2DataSource. Somewhat hardcoded test list.
 *
 * @since 2015-10-16
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 */
public class OMNI2DataSourceTest {

    private static class OMNI2RawDataSourceTestEmulator implements OMNI2RawDataSource {

        private final int yearMin;
        private final int yearMax;
        private final OMNI2Data data;


        public OMNI2RawDataSourceTestEmulator(int mYearMin, int mYearMax, Map<OMNI2Data.FieldID, double[]> dataFields) {
            yearMin = mYearMin;
            yearMax = mYearMax;
            data = new OMNI2Data(
                    Time.getMjd(mYearMin, 1, 1, 0, 0, 0),
                    Time.getMjd(mYearMax + 1, 1, 1, 0, 0, 0),
                    dataFields
            );
        }


        @Override
        public OMNI2Data getData_hourlyAvg(int year) throws IOException {
            //final double[] interval = data.getInterval();
            final double beginMjd = Time.getMjd(year, 1, 1, 0, 0, 0);
            final double endMjd = Time.getMjd(year + 1, 1, 1, 0, 0, 0);

            if (beginMjd < endMjd) {
                return (OMNI2Data) data.createNew(beginMjd, endMjd);
            } else {
                return new OMNI2Data(beginMjd, endMjd);   // Return empty data.
            }
            //return new OMNI2Data(beginMjd, endMjd);
        }


        @Override
        public int[] getYearMinMax_hourlyAvg() {
            return new int[]{yearMin, yearMax};
        }

        /*public double[] getValues(double mjd) {
         //OMNI2Data.
         data.selectSubset(DOUBLE_FILL_VALUE, DOUBLE_FILL_VALUE)
         }*/
    }

    //##########################################################################

    // NOTE: Does not cover magnetic field vector.
    private static double getTestDataScalar(FieldID fieldID, int i) {
        final int r = fieldID.ordinal();
        final double k = Math.sqrt(2) * r;
        final double x = Math.sin(i) + 1 / k * Math.sin(k * i);
        return x;
    }


    /**
     * Generate test data for a specific field and index in an array.<BR>
     * NOTE: Originally, we did not generate a data point for a specific time
     * since we wanted to be able to change the time without changing the value.
     * That might not be true/important anymore.
     */
    private static double[] getTestDataValues(FieldID fieldID, boolean getIMFVector, int i) {
        if (getIMFVector) {
            return new double[]{
                getTestDataScalar(FieldID.IMFx_nT_GSM_GSE, i),
                getTestDataScalar(FieldID.IMFy_nT_GSM, i),
                getTestDataScalar(FieldID.IMFz_nT_GSM, i)
            };
        } else {
            return new double[]{getTestDataScalar(fieldID, i)};
        }

    }


    private static double[] getTestDataFieldArray(FieldID fieldID, int N) {
        final double[] y = new double[N];
        for (int i = 0; i < N; i++) {
            y[i] = getTestDataScalar(fieldID, i);
        }
        return y;
    }


    /**
     * Create an OMNI2DataSource with test data based on #getTestDataFieldArray.
     */
    private static OMNI2DataSource createOMNI2DataSource(double mMaxTimeDifference) {
        final double[] time_mjd = new double[]{
            Time.getMjd(2000, 1, 01, 0, 0, 0), // index 0
            Time.getMjd(2000, 2, 01, 0, 0, 0),
            Time.getMjd(2000, 3, 01, 0, 0, 0),
            Time.getMjd(2004, 4, 01, 0, 0, 0), // index 3
            Time.getMjd(2005, 5, 01, 0, 0, 0), // index 4
            Time.getMjd(2005, 6, 01, 0, 0, 0),
            Time.getMjd(2005, 7, 01, 0, 0, 0), // index 6
            Time.getMjd(2009, 8, 01, 0, 0, 0),
            Time.getMjd(2009, 9, 01, 0, 0, 0),
            Time.getMjd(2010, 10, 01, 0, 0, 0) // index 9
        };
        final Map<OMNI2Data.FieldID, double[]> dataFields = new HashMap();
        dataFields.put(OMNI2Data.FieldID.time_mjd, time_mjd);
        //System.out.println("Times (mjd) = " + Arrays.toString(time_mjd));

        // Create "data".
        final int N = time_mjd.length;
        for (FieldID ID : FieldID.values()) {
            if (ID == FieldID.time_mjd) {
                continue;
            }

            dataFields.put(ID, getTestDataFieldArray(ID, N));
        }

        // Replace some data values with fill values.
        dataFields.get(FieldID.DST)[0] = OMNI2RawDataSource.DOUBLE_FILL_VALUE;
        dataFields.get(FieldID.IMFx_nT_GSM_GSE)[0] = OMNI2RawDataSource.DOUBLE_FILL_VALUE;
        dataFields.get(FieldID.IMFz_nT_GSM)[5] = OMNI2RawDataSource.DOUBLE_FILL_VALUE;

        final OMNI2RawDataSourceTestEmulator rawDataSrc = new OMNI2RawDataSourceTestEmulator(2000, 2010, dataFields);
        final OMNI2DataSource dataSrc = new OMNI2DataSource(rawDataSrc, mMaxTimeDifference);

        return dataSrc;
    }


    public static void main(String[] args) throws OMNI2DataSource.ValueNotFoundException, IOException {
        Log.setLogLevel(2);
        final int N_shufflings = 10;

        final List<Object[]> testList = new ArrayList();
        testList.add(new Object[]{Time.getMjd(1999, 01, 03, 0, 0, 0), FieldID.SW_M_A, false, 0, false});   // Before beginning of data.
        testList.add(new Object[]{Time.getMjd(1999, 01, 03, 0, 0, 0), FieldID.DST, false, 1, true});   // Before beginning of data, and first later data point has fill value, and the following one is too far away.
        testList.add(new Object[]{Time.getMjd(1999, 01, 03, 0, 0, 0), FieldID.SW_M_A, false, 0, false}); // Before beginning of data, and first later data point has fill value.
        testList.add(new Object[]{Time.getMjd(2005, 06, 11, 0, 0, 0), FieldID.SW_M_A, true, 4, false});  // Next lower value is fill value.
        testList.add(new Object[]{Time.getMjd(2000, 3, 31, 23, 59, 59.9), FieldID.SW_M_A, false, 2, false});        // Time just before data point (subsecond).
        testList.add(new Object[]{Time.getMjd(2004, 3, 31, 23, 59, 59.9), FieldID.SW_M_A, false, 3, false});        // Time just before data point (subsecond).
        testList.add(new Object[]{Time.getMjd(2004, 3, 31, 23, 59, 59.9 + 1.0), FieldID.SW_M_A, false, 3, false});  // Time exactly on data point.
        testList.add(new Object[]{Time.getMjd(2007, 1, 31, 0, 0, 0), FieldID.SW_M_A, false, 3, true});  // Data points too far away.
        testList.add(new Object[]{Time.getMjd(2010, 12, 31, 0, 0, 0), FieldID.SW_M_A, false, 9, false});  // After data point range.
        testList.add(new Object[]{Time.getMjd(2020, 12, 31, 0, 0, 0), FieldID.SW_M_A, false, 9, true});  // Many years after data point range.
        testList.add(new Object[]{Time.getMjd(2006, 12, 31, 23, 59, 59.9), FieldID.SW_M_A, false, 6, true}); // Previous data point one year before.

        boolean ok = true;
        for (long randSeed = 0; randSeed < N_shufflings; randSeed++) {
            final Random randGen = new Random(randSeed);
            //System.out.println(randGen.nextInt()+", "+randGen.nextInt()+", "+randGen.nextInt()+", "+randGen.nextInt());
            final List<Object[]> shuffledTestList = new ArrayList(testList);
            Collections.shuffle(shuffledTestList, randGen);

            final OMNI2DataSource dataSrc = createOMNI2DataSource(365);

            for (Object[] test : shuffledTestList) {
                final double mjd = (double) test[0];
                final FieldID fieldID = (FieldID) test[1];
                final boolean getIMFVector = (boolean) test[2];
                final int index = (int) test[3];
                final boolean expectException = (boolean) test[4];

                try {
                    final double[] testValues = dataSrc.getValues(mjd, fieldID, getIMFVector);
                    final double[] actualValues = getTestDataValues(fieldID, getIMFVector, index);
                    ok = ok && Arrays.equals(testValues, actualValues);
                    ok = ok && !expectException;
                } catch (OMNI2DataSource.ValueNotFoundException e) {
                    ok = ok && expectException;
                }
            }
        }

        if (!ok) {
            throw new RuntimeException("######## FAILED TEST ########");
        } else {
            System.out.println("######## TEST OK ########");
        }

    }
}
