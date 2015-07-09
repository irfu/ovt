/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ovt.Const;
import ovt.datatype.Time;

/**
 * Class which instances can replace SSC Web Services (SSCWSLibraryImpl) as a
 * source of data for testing purposes. This class is supposed to return
 * "better" FICTIOUS test data for the purposes of testing the entire GUI:<BR>
 * - Graphics (3D visualizations)<BR>
 * - Orbital period calculations<BR>
 * - Orbit data resolution (possibly varying over time) and interpolation<BR>
 * - Menus (selecting satellites, tree panel)<BR>
 * - Caching, download delays<BR>
 * - Data gaps<BR>
 * - Various errors, error messages: network failures, exceptions etc.<BR>
 *
 * NOTE: One could fake/throw exceptions to simulate network and SSC Web
 * Services failure. NOTE: One could add delays to simulate download over
 * internet.
 *
 * Should therefore return somewhat physical orbits, at least by length scales
 * and time scales (velocities).
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class SSCWSLibraryTestEmulator extends SSCWSLibrary {

    public static final SSCWSLibrary DEFAULT_INSTANCE = new SSCWSLibraryTestEmulator();


    private SSCWSLibraryTestEmulator() {
    }


    @Override
    public List<SSCWSSatelliteInfo> getAllSatelliteInfo() {
        final List<SSCWSSatelliteInfo> satInfos = new ArrayList<>();
        final double dataBeginMjd = 0;
        final double dataEndMjd = 1e9;
        final int bestTimeResolution = 60;
        
        satInfos.add(new SSCWSSatelliteInfo("TestSat", "Test Satellite", dataBeginMjd, dataEndMjd, bestTimeResolution));
        satInfos.add(new SSCWSSatelliteInfo("DataGapSat", "Satellite with lots of data gaps", dataBeginMjd, dataEndMjd, bestTimeResolution));
        satInfos.add(new SSCWSSatelliteInfo("DownloadFailSat", "Satellite with lots of download failures", dataBeginMjd, dataEndMjd, bestTimeResolution));
        satInfos.add(new SSCWSSatelliteInfo("SlowDownloadSat", "Satellite with (simulated) slow downloads", dataBeginMjd, dataEndMjd, bestTimeResolution));        
        satInfos.add(new SSCWSSatelliteInfo("Enterprise", "USS Enterprise (NCC-1701)", dataBeginMjd, dataEndMjd, bestTimeResolution));
        satInfos.add(new SSCWSSatelliteInfo("UFO", "Unidentified Flying Object", dataBeginMjd, dataEndMjd, bestTimeResolution));
        return satInfos;
    }


    @Override
    /**
     * NOTE: Returns lengths in km.
     */
    public double[][] getOrbitData(String satID, double beginMjdInclusive, double endMjdInclusive, int resolutionFactor) throws IOException {

        // Check that satID is valid, at the very least.
        final SSCWSSatelliteInfo satInfo = getSatelliteInfo(satID);
        //if (!satInfo.ID.equals("Enterprise")) {
        //    throw new IOException("No orbit defined for this satellite yet: " + satID);
        //}

        // Calculate physically correct circular orbit.
        // NOTE: Gravitational constant (m^3 * kg^-1 * s^-2; does not use km!).
        final double rand = ((double) satID.hashCode()) / ((double) Integer.MAX_VALUE);  // "Random" value from -1 to 1.
        final double yzAngle = satID.hashCode();  // Obtain "random" angle that is constant for all calls with this satID.
        final double R = (2.1 + rand) * Const.RE * 1e3;
        final double omega = Math.sqrt(Const.GRAV_CONST * Const.ME / (R * R * R)); // Orbital angular velocity

        // Calculate times for which to produce data. We want these points in time to be consistent over different calls, specified time intervals.
        final double timeResolutionMjd = Time.DAYS_IN_SECOND * 60 * resolutionFactor;
        final int i_begin = (int) Math.ceil(beginMjdInclusive / timeResolutionMjd);
        final int i_end = (int) Math.floor(endMjdInclusive / timeResolutionMjd) + 1;   // Exclusive bound.
        final int N = i_end - i_begin;
        final double[] timesMjd = new double[N];
        int j_array = 0;
        for (int i = i_begin; i < i_end; i++) {
            timesMjd[j_array] = i * timeResolutionMjd;
            j_array++;
        }

        // Calculate orbit data.
        final double[][] coords = new double[4][N];
        coords[3] = timesMjd;
        for (int i = 0; i < N; i++) {
            final double t = timesMjd[i] * Time.SECONDS_IN_DAY;
            coords[0][i] = R * Math.cos(omega * t) / 1e3;
            coords[1][i] = R * Math.sin(omega * t)*Math.cos(yzAngle) / 1e3;
            coords[2][i] = R * Math.sin(omega * t)*Math.sin(yzAngle) / 1e3;
        }

        return coords;

    }

}
