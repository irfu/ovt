/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2RawDataSourceImpl.java $
 Date:      $Date: 2015/09/15 12:00:00 $
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
package ovt.mag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import ovt.datatype.Time;
import static ovt.util.Utils.downloadURLToFile;

/**
 * Class for utilities referring to all raw OMNI2 data.
 *
 * IMPLEMENTATION NOTE: The implementation is chosen such as to make certain
 * conceivable future changes reasonably easy:<BR>
 * 1) ADD support for another OMNI2 file type (averages over other time periods)
 * given another equivalent to OMNI2FileUtils_HourlyAvg.<BR>
 * 2) SWITCH between using different OMNI2 file types entirely<BR>
 * 3) implement usage of OMNI2 with different time resolution at the same time
 * (possibly with different resolution for different time intervals).
 *
 * NOTE: Different OMNI2 data files with different time resolutions have
 * available data for different (global) time intervals (different starting
 * years).
 *
 * NOTE: OMNI2 data files may have fill values for future times.
 *
 * IMPLEMENTATION NOTE: One could in principle move most of the functionality in
 * this class (hourly average data) into OMNI2FileUtils_HourlyAvg too but that
 * would mean mixing "data definitions" code with other code.
 *
 * NOTE: Things that should be common for all OMNI2 data file types (different
 * averages) and therefore should be here:<BR>
 * 1) fill values (not in OMNI2 files, but in java variables), <BR>
 * 2) how/if to cache downloaded data: filenaming conventions on disk, choice of
 * cache directory on disk. <BR>
 * 3) how to handle data availability time interval (for all OMNI2 data, all
 * time resolutions) (?), and how to handle the moving upper time boundary as
 * time progresses (may have to redownload files). <BR>
 *
 * IMPLENTATION NOTE: The code only reads the current time (time of execution,
 * walltime) once (twice really) to avoid a minor bug. If current time was read
 * multiple times, a data file could go from "recent enough to use" to "old
 * enough to be redownloaded" during the course of an OVT session. ==> Two
 * different versions of the file may be used read during the course of an OVT
 * session, something which MAY be undesirable.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-10
 */
public class OMNI2RawDataSourceImpl implements OMNI2RawDataSource {
    
    private final OMNI2FileCache hourlyAvgFileCache;

    private static final int FIRST_YEAR___HOURLY_AVG = 1963;
    private static final int LAST_YEAR___HOURLY_AVG = getUTCYearDaysAgo(0);

    /**
     * Determines the boundary between "old data" and "new data". The age refers
     * to the time when measurements were made.
     */
    private static final int NEWOLD_MEASUREMENTS_AGE_BOUNDARY_DAYS = 365 / 2;

    /* How old a _file_ with "new measurements" can get before it is being redownloaded. */
    private static final double NEW_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS = 7;

    /* How old a _file_ with "old measurements" can get before it is being redownloaded. */
    private static final double OLD_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS = 365;

    /**
     * One single point in time to be used to represent the present. Necessary
     * to ensure consistent caching behaviour over one OVT session.
     */
    private static final ZonedDateTime REFERENCE_NOW_UTC = ZonedDateTime.now(ZoneOffset.UTC);

    /**
     * One single point in time to be used to represent the present. Necessary
     * to ensure consistent caching behaviour over one OVT session.
     */
    private static final double CURRENT_TIME_MS = System.currentTimeMillis();   // "measured in milliseconds, between the current time and midnight, January 1, 1970 UTC." Really "long" but stored as double to avoid mistaken integer division.

    private static final OMNI2FileUtils_HourlyAvg hourlyAvg = new OMNI2FileUtils_HourlyAvg(DOUBLE_FILL_VALUE);
    
    //##########################################################################


    public OMNI2RawDataSourceImpl(File mOMNI2FileDir) {
        hourlyAvgFileCache = new OMNI2FileCache(mOMNI2FileDir);
    }


