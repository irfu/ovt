/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/ElectPotFileReader.java,v $
  Date:      $Date: 2003/09/28 17:52:55 $
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


/*
 * ElectPotFileReader.java
 * by Grzegorz Juchnikowski
 * utility helping reading of numerical data from ascii files.
 */

package ovt.util;

import java.io.*;

public class ElectPotFileReader {

  private FileReader fr;
  private StreamTokenizer tok;
  private boolean isopen;

  public ElectPotFileReader(){ isopen = false; }

  public ElectPotFileReader(String fn) throws IOException {
    isopen = false; 
    Open(fn); 
  }

  protected void finalize(){ Close(); }

  public void Open(String fn) throws IOException {
    Close();
    try {
      fr = new FileReader(fn);
    }catch(FileNotFoundException e){
      throw new IOException("ElectPotFileReader: file not found "+fn);
    }
    isopen = true;
    tok = new StreamTokenizer(fr);
    tok.resetSyntax();
    tok.wordChars(33,255);
    tok.whitespaceChars(0,32);
//    tok.parseNumbers();
    tok.eolIsSignificant(true);
  }

  public void Close(){
    if( isopen ){
      try {
        fr.close();
      }catch(IOException e){
        //ignore
      }
      isopen = false;
    }
  }

  private void checkOpen() throws IOException {
    if(! isopen ) throw new IOException("ElectPotFileReader: reading of not opened file");
  }

  private void checkEOF() throws IOException {
    if( tok.ttype == tok.TT_EOF ) throw new IOException("ElectPotFileReader: unexpected end of file");
  }

  private void checkEOL() throws IOException {
    if( tok.ttype == tok.TT_EOL ) throw new IOException("ElectPotFileReader: unexpected end of line");
  }

  public void SkipEOL() throws IOException {
    checkOpen();
    while( tok.nextToken() != tok.TT_EOL ) checkEOF();
  }

  public double GetNumber() throws IOException {
    checkOpen();
    tok.nextToken();
    checkEOF();
    checkEOL();
    if( tok.ttype == tok.TT_NUMBER ) return tok.nval;
    try {
      return new Double(tok.sval).doubleValue();
    }catch(NumberFormatException e){      
      throw new IOException("ElectPotFileReader: wrong numerical format \"" + tok.sval + "\"");
    }
  }

}
