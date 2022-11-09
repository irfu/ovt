/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2FileUtils_HourlyAvg.java $
 Date:      $Date: 2015/09/15 12:10:00 $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ovt.datatype.Time;
import ovt.util.FCBPTextTableFileReader;
import ovt.util.FCBPTextTableFileReader.FileColumnReader;
import ovt.util.FCBPTextTableFileReader.FileDoubleColumnReader;
import ovt.util.FCBPTextTableFileReader.FileIntColumnReader;
import ovt.util.Utils;

/**
 * See OMNI2DataSource for the class' documentation and purpose.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-0x-xx
 */
//
// PROPOSAL: Move localFileNamePattern and urlPattern out of class?
//    CON: Fits bad with testing code?
//
/*
 Excerpt from column descriptions:
 Source, 2015-09-04: ftp://spdf.gsfc.nasa.gov/pub/data/omni/low_res_omni/omni2.text
 -------------------------------------------------------------------------------
 OMNI2_YYYY.DAT FORMAT DESCRIPTION


 WORD  FORMAT  Fill Value         MEANING                  UNITS/COMMENTS

 1      I4              Year                              1963, 1964, etc.
 2      I4              Decimal Day                       January 1 = Day 1
 3      I3              Hour                              0, 1,...,23
 4      I5   9999      Bartels rotation number
 5      I3    0        ID for IMF spacecraft             See table
 6      I3    0        ID for SW plasma spacecraft       See table
 7      I4   999       # of points in the IMF averages
 8      I4   999       # of points in the plasma averages
 9      F6.1  999.9     Field Magnitude Average |B|       1/N SUM |B|, nT
 10     F6.1  999.9     Magnitude of Average Field Vector sqrt(Bx^2+By^2+Bz^2)
 11     F6.1  999.9     Lat.Angle of Aver. Field Vector   Degrees (GSE coords)
 12     F6.1  999.9     Long.Angle of Aver.Field Vector   Degrees (GSE coords)
 13     F6.1  999.9     Bx GSE, GSM                       nT
 14     F6.1  999.9     By GSE                            nT
 15     F6.1  999.9     Bz GSE                            nT
 16     F6.1  999.9     By GSM                            nT
 17     F6 1  999.9     Bz GSM                            nT
 18     F6.1  999.9     sigma|B|            RMS Standard Deviation in average
 magnitude (word 10), nT
 19     F6.1  999.9     sigma B             RMS Standard Deviation in field
 vector, nT (**)
 20     F6.1  999.9     sigma Bx            RMS Standard Deviation in GSE
 X-component average, nT
 21     F6.1  999.9     sigma By            RMS Standard Deviation in GSE
 Y-component average, nT
 22     F6.1  999.9     sigma Bz            RMS Standard Deviation in GSE
 Z-component average, nT

 23     F9.0  9999999.  Proton temperature                Degrees, K
 24     F6.1  999.9     Proton Density                    N/cm^3

 25     F6.0  9999.     Plasma (Flow) speed               km/s
 26     F6.1  999.9     Plasma Flow Long. Angle    Degrees, quasi-GSE*
 27     F6.1  999.9     Plasma  Flow Lat. Angle     Degrees, GSE*

 28     F6.3  9.999     Na/Np                    Alpha/Proton ratio
 29     F6.2  99.99     Flow Pressure            P (nPa) = (1.67/10**6) * Np*V**2 * (1+ 4*Na/Np)
 for hours with non-fill Na/Np ratios and
 P (nPa) = (2.0/10**6) * Np*V**2
 for hours with fill values for Na/Np

 30     F9.0  9999999.  sigma T                           Degrees, K
 31     F6.1  999.9     sigma N                           N/cm^3
 32     F6.0  9999.     sigma V                           km/s
 33     F6.1  999.9     sigma phi V                       Degrees
 34     F6.1  999.9     sigma theta V                     Degrees
 35     F6.3  9.999     sigma-Na/Np

 36     F7.2  999.99    Electric field         -[V(km/s) * Bz (nT; GSM)] * 10**-3. (mV/m)
 37     F7.2  999.99    Plasma beta            Beta = [(T*4.16/10**5) + 5.34] * Np / B**2
 38     F6.1  999.9     Alfven mach number      Ma = (V * Np**0.5) / 20 * B

 39     I3    99        Kp               Planetary Geomagnetic Activity Index
 (e.g. 3+ = 33, 6- = 57, 4 = 40, etc.)

 40      I4   999        R                          Sunspot number
 41      I6   99999     DST Index                         nT
 42      I5   9999      AE-index                    from NGDC
 43     F10.2 999999.99 Proton flux                 number/cmsq sec sr >1 Mev
 44     F9.2  99999.99  Proton flux                 number/cmsq sec sr >2 Mev
 45     F9.2  99999.99  Proton flux                 number/cmsq sec sr >4 Mev
 46     F9.2  99999.99  Proton flux                 number/cmsq sec sr >10 Mev
 47     F9.2  99999.99  Proton flux                 number/cmsq sec sr >30 Mev
 48     F9.2  99999.99  Proton flux                 number/cmsq sec sr >60 Mev
 49      I3   0         Flag(***)                       (-1,0,1,2,3,4,5,6)

 50      I4                                       ap-index, nT, from NGDC
 51       F6.1                                    f10.7_index, from NGDC
 52       F6.1                                    PC(N) index, from NGDC
 53       I6                                      AL-index, nT, from Kyoto
 54       I6                                     AU-index, nT, from Kyoto
 55       F5.1  99.9   Magnetosonic mach number= = V/Magnetosonic_speed
 Magnetosonic speed = [(sound speed)**2 + (Alfv speed)**2]**0.5
 The Alfven speed = 20. * B / N**0.5
 The sound speed = 0.12 * [T + 1.28*10**5]**0.5
 */