    @Override
    public OMNI2Data getData_hourlyAvg(int year) throws IOException {
        
        final double beginIncl_mjd = new Time(year, 1, 1, 0, 0, 0).getMjd();
        final double endExcl_mjd = new Time(year + 1, 1, 1, 0, 0, 0).getMjd();
        
        if ((year < FIRST_YEAR___HOURLY_AVG) || (LAST_YEAR___HOURLY_AVG < year)) {
            return new OMNI2Data(beginIncl_mjd, endExcl_mjd);   // Return empty data.
        }        

        double maxFileAgeBeforeRedownload_days;
        if (year >= (getUTCYearDaysAgo(NEWOLD_MEASUREMENTS_AGE_BOUNDARY_DAYS))) {
            maxFileAgeBeforeRedownload_days = NEW_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS;
        } else {
            maxFileAgeBeforeRedownload_days = OLD_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS;
        }

        final String urlStr = OMNI2FileUtils_HourlyAvg.getOnlineURL(year);
        final String localFilenameStr = OMNI2FileUtils_HourlyAvg.getDefaultFilename(year);

        File file;
        try {
            file = hourlyAvgFileCache.ensureFileExists(urlStr, localFilenameStr, maxFileAgeBeforeRedownload_days);
        } catch (java.io.FileNotFoundException e) {

            if (year == LAST_YEAR___HOURLY_AVG) {
                // CASE: Could not find file (URL) and tried to find file for the current year.
                // ==> It might be that there is no data file for the current year yet, as during the very beginning of a year. Assume this.
                // ==> Ignore error and return empty data.
                // This is not a perfect treatment but it should reduce the number of unnecessary error messages.
                return new OMNI2Data(beginIncl_mjd, endExcl_mjd);
            } else {
                throw e;
            }
        }

        try (InputStream in = new FileInputStream(file)) {
            return hourlyAvg.read(in, beginIncl_mjd, endExcl_mjd);
        }
    }
    
    @Override
    public int[] getYearMinMax_hourlyAvg() {
        return new int[] {FIRST_YEAR___HOURLY_AVG, LAST_YEAR___HOURLY_AVG};
    }


    private static int getUTCYearDaysAgo(int days) {
        return REFERENCE_NOW_UTC.minusDays(days).getYear();
    }

    //##########################################################################
    private class OMNI2FileCache {

        private final File cacheDir;


        public OMNI2FileCache(File mCacheDir) {
            cacheDir = mCacheDir;
        }


        /**
         * Always tries to return an object to an existing file. If there is no
         * file, then obtain it first.
         *
         * @return The requested file. Never null.
         */
        public File ensureFileExists(String urlStr, String localFilename, double maxFileAgeBeforeRedownload_days) throws IOException {
            final File file = new File(cacheDir, localFilename);

            if (!file.isFile()) {

                cacheDir.mkdirs();    // Silent error. Lets file writing complain about errors.

                // NOTE: Allow failure to propagate since error here means not having any data.
                downloadFile(urlStr, file, "First download");

            } else {

                /**
                 * NOTE: Uses last modification time to determine whether to
                 * redownload file. Ideally one would want to use the file
                 * creation time but that is not available on all platforms.
                 */
                final double lastModified_ms = file.lastModified(); // "measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)"
                final double fileAge_days = (CURRENT_TIME_MS - lastModified_ms) / (1000.0 * Time.SECONDS_IN_DAY);

                if (fileAge_days > maxFileAgeBeforeRedownload_days) {

                    // NOTE: Capture exception since failure is not too bad here. There is still old data to work with.
                    try {
                        downloadFile(urlStr, file, "Redownloading old file in case the online source has been updated.");
                    } catch (IOException e) {
                        System.out.println("ERROR: Failed to redownload (possibly newer) OMNI2 file.");
                    }
                }
            }

            return file;
        }


        /**
         * Implementation should "somehow" obtain the file and put it on the
         * specificed path.
         */
        public void downloadFile(String urlStr, File mLocalFile, String msg) throws IOException {
            mLocalFile.getParentFile().mkdirs();   // Does not throw IOExeptions. Let the file writing handle failure to create directory.

            System.out.println("Downloading OMNI2 data file from " + urlStr);
            System.out.println("   " + msg);
            final long t_start = System.nanoTime();  // Not related to any external clock/time.

            final int bytesDownloaded = downloadURLToFile(urlStr, mLocalFile);

            final double duration = (System.nanoTime() - t_start) / 1.0e9;     // Unit: seconds
            System.out.printf("   Downloaded %d bytes, elapsed time %.1f s - Average speed: %.1f kiB/s\n", bytesDownloaded, duration, bytesDownloaded / duration / 1024.0);
        }
    }
    //##########################################################################


    /**
     * Informal test code.
     */
    public static void main(String[] args) throws IOException {
        OMNI2RawDataSourceImpl ofu = new OMNI2RawDataSourceImpl(new File("/home/erjo/temp/cachedir/"));
        OMNI2Data data;
        //data = ofu.getData_hourlyAvg(1990);
        //data = ofu.getData_hourlyAvg(1960);
        //data = ofu.getData_hourlyAvg(1990);
        data = ofu.getData_hourlyAvg(2015);
        data = ofu.getData_hourlyAvg(2016);
        data = ofu.getData_hourlyAvg(2017);
    }
}
