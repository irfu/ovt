package ovt.util;

// import com.sun.media.jfxmedia.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import ovt.datatype.Time;
import ovt.util.IndexedSegmentsCache.NoSuchTPositionException;
import ovt.util.SSCWSLibrary.NoSuchSatelliteException;
import ovt.util.SSCWSOrbitCache.OrbitalData;

/**
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class SSCWSOrbitCacheTest {

    private final String SAT_ID = "TestSat";
    private final String SAT_NAME = "Test Satellite";

    //##########################################################################
    private static class SSCWSLibraryEmul extends SSCWSLibrary {

        private final double syntheticData[][];
        private final SSCWSLibrary.SSCWSSatelliteInfo satInfo;


        SSCWSLibraryEmul(double[][] mSyntheticData, SSCWSLibrary.SSCWSSatelliteInfo satInfo) {
            this.syntheticData = mSyntheticData;
            this.satInfo = satInfo;
        }


        @Override
        public List<SSCWSLibrary.SSCWSSatelliteInfo> getAllSatelliteInfo() {
            List<SSCWSLibrary.SSCWSSatelliteInfo> satInfos = new ArrayList();
            satInfos.add(satInfo);
            return satInfos;
        }


        @Override
        /**
         * NOT: This function can not automatically be implemented using
         * SSCWSLibraryEmul#getOrbitData(..) since the behaviour in case of no
         * data is different. This one must return "empty data"/arrays when
         * there is nothing in the interval.
         */
        public double[][] getTrajectory_GEI(
                String satID,
                double beginInclusiveMjd, double endInclusiveMjd,
                int reqResolution)
                throws IOException {

            System.out.println(this.getClass().getSimpleName() + ".SSCWSLibraryEmul#getOrbitData("
                    + beginInclusiveMjd + ", " + endInclusiveMjd + ", " + reqResolution + ")");

            /*final int[] i_interval = Utils.findInterval(syntheticData[3], beginInclusiveMjd, endInclusiveMjd, true, true);   // NOTE: inclusive + INclusive.
             final int i_lowerBoundInclusive = i_interval[0];
             final int i_upperBoundExclusive = i_interval[1];
             final int N_request = i_upperBoundExclusive - i_lowerBoundInclusive;

             final double[][] coords_axisPos_kmMjd = new double[4][N_request];
             for (int k_axis = 0; k_axis < 4; k_axis++) {
             System.arraycopy(syntheticData[k_axis], i_lowerBoundInclusive, coords_axisPos_kmMjd[k_axis], 0, N_request);
             }
             //coords_axisPos_kmMjd[0][0] = -1;   // INTENTIONAL "TEST BUG".
             return coords_axisPos_kmMjd;*/
            final OrbitalData orbitalData;
            try {
                orbitalData = getOrbitData(beginInclusiveMjd, endInclusiveMjd, RoundingMode.CEILING, RoundingMode.FLOOR, 0, 0, reqResolution);
            } catch (IndexedSegmentsCache.NoSuchTPositionException e) {
                return new double[4][0];
            }
            return orbitalData.coords_axisPos_kmMjd;
        }


        /**
         * NOTE: This method is not prescribed by SSCWSLibrary but is useful for
         * testing since it should return exactly what the corresponding cache
         * method should return. One can therefore compare the results.
         *
         * NOTE: Has to trigger exceptions the same way. This one has to trigger
         * exception when there are no corresponding t positions.
         *
         * NOTE: Currently ignores the requested resolution, but the cache still
         * cares about the requested value when deciding whether to keep old
         * cache slot contents or replace them.
         *
         * NOTE: Does not take any satellite ID to maintain identical argument
         * list with SSCWSOrbitCache#getOrbitData.
         */
        public SSCWSOrbitCache.OrbitalData getOrbitData(
                double beginInclusiveMjd, double endInclusiveMjd,
                RoundingMode tBeginRoundingMode, RoundingMode tEndRoundingMode,
                int beginIndexMargin, int endIndexMargin,
                int reqResolution_s) throws IOException, NoSuchTPositionException {

            //System.out.println("SSCWSLibraryEmul#getOrbitData");
            if ((tBeginRoundingMode != RoundingMode.CEILING)
                    | (tEndRoundingMode != RoundingMode.FLOOR)) {
                new IllegalArgumentException("Method does not support these arguments yet.");
            }

            final double mjdMin = syntheticData[3][0];
            final double mjdMax = syntheticData[3][syntheticData[3].length - 1];
            if ((endInclusiveMjd < mjdMin) | (mjdMax < beginInclusiveMjd)) {
                throw new NoSuchTPositionException("TEST CODE: Can not satisfy request. There are no such t positions.");
            }

            final int[] i_interval = Utils.findInterval(syntheticData[3], beginInclusiveMjd, endInclusiveMjd, true, true);   // NOTE: inclusive + INclusive.
            final int i_lowerBoundInclusive = i_interval[0] - beginIndexMargin;
            final int i_upperBoundExclusive = i_interval[1] + endIndexMargin;
            final int N_request = i_upperBoundExclusive - i_lowerBoundInclusive;
            if ((i_lowerBoundInclusive < 0) | (i_upperBoundExclusive > syntheticData[3].length)) {
                throw new NoSuchTPositionException("TEST: Can not satisfy request. The requested positions do not exist.");
                //return new double[4][0];
            }

            final double[][] returnData = new double[4][N_request];
            for (int k_axis = 0; k_axis < 4; k_axis++) {
                System.arraycopy(syntheticData[k_axis], i_lowerBoundInclusive, returnData[k_axis], 0, N_request);
            }
            final List<Integer> dataGaps = Utils.findJumps(returnData[3], reqResolution_s * 2 * Time.DAYS_IN_SECOND);

            return new OrbitalData(returnData, reqResolution_s, dataGaps);
        }


        public List<String> getPrivacyAndImportantNotices() {
            throw new UnsupportedOperationException();
        }


        public List<String> getAcknowledgements() {
            throw new UnsupportedOperationException();
        }
    }

    //##########################################################################

    public double[][] createSyntheticData1() {
        final int N_data = 5 * 10 + 1;
        final double[][] data = new double[4][N_data];
        for (int i = 0; i < N_data; i++) {
            data[0][i] = 0;
            data[1][i] = 0;
            data[2][i] = 0;
            data[3][i] = i / 5.0;   // 0.0, 0.2, ..., 5.0
        }
        return data;
    }


    public double[][] createSyntheticData2() {
        final double[][] data = new double[4][];
        data[3] = new double[]{0.1, 0.3, 0.9, 1.5};
        final int N_data = data[3].length;

        data[0] = new double[N_data];
        data[1] = new double[N_data];
        data[2] = new double[N_data];
        for (int i = 0; i < N_data; i++) {
            data[0][i] = 0;
            data[1][i] = 0;
            data[2][i] = 0;
        }
        return data;
    }

    //##########################################################################

    /**
     * Utility function to be used by other test code. Serializes and
     * deserializes the cache via a RAM buffer.
     * 
     * PROPOSAL: Implement comparison of the two caches with a special equals function.
     */
    public SSCWSOrbitCache saveToLoadFromStream(
            SSCWSOrbitCache cache, SSCWSLibrary lib, String satID, double approxProactiveCachingFillMarginMjd)
            throws IOException, ClassNotFoundException, NoSuchSatelliteException {

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(os);
        cache.writeToStream(oos);

        oos.close();
        os.close();
        //######################################################################
        final byte[] data = os.toByteArray();
        //######################################################################
        final InputStream is = new ByteArrayInputStream(data);
        final ObjectInputStream ois = new ObjectInputStream(is);
        final SSCWSOrbitCache readCache = new SSCWSOrbitCache(ois, lib, satID, approxProactiveCachingFillMarginMjd);
        is.close();
        ois.close();
        return readCache;
    }


    //##########################################################################
    @Test
    public void test_CacheIntegrity() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException, NoSuchSatelliteException {
        System.out.println("test_run1");
        // Logger.setLevel(2);

        final Method actualMethod = SSCWSOrbitCache.class.getDeclaredMethod("getOrbitData", Double.TYPE, Double.TYPE, RoundingMode.class, RoundingMode.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        final Method resultsMethod = SSCWSLibraryEmul.class.getDeclaredMethod("getOrbitData", Double.TYPE, Double.TYPE, RoundingMode.class, RoundingMode.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);

        final List<double[][]> syntheticDataList = new ArrayList();
        final List<double[]> syntheticDataLimitsList = new ArrayList();
        syntheticDataList.add(createSyntheticData1());
        syntheticDataLimitsList.add(new double[]{0, 10});
        syntheticDataList.add(createSyntheticData2());
        syntheticDataLimitsList.add(new double[]{0, 2});

        final List<Double> cacheSlotSizeMjdList = new ArrayList();
        cacheSlotSizeMjdList.add(0.1);
        cacheSlotSizeMjdList.add(1.0);
        cacheSlotSizeMjdList.add(100.0);

        final List<Integer> proactiveFillMarginList = new ArrayList();
        proactiveFillMarginList.add(0);
        proactiveFillMarginList.add(3);

        final List<Integer> reqResolutionSList = new ArrayList();
        reqResolutionSList.add(5);
        reqResolutionSList.add(Time.SECONDS_IN_DAY);

        for (int i = 0; i < syntheticDataList.size(); i++) {
            for (double cacheSlotSizeMjd : cacheSlotSizeMjdList) {
                for (int proactiveFillMargin : proactiveFillMarginList) {
                    for (int reqResolution_s : reqResolutionSList) {

                        final SSCWSLibraryEmul lib = new SSCWSLibraryEmul(
                                syntheticDataList.get(i),
                                new SSCWSLibrary.SSCWSSatelliteInfo(SAT_ID, SAT_NAME,
                                        syntheticDataLimitsList.get(i)[0],
                                        syntheticDataLimitsList.get(i)[1],
                                        reqResolution_s));
                        SSCWSOrbitCache cache = new SSCWSOrbitCache(lib, SAT_ID, cacheSlotSizeMjd, proactiveFillMargin);

                        
                        final List<Object[]> callList = new ArrayList();
                        callList.add(new Object[]{4.5, 5.5, RoundingMode.CEILING, RoundingMode.FLOOR, 0, 0, reqResolution_s});
                        callList.add(new Object[]{5.0, 7.0, RoundingMode.CEILING, RoundingMode.FLOOR, 0, 0, reqResolution_s});
                        callList.add(new Object[]{0.0, 2.0, RoundingMode.CEILING, RoundingMode.FLOOR, 0, 0, reqResolution_s});
                        callList.add(new Object[]{1.0, 2.0, RoundingMode.CEILING, RoundingMode.FLOOR, 1, 1, reqResolution_s});
                        callList.add(new Object[]{2.0, 3.0, RoundingMode.CEILING, RoundingMode.FLOOR, 7, 7, reqResolution_s});
                        callList.add(new Object[]{2.3, 3.7, RoundingMode.CEILING, RoundingMode.FLOOR, 7, 7, reqResolution_s});
                        callList.add(new Object[]{9.8, 9.8, RoundingMode.CEILING, RoundingMode.FLOOR, 5, 5, reqResolution_s});
                        callList.add(new Object[]{0.2, 0.2, RoundingMode.CEILING, RoundingMode.FLOOR, 5, 5, reqResolution_s});;
                        callList.add(new Object[]{-3.1, -2.9, RoundingMode.CEILING, RoundingMode.FLOOR, 5, 5, reqResolution_s});
                        callList.add(new Object[]{15.2, 16.2, RoundingMode.CEILING, RoundingMode.FLOOR, 5, 5, reqResolution_s});                        
                        for (Object[] argumentList : callList) {
                            cache = saveToLoadFromStream(cache, lib, SAT_ID, proactiveFillMargin * cacheSlotSizeMjd);
                            assertCallEqualsExpected(actualMethod, cache, resultsMethod, lib, argumentList);
                        }

                    }
                }

            }
        }
    }
    //##########################################################################

    /*@Test
     public void test_simpleSerialization() {
     final SSCWSLibraryEmul lib = new SSCWSLibraryEmul(
     syntheticDataList.get(i),
     new SSCWSLibrary.SSCWSSatelliteInfo(SAT_ID, SAT_NAME,
     syntheticDataLimitsList.get(i)[0],
     syntheticDataLimitsList.get(i)[1],
     reqResolution_s));
     SSCWSOrbitCache cache = new SSCWSOrbitCache(lib, SAT_ID, cacheSlotSizeMjd, proactiveFillMargin);
     cache = saveToLoadFromStream(cache, lib, SAT_ID, proactiveFillMargin * cacheSlotSizeMjd);
     }*/
    //##########################################################################

    /**
     * Somewhat experimental method for calling method (to be tested) and
     * comparing the result with other method with the same argument list.
     */
    private static void assertCallEqualsExpected(
            Method testMethod, Object testInstance,
            Method comparisonMethod, Object comparisonInstance,
            Object[] argumentList) throws IllegalAccessException {

        // Call "comparison method".
        Object comparisonResult;
        Throwable comparisonException;
        try {
            comparisonResult = comparisonMethod.invoke(comparisonInstance, argumentList);
            comparisonException = null;   // represents that no exception was thrown.
        } catch (InvocationTargetException e) {
            comparisonResult = null;   // Invalid value
            comparisonException = e.getCause();            // Returns Throwable, not Exception.
        }

        // Call method to be tested.
        Throwable actualException;
        Object actualResult;
        try {
            actualResult = testMethod.invoke(testInstance, argumentList);
            actualException = null;   // represents that no exception was thrown.
        } catch (InvocationTargetException e) {
            actualResult = null;   // Invalid value
            actualException = e.getCause();
        }

        // Compare results.
        if (actualException != null) {
            assertTrue(comparisonException != null);
            assertEquals(actualException.getClass(), comparisonException.getClass());
        } else {
            assertTrue(comparisonException == null);
            assertEquals(actualResult, comparisonResult);   // Requires properly implemented .equals method.
        }
    }

    //##########################################################################

}
