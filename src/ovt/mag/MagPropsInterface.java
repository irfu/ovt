/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/MagPropsInterface.java,v $
 Date:      $Date: 2015/10/21 17:07:00 $
 Version:   $Revision: 1.00 $


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
import ovt.OVTCore;

/**
 * Interface which MagProps implements as it is (without modifying MagProps).
 * This interface is only meant to specify a subset of methods which MagProps
 * already has(!). Its only purpose is to make testing easier since it makes it
 * possible to implement and instantiate "fake MagProp" classes which can be
 * used to instantiate other classes that require a reference to MagProps but in
 * reality only use a small subset of its methods. This makes it possible to
 * launch these without launching all of OVT since MagProps itself is hard to
 * instantiate without all of OVT. (The same problem exists with OVTCore.)
 *
 * NOTE: Only works for instance methods, but not for static methods and not for
 * (instance/static) variables.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-10-20
 */
public interface MagPropsInterface {

    //public String getActivityName(int index);
    public OVTCore getCore();


    // See MagProps#getActivityOMNI2.
    public double[] getActivityOMNI2(int activityIndex, double mjd)
            throws OMNI2DataSource.ValueNotFoundException, IOException;


    public MagProps.ActivityEditorOrOMNI2_DataModel getActivityEditorOrOMNI2_DataModel(int activityIndex);
}
