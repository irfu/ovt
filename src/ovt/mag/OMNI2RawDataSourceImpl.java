/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2RawDataSourceImpl.java $
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

 OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson

 =========================================================================*/
package ovt.mag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import ovt.datatype.Time;
import ovt.util.Log;
import static ovt.util.Utils.downloadURLToFile;

/**
 * See OMNI2DataSource for the class' documentation and purpose.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-10
 */
//
// PROPOSAL: Move FIRST_YEAR___HOURLY_AVG to OMNI2FileUtils_HourlyAvg.
// PROPOSAL: Make OMNI2FileCache into separate generic class (in Utils?).
// PROPOSAL: Make downloadFile into separate generic method (in Utils).
//
public final class OMNI2RawDataSourceImpl implements OMNI2RawDataSource {

    /* The last year for which there is (expected) to be (hourly averaged) OMNI2
     * data. */
    private final int LAST_YEAR___HOURLY_AVG;

    private final static int FIRST_YEAR___HOURLY_AVG = 1963;

    /**
     * Determines the boundary between "older data" and "newer data". The age
     * refers to the time between the (approximate) present and the time when
     * the measurements were made.
     */
    private static final int NEWER_OLDER_MEASUREMENTS_AGE_BOUNDARY_DAYS = 365 / 2;

    /* How old a _FILE_ with "NEWER" data/measurements can get before it is being redownloaded. */
    private static final double NEWER_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS = 7;

    /* How old a _FILE_ with "OLDER" data/measurements can get before it is being redownloaded. */
    private static final double OLDER_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS = 365;

    private final OMNI2FileCache fileCache_hourlyAvg;
    private final OMNI2FileUtils_HourlyAvg fileUtils_hourlyAvg;

    /**
     * One single point in time to be used to represent the present. Has to be a
     * constant over the lifetime of an instance to ensure consistent caching
     * behaviour.
     */
    private final ZonedDateTime REFERENCE_NOW_UTC;


    //##########################################################################
    public OMNI2RawDataSourceImpl(File mOMNI2FileDir, String urlPattern_hourlyAvg, String localFileNamePattern) {

        REFERENCE_NOW_UTC = ZonedDateTime.now(ZoneOffset.UTC);
        LAST_YEAR___HOURLY_AVG = REFERENCE_NOW_UTC.getYear();

        fileCache_hourlyAvg = new OMNI2FileCache(mOMNI2FileDir, REFERENCE_NOW_UTC);

        fileUtils_hourlyAvg = new OMNI2FileUtils_HourlyAvg(
                OMNI2RawDataSource.DOUBLE_FILL_VALUE,
                urlPattern_hourlyAvg,
                localFileNamePattern
        );
    }


    @Override
    public OMNI2Data getData_hourlyAvg(int year) throws IOException {

        final double beginIncl_mjd = new Time(year + 0, 1, 1, 0, 0, 0).getMjd();
        final double endExcl_mjd = new Time(year + 1, 1, 1, 0, 0, 0).getMjd();

        if ((year < FIRST_YEAR___HOURLY_AVG) || (LAST_YEAR___HOURLY_AVG < year)) {
            return new OMNI2Data(beginIncl_mjd, endExcl_mjd);   // RETURN empty data.
        }

        final double maxFileAgeBeforeRedownload_days;
        if (year >= (getUTCYearDaysAgo(NEWER_OLDER_MEASUREMENTS_AGE_BOUNDARY_DAYS))) {
            // CASE: The requested data will come from a file which at least
            // partly contains "newer" data (data from recently made measurements).
            maxFileAgeBeforeRedownload_days = NEWER_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS;
        } else {
            maxFileAgeBeforeRedownload_days = OLDER_MEASUREMENTS_FILE_AGE_BEFORE_REDOWNLOAD_DAYS;
        }

        final String urlStr = fileUtils_hourlyAvg.getOnlineURL(year);
        final String localFilenameStr = fileUtils_hourlyAvg.getLocalFilename(year);

        final File file;
        try {
            file = fileCache_hourlyAvg.ensureFileExists(urlStr, localFilenameStr, maxFileAgeBeforeRedownload_days);
        } catch (java.io.FileNotFoundException e) {
            Log.err("Failed to download OMNI2 file from " + urlStr);
            if (year == LAST_YEAR___HOURLY_AVG) {
                // CASE: Tried to find file for the current year, but could not download it.
                // ==> It might be that there is no data file for the current year yet, as during the very beginning of a year. Assume this.
                // ==> Ignore error and return empty data.
                // This is not a perfect treatment but it should reduce the number of unnecessary error messages.
                //
                // NOTE: Still no new file (hourly averaged) on 2016-01-21 for the year 2016 (entire year).
                // ==> Can take >=21 days after new year before a file is available.
                Log.log("Return empty OMNI2 data for the current year since failed to download file for the current year.");
                return new OMNI2Data(beginIncl_mjd, endExcl_mjd);   // RETURN empty data.
            } else {
                throw e;
            }
        }

        try (InputStream in = new FileInputStream(file)) {
            return fileUtils_hourlyAvg.read(in, beginIncl_mjd, endExcl_mjd);
        } catch (IOException e) {
            throw new IOException("Can either not read or interpret file \"" + file.getCanonicalPath() + "\": " + e.getMessage(), e);
        }
    }


