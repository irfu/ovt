/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.object;

import ovt.util.SSCWSOrbitCache;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import ovt.Const;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.mag.model.IgrfModel;
import ovt.util.Log;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;
import ovt.util.SSCWSOrbitCache.OrbitalData;
import ovt.util.Trans;
import ovt.util.TransCollection;
import ovt.util.Utils.OrbitalState;
import ovt.util.Vect;

/**
 * @author Erik P_SI G Johansson, erik.johansson@irfu.se
 *
 * Sat subclass for satellites where OVT itself downloads data from SSC Web
 * Services (SSC WS) via the internet and caches the data internally.<BR>
 */
// PROPOSAL: Change name? 
//    PRO: The common thread is not SSC Web Services?
//    PRO: Caching and download code is outside of class?
//
public class SSCWSSat extends Sat {

    private static final int DEBUG = 1;   // Set the minimum log message level for this class.
    private static final double CACHE_SLOT_SIZE_MJD = 1.0;
    //private static final int PROACTIVE_CACHE_FILL_MARGIN_SLOTS = 10;
    private static final int PROACTIVE_CACHE_FILL_MARGIN_SLOTS = 10;
    private static final int SATELLITE_NBR = 1;     // Made-up value. Should be OK for single satellites(?).

    public final SSCWSSat.DataSource dataSource;


    /**
     * @param SSCWS_satID The satellite ID string used by SSC Web Services to
     * reference satellites.
     *
     * @see SatelliteDescription#getId().
     */
    public SSCWSSat(OVTCore core, SSCWSLibrary sscwsLibrary, String SSCWS_satID) throws IOException {
        super(core);
        //try {
        dataSource = new DataSource(sscwsLibrary, SSCWS_satID);
        /*} catch (Exception e) {
         throw new IOException("Can not download satellite description from SSC Web Services: " + e.getMessage(), e);
         }*/

    }


    @Override
    public void setOrbitFile(File orbitFile) throws IOException {
        if (orbitFile != null) {
            throw new RuntimeException("There must be no orbitFile for this class."
                    + " This exception indicates a pure OVT bug.");
        }
        super.setOrbitFile(orbitFile);
        //throw new RuntimeException("Method not supported by this class since it does not make use of an coords_axisPos_kmMjd file. This exception indicates a bug.");
    }


    @Override
    double[] getFirstLastMjdPeriodSatNumber() throws IOException {

        final OrbitalState state = dataSource.getRepresentativeOrbitalState();

        double period = Double.NaN;   // Default value.
        if (state != null) {
            period = state.P_SI * Time.DAYS_IN_SECOND;
        }

        return new double[]{dataSource.satInfo.availableBeginTimeMjd, dataSource.satInfo.availableEndTimeMjd, period, SATELLITE_NBR};
    }


    @Override
    /**
     * NOTE: GEI=geocentric equatorial inertial, VEI=velocity in GEI
     * (presumably).
     *
     * NOTE: Presently uses only LINEAR interpolation to produce positions for
     * the requested points in time.
     */
    void fill_GEI_VEI(double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr) throws IOException {
        this.dataSource.fill_GEI_VEI(timeMjdMap, gei_arr, vei_arr);
    }


    /**
     * Function for deriving the name to be displayed in the GUI tree in a
     * standardized fashion (in one single location in the code). Useful for
     * finding the right SSCWSSat object in the GUI tree given only the SSCWS
     * Satellite ID. Used for the "name" property (field) that is set/read with
     * OVTObject#setName and OVTObject#getName.
     */
    public static String deriveNameFromSSCWSSatID(String satID) throws IOException {
        final String satName = OVTCore.SSCWS_LIBRARY.getSatelliteInfo(satID).name;   // throws IOException

        return "(SSC) "+satName;
        //return satID + " (SSC)";
    }

    //##########################################################################
    /**
     * The parent class delegates most of the work to this class. This looks
     * somewhat ugly at first but the reason for this is to divide the
     * functionality into parts which are connected to the GUI (SSCWSSat) and
     * parts which are not (SSCWSSat.DataSource). This is necessary in order to
     * easily be able to automatically test trajectory data (without creating a
     * GUI), in particular to compare trajectories for the same satellite but
     * from different source to verify the coordinate system.
     */
    public static class DataSource {

        public final SSCWSSatelliteInfo satInfo;
        private final SSCWSOrbitCache cache;
        private OrbitalState representativeOrbitalState = null;
        /**
         * Time resolution in seconds.
         */
        private final int timeResolutionToRequest;


