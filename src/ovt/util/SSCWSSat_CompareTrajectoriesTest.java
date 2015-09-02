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
 * Informal test_compareTrajectories code for comparing trajectories to
 * determine differences in coordinate systems or time. Used in particular to
 * verify the coordinate system used by SSCWS satellites data.
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

    /**
     * Useful when modifying OVT code obtaining/deriving SSCWS data.
     */
    private static final boolean USE_SSCWS_DISK_CACHE = true;


    public static void main(String[] args) throws IOException {
        //DistributionEstimator.test_compareTrajectories();
        //test_testCode();
        test_compareTrajectories();
        //test_pointCalculation();
    }


    /**
     * Test code for compareTrajectories (test_compareTrajectories code for the
     * test_compareTrajectories code...).
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
        final double[][] vei_arr1 = new double[N][3];
        final double[][] vei_arr2 = new double[N][3];

        sscwsDataSource1.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km1, vei_arr1);
        sscwsDataSource2.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km2, vei_arr2);
        //System.out.println("gei_arr_posAxis_km1[0] = " + Arrays.toString(gei_arr_posAxis_km1[0]));
        //System.out.println("gei_arr_posAxis_km2[0] = " + Arrays.toString(gei_arr_posAxis_km2[0]));

        compareTrajectories(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr1, vei_arr2, timeMjdMap);
    }


    public static void test_pointCalculation() throws IOException {
        final double[] timeMjdMap = {Time.getMjd(1997, 01, 01, 00, 00, 00)};
        TrajectoryDataSource tds = new LTOFFileDataSource(
                "/home/erjo/work_files/ovt_diverse/ESOC_LTOF_validation/ltof.cl1"
        );

        /*final double[] timeMjdMap = {Time.getMjd(2005, 01, 01, 00, 00, 00)};
         TrajectoryDataSource tds = new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Double_Star_2.ltof"
         );*/
        final double[][] gei_arr_posAxis_km = new double[1][3];
        final double[][] vei_arr = new double[1][3];
        tds.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km, vei_arr);

        System.out.printf("gei_arr : %f, %f, %f\n", gei_arr_posAxis_km[0][0], gei_arr_posAxis_km[0][1], gei_arr_posAxis_km[0][2]);
        System.out.printf("vei_arr : %f, %f, %f\n", vei_arr[0][0], vei_arr[0][1], vei_arr[0][2]);
    }


    /**
     * Informal test_compareTrajectories code for comparing coords_axisPos_kmMjd
     * data for the same satellite from two different sources (instance of
     * "Sat").
     */
    // PROPOSAL: Try trajectories of more satellites than cluster.
    // PROPOSAL: Verify coordinate system of LTOF files?
    public static void test_compareTrajectories() throws IOException {
        /*
         -------- SSC Web Services --------
         getID(): cluster1;   getName(): Cluster-1 (FM5/Rumba)
         getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
         getID(): cluster2;   getName(): Cluster-2 (FM6/Salsa)
         getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
         getID(): polar;   getName(): Polar
         getStartTime(): 1996-03-01T00:00:00Z;   getEndTime(): 2009-06-27T00:00:00Z;   getResolution(): 180
         getID(): doublestar1;   getName(): Double Star-1
         getStartTime(): 2004-01-03T07:05:59Z;   getEndTime(): 2007-10-10T12:37:00Z;   getResolution(): 60
         getID(): doublestar2;   getName(): Double Star-2
         getStartTime(): 2004-09-16T23:53:59Z;   getEndTime(): 2009-12-31T23:58:59Z;   getResolution(): 60
         getID(): akebono;   getName(): Akebono
         getStartTime(): 1989-02-23T00:00:00Z;   getEndTime(): 2015-07-05T00:00:00Z;   getResolution(): 60
         ----------------------------------
         // Double star 1 : LTOF-fil är konstig. Oanvändbar för jämförelser. Varför?!!
         Cluster1.ltof : 2000-08-09 - 2013-01-01
         SUPER_LTOF_C1.CR.ltof : 2000-08-09 - 2020-01-01 - predicted from 2015-08-21
         SUPER_LTOF_C2.CR.ltof : 2000-07-16 - 2020-01-01 - predicted from 2015-08-21
         SUPER_LTOF_C3.CR.ltof : 2000-07-16 - 2020-01-01 - predicted from 2015-08-18
         SUPER_LTOF_C4.CR.ltof : 2000-08-09 - 2020-01-01 - predicted from 2015-08-23
         polar.tle   : 1996-10-02 - 2004-03-09
         akebono.tle : 1989-02-27 - 1994-10-26
         Double_Star_1.ltof : 2004-01-01 - 2007-10-10 - predicted from 2006-10-05
         Double_Star_2.ltof : 2004-07-25 - 2008-09-02 - predicted from 2007-08-02
         */

        final int N = 20000;   // Note: 1 day = 1440 min.
        final double startMjd = Time.getMjd(2010, 1, 1, 0, 0, 0);
        final double lengthMjd = 120;
        final double timeDifferenceMjd = Time.DAYS_IN_SECOND * 30;
        compareTrajectories(
                new SSCWSDataSource("cluster3", SSCWSLibraryImpl.DEFAULT_INSTANCE),
                new LTOFFileDataSource(
                        //"/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof"
                        "/home/erjo/work_files/INBOX/SUPER_LTOF_C3.CR.ltof"
                ),
                Utils.newLinearArray(
                        startMjd,
                        startMjd + lengthMjd, N),
                timeDifferenceMjd);//*/
        /*compareTrajectories(
         new SSCWSDataSource("doublestar1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Double_Star_1.ltof"),
         Utils.newLinearArray(
         startMjd,
         startMjd + lengthMjd, N),
         timeDifferenceMjd);//*/
        //--------------------------
        /*compareTrajectories(
         new SSCWSDataSource("doublestar2", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Double_Star_2.ltof"),
         Utils.newLinearArray(
         startMjd,
         startMjd + lengthMjd, N),
         timeDifferenceMjd);//*/
        //--------------------------
        /*compareTrajectories(
         new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof"
         ),
         new LTOFFileDataSource(
         "/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"
         ),
         Utils.newLinearArray(
         startMjd,
         startMjd + lengthMjd, N),
         0.0);//*/
        //--------------------------
        /*compareTrajectories(
         new SSCWSDataSource("polar", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new TLEFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Polar.tle"),
         Utils.newLinearArray(
         startMjd,
         startMjd + lengthMjd, N),
         timeDifferenceMjd);//*/
        //--------------------------
        /*compareTrajectories(
         new SSCWSDataSource("akebono", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new TLEFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/akebono.tle"),
         Utils.newLinearArray(
         startMjd,
         startMjd + lengthMjd, N),
         timeDifferenceMjd);//*/
        //--------------------------
    }


    /**
     * Compare trajectories and print measures of the difference. Used for
     * verifying that the same trajectory, retrieved from two different sources,
     * are in the same coordinate system. Can also be used to obtain statistics
     * on the differences between two similar trajectories.
     *
     * NOTE: The analysis is not necessarily symmetric in trajectory 1 and 2.
     *
     * @param timeMjdList Only required for some limited analysis.
     */
    private static void compareTrajectories(
            double[][] coord_posAxis_km1,
            double[][] coord_posAxis_km2,
            double[][] vei_arr1,
            double[][] vei_arr2,
            double timeMjdList[]) {

        // Argument checks.
        if ((timeMjdList.length != coord_posAxis_km1.length)
                | (timeMjdList.length != coord_posAxis_km2.length)
                | (timeMjdList.length != vei_arr1.length)
                | (timeMjdList.length != vei_arr2.length)) {
            throw new IllegalArgumentException("Inconsistent array dimensions.");
        } else if ((coord_posAxis_km1[0].length != 3)
                | (coord_posAxis_km2[0].length != 3)) {
            throw new IllegalArgumentException("Illegal array dimensions.");
        }

        final int N = timeMjdList.length;
        final double timeLengthMjd = timeMjdList[N - 1] - timeMjdList[0];
        // d = difference in position between the two trajectories (at a specific time) .

        final double[] d_abs_array = new double[N];
        final double[] d_x_array = new double[N];
        final double[] d_y_array = new double[N];
        final double[] d_z_array = new double[N];

        final double[] d_r_array = new double[N];
        final double[] d_v2_array = new double[N];
        final double[] d_rxv2_array = new double[N];
        final double[] d_v2xrxv2_array = new double[N];
        //final double[] dPosdt_div_v_array = new double[N];
        //dPosdt_div_v_array[0] = Double.NaN;   // Value must be ignored when doing statistics.
        final double[] d_v2p_array = new double[N];

        final double[] d_dPos2dtp_array = new double[N];
        d_dPos2dtp_array[0] = Double.NaN;
        final double[] v_dPos2dt_array = new double[N];
        v_dPos2dt_array[0] = Double.NaN;

        final double[] dPos2dt_dot_v2_norm2_array = new double[N];

        final double[] rotAxisX_array = new double[N];
        final double[] rotAxisY_array = new double[N];
        final double[] rotAxisZ_array = new double[N];
        final double[] rotAxis_abs_array = new double[N];

        for (int i = 0; i < N; i++) {
            final double[] pos1 = coord_posAxis_km1[i];
            final double[] pos2 = coord_posAxis_km2[i];
            final double[] v2 = vei_arr2[i];
            final double[] rxv2 = Vect.cross(pos2, v2);    // Orthogonal to both r and v.
            final double[] v2xrxv2 = Vect.cross(v2, rxv2);  // Orthogonal to both v and rxv.
            final double[] d = Vect.sub(pos2, pos1);
            // NOTE: (r,v,rxv) are NOT all orthogonal to each other.
            // NOTE: (v, rxv, vxrxv) ARE all orthogonal to each other.

            final double[] rotAxis = Vect.multiply(Vect.cross(pos1, pos2), 1 / (Vect.absv(pos1) * Vect.absv(pos2)));

            d_abs_array[i] = Vect.absv(d);
            d_x_array[i] = d[0];
            d_y_array[i] = d[1];
            d_z_array[i] = d[2];

            // NOTE: r and v are NOT perpendicular (but are at least unlikely to be parallel).
            d_r_array[i] = getVectorComponent(d, pos2, false);      // NOTE: r = pos2 = vector from origin.
            d_v2_array[i] = getVectorComponent(d, v2, false);
            d_rxv2_array[i] = getVectorComponent(d, rxv2, false);
            d_v2xrxv2_array[i] = getVectorComponent(d, v2xrxv2, false);

            d_v2p_array[i] = getVectorComponent(d, v2, true);   // NOTE: Double normalization of v. Useful for the case of exact time difference.

            rotAxisX_array[i] = rotAxis[0];
            rotAxisY_array[i] = rotAxis[1];
            rotAxisZ_array[i] = rotAxis[2];
            rotAxis_abs_array[i] = Vect.absv(rotAxis);

            /* Make comparisons between the current and the previous data point. */
            if (i > 0) {
                // IMPORTANT: One can only meaningfully compare positions over time if the time step is small enough!
                final double[] pos2_prev = coord_posAxis_km2[i - 1];
                final double[] dPos2 = Vect.sub(pos2, pos2_prev);
                final double dt_seconds = (timeMjdList[i] - timeMjdList[i - 1]) * Time.SECONDS_IN_DAY;
                final double[] dPos2dt = Vect.multiply(dPos2, 1 / dt_seconds);   // "Empirically derived" velocity vector.

                dPos2dt_dot_v2_norm2_array[i] = Vect.dot(dPos2dt, v2) / (Vect.absv(dPos2dt) * Vect.absv(v2));
                v_dPos2dt_array[i] = getVectorComponent(v2, dPos2dt, true);
                d_dPos2dtp_array[i] = getVectorComponent(d, dPos2dt, true);   // NOTE: Double normalization of v. Useful for the case of exact time difference.
            }
        }
        //for (int i = 1; i < N - 1; i++) {
        //final Time time = new Time(timeMjdList[i]);
        //String minMaxNotice = "";
            /*final boolean minimum = (dPos_abs_array[i - 1] > dPos_abs_array[i]) & (dPos_abs_array[i] < dPos_abs_array[i + 1]);
         final boolean maximum = (dPos_abs_array[i - 1] < dPos_abs_array[i]) & (dPos_abs_array[i] > dPos_abs_array[i + 1]);
         if (minimum | maximum) {
         minMaxNotice = " - min/max";
         //System.out.println("min/max");
         }*/
        //System.out.printf(time.toString() + ": d = %3.0f (%.2f), dPos_vp = "+dPos_vp_array[i]+""+minMaxNotice+"\n", dPos_abs_array[i], dPos_abs_array[i] - dPos_abs_array[i - 1]);
        //System.out.printf("dPos_abs=%5.1f, vei=%8.1f, dPos_vp=%f\n", d_abs_array[i], Vect.absv(vei_arr[i]), d_vp_array[i]);
        //}

        System.out.println("---");
        System.out.println("N = " + N);
        System.out.printf("Average time step: %.2f [s]\n", timeLengthMjd / (N - 1) * Time.SECONDS_IN_DAY);
        printStatistics("d_abs", new Statistics(d_abs_array, false));
        System.out.println("========");
        printStatistics("d_x", new Statistics(d_x_array, false));
        printStatistics("d_y", new Statistics(d_y_array, false));
        printStatistics("d_z", new Statistics(d_z_array, false));
        System.out.println("========");
        printStatistics("d_r", new Statistics(d_r_array, false));
        printStatistics("d_v2", new Statistics(d_v2_array, false));
        printStatistics("d_rxv2 (perp. to orbital plane)", new Statistics(d_rxv2_array, false));
        printStatistics("d_v2xrxv2", new Statistics(d_v2xrxv2_array, false));
        System.out.println("========");
        printStatistics("d_v2p", new Statistics(d_v2p_array, false));
        //printStatistics("dPos2dt_dot_v2_norm_array", new Statistics(dPos2dt_dot_v2_norm2_array, true));
        //printStatistics("v_dPos2dt_array", new Statistics(v_dPos2dt_array, true));
        //printStatistics("d_dPos2dtp_array", new Statistics(d_dPos2dtp_array, true));
        //System.out.println("========");
        //printStatistics("rotAxisX", new Statistics(rotAxisX_array, false));
        //printStatistics("rotAxisY", new Statistics(rotAxisY_array, false));
        //printStatistics("rotAxisZ", new Statistics(rotAxisZ_array, false));
        //printStatistics("rotAxis_abs", new Statistics(rotAxis_abs_array, false));
    }


    /**
     * IMPLEMENTATION NOTE: Not merged into<BR>
     * compareTrajectories(double[][] coord_posAxis_km1, double[][]
     * coord_posAxis_km2, double[][] vei_arr)<BR>
     * to make it possible to write comparison functions that make multiple
     * calls with different values for timeMjdList.
     */
    private static void compareTrajectories(
            TrajectoryDataSource src1,
            TrajectoryDataSource src2,
            double[] timeMjdList1,
            double timeMjdAddedToTrajectory2) throws IOException {

        final int N = timeMjdList1.length;
        final double[][] gei_arr_posAxis_km1 = new double[N][3];
        final double[][] vei_arr1 = new double[N][3];
        final double[][] gei_arr_posAxis_km2 = new double[N][3];
        final double[][] vei_arr2 = new double[N][3];

        double[] timeMjdList2 = new double[N];
        for (int i = 0; i < N; i++) {
            timeMjdList2[i] = timeMjdList1[i] + timeMjdAddedToTrajectory2;
        }

        src1.fill_GEI_VEI(timeMjdList1, gei_arr_posAxis_km1, vei_arr1);
        src2.fill_GEI_VEI(timeMjdList2, gei_arr_posAxis_km2, vei_arr2);

        System.out.println("Comparing:");
        System.out.println("   src1 =" + src1);
        System.out.println("   src2 =" + src2);

        compareTrajectories(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr1, vei_arr2, timeMjdList1);
    }


    //##########################################################################
    private static void printStatistics(String title, Statistics s) {
        //final String f = "%.2f";
        final String f = "%.2e";
        System.out.println(title);
        System.out.printf("   Mean +/- std deviation = " + f + " +/- " + f + "\n", s.mean, s.stdDeviation);
        System.out.printf("   Std deviation/|mean|   = " + f + "\n", s.stdDeviation / Math.abs(s.mean));
        System.out.printf("   (min, max)             = (" + f + "; " + f + ")\n", s.min, s.max);
        //System.out.printf("   (x_02, x_98)           = (" + f + "; " + f + ")\n", s.x_02, s.x_98);
        //System.out.printf("   (x_05, x_95)     = ("+format+"; "+format+")\n", s.x_05, s.x_95);
        //System.out.printf("   (i_min, i_max) = (%d; %d)\n", s.i_min, s.i_max);
    }


    /**
     * Get vector component in an arbitrary direction.
     *
     * @param v_ref NOTE: Must have dimensions (units) that match v if it is not
     * normalized.
     */
    private static double getVectorComponent(double[] v, double[] v_ref, boolean doubleNormalizeRef) {
        double C;
        if (doubleNormalizeRef) {
            C = Vect.absv2(v_ref);
        } else {
            C = Vect.absv(v_ref);
        }
        if (C == 0) {
            throw new IllegalArgumentException("v_ref is zero length.");
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


        public String toString() {
            return "SSCWS: " + satID;
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


        public String toString() {
            return "LTOF file: " + filePath;
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


        public String toString() {
            return "TLE file: " + filePath;
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
        public final double x_05, x_95;   // Percentiles.
        public final double x_02, x_98;   // Percentiles.
        // Flag for when encountering non-finite values?


        private Statistics(double[] xa, boolean permitNaN) {
            double N = xa.length;
            double xsum = 0, x2sum = 0;
            double tempMin = Double.POSITIVE_INFINITY;
            double tempMax = Double.NEGATIVE_INFINITY;
            int i_min_temp = -1, i_max_temp = -1;

            for (int i = 0; i < N; i++) {
                final double x = xa[i];
                if (!Double.isFinite(x)) {
                    if (Double.isNaN(x) && permitNaN) {
                        N--;
                        continue;
                    } else {
                        throw new IllegalArgumentException("Encountered non-finite value at i=" + i + ".");
                    }
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
            if (N == 0) {
                throw new IllegalArgumentException("Array only contains non-finite values.");
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
        return new File("/home/erjo/work_files/ovt_diverse/temp/OVT." + SSCWS_satID + ".SSCWS.cache");
    }
}
