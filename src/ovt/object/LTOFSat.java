/*
 * LTOFSat.java
 *
 * Created on September 10, 2003, 4:14 PM
 */

package ovt.object;

import ovt.*;
import ovt.beans.*;
import ovt.interfaces.*;
import ovt.datatype.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author  ko
 */
public class LTOFSat extends Sat {
    
 /** Creates a new instance of LTOFSat */
  public LTOFSat(OVTCore core) {
      super(core);
 }
    
    

 /**
  * Gets min/max Mjd from LTOF datafile (reads all file from the beginning to the end. can by probabny optimized by moveing the pointer)
  */   
protected double[] getFirstLastMjdPeriodSatNumber() throws java.io.IOException {
    
    String line, lastTimeLine = null;
    int lineCount=0;
    double firstTime=-1000.0, lastTime=-1000.0;
    
     BufferedReader inData=new BufferedReader(new FileReader(orbitFile));
    
     /* taken from Fortran code:
     READ (CFILE_BUFFER,42,ERR=506)
       &NREC,DAYBEG,DAYEND,EPOCH,REVEPO,SMAXIS,OMOTIN
        42    FORMAT(I3,2F12.6,F15.9,F11.3,2F13.5)
     */
    while(inData.ready()) {
       line = inData.readLine();
       ++lineCount;
       if (line == null) break;
       
       if (line.length()<40) continue;
       
       if (line.startsWith("2")) { // this line contains time
          try {
              if (firstTime == -1000) firstTime = new Double(line.substring(3,15)).doubleValue();  // set firstTime
              else lastTimeLine = line;
          } catch (NumberFormatException nfe) {
              throw new IOException("Can not extract time from "+orbitFile+" line " +lineCount);
          }
       }
    }
    inData.close();
    
    // set lastTime
    LTOFRecord rec = new LTOFRecord();
    try {
        rec.set2ndRecord(lastTimeLine);
    } catch (IllegalArgumentException e2) {
        throw new IOException("Error in file '"+orbitFile+"' : "+e2);
    }
    double lastMjd =  rec.dayEnd;  //new Double(lastTimeLine.substring(15,27)).doubleValue();
    //Log.log(""+rec);
    double evolPeriodDays = rec.oMotin *2*Math.PI/Time.SECONDS_IN_DAY;
    
    int satNumber = new Integer(lastTimeLine.substring(1,3)).intValue();
    
    return new double[] {  firstTime+Time.Y2000,  lastMjd,  evolPeriodDays,  satNumber}; 
 }
    
/**Reads LTOFile, computes positions and velocities for given times */
protected void fill_GEI_VEI(double[] timeMap, double[][] gei_arr, double[][] vei_arr) throws IOException {
    BufferedReader inData;
    String line;
    int codeOfLine,i,j, k=0,lineNumber=0;
    double mjd = timeMap[k];

    try {
        inData = new BufferedReader(new FileReader(orbitFile));
    } catch (FileNotFoundException e){
        throw new IOException("File "+orbitFile+" not found.");
    } catch (IOException e){
        throw new IOException("IO error with "+orbitFile+" datafile.");
    }
    
    // the structure of the the LTOF is given in docs/LTOF.pdf
    

    while (inData.ready() && k<timeMap.length) {
        LTOFRecord rec = new LTOFRecord();
        line = inData.readLine();    // Read 1st record
        lineNumber++;
        //if (line == null) throw new IOException("Can not read line "+lineNumber+", file '"+orbitFile.getAbsolutePath()+"'");
        if(line.length()<40)
            throw new IOException("Error in the line "+lineNumber+", file '"+orbitFile.getAbsolutePath()+"': line.length < 40");
        
        char c = line.charAt(5);
        if (c != 'P' && c != 'R') continue; // search for the 1-st record. It contains the number of a satellite +2spaces +  P or R - Predict or Recon.
        // I3,X2,A1,... (Integer 123, two spaces, 1 char,...
        
        codeOfLine = new Integer(line.substring(0,3).trim()).intValue(); // trim to remove leading spaces
        //Log.log("mjd["+k+"] codeOfLine="+codeOfLine);
        
        //if (codeOfLine>=1 && codeOfLine<99 ){   //Satellite ID (sc_id)
        
        rec.sc_id=codeOfLine;  // Set up number of satellite (sc_id)!!!
        
        line = inData.readLine(); // Read 2nd record: 200+satin ...
        lineNumber++;
        try {
            rec.set2ndRecord(line);
        } catch (IllegalArgumentException e2) {
             throw new IOException("Error in the line "+lineNumber+" of file '"+orbitFile.getAbsolutePath()+"' : "+e2);
        }
        
        if ( mjd < rec.dayBeg )
            throw new IOException("The requested time ("+new Time(mjd)+") is  earlier than the data time.  "+lineNumber+", file '"+orbitFile.getAbsolutePath()+"'");
        
        if (   rec.dayEnd < mjd  )
            continue; // too early to fo forward... too early... the required record has not been reached yet.
        
        line = inData.readLine(); // Reading 3rd record: 30X ...
        lineNumber++;
        try {
            rec.set3rdRecord(line);
        } catch (Exception e2) { // why not IllegalArgumentException ???????????????????? hmm.....
            throw new IOException("Error in the line "+lineNumber+", file '"+orbitFile.getAbsolutePath()+"' : "+e2.getMessage());
        }
        
        for (i=1,j=0; i<=rec.coeffLinesNumber; ++i) { // Read lines with polynomial coefficients of x-y-z components of position vector
            line = inData.readLine();
            lineNumber++;
            if (rec.setDataRecord(i,line) !=0 )
                 throw new IOException("Error reading polynomial coefficients : "+orbitFile.getAbsolutePath()+": line "+lineNumber);
            /*{
                j=1;
                break; //Just skipping bad data lines
            }*/
        }
        
        // calculate and fill in gei_arr vei_arr  for the time valid for this record
        while (mjd<=rec.dayEnd && k<timeMap.length) {  //Treatment of MJDs as much as possible.
            //Log.log("k="+k+" timeMap.length="+timeMap.length+" ");
            mjd = timeMap[k];
            double[] posAndVel = solveKepler(mjd, rec);
            for (int jx=0; jx<3; jx++) {
                gei_arr[k][jx] = posAndVel[jx];
                vei_arr[k][jx] = posAndVel[jx+3];                
            }
           // Log.log("pos="+Vect.toString(gei_arr[k])); 
            k++;
        }
        
    } 
    inData.close();
}



