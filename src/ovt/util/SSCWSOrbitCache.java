/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ovt.datatype.Time;
import ovt.util.IndexedSegmentsCache;
import ovt.util.IndexedSegmentsCache.SegmentCacheUnit;
import ovt.util.Log;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;
import ovt.util.Utils;

/**
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Class for handling the caching of orbital data downloaded from SSC Web
 * Services.
 *
 * NOTE: Uncertain if this is the right package for the class.<BR>
 * NOTE: SSCWS = SSC Web Services
 *
 * IMPLEMENTATION NOTE: The cache unit size is not a constant to simplify
 * automated code testing.
 */
// PROPOSAL: Use IndexObjectsCache through composition, not inheritance.
public class SSCWSOrbitCache {

    private final static java.util.function.Predicate ACCEPT_CACHED_UNIT_FUNCTION = (Object o) -> (true);     // TEMPORARY!!!
    private final static Object NEW_UNITS_ARGUMENT = 1;    // TEMPORARY

    private static final int DEBUG = 1;   // Set the minimum log message level for this class.

    private final IndexedSegmentsCache segmentsCache;
    private final double cacheUnitSizeMjd;    // Length of time that a cache unit covers.
    private final SSCWSLibrary sscwsLibrary;
    private final SSCWSSatelliteInfo satInfo;
    //private final File fileCache;   // When write to? When downloading? When OVT quits?

    //##########################################################################
    private static class CacheUnit implements IndexedSegmentsCache.SegmentCacheUnit {

        // NOTE: ResolutionFactor currently unused. TEMP
        /**
         * The requested time resolution (after multiplying with the resolution
         * factor) used when requesting data from SSC Web Services.
         *
         * NOTE: The actual data is NOT required to have this time resolution.
         * There may be data gaps or irregular jumps in time.
         *
         * NOTE: SatelliteDescription#getResolution() returns int.
         */
        final int requestedTimeResolution;   // Unit: seconds
        final double[][] coordinates;


        // NOTE: Only soft-copies mCoordinates.
        public CacheUnit(int mResolutionFactor, double[][] mCoordinates) {
            this.requestedTimeResolution = mResolutionFactor;
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
        public int getCacheUnit(double t) {
            return SSCWSOrbitCache.this.getCacheUnit(t);
        }


        @Override
        public Object extractCacheUnitContents(List<SegmentCacheUnit> requestedUnits, int i_beginUnitArrayInclusive, int i_endUnitArrayExclusive) {
            return SSCWSOrbitCache.this.extractCacheUnitContents(requestedUnits, i_beginUnitArrayInclusive, i_endUnitArrayExclusive);
        }


        @Override
        public List<SegmentCacheUnit> createNewCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException {
            return SSCWSOrbitCache.this.createNewCacheUnits(i_beginInclusive, i_endExclusive, newUnitsArgument);
        }
    }
    //##########################################################################


    /**
     * IMPLEMENTATION NOTE: Constructor uses SSC Web Services satellite ID
     * string to identify satellite instead of SSCWSSatelliteInfo to make
     * autmated testing more convenient.
     */
    public SSCWSOrbitCache(SSCWSLibrary mSSCWSLibrary, String SSCWSSatID,
            double mCacheUnitSizeMjd, int proactiveFillMargin) throws Exception {

        if (mCacheUnitSizeMjd <= 0) {
            throw new IllegalArgumentException();
        }
        this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWSSatID);
        this.sscwsLibrary = mSSCWSLibrary;
        this.cacheUnitSizeMjd = mCacheUnitSizeMjd;
        this.segmentsCache = new IndexedSegmentsCache(new CacheDataSource(), satInfo.availableBeginTimeMjd, satInfo.availableEndTimeMjd, proactiveFillMargin);
    }


