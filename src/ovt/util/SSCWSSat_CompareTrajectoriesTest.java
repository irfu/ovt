/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.object.LTOFSat;
import ovt.object.SSCWSSat;
import ovt.object.TLESat;

/**
 * Informal test code for comparing trajectories to determine differences in
 * coordinate systems or time. Used in particular to verify the coordinate
 * system used by SSCWS satellites data.
 *
 * Theories for why trajectories may differ:<BR>
 * 1) Interpolation<BR>
 * 2) Not using the same coordinate system<BR>
 * __2a) SSCWSLibrary uses the wrong coordinate system of its two GEI
 * variants.<BR>
 * 3) Not using the same time (~constant time difference)<BR>
 * 4) Something wrong with the TLE or LTOF calculation<BR>
 * 5) Something wrong with the TLE or LTOF files<BR>
 * __5a) Using time interval for which TLE/LTOF file contains predicted
 * trajectory (and SSC does not).<BR>
 * __5b) Files are not the "latest" version.<BR>
 * 6) Something wrong with the SSC data<BR>
 * 7) Difference in how handling data gaps (or special cases) somehow.<BR>
 *
 * NOTE: If one tries to download SSC in different coordinate systems, then one
 * MUST DEACTIVATE CACHING TO FILE.
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class SSCWSSat_CompareTrajectoriesTest {

    /** Useful when modifying OVT code obtaining/deriving SSCWS data. */
    private static final boolean USE_SSCWS_DISK_CACHE = true;


    public static void main(String[] args) throws IOException {
        //DistributionEstimator.test();
        //test_testCode();
        test2();
    }


    /**
     * Test code for compareTrajectories (test code for the test code...).
     *
     * @throws IOException
     */
    public static void test_testCode() throws IOException {
        final SSCWSLibrary lib = ovt.util.SSCWSLibraryTestEmulator.DEFAULT_INSTANCE;
        final SSCWSSat.DataSource sscwsDataSource1 = new SSCWSSat.DataSource(lib, "CompOrbitSat1", null);
        final SSCWSSat.DataSource sscwsDataSource2 = new SSCWSSat.DataSource(lib, "CompOrbitSat2b", null);

        final int N = 1000;
        double[] timeMjdMap = ovt.util.Utils.newLinearArray(
                Time.getMjd(2000, 01, 01, 00, 00, 00),
                Time.getMjd(2000, 01, 10, 00, 00, 00), N);
        final double[][] gei_arr_posAxis_km1 = new double[N][3];
        final double[][] gei_arr_posAxis_km2 = new double[N][3];
        final double[][] vei_arr = new double[N][3];

        sscwsDataSource1.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km1, vei_arr);
        sscwsDataSource2.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km2, vei_arr);
        //System.out.println("gei_arr_posAxis_km1[0] = " + Arrays.toString(gei_arr_posAxis_km1[0]));
        //System.out.println("gei_arr_posAxis_km2[0] = " + Arrays.toString(gei_arr_posAxis_km2[0]));

        compareTrajectories(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr, timeMjdMap);
    }


    /**
     * Informal test code for comparing coords_axisPos_kmMjd data for the same
     * satellite from two different sources (instance of "Sat").
     */
    // PROPOSAL: Try trajectories of more satellites than cluster.
    // PROPOSAL: Verify coordinate system of LTOF files?
    public static void test2() throws IOException {
        /*
         -------- SSC Web Services --------
         getID(): cluster1;   getName(): Cluster-1 (FM5/Rumba)
         getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
         getID(): cluster2;   getName(): Cluster-2 (FM6/Salsa)
         getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
         getID(): polar;   getName(): Polar
         getStartTime(): 1996-03-01T00:00:00Z;   getEndTime(): 2009-06-27T00:00:00Z;   getResolution(): 180
         ----------------------------------
         // Double star 1 : LTOF-fil är konstig. Oanvändbar för jämförelser.
         Cluster1.ltof : 2000-08-09 - 2013-01-01
         polar.tle   : 1996-10-02 - 2004-03-09
         akebono.tle : 1989-02-27 - 1994-10-26
         */

        final int N = 2000;
        //final int N = 24*60;

        compareTrajectories(
                new SSCWSDataSource("cluster1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
                new LTOFFileDataSource(
                        "/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof"
                //"/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"
                ),
                Utils.newLinearArray(
                        Time.getMjd(2008, 01, 01, 00, 00, 00),
                        Time.getMjd(2008, 04, 01, 00, 00, 00), N));//*/
        /*compareTrajectories(
         new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof"
         ),
         new LTOFFileDataSource(
         "/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"
         ),
         Utils.newLinearArray(
         Time.getMjd(2000, 10, 01, 00, 00, 00),
         Time.getMjd(2009, 10, 01, 00, 00, 00), N));//*/
        /*compareTrajectories(
         new SSCWSDataSource("polar", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new TLEFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Polar.tle"),
         Utils.newLinearArray(
         Time.getMjd(2001, 01, 01, 00, 00, 00),
         Time.getMjd(2001, 02, 02, 00, 00, 00), N));//*/
        /*compareTrajectories(
         new SSCWSDataSource("akebono", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new TLEFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/akebono.tle"),
         Utils.newLinearArray(
         Time.getMjd(1990, 01, 01, 00, 00, 00),
         Time.getMjd(1990, 02, 02, 00, 00, 00), N));//*/
    }


    /**
     * Compare trajectories and print measures of the difference. Used for
     * verifying that the same trajectory, retrieved from two different sources,
     * are in the same coordinate system. Can also be used to obtain statistics
     * on the differences between two similar trajectories.
     *
     * @param vei_arr Not defined whether it belongs to trajectory 1 or 2.
     * @param timeMjdList Only required for some limited analysis.
     */
    private static void compareTrajectories(
            double[][] coord_posAxis_km1,
            double[][] coord_posAxis_km2,
            double[][] vei_arr,
            double timeMjdList[]) {

        // Argument checks.
        if ((timeMjdList.length != coord_posAxis_km1.length) | (timeMjdList.length != coord_posAxis_km2.length)) {
            throw new IllegalArgumentException("Inconsistent array dimensions.");
        } else if ((coord_posAxis_km1[0].length != 3) | (coord_posAxis_km2[0].length != 3)) {
            throw new IllegalArgumentException("Illegal array dimensions.");
        }

        final int N = coord_posAxis_km1.length;

        final double[] dPos_abs_array = new double[N];
        final double[] dPos_x_array = new double[N];
        final double[] dPos_y_array = new double[N];
        final double[] dPos_z_array = new double[N];

        final double[] dPos_r_array = new double[N];
        final double[] dPos_v_array = new double[N];
        final double[] dPos_rxv_array = new double[N];
        final double[] dPos_vxrxv_array = new double[N];

        final double[] dPos_vp_array = new double[N];

        final double[] rotAxisX_array = new double[N];
        final double[] rotAxisY_array = new double[N];
        final double[] rotAxisZ_array = new double[N];

        for (int i = 0; i < N; i++) {
            final double[] pos1 = new double[]{coord_posAxis_km1[i][0], coord_posAxis_km1[i][1], coord_posAxis_km1[i][2]};
            final double[] pos2 = new double[]{coord_posAxis_km2[i][0], coord_posAxis_km2[i][1], coord_posAxis_km2[i][2]};
            final double[] v = vei_arr[i];
            final double[] rxv = Vect.cross(pos1, v);    // Orthogonal to both r and v.
            final double[] vxrxv = Vect.cross(v, rxv);  // Orthogonal to both v and rxv.
            final double[] dPos = Vect.sub(pos2, pos1);
            // NOTE: (r,v,rxv) are NOT all orthogonal to each other.
            // NOTE: (v, rxv, vxrxv) ARE all orthogonal to each other.

            final double[] rotAxis = Vect.multiply(Vect.cross(pos1, pos2), 1 / (Vect.absv(pos1) * Vect.absv(pos2)));

            dPos_abs_array[i] = Vect.absv(dPos);
            dPos_x_array[i] = dPos[0];
            dPos_y_array[i] = dPos[1];
            dPos_z_array[i] = dPos[2];

            // NOTE: r=pos1 = vector from origin.
            // NOTE: r and v are NOT perpendicular (but are at least unlikely to be parallel).
            dPos_r_array[i] = getVectorComponent(dPos, pos1, true);
            dPos_v_array[i] = getVectorComponent(dPos, v, true);
            dPos_rxv_array[i] = getVectorComponent(dPos, rxv, true);
            dPos_vxrxv_array[i] = getVectorComponent(dPos, vxrxv, true);

            dPos_vp_array[i] = getVectorComponent(dPos, v, false);   // NOTE: No normalization of v. Useful for the case of exact time difference.

            rotAxisX_array[i] = rotAxis[0];
            rotAxisY_array[i] = rotAxis[1];
            rotAxisZ_array[i] = rotAxis[2];

            final Time time = new Time(timeMjdList[i]);
            System.out.println(time.toString()+": d = " + Vect.absv(dPos));
        }

        //System.out.println("---");
        System.out.println("N = " + N);
        printStatistics("dPos_abs", new Statistics(dPos_abs_array));
        //System.out.println("========");
        //printStatistics("dPos_x", new Statistics(dPos_x_array));
        //printStatistics("dPos_y", new Statistics(dPos_y_array));
        //printStatistics("dPos_z", new Statistics(dPos_z_array));
        System.out.println("========");
        printStatistics("dPos_r", new Statistics(dPos_r_array));
        printStatistics("dPos_v", new Statistics(dPos_v_array));
        printStatistics("dPos_rxv", new Statistics(dPos_rxv_array));
        printStatistics("dPos_vxrxv", new Statistics(dPos_vxrxv_array));
        System.out.println("========");
        printStatistics("dPos_vp", new Statistics(dPos_vp_array));
        //System.out.println("========");
        //printStatistics("rotAxisX", new Statistics(rotAxisX_array));
        //printStatistics("rotAxisY", new Statistics(rotAxisY_array));
        //printStatistics("rotAxisZ", new Statistics(rotAxisZ_array));
    }


    /**
     * IMPLEMENTATION NOTE: Not merged into<BR>
     * compareTrajectories(double[][] coord_posAxis_km1, double[][]
     * coord_posAxis_km2, double[][] vei_arr)<BR>
     * to make it possible to write comparison functions that make multiple
     * calls with different values for timeMjdList.
     */
    private static void compareTrajectories(TrajectoryDataSource src1, TrajectoryDataSource src2, double[] timeMjdList) throws IOException {

        final int N = timeMjdList.length;
        final double[][] gei_arr_posAxis_km1 = new double[N][3];
        final double[][] vei_arr1 = new double[N][3];
        final double[][] gei_arr_posAxis_km2 = new double[N][3];
        final double[][] vei_arr2 = new double[N][3];

        src1.fill_GEI_VEI(timeMjdList, gei_arr_posAxis_km1, vei_arr1);
        src2.fill_GEI_VEI(timeMjdList, gei_arr_posAxis_km2, vei_arr2);

        compareTrajectories(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr1, timeMjdList);
    }


    //##########################################################################
    private static void printStatistics(String title, Statistics s) {
        System.out.println(title);
        System.out.printf("   Mean +/- Std deviation = %.2f +/- %.2f\n", s.mean, s.stdDeviation);
        System.out.printf("   Std_deviation/mean     = %.2f\n", s.stdDeviation / s.mean);
        System.out.printf("   (min, max)     = (%.2f; %.2f)\n", s.min, s.max);
        System.out.printf("   (x_02, x_98)     = (%.2f; %.2f)\n", s.x_02, s.x_98);
        System.out.printf("   (x_05, x_95)     = (%.2f; %.2f)\n", s.x_05, s.x_95);
        System.out.printf("   (i_min, i_max) = (%d; %d)\n", s.i_min, s.i_max);
    }


    /**
     * Get vector component in an arbitrary direction.
     *
     * @param v_ref NOTE: Only uses the normalization of v_ref.
     */
    private static double getVectorComponent(double[] v, double[] v_ref, boolean normalize_ref) {
        double C = 0.0;
        if (normalize_ref) {
            C = Vect.absv(v_ref);
        } else {
            C = 1.0;
        }
        return Vect.dot(v, v_ref) / C;
    }

    //##########################################################################
    private interface TrajectoryDataSource {

        public void fill_GEI_VEI(
                double[] timeMjdMap,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr)
                throws IOException;
    }

    //##########################################################################
    private static class SSCWSDataSource implements TrajectoryDataSource {

        private final SSCWSSat.DataSource dataSource;
        private final String satID;   // Save cache file instead?


        public SSCWSDataSource(String mSatID, SSCWSLibrary mLib) throws IOException {
            satID = mSatID;
            File cacheFile = null;
            if (USE_SSCWS_DISK_CACHE) {
                cacheFile = selectCacheFile(mSatID);
            }
            dataSource = new SSCWSSat.DataSource(mLib, mSatID, cacheFile);
        }


        // NOTE: Saves cahce to disk after every read. Maybe somewhat inefficient.
        public void fill_GEI_VEI(
                double[] timeMjdMap,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr) throws IOException {

            dataSource.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km, vei_arr);
            if (USE_SSCWS_DISK_CACHE) {
                dataSource.saveCacheToFile(selectCacheFile(satID));
            }
        }

    }

    //##########################################################################
    private static class LTOFFileDataSource implements TrajectoryDataSource {

        private final String filePath;


        public LTOFFileDataSource(String mFilePath) {
            filePath = mFilePath;
        }


        public void fill_GEI_VEI(
                double[] timeMjdMap,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr) throws IOException {

            LTOFSat.fill_GEI_VEI_Raw(new File(filePath), timeMjdMap, gei_arr_posAxis_km, vei_arr);
        }
    }

    //##########################################################################
    private static class TLEFileDataSource implements TrajectoryDataSource {

        private final String filePath;


        public TLEFileDataSource(String mFilePath) {
            filePath = mFilePath;
        }


        public void fill_GEI_VEI(
                double[] timeMjdList,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr) throws IOException {

            System.loadLibrary("ovt-" + OVTCore.VERSION);   // Required for calling native code.
            TLESat.getSatPosJNI(filePath, timeMjdList, gei_arr_posAxis_km, vei_arr, timeMjdList.length);
        }
    }

    //##########################################################################
    /**
     * Class for compiling statistics for the contents of an array.
     */
    private static class Statistics {

        public final double mean;
        public final double stdDeviation;
        public final double min, max;
        public final int i_min, i_max;
        public final double x_05, x_95;
        public final double x_02, x_98;
        // Flag for when encountering non-finite values?


        private Statistics(double[] xa) {
            final double N = xa.length;
            double xsum = 0, x2sum = 0;
            double tempMin = Double.POSITIVE_INFINITY;
            double tempMax = Double.NEGATIVE_INFINITY;
            int i_min_temp = -1, i_max_temp = -1;

            for (int i = 0; i < N; i++) {
                final double x = xa[i];
                if (!Double.isFinite(x)) {
                    throw new IllegalArgumentException("Encountered non-finite value.");
                }
                xsum += x;
                x2sum += x * x;
                if (x < tempMin) {
                    tempMin = x;
                    i_min_temp = i;
                }
                if (tempMax < x) {
                    tempMax = x;
                    i_max_temp = i;
                }
            }
            min = tempMin;
            max = tempMax;
            i_min = i_min_temp;
            i_max = i_max_temp;
            mean = xsum / N;

            final double x2mean = x2sum / N;
            stdDeviation = Math.sqrt(x2mean - mean * mean);

            // Derive percentiles.
            final double[] x_sort = Arrays.copyOf(xa, xa.length);
            Arrays.sort(x_sort);  // NOTE: Method can also sort NaN, +/-Inf. See Java documentation.
            x_05 = x_sort[(int) (x_sort.length * 0.05)];
            x_95 = x_sort[(int) (x_sort.length * 0.95)];
            x_02 = x_sort[(int) (x_sort.length * 0.02)];
            x_98 = x_sort[(int) (x_sort.length * 0.98)];
        }

    }


    /**
     * Defines file(s) one can use for SSCWS data caching.
     */
    public static File selectCacheFile(String SSCWS_satID) {
        return new File("/home/erjo/temp/OVT.SSCWS." + SSCWS_satID + ".cache");
    }
}