        public DataSource(SSCWSLibrary mSSCWSLibrary, String SSCWS_satID) throws IOException {
            this.cache = new SSCWSOrbitCache(mSSCWSLibrary, SSCWS_satID, CACHE_SLOT_SIZE_MJD, PROACTIVE_CACHE_FILL_MARGIN_SLOTS);
            this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWS_satID);
            timeResolutionToRequest = getTimeResolutionToRequest();
        }


        /**
         * Analogous to Sat#fill_GEI_VEI. Its implementation should delegate to
         * this one.
         *
         * @param vei_arr_posAxis
         */
        public void fill_GEI_VEI(
                double[] timeMjdMap,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr_posAxis)
                throws IOException {

            /*=============
             Argument check.
             ==============*/
            if ((gei_arr_posAxis_km.length > 0) && (gei_arr_posAxis_km[0].length != 3)) {
                throw new IllegalArgumentException("Illegal array dimensions: gei_arr_posAxis[0].length != 3");
            }
            if ((vei_arr_posAxis.length > 0) && (vei_arr_posAxis[0].length != 3)) {
                throw new IllegalArgumentException("Illegal array dimensions: vei_arr_posAxis[0].length != 3");
            }

            final int INDEX_MARGIN = 1;   // Depends on what is optimal for the interpolation.
            final double beginReqMjd = timeMjdMap[0];     // Req = Request/requested
            final double endReqMjd = timeMjdMap[timeMjdMap.length - 1];

            final SSCWSOrbitCache.OrbitalData data = cache.getOrbitData(
                    beginReqMjd, endReqMjd,
                    RoundingMode.CEILING, RoundingMode.FLOOR,
                    INDEX_MARGIN, INDEX_MARGIN, timeResolutionToRequest);
            if (data.coords_axisPos_kmMjd[3].length < 2) {
                throw new IOException("Less than two data points available for the specified time interval. Can not interpolate.");
            }
            if (!data.dataGaps.isEmpty()) {
                throw new IOException("Orbital data contains " + data.dataGaps.size() + " data gap(s) in the selected time interval.");
                /* PROPOSAL: Use new Exception subclass DataGapException for this?!! Is not really an I/O error.
                 NOTE: Code may react to data gap outside the requested time interval
                 since it uses data points outside of it for interpolation.*/
            }

            final double[] interpCoords_pos_km = new double[timeMjdMap.length];      // For one X/Y/Z axis.
            final double[] interpVelocity_pos_kmMjd = new double[timeMjdMap.length];    // For one X/Y/Z axis.
            for (int i_axis = 0; i_axis < 3; i_axis++) {
                ovt.util.Utils.linearInterpolation(
                        data.coords_axisPos_kmMjd[3],
                        data.coords_axisPos_kmMjd[i_axis],
                        timeMjdMap,
                        interpCoords_pos_km,
                        interpVelocity_pos_kmMjd);

                for (int i_pos = 0; i_pos < gei_arr_posAxis_km.length; i_pos++) {
                    gei_arr_posAxis_km[i_pos][i_axis] = interpCoords_pos_km[i_pos];
                    vei_arr_posAxis[i_pos][i_axis] = interpVelocity_pos_kmMjd[i_pos];
                }
            }

        }


