/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import gov.nasa.gsfc.spdf.ssc.client.CoordinateComponent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import gov.nasa.gsfc.spdf.ssc.client.CoordinateData;
import gov.nasa.gsfc.spdf.ssc.client.CoordinateSystem;
import gov.nasa.gsfc.spdf.ssc.client.DataFileRequest;
import gov.nasa.gsfc.spdf.ssc.client.DataRequest;
import gov.nasa.gsfc.spdf.ssc.client.DataResult;
import gov.nasa.gsfc.spdf.ssc.client.DistanceUnits;
import gov.nasa.gsfc.spdf.ssc.client.FileResult;
import gov.nasa.gsfc.spdf.ssc.client.FilteredCoordinateOptions;
import gov.nasa.gsfc.spdf.ssc.client.FormatOptions;
import gov.nasa.gsfc.spdf.ssc.client.OutputOptions;
import gov.nasa.gsfc.spdf.ssc.client.ResultStatusCode;
import gov.nasa.gsfc.spdf.ssc.client.SSCDatabaseLockedException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SSCExternalException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SSCResourceLimitExceededException_Exception;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteData;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterInterface;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterService;
import gov.nasa.gsfc.spdf.ssc.client.SatelliteSpecification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

/**
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Class which supplies a small library of static functions for all accessing of
 * data from the Satellite Situation Center (SSC) Web Services. The purpose of
 * having this separate from SSCWSSat and the rest of OVT is to:<BR>
 * 1) as much as possible, isolate all dependence on gov.nasa.gsfc.spdf.ssc.*
 * (the SSC Web Services libraries),<BR>
 * 2) to make testing of this library easier, e.g. with external java code,<BR>
 * 3) make code easier to reuse in contexts other than OVT.<BR>
 *
 * NOTE: This class does not throw any SSC Web Services-specific exceptions to
 * the minimize dependence on the SSCWS package.
 *
 * IMPLEMENTATION NOTE: Some things (satellite list, privacy and important
 * notices, acknowledgements, sscService) could be initialized immediately in
 * the constructor but that would (should) result in throwing exceptions which
 * one does not want to throw since a static initializer is not permitted to
 * throw (checked) exceptions.
 *
 * API documentation for "gov.nasa.gsfc.spdf.ssc" at
 * http://sscweb.gsfc.nasa.gov/WebServices/SOAP/public/api/index.html
 *
 * Much of the implementation of actual calls to SSC Web Services is based on
 * what can be derived from the example code "WsExample.java" at
 * http://sscweb.gsfc.nasa.gov/WebServices/SOAP/WsExample.java
 *
 * NOTE: From the API documentation for SatelliteSituationCenterInterface: "It
 * is also likely that the object constructors used by a client do not function
 * exactly like the ones described here (client developers should not trust that
 * their constructors have the same default behavior). These types of
 * differences are determined by the language (Java, Perl, etc.) and the tools
 * and libararies (for example JAX-WS) the client is using."
 *
 *
 * NOTE: "SSCweb does not explicitly handle leap seconds (similar to Unix time
 * and most software). Times in UTC for orbit locations are converted to CDF
 * Epoch times for storage, and the same values are returned on conversion back
 * to UTC for the web services, but the leap second is stored with the same
 * value as the next second. I expect that times near the leap second are off by
 * some amount, both depending on the software used in computing the original
 * orbit locations and on our ingest process. Although not always connected to
 * the orbit computations, operators for many spacecraft do not explicitly add
 * each leap second to the onboard clock (instead corrected with normal clock
 * drift), or make the change on days near the leap second or skewed across the
 * leap second. Thus, orbit locations may be up to 8km near the leap second, but
 * this is not far off from the input accuracy of a few kilometers (especially
 * for two-line element inputs: about 1km at TLE time and increasing 1-2km per
 * day)."<BR>
 * Source: email 2015-06-15, 20:05-02:00,<BR>
 * Bobby Robert.M.Candey@nasa.gov 1-301-286-6707 NASA Goddard Space Flight
 * Center, Code 672 Greenbelt MD 20771 USA
 *
 */
