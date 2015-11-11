/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/object/LTOFSat.java $
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
 
 OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
 =========================================================================*/
/*
 * Created on September 10, 2003, 4:14 PM
 */
package ovt.object;

import ovt.*;
import ovt.datatype.*;
import java.io.*;

/**
 *
 * @author ko
 */
public class LTOFSat extends Sat {

    /**
     * Creates a new instance of LTOFSat
     */
    public LTOFSat(OVTCore core) {
        super(core);
    }


    /**
     * Gets min/max Mjd from LTOF datafile (reads all file from the beginning to
     * the end. Can by probably be optimized by moving the pointer)
     */
    @Override
    protected double[] getFirstLastMjdPeriodSatNumber() throws java.io.IOException {

        String line, lastTimeLine = null;
        int lineCount = 0;
        double firstTime = -1000.0, lastTime = -1000.0;

        BufferedReader inData = new BufferedReader(new FileReader(orbitFile));

        /* taken from Fortran code:
         READ (CFILE_BUFFER,42,ERR=506)
         &NREC,DAYBEG,DAYEND,EPOCH,REVEPO,SMAXIS,OMOTIN
         42    FORMAT(I3,2F12.6,F15.9,F11.3,2F13.5)
         */
        while (inData.ready()) {
            line = inData.readLine();
            ++lineCount;
            if (line == null) {
                break;
            }

            if (line.length() < 40) {
                continue;
            }

            if (line.startsWith("2")) { // this line contains time
                try {
                    if (firstTime == -1000) {
                        firstTime = Double.parseDouble(line.substring(3, 15));  // set firstTime
                    } else {
                        lastTimeLine = line;
                    }
                } catch (NumberFormatException nfe) {
                    throw new IOException("Can not extract time from " + orbitFile + " line " + lineCount);
                }
            }
        }
        inData.close();

        // set lastTime
        LTOFRecord rec = new LTOFRecord();
        try {
            rec.set2ndRecord(lastTimeLine);
        } catch (IllegalArgumentException e2) {
            throw new IOException("Error in file '" + orbitFile + "' : " + e2);
        }
        double lastMjd = rec.dayEnd;  //new Double(lastTimeLine.substring(15,27)).doubleValue();
        //Log.log(""+rec);
        double evolPeriodDays = rec.oMotin * 2 * Math.PI / Time.SECONDS_IN_DAY;

        int satNumber = Integer.parseInt(lastTimeLine.substring(1, 3));

        return new double[]{firstTime + Time.Y2000, lastMjd, evolPeriodDays, satNumber};
    }


    /**
     * Reads LTOF file, computes positions and velocities for given times.
     */
    @Override
    protected void fill_GEI_VEI(double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr) throws IOException {

        try {
            fill_GEI_VEI_Raw(orbitFile, timeMjdMap, gei_arr, vei_arr);
        } catch (FileNotFoundException e) {
            throw new IOException("File " + orbitFile.getAbsolutePath() + " not found.");
        }  /*catch (IOException e){
         throw new IOException("IO error with "+orbitFile+" datafile");
         }*/

    }