        /**
         * Calculate a time resolution to request when requesting data.
         *
         * This function could in principle be redesigned to give different
         * results over time and be called directly when requesting data (and
         * thus take the request into account?).
         */
        private int getTimeResolutionToRequest() throws IOException {
            final double PERIGEE_TRAJECTORY_TIMESCALE_FRACTION = 0.1;
            final double COORD_SYS_ROTATION_TIMESCALE_FRACTION = 0.01;
            /**
             * Try to determine sensible time resolution to use for data.<BR>
             * NOTE: Only SI units without prefixes, e.g. seconds.
             *
             * First calculate some timescales for acceleration that MIGHT be
             * useful, then select what to actually use.
             */

            // NOTE: Should be unnecessary if perigeeTimeScale works.
            final OrbitalState orbitalState = getRepresentativeOrbitalState();  // Useful variable for debugging.
            final double orbitalPeriod = orbitalState.P_SI;

            /**
             * Fastest coordinate system rotation. Needed since some coordinate
             * systems rotate or wobble due to the rotation of the Earth
             * relative to the Sun or to the Universe.
             */
            final double coordSysRotationPeriod = Time.SECONDS_IN_DAY;

            // Time scale from angular velocity at r_perigee_SI.
            final double perigeeTimescale = 1.0 / getRepresentativeOrbitalState().omega_perigee_SI;   // IOException

            double timeResolution = Math.min(
                    perigeeTimescale * PERIGEE_TRAJECTORY_TIMESCALE_FRACTION,
                    coordSysRotationPeriod * COORD_SYS_ROTATION_TIMESCALE_FRACTION);
            //timeResolution = this.satInfo.bestTimeResolution;   // TEST            

            // "Rounding" to one of a few (logarithmically distributed) rounding levels.
            // Only really useful if one wants different time resolution
            // for different requests for orbital data.
            //timeResolution = floorTimeResolution(timeResolution);
            if (!Double.isFinite(timeResolution)) {
                timeResolution = satInfo.bestTimeResolution;
                Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest: "
                        + "Calculated timeResolution is non-finite (e.g. NaN). "
                        + "This indicates a pure OVT code bug.", DEBUG);
                //throw new RuntimeException("Calculated timeResolution is non-finite (e.g. NaN). This indicates a pure OVT code bug.");
            }

            Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest (satInfo.ID=\"" + satInfo.ID + "\")", DEBUG);
            Log.log("   timeResolution   = " + timeResolution + " [s] (return value before rounding)", DEBUG);
            Log.log("   perigeeTimescale = " + perigeeTimescale + " [s]", DEBUG);
            Log.log("   orbitalPeriod    = " + orbitalPeriod + " [s]", DEBUG);
            return (int) Math.floor(timeResolution);
        }


        /**
         * Round down to the next lower (integer) power of two.
         */
        /*private int floorTimeResolution(double timeResolution) {
         final double twoLog = Math.log((timeResolution) / satInfo.bestTimeResolution) / Math.log(2.0);
         final int newRes = (int) (Math.pow(2.0, twoLog) * satInfo.bestTimeResolution);
         return newRes;
         }*/
        // -------
        /**
         * May return null if can not find any gap-free data (highly unlikely).
         */
        // PROPOSAL: Exception instead of null for no orbital state.
        public OrbitalState getRepresentativeOrbitalState() throws IOException {
            final int NBR_OF_DATA_POINTS = 100;

            if (representativeOrbitalState == null) {
                //final OrbitalData data = cache.getTrajectory_GEI(
                //        satInfo.availableEndTimeMjd, satInfo.availableEndTimeMjd,
                //        NBR_OF_DATA_POINTS, 0, satInfo.bestTimeResolution);
                final OrbitalData data = cache.getOrbitData(
                        satInfo.availableEndTimeMjd, satInfo.availableEndTimeMjd,
                        RoundingMode.FLOOR, RoundingMode.FLOOR,
                        NBR_OF_DATA_POINTS, 0, satInfo.bestTimeResolution);
                if (data.coords_axisPos_kmMjd[3].length < 2) {
                    throw new IOException("Less than two data points available for the specified time interval. Can not calculate orbital period.");
                }

                for (int i = data.coords_axisPos_kmMjd[3].length - 2; i >= 0; i--) {
                    if (!data.dataGaps.contains(i)) {

                        final double deltaTime_SI = (data.coords_axisPos_kmMjd[3][i + 1] - data.coords_axisPos_kmMjd[3][i]) * Time.SECONDS_IN_DAY; // Unit: seconds
                        final double[] r_SI = new double[3];
                        final double[] v_SI = new double[3];
                        for (int k = 0; k < 3; k++) {
                            v_SI[k] = (data.coords_axisPos_kmMjd[k][i + 1] - data.coords_axisPos_kmMjd[k][i]) * Const.METERS_PER_KM / deltaTime_SI;
                            r_SI[k] = (data.coords_axisPos_kmMjd[k][i + 1] + data.coords_axisPos_kmMjd[k][i]) * Const.METERS_PER_KM / 2.0;
                        }

                        representativeOrbitalState = new OrbitalState(r_SI, v_SI);
                        return representativeOrbitalState;
                    }
                }

                return null;   // If could not derive an orbital state.

            } else {

                return representativeOrbitalState;
            }
        }

    }   // DataSource

    //##########################################################################

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
            final SSCWSSat.DataSource sscwsDataSource1 = new SSCWSSat.DataSource(lib, "CompOrbitSat1");
            final SSCWSSat.DataSource sscwsDataSource2 = new SSCWSSat.DataSource(lib, "CompOrbitSat2b");

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
        final SSCWSSat.DataSource sscwsDataSource = new SSCWSSat.DataSource(lib, satIdSSCWS);
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
        final SSCWSSat.DataSource sscwsDataSource = new SSCWSSat.DataSource(lib, satIdSSCWS);

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
