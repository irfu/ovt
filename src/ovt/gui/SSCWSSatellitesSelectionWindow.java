/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/gui/SSCWSSatellitesSelectionWindow.java $
 Date:      $Date: 2015/09/15 13:17:00 $
 Version:   $Revision: 1.0 $
 
 
 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P. G Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
 =========================================================================*/
package ovt.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ovt.Const;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.event.ChildrenEvent;
import ovt.interfaces.ChildrenListener;
import ovt.object.SSCWSSat;
import ovt.util.SSCWSLibrary;
import ovt.util.SSCWSLibrary.NoSuchSatelliteException;
import ovt.util.SSCWSLibraryTestEmulator;
import ovt.util.Utils;

/**
 * GUI window for listing and selecting satellite offered online using NASA's
 * Satellite Situation Center's (SSC) Web Services.
 *
 * NOTE: The class stores "bookmarked" SSCWS satellites. This should maybe be
 * moved to OVTCore (or XYZWindow)?! (Bad for testing?) Would make storing
 * properties when quitting more natural (using getCore().saveSettings())?!
 *
 * NOTE: The class does NOT use any dynamic ("Updateable") model of the SSCWS
 * satellites list but relies on the list being static and the constructor
 * failing if the list can not be obtained.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-0x-xx
 */
// --
// PROPOSAL: Rename to xxxCustomizer in analogy with other window classes.
// PROPOSAL: Save window location to Settings/properties (config file).
public class SSCWSSatellitesSelectionWindow extends JFrame {

    //private static final int DEBUG = 2;
    private static final String WINDOW_TITLE = "Satellite orbit data offered online by NASA SSC";
    private static final String INFO_TEXT = "Satellite orbit data offered online by NASA's Satellite Situation Center (SSC)"
            + " and available through OVT."
            + " Note that some of these \"satellites\" may be located at Lagrange points"
            + " (e.g. ACE), some may be balloons (e.g. BARREL-*), and some may be leaving for other celestial bodies (e.g. MAVEN)."
            + " Note also that OVT does not permit setting the current time to earlier than " + Time.toString(Const.EARLIEST_PERMITTED_GUI_TIME_MJD) + ".";
    //+ " and hence may not be able to make use of all orbit data.";
    private static final String[] COLUMN_GUI_TITLES = {"Bookmarked", "Added", "Name", "Data begins", "Data ends"};
    private static final int COLUMN_INDEX_BOOKMARK = 0;
    private static final int COLUMN_INDEX_GUI_TREE_ADDED = 1;
    private static final int COLUMN_INDEX_SATELLITE_NAME = 2;
    private static final int COLUMN_INDEX_DATA_AVAILABLE_BEGIN = 3;
    private static final int COLUMN_INDEX_DATA_AVAILABLE_END = 4;

    private static final int PREFERRED_WIDTH_EXTRA_MARGIN = 10 + 50;

    private final SSCWSLibrary sscwsLib;
    private final JTable table;
    private final LocalTableModel tableModel;