public class OMNI2FileUtils_HourlyAvg {

    /**
     * Filenames used for files on the local storage device (in the cache). Does
     * not necessarily have to be the same filenaming convention as implied by
     * FTP_URL_PATTERN. It is however probably wise(?) to follow the same naming
     * convention to not confuse the user, should the user ever have to manually
     * download OMNI2 data files and put them in the corresponding caching
     * directory.
     *
     * NOTE: This class does not (and should not) actually download and store
     * files on disk. Therefore, this code has to called by the code that does
     * that.
     */
    private final String localFileNamePattern;
    private final String urlPattern;


    // Choose buffer sizes based on expected number of data points (approximate, does not have to be exact).
    // NOTE: Some years are leap years and are longer.
    private final static int INITIAL_READ_BUFFER_SIZE = (366 * 24) + 1;   // Hours in leap year plus one.
    //private final int INITIAL_READ_BUFFER_SIZE = 1;   // DEBUG

    /**
     * Charset used for converting bytes to characters. Every implementation of
     * Java is required to have this charset:
     * https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html
     */
    private static final String TEXT_FILE_CHARSET = "ISO-8859-1";

    /**
     * Column widths for all columns in the hourly averaged OMNI2 data text
     * files. These are used for (1) easily deriving absolute positions and
     * widths of those columns that are actually of interest (and implicitly to
     * make it easy to use other columns), and (2) deriving the total length of
     * each line/row in the text files (for error checking: both of file format
     * and of the validity of this table).
     */
    private static final int[] TEXT_FILE_COLUMN_WIDTHS = {//
        4, 4, 3, 5, 3, 3, 4, 4, //
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, //
        9, //
        6, 6, 6, 6, 6, 6, //
        9, //
        6, 6, 6, 6, 6, //
        7, 7, 6, 3, 4, 6, 5, 10,//
        9, 9, 9, 9, 9, //
        3, 4, 6, 6, 6, 6, 5};
    private static final int[] COLUMNS_BEGIN = Utils.getCumulativeIntArray(TEXT_FILE_COLUMN_WIDTHS, false);   // Inclusive index.
    private static final int[] COLUMNS_END = Utils.getCumulativeIntArray(TEXT_FILE_COLUMN_WIDTHS, true);      // Exclusive index.
    private static final int N_chars_per_line = COLUMNS_END[COLUMNS_END.length - 1];   // Excluding CR, LF.