 /** Keper solver. Returns S/C position & velocity for input mjd
  * @param Mjd
  * @return double []
  */
 public static double[] solveKepler(double day, LTOFRecord x){
    double[] X=new double[6]; //Outout position (km) & velocity (km/s)
    double dmanom=(day-x.epoch)*86400.0/x.oMotin;
    double revnum=x.revEpo+dmanom/6.2831853072;
    double arin,arm,rvwam,tam,comp,b,g0=0.0,g1=0.0,bet,d=0.0,g2,g3,fx,
       gx,rx,ft,gt,daymid,scale,s,pa,p,pb;
    int i,l;

    arin=x.smAxis/x.rDist;
    arm=(x.rDist-x.smAxis)/x.smAxis;
    rvwam=(x.Y[0]*x.Y[3]+x.Y[1]*x.Y[4]+x.Y[2]*x.Y[5])*x.oMotin/(x.smAxis*x.smAxis);

    // Calc. of ECC anomaly by Newton's iteration
    tam=dmanom-rvwam;
    comp=1.0e-7+1.0e-10*Math.abs(tam);
    b=tam;

    //Iterations to solve Kepler's equation
    for(i=1;i<=15;++i){
       g0=Math.cos(b);
       g1=Math.sin(b);
       bet=tam-arm*g1+rvwam*g0;
       d=(bet-b)/(1.0+arm*g0+rvwam*g1);
       b+=d;
       //This gives the accuracy 1.0e-14 in b & g's
       if(Math.abs(d)<=comp)
          break;
    }
    g0-=d*g1;
    g1+=d*g0;
    g2=1.0-g0;
    g3=b-g1;
    fx=1.0-g2*arin;
    gx=(dmanom-g3)*x.oMotin;
    
    for(i=0;i<3;++i)
       X[i]=fx*x.Y[i]+gx*x.Y[i+3];

    rx=Math.sqrt(X[0]*X[0]+X[1]*X[1]+X[2]*X[2]);
    ft=-g1*x.smAxis*arin/(x.oMotin*rx);
    gt=1.0-g2*x.smAxis/rx;
    
    for(i=3;i<6;++i)
       X[i]=ft*x.Y[i-3]+gt*x.Y[i];

    //Check if polynomial coefs. are required
    if(x.coeffLinesNumber<=1)return X;
    daymid=0.5*(x.dayBeg+x.dayEnd);
    scale=4.0/(x.dayEnd-x.dayBeg);
    //Add chebyshev polynomial to kepler state vector
    s=scale*(day-daymid);
    pa=1.0;
    p=s*0.5;
    for(i=0;i<6;++i)
       X[i]+=x.Coefs[0][i]+x.Coefs[1][i]*p;

    if(x.coeffLinesNumber<=2)return X;
    
    for(l=2;l<x.coeffLinesNumber;++l){
       pb=pa;
       pa=p;
       p=s*pa-pb;

       for(i=0;i<6;++i)
          X[i]+=x.Coefs[l][i]*p;
    }
    return X; 
 }


 /*
 public JMenuItem[] getMenuItems() {
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
  */

}


