/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/util/Log.java,v $
 Date:      $Date: 2003/09/28 17:52:55 $
 Version:   $Revision: 2.4 $


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

/*
 * Log.java
 *
 * Created on October 11, 2000, 1:24 PM
 */
package ovt.util;

import java.io.*;

/**
 * Log class with only static methods. Log and error messages are "registered"
 * with a message level. Messages are only logged (printed/saved) if the message
 * level is lower or equal to the current "logLevel". Higher logLevel means more
 * detailed logs.
 *
 * NOTE: The policy in OVT (if there is any) for when to print to
 * System.out.println and when to print to Log, and with what message level is
 * very confused. Never use System.out.println?! Always use log? What if the log
 * is saved to disk and not printed to stdout? Special log method for printing
 * both to log and to stdout? System.out can be redirected to a log file and
 * Log.printStream can be set to System.out.
 *
 * NOTE: It is not clear if one should use log messages for debugging when
 * working with the code during development. Maybe one should use a separate Log
 * object (if this one could be instantiated)?
 *
 * Message and log levels:<BR>
 * 0 - No (non-error) log messages (?!)<BR>
 * 1 - Some essential log messages<BR>
 * 2 or greater - More esoteric/detailed log messages the higher the level.<BR>
 * 
 * NOTE: The code permits (no IllegalArgumentException) log messages with
 * level=0 since it is useful while debugging (?!). (Uncertain whether it is
 * advisable to throw IllegalArgumentException in any commonly used log method.)
 *
 * @author ko
 * @version
 */
// PROPOSAL: Use PrintWriter instead? Supposed to be used for printing characters.
public class Log extends Object {

    private static final int DEFAULT_MSG_LEVEL = 1;
    private static final int DEFAULT_ERROR_MSG_LEVEL = 0;

    /**
     * NOTE: There is an advantage in using System.err as the default for log
     * messages since System.err seems to always(?) end up in a log file by
     * default in actual installations (error.log) as opposed to System.out.
     */
    private static PrintStream printStream = System.err;

    private static int logLevel = 0;


    private Log() {
    }


    public static void setPrintStream(PrintStream output) {
        printStream = output;
    }


    /**
     * Useful for printing to the same stream, e.g. with custom functions that
     * accept it as an argument.
     */
//    public static PrintStream getPrintStream() {
//        return printStream;
//    }
    public static void setLogLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Log level must be non-negative.");
        }
        logLevel = level;
    }


//    public static int getDebugLevel() {
//        return debugLevel;
//    }
    /**
     * Log the string with the debug level
     */
    public static void log(String msg, int msgLevel) {
        if (msgLevel <= logLevel) {
            printStream.println(msg);
        }
    }


    public static synchronized void log(String msg) {
        log(msg, DEFAULT_MSG_LEVEL);
    }


    // NOTE: Method is called 35 times. /2017-10-04
    public static void err(String msg) {
        err(msg, DEFAULT_ERROR_MSG_LEVEL);
    }


    /**
     * Log the error string with the debug level.
     */
    // QUESTION: Should stop using since there is another "err" method?
    // NOTE: Method is called 6 times. /2017-10-04
    public static void err(String msg, int msgLevel) {
        if (msgLevel <= logLevel) {
            printStream.println("ERROR: " + msg);
        }
    }


    // PROPOSAL: Remove and use e.printStackTrace(Log.getPrintStream()) instead?
    public static void logStackTrace(Exception e) {
        // NOTE: Throwable#printStackTrace prints both (1) the contents of
        // the exception (message, toString()) and (2) the stack trace.
        e.printStackTrace(printStream);
    }
}
