package ovt.util;

import gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import ovt.datatype.Time;

/**
 * Abstract class for implementations that return data from SSC Web Services, or
 * emulate doing so. This implementation (division into classes) is chosen to
 * make automated testing easier. One can thus easily substitute SSC Web
 * Services as a source of data for customized code that returns its own
 * (presumably hardcoded) data. One can thus easily test all code that relies on
 * SSC Web Services data (1) without relying on SSC being online or the local
 * internet working, and (2) enjoy faster response times meaning faster testing.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
// PROPOSAL: Change to model where satellite descriptions are (technically) not
//           cached, but can be refreshed with a special command at any point.
//           Can refresh list at instantiation and then never bother again, except
//           possibly wth button in the SSCWS Satellites list window.
//    PRO: No IOException for getAllSatelliteInfo(...) (two functions).
// 
public abstract class SSCWSLibrary {

    /**
     * Used for creating instances of XMLGregorianCalendar. Can not be assigned
     * here since DatatypeFactory.newInstance() throws an exception. Can/should
     * not be initialized in a static initialization block for the same reason.
     */
    private static DatatypeFactory datatypeFactory = null;

    //##########################################################################
    /**
     * Stores basic satellite information. Immutable.
     *
     * IMPLEMENTATION NOTE: In practice an immutable replacement for
     * gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription. Reasons for having
     * this class instead of SatelliteDescription: (1) Reduce the number of
     * locations in OVT which are dependent on SatelliteDescription (since it
     * does stem from an external package), (2) it is immutable, as opposed to
     * SatelliteDescription, (3) it can store five+ different variables as one
     * single variable but still have fast access to availableBegin/EndTimeMjd
     * (does not need to call convertXMLGregorianCalendarToMjd).
     *
     * NOTE: The class does not yet have new implementations of Object#equals,
     * and Object#hashCode yet, making it unsuitable for use in many Java
     * standard collections classes.
     */
    public static class SSCWSSatelliteInfo implements Serializable {

        public final String ID;
        public final String name;
        public final double availableBeginTimeMjd;
        public final double availableEndTimeMjd;
        public final int bestTimeResolution;    // Unit: seconds, not mjd!


        public SSCWSSatelliteInfo(SatelliteDescription satDescr) {
            this.ID = satDescr.getId();
            this.name = satDescr.getName();
            this.availableBeginTimeMjd = SSCWSLibrary.convertXMLGregorianCalendarToMjd(satDescr.getStartTime());
            this.availableEndTimeMjd = SSCWSLibrary.convertXMLGregorianCalendarToMjd(satDescr.getEndTime());
            this.bestTimeResolution = satDescr.getResolution();
        }


        /**
         * Constructor that is useful for test code.
         */
        public SSCWSSatelliteInfo(String mID, String mName, double mAvailableBeginTimeMjd, double mAvailableEndTimeMjd, int mNormalTimeResolution) {
            if (mID == null) {
                throw new NullPointerException("mID is null.");
            } else if (mName == null) {
                throw new NullPointerException("mName is null.");
            }
            this.ID = mID;
            this.name = mName;
            this.availableBeginTimeMjd = mAvailableBeginTimeMjd;
            this.availableEndTimeMjd = mAvailableEndTimeMjd;
            this.bestTimeResolution = mNormalTimeResolution;
        }


        @Override
        public boolean equals(Object o) {
            throw new RuntimeException("Method not supported yet.");
        }


        @Override
        public int hashCode() {
            throw new RuntimeException("Method not supported yet.");
        }
    }

    //##########################################################################

    /**
     * Retrieve (unmodifiable) list of satellite descriptions.
     *
     * NOTE: Implementations of the function are supposed to return identical
     * lists for every successful call. That means the satellite list can never
     * change or be updated during a session (except by creating a new
     * SSCWSLibraryImpl object, but one is not supposed to do that). OVT assumes
     * this behaviour and can therefore presently NOT use the MVC pattern for the
     * satellite list. Change this?
     */
    public abstract List<SSCWSSatelliteInfo> getAllSatelliteInfo() throws IOException;


    /**
     * NOTE: Returns private immutable instance, not a copy.
     */
    public final SSCWSSatelliteInfo getSatelliteInfo(String satID) throws IOException {

        if (satID == null) {
            throw new NullPointerException("satID is null.");
        }
        for (SSCWSSatelliteInfo satInfo : getAllSatelliteInfo()) // throws IOException
        {
            if (satInfo.ID.equals(satID)) {
                return satInfo;
            }
        }
        throw new IOException("Could not find SSC Web Services satellite description for satellite ID = \"" + satID + "\".");
    }


    /**
     * Return trajectory data for a given Earth satellite.<BR>
     *
     * NOTE: One can NOT assume that the first and last coordinate points are
     * exactly at beginMjd/endMjd since (1) the specified time interval may
     * extend beyond the length of the entire time series, (2) there may be data
     * gaps, and (3) coordinates are only given for specific points in time
     * specified by SSC Web Services anyway (i.e. there is a brief "data gap"
     * between any two data points).
     *
     * @param satID String referring to a satellite as returned by
     * gov.nasa.gsfc.spdf.ssc.client.SatelliteDescription.getID().
     *
     * @return 2D array where the indices refer to [X/Y/Z/time][position index].
     * Units: km, mjd.
     */
    // PROPOSAL: Return empty arrays when there is no data.
    //    NOTE: SSC Web Services returns error when requesting data from entirely outside the available time interval.
    // PROPOSAL: Remove check for coordinate system of returned data.
    //
    // TODO: Check which coordinate system to use: CoordinateSystem.GEI_J_2000, or .GEI_TOD or some other which is unambiguous.
    // TODO: formatOptions.setDistanceUnits(DistanceUnits.KM); or at least check with units are actually returned.
    public abstract double[][] getTrajectory_GEI(
            String satID,
            double beginMjdInclusive,
            double endMjdInclusive,
            int resolutionFactor)
            throws IOException;


    public abstract List<String> getPrivacyAndImportantNotices() throws IOException;


    public abstract List<String> getAcknowledgements() throws IOException;


    // PROPOSAL: Move to Utils or Time?
    //    CON: Is the time conversion used by data from the SSC Web Servies. Therefore one wants to keep it close to that code.
    /**
     * Convert from XMLGregorianCalendar to modified Julian Day (mjd).<BR>
     *
     * NOTE: XMLGregorianCalendar is used by the SSC Web Services.<BR>
     * NOTE: Ignores leap seconds.
     *
     * @see Time#getMjd(int year, int month, int day, int hour, int mins, double
     * sec) for the details of time conversion.
     */
    public static double convertXMLGregorianCalendarToMjd(XMLGregorianCalendar cal) {
        /* NOTE: XMLGregorianCalendar#getMillisecond() is not always defined.
         Code must handle this case since the value in that case is
         XMLGregorianCalendar#getMillisecond() == DatatypeConstants.FIELD_UNDEFINED = -2^31 << 0
         which gives an error of ca -25 days. */
        double seconds;
        if (cal.getMillisecond() == DatatypeConstants.FIELD_UNDEFINED) {
            seconds = 0;
        } else {
            seconds = cal.getSecond() + cal.getMillisecond() / 1000.0;
        }
        return Time.getMjd(
                cal.getYear(),
                cal.getMonth(),
                cal.getDay(),
                cal.getHour(),
                cal.getMinute(),
                seconds);
    }


    // Move to Utils or Time?
    /**
     * Convert from modified Julian Day (mjd) to XMLGregorianCalendar.<BR>
     *
     * NOTE: XMLGregorianCalendar is used by the SSC Web Services.<BR>
     * NOTE: Ignores leap seconds.
     *
     * @see Time#getMjd(int year, int month, int day, int hour, int mins, double
     * sec)
     */
    public static XMLGregorianCalendar convertMjdToXMLGregorianCalendar(double mjd) throws DatatypeConfigurationException {
        if (datatypeFactory == null) {
            //try {
            datatypeFactory = DatatypeFactory.newInstance();   // throws DatatypeConfigurationException
            //} catch(DatatypeConfigurationException e) {
            //throw new RuntimeException("Call to DatatypeFactory.newInstance() failed.", e);
            //}
        }

        final Time time = new Time(mjd);
        final int seconds = (int) Math.floor(time.getSeconds());
        final int milliseconds = (int) Math.round(time.getSeconds() - seconds);

        return datatypeFactory.newXMLGregorianCalendar(
                time.getYear(),
                time.getMonth(),
                time.getDay(),
                time.getHour(),
                time.getMinutes(),
                seconds,
                milliseconds,
                0);   // Last parameter refers to time zone.
    }


    /**
     * Informal test code.
     */
    public static void main(String[] args) throws DatatypeConfigurationException {
        final double mjd1 = Time.getMjd(2015, 4, 9, 00, 00, 00);   // 23839
        final XMLGregorianCalendar xgc = convertMjdToXMLGregorianCalendar(mjd1);   // 2015-04-09 00:00:00
        final double mjd2 = convertXMLGregorianCalendarToMjd(xgc);   // 23839
        final Time time = new Time(mjd2);   // 2015-04-09 00:00:1.04773789...E-7
    }

}
