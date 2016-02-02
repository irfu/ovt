/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2Customizer.java,v $
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import ovt.util.Utils;

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
// PROPOSAL: Add "Close" button, which is then fed to getRootPane().setDefaultButton(...).
public class OMNI2Customizer extends JFrame {

    //private static final int DEBUG = 2;
    private static final String INITIAL_VALUE_STRING = "<INITIAL STRING>";   // Should never be visible in the GUI if everything works.
    private static final String NO_DATA_DISPLAY_STR = "<data gap>";   // Displayed when can not find any value (ValueNotFoundException).
    private static final String IO_ERROR_DISPLAY_STR = "<I/O ERROR>";
    private static final String DISABLED_DISPLAY_STR = "(disabled)";

    private static final String DISPLAY_OMNI2_VALUES_CHECKBOX_TEXT = "Display OMNI2 values (might slow down application in case of I/O errors)";

    private static final String WINDOW_TITLE = "OMNI2 Data Settings";

    // PROPOSAL: Say something about which values are used in case of (1) data gaps, or (2) I/O error?
    // PROPOSAL: Say something about usage of internet/FTP, in case there is none?
    private static final String INFO_TEXT
            = "Settings for OMNI2 data (hourly averaged) offered online by NASA SPDF and available through OVT."
            + " OMNI2 data is downloaded in the form of OMNI2 text files which are stored in their original format in a cache directory from which they are read.";
    //+ " In the event of network failure, the user can obtain and add these files him-/herself as a backup solution.";
    // Add ftp address, directory where files are stored, that files can be added manually?!

    /**
     * List of activity indices to use. The order defines the order in which
     * they will be displayed.
     */
    private final static int[] ACTIVITY_INDICES = new int[]{
        MagProps.KPINDEX, MagProps.IMF, MagProps.SWP,
        MagProps.DSTINDEX, MagProps.MACHNUMBER, MagProps.SW_VELOCITY};
    private final Map<Integer, JTextField> activityValueTextFields = new HashMap();
    private final MagPropsInterface magProps;
    private final TimeSettingsInterface timeSettings;

    final JCheckBox omni2ValuesDisplayedCheckBox;


    public OMNI2Customizer(
            MagPropsInterface mMagProps,
            TimeSettingsInterface mTimeSettings) {

        // Class requires core.timeSettings to be supplied as parameter and hence be initialized first.
        // Therefore it is good to check if the parameter is in fact initialized.
        // (OVTCore initializing core.magProps before code.timeSettings could lead to this error.)
        if (mTimeSettings == null) {
            throw new IllegalArgumentException("mTimeSettings == null");
        }

        magProps = mMagProps;
        timeSettings = mTimeSettings;
        setTitle(WINDOW_TITLE);

        this.getContentPane().setLayout(new GridBagLayout());

        int rootGridY = 0;  // Value which is updated throughout the method.
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
                // For every (OMNI2) activity index...
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

                    final GridBagConstraints c = createGBConstraints(1, indexGridY, 0.5, 1, GridBagConstraints.HORIZONTAL);
                    addComponentToPanel(indicesPanel, newTextField, c);
                }
                indexGridY++;
            }//*/

            timeSettings.addTimeChangeListener(new TimeChangeListener() {
                @Override
                public void timeChanged(TimeEvent evt) {
                    if (OMNI2Customizer.this.isVisible() && evt.currentMjdChanged()) {
                        refresh();
                    }
                }
            });

            {
                final GridBagConstraints c = createGBConstraints(0, rootGridY, 0.5, 0.5, GridBagConstraints.HORIZONTAL);
                addComponentToPanel(this.getContentPane(), indicesPanel, c);
            }
        }
        rootGridY++;
        
        // Add empty space.
        {
            final GridBagConstraints c = createGBConstraints(0, rootGridY, 1, 0, GridBagConstraints.BOTH);
            addComponentToPanel(this.getContentPane(), new Box.Filler(new Dimension(1,10), new Dimension(1,10), new Dimension(1,200)), c);
        }
        rootGridY++;

        {
            omni2ValuesDisplayedCheckBox = new JCheckBox(DISPLAY_OMNI2_VALUES_CHECKBOX_TEXT);
            final GridBagConstraints c = createGBConstraints(0, rootGridY, 1, 0, GridBagConstraints.BOTH);
            // c.weighty = 0;  // Important for fitting text initially. Do not know why.
            c.anchor = GridBagConstraints.NORTHWEST;   // Put component at upper-left.
            addComponentToPanel(this.getContentPane(), omni2ValuesDisplayedCheckBox, c);

            omni2ValuesDisplayedCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setOMNI2ValuesDisplayed(omni2ValuesDisplayedCheckBox.isSelected());
//                    System.out.println("actionPerformed: newCheckBox.isSelected(); = " + omni2ValuesDisplayedCheckBox.isSelected());
                }
            });
            setOMNI2ValuesDisplayed(false);   // Default value.
        }
        rootGridY++;//*/

        refresh();
        pack();
        setResizable(false);


        // Set location at center of screen.
        Utils.centerWindow(this);
    }//*/


    public void setOMNI2ValuesDisplayed(boolean mOMNI2ValuesDisplayed) {
        omni2ValuesDisplayedCheckBox.setSelected(mOMNI2ValuesDisplayed);  // Does (fortunately) not trigger ActionEvent.
        refresh();
    }


    public boolean isOMNI2ValuesDisplayed() {
        return omni2ValuesDisplayedCheckBox.isSelected();
    }


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


    /**
     * Update/refresh all OMNI2 text fields.
     */
    private void refresh() {
        final double timeMjd = timeSettings.getCurrentMjd();
        for (int activityIndex : ACTIVITY_INDICES) {
            refresh(activityIndex, timeMjd);
        }
    }


    /**
     * Update/refresh the text string displayed in any one OMNI2 text field of
     * the "activityValueTextFields".
     */
    private void refresh(int activityIndex, double timeMjd) {
        String str = null;
        if (isOMNI2ValuesDisplayed()) {
            try {

                final double[] values = magProps.getActivityOMNI2(
                        activityIndex, timeMjd);
                final String unitStr = MagProps.getUnitString(activityIndex);
                final String unitSuffix = (unitStr != null) ? " [" + unitStr + "]" : "";  // String to append to string.

                for (double value : values) {
                    final String componentValue = Double.toString(value) + unitSuffix;
                    if (str == null) {
                        str = componentValue;
                    } else {
                        str = str + ", " + componentValue;
                    }
                }

            } catch (OMNI2DataSource.ValueNotFoundException e) {
                str = NO_DATA_DISPLAY_STR;
            } catch (IOException ex) {
                str = IO_ERROR_DISPLAY_STR;
                /**
                 * NOTE: This is a bad place to generate an error message
                 * window/popup since (1) the text field says that there is an
                 * error anyway, and (2) the error message popups can not be
                 * disabled since OMNI2 data is requested to fill the text
                 * fields even if the user disables the use of OMNI2 data in the
                 * visualization (2016-02-01).
                 */
                String msg = "I/O error when trying to obtain OMNI2 value for display: " + ex.getMessage();
                Log.err(msg);
            }
        } else {
            str = DISABLED_DISPLAY_STR;
        }

        activityValueTextFields.get(activityIndex).setText(str);
    }


    //##########################################################################
    /**
     * Run test class. For convenience when modifying & testing code.
     */
    public static void main(String[] args) throws InterruptedException {
        OMNI2CustomizerTest.main(args);
    }

}
