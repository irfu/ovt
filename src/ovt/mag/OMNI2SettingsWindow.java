/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2SettingsWindow.java,v $
 Date:      $Date: 2015/10/19 20:14:00 $
 Version:   $Revision: 1.00 $


 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
 Yuri Khotyaintsev, Erik P. G. Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson

 =========================================================================*/
package ovt.mag;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import ovt.event.TimeEvent;
import ovt.gui.Style;
import ovt.interfaces.TimeChangeListener;
import ovt.object.TimeSettingsInterface;
import ovt.util.Log;

/**
 * Window for OMNI2 settings.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 */
// PROPOSAL: Add text field for viewing & modifying a variable for how far away
//           (in time) an OMNI2 value may be without receiving a warning.
//    NOTE: Should probably reuse 
//          ovt.beans.editor.IntervalEditor, ovt.beans.editor.IntervalEditorPanel,
//          and ovt.datatype.Interval for this.
//
// PROPOSAL: Change name to be more in agreement with the rest of OVT: OMNI2Customizer? OMNI2CustomizerWindow?
// PROPOSAL: Add "Close" button, which is then fed to getRootPane().setDefaultButton(...).
public class OMNI2SettingsWindow extends JFrame {

    private static final int DEBUG = 2;
    private static final String INITIAL_VALUE_STRING = "<INITIAL STRING>";   // Should never be visible in the GUI if everything works.
    private static final String NO_DATA_DISPLAY_STR = "<NO DATA>"; // Displayed when can not find any value (ValueNotFoundException).
    private static final String IO_ERROR_DISPLAY_STR = "<I/O ERROR>";
    private static final String WINDOW_TITLE = "OMNI2 Data Settings";
    private static final String INFO_TEXT
            = "Settings for OMNI2 data (hourly averaged) offered online by NASA SPDF and available through OVT.";
    //+ " OMNI2 data is downloaded in the form of OMNI2 text files which are stored in their original format in a cache directory from which they are read."
    //+ " In the event of network failure, the user can obtain and add these files him-/herself as a backup solution.";
    // Add ftp address, directory where files are stored, that files can be added manually?!

    /**
     * List of activity indices to use. The order defines the order in which
     * they will be displayed.
     */
    private final static int[] ACTIVITY_INDICES = new int[]{
        MagProps.KPINDEX, MagProps.IMF, MagProps.SWP,
        MagProps.DSTINDEX, MagProps.MACHNUMBER, MagProps.SW_VELOCITY};
//    private final static int[] ACTIVITY_INDICES = new int[]{
//        MagProps.KPINDEX, MagProps.IMF/*, MagProps.SWP*/};
    private final Map<Integer, JTextField> activityValueTextFields = new HashMap();
    private final MagPropsInterface magProps;
    private final TimeSettingsInterface timeSettings;


    public OMNI2SettingsWindow(
            MagPropsInterface mMagProps,
            TimeSettingsInterface mTimeSettings) {

        magProps = mMagProps;
        timeSettings = mTimeSettings;
        setTitle(WINDOW_TITLE);

        this.getContentPane().setLayout(new GridBagLayout());

        int rootGridY = 0;  // Value which is update throughout the code.
        {
            final JTextArea infoTextArea = createDefaultTextArea(INFO_TEXT);
            final GridBagConstraints c = createGBConstraints(0, rootGridY, 1, 0, GridBagConstraints.BOTH);
            // c.weighty = 0;  // Important for fitting text initially. Do not know why.
            c.anchor = GridBagConstraints.NORTHWEST;   // Put component at upper-left.
            addComponentToPanel(this.getContentPane(), infoTextArea, c);
        }
        rootGridY++;//*/

        {
            final JPanel indicesPanel = new JPanel(new GridBagLayout());
            int indexGridY = 0;
            {
                final JTextArea checkBoxesTitleTextArea = createDefaultTextArea(
                        "Activity data taken from OMNI2:");
                checkBoxesTitleTextArea.setColumns(20);
                checkBoxesTitleTextArea.setRows(2);
                final GridBagConstraints c = createGBConstraints(0, indexGridY, 0.5, 0.0, GridBagConstraints.HORIZONTAL);
                c.anchor = GridBagConstraints.SOUTHWEST;
                addComponentToPanel(indicesPanel, checkBoxesTitleTextArea, c);
            }
            {
                final JTextArea checkBoxesTitleTextArea = createDefaultTextArea(
                        "OMNI2 values for the currently selected time:");
                checkBoxesTitleTextArea.setColumns(20);
                checkBoxesTitleTextArea.setRows(2);
                final GridBagConstraints c = createGBConstraints(1, indexGridY, 0.5, 0, GridBagConstraints.HORIZONTAL);
                c.anchor = GridBagConstraints.SOUTHWEST;
                addComponentToPanel(indicesPanel, checkBoxesTitleTextArea, c);
            }
            indexGridY++;//*/

            for (int activityIndex : ACTIVITY_INDICES) {
                {
                    final JCheckBox newCheckBox = new MagProps.ActivityEditorOrOMNI2_CheckBox(
                            MagProps.getActivityName(activityIndex),
                            magProps.getActivityEditorOrOMNI2_DataModel(activityIndex)
                    );
//                    newCheckBox.setMinimumSize(newCheckBox.getPreferredSize());  // Does not seem to work.
                    final GridBagConstraints c = createGBConstraints(0, indexGridY, 0.5, 1, GridBagConstraints.HORIZONTAL);
                    addComponentToPanel(indicesPanel, newCheckBox, c);
                }
                {
                    final JTextField newTextField = new JTextField(INITIAL_VALUE_STRING + rootGridY);
                    activityValueTextFields.put(activityIndex, newTextField);
//                    newTextField.setMinimumSize(newTextField.getPreferredSize());  // Does not seem to work.
                    newTextField.setEditable(false);

                    timeSettings.addTimeChangeListener(new TimeChangeListener() {
                        @Override
                        public void timeChanged(TimeEvent evt) {
//                            Log.log(this.getClass().getSimpleName() + "#timeChanged", DEBUG);
                            if (OMNI2SettingsWindow.this.isVisible() && evt.currentMjdChanged()) {
                                final double currentMjd = evt.getTimeSet().getCurrentMjd();
//                                Log.log("   timeChanged - Updating text field: "
//                                        + "currentMjd = " + currentMjd
//                                        + "; activityIndex = " + activityIndex, DEBUG);
                                refresh(activityIndex, currentMjd);
                            }
                        }
                    });

                    final GridBagConstraints c = createGBConstraints(1, indexGridY, 0.5, 1, GridBagConstraints.HORIZONTAL);
                    addComponentToPanel(indicesPanel, newTextField, c);
                }
                indexGridY++;
            }//*/

            {
                final GridBagConstraints c = createGBConstraints(0, rootGridY, 0.5, 0.5, GridBagConstraints.HORIZONTAL);
                addComponentToPanel(this.getContentPane(), indicesPanel, c);
            }
        }
        rootGridY++;

        /*{
         final IntervalEditor intervalEditor = new IntervalEditor();
         final IntervalEditorPanel intervalEditorPanel = new IntervalEditorPanel(intervalEditor);
         final GridBagConstraints c = createGBConstraints(0, rootGridY, 0.5, 0.5, GridBagConstraints.HORIZONTAL);
         addComponentToPanel(this.getContentPane(), intervalEditorPanel, c);
         }
         rootGridY++;  //*/
        refresh();
        pack();
        
        // Set location at center of screen.
        final Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = getSize();
        setLocation(scrnSize.width / 2 - frameSize.width / 2, scrnSize.height / 2 - frameSize.height / 2);
    }//*/


