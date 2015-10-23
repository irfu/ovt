/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2DataSource.java $
 Date:      $Date: 2015/09/15 13:17:00 $
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import ovt.datatype.Time;
import ovt.mag.OMNI2Data.FieldID;
import ovt.util.Log;
import ovt.util.SegmentsCache;
import ovt.util.Utils;

/**
 * Class from which all OMNI2 data can be retrieved and used by the application
 * and without knowledge of the underlying OMNI2 data files and their format.
 * All OMNI2 data used by the application should pass through this class.
 *
 * This class serves as the bridge between<BR>
 * (1) (implementations of) OMNI2RawDataSource (where the interface is partly
 * dependent on the format of the underlying OMNI2 data files and may change if
 * implementing support for other OMNI2 files), and<BR>
 * (2) the rest of OVT (which should be ignorant of the format of the underlying
 * OMNI2 data files), minus the GUI (almost).
 *
 * NOTE: Has no good way of finding the beginning and end time of available
 * data. Does throw proper exception when the code fails to obtain data though.
 *
 * IMPLEMENTATION NOTE: Implemented as an instantiated class to make automated
 * testing without the GUI and an arbitrary implementation of OMNI2RawDataSource
 * (and implicitly arbitrary cache directory) easier.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-xx
 */
public class OMNI2DataSource {

    // Exact value not so important but should probably not be more than
    // the length of time covered by a year or the underlying OMNI2 files (?).
    private static final double MIN_DATA_SOURCE_T_SCALE_DAYS = 1;

    /**
     * Greatest tolerated time (days) between the time for which data is requested, and
     * the time of the data point used.
     */
    private final double maxTimeDifference_days;

    private final static int DEBUG = 4;   // Log level for log messages.

    //##########################################################################
    /**
     * Class which the cache uses as a data source.
     */
    private static class CacheDataSource implements SegmentsCache.DataSource {

        private final OMNI2RawDataSource rawDataSrc;


        private CacheDataSource(OMNI2RawDataSource mRawDataSrc) {
            rawDataSrc = mRawDataSrc;
        }


        /**
         * @param t_begin Mjd.
         * @param t_end Mjd.
         */
        @Override
        public OMNI2Data getSupersetSegment(double t_begin, double t_end) throws IOException {
            if (t_end < t_begin) {
                throw new IllegalArgumentException();
            }
            final int beginYear = new Time(t_begin).getYear();
            final int endYear = new Time(t_end).getYear();    // Inclusive.

            final List<SegmentsCache.DataSegment> dataList = new ArrayList();
            for (int year = beginYear; year <= endYear; year++) {

                /*==========================================
                 This is where data is read from the source.
                 ==========================================*/
                dataList.add(rawDataSrc.getData_hourlyAvg(year));
            }

            return mergeAdjacent(dataList);
        }


        @Override
        public OMNI2Data mergeAdjacent(List<SegmentsCache.DataSegment> segments) {

            // Convert List<SegmentsCache.DataSegment> ==> List<OMNI2Data>
            List<OMNI2Data> dataList = new ArrayList();
            for (SegmentsCache.DataSegment seg : segments) {
                dataList.add((OMNI2Data) seg);
            }

            return OMNI2Data.mergeAdjacent(dataList);
        }
    }

    //##########################################################################
    private final SegmentsCache segmentsCache;


    public OMNI2DataSource(OMNI2RawDataSource rawDataSrc, double mMaxTimeDifference_days) {
        maxTimeDifference_days = mMaxTimeDifference_days;
        
        /*=====================
         Setup cache.
         =====================*/
        // Derive approximate beginning and end of time interval that covers all data.
        final int[] yearMinMax = rawDataSrc.getYearMinMax_hourlyAvg();
        final double searchMin_mjd = Time.getMjd(yearMinMax[0] + 0, 1, 1, 0, 0, 0);
        final double searchMax_mjd = Time.getMjd(yearMinMax[1] + 1, 1, 1, 0, 0, 0);

        segmentsCache = new SegmentsCache(
                new CacheDataSource(rawDataSrc),
                MIN_DATA_SOURCE_T_SCALE_DAYS,
                searchMin_mjd,
                searchMax_mjd);
    }


