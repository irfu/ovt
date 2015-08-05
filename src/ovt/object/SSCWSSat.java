/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.object;

import ovt.util.SSCWSOrbitCache;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ovt.Const;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.util.Log;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;
import ovt.util.SSCWSOrbitCache.OrbitalData;
import ovt.util.Utils;
import ovt.util.Utils.OrbitalState;
import ovt.util.Vect;

/**
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Sat subclass for satellites where OVT itself downloads data from SSC Web
 * Services (SSC WS) via the internet and caches the data internally.<BR>
 * <BR>
 */
// PROPOSAL: Change name? 
//    PRO: The common thread is not SSC Web Services?
//    PRO: Caching and download code is outside of class?
//
// PROPOSAL: Always load some data at creation to initialize orbital period?
// PROPOSAL: Should contain the caching of orbital data?
// PROPOSAL: Move orbitalPeriod() to Utils.
//
public class SSCWSSat extends Sat {

    private static final int DEBUG = 1;   // Set the minimum log message level for this class.
    private static final double CACHE_UNIT_SIZE_MJD = 1.0;
    private static final int PROACTIVE_CACHE_FILL_MARGIN = 20;
    private static final int SATELLITE_NBR = 1;     // Made-up value. Should(?) be OK for single satellites.

    /**
     * Time resolution in seconds.
     */
    private final int timeResolutionToRequest;

    private OrbitalState representativeOrbitalState = null;
    private final SSCWSSatelliteInfo satInfo;
    private final SSCWSOrbitCache cache;


    /**
     * @param SSCWS_satID The satellite ID string used by SSC Web Services to
     * reference satellites.
     *
     * @see SatelliteDescription#getId().
     */
    public SSCWSSat(OVTCore core, SSCWSLibrary sscwsLibrary, String SSCWS_satID) throws IOException {
        super(core);
        try {
            this.satInfo = sscwsLibrary.getSatelliteInfo(SSCWS_satID);

            this.cache = new SSCWSOrbitCache(sscwsLibrary, satInfo.ID, CACHE_UNIT_SIZE_MJD, PROACTIVE_CACHE_FILL_MARGIN);
        } catch (Exception e) {
            throw new IOException("Can not download satellite description from SSC Web Services: " + e.getMessage(), e);
        }

        timeResolutionToRequest = getTimeResolutionToRequest();
    }


    @Override
    public void setOrbitFile(File orbitFile) throws IOException {
        if (orbitFile != null) {
            throw new RuntimeException("There must be no orbitFile for this class."
                    + " This exception indicates a bug.");
        }
        super.setOrbitFile(orbitFile);
        //throw new RuntimeException("Method not supported by this class since it does not make use of an orbit file. This exception indicates a bug.");
    }


    @Override
    double[] getFirstLastMjdPeriodSatNumber() throws IOException {

        final OrbitalState state = getRepresentativeOrbitalState();

        double period = Double.NaN;
        if (state != null) {
            period = state.P * Time.DAYS_IN_SECOND;
        }

        return new double[]{satInfo.availableBeginTimeMjd, satInfo.availableEndTimeMjd, period, SATELLITE_NBR};
    }


    @Override
    /**
     * NOTE: GEI=geocentric equatorial inertial, VEI=velocity in GEI
     * (presumably).
     */
    void fill_GEI_VEI(double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr) throws IOException {

        final int INDEX_MARGIN = 1;
        final double beginReqMjd = timeMjdMap[0];     // Req = Request/requested
        final double endReqMjd = timeMjdMap[timeMjdMap.length - 1];

        final SSCWSOrbitCache.OrbitalData data = cache.getOrbitData(
                beginReqMjd, endReqMjd,
                INDEX_MARGIN, INDEX_MARGIN, timeResolutionToRequest);
        if (data.orbit[3].length < 2) {
            throw new IOException("Less than two data points available for the specified time interval.");
        }
        if (!data.dataGaps.isEmpty()) {
            throw new IOException("Orbital data contains "+data.dataGaps.size()+" data gap(s) in the selected time interval.");
            /* PROPOSAL: Use new Exception subclass DataGapException for this?!! Is not really an I/O error.
             NOTE: Code may react to data gap outside requested time interval
             since it uses data points outside of it for interpolation.*/
        }

        final double[] interpCoords = new double[timeMjdMap.length];      // For one X/Y/Z axis.
        final double[] interpVelocity = new double[timeMjdMap.length];    // For one X/Y/Z axis.
        for (int i_axis = 0; i_axis < 3; i_axis++) {
            ovt.util.Utils.linearInterpolation(data.orbit[3], data.orbit[i_axis], timeMjdMap, interpCoords, interpVelocity);

            for (int i_pos = 0; i_pos < gei_arr.length; i_pos++) {
                gei_arr[i_pos][i_axis] = interpCoords[i_pos];
                vei_arr[i_pos][i_axis] = interpCoords[i_pos];
            }
        }

    }