    /*@Override
     public void timeChanged(TimeEvent evt) {
     throw new UnsupportedOperationException("Not supported yet.");
     }*/
    /**
     * Add JComponent to panel. Useful to add through this method when debugging
     * since one can easily add printouts for all manually added JComponents.
     */
    private static void addComponentToPanel(Container container, JComponent comp, Object o) {
//        if (o instanceof GridBagConstraints) {
//            final GridBagConstraints c = (GridBagConstraints) o;
//            System.out.println("c.gridy = " + c.gridy);
//        }
//     System.out.println(s + ".min =" + comp.getMinimumSize());
//     System.out.println(s + ".pref=" + comp.getPreferredSize());
//     System.out.println(s + ".max =" + comp.getMaximumSize());
        container.add(comp, o);
    }


    /**
     * Utility function to make initialization more consise and clear.
     */
    private static JTextArea createDefaultTextArea(String text) {
        final int BORDER_SIZE = 5;

        final JTextArea textArea = new JTextArea(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);    // NOTE: Makes text field grey by default.
        textArea.setOpaque(false);  // Use the background color of the window by making it transparent.
        textArea.setFont(Style.getLabelFont());
        textArea.setBorder(BorderFactory.createEmptyBorder(
                BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        return textArea;
    }


    /**
     * Utility function to make initialization more consise and clear.
     */
    private static GridBagConstraints createGBConstraints(
            int gridx, int gridy, double weightx, double weighty, int fill) {

        // Quick argument check to make sure parameters are not confused with
        // eachother. Nonessential.
        if ((fill != GridBagConstraints.BOTH) & (fill != GridBagConstraints.HORIZONTAL)) {
            throw new IllegalArgumentException();
        }

        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.weightx = weightx;
        c.weighty = weighty;
        c.fill = fill;
        return c;
    }


    private void refresh() {
        final double timeMjd = timeSettings.getCurrentMjd();
        for (int activityIndex : ACTIVITY_INDICES) {
            refresh(activityIndex, timeMjd);
        }
    }


    private void refresh(int activityIndex, double timeMjd) {
        String str = null;
        try {
            final double[] values = magProps.getActivityOMNI2(
                    activityIndex, timeMjd);
            final String unitStr = MagProps.getUnitString(activityIndex);            
            final String unitSuffix = (unitStr!=null) ? " ["+unitStr+"]" : "" ;  // String to append to string.
            
            for (double value : values) {
                final String componentValue = Double.toString(value) + unitSuffix;
                if (str == null) {
                    str = componentValue;
                } else {
                    str = str + ", " + componentValue;
                }
            }

        } catch (OMNI2DataSource.ValueNotFoundException ex) {
            str = NO_DATA_DISPLAY_STR;
        } catch (IOException ex) {
            str = IO_ERROR_DISPLAY_STR;
            // Restating the title since it is long and may not display entirely in
            // the title bar if the window is small.
            magProps.getCore().sendErrorMessage("I/O error while trying to obtain OMNI2 value.",
                    "I/O error while trying to obtain OMNI2 value:\n"+ ex.getMessage());
        }
        activityValueTextFields.get(activityIndex).setText(str);
    }


    //##########################################################################
    /**
     * Run test class. For convenience when modifying & testing code.
     */
    public static void main(String[] args) throws InterruptedException {
        OMNI2SettingsWindowTest.main(args);
    }

}