    /**
     * @throws IOException if the initialization can not obtain list of SSCWS
     * satellites. There should NEVER be a window with an empty list since OVT
     * will reuse the window (the object) when the user asks for it again AND
     * the class is not able to tell that a later attempt to obtain the
     * satellites list has succeeded.
     */
    public SSCWSSatellitesSelectionWindow(
            SSCWSLibrary mSSCWSLib, OVTCore core, SSCWSSatellitesBookmarksModel bookmarks)
            throws IOException {
        
        sscwsLib = mSSCWSLib;
        setTitle(WINDOW_TITLE);

        //this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setLayout(new GridBagLayout());

        /*================
         Create text area
         ===============*/
        final JTextArea infoTextArea = new JTextArea();
        {
            final String infoText = INFO_TEXT
                    + "\n\nSSC Acknowledgements: " + mSSCWSLib.getAcknowledgements().get(0)
                    + "\nSSC Privacy and Important Notices: " + mSSCWSLib.getPrivacyAndImportantNotices().get(0);
            infoTextArea.setText(infoText);
            infoTextArea.setWrapStyleWord(true);
            infoTextArea.setLineWrap(true);
            infoTextArea.setEditable(false);
            infoTextArea.setOpaque(false);  // Use the background color of the window by making it transparent.
            infoTextArea.setFont(Style.getLabelFont());
            infoTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            this.getContentPane().add(infoTextArea, c);
        }


        /*============
         Create Table
         ===========*/
        {
            tableModel = new LocalTableModel(core, bookmarks);
            table = new JTable(tableModel);
            table.setAutoCreateRowSorter(true);   // To enable sorting by the user when clicking on column headers (default sorting).
            table.getRowSorter().toggleSortOrder(COLUMN_INDEX_SATELLITE_NAME); // Sort column once. Does not seem to reverse sorting of already sorted SSC satellites.

            initColumnWidths();

            // Center the contents of the two data availability columns.
            final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(COLUMN_INDEX_DATA_AVAILABLE_BEGIN).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(COLUMN_INDEX_DATA_AVAILABLE_END).setCellRenderer(centerRenderer);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

            //table.setFillsViewportHeight(true);        
            final JScrollPane tableScrollPane = new JScrollPane(table,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    //ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    //ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            final GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.weighty = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.BOTH;
            this.getContentPane().add(tableScrollPane, c);
        }

        /**
         * Needed to get convenient initial width, roughly the width needed to
         * display all data in all columns. Unknown why this has to be done
         * manually. Unknown why extra small margin is needed even without
         * scroll pane (the command changes the content pane, not the window; an
         * otherwise common mistake). Added additional extra arbitrary width
         * margin to cover vertical scroll pane. This is probably not how one is
         * supposed to, but lacking something better, it will have to do. It is
         * not crucial that this works. If it fails, the initial window size can
         * not display all the content but the user can still resize the window
         * to make it do so.
         */
        this.getContentPane().setPreferredSize(
                new Dimension(table.getPreferredSize().width + PREFERRED_WIDTH_EXTRA_MARGIN,
                        this.getPreferredSize().height));

        pack();

        // Only use "core" if non-null. It is useful for testing to be able to
        // instantiate the window without an OVTCore object.
        if (core != null) {
            core.getSats().getChildren().addChildrenListener(tableModel);
        }

        Utils.setInitialWindowPosition(this, core.getXYZWin());
    }


    /**
     * Should in theory set the (preferred) column widths to the width needed to
     * fit the widest cell content in each column. In practice, this does not
     * translate to the JFrame size (via pack()). The relative column sizes seem
     * to be accurate but the initial JFrame size is slightly too small.<BR>
     * /Erik P G Johansson 2015-08-12
     */
    private void initColumnWidths() {

        int totColumnWidth = 0;
        final TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int col = 0; col < COLUMN_GUI_TITLES.length; col++) {

            final TableColumn column = table.getColumnModel().getColumn(col);

            final Component headerComp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            final int headerWidth = headerComp.getPreferredSize().width;

            int cellWidth = 0;
            final Object[] columnWidestValues = tableModel.getWidestValues(col);
            for (Object columnWidestValue : columnWidestValues) {
                final Component cellComp = table.getDefaultRenderer(
                        tableModel.getColumnClass(col)).getTableCellRendererComponent(
                                table, columnWidestValue, false, false, 0, col);
                cellWidth = Math.max(cellWidth, cellComp.getPreferredSize().width);
            }

            final int newColumnWidth = (int) (Math.max(headerWidth, cellWidth));   // Margin helps but not sure why it is needed.
            column.setPreferredWidth(newColumnWidth);
            //column.setPreferredWidth(30);   // TEST
            totColumnWidth = totColumnWidth + newColumnWidth;

        }
        //System.out.println("totColumnWidth = " + totColumnWidth);

        //this.setPreferredSize(new Dimension(totColumnWidth, table.getPreferredSize().height));   // TEST
        //this.getContentPane().setPreferredSize(new Dimension(totColumnWidth+10, this.getPreferredSize().height));   // TEST
    }

