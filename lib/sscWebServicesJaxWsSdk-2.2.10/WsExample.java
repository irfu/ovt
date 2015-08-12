/*
 * NOSA HEADER START
 *
 * The contents of this file are subject to the terms of the NASA Open 
 * Source Agreement (NOSA), Version 1.3 only (the "Agreement").  You may 
 * not use this file except in compliance with the Agreement.
 *
 * You can obtain a copy of the agreement at
 *   docs/NASA_Open_Source_Agreement_1.3.txt
 * or 
 *   http://sscweb.gsfc.nasa.gov/WebServices/NASA_Open_Source_Agreement_1.3.txt.
 *
 * See the Agreement for the specific language governing permissions
 * and limitations under the Agreement.
 *
 * When distributing Covered Code, include this NOSA HEADER in each
 * file and include the Agreement file at 
 * docs/NASA_Open_Source_Agreement_1.3.txt.  If applicable, add the 
 * following below this NOSA HEADER, with the fields enclosed by 
 * brackets "[]" replaced with your own identifying information: 
 * Portions Copyright [yyyy] [name of copyright owner]
 *
 * NOSA HEADER END
 *
 * Copyright (c) 2006-2014 United States Government as represented by the 
 * National Aeronautics and Space Administration. No copyright is claimed 
 * in the United States under Title 17, U.S.Code. All Other Rights Reserved.
 *
 * $Id: WsExample.java,v 1.3.4.24.2.26 2014/09/19 14:41:04 bharris Exp $
 */

package gov.nasa.gsfc.spdf.ssc.test;


import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.text.SimpleDateFormat;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.sun.xml.ws.developer.JAXWSProperties;


import gov.nasa.gsfc.spdf.ssc.client.*;


public class WsExample {


    private static final SimpleTimeZone UTC_TIME_ZONE =
                                           new SimpleTimeZone(0, "UTC");

    private static final SimpleDateFormat DATE_FORMATTER;

    static {

        DATE_FORMATTER = new SimpleDateFormat("yyyy/DDD HH:mm:ss");
        DATE_FORMATTER.setTimeZone(UTC_TIME_ZONE);
    }


