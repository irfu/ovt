/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/object/SSCWSSat.java $
 Date:      $Date: 2015/09/15 12:00:00 $
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
package ovt.object;

import ovt.util.SSCWSOrbitCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import ovt.Const;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.util.IndexedSegmentsCache;
import ovt.util.Log;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.NoSuchSatelliteException;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;
import ovt.util.SSCWSOrbitCache.OrbitalData;
import ovt.util.Utils;
import ovt.util.Utils.OrbitalState;

/**
 *
 * Sat subclass for satellites where OVT itself downloads data from SSC Web
 * Services (SSC WS) via the internet and caches the data internally.<BR>
 *
 * IMPLEMENTATION NOTE: The class is divided into (1) one "context-dependent"
 * outer class (for the GUI), and (2) a more "context-independent" nested class
 * which is independent of the GUI.
 *
 * IMPLEMENTATION NOTE: The code is originally written to handle that (1) the
 * time resolution requested from SSC can vary between requested and thus vary
 * between different cache slots, and (2) that the time resolution requested
 * from SSC for a given cache slot can be increased, thus replacing older data
 * with lower time resolution in cache slot. Neither of these two features is
 * however presently (2015-09-10) used DURING AN OVT SESSION, and the same
 * constant time resolution is always requested (for the same satellite) from
 * the SSC. However, the derived time resolution can however still change from
 * OVT session to OVT session due to (1) updated SSC data (from which resolution
 * is derived), or (2) changes in the OVT code. This is relevant if the code
 * reads cached data from disk from a previous session (with a different time
 * resolution).
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-0x-xx
 */
// PROPOSAL: Change name? 
//    PRO: The common thread is not SSC Web Services?
//    PRO: Caching and download code is outside of class?
//
public class SSCWSSat extends Sat {

    private static final int DEBUG = 2;   // The minimum log message level for this class.
    private static final boolean ALWAYS_REQUEST_BEST_TIME_RESOLUTION = false;   // For debugging.
    /**
     * See DiscreteIntervalToListCache to understand the variable.
     */
    private static final double CACHE_SLOT_SIZE_MJD = 1.0;

    /**
     * See DiscreteIntervalToListCache to understand this variable. Must be
     * non-negative.
     */
    private static final int PROACTIVE_CACHING_FILL_MARGIN_SLOTS = 5;
    /**
     * String added immediately after the "file base name" without any implicit
     * period. Therefore, you (probably) want to begin with a period.
     */
    private static final String SSCWS_CACHE_FILE_SUFFIX = ".SSCWS.cache";
    private static final int SATELLITE_NBR = 1;  // Made-up value. Assumed to be OK for single satellites(?).

    private static final String CAN_NOT_FILL_ERROR_MSG
            = "Can not fill the specified time interval with orbit data due to time boundaries";  // Do not end with period.
    // Can not fill the specified time interval with orbit data since requires data from outside...
    // There is not enough orbit data to fill the specified time interval.
    // The orbit data does not cover the time interval.
    // The specified time interval exceeds the time interval for which there is data.
    // The specified time interval exceeds the time interval for which there is data.

    //##########################################################################
    /**
     * Cache file. Read (if it exists) and overwritten when quitting.
     */
    private File cacheFile = null;
    private SSCWSSat.DataSource dataSource = null;
    private String SSCWS_satID = null;

    //##########################################################################

    /**
     * @param SSCWS_satID The satellite ID string used by SSC Web Services to
     * reference satellites.
     *
     * @see SatelliteDescription#getId().
     */
    public SSCWSSat(OVTCore core, String mSSCWS_satID) throws IOException, NoSuchSatelliteException {
        super(core);
        initializeSSCWSSat(mSSCWS_satID);
    }


    /**
     * Initializer intended to be called from Settings.java using Java Beans
     * when loading settings. <BR>
     * NOTE: Leaves instance fields and thus the cache unitialized and in a
     * non-functioning state. "Settings" should call setSSCWSSatelliteID to
     * complete the initialization.
     */
    public SSCWSSat(OVTCore core) throws IOException {
        super(core);
    }


