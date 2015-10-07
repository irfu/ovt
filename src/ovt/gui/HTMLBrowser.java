/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/HTMLBrowser.java,v $
  Date:      $Date: 2003/09/28 17:52:41 $
  Version:   $Revision: 2.4 $


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
 * HTMLBrowser.java
 *
 * Created on den 2 juni 2000, 19:58
 */
 
package ovt.gui;

import ovt.util.Utils;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.swing.event.*;

/** Basic class for browsing HTML files
 * @author Yuri Khotyaintsev
 * @version 1.0
 */
public class HTMLBrowser extends javax.swing.JFrame {

  private urlHistory history;
  
  private ovt.OVTCore core = null;
  
  /** Creates new  HTMLBrowser
   * @param core {@link ovt.OVTCore}
   */
  public HTMLBrowser(ovt.OVTCore core) {
    initComponents ();
    this.core = core;
    history = new urlHistory();
    browserPane.setEditable(false);
    browserPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().toString().compareTo("ACTIVATED")==0)
        try {
          String uurl = e.getURL().toString();
          browserPane.setPage(uurl);
          history.putUrl(uurl);
          backBtn.setEnabled( !history.isAtStart());
          forwardBtn.setEnabled( !history.isAtEnd());
        }
        catch(IOException ex) { ex.printStackTrace(); }
        else {
          if (e.getEventType().toString().compareTo("ENTERED")==0)
          browserPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
          else if (e.getEventType().toString().compareTo("EXITED")==0)
          browserPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      } 
      
    });
    /*try {
      browserPane.setPage(url);
      history.putUrl(url);
    }catch(Exception e){
      e.printStackTrace();
    }*/
    pack ();
    setSize(new Dimension(600, 600));
  }
  
  /** sets URL
   * @param url {@link java.net.URL}
   * @throws IOException
   */
  public void setPage(URL url) throws IOException {
    flash();
    history.putUrl(url.toString());
    browserPane.setPage(url);
  }
  
  /** sets URL
   * @param url
   * @throws IOException
   */
  public void setPage(String url) throws IOException {
    flash();
    history.putUrl(url);
    browserPane.setPage(url);
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents() {//GEN-BEGIN:initComponents
      scrollPane = new javax.swing.JScrollPane();
      browserPane = new javax.swing.JEditorPane();
      toolBar = new javax.swing.JToolBar();
      backBtn = new javax.swing.JButton();
      forwardBtn = new javax.swing.JButton();
      homeBtn = new javax.swing.JButton();
      try {
          setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/ovt.gif")));
      } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); };
      setTitle("OVT Browser");
      addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) {
              exitForm(evt);
          }
      }
      );
      
      scrollPane.setMinimumSize(new java.awt.Dimension(200, 200));
      
      scrollPane.setViewportView(browserPane);
        
        
      getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
      
      
      try {
          backBtn.setIcon(new javax.swing.ImageIcon(Utils.findResource("images/VCRBack.gif")));
      } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); };
      
        backBtn.setMaximumSize(new java.awt.Dimension(120, 50));
        backBtn.setMinimumSize(new java.awt.Dimension(120, 50));
        backBtn.setText("Back");
        backBtn.setLabel("Back");
        backBtn.setEnabled(false);
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnPressed(evt);
            }
        }
        );
        toolBar.add(backBtn);
        
      try { 
          forwardBtn.setIcon(new javax.swing.ImageIcon(Utils.findResource("images/VCRForward.gif")));
      } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); };
        forwardBtn.setMaximumSize(new java.awt.Dimension(120, 50));
        forwardBtn.setMinimumSize(new java.awt.Dimension(120, 50));
        forwardBtn.setText("Forward");
        forwardBtn.setLabel("Forward");
        forwardBtn.setEnabled(false);
        forwardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardBtnPressed(evt);
            }
        }
        );
        toolBar.add(forwardBtn);
        
      try {   
        homeBtn.setIcon(new javax.swing.ImageIcon(Utils.findResource("images/ovt.gif")));
      } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); };
       
        homeBtn.setMaximumSize(new java.awt.Dimension(120, 50));
	try {
          homeBtn.setPressedIcon(new javax.swing.ImageIcon(Utils.findResource("images/ovt.gif")));
        } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); };
	homeBtn.setMinimumSize(new java.awt.Dimension(120, 50));
        homeBtn.setText("Home");
        homeBtn.setLabel("OVT Home");
        homeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeBtnPressed(evt);
            }
        }
        );
        toolBar.add(homeBtn);
        
        
      getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);
      
  }//GEN-END:initComponents

  private void forwardBtnPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardBtnPressed
  try {
      browserPane.setPage(history.goForward());
      backBtn.setEnabled( !history.isAtStart());
      forwardBtn.setEnabled( !history.isAtEnd());
    }catch(Exception e){
      e.printStackTrace();
    }
  }//GEN-LAST:event_forwardBtnPressed

  private void backBtnPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnPressed
  try {
      browserPane.setPage(history.goBack());
      backBtn.setEnabled( !history.isAtStart());
      forwardBtn.setEnabled( !history.isAtEnd());
    }catch(Exception e){
      e.printStackTrace();
    }
  }//GEN-LAST:event_backBtnPressed

  private void homeBtnPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeBtnPressed
    String url = ovt.OVTCore.OVT_HOMEPAGE; 
    try {
      browserPane.setPage(url);
      history.putUrl(url);
      backBtn.setEnabled( !history.isAtStart());
      forwardBtn.setEnabled( !history.isAtEnd());
    }catch(Exception e){
      e.printStackTrace();
    }
  }//GEN-LAST:event_homeBtnPressed

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
    this.setVisible(false);
  }//GEN-LAST:event_exitForm



  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane scrollPane;
  private javax.swing.JEditorPane browserPane;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JButton backBtn;
  private javax.swing.JButton forwardBtn;
  private javax.swing.JButton homeBtn;
  // End of variables declaration//GEN-END:variables

  private void flash() {
    history.flash();
    backBtn.setEnabled( !history.isAtStart());
    forwardBtn.setEnabled( !history.isAtEnd());
  }
  /** Browser history tracking class
   */
  public class urlHistory {
    private Vector history =null;

    private int position =0;

    /** Creates new urlHistory
     */
    public urlHistory() {
      history = new Vector();
    }

    /** Check if we are at the end of history
     * @return <CODE>true</CODE> if end of history
     */
    public boolean isAtEnd() {
      //System.out.println("isAtEnd "+position);
      //System.out.println("isAtEnd:size "+history.size());
      if ( position >= history.size() ) return true;
      else return false;
    }

    /** Check if we are at the start of history
     * @return <CODE>true</CODE> if start
     */
    public boolean isAtStart() {
      //System.out.println("isAtStart "+position);
      if ( position <= 1 ) return true;
      else return false;
    }

    /** shift forward
     * @return next URL
     */
    public String goForward() {
      //System.out.println("goForw "+position);
      if ( isAtEnd() ) return null;
      else {
        position++;
        return history.elementAt(position - 1).toString();
      }
    }

    /** next URL in history
     * @return nest URL
     */
    public String getNextUrl() {
      //System.out.println("getNextUrl "+position);
      if ( isAtEnd() ) return null;
      else return history.elementAt(position).toString();
    }

    /** shift back
     * @return previous URL
     */
    public String goBack() {
      //System.out.println("getPreviousUrl "+position);
      if ( isAtStart() ) return null;
      else {
        position--;
        return history.elementAt(position - 1).toString();
      }
    }

    /** insert URL into history
     * @param url
     */
    public void putUrl(String url) {
      //System.out.println("putUrl "+position);
      if ( isAtEnd() ) {
        history.addElement(url);
        position++;
      }
      else if ( url.compareTo(getNextUrl()) != 0) {
        position++;
        history.insertElementAt(url,position);
        history.setSize(position);
      }
    }


    protected void flash() {
      position=0;
      history.setSize(position);
    }
  }
}
