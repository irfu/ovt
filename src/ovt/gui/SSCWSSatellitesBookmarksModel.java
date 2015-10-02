/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/gui/SSCWSSatellitesBookmarksModel.java $
 Date:      $Date: 2015/09/15 13:17:00 $
 Version:   $Revision: 1.0 $
 
 
 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P G Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson)
 
 =========================================================================*/
package ovt.gui;

import java.util.HashSet;
import java.util.Set;

/**
 * Set of SSCWS satellites that are "bookmarked", i.e. shortlisted to also
 * appear on the Satellites menu (or conceivably similar uses in the future).
 *
 * IMPLEMENTATION NOTE: Stores satellite IDs instead of
 * SSCWSLibrary.SSCWSSatelliteInfo objects since:<BR>
 * (1) the class probably should have proper implementations of equals, hashCode
 * etc which the latter does not have,<BR>
 * (2) we want to be able to easily store all the satellite "identities" as one
 * string in the global settings (properties; config file),<BR>
 * (3) we want to be able to store satellites which are NOT present in the
 * currently available SSCWS satellite list (SSCWSLibrary#getAllSatelliteInfo).
 * This is useful for (a) during networks failure (no list available at launch)
 * and (b) when switching between a real SSCWSLibrary and a test emulator one.
 * If the code removed invalid satellite IDs, the bookmarks would be permanently
 * removed in those cases (or at least the latter).
 *
 * NOTE: Ideally, this class should be the model in the MVC pattern and have
 * "listeners" which get updates about changes, but that has not been needed so
 * far since the only class that modifies it (SSCWSSatellitesSelectionWindow) is
 * the only one that needs to be immediately informed of changes.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
public class SSCWSSatellitesBookmarksModel {

    private final Set<String> bookmarkedSatIds = new HashSet();


    public Set<String> getBookmarkedSSCWSSatIds() {
        return new HashSet(bookmarkedSatIds);   // Return copy (instead of read-only view), in case the information changes.
    }


    public void setBookmark(String satId, boolean addBookmark) {
        if (addBookmark) {
            bookmarkedSatIds.add(satId);
        } else {
            bookmarkedSatIds.remove(satId);
        }
    }


    public boolean isBookmark(String satId) {
        return bookmarkedSatIds.contains(satId);
    }


    /**
     * Sets contents from string (multiple satellites).
     *
     * IMPLEMENTATION NOTE: Handles null value since OVTCore.getGlobalSetting
     * may returs null if it does not find a property.
     *
     * @param value String as it is stored in global settings, representing the
     * contents of the class. null means empty.
     */
    // PROPOSAL: Better name.
    public void loadFromGlobalSettingsValue(String value) {
        //final String s = OVTCore.getGlobalSetting(SETTINGS_BOOKMARKED_SSCWS_SATELLITE_IDS);
        bookmarkedSatIds.clear();
        if (value == null) {
            return;  // Property was not found.
        }

        // First tried using StringTokenizer but that (the Java library class)
        // seemed to have some form of bug the produced a never-ending loop.
        final String[] satIDs = value.split(";");
        for (String satId : satIDs) {
            // Ignore empty string since empirically, this value has been found
            // in the config string (due to earlier bugs?).
            // Should in principle not be needed but can avoid unnecessary log
            // messages about satellite not found.
            if (!satId.isEmpty()) {
                bookmarkedSatIds.add(satId);
            }
        }
    }


    /**
     * @returns Satellite IDs as a list. Always returns a string, also when
     * empty (needed for setting global settings).
     */
    public String getGlobalSettingsValue() {
        String list = "";
        boolean first = true;
        for (String satID : bookmarkedSatIds) {
            if (first) {
                list = satID;
                first = false;
            } else {
                list = list + ";" + satID;
            }
        }
        return list;
        //OVTCore.setGlobalSetting(SETTINGS_BOOKMARKED_SSCWS_SATELLITE_IDS, list);
    }

}