    /**
     * Initializes the cache to use a specific SSCWS_satID.<BR>
     * IMPLEMENTATION NOTE: Does not want to use the method name "initialize"
     * since that would override the inherited method with the same name.
     */
    private void initializeSSCWSSat(String mSSCWS_satID) throws IOException, NoSuchSatelliteException {
        if (mSSCWS_satID == null) {
            throw new IllegalArgumentException("mSSCWS_satID is null.");
        }
        SSCWS_satID = mSSCWS_satID;

        /*==============================================
         Determine where a previous cache should be.
         ===============================================*/
        // Do not try to create parent directory. That is done when saving.
        final File dir = new File(OVTCore.getUserDir() + OVTCore.getSSCWSCacheSubdir());
        cacheFile = new File(dir, Utils.replaceSpaces(SSCWS_satID) + SSCWS_CACHE_FILE_SUFFIX);   // Determine which file to use.
        Log.log(this.getClass().getSimpleName() + ".DataSource: cacheFile = " + cacheFile.getAbsolutePath(), DEBUG);

        dataSource = new DataSource(OVTCore.SSCWS_LIBRARY, SSCWS_satID, cacheFile);
    }


    /**
     * Partly intended to be called from Settings.java using Java Beans when
     * loading settings. Described in SSCWSSatBeanInfo.
     */
    public String getSSCWSSatelliteID() {
        return dataSource.satInfo.ID;
    }


    /**
     * Intended to be called from Settings.java using Java Beans when loading
     * settings. Described in SSCWSSatBeanInfo.
     */
    public void setSSCWSSatelliteID(String mSSCWS_satID) throws IOException, NoSuchSatelliteException {
        initializeSSCWSSat(mSSCWS_satID);
    }


    /**
     * Intended to be called from Settings.java using Java Beans when saving
     * settings. Described in SSCWSSatBeanInfo.
     *
     * NOTE: In its current form, this method must be called AFTER
     * setSSCWSSatelliteID for the instance to be properly initialized.
     * Otherwise (probably) an exception will be thrown. The "Settings" class
     * will initialize this class by calling first the (right) constructor, then
     * setSSCWSSatelliteID and then setOrbitFile, which works, but there is no
     * guarantee that the method will always be called in this order. /Erik P G
     * Johansson
     *
     * NOTE: It appears that class "Settings" calls this method with a File
     * object initialized with the path "null" (a string!) when loading settings
     * which once originated from a null pointer returned from getOrbitFile.
     * Therefore the method treats the path "null" (the string) the same as the
     * null pointer.
     *
     * orbitFile.getPath().equals("null")
     */
    @Override
    public void setOrbitFile(File orbitFile) throws IOException {
        if ((orbitFile != null) && (!orbitFile.getPath().equals("null"))) {
            throw new IllegalArgumentException("There must be no orbitFile for this class."
                    + " This exception indicates a pure OVT bug. orbitFile=" + orbitFile);
        }
        if (SSCWS_satID != null) {
            // Avoid making this call if this class has not been properly initialized yet.
            // Not entirely correct but the initialization in setOrbitFile is not very important.
            super.setOrbitFile(orbitFile);
        }
    }


    /**
     * Try save cache to file.
     *
     * IMPLEMENTATION NOTE: We want to save cache to file (1) when quitting OVT,
     * (2) when removing the SSCWSSat from the GUI tree. Can call this method on
     * both occasions. Therefore want to catch exception and display error
     * message here, ONCE, in ONE location. Note that the method does not have
     * to succeed for OVT to continue.
     */
    public void trySaveCacheToFile() {
        try {
            this.dataSource.saveCacheToFile(cacheFile);
        } catch (IOException e) {
            this.getCore().sendErrorMessage("Error saving SSC orbit cache file: " + e.getMessage(), e);
        }
    }


    @Override
    double[] getFirstLastMjdPeriodSatNumber() throws IOException {

        final OrbitalState state = dataSource.getRepresentativeOrbitalState();

        double period_days = Double.NaN;   // Default value.
        if ((state != null) | (!state.isReasonableEllipticOrbit())) {
            period_days = state.P_SI * Time.DAYS_IN_SECOND;
        }

        return new double[]{
            dataSource.satInfo.availableBeginTimeMjd,
            dataSource.satInfo.availableEndTimeMjd,
            period_days, SATELLITE_NBR};
    }