// TODO: Go through code and set all or as many as possible options since one can not trust default values.
//
// PROPOSAL: Attempt to make library independent of OVT functions?
//    Ex: Time conversion functions
//    Ex: Coordinates system functions (if there are any).
// PROPOSAL: Measure and log download times (durations).
// PROPOSAL: Display GUI message when downloading (modal info window?).
//    CON: Should be done by caller.
// 
// PROPOSAL: Reorganize SSC Web Services exceptions somehow?
//    PROPOSAL: MalformedURLException seems to originate from "new SatelliteSituationCenterService(...)". Capture somehow? Rethrow as other exception?
// QUESTION: Which types of exception should be caught and rethrown as something else? Rethrown as what?
//    PROPOSAL: Exceptions which are hard to explain without understanding the insides of SSC Web Services.
// QUESTION: How reuse catching and rethrowing of exceptions?
//
// PROPOSAL: Somehow use
//      getPrivacyAndImportantNotices() ??
//      getAcknowledgements() ??
//      to display text somewhere in OVT.
//
public class SSCWSLibraryImpl extends SSCWSLibrary {

    /**
     * Only one "canonical" instance of SSCWSLibraryImpl is needed (except maybe
     * for some kind of testing). This is that one instance.
     */
    public static final SSCWSLibrary DEFAULT_INSTANCE = new SSCWSLibraryImpl();

    /* Data used for connecting to SSC Web Services. */
    private static final String WSDL_URL_STRING = "http://sscWeb.gsfc.nasa.gov/WS/ssc/2/SatelliteSituationCenterService?wsdl";
    private static final String QNAME_NAMESPACE_URI = "http://ssc.spdf.gsfc.nasa.gov/";
    private static final String QNAME_LOCAL_PART = "SatelliteSituationCenterService";

    private SatelliteSituationCenterService sscService = null;

    private List<SSCWSSatelliteInfo> allSatelliteInfoCache = null;
    private List<String> privacyAndImportantNotices = null;
    private List<String> acknowledgements = null;

    /**
     * Set the minimum log message level for this class.
     */
    private static final int DEBUG = 1;


    /**
     * Private constructor to prevent instantiation.
     */
    private SSCWSLibraryImpl() {
        /*try {
         } catch (MalformedURLException | SSCExternalException_Exception e) {
         throw new IOException("Can not retrieve SSC Privacy and Important Notices, or Acknowledgements.");
         }*/
    }


