/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import ovt.Const;
import ovt.datatype.Time;
import ovt.util.IndexedSegmentsCache.CacheSlotContents;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;

/**
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Class for handling the caching of orbital data downloaded from SSC Web
 * Services.
 *
 * NOTE: Uncertain if this is the right package for the class.<BR>
 * NOTE: SSCWS = SSC Web Services
 *
 * IMPLEMENTATION NOTE: The cache slot size is not a STATIC constant to simplify
 * automated code testing.
 */
public class SSCWSOrbitCache {

    private static final int DEBUG = 3;   // Set the minimum log message level for this class.
    private static final boolean OUTSIDE_EARTH_ASSERTION = false;   // You probably want to disable this during automated testing.
    private final IndexedSegmentsCache segmentsCache;
    private final double cacheSlotSizeMjd;    // Length of time that a cache slot covers.
    private final SSCWSLibrary sscwsLibrary;
    private final SSCWSSatelliteInfo satInfo;
    //private final File fileCache;   // When write to? When downloading? When OVT quits?

    //##########################################################################
    public static class OrbitalData {

        private static final int UNDEFINED_TIME_RESOLUTION = -1;
        /**
         * Orbit positions. "axisPos" = Indices [axis X/Y/Z/time][position] in
         * km & mjd.
         */
        public final double[][] coords_axisPos_kmMjd;
        public final int worstRequestedResolutionSeconds;  // Unit: seconds
        public final List<Integer> dataGaps;


        public OrbitalData(double[][] mCoords_axisPos, int mWorstRequestedResolutionSeconds, List<Integer> mDataGaps) {
            this.coords_axisPos_kmMjd = mCoords_axisPos;
            this.worstRequestedResolutionSeconds = mWorstRequestedResolutionSeconds;
            this.dataGaps = mDataGaps;
        }
    }
    //##########################################################################