    private static final int INT_FILL_VALUE = Integer.MIN_VALUE;   // Only used internally in the present implementation.
    private final double doubleFillValue;


    //##########################################################################
    public OMNI2FileUtils_HourlyAvg(double mDoubleFillValue/*, int mIntFillValue*/, String mUrlPattern, String mLocalFileNamePattern) {
        doubleFillValue = mDoubleFillValue;
        //intFillValue = mIntFillValue;

        urlPattern = mUrlPattern;
        localFileNamePattern = mLocalFileNamePattern;
    }


    // QUESTION: How handle years before the first available OMNI2 data? Years for which there may be no data (incl. current year)?
    //   PROPOSAL: Hardcoded first year.
    public String getOnlineURL(int year) {
        return String.format(urlPattern, year);
    }


    public String getLocalFilename(int year) {
        return String.format(localFileNamePattern, year);
    }


    /**
     * Read and interpret hourly average OMNI2 file in the form of a (byte)
     * stream.
     *
     * IMPLEMENTATION NOTE: Takes data as an InputStream rather than a file
     * since reading data from a URL comes as an InputStream (and a file is
     * easily converted to a stream). Does not use "Reader" in class name since
     * this technically can refer to another form of stream (stream of
     * characters). Uses InputStream instead of Reader so that the class has
     * control over the byte-to-characters conversion.
     *
     * @param beginIncl_mjd Beginning (inclusive) of the time interval which the
     * returned OMNI2Data claims to cover. No data points must be earlier than
     * this value.
     * @param endExcl_mjd End (exclusive) of the time interval which the
     * returned OMNI2Data claims to cover. No data points must be later or equal
     * to this value.
     */
    public OMNI2Data read(InputStream in, double beginIncl_mjd, double endExcl_mjd) throws IOException {

        /**
         * Define the columns we want to read, and how we want to read them.<BR>
         * FCR = File column reader.
         */
        final FileIntColumnReader year_FCR = getFileIntColumnReader(1 - 1, null);
        final FileIntColumnReader doy_FCR = getFileIntColumnReader(2 - 1, null);   // doy = day of year
        final FileIntColumnReader hod_FCR = getFileIntColumnReader(3 - 1, null);   // hod = hour of day

        final FileDoubleColumnReader Kp_FCR = getFileDoubleColumnReader(39 - 1, "99");
        final FileDoubleColumnReader DST_FCR = getFileDoubleColumnReader(41 - 1, "99999");
        final FileDoubleColumnReader IMFx_nT_GSM_GSE_FCR = getFileDoubleColumnReader(13 - 1, "999.9");
        final FileDoubleColumnReader IMFy_nT_GSE_FCR = getFileDoubleColumnReader(14 - 1, "999.9");
        final FileDoubleColumnReader IMFz_nT_GSE_FCR = getFileDoubleColumnReader(15 - 1, "999.9");
        final FileDoubleColumnReader IMFy_nT_GSM_FCR = getFileDoubleColumnReader(16 - 1, "999.9");
        final FileDoubleColumnReader IMFz_nT_GSM_FCR = getFileDoubleColumnReader(17 - 1, "999.9");
        final FileDoubleColumnReader velocity_FCR = getFileDoubleColumnReader(25 - 1, "9999.");   // Actual fill value from looking at files.
        final FileDoubleColumnReader pressure_FCR = getFileDoubleColumnReader(29 - 1, "99.99");
        final FileDoubleColumnReader MA_FCR = getFileDoubleColumnReader(38 - 1, "999.9");
        final FileDoubleColumnReader Mms_FCR = getFileDoubleColumnReader(55 - 1, "99.9");

        final List<FileColumnReader> fileColumnReaders = new ArrayList();
        fileColumnReaders.add(year_FCR);
        fileColumnReaders.add(doy_FCR);
        fileColumnReaders.add(hod_FCR);

        fileColumnReaders.add(Kp_FCR);
        fileColumnReaders.add(DST_FCR);
        fileColumnReaders.add(IMFx_nT_GSM_GSE_FCR);
        fileColumnReaders.add(IMFy_nT_GSE_FCR);
        fileColumnReaders.add(IMFz_nT_GSE_FCR);
        fileColumnReaders.add(IMFy_nT_GSM_FCR);
        fileColumnReaders.add(IMFz_nT_GSM_FCR);
        fileColumnReaders.add(velocity_FCR);
        fileColumnReaders.add(pressure_FCR);
        fileColumnReaders.add(MA_FCR);
        fileColumnReaders.add(Mms_FCR);

        // Actually read the file.
        FCBPTextTableFileReader.readTable(new InputStreamReader(in, TEXT_FILE_CHARSET), N_chars_per_line, fileColumnReaders);

        // Create one single time_mjd field/array from the multiple time columns in the file.
        final int[] year = year_FCR.getBuffer();
        final int[] doy = doy_FCR.getBuffer();
        final int[] hod = hod_FCR.getBuffer();
        final double[] times_mjd = new double[year.length];
        for (int i = 0; i < year.length; i++) {
            final double time_mjd = Time.getMjd(year[i], 1, 1, hod[i], 0, 0) + (doy[i] - 1); // Technically cheating with leap seconds, maybe, since differences in mjd are not proportional to physical  time.

            /* // Check assertion. Check is now made in OMNI2Data constructor.
             if ((time_mjd < beginIncl_mjd) || (endExcl_mjd <= time_mjd)) {
             throw new IllegalArgumentException("File times do not fit the stated time interval.");
             }*/
            times_mjd[i] = time_mjd;
        }

        Map<OMNI2Data.FieldID, double[]> fieldArrays = new HashMap();

        fieldArrays.put(OMNI2Data.FieldID.time_mjd, times_mjd);
        fieldArrays.put(OMNI2Data.FieldID.Kp, Kp_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.DST, DST_FCR.getBuffer());

        fieldArrays.put(OMNI2Data.FieldID.SW_velocity_kms, velocity_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.SW_ram_pressure_nP, pressure_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.SW_M_A, MA_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.SW_M_ms, Mms_FCR.getBuffer());

        fieldArrays.put(OMNI2Data.FieldID.IMFx_nT_GSM_GSE, IMFx_nT_GSM_GSE_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.IMFy_nT_GSE, IMFy_nT_GSE_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.IMFz_nT_GSE, IMFz_nT_GSE_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.IMFy_nT_GSM, IMFy_nT_GSM_FCR.getBuffer());
        fieldArrays.put(OMNI2Data.FieldID.IMFz_nT_GSM, IMFz_nT_GSM_FCR.getBuffer());

        final OMNI2Data data;
        try {
            data = new OMNI2Data(beginIncl_mjd, endExcl_mjd, fieldArrays);
        } catch (IllegalArgumentException e) {
            /**
             * NOTE: Can be triggered if time is outside of time interval. Needs to
             * rethrow the IllegalArgumentException as IOException so that
             * caller can handle it correctly.
             */
            throw new IOException("Can not interpret OMNI2 file format (hourly averages).", e);
        }

        return data;
    }


    //##########################################################################
    /**
     * Integer columns are used internally for reading columns which are
     * converted to dates/times.
     */
    private FileIntColumnReader getFileIntColumnReader(int colIdx, String srcFillValue) {
        return new FileIntColumnReader(COLUMNS_BEGIN[colIdx], COLUMNS_END[colIdx], srcFillValue, INT_FILL_VALUE, INITIAL_READ_BUFFER_SIZE);
    }


    private FileDoubleColumnReader getFileDoubleColumnReader(int colIdx, String srcFillValue) {
        return new FileDoubleColumnReader(COLUMNS_BEGIN[colIdx], COLUMNS_END[colIdx], srcFillValue, doubleFillValue, INITIAL_READ_BUFFER_SIZE);
    }

}
