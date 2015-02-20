/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/TLESorter.java,v $
  Date:      $Date: 2003/09/28 17:52:56 $
  Version:   $Revision: 1.10 $


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

package ovt.util;

/*
 * TLESorter.java
 *
 * Created on January 23, 2002, 4:42 PM
 */

import java.io.*;
import java.util.*;

/**
 * Sorts tle files with data 1960 - 2060
 * @author  ko
 * @version 
 */
public class TLESorter extends Object {

    public static final double Y1960 = 60000; // YYDDD
    
    /** Creates new TLESorter */
    public TLESorter() {
    }
    
    /** 
     * Lines are treeted as duplicated if time gap is less than 0.5 day
     */
    public static void sort(File infile, File outfile) throws IOException {
        BufferedReader in = new BufferedReader( new FileReader(infile) );
        long lineNumber = 0;
        byte prev_line_type = 1;
        Vector data = new Vector(200,50);
        String header = null;
        try {
            
            String line1 = "", line2 = "";
            while (true) { // until EOF
                lineNumber++;
                String s = in.readLine();
                
                if (s == null) throw new EOFException(); // end of file reached
                
                if (lineNumber == 1  &&  !s.startsWith("1 ")) {
                    // This is probably a header - usually name of the satellite
                    header = s;
                    continue;
                }
                
				// checksum
				if (s.length() == 68) {
					System.out.println("Warning: Looks like checksum (69-th char) is not present in the line "+lineNumber+". Computing checksum. THIS LINE MAY BE INCORRECT!");
					s += ""+TwoLines.computeChecksum(s);
				} else if (s.length() != 69) {
					throw new IOException("Incorrect width of line #"+lineNumber+". The line should contain 69 characters.");
				} else {
					// the length of the line is ok. Check the checksum.
					try {
						if (TwoLines.computeChecksum(s) != TwoLines.getChecksum(s))
							throw new IOException("Checksum error in the line "+lineNumber+". "+TwoLines.computeChecksum(s)+" != "+s.charAt(68) );
					} catch (NumberFormatException nfe) { throw new IOException("Checksum error in the line "+lineNumber+". "+TwoLines.computeChecksum(s)+" != "+s.charAt(68) ); }
				}
		
                //System.out.println(""+lineNumber+":"+s);
                
                if (s.startsWith("1 ")) {
                    if (!"".equals(line1)) throw new IOException("Incorrect ordering of lines : duplicate line '1' : line #"+lineNumber+")");
                    line1 = s;
                } else if (s.startsWith("2 ")) {
                    if (!"".equals(line2) || "".equals(line1)) throw new IOException("Incorrect ordering of lines (line #"+lineNumber+")");
                    line2 = s;
                    try {
                        data.addElement(new TwoLines(line1, line2));
                        line1 = ""; line2 = "";
                    } catch (NumberFormatException nfe) {
                        throw new IOException(nfe.getMessage() + " in line #"+(lineNumber-1));
                    }
                } else throw new IOException("Incorrect ordering of lines (line #"+lineNumber+")");
            
            }
            
        } catch (EOFException eof) { 
            // end of file reached
            in.close(); 
            // sort lines
            Collections.sort(data, new Comparator() {
                public int compare(Object o1, Object o2) {
                  double time1 = ((TwoLines)o1).getTime();
                  double time2 = ((TwoLines)o2).getTime();
                  if (time1 == time2) return 0;
                  // assume that if the time > Y1960 it is 20'throws sentury
                  // if time < 1960 it is 21 sentury
                  // so if time1 and time 2 are in the different senturies - the result is oposit to normal
                  if (time1 > Y1960 && time2 < Y1960) return -1;
                  if (time1 < Y1960 && time2 > Y1960) return 1;
                  
                  if (time1 > time2) return 1;
                  else return -1;
                }
            });
            
            BufferedWriter out = new BufferedWriter( new FileWriter(outfile) );
            if (header != null) 
	    	out.write(header+"\n");
	    else // if no header is given we have to generate it, because OVT needs it 
	        out.write(outfile+"\n");
		
            Enumeration e = data.elements();
            TwoLines prev_tl = null;
            long dup_lines_count = 0;
            while (e.hasMoreElements()) {
                TwoLines tl = (TwoLines)e.nextElement();
                // eliminate dupplicate lines
                if (prev_tl == null) { // this is the first written line
                    out.write(tl.getLine1()+"\n");
                    out.write(tl.getLine2()+"\n");
                    prev_tl = tl;
                } else if (Math.abs(prev_tl.getTime() - tl.getTime()) > 0.01) { // this is not a duplicate line
                    out.write(tl.getLine1()+"\n");
                    out.write(tl.getLine2()+"\n");
                    prev_tl = tl;
                } else { // this is a duplicate line
                    System.out.println("abs("+prev_tl.getTime() + " - " + tl.getTime()+") <= 0.01");
                    dup_lines_count++;
                }
            }
            out.close();
            if (dup_lines_count > 0) System.out.println("Removed "+(dup_lines_count*2)+" duplicated lines from "+lineNumber+" processed.");
        }
        outfile.setLastModified(infile.lastModified()); // preserve last modified
    }
    
    public static void main(String[] args) {
        if (args.length != 2) { 
            System.out.println("Usage: checktle infile.tle outfile.tle"); 
            System.exit(-1);
        }
        
        try {
            new TLESorter().sort(new File(args[0]), new File(args[1]));
            System.out.println("Done.");
        } catch (IOException e2) { 
            System.err.println(e2.getMessage()); 
            //e2.printStackTrace();
        }
        
    }

    
    
}

class TwoLines {

    private String line1, line2;
    private double time;
    
    TwoLines(String line1, String line2) throws NumberFormatException {
        this.line1 = line1;
        this.line2 = line2;
        
        try {
            int year = new Integer(line1.substring(18,20)).intValue();
            double day_of_year = new Double(line1.substring(20, 32)).doubleValue();
            this.time = 1000.*year + day_of_year;
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Incorrect time format ("+line1.substring(18, 32)+"). Should be yyddd.dddddddd.");
        }
    }
     
    
    public String getLine1() {
        return line1;
    }
    
    public String getLine2() {
        return line2;
    }
    
    public double getTime() {
        return time;
    }
    
	/** Comptes and returns the checksum for the line */
    public static int computeChecksum(String line) {
    	int csum = 0;
        for (int i = 0; i < 68; i++) 
		{
			if (line.charAt(i) == '-') { 
				csum++;
			} else {
	    		if (Character.isDigit(line.charAt(i))) csum += new Integer(""+line.charAt(i)).intValue();
	    	}
	    }
    	return csum % 10;
	}
	
	/** returns the 69-th element of the line (line[68]) */
	public static int getChecksum(String line) throws NumberFormatException {
    	return new Integer(""+line.charAt(68)).intValue();
    }
    
}