    /**
     * Read arbitrary LTOF file. The structure of the LTOF is given in
     * docs/LTOF.pdf
     *
     * Some information on the LTOF format can be found in the "CLUSTER Data
     * Disposition System - Data Delivery Interface Document (DDID)"
     * http://www.jsoc.rl.ac.uk/pub/fd_files/index.php
     *
     * IMPLEMENTATION NOTE: This method is only used by the method fill_GEI_VEI
     * (and test code). It is however useful to separate the code that reads and
     * interprets the actual file from the rest of the class. The LTOFSat class
     * itself can not be instantiated without an OVTCore object which (probably)
     * can not easily be instantiated without launching the whole OVT GUI. With
     * this method separate, automatic test code can call it to read LTOF files
     * without having to launch the whole OVT GUI.
     */
    public static void fill_GEI_VEI_Raw(File LTOFFile, double[] timeMjdMap, double[][] gei_arr, double[][] vei_arr)
            throws IOException {

        /*=============
         Argument check.
         ==============*/
        if ((gei_arr.length > 0) && (gei_arr[0].length != 3)) {
            throw new IllegalArgumentException("Illegal array dimensions: gei_arr_posAxis[0].length != 3");
        } else if ((vei_arr.length > 0) && (vei_arr[0].length != 3)) {
            throw new IllegalArgumentException("Illegal array dimensions: vei_arr_posAxis[0].length != 3");
        } else if ((timeMjdMap.length != gei_arr.length)
                | (timeMjdMap.length != vei_arr.length)) {
            throw new IllegalArgumentException("Illegal array dimensions. Lengths are not identical.");
        }

        int i_mjdTimeMap = 0, lineNumber = 0;
        double mjd = timeMjdMap[i_mjdTimeMap];

        try (BufferedReader inData = new BufferedReader(new FileReader(LTOFFile))) {
            while (inData.ready() && i_mjdTimeMap < timeMjdMap.length) {
                final LTOFRecord rec = new LTOFRecord();

                {
                    final String line = inData.readLine();    // Read 1st record (assumption).
                    lineNumber++;

                    if (line.length() < 40) {
                        throw new IOException("Error in the line " + lineNumber + ", file '" + LTOFFile.getAbsolutePath() + "': line.length < 40");
                    }

                    final char c = line.charAt(5);   // Try read "PREREC".
                    if (c != 'P' && c != 'R') {
                        continue; // search for the 1-st record. It contains the number of a satellite +2spaces +  P or R - Predict or Recon.
                    }              // I3,X2,A1,... (Integer 123, two spaces, 1 char,...

                    final int codeOfLine = Integer.parseInt(line.substring(0, 3).trim()); // Trim to remove leading spaces
                    //Log.log("mjd["+k+"] codeOfLine="+codeOfLine);

                    //if (codeOfLine>=1 && codeOfLine<99 ){   //Satellite ID (sc_id)
                    rec.sc_id = codeOfLine;  // Set up number of satellite (sc_id)!!!
                }
                {
                    final String line = inData.readLine(); // Read 2nd record: 200+satin ...
                    lineNumber++;
                    try {
                        rec.set2ndRecord(line);
                    } catch (IllegalArgumentException e2) {
                        throw new IOException("Error in the line " + lineNumber + " of file '" + LTOFFile.getAbsolutePath() + "' : " + e2);
                    }

                    if (mjd < rec.dayBeg) {
                        throw new IOException("The requested time (" + new Time(mjd) + ") is  earlier than the data time.  " + lineNumber + ", file '" + LTOFFile.getAbsolutePath() + "'");
                    }

                    if (rec.dayEnd < mjd) {
                        continue; // too early to fo forward... too early... the required record has not been reached yet.
                    }
                }
                {
                    String line = inData.readLine(); // Reading 3rd record: 30X ...
                    lineNumber++;
                    try {
                        rec.set3rdRecord(line);
                    } catch (Exception e2) { // why not IllegalArgumentException ???????????????????? hmm.....
                        throw new IOException("Error in the line " + lineNumber + ", file '" + LTOFFile.getAbsolutePath() + "' : " + e2.getMessage());
                    }

                    for (int i = 1 /*, j = 0*/; i <= rec.coeffLinesNumber; ++i) { // Read lines with polynomial coefficients of x-y-z components of position vector
                        line = inData.readLine();
                        lineNumber++;
                        if (rec.setDataRecord(i, line) != 0) {
                            throw new IOException("Error reading polynomial coefficients : " + LTOFFile.getAbsolutePath() + ": line " + lineNumber);
                        }
                        /*{
                         j=1;
                         break; //Just skipping bad data lines
                         }*/
                    }
                }

                // Calculate and fill in "gei_arr", "vei_arr" for the time valid for this LTOF record.
                while (mjd <= rec.dayEnd && i_mjdTimeMap < timeMjdMap.length) {  //Treatment of MJDs as much as possible.
                    //Log.log("k="+k+" timeMap.length="+timeMap.length+" ");
                    mjd = timeMjdMap[i_mjdTimeMap];

                    // BUG FIX. /Erik P G Johansson 2015-08-24
                    if (mjd > rec.dayEnd) {
                        break;
                    }

                    final double[] posAndVel = solveKepler(mjd, rec);
                    for (int jx = 0; jx < 3; jx++) {
                        gei_arr[i_mjdTimeMap][jx] = posAndVel[jx];
                        vei_arr[i_mjdTimeMap][jx] = posAndVel[jx + 3];
                    }
                    // Log.log("pos="+Vect.toString(gei_arr[k]));
                    i_mjdTimeMap++;
                }

            }   // while
        }  // try-with-resources
    }


