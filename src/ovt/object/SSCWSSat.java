/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.object;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ovt.OVTCore;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;

/**
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Sat subclass for satellites where OVT itself downloads data from SSC Web
 * Services via the internet and caches the data internally.<BR>
 * <BR>
 *
 */
// PROPOSAL: Change name? 
//    CON: The common thread is not SSC Web Services?
//    CON: Caching and download code is outside of class?
//
// PROPOSAL: Always load some data at creation to initialize orbital period?
// PROPOSAL: Should contain the caching of orbital data?
// PROPOSAL: Move orbitalPeriod() to Utils.
//
public class SSCWSSat extends Sat {

    private static final double CACHE_UNIT_SIZE_MJD = 1.0;
    private static final int PROACTIVE_CACHE_FILL_MARGIN = 0;
    private static final int SATELLITE_NBR = 1; // Made-up value for now. Should(?) be OK for single satellites. TEMP
    private final SSCWSSatelliteInfo satInfo;
    //private final String satelliteID;
    //private final int bestTimeResolution;     // Unit: seconds    
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

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void setOrbitFile(File orbitFile) {
        throw new RuntimeException("Method not supported by this class since it does not make use of an orbit file. This exception indicates a bug.");
    }


    @Override
    /**
     * UNFINISHED IMPLEMENTATION: Returns values for testing, but not yet
     * correct values.
     */
    // PROPOSAL: Derive satellite number from satellite ID somehow?!!
    //    CON: Relies on that SSC Web Service using specific naming scheme.
    // PROPOSAL: Derive orbital period from position and velocity vector at most recent time.
    //    NOTE: Requires access to orbital data in this function.
    //
    // TODO: Use orbitalPeriod(...)
    double[] getFirstLastMjdPeriodSatNumber() throws IOException {
        //throw new RuntimeException("Not supported yet.");

        final double period = 3.14159265358979;   // Made-up value for now (pi). TEMP

        /*final double[][] coords = cache.getOrbitData(satInfo.availableEndTimeMjd, satInfo.availableEndTimeMjd, 1, 1);
         if (coords[3].length < 2) {
         throw new IOException("Less than two data points available for the specified time interval. Can not calculate orbital period");
         }
         findJumps(coords[3], )*/
        return new double[]{satInfo.availableBeginTimeMjd, satInfo.availableEndTimeMjd, period, SATELLITE_NBR};
    }


    @Override
    /**
     * UNFINISHED IMPLEMENTATION.
     */
    // TODO: Calculate velocities from coordinates.
    //    NOTE: GEI=geocentric equatorial inertial, VEI=velocity in GEI (presumably)
    //
    void fill_GEI_VEI(double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr) throws IOException {

        //final int maxResolutionFactor = 1;          // TEMP
        final int INDEX_MARGIN = 1;
        final double beginReqMjd = timeMjdMap[0];     // Req = Request/requested
        final double endReqMjd = timeMjdMap[timeMjdMap.length - 1];

        final double[][] coords = cache.getOrbitData(beginReqMjd, endReqMjd, INDEX_MARGIN, INDEX_MARGIN);
        if (coords[3].length < 2) {
            throw new IOException("Less than two data points available for the specified time interval.");
        }
        //final double beginDataMjd = coords[3][0];
        //final double endDataMjd = coords[3][coords[3].length-1];
        //if (beginReqMjd < beginDataMjd) || (endDataMjd < endReqMjd)
        //final List dataGaps = findInternalDataGaps(coords[3], );

        //if (!dataGaps.isEmpty()) {
        // NOTE: New Exception class?!! Needs to display data gap size.
        // NOTE: Could end up reacting to data gap outside requested time interval.
        //    throw new DataGapException();    
        //}
        final double[] interpCoords = new double[timeMjdMap.length];      // For one X/Y/Z axis.
        final double[] interpVelocity = new double[timeMjdMap.length];    // For one X/Y/Z axis.
        for (int i_axis = 0; i_axis < 3; i_axis++) {
            ovt.util.Utils.linearInterpolation(coords[3], coords[i_axis], timeMjdMap, interpCoords, interpVelocity);

            for (int i_pos = 0; i_pos < gei_arr.length; i_pos++) {
                gei_arr[i_pos][i_axis] = interpCoords[i_pos];
                vei_arr[i_pos][i_axis] = interpCoords[i_pos];
            }
        }

    }


    /**
     * Look for jumps greater or equal to threshold. Return list of indicies for
     * which a[i + 1] - a[i] >= minJumpGap.
     *
     * Behaviour is undefined for NaN, +Inf, -Inf.
     */
    // PROPOSAL: Move to Utils?
    // PROPOSAL: Reorganize into a "splitByDataGaps"?
    private static List<Integer> findJumps(double[] a, double minJumpGap) {
        final List<Integer> dataGaps = new ArrayList();
        for (int i = 0; i < a.length - 1; i++) {
            // Check if there is a (positive) jump.
            if (a[i + 1] - a[i] >= minJumpGap) {
                dataGaps.add(i);
            }
        }
        return dataGaps;
    }//*/

    /**
     * For debugging/testing purposes. Generate made-up orbit data.
     */
    /*void fill_GEI_VEI___TEST_FAKE_DATA(double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr) throws IOException {

     // TEST/DEBUG: Made-up orbit.
     for (int i = 0; i < timeMjdMap.length; i++) {
     gei_arr[i][0] = Const.RE * Math.cos(timeMjdMap[i]) * 5;
     gei_arr[i][1] = Const.RE * Math.sin(timeMjdMap[i]) * 10;
     gei_arr[i][2] = Const.RE * Math.sin(timeMjdMap[i] * Math.sqrt(2)) * 0.2;
     vei_arr[i][0] = 10.0;
     vei_arr[i][1] = 20.0;
     vei_arr[i][2] = 30.0;
     }
     }*/
}
