/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/StringToFileWriter.java,v $
  Date:      $Date: 2003/09/28 17:52:56 $
  Version:   $Revision: 2.3 $


Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
Yuri Khotyaintsev)
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

import ovt.util.*;

import java.io.*;
import java.lang.*;
import java.util.*;

/** String writer
 * @author kono
 */
public class StringToFileWriter {
  
  private String fileName = "";
  private BufferedWriter writer;
  private boolean MODE = true; //append
  
  /**
   * -1 - output not initialized or IO error
   * 0 - output file is opened
   * >0 - number of writed records to output file
   */
  private int status = -1;

  /*
   * @param mode, true - append to file, false - overwrite
   */
  public StringToFileWriter(String fn, boolean mode){
    initialize(fn,mode);
  }
  
  //Open, save and close
  public StringToFileWriter(String fn, String data, boolean modeX){
    initialize(fn,modeX);
    writeString(data);
    close();
  }
  
  /*public Dumper(String fn,boolean pos,boolean fp,boolean mp,boolean spin){
    initialize(fn);
    this.setupDump(pos,fp,mp,spin);
  }*/
  
  public void initialize(String filename, boolean modex){
    if(getStatus()!=-1)
      close();
    this.fileName = new String(filename);
    setMode(modex);
    try {
      FileWriter fw = new FileWriter(this.fileName, getMode());
      writer = new BufferedWriter(fw);
      status = 0;
    } catch (IOException e){
      System.err.println("IO error with file "+fileName+" : "+e.toString());
      status = -1;
    }
  }
  
  
  /*
   *
   * 
   */
  public void writeString(String data){
    if(data==null || getStatus()==-1)
      return;
    try {
      writer.write(data);  // Writing data as one long string
      writer.newLine();    // Adding NL symbol
      ++status;
    } catch (IOException e){
      status = -1;
    }
  }
  
  public void close(){
    try {
      if(getStatus()!=-1)
        this.writer.close();
    } catch (IOException e){
    }
    status = -1;
  }
  
  public int getStatus(){
    return this.status;
  }
  
  public boolean getMode(){
    return this.MODE;
  }
  
  public void setMode(boolean md){
    this.MODE = md;
  }
  
  /*public static void main(String[] ee){
    //StringToFileWriter sfw = new StringToFileWriter("data.tmp", "&&&&",true);
    StringToFileWriter sfw = new StringToFileWriter("data.tmp",true);
    sfw.writeString("Hello!!!!!");
    sfw.writeString("aaaaaaaaaaaaaaaa\n\n\n######");
    sfw.close();
  }*/
}