    /**
     * IMPLEMENTATION NOTE: Constructor uses SSC Web Services satellite ID
     * string to identify satellite instead of SSCWSSatelliteInfo to make
     * automated testing more convenient.
     */
    public SSCWSOrbitCache(
            SSCWSLibrary mSSCWSLibrary, String SSCWSSatID,
            double mCacheSlotSizeMjd, int proactiveFillMargin) throws IOException {

        if (mCacheSlotSizeMjd <= 0) {
            throw new IllegalArgumentException("mCacheSlotSizeMjd <= 0");
        }
        this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWSSatID);
        this.sscwsLibrary = mSSCWSLibrary;
        this.cacheSlotSizeMjd = mCacheSlotSizeMjd;
        this.segmentsCache = new IndexedSegmentsCache(
                new CacheDataSource(),
                satInfo.availableBeginTimeMjd, satInfo.availableEndTimeMjd,
                proactiveFillMargin);
    }


    public void setCachingEnabled(boolean cachingEnabled) {
        this.segmentsCache.setCachingEnabled(cachingEnabled);
    }


    public OrbitalData getOrbitData(
            double beginMjdInclusive, double endMjdInclusive,
            RoundingMode tBeginRoundingMode, RoundingMode tEndRoundingMode,
            int beginIndexMargin, int endIndexMargin,
            int callerRequestedResolution)
            throws IOException {

        if (callerRequestedResolution <= 0) {
            throw new IllegalArgumentException("Illegal requested time resolution. callerRequestedResolution = " + callerRequestedResolution);
        }

        /**
         * Limit requested solution to the best possible. May otherwise
         * unnecessarily reject old cached data and fetch new data that can not
         * possible have a higher resolution anyway. Since the
         * accept-cache-slot-contents function is defined here, the check also
         * has to be done here.
         *
         * NOTE: Need to define "final" variable so that the value can be used
         * in the lambda function (anonymous class).
         */
        final int actualRequestedResolution = Math.max(callerRequestedResolution, satInfo.bestTimeResolution);

        final Predicate ACCEPT_CACHED_SLOT_CONTENTS_FUNCTION = (Predicate) (Object o) -> {
            final LocalCacheSlotContents unit = (LocalCacheSlotContents) o;
            return (unit.requestedTimeResolutionSeconds <= actualRequestedResolution);
        };

        final Object data = this.segmentsCache.getData(
                beginMjdInclusive, endMjdInclusive,
                tBeginRoundingMode, tEndRoundingMode,
                beginIndexMargin, endIndexMargin,
                ACCEPT_CACHED_SLOT_CONTENTS_FUNCTION, actualRequestedResolution);

        return (OrbitalData) data;
    }

    //##########################################################################
    private static class LocalCacheSlotContents implements IndexedSegmentsCache.CacheSlotContents {

        /**
         * The requested time resolution (after multiplying with the resolution
         * factor) used when requesting data from SSC Web Services.
         *
         * NOTE: The actual data is NOT required to have this time resolution.
         * There may be data gaps or irregular jumps in time.
         *
         * NOTE: SatelliteDescription#getResolution() returns int.
         */
        final int requestedTimeResolutionSeconds;   // Unit: seconds
        final double[][] coordinates;


        // NOTE: Only soft-copies mCoordinates.
        public LocalCacheSlotContents(int mResolutionFactor, double[][] mCoordinates) {
            this.requestedTimeResolutionSeconds = mResolutionFactor;
            this.coordinates = mCoordinates;
        }


        @Override
        public double[] getTArray() {
            return coordinates[3];
        }
    }

    //##########################################################################
    /**
     * Class which only purpose is to serve as a
     * IndexedSegmentsCache.DataSource. (1) In principle the outer class could
     * be used for this but that would mean that the methods prescribed by
     * IndexedSegmentsCache.DataSource had to be public. This way less about the
     * implementation is revealed to the outside. (2) In principle, those
     * methods (the implementations) in the outer class that are called from
     * this class could also be moved here but that would only make the code
     * confusing since the inner class would become so large.
     */
    private class CacheDataSource implements IndexedSegmentsCache.DataSource {

        @Override
        public int getCacheSlotIndex(double t) {
            return SSCWSOrbitCache.this.getCacheSlotIndex(t);
        }


        @Override
        public Object getDataFromCacheSlotContents(List<CacheSlotContents> requestedCacheSlotsContents, int i_beginSlotArrayInclusive, int i_endSlotArrayExclusive) {
            return SSCWSOrbitCache.this.getDataFromCacheSlotContents(requestedCacheSlotsContents, i_beginSlotArrayInclusive, i_endSlotArrayExclusive);
        }


        @Override
        public List<CacheSlotContents> getCacheSlotContents(int i_beginInclusive, int i_endExclusive, Object getCacheSlotContentsArgument) throws IOException {
            return SSCWSOrbitCache.this.getCacheSlotContents(i_beginInclusive, i_endExclusive, getCacheSlotContentsArgument);
        }
    }


    /**
     * Return the time period for which this cache slot MAY contain data. The
     * method should be consistent with int getCacheSlotIndex(double mjd).<BR>
     *
     * NOTE: The returned values do NOT necessarily refer to the time span of
     * the data that is actually in the cache slot. There might not be any data
     * for the given time interval data or only partially because it is at the
     * beginning/end of the available time series (at SSC Web Services). This is
     * what "Span" refers to in the method name, as opposed to e.g. "data". <BR>
     *
     * NOTE: The min value should be regarded as inclusive, while the max value
     * is exclusive.<BR>
     * NOTE: Method is independent of preexisting cache slots since it needs to
     * be called when creating them.<BR>
     */
    private double[] getCacheSlotSpanMjd(int i) {
        return new double[]{i * cacheSlotSizeMjd, (i + 1) * cacheSlotSizeMjd};
    }


    private int getCacheSlotIndex(double mjd) {
        if ((mjd < (Integer.MIN_VALUE / cacheSlotSizeMjd)) || ((Integer.MAX_VALUE / cacheSlotSizeMjd) < mjd)) {
            throw new RuntimeException("Can not convert modified Julian Day (mjd) value to int."
                    + "This (probably) indicates a bug.");
        }
        return (int) Math.floor(mjd / cacheSlotSizeMjd);  // NOTE: Typecasting with (int) implies rounding toward zero, not negative infinity.
    }


    /**
     * Fill specified cache slots with new data downloaded from SSC Web
     * Services.
     */
    private List<CacheSlotContents> getCacheSlotContents(
            int i_beginInclusive, int i_endExclusive,
            Object getCacheSlotContentsArgument)
            throws IOException {

        Log.log(this.getClass().getSimpleName()
                + ".getCacheSlotContents(i_beginInclusive=" + i_beginInclusive
                + ", i_endExclusive=" + i_endExclusive + ", ...)", DEBUG);

        final int requestedTimeResolution = (int) getCacheSlotContentsArgument;
        final double requestBeginMjd = getCacheSlotSpanMjd(i_beginInclusive)[0];
        final double requestEndMjd = getCacheSlotSpanMjd(i_endExclusive - 1)[1];
        final List<CacheSlotContents> filledSlots = new ArrayList();

        Log.log(this.getClass().getSimpleName() + ".getCacheSlotContents: " + Time.toString(requestBeginMjd) + " (" + requestBeginMjd + ") to " + Time.toString(requestEndMjd) + " (" + requestEndMjd + ")", DEBUG);

        /*======================================================================
         Download data        
         ---------------
         NOTE: The resolution factor must not be lower than one.
         NOTE: It is better to round down to a better resolution, and integer
         division does round down.
         ======================================================================*/
        final int resolutionFactor = Math.max(requestedTimeResolution / this.satInfo.bestTimeResolution, 1);
        System.out.println("Downloading data from SSC Web Services.");   // Printout (not log message) is here to cover the SSCWSLibrary for testing.
        final long t_start = System.nanoTime();
        final double[][] coords_axisPos = sscwsLibrary.getTrajectory_GEI(this.satInfo.ID, requestBeginMjd, requestEndMjd, resolutionFactor);
        final double duration = (System.nanoTime() - t_start) / 1.0e9;  // Unit: seconds
        System.out.println("   Time used for downloading data: " + duration + " [s]");

        /*==============
         Check assertion
         ==============*/
        if (OUTSIDE_EARTH_ASSERTION) {
            final double[] origin = new double[]{0, 0, 0};
            for (int i = 0; i < coords_axisPos[3].length; i++) {
                double[] x_km = new double[]{
                    coords_axisPos[0][i],
                    coords_axisPos[1][i],
                    coords_axisPos[2][i]};
                double r_km = Vect.distance(origin, x_km);
                if (r_km < Const.RE) {
                    //System.out.print();
                    Log.log("Encountered satellite position inside Earth, r_km = " + r_km + " [km].", DEBUG);
                }
            }
        }

        /*===============================================
         Create cache slots containing sections of data.
         ===============================================*/
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final double[] slotBeginEndMjd = getCacheSlotSpanMjd(i);
            double[][] slotCoords_axisPos;
            if (coords_axisPos[3].length != 0) {
                slotCoords_axisPos = new double[4][];
                final int[] n_slotInterval = Utils.findInterval(coords_axisPos[3], slotBeginEndMjd[0], slotBeginEndMjd[1], true, false);

                for (int k = 0; k < 4; k++) {
                    slotCoords_axisPos[k] = Arrays.copyOfRange(coords_axisPos[k], n_slotInterval[0], n_slotInterval[1]);   // Parameters are inclusive+exclusive.
                }
            } else {
                slotCoords_axisPos = new double[4][0];   // Important to create zero-length sub-arrays rather than keep null(s).
            }
            filledSlots.add(new LocalCacheSlotContents(requestedTimeResolution, slotCoords_axisPos));
        }

        return filledSlots;
    }


    private Object getDataFromCacheSlotContents(
            List<CacheSlotContents> slotContentsList,
            int i_beginSlotTArrayInclusive,
            int i_endSlotTArrayExclusive) {
        /* Merge and clip time series from the various cache slot into one long
         time series (array) for every component X/Y/Z/mjd.
         -----------------------------------------------------------------
         NOTE: Returning the whole array unclipped (returning more data than
         requested) is probably OK for the purposes of OVT,
         but clipping simplifies automated testing since it makes the output more
         predictable and directly comparable with the return value of a simple
         call to SSCWSLibrary#getTrajectory_GEI.
         */

        // Argument checks
        if (i_beginSlotTArrayInclusive < 0) {
            throw new IllegalArgumentException("Illegal argument i_beginSlotTArrayInclusive=" + i_beginSlotTArrayInclusive);
        } else if ((slotContentsList == null) && !((i_beginSlotTArrayInclusive == 0) && (i_endSlotTArrayExclusive == 0))) {
            throw new IllegalArgumentException("requestedSlotContents == null");
        }

        // Return special empty set of data.
        if (slotContentsList == null) {
            if ((i_beginSlotTArrayInclusive == 0) & (i_endSlotTArrayExclusive == 0)) {
                return new OrbitalData(new double[4][0], OrbitalData.UNDEFINED_TIME_RESOLUTION, new ArrayList<>());
            }
            throw new IllegalArgumentException("requestedSlotContents == null.");
        }

        // Indices [component X/Y/Z/mjd][cache slot][position index]
        final double[][][] coordUnmerged_axisSlotPos = new double[4][slotContentsList.size()][];

        int worstResolutionSeconds = 0;
        for (int i_slotInList = 0; i_slotInList < slotContentsList.size(); i_slotInList++) {
            final LocalCacheSlotContents slotContents = (LocalCacheSlotContents) slotContentsList.get(i_slotInList);   // NOTE: Typecasting
            worstResolutionSeconds = Math.max(worstResolutionSeconds, slotContents.requestedTimeResolutionSeconds);

            // Construct indices to copy from this particular unit.
            // NOTE: A slot may be BOTH FIRST AND LAST in the list.
            int j_begin = 0;
            int j_end = slotContents.coordinates[3].length;

            if (i_slotInList == 0) {
                // CASE: slot is FIRST in list.
                j_begin = i_beginSlotTArrayInclusive;
            }

            if (i_slotInList == slotContentsList.size() - 1) {
                // CASE: slot is LAST in list.
                j_end = i_endSlotTArrayExclusive;
            }

            for (int k_comp = 0; k_comp < 4; k_comp++) {
                coordUnmerged_axisSlotPos[k_comp][i_slotInList] = Utils.selectArrayIntervalMC(slotContents.coordinates[k_comp], j_begin, j_end);
            }
        }

        final double[][] coordMerged_axisPos = new double[4][];  // AxisPos = Indices [axis X/Y/Z/mjd][position index]
        for (int k_comp = 0; k_comp < 4; k_comp++) {
            coordMerged_axisPos[k_comp] = Utils.concatDoubleArrays(coordUnmerged_axisSlotPos[k_comp]);
        }

        /*========================
         Detect data gaps
         ----------------
         Move code to OrbitalData?
         ========================*/
        final List<Integer> dataGaps = Utils.findJumps(coordMerged_axisPos[3], worstResolutionSeconds * 2 * Time.DAYS_IN_SECOND);
        if (DEBUG > 0) {
            final double[] t = coordMerged_axisPos[3];
            for (int i_dg = 0; i_dg < dataGaps.size(); i_dg++) {  // dg = data gap
                int i = dataGaps.get(i_dg);
                final Time dgBegin = new Time(t[i]);
                final Time dgEnd = new Time(t[i + 1]);
                System.out.println("Detected data gap " + i_dg + ": " + dgBegin.toString() + " to " + dgEnd.toString() + ", length=" + (t[i + 1] - t[i]) + " (mjd)");
                System.out.println("                     " + t[i] + " to " + t[i + 1] + " (mjd)");
                // NOTE: Log.log and System.out.println might not always print in the order they are executed.
            }
        }

        final OrbitalData data = new OrbitalData(coordMerged_axisPos, worstResolutionSeconds, dataGaps);
        return data;
    }


    //##########################################################################
    /**
     * Informal test code.
     */
    public static void main(String[] args) throws IOException, Exception {
        //======================================================================
        class SSCWSLibraryEmul extends SSCWSLibrary {

            private final double data[][];
            private final SSCWSSatelliteInfo satInfo;


            SSCWSLibraryEmul(double[][] data, SSCWSSatelliteInfo satInfo) {
                this.data = data;
                this.satInfo = satInfo;
            }


            @Override
            public List<SSCWSSatelliteInfo> getAllSatelliteInfo() {
                List<SSCWSSatelliteInfo> satInfos = new ArrayList();
                satInfos.add(satInfo);
                return satInfos;
            }


            @Override
            public double[][] getTrajectory_GEI(String satID, double beginInclusiveMjd, double endInclusiveMjd, int reqResolution) {
                System.out.println(this.getClass().getSimpleName() + ".SSCWSLibraryEmul#getOrbitData("
                        + beginInclusiveMjd + ", " + endInclusiveMjd + ", " + reqResolution + ")");
                final double[][] data = getOrbitData(satID, beginInclusiveMjd, endInclusiveMjd, 0, 0, reqResolution);
                //System.out.println("    getTrajectory_GEI(..)[3] = "+Arrays.toString(data[3]));
                return data;

            }


            /**
             * NOTE: This method is not prescribed by SSCWSLibrary but is useful for
             * testing since it should return exactly what the cache method
             * should return.
             *
             * NOTE: Currently ignores the requested resolution, but the cache
             * still cares about the requested value when deciding whether to
             * keep old cache slot contents or replace them.
             */
            public double[][] getOrbitData(String satID, double beginInclusiveMjd, double endInclusiveMjd, int beginIndexMargin, int endIndexMargin, int reqResolution) {

                // Select and extract data range.
                int[] i_interval = Utils.findInterval(data[3], beginInclusiveMjd, endInclusiveMjd, true, true);   // NOTE: inclusive + INclusive.
                final int i_lowerBoundInclusive = i_interval[0] - beginIndexMargin;
                final int i_upperBoundExclusive = i_interval[1] + endIndexMargin;
                if ((i_lowerBoundInclusive < 0) | (i_upperBoundExclusive > data[3].length)) {
                    return new double[4][0];
                }
                final int N_request = i_upperBoundExclusive - i_lowerBoundInclusive;

                double[][] returnData = new double[4][N_request];
                for (int k_axis = 0; k_axis < 4; k_axis++) {
                    System.arraycopy(data[k_axis], i_lowerBoundInclusive, returnData[k_axis], 0, N_request);
                }
                return returnData;
            }


            public List<String> getPrivacyAndImportantNotices() {
                return null;
            }


            public List<String> getAcknowledgements() {
                return null;
            }
        }
        //======================================================================
        class TestCall {

            double beginMjd, endMjd;
            double[][] result;
            int beginEndIndexMargin;


            public TestCall(double a, double b, int beginEndIndexMargin, double[][] mResult) {
                beginMjd = a;
                endMjd = b;
                this.beginEndIndexMargin = beginEndIndexMargin;
                result = mResult;
            }
        }
        //======================================================================
        class TestRun {

            SSCWSLibraryEmul lib;
            SSCWSOrbitCache orbitCache;
            List<TestCall> testCalls = new ArrayList<>();


            TestRun(SSCWSLibraryEmul lib, SSCWSOrbitCache orbitCache) {
                this.lib = lib;
                this.orbitCache = orbitCache;
                //this.testCalls = testCalls;
            }
        }
        //======================================================================
        final double[][] data1;
        {
            final int N_data = 5 * 10 + 1;
            data1 = new double[4][N_data];
            for (int i = 0; i < N_data; i++) {
                data1[0][i] = 0;
                data1[1][i] = 0;
                data1[2][i] = 0;
                data1[3][i] = i / 5.0;   // 0.0, 0.2, ..., 5.0
            }
        }
        //======================================================================
        final double[][] data2 = new double[4][];
        {
            data2[3] = new double[]{0.1, 0.3, 0.9, 1.5};
            final int N_data = data2[3].length;
            data2[0] = new double[N_data];
            data2[1] = new double[N_data];
            data2[2] = new double[N_data];
            for (int i = 0; i < N_data; i++) {
                data2[0][i] = 0;
                data2[1][i] = 0;
                data2[2][i] = 0;
            }
        }
        //======================================================================
        final int reqResolution = Time.SECONDS_IN_DAY / 5;

        final List<TestRun> testRuns = new ArrayList<>();

        double[][][] dataList = new double[][][]{data1, data2};
        double[][] dataLimitsList = new double[][]{{0, 10}, {0, 2}};
        for (int i = 0; i < 2; i++) {
            final SSCWSLibraryEmul lib = new SSCWSLibraryEmul(
                    dataList[i],
                    new SSCWSSatelliteInfo("TestSat", "Test Satellite", dataLimitsList[i][0], dataLimitsList[i][1], reqResolution));
            testRuns.add(new TestRun(
                    lib,
                    new SSCWSOrbitCache(
                            lib,
                            "TestSat", 1, 0)));
        }

        for (TestRun run : testRuns) {
            final List<TestCall> newCalls = new ArrayList();
            newCalls.add(new TestCall(4.5, 5.5, 0, run.lib.getTrajectory_GEI(null, 4.5, 5.5, reqResolution)));
            newCalls.add(new TestCall(5.0, 7.0, 0, run.lib.getTrajectory_GEI(null, 5.0, 7.0, reqResolution)));
            newCalls.add(new TestCall(0.0, 2.0, 0, run.lib.getTrajectory_GEI(null, 0.0, 2.0, reqResolution)));
            newCalls.add(new TestCall(1.0, 2.0, 1, run.lib.getOrbitData(null, 1.0, 2.0, 1, 1, reqResolution)));
            newCalls.add(new TestCall(2.0, 3.0, 7, run.lib.getOrbitData(null, 2.0, 3.0, 7, 7, reqResolution)));
            newCalls.add(new TestCall(2.3, 3.7, 7, run.lib.getOrbitData(null, 2.3, 3.7, 7, 7, reqResolution)));
            newCalls.add(new TestCall(9.8, 9.8, 5, run.lib.getOrbitData(null, 9.8, 9.8, 5, 5, reqResolution)));
            newCalls.add(new TestCall(0.2, 0.2, 5, run.lib.getOrbitData(null, 0.2, 0.2, 5, 5, reqResolution)));
            run.testCalls.addAll(newCalls);
        }
        //testRuns.remove(1);

        //======================================================================
        for (TestRun run : testRuns) {

            System.out.println("================================");
            System.out.println("======== Start test run ========");
            System.out.println("================================");

            for (TestCall call : run.testCalls) {

                final OrbitalData actualResult = run.orbitCache.getOrbitData(
                        call.beginMjd, call.endMjd,
                        RoundingMode.CEILING, RoundingMode.FLOOR, // NOTE: Not varying these parameters.
                        call.beginEndIndexMargin, call.beginEndIndexMargin,
                        reqResolution);
                //System.out.println("actualResult[3] = " + Arrays.toString(actualResult[3]));

                if (Arrays.deepEquals(actualResult.coords_axisPos_kmMjd, call.result)) {
                    System.out.println("OK");
                } else {
                    System.out.println("#############################################");
                    System.out.println("orbitCache.getOrbitData(" + call.beginMjd + ", " + call.endMjd + ", " + call.beginEndIndexMargin + ", " + reqResolution + ");");
                    System.out.println("ERROR: actualResult.orbit[3] = " + Arrays.toString(actualResult.coords_axisPos_kmMjd[3]));
                    System.out.println("       call.result[3]        = " + Arrays.toString(call.result[3]));
                    System.out.println("#############################################");
                    System.exit(1);
                }
                System.out.println("--------------------------------");
            }
        }
    } // main

}
