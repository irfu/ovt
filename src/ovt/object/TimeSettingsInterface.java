/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/object/TimeSettingsInterface.java,v $
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
package ovt.object;

import ovt.interfaces.TimeChangeListener;

/**
 * Interface which TimeSettings implements as it is (without modifying TimeSettings).
 * This interface is only meant to specify a subset of methods which TimeSettings
 * already has(!). Its only purpose is to make testing easier since it makes it
 * possible to implement and instantiate "fake TimeSettings" classes which can be
 * used to instantiate other classes that require a reference to TimeSettings but in
 * reality only use a small subset of its methods. This makes it possible to
 * launch these without launching all of OVT since TimeSettings itself is hard to
 * instantiate without all of OVT. (The same problem exists with e.g. OVTCore.)
 *
 * NOTE: Only works for instance methods, but not for static methods and not for
 * (instance/static) variables.
 *
 * NOTE: Analogous with ovt.mag.MagPropsInterface.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 */
public interface TimeSettingsInterface {
      public void addTimeChangeListener (TimeChangeListener listener);
      public double getCurrentMjd();
}