    //##########################################################################
    /**
     * NOTE: The row indices still refer to the same row of data even when the
     * table is sorted by a column (because that is how JTable uses TableModel).
     */
    private class LocalTableModel extends AbstractTableModel implements ChildrenListener {

        /**
         * Used for local copy of
         * SSCWSSatellitesSelectionWindow.this.sscwsLib.getAllSatelliteInfo();
         * Could probably be abolished but it is important that the constructor
         * fails if it can not obtain the satellites list.
         */
        private final List<SSCWSLibrary.SSCWSSatelliteInfo> localSatInfoList;
        private final OVTCore core;
        private final SSCWSSatellitesBookmarksModel bookmarks;


        /**
         * NOTE: mCore == null can be used for test code.
         */
        public LocalTableModel(OVTCore mCore, SSCWSSatellitesBookmarksModel mBookmarks) throws IOException {
            List<SSCWSLibrary.SSCWSSatelliteInfo> tempSatInfoList;
            core = mCore;
            localSatInfoList = SSCWSSatellitesSelectionWindow.this.sscwsLib.getAllSatelliteInfo();
            bookmarks = mBookmarks;
        }


        @Override
        public int getRowCount() {
            return localSatInfoList.size();
        }


        @Override
        public int getColumnCount() {
            return COLUMN_GUI_TITLES.length;
        }


