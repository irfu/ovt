/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/ErrorMessageWindow.java,v $
  Date:      $Date: 2003/09/28 17:52:40 $
  Version:   $Revision: 1.2 $


Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
Yuri Khotyaintsev)
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
Khotyaintsev

=========================================================================*/


/*
 * ErrorMessageWindow.java
 *
 * Created on ?'??????, 12, ??????? 2003, 12:18
 *
 * NOTE: Only displays message originating from an exception. It does not appear
 * the caller can add extra text for context.
 */

package ovt.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


/**
 *
 * @author  ko
 */
public class ErrorMessageWindow extends JDialog  {
    private final static String SHOW_DETAILS = "Details >>";
    private final static String HIDE_DETAILS   = "<< Details";
    private final Exception e = new Exception();
    private final JLabel messageLabel = new JLabel();
    private final JButton okButton = new JButton("OK");
    private final JButton detailsButton = new JButton(SHOW_DETAILS);
    private final JTextArea detailsTA = new JTextArea(25, 80);   // TA = Text Area
    private final JScrollPane detailsPanel = new JScrollPane();

    /** Creates a new instance of ErrorMessageWindow */
    public ErrorMessageWindow(Frame owner, Exception e) {
        super(owner, "Error", true); // modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel cont = new JPanel();
        cont.setLayout( new BorderLayout(20,20) );
        setContentPane(cont);
        //cont.setBorder( BorderFactory.createEmptyBorder(10,10,10));

        messageLabel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // OK / Details Buttons Panel

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add( Box.createHorizontalGlue() );

        getRootPane().setDefaultButton(okButton);
        okButton.setPreferredSize( detailsButton.getPreferredSize() );
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        detailsButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                 showHideDetailsPanel();
            }
        });

        panel.add(okButton);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(detailsButton);
        panel.add(Box.createHorizontalStrut(15));

        // detailsPanel
        detailsTA.setEditable(false);
        detailsTA.setBackground( panel.getBackground() );
        detailsPanel.setViewportView(detailsTA);
        int width = panel.getPreferredSize().width;
        if (width < 450) width=450;
        detailsPanel.setPreferredSize(new Dimension(width,300));

        cont.add(messageLabel, BorderLayout.NORTH);
        cont.add(panel, BorderLayout.CENTER); // buttonsPanel

        setException(e);
    }


    public void setException(Exception e) {
       setResizable(true);
       messageLabel.setText(e.getMessage());
       detailsTA.setText("");
       for (int i=0; i<e.getStackTrace().length; i++) {
            detailsTA.append(""+e.getStackTrace()[i]+"\n   ");
       }

        pack();
        setResizable(false);

        // Center the window.
        // NOTE: Not using Utils.setInitialWindowPosition to minimize risk of triggering other exception.
//        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension windowSize = getSize();
//        setLocation(scrnSize.width/2 - windowSize.width/2,
//                 scrnSize.height/2 - windowSize.height/2);
        this.setLocationRelativeTo(null);
    }

    public void showHideDetailsPanel() {
        if (detailsButton.getText().equals(SHOW_DETAILS)) {
            setResizable(true);
            getContentPane().add(detailsPanel, BorderLayout.SOUTH);
            detailsButton.setText(HIDE_DETAILS);
            pack();
            setResizable(false);
        } else {
            setResizable(true);
            getContentPane().remove(detailsPanel);
            detailsButton.setText(SHOW_DETAILS);
            pack();
            setResizable(false);
        }

    }
}
