/*
 * TimeSet.java
 *
 * Created on March 24, 2001, 3:32 PM
 */

package ovt.interfaces;

/**
 *
 * @author  ko
 * @version 
 */
public interface TimeSetSource extends TimePeriodSource {

//  public double getIntervalMjd();
  
  public double getStepMjd();
  
  public double getCurrentMjd();
    
}

