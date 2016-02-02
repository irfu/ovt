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
 * =========================================================================<BR>
 * IMPORTANT NOTE: The code for retrieving NASA OMNI2 data is spread out on a
 * number of classes. To make the organization, and the reasons for the
 * organization more clear, all relevant class descriptions are collected
 * here.<BR>
 * =============================================================================
 *
 * IMPLEMENTATION NOTE: OMNI2 offers data with different time averages but the
 * current implementation (2016-01-21) only uses one such time average (hourly
 * average). The implementation is however chosen such as to make certain things
 * easy, like certain conceivable future changes reasonably easy:<BR>
 * 1) ADD support for other OMNI2 file types (averages over other periods of
 * time)<BR>
 * 2) SWITCH between using different OMNI2 file types entirely,<BR>
 * 3) implement usage of OMNI2 with different time resolution at the same time
 * (possibly with different resolution for different time intervals).<BR>
 * 4) make testing with fake OMNI2 data easy.
 *
 * Crudely speaking, this is the dependency/data flows between the
 * classes/interfaces (2016-01-21) when using real OMNI2 data (not test
 * data):<BR>
 * (1) The-rest-of-OVT --USES-- OMNI2DataSource.<BR>
 * (2) OMNI2DataSource --USES-- an implementation of OMNI2RawDataSource.<BR>
 * (3) OMNI2RawDataSourceImpl (which implements OMNI2RawDataSource) --USES--
 * OMNI2FileUtils_HourlyAvg<BR>
 *
 * NOTE: To add support for other OMNI2 file types (time averages) one would
 * create new classes, one equivalent to OMNI2FileUtils_HourlyAvg for every new
 * OMNI2 file type to support (OMNI2FileUtils_DailyAvg, OMNI2FileUtils_5MinAvg
 * etc). Then OMNI2RawDataSource, OMNI2RawDataSourceImpl (most of the work?),
 * and OMNI2DataSource and would be modified accordingly.
 *
 * NOTE: The suffix "_hourlyAvg" refers to identifiers for hourly averaged data.
 *
 * NOTE: The exact file format is different for different OMNI2 data files with
 * different time resolutions/averages.
 *
 * NOTE: Somethings should maybe/probably ideally be organized differently:
 * OMNI2FileUtils_HourlyAvg (and any future counterparts) should (each) be split
 * up into (1) one generic method for reading columns (byte positions) out of
 * files, (2) one generic method for reading OMNI2 files (all file types), (3)
 * data specifying file format be moved to OMNI2RawDataSourceImpl.
 *
 *
 * ==============================<BR>
 * class OMNI2FileUtils_HourlyAvg<BR>
 * ==============================<BR>
 * PURPOSE: Class with code and settings specifically for handling OMNI2 files
 * with 1-hour averages and nothing else: Code for understanding file format,
 * default URL, default local file names.
 *
 * IMPLEMENTATION NOTE: The code reads all fields which are required to fill
 * OMNI2Data, which may be more than what OVT really requires. See OMNI2Data.
 *
 * IMPLEMENTATION NOTE: The class (or any other class for a specific OMNI2 file
 * type) should NOT have any form of caching (neither RAM nor disk) since a user
 * might want to combine data of different time resolution or from different
 * sources (before caching), or use the same cache for multiple types of data
 * (other variables) etc. In short, a user/caller might want a policy for
 * caching to disk that covers more than this OMNI2 data file type.
 *
 *
 *
 * ============================<BR>
 * interface OMNI2RawDataSource<BR>
 * ============================<BR>
 * PURPOSE: Interface describing a "raw" OMNI2 data source that supplies all
 * forms of OMNI2 data in separate methods (all averages that are implemented in
 * the application). One canonical implementation, "OMNI2RawDataSourceImpl", is
 * the actual source of all actual OMNI2 data. Other implementations can be
 * sources of test data for testing purposes. The application should read all
 * its OMNI2 data through one instance (on an implementation) of this interface.
 * The interface exists to make it easy to switch from one "raw" OMNI2 data
 * source to another.
 *
 * IMPLEMENTATION NOTE: Because of its purpose, this interface should in
 * practice describe (be based upon) the methods for RETRIEVING THE DIFFERENT
 * TYPES OF OMNI2 DATA, INCLUDING DIFFERENT AVERAGES, from the "true"
 * implementation, OMNI2RawDataSourceImpl, rather than the other way around
 * which is the usual way of thinking about java interfaces. The interface
 * SHOULD NOT cover methods for setting up the underlying data sources
 * (OMNI2FileUtils_HourlyAvg and analogous).
 *
 * IMPLEMENTATION NOTE: The methods are PERMITTED to be DEPENDENT on the format
 * of the underlying OMNI2 files: how data is distributed over time (time
 * resolution), how data is distributed in chunks (one file worth's of data) but
 * should of course still be as generic as possible.
 *
 *
 *
 * ============================<BR>
 * class OMNI2RawDataSourceImpl<BR>
 * ============================<BR>
 * PURPOSE: The canonical implementation of the interface OMNI2RawDataSource
 * supplying real OMNI2 data (not test data). The class should serve as a bridge
 * between: (1) Classes that handle specific files types (e.g.
 * OMNI2FileUtils_HourlyAvg), and (2) OMNI2DataSource. The class is intended to
 * contain things that are common for different OMNI2 data file types (different
 * averages), but not RAM caching (caching that is INdependent of different
 * OMNI2 file types):<BR>
 * 1) definitions of fill values (fill values used in java variables; not the
 * fill values used in OMNI2 files and which may depend on file type), <BR>
 * 2) how/if to cache downloaded OMNI2 files: filenaming conventions on disk,
 * choice of cache directory on disk. <BR>
 * 3) how to handle the data availability time interval (for all OMNI2 data, all
 * time resolutions), and how to handle the moving upper time boundary as time
 * progresses (may have to redownload files). <BR>
 *
 * NOTE: Different OMNI2 data files with different time resolutions have data
 * available for different (global) time intervals (different starting years).
 *
 * NOTE: OMNI2 data files can have fill values for data points in the future.
 *
 * IMPLENTATION NOTE: The code only reads the current time (time of execution,
 * walltime) once (twice really) to avoid a minor bug. If current time was read
 * multiple times, a data file could go from "recent enough to use" to "old
 * enough to be redownloaded" during the course of an OVT session. ==> Two
 * different versions of the same file may be used during the course of an OVT
 * session, something which MAY be undesirable since the code (.e.g. caching in
 * RAM) is not made to handle changes in the underlying OMNI2 data.
 *
 *
 *
 * =====================<BR>
 * class OMNI2DataSource<BR>
 * =====================<BR>
 * PURPOSE: Class from which all OMNI2 data can be retrieved as nice and simple
 * time series ("non-raw") for arbitrary periods of time by the rest of the
 * application. With the possible exception of initialization, the rest of the
 * application should thus not need any knowledge of underlying OMNI2 data
 * files, their internal format, caching (in RAM and/or on disk) etc. All OMNI2
 * data used by the application's general code should pass through this class.
 *
 * This class serves as the bridge between<BR>
 * (1) (implementations of) OMNI2RawDataSource (where the interface is partly
 * dependent on the format of the underlying OMNI2 data files and may change if
 * implementing support for other OMNI2 files), and<BR>
 * (2) the rest of OVT (which should be ignorant of the format of the underlying
 * OMNI2 data files).
 *
 * NOTE: It has no good way of finding the beginning and end time of available
 * data. Does throw proper exception when the code fails to obtain data though.
 *
 * IMPLEMENTATION NOTE: Implemented as an instantiated class (no static methods)
 * to make automated testing without the GUI and an arbitrary implementation of
 * OMNI2RawDataSource (and implicitly arbitrary cache directory) easier.
 *
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-xx
 */
