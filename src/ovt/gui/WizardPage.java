/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/WizardPage.java,v $
Date:      $Date: 2003/09/28 17:52:42 $
Version:   $Revision: 2.2 $


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

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * WizardPage.java
 *
 * Created on June 29, 2002, 5:54 PM
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
 * @version 
 */
public class WizardPage extends JPanel {
    private String title = "No Title";
    private boolean startPage  = false;
    private boolean finishPage = false;
    private WizardPage previousPage = null;
    private WizardPage nextPage = null;
    
    
    public WizardPage(String title) {
        super();
        this.title = title;
    }
    
    public WizardPage(String title, WizardPage previousPage) {
        super();
        this.title = title;
        this.previousPage = previousPage;
        previousPage.setNextPage(this);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isFirstPage() {
        return startPage;
    }
    
    public void isFirstPage(boolean startPage) {
        this.startPage = startPage;
    }
    
    public boolean isLastPage() {
        return finishPage;
    }
    
    public void isLastPage(boolean finishPage) {
        this.finishPage = finishPage;
    }
    
    public WizardPage getPreviousPage() {
        return previousPage;
    }
    
    public void setPreviousPage(WizardPage previousPage) {
        this.previousPage = previousPage;
    }
    
    public WizardPage getNextPage() {
        return nextPage;
    }
    
    public void setNextPage(WizardPage nextPage) {
        this.nextPage = nextPage;
    }
    
    public WizardPage nextButtonPressed() {
        return nextPage;
    }
    
}