    public double[][] getOrbitData(double beginMjdInclusive, double endMjdInclusive, int beginIndexMargin, int endIndexMargin) throws IOException {
        final Object data = this.segmentsCache.getData(beginMjdInclusive, endMjdInclusive, beginIndexMargin, endIndexMargin, ACCEPT_CACHED_UNIT_FUNCTION, NEW_UNITS_ARGUMENT);
        return (double[][]) data;
    }


    /**
     * Return the time period for which this cache unit MAY contain data. The
     * method should be consistent with int getCacheUnitIndex(double mjd).<BR>
     *
     * NOTE: The returned values do NOT necessarily refer to the time span of
     * the data that is actually in the cache unit. There might not be any data
     * for the given time interval data or only partially because it is at the
     * beginning/end of the available time series (at SSC Web Services). This is
     * what "Span" refers to in the method name, as opposed to e.g. "data". <BR>
     *
     * NOTE: The min value should be regarded as inclusive, while the max value
     * is exclusive.<BR>
     * NOTE: Method is independent of preexisting cache units since it needs to
     * be called when creating them.<BR>
     */
    private double[] getCacheUnitSpanMjd(int i) {
        return new double[]{i * cacheUnitSizeMjd, (i + 1) * cacheUnitSizeMjd};
    }


    private int getCacheUnit(double mjd) {
        if ((mjd < (Integer.MIN_VALUE / cacheUnitSizeMjd)) || ((Integer.MAX_VALUE / cacheUnitSizeMjd) < mjd)) {
            throw new RuntimeException("Can not convert modified Julian Day (mjd) value to int."
                    + "This (probably) indicates a bug.");
        }
        return (int) Math.floor(mjd / cacheUnitSizeMjd);  // NOTE: Typecasting with (int) implies rounding toward zero, not negative infinity.
    }


    /**
     * Fill specified cache units with new data downloaded from SSC Web
     * Services.
     */
    private List<SegmentCacheUnit> createNewCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException {

        Log.log("Call: createNewCacheUnits(i_beginInclusive=" + i_beginInclusive + ", i_endExclusive=" + i_endExclusive + ", ...)", DEBUG);

        final int requestedTimeResolution = (int) newUnitsArgument;
        final double requestBeginMjd = getCacheUnitSpanMjd(i_beginInclusive)[0];
        final double requestEndMjd = getCacheUnitSpanMjd(i_endExclusive - 1)[1];
        final List<SegmentCacheUnit> filledUnits = new ArrayList();

        Log.log("      createNewCacheUnits: " + Time.toString(requestBeginMjd) + " (" + requestBeginMjd + ") to " + Time.toString(requestEndMjd) + " (" + requestEndMjd + ")", DEBUG);

        /* Download data        
         ---------------
         NOTE: The resolution factor must not be lower than one.
         NOTE: It is better to round down to a better resolution, and integer division does round down.
         */
        final int resolutionFactor = Math.max(requestedTimeResolution / this.satInfo.bestTimeResolution, 1);
        final double[][] coords = sscwsLibrary.getOrbitData(this.satInfo.ID, requestBeginMjd, requestEndMjd, resolutionFactor);

        /* Create cache units containing sections of data. */
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final double[] unitBeginEndMjd = getCacheUnitSpanMjd(i);
            double[][] unitCoordinates;
            if (coords[3].length != 0) {
                unitCoordinates = new double[4][];
                final int[] n_unitInterval = Utils.findInterval(coords[3], unitBeginEndMjd[0], unitBeginEndMjd[1], true, false);

                for (int k = 0; k < 4; k++) {
                    unitCoordinates[k] = Arrays.copyOfRange(coords[k], n_unitInterval[0], n_unitInterval[1]);   // Parameters are inclusive+exclusive.
                }
            } else {
                unitCoordinates = new double[4][0];   // Important to create zero-length sub-arrays rather than keep null(s).
            }
            filledUnits.add(new CacheUnit(requestedTimeResolution, unitCoordinates));
        }

