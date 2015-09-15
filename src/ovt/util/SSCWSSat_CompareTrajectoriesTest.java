/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/SSCWSSat_CompareTrajectoriesTest.java $
 Date:      $Date: 2015/09/15 11:54: $
 Version:   $Revision: 1.0 $
 
 
 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P G Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson)
 
 =========================================================================*/
package ovt.util;

import gov.nasa.gsfc.spdf.ssc.client.CoordinateSystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ovt.OVTCore;
import ovt.datatype.Matrix3x3;
import ovt.datatype.Time;
import ovt.mag.model.IgrfModel;
import ovt.object.LTOFSat;
import ovt.object.SSCWSSat;
import ovt.object.TLESat;

/**
 * Informal manual test code for comparing trajectories to
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
 * MUST DEACTIVATE CACHING TO FILE ot avoid mixing of coordinate systems in the
 * disk cache over multiple sessions.
 * 
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
public class SSCWSSat_CompareTrajectoriesTest {

    /**
     * Useful when modifying OVT code obtaining/deriving SSCWS data.
     */
    private static final boolean USE_SSCWS_DISK_CACHE = true;
    //private static final boolean USE_SSCWS_DISK_CACHE = false;


    public static void main(String[] args) throws IOException {
        //test_testCode();
        test_pointCalculation();
        //test_compareTrajectories();
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

        compareTrajectoriesPrint(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr1, vei_arr2, timeMjdMap);
    }


    /**
     * Print the orbit for a single point in time. Used for JSOC's external test
     * cases.
     */
    public static void test_pointCalculation() throws IOException {

        /*final int N = 101;
         final int i = 51 - 1;  // The point to look at.
         final double timeMjd = Time.getMjd(1997, 01, 01, 00, 00, 00);
         final double[] timeMjdMap = Utils.newLinearArray(timeMjd - 1, timeMjd + 1, N);
         final TrajectoryDataSource tds = new LTOFFileDataSource(
         "/home/erjo/work_files/ovt_diverse/ESOC_LTOF_validation/ltof.cl1"
         );//*/
        final int N = 10000;
        final int i = 0 * N;  // The point to look at.
        //final double timeMjd = Time.getMjd(2013, 01, 30, 20, 39, 30);
        final double timeMjd = Time.getMjd(2000, 11, 01, 00, 00, 30);
        //final double timeMjd = 0;
        final double[] timeMjdMap = Utils.newLinearArray(timeMjd, timeMjd + 1, N);

        //final TrajectoryDataSource tds = new TLEFileDataSource("/home/erjo/work_files/ovt_diverse/Spacetrack_TLE_validation/SGP4/SGP4-VER.TLE");
        //final TrajectoryDataSource tds = new LTOFFileDataSource("/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof");
        //final TrajectoryDataSource tds = new SSCWSDataSource("cluster1", SSCWSLibraryImpl.DEFAULT_INSTANCE);
        final TrajectoryDataSource tds = new RawSSCWSDataSource("cluster1", CoordinateSystem.GEI_J_2000);

        /*final double[] timeMjdMap = {Time.getMjd(2005, 01, 01, 00, 00, 00)};
         TrajectoryDataSource tds = new LTOFFileDataSource(
         "/home/erjo/work_files/ovt/build/classes/odata/Double_Star_2.ltof"
         );*/
        final double[][] gei_arr_posAxis_km = new double[N][3];
        final double[][] vei_arr_posAxis_km = new double[N][3];
        //tds.fill_GEI_VEI(timeMjdMap, gei_arr_posAxis_km, vei_arr_posAxis_km);

        double mjd = timeMjdMap[i];
        double[] pos_GEI = gei_arr_posAxis_km[i];

        final Trans trans = new Trans(mjd, new IgrfModel(null));
        final Trans trans2 = new Trans(mjd-2.0, new IgrfModel(null));
        final Matrix3x3 gei_gsm = trans.gei_gse_trans_matrix();
        final Matrix3x3 gei_gsm2 = trans2.gei_gse_trans_matrix();
        double[] pos_GSE = gei_gsm.multiply(gei_arr_posAxis_km[i]);
        //System.out.println(mjd);
        //System.out.println(Time.Y2000);
        System.out.println("-----");
        printMatrix("gei_gsm", gei_gsm);
        printMatrix("gei_gsm2", gei_gsm2);
        System.out.println("-----");

        System.out.printf("pos GEI : %s\n", Arrays.toString(pos_GEI));
        System.out.printf("pos_GSE : %s\n", Arrays.toString(pos_GSE));

        List<TrajectoryPosition> valPosList = getValidationPositions_GSE();
        for (TrajectoryPosition valPos : valPosList) {
            printPointComparison(pos_GSE, valPos.pos);
        }

        // OVT results when running code for JSOC's verification test:
        // gei_arr : -93378,131462, 1951,879165, 45818,774600
        // vei_arr : -1,352745, 0,006500, -0,733957
    }


    private static void printMatrix(String label, Matrix3x3 m) {
        System.out.println("----- "+ label + " -----");
        System.out.println(m.toString());

    }


    // Intended for all coordinate systems, same or different times.
    private static void printPointComparison(double[] pos1, double[] pos2) {
        System.out.println("Distance =  " + Vect.absv(Vect.sub(pos1, pos2)));
    }

    /**
     * Position at a specific time on specific trajectory. Used for all
     * coordinate system. Primarily intended for storing validation points.
     */
    private static class TrajectoryPosition {

        final double mjd;
        final double[] pos;


        TrajectoryPosition(double mMjd, double[] mPos) {
            mjd = mMjd;
            pos = mPos;
        }
    }


    private static List<TrajectoryPosition> getValidationPositions_GSE() {
        final List<TrajectoryPosition> positions = new ArrayList();

        positions.add(new TrajectoryPosition(Time.getMjd(2000, 11, 01, 00, 00, 00), new double[]{-20454.7, 67241.1, -41574.4}));
        positions.add(new TrajectoryPosition(Time.getMjd(2000, 11, 01, 00, 01, 00), new double[]{-20381.2, 67141.4, -41603.4}));
        //positions.add(new TrajectoryPosition(Time.getMjd(2000, 11, 01, 00, 02, 00), new double[]{-20307.7, 67041.6, -41632.4}));
        //positions.add(new TrajectoryPosition(Time.getMjd(2000, 11, 01, 00, 03, 00), new double[]{-20234.1, 66941.6, -41661.2}));

        // @spis: /data/caalocal/C1_CP_AUX_POSGSE_1M/C1_CP_AUX_POSGSE_1M__20130101_000000_20130131_235959_V140124.cdf
        //positions.add(new TrajectoryPosition(Time.getMjd(2013, 01, 30, 20, 39, 00), new double[]{101855.0, 47392.6, -50969.2}));
        //positions.add(new TrajectoryPosition(Time.getMjd(2013, 01, 30, 20, 40, 00), new double[]{101860.0, 47356.0, -50921.4}));
        //positions.add(new TrajectoryPosition(Time.getMjd(2013, 01, 30, 20, 41, 00), new double[]{101865.0, 47319.3, -50873.6}));
        //positions.add(new TrajectoryPosition(Time.getMjd(2013, 01, 30, 20, 42, 00), new double[]{101869.0, 47282.5, -50825.8}));
        return positions;
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

        final int N = 10000;   // Note: 1 day = 1440 min.
        //final double startMjd = Time.getMjd(2005, 1, 1, 0, 0, 0);
        final double startMjd = Time.getMjd(1997, 1, 1, 0, 0, 0);
        final double lengthMjd = 120;
        //final double lengthMjd = 19/24.0;
        final double timeDifferenceMjd2 = Time.DAYS_IN_SECOND * 0; // -21.7;
        final double[] rotationVec2 = {0, 0, 0};
        //final double[] rotationVec2 = {Math.PI/2, 0, 0};
        //final double[] rotationVec2 = {2*5.0/Const.RE, 0, 0};
        //--------------------------
        /*compareTrajectories(
         new SSCWSDataSource("cluster1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         //new SSCWSDataSource("cluster1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new LTOFFileDataSource("/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"),
         //new LTOFFileDataSource("/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"),
         Utils.newLinearArray(startMjd, startMjd + lengthMjd, N), timeDifferenceMjd2, rotationVec2);//*/
        //--------------------------
        /*compareTrajectories(
         new SSCWSDataSource("doublestar1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new LTOFFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Double_Star_1.ltof"),
         Utils.newLinearArray(startMjd, startMjd + lengthMjd, N), timeDifferenceMjd2, rotationVec2);//*/
        //--------------------------
        /*compareTrajectories(
         //new SSCWSDataSource("doublestar1", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         new SSCWSDataSource("doublestar2", SSCWSLibraryImpl.DEFAULT_INSTANCE),
         //new LTOFFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Double_Star_1.ltof"),
         new LTOFFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Double_Star_2.ltof"),
         Utils.newLinearArray(startMjd, startMjd + lengthMjd, N), timeDifferenceMjd);//*/
        //--------------------------
        /*compareTrajectories(
         new LTOFFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Cluster1.ltof"),
         new LTOFFileDataSource("/home/erjo/work_files/INBOX/SUPER_LTOF_C1.CR.ltof"),
         Utils.newLinearArray(startMjd, startMjd + lengthMjd, N), timeDifferenceMjd2, rotationVec2);//*/
        //--------------------------
        compareTrajectories(
                new SSCWSDataSource("polar", SSCWSLibraryImpl.DEFAULT_INSTANCE),
                new TLEFileDataSource("/home/erjo/work_files/ovt/build/classes/odata/Polar.tle"),
                Utils.newLinearArray(startMjd, startMjd + lengthMjd, N), timeDifferenceMjd2, rotationVec2);//*/
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
    private static void compareTrajectoriesPrint(
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
        final double[] d_v_array = new double[N];
        final double[] abs_v_array = new double[N];
        final double[] d_rxv_array = new double[N];
        final double[] d_v_x_rxv_array = new double[N];
        //final double[] dPosdt_div_v_array = new double[N];
        //dPosdt_div_v_array[0] = Double.NaN;   // Value must be ignored when doing statistics.
        final double[] d_vp_array = new double[N];

        final double[] d_dPos2dtp_array = new double[N];
        d_dPos2dtp_array[0] = Double.NaN;
        final double[] v_dPos2dt_array = new double[N];
        v_dPos2dt_array[0] = Double.NaN;

        final double[] dPos2dt_dot_v2_norm2_array = new double[N];

        //final double[] rotAxisX_array = new double[N];
        //final double[] rotAxisY_array = new double[N];
        //final double[] rotAxisZ_array = new double[N];
        //final double[] rotAxis_abs_array = new double[N];
        final double[] totEnergy1_array = new double[N];
        final double[] totEnergy2_array = new double[N];

        for (int i = 0; i < N; i++) {
            final double[] pos1 = coord_posAxis_km1[i];
            final double[] pos2 = coord_posAxis_km2[i];
            final double[] r = pos2;
            final double[] v = vei_arr2[i];
            final double[] rxv = Vect.cross(r, v);     // Orthogonal to both r and v.
            final double[] v_x_rxv = Vect.cross(v, rxv);  // Orthogonal to both v and rxv.
            final double[] d = Vect.sub(pos2, pos1);
            // NOTE: (r,v,rxv) are NOT all orthogonal to each other.
            // NOTE: (v, rxv, vxrxv) ARE all orthogonal to each other.

            //double[] rotAxis = Vect.multiply(Vect.cross(pos1, pos2), 1 / (Vect.absv(pos1) * Vect.absv(pos2)));   // Length = sin(theta), not theta
            {
                // Make vector length equal to rotation angle - NOTE: No difference for small angles << 1.
                //final double angle = Math.asin(Vect.absv(rotAxis));   // NOTE: Vect.absv(rotAxis)>0 ==> angle>0, despite that Math.asin can be negative for negative arguments.
                //rotAxis = Vect.multiply(Vect.norm(rotAxis), angle);
            }

            d_abs_array[i] = Vect.absv(d);
            d_x_array[i] = d[0];
            d_y_array[i] = d[1];
            d_z_array[i] = d[2];

            // NOTE: r and v are NOT perpendicular (but they are at least unlikely to be parallel).
            d_r_array[i] = Vect.absv(pos2) - Vect.absv(pos1);  // Works for large angle differences.
            d_v_array[i] = getVectorComponent(d, v, false);
            abs_v_array[i] = Vect.absv(v);
            d_rxv_array[i] = getVectorComponent(d, rxv, false);
            d_v_x_rxv_array[i] = getVectorComponent(d, v_x_rxv, false);

            d_vp_array[i] = getVectorComponent(d, v, true);   // NOTE: Double normalization of v. Interprets the difference as due to motion in seconds. Useful for the case of exact time difference.

            //rotAxisX_array[i] = rotAxis[0];
            //rotAxisY_array[i] = rotAxis[1];
            //rotAxisZ_array[i] = rotAxis[2];
            //rotAxis_abs_array[i] = Vect.absv(rotAxis);   // NOTE: Not meaningfull if vector already normalized.
            final Utils.OrbitalState s1 = new Utils.OrbitalState(Vect.multiply(pos1, 1e3), Vect.multiply(vei_arr1[i], 1e3));
            totEnergy1_array[i] = s1.E_orbital_norm_SI;
            final Utils.OrbitalState s2 = new Utils.OrbitalState(Vect.multiply(pos2, 1e3), Vect.multiply(vei_arr2[i], 1e3));
            totEnergy2_array[i] = s2.E_orbital_norm_SI;

            /* Make comparisons between the current and the previous data point. */
            if (i > 0) {
                // IMPORTANT: One can only meaningfully compare positions over time if the time step is small enough!
                final double[] pos2_prev = coord_posAxis_km2[i - 1];
                final double[] dPos2 = Vect.sub(pos2, pos2_prev);
                final double dt_seconds = (timeMjdList[i] - timeMjdList[i - 1]) * Time.SECONDS_IN_DAY;
                final double[] dPos2dt = Vect.multiply(dPos2, 1 / dt_seconds);   // "Empirically derived" velocity vector.

                dPos2dt_dot_v2_norm2_array[i] = Vect.dot(dPos2dt, v) / (Vect.absv(dPos2dt) * Vect.absv(v));
                v_dPos2dt_array[i] = getVectorComponent(v, dPos2dt, true);
                d_dPos2dtp_array[i] = getVectorComponent(d, dPos2dt, true);   // NOTE: Double normalization of v. Useful for the case of exact time difference.
            }

            if (Vect.absv2(r) < 1) {
                System.out.println("Found suspicious position close to origin.");
                throw new RuntimeException("Found suspicious position close to origin.");
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
        printStatistics("d_v", new Statistics(d_v_array, false));
        printStatistics("d_rxv (normal to orbital plane)", new Statistics(d_rxv_array, false));
        printStatistics("d_vx(rxv) (perp. to both velocity and the normal to the orbital plane)", new Statistics(d_v_x_rxv_array, false));
        System.out.println("========");
        printStatistics("d_vp", new Statistics(d_vp_array, false));
        //printStatistics("dPos2dt_dot_v2_norm_array", new Statistics(dPos2dt_dot_v2_norm2_array, true));
        //printStatistics("v_dPos2dt_array", new Statistics(v_dPos2dt_array, true));
        //printStatistics("d_dPos2dtp_array", new Statistics(d_dPos2dtp_array, true));
        //System.out.println("========");
        //printStatistics("rotAxisX", new Statistics(rotAxisX_array, false));
        //printStatistics("rotAxisY", new Statistics(rotAxisY_array, false));
        //printStatistics("rotAxisZ", new Statistics(rotAxisZ_array, false));
        //printStatistics("rotAxis_abs", new Statistics(rotAxis_abs_array, false));
        //System.out.println("========");
        //printStatistics("abs_v", new Statistics(abs_v_array, false));
        //printStatistics("totEnergy1", new Statistics(totEnergy1_array, false));
        //printStatistics("totEnergy2", new Statistics(totEnergy2_array, false));
    }


    /**
     * Compare two trajectories, and optionally modify one of them.
     */
    private static void compareTrajectories(
            TrajectoryDataSource src1,
            TrajectoryDataSource src2,
            double[] timeMjdList1,
            double timeMjdAddedToTrajectory2,
            double[] rotationVec2) throws IOException {

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

        final Matrix3x3 R2 = Matrix3x3.getRotationMatrix(rotationVec2);
        for (int i = 0; i < N; i++) {
            gei_arr_posAxis_km2[i] = R2.multiply(gei_arr_posAxis_km2[i]);
            vei_arr2[i] = R2.multiply(vei_arr2[i]);
        }

        System.out.println("Comparing:");
        System.out.println("   src1 = " + src1);
        System.out.println("   src2 = " + src2);
        System.out.println("   timeMjdAddedToTrajectory2  / Time.DAYS_IN_SECOND = " + timeMjdAddedToTrajectory2 / Time.DAYS_IN_SECOND);
        System.out.println("   rotationVec2                                     = " + Arrays.toString(rotationVec2));
        System.out.println("   timeMjdList1[0]   = " + new Time(timeMjdList1[0]));
        System.out.println("   timeMjdList1[N-1] = " + new Time(timeMjdList1[N - 1]));

        compareTrajectoriesPrint(gei_arr_posAxis_km1, gei_arr_posAxis_km2, vei_arr1, vei_arr2, timeMjdList1);
    }


    //##########################################################################
    private static void printStatistics(String title, Statistics s) {
        //final String f = "%.2f";
        final String f = "%.2e";
        System.out.println(title);
        System.out.printf("   Mean +/- std deviation = " + f + " +/- " + f + "\n", s.mean, s.stdDeviation);
        System.out.printf("   Std deviation/|mean|   = " + f + "\n", s.stdDeviation / Math.abs(s.mean));
        System.out.printf("   (min, max)             = (" + f + "; " + f + ")\n", s.min, s.max);
        System.out.printf("   (x_02, x_98)           = (" + f + "; " + f + ")\n", s.x_02, s.x_98);
        System.out.printf("   (x_05, x_95)           = (" + f + "; " + f + ")\n", s.x_05, s.x_95);
        //System.out.printf("   (i_min, i_max)         = (%d; %d)\n", s.i_min, s.i_max);
    }


    /**
     * Get vector component in an arbitrary direction.
     *
     * @param v_ref NOTE: Unit important iff doubleNormalizeRef==true.
     * @param doubleNormalizeRef False: Do not change the length of the
     * projected vector. True: Divide the length of the projected vector by
     * abs(v_ref). If v [km] and v_ref [km/s], the return result is [s].
     */
    private static double getVectorComponent(double[] v, double[] v_ref, boolean doubleNormalizeRef) {
        double C;
        if (doubleNormalizeRef) {
            C = Vect.absv2(v_ref);  // Will effectively normalize v_ref (and remove its unit), then divide the result by abs(v_ref) (which add a unit).
        } else {
            C = Vect.absv(v_ref);   // Will effectively normalize v_ref (and remove its unit).
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
    // Fetch data via SSCWSSat.
    private static class SSCWSDataSource implements TrajectoryDataSource {

        private final SSCWSSat.DataSource dataSource;
        private final String satID;   // Save cache file instead?


        public SSCWSDataSource(String mSatID, SSCWSLibrary mLib) throws IOException {
            satID = mSatID;
            File cacheFile = null;
            if (USE_SSCWS_DISK_CACHE) {
                cacheFile = selectCacheFile(mSatID);   // Needed for loading cache from the right file.
            }
            dataSource = new SSCWSSat.DataSource(mLib, mSatID, cacheFile);
        }


        // NOTE: Saves cache to disk after every read. Maybe somewhat inefficient.
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
    // Fetch data directly from SSCWSLibraryImpl.
    private static class RawSSCWSDataSource implements TrajectoryDataSource {

        private final CoordinateSystem coordSys;
        private final String satID;


        public RawSSCWSDataSource(String mSatID, CoordinateSystem mCoordSys) {
            satID = mSatID;
            coordSys = mCoordSys;
        }


        @Override
        public void fill_GEI_VEI(double[] timeMjdMap, double[][] gei_arr_posAxis_km, double[][] vei_arr) throws IOException {
            SSCWSSat_CompareTrajectoriesTest.fill_pos_vel_RawSSCWS(satID, timeMjdMap, gei_arr_posAxis_km, vei_arr, coordSys);
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
     * Retrieve data in a way similar to the way used in the "propoer" code, but
     * (1) without caching, (2) with arbitrary coordinate system as delivered
     * from SSC, (3) always highest resolution.
     *
     * Function is intended for testing without having to change coordinate
     * system in OVT proper (and avoid enabling/disabling disk cache).
     */
    public static void fill_pos_vel_RawSSCWS(
            String satID,
            double[] timeMjdMap,
            double[][] gei_arr_posAxis_km,
            double[][] vei_arr_posAxis_kms,
            CoordinateSystem coordSys)
            throws IOException {

        final SSCWSLibraryImpl lib = (SSCWSLibraryImpl) SSCWSLibraryImpl.DEFAULT_INSTANCE;

        final int resolutionFactor = 1;
        final int timeResolution_s = lib.getSatelliteInfo(satID).bestTimeResolution * resolutionFactor;
        final double timeMarginMjd = 2 * timeResolution_s * Time.DAYS_IN_SECOND;
        final double beginMjd = timeMjdMap[0];
        final double endMjd = timeMjdMap[timeMjdMap.length - 1];

        // NOTE: coords_axisPos_kmMjd[3][..] = time.
        final double[][] coords_axisPos_kmMjd = lib.getTrajectoryRaw_GEI(
                satID,
                beginMjd - timeMarginMjd,
                endMjd + timeMarginMjd,
                resolutionFactor, coordSys);

        final List<Integer> dataGaps = Utils.findJumps(coords_axisPos_kmMjd[3], timeResolution_s * 2 * Time.DAYS_IN_SECOND);
        if (!dataGaps.isEmpty()) {
            throw new IOException("Found data gaps in the requested interval.");
        }

        final double[] interpCoords_pos_km = new double[timeMjdMap.length];         // Temporary variable for one X/Y/Z axis.
        final double[] interpVelocity_pos_kmMjd = new double[timeMjdMap.length];    // Temporary variable for one X/Y/Z axis.
        for (int i_axis = 0; i_axis < 3; i_axis++) {
            // NOTE: The indata covers a larger time interval than requested by user,
            // but the interpolated times are still only the ones the caller requested.
            // NOTE: Time unit is mjd. Therefore, interpolated velocity is km/day.
            Utils.cubicSplineInterpolation(
                    coords_axisPos_kmMjd[3],
                    coords_axisPos_kmMjd[i_axis],
                    timeMjdMap,
                    interpCoords_pos_km,
                    interpVelocity_pos_kmMjd,
                    Utils.SplineInterpolationBC.SET_SECOND_DERIV,
                    Utils.SplineInterpolationBC.SET_SECOND_DERIV
            );

            for (int i_pos = 0; i_pos < gei_arr_posAxis_km.length; i_pos++) {
                gei_arr_posAxis_km[i_pos][i_axis] = interpCoords_pos_km[i_pos];
                vei_arr_posAxis_kms[i_pos][i_axis] = interpVelocity_pos_kmMjd[i_pos] / Time.SECONDS_IN_DAY;   // Convert unit from km/day to km/s.
            }
        }
    }

    //##########################################################################
    /**
     * Class for compiling statistics for the contents of an array.
     */
    // PROPOSAL: Introduce weights.
    //    PRO: Can weight by time (weight=time difference=constant), length (inverse velocity), radians.
    //    CON: Complicated implementation for percentiles.
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
