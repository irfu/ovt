/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import gov.nasa.gsfc.spdf.ssc.client.CoordinateData;
import gov.nasa.gsfc.spdf.ssc.client.CoordinateSystem;
import gov.nasa.gsfc.spdf.ssc.client.DataFileRequest;
import gov.nasa.gsfc.spdf.ssc.client.DataResult;
import gov.nasa.gsfc.spdf.ssc.client.SSCDatabaseLockedException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SSCExternalException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SSCResourceLimitExceededException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteData;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterInterface;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterService;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSpecification;

/**
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Class which supplies a small library of static functions for accessing data
 * from the SSC Web Services. The purpose of having this separate from
 * SSCWebServicesSat is to<BR>
 * 1) isolate all dependence on gov.nasa.gsfc.spdf.ssc.* (the SSC WebServices
 * libraries),<BR>
 * 2) to make testing of this library easier, e.g. with external java code,<BR>
 * 3) make code easier to resuse in contexts other than OVT.<BR>
 *
 * API documentation for "gov.nasa.gsfc.spdf.ssc" at
 * http://sscweb.gsfc.nasa.gov/WebServices/SOAP/public/api/index.html
 *
 * Much is based on what can be derived from the example code "WsExample.java"
 * at http://sscweb.gsfc.nasa.gov/WebServices/SOAP/WsExample.java
 */
//
// PROPOSAL: Use SatelliteSituationCenterInterface.getAcknowledgements() for display somewhere in OVT.
// PROPOSAL: MalformedURLException seems to originate from "new SatelliteSituationCenterService(...)". Capture somehow? Rethrow as other exception?
// PROPOSAL: Attempt to make library independent of OVT functions?
//    Ex: Time conversion functions
//    Ex: Coordinates system functions (if there are any).
// PROPOSAL: Measure and log the time a download takes.
// QUESTION: Which types of exception should be caught and rethrown as something else? Rethrown as what?
//    PROPOSAL: Exceptions which are hard to explain without understanding the insides of SSC Web Services.
// QUESTION: How reuse catching and rethrowing of exceptions?
//
// PROPOSAL: Somehow use
//  getPrivacyAndImportantNotices() ??
//  getAcknowledgements() ??
//
public final class SSCWebServicesLibrary {

    /* Data used for connection to SSC Web Services. */
    private static final String WSDL_URL_STRING = "http://sscWeb.gsfc.nasa.gov/WS/ssc/2/SatelliteSituationCenterService?wsdl";
    private static final String QNAME_NAMESPACE_URI = "http://ssc.spdf.gsfc.nasa.gov/";
    private static final String QNAME_LOCAL_PART = "SatelliteSituationCenterService";

    private static SatelliteSituationCenterService sscService = null;


    /**
     * Return a SatelliteSituationCenterInterface object that can be used for
     * downloading data.
     */
    private static SatelliteSituationCenterInterface getSSCInterface() throws MalformedURLException {
        /*
         NOTE: The same SatelliteSituationCenterService object can and probably
         should should be reused for the duration of the entire application
         session (unless it expires somehow), i.e. over multiple requests and
         multiple class method calls but without being exposed outside the class
         (since it unnecessary).
        
         NOTE: I can not find the documentation (API) for neither
         gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterService
         nor its ancestor/superclass javax.xml.ws.Service.
         /Erik P G Johansson, IRFU 2015-06-05.
        
         QUESTION: Do SatelliteSituationCenterService objects expire somehow?
        
         QUESTION: Can/should/must one have exactly one service running at
         the same time for an application session? Can one shut it down when
         it is no longer needed?
         */
        if (sscService != null) {
            return sscService.getSatelliteSituationCenterPort();
        } else {
            /* "You are strongly encouraged to have your client set the HTTP User-Agent header (RFC 2068)
             to a value that identifies your client application in each SSC Web Service request that it makes.
             This will allow us to measure the usefulness of these services and justify their continued
             support. It isn't too important what value you use but it's best if it uniquely identifies
             your application."
             http://sscweb.gsfc.nasa.gov/WebServices/SOAP/DevelopersKit.html
             */
            System.setProperty("http.agent", "Orbit Visualization Tool (OVT; " + ovt.OVTCore.ovtHomePage + ") ("
                    + System.getProperty("os.name") + " "
                    + System.getProperty("os.arch") + ")");

            sscService = new SatelliteSituationCenterService(
                    new URL(WSDL_URL_STRING),
                    new QName(QNAME_NAMESPACE_URI, QNAME_LOCAL_PART));
            return sscService.getSatelliteSituationCenterPort();
        }

    }