        return filledUnits;
    }


    private Object extractCacheUnitContents(List<SegmentCacheUnit> requestedUnits, int i_beginUnitArrayInclusive, int i_endUnitArrayExclusive) {
        /* Merge and clip time series from the various units into one long
         time series (array) for every component X/Y/Z/mjd.
         -----------------------------------------------------------------
         NOTE: Returning the whole array unclipped (returning more data than
         requested) is probably OK for the purposes of OVT,
         but clipping simplifies automated testing since it makes the output more
         predictable and directly comparable with the return value of a simple
         call to SSCWSLibrary#getOrbitData.
         */

        final double[][][] coordUnmerged = new double[4][requestedUnits.size()][];  // Indices [component X/Y/Z/mjd][unit][position index]
        for (int i_unitInList = 0; i_unitInList < requestedUnits.size(); i_unitInList++) {
            final CacheUnit unit = (CacheUnit) requestedUnits.get(i_unitInList);   // NOTE: Typecasting

            // Construct indices to copy from this particular unit.
            // NOTE: A unit may be BOTH FIRST AND LAST in the list.
            int j_begin = 0;
            int j_end = unit.coordinates[3].length;
            if (i_unitInList == 0) {
                //j_begin = Math.max(i_beginDataPointInclusive, j_begin);   // beginDataPointInclusive might be negative.
                j_begin = i_beginUnitArrayInclusive;
            }
            if (i_unitInList == requestedUnits.size() - 1) {
                //j_end = Math.min(i_endDataPointExclusive, j_end);   // endDataPointExclusive might be greater than length of array.
                j_end = i_endUnitArrayExclusive;
            }

            for (int k_comp = 0; k_comp < 4; k_comp++) {
                coordUnmerged[k_comp][i_unitInList] = Utils.selectArrayIntervalMC(unit.coordinates[k_comp], j_begin, j_end);
            }
        }

        final double[][] coordMerged = new double[4][];     // Indices [component X/Y/Z/mjd][position index]
        for (int k_comp = 0; k_comp < 4; k_comp++) {
            coordMerged[k_comp] = Utils.concatDoubleArrays(coordUnmerged[k_comp]);
        }

        return coordMerged;
    }

    //########################################################################

    /**
     * Informal test code.
     */
    public static void main(String[] args) throws IOException, Exception {
        //======================================================================
        class SSCWSLibraryEmul extends SSCWSLibrary {

            private double data[][];
            //private double availableBeginTimeMjd , availableEndTimeMjd;
            private SSCWSSatelliteInfo satInfo;


            SSCWSLibraryEmul(double[][] data, SSCWSSatelliteInfo satInfo) {
                this.data = data;
                this.satInfo = satInfo;
            }


            public List<SSCWSSatelliteInfo> getAllSatelliteInfo() {
                List<SSCWSSatelliteInfo> satInfos = new ArrayList();
                satInfos.add(satInfo);
                return satInfos;
            }


            public double[][] getOrbitData(String satID, double beginInclusiveMjd, double endInclusiveMjd, int resolutionFactor) {
                System.out.println("SSCWSLibraryEmul#getOrbitData(" + beginInclusiveMjd + ", " + endInclusiveMjd + ", " + resolutionFactor + ")");
                return getOrbitData(satID, beginInclusiveMjd, endInclusiveMjd, 0, 0, resolutionFactor);
            }


            /**
             * Method is no prescribed by SSCWSLibrary but is useful for testing
             * since it should return exactly what the cache method should
             * return.
             */
            public double[][] getOrbitData(String satID, double beginInclusiveMjd, double endInclusiveMjd, int beginIndexMargin, int endIndexMargin, int resolutionFactor) {

                // Select and extract data range.
                int[] i_interval = Utils.findInterval(data[3], beginInclusiveMjd, endInclusiveMjd, true, true);   // NOTE: inclusive + INclusive.
                final int i_begin = Math.max(i_interval[0] - beginIndexMargin, 0);
                final int i_end = Math.min(i_interval[1] + endIndexMargin, data[3].length);
                final int N_request = i_end - i_begin;

                double[][] returnData = new double[4][N_request];
                for (int k_axis = 0; k_axis < 4; k_axis++) {
                    System.arraycopy(data[k_axis], i_begin, returnData[k_axis], 0, N_request);
                }
                return returnData;
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
        double[][] data1;
        {
            int N_data = 5 * 10 + 1;
            data1 = new double[4][N_data];
            for (int i = 0; i < N_data; i++) {
                data1[0][i] = 0;
                data1[1][i] = 0;
                data1[2][i] = 0;
                data1[3][i] = i / 5.0;
            }
        }
        //======================================================================
        double[][] data2 = new double[4][];
        {
            data2[3] = new double[]{0.1, 0.3, 0.9, 1.5};
            int N_data = data2[3].length;
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
        final int resolutionFactor = 1;

        final List<TestRun> testRuns = new ArrayList<>();

        double[][][] dataList = new double[][][]{data1, data2};
        double[][] dataLimitsList = new double[][]{{0, 10}, {0, 2}};
        for (int i = 0; i < 2; i++) {
            SSCWSLibraryEmul lib = new SSCWSLibraryEmul(dataList[i], new SSCWSSatelliteInfo("TestSat", "Test Satellite", dataLimitsList[i][0], dataLimitsList[i][1], Time.SECONDS_IN_DAY / 5));
            testRuns.add(new TestRun(
                    lib,
                    new SSCWSOrbitCache(
                            lib,
                            "TestSat", 1, 0)));
        }

        for (TestRun run : testRuns) {
            final List<TestCall> newCalls = new ArrayList();
            newCalls.add(new TestCall(4.5, 5.5, 0, run.lib.getOrbitData(null, 4.5, 5.5, resolutionFactor)));
            newCalls.add(new TestCall(5.0, 7.0, 0, run.lib.getOrbitData(null, 5.0, 7.0, resolutionFactor)));
            newCalls.add(new TestCall(0.0, 2.0, 0, run.lib.getOrbitData(null, 0.0, 2.0, resolutionFactor)));
            newCalls.add(new TestCall(1.0, 2.0, 1, run.lib.getOrbitData(null, 1.0, 2.0, 1, 1, resolutionFactor)));
            newCalls.add(new TestCall(2.0, 3.0, 7, run.lib.getOrbitData(null, 2.0, 3.0, 7, 7, resolutionFactor)));
            newCalls.add(new TestCall(2.3, 3.7, 7, run.lib.getOrbitData(null, 2.3, 3.7, 7, 7, resolutionFactor)));
            newCalls.add(new TestCall(9.8, 9.8, 5, run.lib.getOrbitData(null, 9.8, 9.8, 5, 5, resolutionFactor)));
            newCalls.add(new TestCall(0.2, 0.2, 5, run.lib.getOrbitData(null, 0.2, 0.2, 5, 5, resolutionFactor)));
            run.testCalls.addAll(newCalls);
        }
        //testRuns.remove(1);

        //======================================================================
        for (TestRun run : testRuns) {

            System.out.println("================================");
            System.out.println("======== Start test run ========");
            System.out.println("================================");

            for (TestCall call : run.testCalls) {

                final double[][] actualResult = run.orbitCache.getOrbitData(call.beginMjd, call.endMjd, call.beginEndIndexMargin, call.beginEndIndexMargin);
                //System.out.println("actualResult[3] = " + Arrays.toString(actualResult[3]));

                if (Arrays.deepEquals(actualResult, call.result)) {
                    System.out.println("OK");
                } else {
                    System.out.println("#############################################");
                    System.out.println("orbitCache.getOrbitData(" + call.beginMjd + ", " + call.endMjd + ", " + call.beginEndIndexMargin + ", " + resolutionFactor + ");");
                    System.out.println("ERROR: actualResult[3] = " + Arrays.toString(actualResult[3]));
                    System.out.println("#############################################");
                    System.exit(1);
                }
                System.out.println("--------------------------------");
            }
        }
    } // main

}
