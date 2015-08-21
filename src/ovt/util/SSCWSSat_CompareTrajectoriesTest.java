/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.File;
import java.io.IOException;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.object.LTOFSat;
import ovt.object.SSCWSSat;
import ovt.object.TLESat;

/**
 * Informal test code for comparing trajectories to determine differences in coordinate systems.
 * Used in particular to verify the coordinate system SSCWS satellites.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class SSCWSSat_CompareTrajectoriesTest {
    
    /**
     * Informal test code for comparing coords_axisPos_kmMjd data for the same
     * satellite from two different sources (instance of "Sat"). Used to verify
     * that the same coordinate system is used.
     */
    // PROPOSAL: Try trajectories of more satellites than cluster.
    // PROPOSAL: Verify coordinate system of LTOF files?
    public static void test_compareTrajectories() throws IOException {
        if (false) {
            // Test code for test_compareTrajectories (test code for the test code...).
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

            test_compareTrajectories(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr);
        }
        if (false) {
            /*
             --- SSC Web Services ---
             getID(): cluster1;   getName(): Cluster-1 (FM5/Rumba)
             getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
             getID(): cluster2;   getName(): Cluster-2 (FM6/Salsa)
             getStartTime(): 2000-08-22T00:02:30Z;   getEndTime(): 2019-12-31T23:59:30Z;   getResolution(): 60
             // Double star 1 : LTOF-fil är konstig. Oanvändbar för jämförelser.
             */

            final int N = 100;
            //test_compare_LTOF_SSCWS_Trajectories("cluster1", "/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof",
            test_compare_LTOF_SSCWS_Trajectories("cluster1", "/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR",
                    Time.getMjd(2001, 01, 01, 00, 00, 00),
                    Time.getMjd(2001, 01, 11, 00, 00, 00), N);//*/
            /*test_compare_LTOF_SSCWS_Trajectories("cluster2", "/home/erjo/work_files/ovt/build/classes/odata/Cluster2.ltof",
             Time.getMjd(2001, 01, 01, 00, 00, 00),
             Time.getMjd(2001, 01, 10, 00, 00, 00), N);//*/

        }
        if (true) {
            final int N = 100;
            test_compare_TLE_SSCWS_Trajectories("polar", "/home/erjo/work_files/ovt/build/classes/odata/Polar.tle",
                    Time.getMjd(2001, 01, 01, 00, 00, 00),
                    Time.getMjd(2001, 01, 11, 00, 00, 00), N);
        }

    }


    /**
     * Test code.
     */
    private static void test_compare_TLE_SSCWS_Trajectories(
            String satIdSSCWS, String filePathLTOF,
            double beginMjd, double endMjd, int N) throws IOException {

        final double[][] gei_arr_posAxis_km_SSCWS = new double[N][3];
        final double[][] gei_arr_posAxis_km_TLE = new double[N][3];
        final double[][] vei_arr = new double[N][3];
        final double[] timeMjdMap = ovt.util.Utils.newLinearArray(beginMjd, endMjd, N);

        System.loadLibrary("ovt-" + OVTCore.VERSION);   // Required for calling native code.
        TLESat.getSatPosJNI(filePathLTOF, timeMjdMap, gei_arr_posAxis_km_TLE, vei_arr, timeMjdMap.length);
        //--------------------------------        
        final SSCWSLibrary lib = ovt.util.SSCWSLibraryImpl.DEFAULT_INSTANCE;
        final SSCWSSat.DataSource sscwsDataSource = new SSCWSSat.DataSource(lib, satIdSSCWS, null);
        sscwsDataSource.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km_SSCWS, vei_arr);

        test_compareTrajectories(gei_arr_posAxis_km_SSCWS, gei_arr_posAxis_km_TLE, vei_arr);
    }


    /**
     * Test code.
     */
    private static void test_compare_LTOF_SSCWS_Trajectories(
            String satIdSSCWS, String filePathLTOF,
            double beginMjd, double endMjd, int N) throws IOException {
        final SSCWSLibrary lib = ovt.util.SSCWSLibraryImpl.DEFAULT_INSTANCE;
        final SSCWSSat.DataSource sscwsDataSource = new SSCWSSat.DataSource(lib, satIdSSCWS, null);

        final double[][] gei_arr_posAxis_km_SSCWS = new double[N][3];
        final double[][] gei_arr_posAxis_km_LTOF = new double[N][3];
        final double[][] vei_arr = new double[N][3];

        final double[] timeMjdMap = ovt.util.Utils.newLinearArray(beginMjd, endMjd, N);

        LTOFSat.fill_GEI_VEI_Raw(new File(filePathLTOF), timeMjdMap, gei_arr_posAxis_km_LTOF, vei_arr);
        sscwsDataSource.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km_SSCWS, vei_arr);

        test_compareTrajectories(gei_arr_posAxis_km_SSCWS, gei_arr_posAxis_km_LTOF, vei_arr);
    }


    /**
     * Compare trajectories and print measures of the difference. Used for
     * verifying that the same trajectory, retrieved from two different sources,
     * are in the same coordinate system.
     *
     * Divides the difference into parts parallel and perpendicular to vei_arr
     * to detect position differences due to (1) a constant difference ion time
     * (which gives position differences approximately parallel to the
     * velocity), and (2) difference in (spatial) coordinate systems.
     *
     * For automated testing.
     */
    private static void test_compareTrajectories(double[][] coord_posAxis_km1, double[][] coord_posAxis_km2, double[][] vei_arr) {
        if (coord_posAxis_km1[0].length != 3) {
            throw new IllegalArgumentException("Illegal array dimensions.");
        }
        final int N = coord_posAxis_km1.length;
        double d_max = 0;
        double d2_sum = 0;
        double d_vpara_max = 0;
        double d2_vpara_sum = 0;
        double d_vperp_max = 0;
        double d2_vperp_sum = 0;

        for (int i = 0; i < N; i++) {
            final double[] pos1 = new double[]{coord_posAxis_km1[i][0], coord_posAxis_km1[i][1], coord_posAxis_km1[i][2]};
            final double[] pos2 = new double[]{coord_posAxis_km2[i][0], coord_posAxis_km2[i][1], coord_posAxis_km2[i][2]};
            final double[] v = vei_arr[i];
            final double[] diff = Vect.sub(pos2, pos1);
            //System.out.println("---");
            //System.out.println("pos1 = (" + pos1[0] + ", " + pos1[1] + ", " + pos1[2] + ")");
            //System.out.println("pos2 = (" + pos2[0] + ", " + pos2[1] + ", " + pos2[2] + ")");
            //System.out.println("v = " + Arrays.toString(v));
            //System.out.println("diff = " + Arrays.toString(diff));
            //System.out.println("diff[i] / v[i] = "+diff[0]/v[0]+", "+diff[1]/v[1]+", "+diff[2]/v[2]);   // To manually check that the vectors are approx. parallel.

            final double[] v_norm = Vect.norm(v);
            final double[] diff_norm = Vect.norm(diff);

            final double s = Vect.dot(diff_norm, v_norm);
            //System.out.println("s = "+s);

            final double[] diff_vpara = Vect.multiply(diff, s);
            final double[] diff_vperp = Vect.sub(diff, diff_vpara);
            //System.out.println("diff_vpara = " + Arrays.toString(diff_vpara));
            //System.out.println("diff_vperp = " + Arrays.toString(diff_vperp));

            final double d = ovt.util.Vect.absv(diff);
            final double d_vpara = ovt.util.Vect.absv(diff_vpara);
            final double d_vperp = ovt.util.Vect.absv(diff_vperp);

            d_max = Math.max(d_max, d);
            d2_sum = d2_sum + d * d;

            d_vpara_max = Math.max(d_vpara_max, d_vpara);
            d2_vpara_sum = d2_vpara_sum + d_vpara * d_vpara;

            d_vperp_max = Math.max(d_vperp_max, d_vperp);
            d2_vperp_sum = d2_vperp_sum + d_vperp * d_vperp;
        }
        final double d_rms = Math.sqrt(d2_sum / N);
        final double d_vpara_rms = Math.sqrt(d2_vpara_sum / N);
        final double d_vperp_rms = Math.sqrt(d2_vperp_sum / N);

        System.out.println("---");
        System.out.println("N = " + N);
        System.out.println("d_max = " + d_max + " [km]");
        System.out.println("d_rms = " + d_rms + " [km]");
        System.out.println("d_vpara_max = " + d_vpara_max + " [km]");
        System.out.println("d_vpara_rms = " + d_vpara_rms + " [km]");
        System.out.println("d_vperp_max = " + d_vperp_max + " [km]");
        System.out.println("d_vperp_rms = " + d_vperp_rms + " [km]");
    }
}