    /**
     * Get the value relevant for a specific time. Will start at the relevant
     * time and search backwards to a point in time where there is data (not
     * fill value). The function is designed to be "friendly" to MagProps' way
     * of organizing data and therefore returns double[] and treats IMF vector
     * as a special case since OMNI2Data.FieldID can not represent it.
     *
     * NOTE: Will NOT return the time for found value.
     *
     * NOTE: Defines how time should be interpreted, i.e. which data point in
     * time should be used.
     *
     * @param fieldID The scalar field for which the data should be returned, if
     * getIMFvector==false.
     * @param getIMFVector Iff true, then return the IMF vector.
     * @return Value. Never null.
     *
     * @throws ValueNotFoundException when no value was found. IOException for
     * I/O errors.
     */
    // PROPOSAL: Search function outside so can find t interval for global time interval.
    // QUESTION: How find the nearest mjd, not the nearest in a search direction?
    //    NOTE: Not enough to implement Utils.findNearestMatch for rounding-to-nearest due to fill values and segment boundaries.
    //          Must probably search both up and down and selectSubset the nearest one.
    // PROPOSAL: Max distance in time beyond which a found value will yield exception instead of a returned value.
    //    NOTE: Needs different value if searching for the boundaries of OMNI2 data.
    public double[] getValues(double time_mjd, FieldID fieldID, boolean getIMFVector) throws ValueNotFoundException, IOException {

        //=====================================================================
        class SearchFunction implements SegmentsCache.SearchFunction {

            /**
             * Returns null on failure and double[][] on success. [0][0]=the
             * time (mjd) where the result was found, [1]=array of acativity
             * values.
             */
            @Override
            public Object searchDataSegment(
                    SegmentsCache.DataSegment seg,
                    double t_start,
                    SegmentsCache.SearchDirection dir) {

                // Search direction-dependent initialization.
                RoundingMode roundingMode;
                int step;
                if (dir == SegmentsCache.SearchDirection.DOWN) {
                    roundingMode = RoundingMode.FLOOR;
                    step = -1;
                } else {
                    roundingMode = RoundingMode.CEILING;
                    step = +1;
                }

                // DataSegment/OMNI2Data-dependent initialization.
                final OMNI2Data data = (OMNI2Data) seg;
                final double[] timeArray = data.getFieldArrayInternal(FieldID.time_mjd);
                final int i_start = Utils.findNearestMatch(timeArray, time_mjd, roundingMode);
                if ((i_start < 0) || (timeArray.length <= i_start)) {
                    // IMPLEMENTATION NOTE: Could happen(!), if data segment
                    // boundaries extend beyond the highest/lowest data point time.
                    return null;

                }//*/

                if (getIMFVector) {
                    return searchDataSegment_IMF(data, timeArray, i_start, step);
                } else {
                    return searchDataSegment_scalar(data, timeArray, i_start, step, fieldID);
                }
            }


            private double[][] searchDataSegment_scalar(
                    OMNI2Data data, double[] timeArray, int i_start, int step, FieldID mFieldID) {
                final double[] fieldArray = data.getFieldArrayInternal(mFieldID);

                /* Look for the first non-fill value.
                 * ----------------------------------
                 * IMPLEMENTATION NOTE: If NaN/Inf is used as a fill value,
                 * then one must use a comparison which treats them
                 * correctly. One can therefore NOT use the usual "!=" operator.
                 */
                for (int i = i_start; ((0 <= i) & (i < fieldArray.length)); i = i + step) {
                    if (Double.compare(fieldArray[i], OMNI2RawDataSource.DOUBLE_FILL_VALUE) != 0) {
                        // CASE: Not a fill value.
                        return new double[][]{{timeArray[i]}, {fieldArray[i]}};
                    }

                }
                return null;
            }


            private double[][] searchDataSegment_IMF(OMNI2Data data, double[] timeArray, int i_start, int step) {
                final double[] IMFx_GSM_array = data.getFieldArrayInternal(FieldID.IMFx_nT_GSM_GSE);
                final double[] IMFy_GSM_array = data.getFieldArrayInternal(FieldID.IMFy_nT_GSM);
                final double[] IMFz_GSM_array = data.getFieldArrayInternal(FieldID.IMFz_nT_GSM);

                /* Look for the first vector consisting of only non-fill values.
                 * -------------------------------------------------------------
                 * IMPLEMENTATION NOTE: If NaN/Inf is used as a fill value,
                 * then one must use a comparison which treats them
                 * correctly. One can therefore NOT use the usual "!=" operator.
                 */
                for (int i = i_start; ((0 <= i) & (i < IMFx_GSM_array.length)); i = i + step) {
                    final double IMFx = IMFx_GSM_array[i];
                    final double IMFy = IMFy_GSM_array[i];
                    final double IMFz = IMFz_GSM_array[i];
                    final boolean IMFisValid = ( //
                            (Double.compare(IMFx, OMNI2RawDataSource.DOUBLE_FILL_VALUE) != 0)
                            && (Double.compare(IMFy, OMNI2RawDataSource.DOUBLE_FILL_VALUE) != 0)
                            && (Double.compare(IMFz, OMNI2RawDataSource.DOUBLE_FILL_VALUE) != 0));
                    if (IMFisValid) {
                        // CASE: Not a fill value.
                        return new double[][]{{timeArray[i]}, {IMFx, IMFy, IMFz}};
                    }

                }
                return null;
            }
        }//=====================================================================

        Log.log(getClass().getSimpleName() + "#getValues("
                + time_mjd + ", " + fieldID + ", " + getIMFVector + ")", DEBUG);

        double[][] result = (double[][]) segmentsCache.search(time_mjd, SegmentsCache.SearchDirection.DOWN, new SearchFunction());

        if ((result == null) || (Math.abs(result[0][0] - time_mjd) > maxTimeDifference_days)) {

            result = (double[][]) segmentsCache.search(time_mjd, SegmentsCache.SearchDirection.UP, new SearchFunction());

            if ((result == null) || (Math.abs(result[0][0] - time_mjd) > maxTimeDifference_days)) {
                throw new ValueNotFoundException("Can not find OMNI2 value for fieldID=" + fieldID + " at time_mjd=" + time_mjd + ".");
            }
        }

        // CASE: "result" is not null.
        //final double dataPointTime_mjd = result[0][0];
        return result[1];  //*/
    }

    //##########################################################################
    public static class ValueNotFoundException extends Exception {

        public ValueNotFoundException(String msg) {
            super(msg);
        }
    }

    //##########################################################################
}