    /**
     * Calculate a time resolution to request when requesting data. This
     * function could conceivably be redesigned to give different results over
     * time and be called directly when requesting data (and thus take the
     * request into account?).
     */
    private int getTimeResolutionToRequest() throws IOException {
        /**
         * Try to determine sensible time resolution to use for data. NOTE: Only
         * SI units without prefixes, e.g. seconds.
         *
         * First calculate some timescales for acceleration that MIGHT be
         * useful, then select what to actually use.
         */

        // NOTE: Should be unnecessary if perigeeTimeScale works.
        final OrbitalState orbitalState = getRepresentativeOrbitalState();  // Useful variable for debugging.
        final double orbitalPeriod = orbitalState.P;

        /**
         * Fastest coordinate system rotation. Using different coordinate
         * systems give different .
         */
        final double coordSysRotationPeriod = Time.SECONDS_IN_DAY;

        // Time scale from angular velocity at r_perigee.
        final double perigeeTimescale = 1.0 / getRepresentativeOrbitalState().omega_perigee;   // IOException

        double timeResolution = Math.min(perigeeTimescale / 10, coordSysRotationPeriod / 100);
        //satInfo.bestTimeResolution

        // "Rounding". Only really useful if one wants different time resolution
        // for different requests for orbital data.
        //timeResolution = floorTimeResolution(timeResolution);
        Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest (satInfo.ID=\"" + satInfo.ID + "\")", DEBUG);
        Log.log("   timeResolution   = " + timeResolution, DEBUG);
        Log.log("   perigeeTimescale = " + perigeeTimescale, DEBUG);
        Log.log("   orbitalPeriod    = " + orbitalPeriod, DEBUG);

        if (!Double.isFinite(timeResolution)) {
            timeResolution = satInfo.bestTimeResolution;
            Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest: Calculated timeResolution is non-finite (e.g. NaN). This indicates a pure OVT code bug.", DEBUG);
            //throw new RuntimeException("Calculated timeResolution is non-finite (e.g. NaN). This indicates a pure OVT code bug.");
        }
        return (int) Math.floor(timeResolution);
    }


    /**
     * May return null if can not find any gap free data (highly unlikely).
     */
    private OrbitalState getRepresentativeOrbitalState() throws IOException {
        final int NBR_OF_DATA_POINTS = 100;

        if (representativeOrbitalState == null) {
            final OrbitalData data = cache.getOrbitData(
                    satInfo.availableEndTimeMjd, satInfo.availableEndTimeMjd,
                    NBR_OF_DATA_POINTS, 1, satInfo.bestTimeResolution);
            if (data.orbit[3].length < 2) {
                throw new IOException("Less than two data points available for the specified time interval. Can not calculate orbital period");
            }

            for (int i = data.orbit[3].length - 2; i >= 0; i--) {
                if (!data.dataGaps.contains(i)) {

                    final double deltaTime = (data.orbit[3][i + 1] - data.orbit[3][i]) * Time.SECONDS_IN_DAY; // Unit: seconds
                    final double[] r = new double[3];
                    final double[] v = new double[3];
                    for (int k = 0; k < 3; k++) {
                        v[k] = (data.orbit[k][i + 1] - data.orbit[k][i]) * Const.METERS_PER_KM / deltaTime;
                        r[k] = (data.orbit[k][i + 1] + data.orbit[k][i]) * Const.METERS_PER_KM / 2.0;
                    }

                    representativeOrbitalState = new OrbitalState(r, v);
                    return representativeOrbitalState;
                }
            }

            return null;   // If could not derive an orbital state.

        } else {

            return representativeOrbitalState;
        }
    }


    /**
     * Round down to the next lower (integer) power of two.
     */
    private int floorTimeResolution(double timeResolution) {
        final double twoLog = Math.log((timeResolution) / satInfo.bestTimeResolution) / Math.log(2.0);
        final int newRes = (int) (Math.pow(2.0, twoLog) * satInfo.bestTimeResolution);
        return newRes;
    }

}