public class OMNI2DataSource {

    /**
     * The exact value is not so important but should probably not be more than
     * the length of time covered by a year or the underlying OMNI2 files (?).
     * Follow the usage of the variable for details.
     */
    private static final double MIN_DATA_SOURCE_T_SCALE_DAYS = 1;

    /**
     * Greatest tolerated time (days) between the time for which data is
     * requested, and the time of the data point used.
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
         * See interface definition.
         */
        @Override
        public OMNI2Data getSupersetSegment(double t_begin, double t_end) throws IOException {
            if (!(t_begin < t_end)) {
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


        /**
         * See interface definition.
         */
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
     * NOTE: Defines how time should be interpreted, i.e. which OMNI2 data point
     * should actually be used for a specified time, e.g. nearest, nearest
     * before/after. and so on.
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

        //======== BEGINNING OF CLASS ==========================================
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
        }//======== END OF CLASS ===============================================

        Log.log(getClass().getSimpleName() + "#getValues("
                + time_mjd + ", " + fieldID + ", " + getIMFVector + ")", DEBUG);

        // Search for nearest PREVIOUS data point.
        double[][] result = (double[][]) segmentsCache.search(
                time_mjd, SegmentsCache.SearchDirection.DOWN, new SearchFunction());

        if ((result == null) || (Math.abs(result[0][0] - time_mjd) > maxTimeDifference_days)) {

            // Search for nearest data point AFTER.
            result = (double[][]) segmentsCache.search(
                    time_mjd, SegmentsCache.SearchDirection.UP, new SearchFunction());

            if ((result == null) || (Math.abs(result[0][0] - time_mjd) > maxTimeDifference_days)) {
                //throw new ValueNotFoundException("Can not find OMNI2 value for fieldID=" + fieldID + " at time_mjd=" + time_mjd + ".");
                throw new ValueNotFoundException(
                        "Can not find OMNI2 value for fieldID=" + fieldID
                        + " within " + maxTimeDifference_days
                        + " days of time_mjd=" + time_mjd + ".");
            }
        }

        // CASE: "result" is not null.
        //final double dataPointTime_mjd = result[0][0];
        return result[1];  //*/
    }

    //##########################################################################
    /**
     * Class for exceptions signifying that an OMNI2 value could not be found
     * for a specified approximate point in time even though the
     * available/accessible OMNI2 data series covers that point in time. In
     * other words, no value could be found due to a data gap in the downloaded
     * OMNI2 data. This should NOT be confused with a failure to download OMNI2
     * data.
     */
    public static class ValueNotFoundException extends Exception {

        public ValueNotFoundException(String msg) {
            super(msg);
        }
    }

    //##########################################################################
}
