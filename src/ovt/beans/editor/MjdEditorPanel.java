/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/editor/MjdEditorPanel.java,v $
Date:      $Date: 2003/09/28 17:52:36 $
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

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * MjdEditorPanel.java
 *
 * Created on June 21, 2002, 2:57 PM
 */

package ovt.beans.editor;

import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*; 
import javax.swing.plaf.*;

import java.awt.Toolkit;


/**
 *
 * @author  ko
 * @version 
 */
public class MjdEditorPanel extends JTextField implements PropertyChangeListener, Syncer {
    
    private MjdEditor editor;
    private Object oldPropertyValue = null;
    private DocumentListener documentListener;
    private int oldCaretPosition = 0;
    
/** Creates new MjdEditorPanel */
public MjdEditorPanel(MjdEditor ed) {
    this.editor = ed;
    editor.addPropertyChangeListener(this);
    
    oldPropertyValue = editor.getValue();
    
    setDocument( new TimeDocument() );
    
    documentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent evt) {
            //ovt.util.Log.log("changedUpdate");
            // is never fired here
        }
        /** New WRONG property value is equal to oldPropertyValue :
         * Whenever the user specifies wrong string the editors
         * property value is rolled back to the original.
         * The original one is the value which was specified from outsede. 
         * Not from this editor.
         */
        public void insertUpdate(DocumentEvent evt) {
            editor.removePropertyChangeListener(MjdEditorPanel.this);
            try {
                editor.setAsText(getText());
            } catch (IllegalArgumentException e2) {
                // revert the value to the original one
                if (oldPropertyValue != null)
                    editor.setValue(oldPropertyValue);
                //Toolkit.getDefaultToolkit().beep();
            }
            editor.addPropertyChangeListener(MjdEditorPanel.this);
        }
        public void removeUpdate(DocumentEvent evt) {
            // do not implement this
        }
    };
    
    
    
    getDocument().addDocumentListener( documentListener );
    
   /*addFocusListener( new FocusListener() {
        public void focusGained(FocusEvent e) {}
        public void focusLost(FocusEvent e) {
            if (e.isTemporary()) return;
            editor.removePropertyChangeListener(MjdEditorPanel.this);
            try {
                editor.setAsText(getText());
                editor.addPropertyChangeListener(MjdEditorPanel.this);
            } catch (IllegalArgumentException e2) {
                editor.addPropertyChangeListener(MjdEditorPanel.this);
                // revert the value to the original one
                if (oldPropertyValue != null)
                    editor.setValue(oldPropertyValue);
            }
            
        }
    }); */
    
    setCaret(new OvertypeCaret());
    
    // prevent user from placing caret on "-", ":", " " places
    // adjust the carret so, that this places will be skiipped
    // when user moves the carret
    addCaretListener( new CaretListener() {
        public void caretUpdate(CaretEvent evt) {
            int newPos = evt.getDot();
            if (newPos != oldCaretPosition) {
                if ((newPos == 4 ) || (newPos == 7) || (newPos == 10) || 
                    (newPos == 13) || (newPos == 16)) {
                        int movingDirection = (newPos - oldCaretPosition > 0) ? 1 : -1 ;
                        oldCaretPosition = newPos + movingDirection;
                        setCaretPosition(oldCaretPosition);
                } else if (newPos == 19) { // prevent caret from standing on 19-throws position
                        oldCaretPosition = 18;
                        setCaretPosition(oldCaretPosition);
                }
            }
        }
    });
}


/** listen to editor's propertyChange event */
public void propertyChange(PropertyChangeEvent evt) {
    // oldPropertyValue is changed only from outside
    oldPropertyValue = editor.getValue();
    getDocument().removeDocumentListener( documentListener );
    setText(editor.getAsText());
    getDocument().addDocumentListener( documentListener );
}