    /**
     * Retrieve list of satellite descriptions.
     */
    public static List<SatelliteDescription> getSatelliteAllDescriptions() throws MalformedURLException, SSCExternalException_Exception {
        return getSSCInterface().getAllSatellites();
    }


    /**
     * Download orbit data for a given Earth satellite.
     *
     * @param satelliteID String referring to a satellite as returned by
     * gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription.getID().
     *
     * UNFINISHED
     */
    // PROPOSAL: Download data for multiple satellites at once?
    //    PRO: Useful for when the OVT GUI changes time interval and wants to update all satellite orbits simultaneously(?)
    //
    // TODO: Return time stamps of some sort.
    public static double[][] getOrbitData(String satelliteID, XMLGregorianCalendar beginTime, XMLGregorianCalendar endTime) throws Exception {
        final SatelliteSpecification satSpec = new SatelliteSpecification();
        satSpec.setId(satelliteID);
        satSpec.setResolutionFactor(1);  // Default value.

        final DataFileRequest dataFileReq = new DataFileRequest();
        dataFileReq.getSatellites().add(satSpec);
        dataFileReq.setBeginTime(null);

        final DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();

            /* NOTE: The API documentation claims that Request#setBeginTime and Request#setEndTime
             use java.util.Calendar but in practice they only accept XMLGregorianCalendar.
             */
            dataFileReq.setBeginTime(beginTime);
            dataFileReq.setEndTime(endTime);
        } catch (DatatypeConfigurationException ex) {
            throw new Exception("Could not construct request for SSC Web Services.");
        }

        /*===================================
         Download data from SSC Web Services.
         ====================================
         NOTE: SatelliteSituationServiceInterface#getData throws
         SSCExternalException,
         SSCResourceLimitExceededException,
         SSCDatabaseLockedException.        
         NOTE: One could also check dataResult.getStatusCode(), dataResult.getStatusSubCode() for error codes (strings?).
         */
        final DataResult dataResult = getSSCInterface().getData(dataFileReq);

        final SatelliteData satData = dataResult.getData().get(0);   // Select data for satellite number 0 (there is only one satellite in the list).

        /* Check the size of the data structure before reading so that it does not contain anything unexpected.
         I do not know why satData.getCoordinates() is a list since it always seems to contain exactly one single value.
         /Erik P G Johansson 2015-06-05. */
        if (satData.getCoordinates().size() != 1) {
            throw new Exception("SSC Web Services returned a data structure with an unexpected size: "
                    + "satData.getCoordinates().size() = " + satData.getCoordinates().size());
        }
        final CoordinateData coordData_GSE = satData.getCoordinates().get(0);

        // Make sure the data uses a supported coordinate system.
        // The code should eventually (?) support all possible coordinate system.
        final CoordinateSystem cs = coordData_GSE.getCoordinateSystem();
        if (!CoordinateSystem.GSE.equals(cs)) {
            throw new Exception("The orbit data downloaded from SSC Web Services "
                    + "uses the \"" + cs + "\" coordinates system, which this method does not yet support.");
        }

        /* Convert the returned data structure into
         the data format we want to return to the caller. */
        final List<Double> X = coordData_GSE.getX();
        final List<Double> Y = coordData_GSE.getY();
        final List<Double> Z = coordData_GSE.getZ();
        final int N = coordData_GSE.getX().size();
        double[][] coordinates_GSE = new double[3][N];
        for (int i = 0; i < N; i++) {
            coordinates_GSE[i][0] = X.get(i);
            coordinates_GSE[i][1] = Y.get(i);
            coordinates_GSE[i][2] = Z.get(i);
        }

        return coordinates_GSE;
    }


    /**
     * Private constructor to make instantiation impossible.
     */
    private SSCWebServicesLibrary() {
    }

}
