/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/src/mag/OMNI2CustomizerTest.java,v $
 Date:      $Date: 2015/10/21 17:05:00 $
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

import java.io.IOException;
import javax.swing.JFrame;
import ovt.OVTCore;
import ovt.beans.TimeChangeSupport;
import ovt.datatype.TimeSet;
import ovt.event.MagPropsEvent;
import ovt.event.TimeEvent;
import ovt.interfaces.MagPropsChangeListener;
import ovt.interfaces.TimeChangeListener;
import ovt.object.TimeSettingsInterface;
import ovt.util.Log;

/**
 * Informal test code for OMNI2Customizer. Instantiates and displays the
 * window without launching all of OVT.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 */
public class OMNI2CustomizerTest {

    //##########################################################################
    private static class CheckBoxDataModel implements MagProps.ActivityEditorOrOMNI2_DataModel {

        private MagProps.DataSourceChoice dataSourceChoice;
        private final MagPropsChangeSupport magPropsChangeSupport = new MagPropsChangeSupport(this);
        private final int activityIndex;


        public CheckBoxDataModel(int mActivityIndex) {
            activityIndex = mActivityIndex;
        }


        @Override
        public MagProps.DataSourceChoice getDataSourceChoice() {
            return dataSourceChoice;
        }


        @Override
        public void setDataSourceChoice(MagProps.DataSourceChoice choice) {
            dataSourceChoice = choice;
            final MagPropsEvent evt = new MagPropsEvent(this, activityIndex);
            magPropsChangeSupport.fireMagPropsChange(evt);
        }


        @Override
        public void addMagPropsChangeListener(MagPropsChangeListener listener) {
            magPropsChangeSupport.addMagPropsChangeListener(listener);
        }
    }

    //##########################################################################
    private static class MagPropsEmulator implements MagPropsInterface {

        @Override
        public MagProps.ActivityEditorOrOMNI2_DataModel getActivityEditorOrOMNI2_DataModel(int activityIndex) {
            return new CheckBoxDataModel(activityIndex);
        }


        @Override
        public OVTCore getCore() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public double[] getActivityOMNI2(int activityIndex, double mjd) throws OMNI2DataSource.ValueNotFoundException, IOException {
            if (mjd > 0.8e4) {
                throw new OMNI2DataSource.ValueNotFoundException("mjd="+mjd);
            }
            
            final double x = activityIndex + mjd/1e3;
            final double xr = Math.round(x*10)/10.0;
            if (activityIndex == MagProps.IMF) {
                return new double[]{xr, xr * 2, -xr};
            } else {
                return new double[]{xr};
            }
        }
    }

    //##########################################################################
    private static class TimeSettingsEmulator implements TimeSettingsInterface {

        private TimeChangeSupport timeChangeSupport = new TimeChangeSupport(this);
        private TimeSet timeSet;


        public TimeSettingsEmulator() {
            changeTimeSet();
        }


        @Override
        public void addTimeChangeListener(TimeChangeListener listener) {
            timeChangeSupport.addTimeChangeListener(listener);
        }


        public void changeTimeSet() {
            final double intervalMjd = Math.random() * 10;
            timeSet = new TimeSet(
                    Math.random() * 1e4,
                    intervalMjd,
                    intervalMjd * 0.1);
            timeChangeSupport.fireTimeChange(new TimeEvent(this, TimeEvent.CURRENT_MJD, timeSet));
        }


        @Override
        public double getCurrentMjd() {
            return timeSet.getCurrentMjd();
        }
    }


    /**
     * Informal test code.
     */
    public static void main(String[] args) throws InterruptedException {
        Log.setLogLevel(2);
        //JCheckBox cb = new MagProps.ActivityEditorOrOMNI2_CheckBox();
        TimeSettingsEmulator tse = new TimeSettingsEmulator();
        final OMNI2Customizer win = new OMNI2Customizer(new MagPropsEmulator(), tse);
        win.setVisible(true);

        // Needed to prevent lingering processes when testing (launching & closing repeatedly).
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        while (true) {
            Thread.sleep(2000);
            System.out.println(OMNI2CustomizerTest.class.getSimpleName()+"#main: Ny TimeSet.");
            tse.changeTimeSet();
        }
    }
}
