/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/SSCWSLibrary.java $
 Date:      $Date: 2015/09/15 11:54: $
 Version:   $Revision: 1.0 $


 Copyright (c) 2000-2022 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P. G. Johansson)
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
 Khotyaintsev, E. P. G. Johansson

 =========================================================================*/
package ovt.util;

import gov.nasa.gsfc.spdf.ssc.client.SatelliteSituationCenterService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import ovt.Const;

/**
 * Class for manually (not automatically) testing (MTest) or playing around with
 * SSCWSLibraryImpl and SSC Web Services features. This class is therefore not
 * called by OVT proper.
 *
 * @author Erik P G Johansson
 */
public class SSCWSLibraryImplMTest {

    public static void main(String[] args) throws MalformedURLException {
        test_sscServiceCall();
    }


    private static void test_sscServiceCall() throws MalformedURLException {
        /*
        public static final String WSDL_URL_STRING
            = "http://sscWeb.gsfc.nasa.gov/WS/ssc/2/SatelliteSituationCenterService?wsdl";
        public static final String QNAME_NAMESPACE_URI = "http://ssc.spdf.gsfc.nasa.gov/";
        public static final String QNAME_LOCAL_PART = "SatelliteSituationCenterService";
        */
        final SatelliteSituationCenterService sscService = new SatelliteSituationCenterService(
            new URL("https://sscWeb.gsfc.nasa.gov/WS/ssc/2/SatelliteSituationCenterService?wsdl"),
            new QName(
                "http://ssc.spdf.gsfc.nasa.gov/",
                "SatelliteSituationCenterService"
            )
        );
        System.out.println("ASD");
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