        @Override
        public Object getValueAt(int row, int col) {
            //Log.log(this.getClass().getSimpleName() + ".getValueAt(rowIndex=" + row + " , columnIndex=" + col + ")", DEBUG);
            final SSCWSLibrary.SSCWSSatelliteInfo satInfo = localSatInfoList.get(row);

            if (col == COLUMN_INDEX_BOOKMARK) {
                return bookmarks.isBookmark(localSatInfoList.get(row).ID);
            } else if (col == COLUMN_INDEX_GUI_TREE_ADDED) {

                if (core != null) {
                    try {
                        return core.getXYZWin().sscwsSatAlreadyAdded(satInfo.ID);
                    } catch (IOException | NoSuchSatelliteException e) {
                        /* It is very, very unlikely that this exception will be
                         thrown since it can only happen if (1) the list of satellites
                         has not already been downloaded, or (2) the satellite ID
                         does not match any satellite which can only happen due to bugs. */
                        core.sendErrorMessage("Error", e);
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (col == COLUMN_INDEX_SATELLITE_NAME) {
                //Log.log("satInfo.name = " + satInfo.name, DEBUG);
                return satInfo.name;
            } else if (col == COLUMN_INDEX_DATA_AVAILABLE_BEGIN) {
                return Time.toString(satInfo.availableBeginTimeMjd);
            } else if (col == COLUMN_INDEX_DATA_AVAILABLE_END) {
                return Time.toString(satInfo.availableEndTimeMjd);
            } else {
                throw new IllegalArgumentException("No value at col=" + col + ", row=" + row + ".");
                //return "(" + row + ", " + col + ")";
            }
        }


        @Override
        public void setValueAt(Object value, int row, int col) {
            //Log.log(this.getClass().getSimpleName() + ".getValueAt(row=" + row + " , col=" + col + ")", DEBUG);
            final SSCWSLibrary.SSCWSSatelliteInfo satInfo = localSatInfoList.get(row);

            if (col == COLUMN_INDEX_BOOKMARK) {
                final boolean toBookmark = (Boolean) value;
                bookmarks.setBookmark(localSatInfoList.get(row).ID, toBookmark);

            } else if (col == COLUMN_INDEX_GUI_TREE_ADDED) {

                if (core != null) {
                    final boolean addSat = (Boolean) value;
                    if (addSat) {
                        core.getXYZWin().addSSCWSSatAction(satInfo.ID);
                    } else {
                        core.getXYZWin().removeSSCWSSatAction(satInfo.ID);
                    }

                } else {
                    // For debugging / test code. Do nothing.
                }

            } else {
                throw new IllegalArgumentException("Function is not defined for this column.");
            }
        }


        @Override
        public boolean isCellEditable(int row, int col) {
            return ((col == COLUMN_INDEX_BOOKMARK) || (col == COLUMN_INDEX_GUI_TREE_ADDED));
        }


        @Override
        public String getColumnName(int col) {
            return COLUMN_GUI_TITLES[col];
        }


        @Override
        public Class getColumnClass(int col) {
            if ((col == COLUMN_INDEX_BOOKMARK) || (col == COLUMN_INDEX_GUI_TREE_ADDED)) {
                // Needed to make the column values "interpreted" as a checkbox.
                return Boolean.class;
            } else {
                return Object.class;
            }
        }


        // interface ChildrenListener
        @Override
        public void childAdded(ChildrenEvent evt) {
            if (evt.getChild() instanceof SSCWSSat) {
                SSCWSSat sat = (SSCWSSat) evt.getChild();

                final int row = getSatIndex(sat.getSSCWSSatelliteID());
                fireTableCellUpdated(row, COLUMN_INDEX_GUI_TREE_ADDED);
            }
        }


        // interface ChildrenListener
        @Override
        public void childRemoved(ChildrenEvent evt) {
            if (evt.getChild() instanceof SSCWSSat) {
                SSCWSSat sat = (SSCWSSat) evt.getChild();

                final int row = getSatIndex(sat.getSSCWSSatelliteID());
                fireTableCellUpdated(row, COLUMN_INDEX_GUI_TREE_ADDED);
            }
        }


        // interface ChildrenListener
        @Override
        public void childrenChanged(ChildrenEvent evt) {
            // Do nothing.
        }


        // PROPSAL: Move to SSCWSLibrary?
        //    NOTE: Will make it need to throw IOException.
        //    CON: Such a method will not act on localSatInfoList.
        private int getSatIndex(String satId) {
            for (int i = 0; i < localSatInfoList.size(); i++) {
                if (localSatInfoList.get(i).ID.equals(satId)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("There is no such satellite ID (SSC based): \"" + satId + "\".");
        }


        /**
         * For a given column, get an array of values which should contain that
         * value which is the widest when displayed.
         */
        // PROPOSAL: Refactor code to directly return the width of the widest
        //           representation of data in a given column.
        //    NOTE: Only called once. Look at that code.
        // BUG: Can not always handle zero rows.
        // 
        private Object[] getWidestValues(int col) {
            if (col == COLUMN_INDEX_BOOKMARK) {
                return new Object[]{Boolean.FALSE};
            } else if (col == COLUMN_INDEX_GUI_TREE_ADDED) {
                return new Object[]{Boolean.FALSE};
            } else if (col == COLUMN_INDEX_SATELLITE_NAME) {
                final Object[] values = new Object[localSatInfoList.size()];
                for (int row = 0; row < getRowCount(); row++) {
                    values[row] = getValueAt(row, col);
                }
                return values;
            } else if ((col == COLUMN_INDEX_DATA_AVAILABLE_BEGIN)
                    || (col == COLUMN_INDEX_DATA_AVAILABLE_END)) {
                return new Object[]{Time.toString(0.0)};
            }

            throw new IllegalArgumentException("Method is not able to handle this column. This indicates a pure code bug.");
        }

    }

    //##########################################################################

    /**
     * Informal test code.
     */
    public static void main(String[] args) throws IOException {
        //final SSCWSLibrary lib = SSCWSLibraryImpl.DEFAULT_INSTANCE;        
        final SSCWSLibrary lib = SSCWSLibraryTestEmulator.DEFAULT_INSTANCE;

        final JFrame frame = new SSCWSSatellitesSelectionWindow(lib, null, new SSCWSSatellitesBookmarksModel());

        // Needed to prevent lingering processes when testing (launching & closing repeatedly).
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