    public static void main(String args[]) 
        throws Exception {

        // ---------- SSC Web Services code ----------
        System.setProperty("http.agent", "WsExample (" + 
                           System.getProperty("os.name") + " " + 
                           System.getProperty("os.arch") + ")");

        SatelliteSituationCenterService service =
            new SatelliteSituationCenterService(
                new URL(args[0]),
                new QName("http://ssc.spdf.gsfc.nasa.gov/",
                          "SatelliteSituationCenterService"));

        SatelliteSituationCenterInterface ssc =
            service.getSatelliteSituationCenterPort();
        // ---------- end SSC Web Services code ----------

        Map<String, Object> requestContextMap =
                ((BindingProvider)ssc).getRequestContext();
                                       // request context map

        String dumpMsgs = System.getProperty(
            "com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump");

        if (dumpMsgs == null || !dumpMsgs.equals("true")) {

            // Optional to request Fast Infoset (ITU-T Rec. X.891 | 
            // ISO/IEC 24824-1) encoding ("binary XML").
            requestContextMap.put(
                JAXWSProperties.CONTENT_NEGOTIATION_PROPERTY, "pessimistic");
        }

        // requestContextMap.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
        //     args[0]);

        Date t1 = new Date();

        // ---------- SSC Web Services code ----------
        FileResult result = ssc.getPrivacyAndImportantNotices();
        // ---------- end SSC Web Services code ----------

        Date t2 = new Date();

        System.out.println("Privacy and Important Notices:");

        List<String> urls = result.getUrls();

        for (String url : urls) {

            System.out.println("  " + url);
        }

        long etGetPrivacyAndImportantNotices = 
            t2.getTime() - t1.getTime();

        System.out.println(
            "Time to execute getPrivacyAndImportantNotices = " +
            (double)etGetPrivacyAndImportantNotices / 1000.0 + 
            "seconds");

        t1 = new Date();

        // ---------- SSC Web Services code ----------
        List<SatelliteDescription> satellites = ssc.getAllSatellites();
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        System.out.println("\n\nSatellites:");

        for (SatelliteDescription sat : satellites) {

            System.out.println(
                "  " + sat.getId() + "  " + sat.getName() +
                "  " + sat.getResolution() + "s " + 
                sat.getStartTime() + " - " + sat.getEndTime() + "  " +
                "  " + sat.getGeometry() + "  " + 
                sat.getTrajectoryGeometry());
        };

        long etGetAllSatellites = t2.getTime() - t1.getTime();

        System.out.println("Number of satellites: " + satellites.size());
        System.out.println("Time to execute getAllSatellites = " +
            (double)etGetAllSatellites / 1000.0 + "seconds");


        t1 = new Date();

        // ---------- SSC Web Services code ----------
        List<SpaseObservatoryDescription> observatories = 
            ssc.getAllSpaseObservatories();
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        System.out.println("\n\nSPASE Observatories:");

        for (SpaseObservatoryDescription observatory : observatories) {

            System.out.println(
                "  " + observatory.getId() + "  " + 
                observatory.getResourceId());

            for (String groupId : observatory.getGroupIds()) {

                System.out.println("    " + groupId);
            }
        }

        long etGetAllObservatories = t2.getTime() - t1.getTime();

        System.out.println("Number of observatories: " + 
            observatories.size());
        System.out.println(
            "Time to execute getAllSpaseObservatories = " +
            (double)etGetAllObservatories / 1000.0 + "seconds");

        t1 = new Date();

        // ---------- SSC Web Services code ----------
        List<GroundStationDescription> stations = 
            ssc.getAllGroundStations();
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        System.out.println("\n\nGround Stations:");

        for (GroundStationDescription station : stations) {

            System.out.println(
                "  " + station.getId() +
                "  " + station.getLatitude() + 
                "  " + station.getLongitude() +
                "  " + station.getName());
        };

        long etGetAllStations = t2.getTime() - t1.getTime();

        System.out.println("Number of ground stations: " + 
            stations.size());
        System.out.println("Time to execute getAllGroundStations = " +
            (double)etGetAllStations / 1000.0 + "seconds");

        Calendar calendar = 
            GregorianCalendar.getInstance(UTC_TIME_ZONE);
        Calendar endDate = (Calendar)calendar.clone();
        calendar.add(Calendar.DATE, -1);
        Calendar startDate = (Calendar)calendar.clone();

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        final XMLGregorianCalendar xmlStart = 
            datatypeFactory.newXMLGregorianCalendar(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH) + 1,
                startDate.get(Calendar.DAY_OF_MONTH),
                startDate.get(Calendar.HOUR_OF_DAY),
                startDate.get(Calendar.MINUTE),
                startDate.get(Calendar.SECOND),
                startDate.get(Calendar.MILLISECOND),
                0
            );
        final XMLGregorianCalendar xmlEnd = 
            datatypeFactory.newXMLGregorianCalendar(
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH) + 1,
                endDate.get(Calendar.DAY_OF_MONTH),
                endDate.get(Calendar.HOUR_OF_DAY),
                endDate.get(Calendar.MINUTE),
                endDate.get(Calendar.SECOND),
                endDate.get(Calendar.MILLISECOND),
                0
            );
        System.out.println("xmlStart/End = " + xmlStart + ", " + 
            xmlEnd);

        System.out.println("start/end date = " + startDate.getTime() + 
                           ", " + endDate.getTime());

        String[] sats = new String[] {"polar"};


        t1 = new Date();

        // ---------- SSC Web Services code ----------
        SatelliteSpecification satSpec = new SatelliteSpecification();

        satSpec.setId("moon");
        satSpec.setResolutionFactor(2);

        GraphRequest graphRequest = new GraphRequest();
        graphRequest.getSatellites().add(satSpec);
        graphRequest.setBeginTime(xmlStart);
        graphRequest.setEndTime(xmlEnd);
        graphRequest.setOrbitOptions(getTestOrbitOptions());

        FileResult graphResult = ssc.getGraphs(graphRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        if (graphResult.getStatusCode() == ResultStatusCode.ERROR) {

            printResult("getGraphs", graphResult);

            return;
        }
        System.out.println("\nOrbit getGraphs results:");
        printFileResult("getGraphs", graphResult);

        long etGetGraph = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getGraphs = " +
                           (double)etGetGraph / 1000.0 + 
                           "seconds");

        t1 = new Date();

        // ---------- SSC Web Services code ----------

        GraphRequest mapRequest = new GraphRequest();
        mapRequest.getSatellites().add(satSpec);
        mapRequest.setBeginTime(xmlStart);
        mapRequest.setEndTime(xmlEnd);
        mapRequest.setMapProjectionOptions(getTestMapOptions());

        FileResult mapResult = ssc.getGraphs(mapRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        if (mapResult.getStatusCode() == ResultStatusCode.ERROR) {

            printResult("getGraphs", mapResult);

            return;
        }
        System.out.println("\nMap getGraphs results:");
        printFileResult("getGraphs", mapResult);

        etGetGraph = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getGraphs = " +
                           (double)etGetGraph / 1000.0 + 
                           "seconds");


        SatelliteSpecification aceSpec = new SatelliteSpecification();
        aceSpec.setId("ace");
        aceSpec.setResolutionFactor(1);
        final XMLGregorianCalendar aceStart = 
            datatypeFactory.newXMLGregorianCalendar(
                2000, 7, 18, 0, 0, 0, 0, 0);
        final XMLGregorianCalendar aceEnd = 
            datatypeFactory.newXMLGregorianCalendar(
                2000, 7, 20, 0, 0, 0, 0, 0);

        t1 = new Date();

        // ---------- SSC Web Services code ----------

        GraphRequest timeSeriesRequest = new GraphRequest();
        timeSeriesRequest.getSatellites().add(aceSpec);
        timeSeriesRequest.setBeginTime(aceStart);
        timeSeriesRequest.setEndTime(aceEnd);
        timeSeriesRequest.setTimeSeriesOptions(
                                 getTestTimeSeriesOptions());

        FileResult timeSeriesResult = ssc.getGraphs(timeSeriesRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        if (timeSeriesResult.getStatusCode() == 
            ResultStatusCode.ERROR) {

            printResult("getGraphs", timeSeriesResult);

            return;
        }
        System.out.println("\nTime Series getGraphs results:");
        printFileResult("getGraphs", timeSeriesResult);

        etGetGraph = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getGraphs = " +
                           (double)etGetGraph / 1000.0 + 
                           "seconds");


        t1 = new Date();

        // ---------- SSC Web Services code ----------
        DataResult dataResult = 
            ssc.getData(getTestDataRequest(xmlStart, xmlEnd));
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        if (dataResult.getStatusCode() == ResultStatusCode.ERROR) {

            printResult("getData", dataResult);

            return;
        }
        System.out.println("\ngetData results:");
        printDataResult(dataResult);

        long etGetData = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getData = " +
                           (double)etGetData / 1000.0 + 
                           "seconds");


        DataFileRequest testDataFileRequest =
            getTestDataRequest(xmlStart, xmlEnd);

        t1 = new Date();

        // ---------- SSC Web Services code ----------
        FileResult dataFileResult = 
            ssc.getDataFiles(testDataFileRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        if (dataFileResult.getStatusCode() == ResultStatusCode.ERROR) {

            printResult("getDataFiles", dataFileResult);

            return;
        }
        printFileResult("getDataFiles", dataFileResult);

        long etGetDataFiles = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getDataFiles = " +
                           (double)etGetDataFiles / 1000.0 + 
                           "seconds");

        FormatOptions testFormatOptions =
            testDataFileRequest.getFormatOptions();
        testFormatOptions.setCdf(true);
        testDataFileRequest.setFormatOptions(testFormatOptions);

        t1 = new Date();

        // ---------- SSC Web Services code ----------
        dataFileResult = ssc.getDataFiles(testDataFileRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        printFileResult("getDataFiles", dataFileResult);

        etGetDataFiles = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getDataFiles 2 = " +
                           (double)etGetDataFiles / 1000.0 + 
                           "seconds");