    @Override
    /**
     * NOTE: GEI=Geocentric Equatorial Inertial, VEI=Velocity in GEI
     * (presumably).
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
    public static String deriveNameFromSSCWSSatID(String satID) throws IOException, SSCWSLibrary.NoSuchSatelliteException {
        final String satName = OVTCore.SSCWS_LIBRARY.getSatelliteInfo(satID).name;   // throws IOException

        return "(SSC) " + satName;
        //return satID + " (SSC)";
    }


    /**
     * Called when trying to delete object from GUI tree.
     */
    @Override
    public void dispose() {
        // NOTE: Can not be allowed to throw IOException since it is located in an inherited method.
        trySaveCacheToFile();
        super.dispose(); // dispose descriptors, remove listeners, dispose children
    }

    //##########################################################################
    //##########################################################################
    //##########################################################################
    //##########################################################################
    //##########################################################################
    //##########################################################################
    //##########################################################################
    /**
     * Nested class to which the parent class delegates most of the work. Class
     * is independent of GUI and OVTCore and the parent class.
     *
     * IMPLEMENTATION NOTE: This looks somewhat ugly at first but the reason for
     * this is to divide the functionality into parts which are connected to the
     * GUI (SSCWSSat, OVTCore) and parts which are not (SSCWSSat.DataSource).
     * This is necessary in order to easily be able to automatically test
     * trajectory data (without creating a GUI), in particular to compare
     * trajectories for the same satellite but from different source to verify
     * the coordinate system.
     *
     * NOTE: The boundary between this class and SSCWSOrbitCache is maybe vague.
     * This class interpolates to requested times. SSCWSOrbitCache tries to
     * encapsulate the cache that is stored between sessions.
     *
     * PROPOSAL: Move this to separate (public) class?<BR>
     */
    public static class DataSource {

        public final SSCWSSatelliteInfo satInfo;
        private final SSCWSOrbitCache cache;

        /**
         * Flag for whether representativeOrbitalState has been assigned or not.
         * NOTE: representativeOrbitalState == null is a valid value and can not
         * be used to indicate that the variable has not been assigned.
         */
        private boolean hasCachedRepresentativeOrbitalState = false;
        private OrbitalState representativeOrbitalState = null;
        /**
         * Time resolution in seconds.
         */
        private final int timeResolutionToRequest;


        /**
         * @param cacheFile If null, then don't try to read from a cache file
         * (but still use in-RAM cache).
         */
        public DataSource(SSCWSLibrary mSSCWSLibrary, String SSCWS_satID, File cacheFile)
                throws IOException, SSCWSLibrary.NoSuchSatelliteException {
            this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWS_satID);