    @Override
    public int[] getYearMinMax_hourlyAvg() {
        return new int[]{FIRST_YEAR___HOURLY_AVG, LAST_YEAR___HOURLY_AVG};
    }


    private int getUTCYearDaysAgo(int days) {
        return REFERENCE_NOW_UTC.minusDays(days).getYear();
    }

    //##########################################################################
    /**
     * Class which implements a simple cache for downloaded (and uniquely named)
     * files.
     *
     * NOTE: The class is written to be generic and almost unrelated to OMNI2
     * files. (The printouts are the exception.) Could almost be moved to
     * ovt.utils.Util.java.
     */
    private class OMNI2FileCache {

        private final File cacheDir;
        private final double REFERENCE_NOW_MS;   // Time since 1970-01-01 00:00:00 UTC.


        public OMNI2FileCache(File mCacheDir, ZonedDateTime referenceNow) {
            cacheDir = mCacheDir;

            // ChronoZonedDateTime#toEpochSecond(): "Converts this date-time to the number of seconds from the epoch of 1970-01-01T00:00:00Z."
            REFERENCE_NOW_MS = referenceNow.toEpochSecond() * 1000;
        }


        /**
         * Always tries to return an object to an existing file. If there is no
         * file, then obtain it first.
         *
         * NOTE: The returned file will always be the same (within a session).
         * If a file is downloaded to replace an old file, that will only happen
         * the first time the file is requested. (This assumes that the file
         * modification date or the contents are not changed from the outside of
         * course.)
         *
         * @param urlStr The URL at which the original file exists.
         * @param localFilename The file name (not path) of the file on disk
         * (regardless of whether it exists or not).
         *
         * @return The requested file. Never null (will have exception instead).
         * @throws IOException for I/O errors including requesting a file which
         * does not exist (e.g. for a year for which there is no data).
         */
        public File ensureFileExists(String urlStr, String localFilename, double maxFileAgeBeforeRedownload_days)
                throws IOException {

            final File file = new File(cacheDir, localFilename);

            if (!file.isFile()) {
                // CASE: There is no local file. ==> Download

                cacheDir.mkdirs();    // Silent error. Let the later writing to file complain about errors.

                // NOTE: Allow failure to propagate since error here means not having any data.
                downloadFile(urlStr, file, "Downloading this file for the first time.");

            } else {
                // CASE: There is a local file. ==> Download again if it seems too old.

                /**
                 * NOTE: Uses last modification time to determine whether to
                 * redownload file. Ideally one would want to use the file
                 * creation time but that is not available on all platforms.
                 */
                // File#lastModified(): "measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)"
                final double lastModified_ms = file.lastModified();
                final double fileAge_days = (REFERENCE_NOW_MS - lastModified_ms) / (1000.0 * Time.SECONDS_IN_DAY);

                if (fileAge_days > maxFileAgeBeforeRedownload_days) {

                    // NOTE: Capture exception since failure is not too bad here. There is still old data to work with.
                    try {
                        downloadFile(urlStr, file, "Redownloading old file in case the online source has been updated.");
                    } catch (IOException e) {
                        final String msg = "Failed to redownload (possibly newer) OMNI2 file.";
                        Log.err(msg);
                        System.out.println("ERROR: "+msg);
                    }
                }
            }

            return file;
        }


        /**
         * The implementation should "somehow" obtain the file and put it on the
         * specificed path.
         */
        private void downloadFile(String urlStr, File mLocalFile, String msg) throws IOException {
            mLocalFile.getParentFile().mkdirs();   // Does not throw IOExeptions. Let the file writing handle failure to create directory.

            System.out.println("Downloading OMNI2 data file from " + urlStr);
            System.out.println("   " + msg);
            final long t_start = System.nanoTime();  // Not related to any external clock/time.

            final int bytesDownloaded = downloadURLToFile(urlStr, mLocalFile);

            final double duration = (System.nanoTime() - t_start) / 1.0e9;     // Unit: seconds
            System.out.printf("   Downloaded %d bytes, elapsed time %.1f s - Average speed: %.1f kiB/s" + System.lineSeparator(),
                    bytesDownloaded, duration, bytesDownloaded / duration / 1024.0);
        }
    }
    //##########################################################################


    /**
     * Informal test code.
     */
    public static void main(String[] args) throws IOException {
        final String URL_PATTERN = "http://spdf.gsfc.nasa.gov/pub/data/omni/low_res_omni/omni2_%4d.dat";
        final String LOCAL_FILE_NAME_PATTERN = "omni2_%4d.dat";
        final OMNI2RawDataSourceImpl ofu = new OMNI2RawDataSourceImpl(new File("/home/erjo/temp/cachedir/"), URL_PATTERN, LOCAL_FILE_NAME_PATTERN);
        OMNI2Data data;
        //data = ofu.getData_hourlyAvg(1990);
        //data = ofu.getData_hourlyAvg(1960);
        //data = ofu.getData_hourlyAvg(1990);
        data = ofu.getData_hourlyAvg(2015);
        data = ofu.getData_hourlyAvg(2016);
        data = ofu.getData_hourlyAvg(2017);
    }
}