KmlRequest kmlRequest = new KmlRequest();
kmlRequest.getSatellites().addAll(testDataFileRequest.getSatellites());
kmlRequest.setBeginTime(testDataFileRequest.getBeginTime());
kmlRequest.setEndTime(testDataFileRequest.getEndTime());
kmlRequest.setBFieldModelOptions(testDataFileRequest.getBFieldModelOptions());
kmlRequest.setTrajectory(true);
kmlRequest.setNorthBFieldTraceFootpoint(true);
kmlRequest.setSouthBFieldTraceFootpoint(true);
        t1 = new Date();

        // ---------- SSC Web Services code ----------
        dataFileResult = ssc.getKmlFiles(kmlRequest);
        // ---------- end SSC Web Services code ----------

        t2 = new Date();

        printFileResult("getKmlFiles", dataFileResult);

        long etGetKmlFiles = t2.getTime() - t1.getTime();

        System.out.println(
            "\nTime to execute getKmlFiles = " +
                           (double)etGetKmlFiles / 1000.0 + 
                           "seconds");

    }


    private static void printResult(String title, Result result) {

        System.out.println("\n" + title + " results:");
        System.out.println("result.getStatusCode() = " +
                            result.getStatusCode());
        System.out.println("result.getStatusSubCode() = " +
                            result.getStatusSubCode());

        printStringArray(title + " status text", 
                         result.getStatusText());
    }


    private static void printFileResult(String title,
                                        FileResult result) {

        printResult(title, result);

        printStringArray(title + " URLs", result.getUrls());
    }


    private static void printStringArray(String title,
                                         List<String> strings) {

        if (strings == null || strings.size() == 0) {

            System.out.println("\n" + title + " is empty");
        }
        else {

            System.out.println("\n" + title + " returned:");

            for (String string : strings) {

                System.out.println("  " + string);
            }
        }
    }


    private static void printDataResult(DataResult result) {

        printResult("Data", result);

        List<SatelliteData> data = result.getData();

        if (data == null || data.size() == 0) {

            System.out.println("No satellite data");
            return;
        }
        else {

            System.out.println("data.length = " + data.size());
            System.out.println("Satellite Data:");
        }

        int numValues = 5;

        for (SatelliteData datum : data) {

            print(datum);
        }
    }


    private static void print(SatelliteData data) {

        System.out.println("  " + data.getId());

        List<XMLGregorianCalendar> time = data.getTime();
        List<CoordinateData> coords = data.getCoordinates();
        List<Double> radialLength = data.getRadialLength();
        List<Double> magneticStrength = data.getMagneticStrength();
        List<Double> ns = data.getNeutralSheetDistance();
        List<Double> bs = data.getBowShockDistance();
        List<Double> mp = data.getMagnetoPauseDistance();
        List<Double> lv = data.getDipoleLValue();
        List<Float> il = data.getDipoleInvariantLatitude();
        List<SpaceRegion> sr = data.getSpacecraftRegion();
        List<FootpointRegion> rtr = 
            data.getRadialTraceFootpointRegion();
        List<FootpointRegion> nbtr = data.getNorthBTracedRegions();
        List<FootpointRegion> sbtr = data.getSouthBTracedRegions();
        List<Double> bGseX = data.getBGseX();
        List<Double> bGseY = data.getBGseY();
        List<Double> bGseZ = data.getBGseZ();

        printHeading(coords, radialLength, magneticStrength, ns, bs, mp,
                     lv, il, sr, rtr, nbtr, sbtr, bGseX, bGseY, bGseZ);

        print(time, coords, radialLength, magneticStrength, ns, bs, mp,
              lv, il, sr, rtr, nbtr, sbtr, bGseX, bGseY, bGseZ,
              5);

    }


    private static void printHeading(List<CoordinateData> coords,
        List<Double> radialLength, List<Double> magneticStrength, 
        List<Double> ns, List<Double> bs, List<Double> mp, 
        List<Double> lv, List<Float> il, List<SpaceRegion> sr,
        List<FootpointRegion> rtr, List<FootpointRegion> nbtr, 
        List<FootpointRegion> sbtr, 
        List<Double> bGseX, List<Double> bGseY, List<Double> bGseZ) {

        StringBuffer hdr1 = new StringBuffer();
        StringBuffer hdr2 = new StringBuffer();

        hdr1.append("  Time                 ");
        hdr2.append("                       ");

        for (CoordinateData point : coords) {

            List<Double> x = point.getX();
            List<Double> y = point.getY();
            List<Double> z = point.getZ();
            List<Float> lat = point.getLatitude();
            List<Float> lon = point.getLongitude();
            List<Double> lt = point.getLocalTime();

            if (x != null && x.size() > 0) {

                hdr1.append("         ");
                hdr2.append("      X  ");
            }

            if (y != null && y.size() > 0) {

                hdr1.append("            ");
                hdr2.append("         Y  ");
            }

            hdr1.append(point.getCoordinateSystem() + "         ");

            if (z != null && z.size() > 0) {

                hdr2.append("         Z  ");
            }

            if (lat != null && lat.size() > 0) {

                hdr1.append("           ");
                hdr2.append("      Lat  ");
            }

            if (lon != null && lon.size() > 0) {

                hdr1.append("           ");
                hdr2.append("      Lon  ");
            }

            if (lt != null && lt.size() > 0) {

                hdr1.append("        Local  ");
                hdr2.append("         Time  ");
            }
        } // endfor each coords

        if (radialLength != null && radialLength.size() > 0) {

            hdr1.append("    Radial ");
            hdr2.append("    Length ");
        }

        if (magneticStrength != null && magneticStrength.size() > 0) {

            hdr1.append("    Magnetic ");
            hdr2.append("    Strength ");
        }

        if (ns != null && ns.size() > 0) {

            hdr1.append("    Neutral ");
            hdr2.append("    Sheet   ");
        }

        if (bs != null && bs.size() > 0) {

            hdr1.append("    Bow     ");
            hdr2.append("    Shock   ");
        }
        
        if (mp != null && mp.size() > 0) {

            hdr1.append("    Magneto ");
            hdr2.append("    Pause   ");
        }

        if (lv != null && lv.size() > 0) {

            hdr1.append("    Dipole  ");
            hdr2.append("    L Value ");
        }

        if (il != null && il.size() > 0) {

            hdr1.append("    Dipole  ");
            hdr2.append("    InvLat  ");
        }

        if (sr != null && sr.size() > 0) {

            hdr1.append("  Spacecraft ");
            hdr2.append("  Region     ");
        }

        if (rtr != null && rtr.size() > 0) {

            hdr1.append("  Radial Trc ");
            hdr2.append("  Region     ");
        }

        if (nbtr != null && nbtr.size() > 0) {

            hdr1.append("  N BTraced ");
            hdr2.append("  Region    ");
        }

        if (sbtr != null && sbtr.size() > 0) {

            hdr1.append("  S BTraced ");
            hdr2.append("  Region    ");
        }

        if (bGseX != null && bGseX.size() > 0) {

            hdr1.append("    GSE    ");
            hdr2.append("    X      ");
        }

        if (bGseY != null && bGseY.size() > 0) {

            hdr1.append("  Magnetic ");
            hdr2.append("    Y      ");
        }

        if (bGseZ != null && bGseZ.size() > 0) {

            hdr1.append("  Vectors ");
            hdr2.append("    Z     ");
        }


        System.out.println(hdr1);
        System.out.println(hdr2);
    }


    private static void print(List<XMLGregorianCalendar> time, 
        List<CoordinateData> coords, List<Double> radialLength, 
        List<Double> magneticStrength, List<Double> ns,
        List<Double> bs, List<Double> mp, List<Double> lv, 
        List<Float> il, List<SpaceRegion> sr, 
        List<FootpointRegion> rtr, List<FootpointRegion> nbtr, 
        List<FootpointRegion> sbtr, List<Double> bGseX, 
        List<Double> bGseY, List<Double> bGseZ, int numValues) {

        if (numValues < 0) {

            numValues = time.size();
        }

        for (int i = 0; i < time.size() && i < numValues; i++) {

            System.out.println("  " + time.get(i));

            for (int j = 0; j < coords.size(); j++) {

                List<Double> x = coords.get(j).getX();
                List<Double> y = coords.get(j).getY();
                List<Double> z = coords.get(j).getZ();
                List<Float> lat = coords.get(j).getLatitude();
                List<Float> lon = coords.get(j).getLongitude();
                List<Double> lt = coords.get(j).getLocalTime();

                if (x != null && i < x.size()) {

                    System.out.printf("  %10.2f", x.get(i));
                }

                if (y != null && i < y.size()) {

                    System.out.printf("  %10.2f", y.get(i));
                }

                if (z != null && i < z.size()) {

                    System.out.printf("  %10.2f", z.get(i));
                }

                if (lat != null && i < lat.size()) {

                    System.out.printf("  %10.2f", lat.get(i));
                }

                if (lon != null && i < lon.size()) {

                    System.out.printf("  %10.2f", lon.get(i));
                }

                if (lt != null && i < lt.size()) {

                    System.out.printf("  %10.2f", lt.get(i));
                }
            } // endfor each coords

            if (radialLength != null && i < radialLength.size()) {

                System.out.printf("  %10.2f" , radialLength.get(i));
            }

            if (magneticStrength != null && 
                i < magneticStrength.size()) {

                if (magneticStrength.get(i) == -1.0E31D) {

                    System.out.print("      NA    ");
                }
                else {

                    System.out.printf("  %10.2f" , 
                        magneticStrength.get(i));
                }
            }

            if (ns != null && i < ns.size()) {

                if (ns.get(i) == -1.0E31D) {

                    System.out.print("      NA    ");
                }
                else {

                    System.out.printf("  %10.2f" , ns.get(i));
                }
            }

            if (bs != null && i < bs.size()) {

                System.out.printf("  %10.2f" , bs.get(i));
            }
        
            if (mp != null && i < mp.size()) {

                System.out.printf("  %10.2f" , mp.get(i));
            }

            if (lv != null && i < lv.size()) {

                System.out.printf("  %10.2f" , lv.get(i));
            }

            if (il != null && i < il.size()) {

                System.out.printf("  %10.2f" , il.get(i));
            }

            if (sr != null && i < sr.size()) {

                System.out.printf("  %10s", getSpaceRegion(sr.get(i)));
            }

            if (rtr != null && i < rtr.size()) {

                System.out.printf("  %10s" , 
                    getTracedRegion(rtr.get(i)));
            }

            if (nbtr != null && i < nbtr.size()) {

                System.out.printf("  %10s" , 
                    getTracedRegion(nbtr.get(i)));
            }

            if (sbtr != null && i < sbtr.size()) {

                System.out.printf("  %10s" , 
                    getTracedRegion(sbtr.get(i)));
            }

            if (bGseX != null && i < bGseX.size()) {

                if (bGseX.get(i) == -1.0E31D) {

                    System.out.print("      NA    ");
                }
                else {

                    System.out.printf("  %10.2f" , bGseX.get(i));
                }
            }

            if (bGseY != null && i < bGseY.size()) {

                if (bGseY.get(i) == -1.0E31D) {

                    System.out.print("      NA    ");
                }
                else {

                    System.out.printf("  %10.2f" , bGseY.get(i));
                }
            }

            if (bGseZ != null && i < bGseZ.size()) {

                if (bGseZ.get(i) == -1.0E31D) {

                    System.out.print("      NA    ");
                }
                else {

                    System.out.printf("  %10.2f" , bGseZ.get(i));
                }
            }

            System.out.println();
        } // endfor each time value
    }


    private static String getSpaceRegion(SpaceRegion value) {

        switch (value) {

        case INTERPLANETARY_MEDIUM:

            return "Intpl Med";

        case DAYSIDE_MAGNETOSHEATH:

            return "D Msheath";

        case NIGHTSIDE_MAGNETOSHEATH:

            return "N Msheath";

        case DAYSIDE_MAGNETOSPHERE:

            return "D Msphere";

        case NIGHTSIDE_MAGNETOSPHERE:

            return "N Msphere";

        case PLASMA_SHEET:

            return "Plasma Sh";

        case TAIL_LOBE:

            return "Tail Lobe";

        case HIGH_LATITUDE_BOUNDARY_LAYER:

            return "HLB Layer";

        case LOW_LATITUDE_BOUNDARY_LAYER:

            return "LLB Layer";

        case DAYSIDE_PLASMASPHERE:

            return "D Psphere";

        case NIGHTSIDE_PLASMASPHERE:

            return "N Psphere";

        default:

            return value.toString();
        }
    }


    private static String getTracedRegion(FootpointRegion value) {

        switch (value) {

        case NORTH_CUSP:

            return "N Cusp   ";

        case SOUTH_CUSP:

            return "S Cusp   ";

        case NORTH_CLEFT:

            return "N Cleft  ";

        case SOUTH_CLEFT:

            return "S Cleft  ";

        case NORTH_AURORAL_OVAL:

            return "N Oval   ";

        case SOUTH_AURORAL_OVAL:

            return "S Oval   ";

        case NORTH_POLAR_CAP:

            return "N PolrCap";

        case SOUTH_POLAR_CAP:

            return "S PolrCap";

        case NORTH_MID_LATITUDE:

            return "N Mid-Lat";

        case SOUTH_MID_LATITUDE:

            return "S Mid-Lat";

        case LOW_LATITUDE:

            return "Low Lat  ";

        default:

            return "  None   ";
        }
    }



    /**
     * Provides a test DataRequest.
     *
     * @return a test DataRequest
     */
    private static DataFileRequest getTestDataRequest(
        XMLGregorianCalendar startDate,
        XMLGregorianCalendar endDate) {

        SatelliteSpecification fastSat = new SatelliteSpecification();
                                       // fast satellite spec.
        fastSat.setId("fast");
        fastSat.setResolutionFactor(2);
        SatelliteSpecification moonSat = new SatelliteSpecification();
                                       // moon satellite spec.
        moonSat.setId("moon");
        moonSat.setResolutionFactor(2);

        SpaceRegionsFilterOptions spaceRegionsFilter =
            new SpaceRegionsFilterOptions();
                                       // space regions filter options
        spaceRegionsFilter.setDaysideMagnetosheath(true);
        spaceRegionsFilter.setDaysideMagnetosphere(true);
        spaceRegionsFilter.setDaysidePlasmasphere(true);
        spaceRegionsFilter.setHighLatitudeBoundaryLayer(true);
        spaceRegionsFilter.setInterplanetaryMedium(true);
        spaceRegionsFilter.setLowLatitudeBoundaryLayer(true);
        spaceRegionsFilter.setNightsideMagnetosheath(true);
        spaceRegionsFilter.setNightsideMagnetosphere(true);
        spaceRegionsFilter.setNightsidePlasmasphere(true);
        spaceRegionsFilter.setPlasmaSheet(true);
        spaceRegionsFilter.setTailLobe(true);

        HemisphereOptions hemisphereOptions =
            new HemisphereOptions();
                                       // hemisphere listing options
                                       //  requesting both the north and
                                       //  south hemisphere
        hemisphereOptions.setNorth(true);
        hemisphereOptions.setSouth(true);

        MappedRegionFilterOptions radialTraceRegionsFilter =
            new MappedRegionFilterOptions();
                                       // radial traced regions filter 
        radialTraceRegionsFilter.setCusp(hemisphereOptions);
        radialTraceRegionsFilter.setCleft(hemisphereOptions);
        radialTraceRegionsFilter.setAuroralOval(hemisphereOptions);
        radialTraceRegionsFilter.setPolarCap(hemisphereOptions);
        radialTraceRegionsFilter.setMidLatitude(hemisphereOptions);
        radialTraceRegionsFilter.setLowLatitude(true);

        MappedRegionFilterOptions magneticTraceRegionsFilter =
            radialTraceRegionsFilter;
                                       // magnetic traced regions filter

        RegionFilterOptions regionFilters = 
            new RegionFilterOptions();
                                       // region filter
        regionFilters.setSpaceRegions(spaceRegionsFilter);
        regionFilters.setRadialTraceRegions(radialTraceRegionsFilter);
        regionFilters.setMagneticTraceRegions(
            magneticTraceRegionsFilter);

        LocationFilter locationFilter = new LocationFilter();
                                       // a location filter
        locationFilter.setMinimum(true);
        locationFilter.setMaximum(true);
        locationFilter.setLowerLimit(-500.0);
        locationFilter.setUpperLimit(500.0);

        LocationFilterOptions locationFilterOptions = 
            new LocationFilterOptions();
                                       // location filter options
        locationFilterOptions.setAllFilters(true);
        locationFilterOptions.setDistanceFromCenterOfEarth(
            locationFilter);
        locationFilterOptions.setMagneticFieldStrength(locationFilter);
        locationFilterOptions.setDistanceFromNeutralSheet(
            locationFilter);
        locationFilterOptions.setDistanceFromBowShock(locationFilter);
        locationFilterOptions.setDistanceFromMagnetopause(
            locationFilter);
        locationFilterOptions.setDipoleLValue(locationFilter);
        locationFilterOptions.setDipoleInvariantLatitude(
            locationFilter);

        BFieldModelParameters externalBFieldModel =
            new BFieldModelParameters();
                                       // external B field model to use
        externalBFieldModel.setModel(ExternalBFieldModel.T_96);
        externalBFieldModel.setUseFixedValues(true);
        externalBFieldModel.setSolarWindPressure(2.1f);
        externalBFieldModel.setDst(-20);
        externalBFieldModel.setByImf(0.0f);
        externalBFieldModel.setBzImf(0.0f);

        BFieldModelOptions bFieldModelOptions = 
            new BFieldModelOptions();
                                       // B field model options
                                       // format options
        bFieldModelOptions.setInternalModel(InternalBFieldModel.IGRF);
        bFieldModelOptions.setExternalModel(externalBFieldModel);
        bFieldModelOptions.setFieldLinesStopAltitude(200.0);

        DataFileRequest dataRequest = new DataFileRequest();
                                        // test data request

        dataRequest.getSatellites().add(fastSat);
        dataRequest.getSatellites().add(moonSat);
        dataRequest.setOutputOptions(getTestOutputOptions());
        dataRequest.setBeginTime(startDate);
        dataRequest.setEndTime(endDate);

//        dataRequest.setRegionFilterOptions(regionFilters);
//        dataRequest.setLocationFilterOptions(locationFilterOptions);
//        dataRequest.setBFieldModelOptions(bFieldModelOptions);

        dataRequest.setFormatOptions(getTestFormatOptions());

        return dataRequest;
    }


    private static OutputOptions getTestOutputOptions() {

        LocationFilter locationFilter = new LocationFilter();
                                       // a test LocationFilter to use 
                                       //  with all 
        locationFilter.setMinimum(true);
        locationFilter.setMaximum(true);
        locationFilter.setLowerLimit(-100.0);
        locationFilter.setUpperLimit(100.0);

        List<FilteredCoordinateOptions> options = 
            new ArrayList<FilteredCoordinateOptions>();
                                       // some test coordinate options

        for (CoordinateComponent component : 
             CoordinateComponent.values()) {

            FilteredCoordinateOptions option = 
                new FilteredCoordinateOptions();

            option.setCoordinateSystem(CoordinateSystem.GSE);
            option.setComponent(component);
            option.setFilter(null);
//            option.setFilter(locationFilter);

            options.add(option);
        }

        RegionOptions regionOptions = new RegionOptions();
                                       // region listing options
        regionOptions.setSpacecraft(true);
        regionOptions.setRadialTracedFootpoint(true);
        regionOptions.setNorthBTracedFootpoint(true);
        regionOptions.setSouthBTracedFootpoint(false);

        ValueOptions valueOptions = new ValueOptions();
                                       // value listing options
        valueOptions.setBFieldStrength(true);
        valueOptions.setDipoleInvLat(true);
        valueOptions.setDipoleLValue(true);
        valueOptions.setRadialDistance(true);

        DistanceFromOptions distanceFromOptions =
            new DistanceFromOptions();
                                       // distance from options
        distanceFromOptions.setBGseXYZ(true);
        distanceFromOptions.setBowShock(true);
        distanceFromOptions.setMPause(true);
        distanceFromOptions.setNeutralSheet(true);

        BFieldTraceOptions geoNorthBFieldTrace =
            new BFieldTraceOptions();
                                       // GEO north B field trace 
                                       // options
        geoNorthBFieldTrace.setCoordinateSystem(CoordinateSystem.GEO);
        geoNorthBFieldTrace.setFieldLineLength(true);
        geoNorthBFieldTrace.setFootpointLatitude(true);
        geoNorthBFieldTrace.setFootpointLongitude(true);
        geoNorthBFieldTrace.setHemisphere(Hemisphere.NORTH);

        BFieldTraceOptions geoSouthBFieldTrace =
            new BFieldTraceOptions();
                                       // GEO south B field trace 
                                       // options
        geoSouthBFieldTrace.setCoordinateSystem(CoordinateSystem.GEO);
        geoSouthBFieldTrace.setFieldLineLength(true);
        geoSouthBFieldTrace.setFootpointLatitude(true);
        geoSouthBFieldTrace.setFootpointLongitude(true);
        geoSouthBFieldTrace.setHemisphere(Hemisphere.SOUTH);

        BFieldTraceOptions gmNorthBFieldTrace =
            new BFieldTraceOptions();
                                       // GM north B field trace options
        gmNorthBFieldTrace.setCoordinateSystem(CoordinateSystem.GM);
        gmNorthBFieldTrace.setFieldLineLength(true);
        gmNorthBFieldTrace.setFootpointLatitude(true);
        gmNorthBFieldTrace.setFootpointLongitude(true);
        gmNorthBFieldTrace.setHemisphere(Hemisphere.NORTH);

        BFieldTraceOptions gmSouthBFieldTrace =
            new BFieldTraceOptions();
                                       // GM south B field trace options
        gmSouthBFieldTrace.setCoordinateSystem(CoordinateSystem.GM);
        gmSouthBFieldTrace.setFieldLineLength(true);
        gmSouthBFieldTrace.setFootpointLatitude(true);
        gmSouthBFieldTrace.setFootpointLongitude(true);
        gmSouthBFieldTrace.setHemisphere(Hemisphere.SOUTH);

        OutputOptions outputOptions = new OutputOptions();

        outputOptions.setAllLocationFilters(true);

        outputOptions.getCoordinateOptions().addAll(options);
        outputOptions.setRegionOptions(regionOptions);
        outputOptions.setValueOptions(valueOptions);
        outputOptions.setDistanceFromOptions(distanceFromOptions);
        outputOptions.setMinMaxPoints(2);
        outputOptions.getBFieldTraceOptions().add(geoNorthBFieldTrace);
        outputOptions.getBFieldTraceOptions().add(geoSouthBFieldTrace);
        outputOptions.getBFieldTraceOptions().add(gmNorthBFieldTrace);
        outputOptions.getBFieldTraceOptions().add(gmSouthBFieldTrace);

        return outputOptions;
    }


    private static FormatOptions getTestFormatOptions() {

        FormatOptions formatOptions = new FormatOptions();

        formatOptions.setDateFormat(DateFormat.YYYY_DDD);
        formatOptions.setTimeFormat(TimeFormat.HH_MM);
        formatOptions.setDistanceUnits(DistanceUnits.RE);
        formatOptions.setDistancePrecision((short)2);
        formatOptions.setDegreeFormat(DegreeFormat.DECIMAL);
        formatOptions.setDegreePrecision((short)2);
        formatOptions.setLatLonFormat(LatLonFormat.LAT_90_LON_360);
        formatOptions.setCdf(false);
        formatOptions.setLinesPerPage((short)1);

        return formatOptions;
    }


    private static OrbitGraphOptions getTestOrbitOptions() {

        OrbitGraphOptions options = new OrbitGraphOptions();

        options.setCoordinateSystem(CoordinateSystem.GSE);
//        options.setCoordinateSystem(CoordinateSystem.GEI_J_2000);
        options.setCombined(true);
        options.setXyView(true);
        options.setXzView(true);
        options.setYzView(true);
        options.setXrView(true);
        options.setShowBowShockMagnetopause(true);
        options.setSolarWindPressure(2.1f);

        return options;
    }


    private static MapProjectionGraphOptions getTestMapOptions() {

        MapProjectionGraphOptions options = 
            new MapProjectionGraphOptions();

        options.setTrace(Trace.RADIAL);
        options.setCoordinateSystem(ProjectionCoordinateSystem.GEO);
        options.setShowContinents(true);
        options.setProjection(MapProjection.CYLINDRICAL);

        List<String> stations = new ArrayList<String>();
        stations.add("HIS");

        options.getGroundStations().addAll(stations);
        options.setPolarMapOrientation(PolarMapOrientation.EQUATORIAL);

        return options;
    }


    private static TimeSeriesGraphOptions getTestTimeSeriesOptions() {

        TimeSeriesGraphOptions options = 
            new TimeSeriesGraphOptions();

        List<CoordinateOptions> coordOptions =
            new ArrayList<CoordinateOptions>();

        CoordinateOptions todX = new CoordinateOptions();
        todX.setCoordinateSystem(CoordinateSystem.GEI_TOD);
        todX.setComponent(CoordinateComponent.X);
        coordOptions.add(todX);

        CoordinateOptions todY = new CoordinateOptions();
        todY.setCoordinateSystem(CoordinateSystem.GEI_TOD);
        todY.setComponent(CoordinateComponent.Y);
        coordOptions.add(todY);

        CoordinateOptions todZ = new CoordinateOptions();
        todZ.setCoordinateSystem(CoordinateSystem.GEI_TOD);
        todZ.setComponent(CoordinateComponent.Z);
        coordOptions.add(todZ);

        options.getCoordinateOptions().addAll(coordOptions);

        ValueOptions valueOptions = new ValueOptions();
        valueOptions.setRadialDistance(true);
        valueOptions.setBFieldStrength(true);
        valueOptions.setDipoleLValue(true);
        valueOptions.setDipoleInvLat(true);

        options.setValueOptions(valueOptions);

        DistanceFromOptions distanceOptions = new DistanceFromOptions();
        distanceOptions.setNeutralSheet(true);
        distanceOptions.setBowShock(true);
        distanceOptions.setMPause(true);
        distanceOptions.setBGseXYZ(true);

        options.setDistanceFromOptions(distanceOptions);

        List<BFieldTraceOptions> traceOptions =
            new ArrayList<BFieldTraceOptions>();

        BFieldTraceOptions geoNBTraceOptions = new BFieldTraceOptions();
        geoNBTraceOptions.setCoordinateSystem(CoordinateSystem.GEO);
        geoNBTraceOptions.setHemisphere(Hemisphere.NORTH);
        geoNBTraceOptions.setFootpointLatitude(true);
        geoNBTraceOptions.setFootpointLongitude(true);
        geoNBTraceOptions.setFieldLineLength(true);
        traceOptions.add(geoNBTraceOptions);

        BFieldTraceOptions geoSBTraceOptions = new BFieldTraceOptions();
        geoSBTraceOptions.setCoordinateSystem(CoordinateSystem.GEO);
        geoSBTraceOptions.setHemisphere(Hemisphere.SOUTH);
        geoSBTraceOptions.setFootpointLatitude(true);
        geoSBTraceOptions.setFootpointLongitude(true);
        geoSBTraceOptions.setFieldLineLength(true);
        traceOptions.add(geoSBTraceOptions);

        BFieldTraceOptions gmNBTraceOptions = new BFieldTraceOptions();
        gmNBTraceOptions.setCoordinateSystem(CoordinateSystem.GM);
        gmNBTraceOptions.setHemisphere(Hemisphere.NORTH);
        gmNBTraceOptions.setFootpointLatitude(true);
        gmNBTraceOptions.setFootpointLongitude(true);
        gmNBTraceOptions.setFieldLineLength(true);
        traceOptions.add(gmNBTraceOptions);

        BFieldTraceOptions gmSBTraceOptions = new BFieldTraceOptions();
        gmSBTraceOptions.setCoordinateSystem(CoordinateSystem.GM);
        gmSBTraceOptions.setHemisphere(Hemisphere.SOUTH);
        gmSBTraceOptions.setFootpointLatitude(true);
        gmSBTraceOptions.setFootpointLongitude(true);
        gmSBTraceOptions.setFieldLineLength(true);
        traceOptions.add(gmSBTraceOptions);

        options.getBFieldTraceOptions().addAll(traceOptions);

        return options;
    }

}