    /**
     * Kepler solver. Returns S/C position & velocity for input mjd.
     *
     * @param Mjd
     * @return Six component-array. The first three components are the position,
     * the last three components are the velocity.
     */
    private static double[] solveKepler(double day, LTOFRecord rec) {
        final int N_MAX_KEPLERS_EQUATION_ITERATIONS = 15;
        final double[] pos_vel = new double[6]; //  Output position (km) & velocity (km/s)
        //final double revnum = x.revEpo + dmanom / 6.2831853072;   // Not used?!!

        {
            // rec.oMotin = Inverse mean motion.
            // rec.smAxis = Semimajor axis.
            // rec.rDist = Absolute value of the reference Kepler orbit position vector (distance to origin).
            final double dmanom = (day - rec.epoch) / (rec.oMotin * Time.DAYS_IN_SECOND);  // Mean anomaly, counting since "rec.epoch". Unit: day/(day/rad)= rad
            final double arin = rec.smAxis / rec.rDist;                  // Unit: None
            final double arm = (rec.rDist - rec.smAxis) / rec.smAxis;    // Unit: None      
            final double rvwam = (rec.Y[0] * rec.Y[3]
                    + rec.Y[1] * rec.Y[4]
                    + rec.Y[2] * rec.Y[5])
                    * rec.oMotin / (rec.smAxis * rec.smAxis);  // Unit: km^2/s * s/rad / km = km/rad

            // Calc. of ECC anomaly by Newton's iteration.
            final double tam = dmanom - rvwam;

            // Iterate to solve Kepler's equation.
            double b = tam;
            double g0 = 0.0;
            double g1 = 0.0;
            double d = 0.0;
            final double comp = 1.0e-7 + 1.0e-10 * Math.abs(tam);
            for (int i = 1; i <= N_MAX_KEPLERS_EQUATION_ITERATIONS; ++i) {
                g0 = Math.cos(b);
                g1 = Math.sin(b);
                final double bet = tam - arm * g1 + rvwam * g0;
                d = (bet - b) / (1.0 + arm * g0 + rvwam * g1);
                b += d;
                // This gives the accuracy 1.0e-14 in b & g's.
                if (Math.abs(d) <= comp) {
                    break;
                }
            }
            g0 -= d * g1;
            g1 += d * g0;
            final double g2 = 1.0 - g0;
            final double g3 = b - g1;
            final double fx = 1.0 - g2 * arin;
            final double gx = (dmanom - g3) * rec.oMotin;

            // Set POSITION.
            for (int i = 0; i < 3; ++i) {
                pos_vel[i] = fx * rec.Y[i] + gx * rec.Y[i + 3];  // NOTE: Uses both position and velocity information.
            }

            final double rx = Math.sqrt(
                    pos_vel[0] * pos_vel[0]
                    + pos_vel[1] * pos_vel[1]
                    + pos_vel[2] * pos_vel[2]);  // Distance to origin.
            final double ft = -g1 * rec.smAxis * arin / (rec.oMotin * rx);
            final double gt = 1.0 - g2 * rec.smAxis / rx;

            // Set VELOCITY.
            for (int i = 3; i < 6; ++i) {
                pos_vel[i] = ft * rec.Y[i - 3] + gt * rec.Y[i];  // NOTE: Uses both position and velocity information.
            }
        }

        // Check if polynomial coeffs. are required.
        if (rec.coeffLinesNumber <= 1) {
            return pos_vel;    // EXIT function.
        }

        final double daymid = 0.5 * (rec.dayBeg + rec.dayEnd);
        final double scale = 4.0 / (rec.dayEnd - rec.dayBeg);
        // Add chebyshev polynomial to kepler state vector.
        final double s = scale * (day - daymid);

        double p = s * 0.5;

        for (int i = 0; i < 6; ++i) {
            pos_vel[i] += rec.coeffs[0][i] + rec.coeffs[1][i] * p;  // Add to POSITION and VELOCITY.
        }

        if (rec.coeffLinesNumber <= 2) {
            return pos_vel;    // EXIT function.
        }

        double pa = 1.0;
        for (int l = 2; l < rec.coeffLinesNumber; ++l) {
            final double pb = pa;
            pa = p;
            p = s * pa - pb;

            for (int i = 0; i < 6; ++i) {
                pos_vel[i] += rec.coeffs[l][i] * p;  // Add to POSITION and VELOCITY.
            }
        }

        return pos_vel;    // EXIT function.
    }

    /*public JMenuItem[] getMenuItems() {
     JMenuItem item0 = new JMenuItem("Info");
     item0.setFont(Style.getMenuFont());
     item0.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent evt) {
     SatInfoWindow infoWindow = new SatInfoWindow(getCore().getXYZWin());
     infoWindow.setObject(LTOFSat.this);
     infoWindow.setVisible(true);
     }
     }); 
     
     JMenuItem item1 = new JMenuItem("Orbit Monitor");
     item1.setFont(Style.getMenuFont());
     item1.setEnabled(isEnabled());
     item1.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent evt) {
     orbitMonitorModule.setVisible(true);
     }
     });
     JMenuItem item2 = new JMenuItem("Load data...");
     item2.setFont(Style.getMenuFont());
     item2.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent evt) {
     LoadDataWizard wizard = new LoadDataWizard(LTOFSat.this, getCore().getXYZWin());
     DataModule data = wizard.start();
     if (data != null) {
     addPropertyChangeListener("enabled", data);
     addChild(data);
     }
     }
     });
     return new JMenuItem[]{ item0, null, item1, null, item2 };
     }
     //*/
}
