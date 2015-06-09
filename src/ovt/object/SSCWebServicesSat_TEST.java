/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.object;

import java.io.File;
import java.io.IOException;
import ovt.Const;
import ovt.OVTCore;

/**
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Sat subclass for satellites where OVT itself downloads data from SSC Web
 * Services via the internet and caches the data internally.<BR>
 * <BR>
 * 
 */
// PROPOSAL: Change name? 
//    CON: The common thread is not SSC Web Services?
//    CON: Caching and download code is outside of class?
public class SSCWebServicesSat_TEST extends Sat {

    public SSCWebServicesSat_TEST(OVTCore core) {
        super(core);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setOrbitFile(File orbitFile) {
        //throw new UnsupportedOperationException("Not supported in this class. Class does not need an orbit file.");        
        throw new RuntimeException("Not supported in this class. Class does not need an orbit file.");        
    }

    @Override
    /** Does not return correct period and satellite number. */
    double[] getFirstLastMjdPeriodSatNumber() throws IOException {
        return new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 3.14, 314};
        //throw new RuntimeException("Not supported yet.");
    }

    @Override
    void fill_GEI_VEI(double[] timeMap, double[][] gei_arr, double[][] vei_arr) throws IOException {
        // TEST / DEBUG: Made-up orbit.
        for (int i=0; i<timeMap.length; i++) {
            gei_arr[i][0] = Const.RE * Math.cos(timeMap[i]) * 5;
            gei_arr[i][1] = Const.RE * Math.sin(timeMap[i]) * 10;
            gei_arr[i][2] = Const.RE * Math.sin(timeMap[i] * Math.sqrt(2)) * 0.2;
            vei_arr[i][0] = 10.0;
            vei_arr[i][1] = 20.0;
            vei_arr[i][2] = 30.0;
        }
        //throw new UnsupportedOperationException("fill_GEI_VEI not supported yet.");
    }

}