            /*==============================================
             Read old cache if available and try to figure
             out whether to use the old cache data, or not.
             ----------------------------------------------
             NOTE: SSC Web Services does not offer any way of knowing whether orbit data
             has been updated at the SSC. Therefore there is no strict way of
             knowing whether the locally cached data is identical to that at the SSC.
             Therefore one has to make some guesses.
             --
             "Unfortunately, there is currently no way to find out when the orbital
             data was updated through the web services.  I'll have to look into how
             difficult that would be to implement.  For most active spacecraft,
             checking the time range (particularly the end time) might be good enough
             (since most updates are to extend the time covered). But we do
             occasionally update past times with better data and I do not know how to
             determine that."
             /Bernie Harris, Bernard.T.Harris@nasa.gov, NASA SSC, e-mail 2015-05-21
             =====================================================================*/
            SSCWSOrbitCache newCache = null;
            boolean createNewCache = true;
            if ((cacheFile != null) && (cacheFile.isFile())) {
                Log.log("SSCWSSat: cacheFile.isFile() = " + cacheFile.isFile(), DEBUG);

                try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {

                    newCache = new SSCWSOrbitCache(
                            ois, mSSCWSLibrary, SSCWS_satID, PROACTIVE_CACHING_FILL_MARGIN_SLOTS * CACHE_SLOT_SIZE_MJD);
                    createNewCache = false;

                    final String msg1 = "   Successfully read SSCWS satellite orbit cache in \"" + cacheFile.getAbsolutePath() + "\".";
                    final String msg2 = "   The old cache has " + newCache.getNbrOfFilledCacheSlots() + " filled cache slots.";
                    //System.out.println(msg1);
                    //System.out.println(msg2);
                    Log.log(msg1, DEBUG);
                    Log.log(msg2, DEBUG);

                    final SSCWSSatelliteInfo oldSatInfo = newCache.getSSCWSSatelliteInfo();
                    if ((oldSatInfo.availableBeginTimeMjd != this.satInfo.availableBeginTimeMjd)
                            | (oldSatInfo.availableEndTimeMjd != this.satInfo.availableEndTimeMjd)
                            | (oldSatInfo.bestTimeResolution != this.satInfo.bestTimeResolution)) {

                        final String msg = "   Reject SSCWS satellite orbit cache in \"" + cacheFile.getAbsolutePath() + "\" due to different start & end dates.";
                        //System.out.println(msg);
                        Log.log(msg, DEBUG);
                        createNewCache = true;
                    }

                    if (CACHE_SLOT_SIZE_MJD != newCache.getSlotSizeMjd()) {
                        final String msg = "   Reject SSCWS satellite orbit cache in \"" + cacheFile.getAbsolutePath() + "\" due to different slot size.";
                        //System.out.println(msg);
                        Log.log(msg, DEBUG);

                        createNewCache = true;
                    }

                } catch (ClassNotFoundException | IOException e) {
                    /**
                     * Can not call OVTCore#sendErrorMessage(msg) since this
                     * class does not have access to the OVTCore, and really
                     * should be independent of the GUI for testing and
                     * modularization reasons. We also do not really want an
                     * error message since this is something the code can
                     * recover from.
                     */
                    final String msg1 = "Failed to reuse SSCWS satellite orbit cache in \"" + cacheFile.getAbsolutePath() + "\". "
                            + "Ignoring and creating new empty cache.";
                    // NOTE: e.getMessage() often null here. Hence the explicit explanation of what is actually printed.
                    final String msg2 = "   " + e.getClass().getName() + ": e.getMessage() = " + e.getMessage();
                    //System.out.println(msg1);
                    //System.out.println(msg2);
                    Log.log(msg1, DEBUG);
                    Log.log(msg2, DEBUG);
                    createNewCache = true;
                }
            }

            if (createNewCache) {
                Log.log("Create new empty SSCWS satellite orbit cache.", DEBUG);
                newCache = new SSCWSOrbitCache(mSSCWSLibrary, SSCWS_satID, CACHE_SLOT_SIZE_MJD, PROACTIVE_CACHING_FILL_MARGIN_SLOTS);
            }
            this.cache = newCache;

            // NOTE: Must come last since it triggers reading from cache.
            timeResolutionToRequest = getTimeResolutionToRequest();
        }


        /**
         * Save cache to specified file. Will overwrite file if it exists.
         */
        public void saveCacheToFile(File cacheFile) throws IOException {
            final File parentDir = cacheFile.getParentFile();

            if (!parentDir.isDirectory()) {
                parentDir.mkdirs();  // Create directories.
                // Permit IOException when trying to write to file, rather than check for it here.
                // Error messages should be taken care of higher up.
            }

            try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {

                this.cache.writeToStream(oos);
                // try-with resources closes the stream independent of exceptions.
            } catch (IOException e) {
                throw new IOException("Error when trying to write cache to file: " + cacheFile.getAbsolutePath(), e);
            }
        }