    /**
     * Return a SatelliteSituationCenterInterface object that can be used for
     * downloading data. Only used internally.
     */
    private SatelliteSituationCenterInterface getSSCInterface() throws MalformedURLException {
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
     * Cached during the program session.<BR>
     *
     * NOTE: Returns internal private instances of SatelliteDescription, not
     * copies.
     */
    @Override
    public List<SSCWSSatelliteInfo> getAllSatelliteInfo() throws IOException {
        /**
         * * NOTE: Judging from the SSC Web Services interface, it appears that
         * one can not download only selected satellite descriptions, only all
         * of them at once.
         */
        if (allSatelliteInfoCache == null) {

            final List<SatelliteDescription> satDescriptions;
            try {
                Log.log(this.getClass().getSimpleName() + ".getAllSatelliteInfo: Download satellite list from SSC Web Services.", DEBUG);
                final long t_start = System.nanoTime();

                satDescriptions = getSSCInterface().getAllSatellites();

                final double duration = (System.nanoTime() - t_start) / 1.0e9;  // Unit: seconds
                Log.log(this.getClass().getSimpleName() + ".getAllSatelliteInfo: Time used for downloading data: " + duration + " [s]", DEBUG);

            } catch (SSCExternalException_Exception ex) {
                throw new IOException("Could not complete request to SSC Web Services: " + ex.getMessage(), ex);
            }

            allSatelliteInfoCache = new ArrayList();
            for (SatelliteDescription satDescr : satDescriptions) {
                allSatelliteInfoCache.add(new SSCWSSatelliteInfo(satDescr));
            }
            allSatelliteInfoCache = Collections.unmodifiableList(allSatelliteInfoCache);
        }

        /* NOTE: The objects in the (immutable) list are themselves most likely mutable, which is not ideal. */
        return allSatelliteInfoCache;
    }


    @Override
    public double[][] getTrajectory_GEI(
            String satID,
            double beginMjdInclusive, double endMjdInclusive,
            int resolutionFactor)
            throws IOException {

        /* IMPLEMENTATION NOTE: De facto wrapper around the function that does the actual work.
         Uses this structure to make sure that a log messsage is written for all exceptions
         before rethrowing them. NOTE: They do keep the SAME stack trace.
         */
        try {
            return getTrajectoryRaw_GEI(satID, beginMjdInclusive, endMjdInclusive, resolutionFactor);
        } catch (Exception e) {
            Log.log("ERROR/EXCEPTION: " + e.getMessage(), DEBUG);
            throw e;   // Re-throws the same exception but keeps the stack trace.
        }
    }


    private double[][] getTrajectoryRaw_GEI(
            String satID,
            double beginMjdInclusive, double endMjdInclusive,
            int resolutionFactor)
            throws IOException {

        /*======================================================================
         Exact coordinate system used for the downloaded orbital positions.
         ============================================================
         The SSC Web Services API lists, among others, two different "GEI" coordinate systems.
         "GEI_J2000 : Geocentric Equatorial Inertial coordinate system with a Julian 2000 equinox epoch."
         "GEI_TOD : Geocentric Equatorial Inertial coordinate system with a true-of-date equinox epoch."
         As it appears from comparisons of trajectories from LTOF files, none of these is 
         exactly the same coordinate system.
         /Erik P G Johansson 2015-06-16.
         =====================================================================*/
        //final CoordinateSystem REQUESTED_CS = CoordinateSystem.GEO;
        //final CoordinateSystem REQUESTED_CS = CoordinateSystem.GEI_TOD;
        final CoordinateSystem REQUESTED_CS = CoordinateSystem.GEI_J_2000;

        final SatelliteSpecification satSpec = new SatelliteSpecification();
        satSpec.setId(satID);
        satSpec.setResolutionFactor(resolutionFactor);

        /* Start configuring a DataFileRequest. */
        final DataFileRequest dataFileReq = new DataFileRequest();
        dataFileReq.getSatellites().add(satSpec);
        try {
            /*==================================================================
             NOTE: The SSC Web Services API documentation claims that
             Request#setBeginTime and Request#setEndTime uses
             java.util.Calendar but in practice they only accept XMLGregorianCalendar.
             This difference is mentioned under "Important Notes:" in the API documentation for 
             "Interface SatelliteSituationCenterInterface"
             =================================================================*/
            dataFileReq.setBeginTime(convertMjdToXMLGregorianCalendar(beginMjdInclusive));
            dataFileReq.setEndTime(convertMjdToXMLGregorianCalendar(endMjdInclusive));
        } catch (DatatypeConfigurationException e) {
            throw new IOException("Could not construct request for SSC Web Services.", e);
        }

        /*======================================================================
         Set coordinate system for the data request
         ==========================================
         Must be done once for every coordinate axes X, Y, Z
         but not for other "coordinate components"
         (CoordinateComponent.LAT, .LOCAL_TIME, and .LON; gives error).
         =====================================================================*/
        final List<FilteredCoordinateOptions> filtCoordOptionList = new ArrayList<>();
        for (CoordinateComponent component : EnumSet.of(CoordinateComponent.X, CoordinateComponent.Y, CoordinateComponent.Z)) {
            final FilteredCoordinateOptions filtCoordOption = new FilteredCoordinateOptions();
            filtCoordOption.setCoordinateSystem(REQUESTED_CS);
            filtCoordOption.setComponent(component);
            filtCoordOption.setFilter(null);   // Used in the SSC Web Services example code ("WsExample.java"). Necessary?
            filtCoordOptionList.add(filtCoordOption);
        }
        final OutputOptions outputOptions = new OutputOptions();
        //outputOptions.setAllLocationFilters(true);       // Used in the SSC Web Services example code ("WsExample.java"). Necessary?
        outputOptions.getCoordinateOptions().addAll(filtCoordOptionList);
        dataFileReq.setOutputOptions(outputOptions);

        final FormatOptions formatOptions = new FormatOptions();
        formatOptions.setDistanceUnits(DistanceUnits.KM);   // Set unit: kilometers.
        dataFileReq.setFormatOptions(formatOptions);

        /*======================================================================
         Download data from SSC Web Service.
         ===================================
         NOTE: SatelliteSituationServiceInterface#getData throws
         SSCExternalException, SSCResourceLimitExceededException, and
         SSCDatabaseLockedException.        
         NOTE: One could also check dataResult.getStatusCode(), dataResult.getStatusSubCode() for error codes (strings?).
         NOTE: Requesting data for a time interval for which there is only partly data counts a ResultStatusCode.SUCCESS (not .CONDITIONAL_SUCCESS).
         ======================================================================*/
        final DataResult dataResult;
        try {
            //Log.log(this.getClass().getSimpleName() + ".getTrajectory_GEI: Download orbit data from SSC Web Services.", DEBUG);
            //final long t_start = System.nanoTime();

            dataResult = getSSCInterface().getData(dataFileReq);

            //final double duration = (System.nanoTime() - t_start) / 1.0e9;  // Unit: seconds
            //Log.log(this.getClass().getSimpleName() + ".getTrajectory_GEI: Time used for downloading data: " + duration + " [s]", DEBUG);
        } catch (SSCDatabaseLockedException_Exception | SSCExternalException_Exception | SSCResourceLimitExceededException_Exception e) {
            throw new IOException("Attempt to download data from SSC Web Services failed: " + e.getMessage(), e);
        }
        if (dataResult.getStatusCode() == ResultStatusCode.ERROR) {
            throw new IOException("Error when requesting data from SSC Web Services. dataResult.getStatusCode()=" + dataResult.getStatusCode()
                    + "; dataResult.getStatusSubCode()=" + dataResult.getStatusSubCode());
        }

        if (dataResult.getData().isEmpty()) {
            return new double[4][0];
        } else {
            final SatelliteData satData = dataResult.getData().get(0);   // Select data for satellite number 0 (there is only one satellite in the list).

            /*======================================================================
             Check the size of the data structure before reading so that it does not contain anything unexpected.
             I do not know why satData.getCoordinates() is a list since it always seems to contain exactly one single value.
             /Erik P G Johansson 2015-06-05.
             =======================================================================*/
            if (satData.getCoordinates().size() != 1) {
                throw new IOException("SSC Web Services returned a data structure with an unexpected size: "
                        + "satData.getCoordinates().size() = " + satData.getCoordinates().size());
            }
            final CoordinateData coordData = satData.getCoordinates().get(0);

            // Make sure the data uses a supported coordinate system.
            final CoordinateSystem receivedCS = coordData.getCoordinateSystem();
            if (!REQUESTED_CS.equals(receivedCS)) {
                throw new IOException("The orbit data downloaded from SSC Web Services "
                        + "uses the \"" + receivedCS + "\" coordinates system, which this method does not support.");
            }

            /*===========================================
             Convert data into data structure to return.
             ==========================================*/
            final List<Double> X = coordData.getX();
            final List<Double> Y = coordData.getY();
            final List<Double> Z = coordData.getZ();
            final List<XMLGregorianCalendar> timeList = satData.getTime(); // Define variable to reduce number of calls to satData.getTime() (or does the compiler figure that out itself?).

            int N_coord = coordData.getX().size();
            final double[][] coordinates_axisPos_kmMjd = new double[4][N_coord];   // axisPos = Indices [axis][position].

            for (int i = 0; i < N_coord; i++) {
                // NOTE: The call to convertXMLGregorianCalendarToMjd is conceivably
                // slow but so far (2015-08-10), no concrete problem has been observed.
                // One could in principle parallelize the call with something like java.util.Arrays.parallelSetAll.
                final double mjd = convertXMLGregorianCalendarToMjd(timeList.get(i));
                double[] position = new double[]{X.get(i), Y.get(i), Z.get(i)};

                // Change coordinate system from GEO to GEI.
                //position = Trans.geo_gei_trans_matrix(mjd).multiply(position);
                coordinates_axisPos_kmMjd[0][i] = position[0];
                coordinates_axisPos_kmMjd[1][i] = position[1];
                coordinates_axisPos_kmMjd[2][i] = position[2];
                coordinates_axisPos_kmMjd[3][i] = mjd;
            }
            return coordinates_axisPos_kmMjd;
        }

    }


    @Override
    public List<String> getPrivacyAndImportantNotices() throws IOException {
        if (privacyAndImportantNotices == null) {
            try {
                final FileResult fileResultPAN = getSSCInterface().getPrivacyAndImportantNotices();
                privacyAndImportantNotices = Collections.unmodifiableList(fileResultPAN.getUrls());
            } catch (SSCExternalException_Exception e) {
                throw new IOException("Can not retrieve SSC Privacy and Important Notices.", e);
            }
        }
        return privacyAndImportantNotices;
    }


    @Override
    public List<String> getAcknowledgements() throws IOException {
        if (acknowledgements == null) {
            try {
                final FileResult fileResultA = getSSCInterface().getAcknowledgements();
                acknowledgements = Collections.unmodifiableList(fileResultA.getUrls());
            } catch (SSCExternalException_Exception e) {
                throw new IOException("Can not retrieve SSC Acknowledgments.", e);
            }
        }
        return acknowledgements;
    }


    /**
     * Informal test code.
     */
    public static void test() throws IOException {
        SSCWSLibraryImpl lib = new SSCWSLibraryImpl();
        List<String> listPIN = lib.getPrivacyAndImportantNotices();
        List<String> listA = lib.getAcknowledgements();

        System.out.println("getPrivacyAndImportantNotices: ");
        for (String s : listPIN) {
            System.out.println("   s = " + s);
        }

        System.out.println("getAcknowledgements: ");
        for (String s : listA) {
            System.out.println("   s = " + s);
        }

    }

}