public void sync() throws SyncException {
    editor.removePropertyChangeListener(MjdEditorPanel.this);
    try {
        editor.setAsText(getText());
    } catch (IllegalArgumentException e2) {
        editor.addPropertyChangeListener(MjdEditorPanel.this);
        throw new SyncException(this, e2);
    }
    editor.addPropertyChangeListener(MjdEditorPanel.this);
}

}

class TimeDocument extends DefaultStyledDocument {

    public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {
        //ovt.util.Log.log("insertStr("+offs+","+str+")");
        int p1 = Math.min(getLength(), offs + str.length());
        //String newString = getText(0, offs) + str + getText( p1, getLength());
        //ovt.util.Log.log("newstr = '"+newString+"'");
        if ( isValidStr(str,offs)) {
            
            super.remove(offs, p1 - offs);
            super.insertString(offs, str, a);
        } else
            Toolkit.getDefaultToolkit().beep();
    }
    
    /** Ovverride the method to do nothing ;-) */
    public void remove(int offs, int len) {
    }
    
    private static boolean isValidStr(String str, int offs){
      if (str != null ) {
        for (int i=0; i<str.length(); i++)
            if (!isValidChar(str.charAt(i), offs+i)) return false;
      }
      return true;
    }
    
    private static boolean isValidChar(char ch, int offs) {
        //ovt.util.Log.log("isValidChar(char="+ch+",offs="+offs+")");
        switch (offs) {
                case 0  : return (ch == '1' || ch == '2'); // year 
                          
                case 1  : return (ch == '0' || ch == '9');
                          
                case 2  : return Character.isDigit(ch);
                          
                case 3  : return Character.isDigit(ch);
                          
                case 4  : return (ch == '-');
                          
                case 5  : return (ch == '0' || ch == '1'); // month
                          
                case 6  : return Character.isDigit(ch);
                          
                case 7  : return (ch == '-');
                          
                case 8  : return (ch == '0' || ch == '1' || ch == '2' || ch == '3'); // day 
                          
                case 9  : return Character.isDigit(ch);
                          
                case 10 : return (ch == ' ');
                          
                case 11 : return (ch == '0' || ch == '1' || ch == '2'); // hour
                          
                case 12 : return Character.isDigit(ch);
                          
                case 13 : return (ch == ':');
                          
                case 14 : return (ch == '0' || ch == '1' || ch == '2' || ch == '3' ||
                                  ch == '4' || ch == '5'); // minute
                          
                case 15 : return Character.isDigit(ch);
                          
                case 16 : return (ch == ':');
                          
                case 17 : return (ch == '0' || ch == '1' || ch == '2' || ch == '3' ||
                                  ch == '4' || ch == '5'); // sec
                          
                case 18 : return Character.isDigit(ch);
                             
        }
        return false;
        //throw new IllegalArgumentException("Invalid offset ("+offs+")");
    }
}

/*
*  Paint a horizontal line the width of a column and 1 pixel high
*/


class OvertypeCaret extends DefaultCaret
{
        /*
                                 *  The overtype caret will simply be a horizontal line one pixel high
         *  (once we determine where to paint it)
         */
        public void paint(Graphics g)
        {
                if (isVisible())
                {
                        try
                        {
                                JTextComponent component = getComponent();
                                TextUI mapper = component.getUI();
                                Rectangle r = mapper.modelToView(component, getDot());
                                g.setColor(component.getCaretColor());
                                int width = g.getFontMetrics().charWidth( '0' );
                                int y = r.y + r.height - 2;
                                g.drawLine(r.x, y, r.x + width - 1, y);
                        }
                        catch (BadLocationException e) {}
                }
        }
        /*
         *  Damage must be overridden whenever the paint method is overridden
         *  (The damaged area is the area the caret is painted in. We must
         *  consider the area for the default caret and this caret)
         */
        protected synchronized void damage(Rectangle r)
        {
                if (r != null)
                {
                        JTextComponent component = getComponent();
                        x = r.x;
                        y = r.y;
                        width = component.getFontMetrics( component.getFont() ).charWidth( 'w' );
                        height = r.height;
                        repaint();
                }
        }
}
