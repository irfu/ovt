/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2RawDataSource.java $
 Date:      $Date: 2015/09/15 13:17:00 $
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
package ovt.mag;

import java.io.IOException;

/**
 * Interface for raw OMNI2 data sources. One implementation is the actual source
 * of OMNI2 data. Other implementations can be sources for generating test data
 * for testing purposes. All OMNI2 data used by the application should pass
 * through an implementation of this interface.
 *
 * NOTE: The methods are PERMITTED to be DEPENDENT on the format of the
 * underlying OMNI2 files (or at least by how data is distributed over time and
 * over data files) but should of course still be as generic as possible.
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-xx
 */
public interface OMNI2RawDataSource {

    //public final static int INT_FILL_VALUE = Integer.MIN_VALUE;
    public final static double DOUBLE_FILL_VALUE = Double.NaN;


    /**
     * @return Never null. For years for which there is no data, an object
     * representing a time interval without data points is still returned.
     */
    public OMNI2Data getData_hourlyAvg(int year) throws IOException;


    public int[] getYearMinMax_hourlyAvg();
}
