/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/Log.java,v $
  Date:      $Date: 2003/09/28 17:52:55 $
  Version:   $Revision: 2.4 $


Copyright (c) 2000-2003 OVT Team 
(Kristof Stasiewicz, Mykola Khotyaintsev, Yuri Khotyaintsev)
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
Khotyaintsev

=========================================================================*/

/*
 * Log.java
 *
 * Created on October 11, 2000, 1:24 PM
 */

package ovt.util;

import java.io.*;

/**
 *
 * @author  ko
 * @version 
 */
public class Log extends Object {
    protected static PrintStream out = System.err;
    // 0 - no debug
    // 1 - some essential things
    protected static int debugLevel = 0;

    /** Creates new Log */
    public Log() {
    }
    
    public static void setOut(PrintStream output) {
        out = output;
    }
    
    public static void setDebugLevel(int level) {
        debugLevel = level;
    }
    
    public static int getDebugLevel() {
        return debugLevel;
    }
    
    /** Log the string with the debug level */
    public static void log(String str, int debug) {
        if (debug<=debugLevel) {
            out.println(" " + str);
        }
    }

    public static synchronized void log(String str) {
        log(str, 1);
    }
    
    public static void err(String str) {
        err(str, 0);
    }
    
    /** Log the error string with the debug level */
    public static void err(String str, int debug) {
        if (debug<=debugLevel) {
            out.println(" " + str);
        }
    }
}