        /**
         * Analogous to Sat#fill_GEI_VEI. Its implementation should delegate to
         * this one.
         *
         * @param vei_arr_posAxis_kms
         */
        public void fill_GEI_VEI(
                double[] timeMjdMap,
                double[][] gei_arr_posAxis_km,
                double[][] vei_arr_posAxis_kms)
                throws IOException {

            /* Nbr of points to require outside requested interval.
             Depends on what is optimal for the interpolation.
             Should probably use a value of at least "2" for cubic spline interpolation to
             reduce effects of boundary conditions.
             NOTE: Requiring points outside the chosen range means that there are
             data points at the boundaries of the available data that can never
             be displayed. */
            final int INDEX_MARGIN = 2;

            /*==============
             Argument check.
             ==============*/
            if ((gei_arr_posAxis_km.length > 0) && (gei_arr_posAxis_km[0].length != 3)) {
                throw new IllegalArgumentException("Illegal array dimensions: gei_arr_posAxis[0].length != 3");
            }
            if ((vei_arr_posAxis_kms.length > 0) && (vei_arr_posAxis_kms[0].length != 3)) {
                throw new IllegalArgumentException("Illegal array dimensions: vei_arr_posAxis[0].length != 3");
            }
            if ((timeMjdMap.length != gei_arr_posAxis_km.length)
                    | (timeMjdMap.length != vei_arr_posAxis_kms.length)) {
                throw new IllegalArgumentException("Illegal array dimensions. Lengths are not identical.");
            }

            /*===================
             Get data from cache
             ===================*/
            //Log.log(this.getClass().getSimpleName() + ".fill_GEI_VEI", DEBUG);
            final double beginReqMjd = timeMjdMap[0];     // Req = Request/requested
            final double endReqMjd = timeMjdMap[timeMjdMap.length - 1];

            final SSCWSOrbitCache.OrbitalData data;
            try {
                data = cache.getOrbitData(
                        beginReqMjd, endReqMjd,
                        RoundingMode.CEILING, RoundingMode.FLOOR,
                        INDEX_MARGIN, INDEX_MARGIN, timeResolutionToRequest);
            } catch (IndexedSegmentsCache.NoSuchTPositionException e) {
                throw new IOException(CAN_NOT_FILL_ERROR_MSG + ": " + e.getMessage(), e);
            }

            /*=======================
             Check data for "errors".
             =======================*/
            if (data.coords_axisPos_kmMjd[3].length < 2) {
                throw new IOException("Less than two data points available for the specified time interval. Can not interpolate.");
            }
            if (!data.dataGaps.isEmpty()) {
                throw new IOException("Orbital data contains " + data.dataGaps.size() + " data gap(s) in the selected time interval.");
                /* PROPOSAL: Use new Exception subclass DataGapException for this?!! Is not really an I/O error.
                 NOTE: Code may react to data gap outside the requested time interval
                 since it uses data points outside of it for interpolation.*/
            }

            /*=========================================================
             (1) Interpolate data to requested points in time and
             (2) store in the format returned to user (change indices)
             =========================================================*/
            final double[] interpCoords_pos_km = new double[timeMjdMap.length];         // Temporary variable for one X/Y/Z axis.
            final double[] interpVelocity_pos_kmMjd = new double[timeMjdMap.length];    // Temporary variable for one X/Y/Z axis.
            for (int i_axis = 0; i_axis < 3; i_axis++) {
                /* NOTE: Concerning bad interpolation observed in the GUI:
                 ---------------------------------------------------------
                 One can see bad interpolation at the orbit endpoints in the GUI
                 when specifying low time resolution (in the GUI). This is not
                 due to bugs in this method or the Utils#cubicSplineInterpolation
                 but due to that there is also interpolation (probably cubic
                 spline) used when plotting the orbit which INTERPOLATES THE
                 (INTERPOLATED) RESULTS OF THIS FUNCTION. That other interpolation
                 does NOT make use of any
                 tabulated points outside of the (time) range that is actually
                 used for plotting.
                 */
                // NOTE: Time unit is mjd. Therefore, interpolated velocity is km/day.
                Utils.cubicSplineInterpolation(
                        data.coords_axisPos_kmMjd[3],
                        data.coords_axisPos_kmMjd[i_axis],
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


        /**
         * Calculate a time resolution to request when requesting data.
         *
         * This function could in principle be redesigned to give different
         * results over time and be called directly when requesting data (and
         * thus take the request into account?).
         *
         * See the comments on time resolution for the entire class.
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

            if (ALWAYS_REQUEST_BEST_TIME_RESOLUTION) {
                final String msg = "DEBUGGING SETTING: getTimeResolutionToRequest: Using best time resolution = " + satInfo.bestTimeResolution + " [s] (return value)";
                System.out.println(msg);
                //Log.log(msg, DEBUG);
                return satInfo.bestTimeResolution;
            }

            // NOTE: Should be unnecessary if perigeeTimeScale works.
            final OrbitalState orbitalState = getRepresentativeOrbitalState();  // Useful variable for debugging.

            /**
             * Fastest coordinate system rotation. Needed since some coordinate
             * systems rotate or wobble due to the rotation of the Earth
             * relative to the Sun or to the Universe.
             */
            final double coordSysRotationPeriod = Time.SECONDS_IN_DAY;
            final double coordSysRotationLimit = coordSysRotationPeriod * COORD_SYS_ROTATION_TIMESCALE_FRACTION;

            // Time scale from angular velocity at r_perigee_SI.
            final double perigeeTimescale = 1.0 / orbitalState.omega_perigee_SI;   // IOException
            final double perigeeLimit = perigeeTimescale * PERIGEE_TRAJECTORY_TIMESCALE_FRACTION;

            double timeResolution;
            if (orbitalState.isReasonableEllipticOrbit()) {
                timeResolution = Math.min(
                        perigeeLimit,
                        coordSysRotationLimit);
            } else {
                //timeResolution = coordSysRotationLimit;
                timeResolution = satInfo.bestTimeResolution;
            }

            //timeResolution = this.satInfo.bestTimeResolution;   // TEST            

            /* "Rounding" to one of a few (logarithmically distributed) rounding levels.
             Only really useful if one wants different time resolution
             for different requests for orbital data.*/
            //timeResolution = floorTimeResolution(timeResolution);
            if (!Double.isFinite(timeResolution)) {
                timeResolution = satInfo.bestTimeResolution;
                Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest: "
                        + "Calculated timeResolution is non-finite (e.g. NaN). "
                        + "This indicates a pure OVT code bug.", DEBUG);
                //throw new RuntimeException("Calculated timeResolution is non-finite (e.g. NaN). This indicates a pure OVT code bug.");
            }

            //Log.log(this.getClass().getSimpleName() + ".getTimeResolutionToRequest (satInfo.ID=\"" + satInfo.ID + "\")", DEBUG);
//            Log.log("   timeResolution             = " + timeResolution + " [s] (return value before rounding)", DEBUG);
//            Log.log("   perigeeTimescale           = " + perigeeTimescale + " [s]", DEBUG);
//            Log.log("   coordSysRotationPeriod     = " + coordSysRotationPeriod + " [s]", DEBUG);
//            Log.log("   satInfo.bestTimeResolution = " + satInfo.bestTimeResolution + " [s]", DEBUG);
//            Log.log("   orbitalState.P_SI          = " + orbitalState.P_SI + " [s] = " + orbitalState.P_SI / 3600.0 + " [h]", DEBUG);
//            Log.log("   orbitalState.isReasonableEllipticOrbit() = " + orbitalState.isReasonableEllipticOrbit(), DEBUG);
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

            // Use cached object if possible.
            if (hasCachedRepresentativeOrbitalState) {
                return representativeOrbitalState;
            }

            final OrbitalData data;
            try {
                data = cache.getOrbitData(
                        satInfo.availableEndTimeMjd, satInfo.availableEndTimeMjd,
                        RoundingMode.FLOOR, RoundingMode.FLOOR,
                        NBR_OF_DATA_POINTS, 0, satInfo.bestTimeResolution);
            } catch (IndexedSegmentsCache.NoSuchTPositionException e) {
                throw new IOException(CAN_NOT_FILL_ERROR_MSG + ": " + e.getMessage(), e);
            }
            if (data.coords_axisPos_kmMjd[3].length < 2) {
                throw new IOException("Less than two data points available for the specified time interval. Can not calculate orbital period.");
            }

            for (int i = data.coords_axisPos_kmMjd[3].length - 2; i >= 0; i--) {
                if (!data.dataGaps.contains(i)) {

                    // CASE: i is not the first index before a data gap, and i+1 is a valid index.
                    final double deltaTime_SI = (data.coords_axisPos_kmMjd[3][i + 1] - data.coords_axisPos_kmMjd[3][i]) * Time.SECONDS_IN_DAY; // Unit: seconds
                    final double[] r_SI = new double[3];
                    final double[] v_SI = new double[3];
                    for (int k = 0; k < 3; k++) {
                        v_SI[k] = (data.coords_axisPos_kmMjd[k][i + 1] - data.coords_axisPos_kmMjd[k][i]) * Const.METERS_PER_KM / deltaTime_SI;
                        r_SI[k] = (data.coords_axisPos_kmMjd[k][i + 1] + data.coords_axisPos_kmMjd[k][i]) * Const.METERS_PER_KM / 2.0;
                    }

                    hasCachedRepresentativeOrbitalState = true;
                    representativeOrbitalState = new OrbitalState(r_SI, v_SI);
                    return representativeOrbitalState;
                }
            }

            hasCachedRepresentativeOrbitalState = true;
            representativeOrbitalState = null;
            return representativeOrbitalState;   // If could not derive an orbital state.

        }

    }   // DataSource

}
