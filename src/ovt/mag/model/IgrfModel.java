/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/IgrfModel.java,v $
 Date:      $Date: 2001/06/21 14:17:41 $
 Version:   $Revision: 2.1 $


 =========================================================================*/
package ovt.mag.model;

import java.io.*;
import java.util.*;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.mag.model.GandHcoefs;

/**
 * NOTE: Has a highest limit to how high n it can read from the IGRF spherical
 * harmonics. Will ignore higher ones!! One can not trivially raise the limit.
 *
 * NOTE: The class caches the IGRF g & h coefficients in bad ways which make the
 * cached data only approximately correct: <BR>
 * (1) The cache contains one set of coefficients for every integer year,<BR>
 * but<BR>
 * (2) derived (interpolated/extrapolated) data in the cache are derived for a
 * non-integer year (approximate month-resolution) which may vary from request
 * to request (for the same year), and <BR>
 * (3) whether to use cached data or not is partly determined by whether the new
 * time is sufficiently close (max N days difference) to the previous one, not
 * whether the new time is within the same year or within the same approximate
 * month.<BR>
 * /Erik P G Johansson 2015-10-30 (who did not write the class)
 *
 * NOTE: MINOR BUG in "void setIgrf(float floatYear)" related to caching. See
 * documentation at/in the method.<BR>
 * /Erik P G Johansson 2015-10-30 (who did not write the class)
 *
 * @author root
 * @version
 */
public class IgrfModel extends AbstractMagModel {

    protected String igrfDatFileName = OVTCore.getMdataSubdir() + "igrf.d";
    //public final static int ERROR_YEAR = -10000;
    //protected int year = ERROR_YEAR;    // Never used? Only assigned?!

    /**
     * Some form of representation of (or at values least derived from) the IGRF
     * g & h coefficients for the time "mjdPrev". NOTE: Apparently only read in
     * double[] igrf(double[] geo).
     */
    public static double Gh[] = new double[144];
    // maximum no of harmonics in igrf
    protected final static int Nmax = 10;          // Can not read higher n than this. Will ignore higher ones!!!

    // Excentric dipole coordinates derived from Gh
    protected static double Eccrr[] = {-0.0625, 0.0405, 0.0281};
    protected static double Eccdx[] = {0.3211, -0.9276, -0.1911};
    protected static double Eccdy[] = {0.9450, 0.3271, 0.0000};
    /**
     * Coordinates of z-axis of Excentric dipole derived from Gh. It is dipole
     * vector.
     */
    protected static double Eccdz[] = {0.0625, -0.1806, 0.9816};

    /**
     * Stores IGRF g & h coefficients for integer years, including years found
     * in data file (multiples of five) and interpolated years in between.
     */
    private static final Hashtable ghTable = new Hashtable();
    private static int minY, maxY;    // Years limits
    private static final GandHcoefs ghSVCoefficients = new GandHcoefs(Nmax);   // Secular variation (SV) coefficients. Previously called "addCol".
    private static boolean hasReadGHSVCoefficients = false;   // Flag for whether ghSVCoefficients has already been filled with values.
    private static double mjdPrev = -100000.0;
    private static final double INVALIDATION_TIME_MARGIN_DAYS = 31.0;
    private static final int EXTRAPOLATION_WARNING_THRESHOLD_YEARS = 5;
    private boolean hasShownExtrapolationWarning = false;


    /**
     * Creates new Igrf.
     */
    public IgrfModel(MagProps magProps) {
        super(magProps);
    }


    @Override
    public double[] bv(double[] gsm, double mjd) {
        setIGRF(mjd);
        // get transformation class
        Trans trans = getTrans(mjd);
        double[] geo = trans.gsm2geo(gsm);
        double[] bb = trans.geo2gsm(igrf(geo));
        return bb;
    }


    //Returns mag. field in GEO CS
    public double[] bvGEO(double[] geo, double mjd) {
        setIGRF(mjd);
        return igrf(geo);
    }


    // Returns the year, for which IGRF coefficients are valid.
/* ------------------------------------------------------------ 
     FUNCTION: 
     compute igrf field for cartesian geo
     input: 
     geo(3) position vector (geo) in earth radii (re = 6371.2 km)
     output: 
     bv(3)  magnetic field vector in geo (units as set by setigrf)
     files/COMMONs:
     COMMON /cigrf/ with coefficients set by setigrf(mjd)
     remarks: 
     CALL setigrf(mjd) before first use 
     --------------------------------------------------------------- */
    protected double[] igrf(double[] geo) {

        // Local variables
        int imax, nmax;
        double f, h[] = new double[144];
        int i, k, m;
        double s, x, y, z;
        int ihmax, ih, il;
        double xi[] = new double[3], rq;
        int ihm, ilm;
        double srq;
        int j;
        double bv[] = new double[3];     // - output

        rq = Vect.absv2(geo);

        if (rq < .8) {
            System.out.println("IGRF call for position below Earth surface.");
        }

        rq = 1. / rq;
        srq = Math.sqrt(rq);
        if (rq < 0.25) {
            nmax = (int) ((Nmax - 3) * 4.0 * rq + 3);
        } else {
            nmax = Nmax;
        }

        // number of harmonics depends on the distance from the earth
        //for (d1 = xi, d2 = geo; d1 < xi + 3; )
        //    *d1++ = *d2++ * rq;
        for (j = 0; j < 3; j++) {
            xi[j] = geo[j] * rq;
        }

        ihmax = nmax * nmax;
        imax = nmax + nmax - 2;
        il = ihmax + nmax + nmax;

//    d1 = h + ihmax;
//    d2 = Gh + ihmax;
//    for ( ; d1 <= h + il; )
//        *d1++ = *d2++;
        for (j = ihmax; j < il; j++) {
            h[j] = Gh[j];
        }

        for (k = 0; k < 3; k += 2) {
            i = imax;
            ih = ihmax;
            while (i >= k) {
                il = ih - i - 1;
                f = 2. / (double) (i - k + 2);
                x = xi[0] * f;
                y = xi[1] * f;
                z = xi[2] * (f + f);
                i += -2;
                if (i >= 2) {
                    for (m = 3; m <= i + 1; m += 2) {
                        ihm = ih + m;
                        ilm = il + m;
                        h[ilm + 1] = Gh[ilm + 1] + z * h[ihm + 1] + x * (h[ihm + 3] - h[ihm - 1])
                                - y * (h[ihm + 2] + h[ihm - 2]);
                        h[ilm] = Gh[ilm] + z * h[ihm] + x * (h[ihm + 2] - h[ihm - 2])
                                + y * (h[ihm + 3] + h[ihm - 1]);
                    }
                    h[il + 2] = Gh[il + 2] + z * h[ih + 2] + x * h[ih + 4]
                            - y * (h[ih + 3] + h[ih]);
                    h[il + 1] = Gh[il + 1] + z * h[ih + 1] + y * h[ih + 4]
                            + x * (h[ih + 3] - h[ih]);
                } else if (i == 0) {
                    h[il + 2] = Gh[il + 2] + z * h[ih + 2] + x * h[ih + 4]
                            - y * (h[ih + 3] + h[ih]);
                    h[il + 1] = Gh[il + 1] + z * h[ih + 1] + y * h[ih + 4]
                            + x * (h[ih + 3] - h[ih]);
                }

                h[il] = Gh[il] + z * h[ih] + (x * h[ih + 1] + y * h[ih + 2]) * 2.;
                ih = il;
            }
        }

        s = h[0] * .5 + (h[1] * xi[2] + h[2] * xi[0] + h[3] * xi[1]) * 2.;
        f = (rq + rq) * srq;
        x = f * (h[2] - s * geo[0]);
        y = f * (h[3] - s * geo[1]);
        z = f * (h[1] - s * geo[2]);
        bv[0] = x;
        bv[1] = y;
        bv[2] = z;
        return bv;
    }


    public double[] getEccrr(double mjd) {
        setIGRF(mjd);
        return Eccrr;
    }


    public double[] getEccdx(double mjd) {
        setIGRF(mjd);
        return Eccdx;
    }


    public double[] getEccdy(double mjd) {
        setIGRF(mjd);
        return Eccdy;
    }


    public double[] getEccdz(double mjd) {
        setIGRF(mjd);
        return Eccdz;
    }


    /**
     * Initializing GH coefs for year #year
     */
    public static void initHashTable(File dataFile, int year)
            throws /*FileNotFoundException,*/ IOException {
        if (!ghTable.containsKey(year)) {
            initHashTableFromFile(dataFile, year, false);
        }
    }


    /**
     * Read _some_ data from the IGRF data file (text table). Either (1) read
     * the column headers to determine the span of years covered, or (2) read
     * ONE specified year from IGRF data file and put the information in the
     * IgrfModel.ghTable. Make sure that IgrfModel.ghSVCoefficients contains the
     * corresponding information for the column for secular variation.
     *
     * NOTE: The way of handling errors is not that great. Should ideally be
     * translated into error messages for the user (and block the change of
     * time) but not sure of a good way to do this.<BR>
     * /Erik P G Johansson 2015-10-29 (who did not write the method)
     *
     * NOTE: This code reads data file "igrf.d" but it is not the only code in
     * OVT to do so. libovt/magpack.c also reads "igrf.d". Therefore, the file
     * format (of "igrf.d") implicitly defined by the code here is not
     * automatically the same as that implicitly defined by the code in
     * libovt/magpack.c. /Erik P G Johansson 2015-10-29 (who did not write the
     * method)
     *
     * @param year Iff initHeader==true, then must be divisible by five and
     * within the range minY-maxY. Iff initHeader==false, then its value is
     * irrelevant.
     *
     * @param initHeader Iff true, only read file header (first row) and
     * initialize minY, maxY, and nothing else. Iff false, then read a specific
     * year's data (and read the secular variation if not already done).
     *
     * @throws IOException for misc. I/O errors, file format errors, AND invalid
     * year (not present in file).
     */
    public static void initHashTableFromFile(File dataFile, int year, boolean initHeader)
            throws IOException {
        int i_column, m_idx = -1, n_idx = -1;
        char ghMarker = '\0';
        float flt = 0.0F;
        final String INVALID_FILE_FORMAT_EXCEPTION_MSG = "Invalid format of IGRF data file, \"" + dataFile.getCanonicalPath() + "\".";
        final BufferedReader inData;
        String str;
        final GandHcoefs ghCoefs = new GandHcoefs(Nmax);  // for Hashtable

        try {
            inData = new BufferedReader(new FileReader(dataFile));
        } catch (NullPointerException | FileNotFoundException e) {
            throw new IOException("File " + dataFile + " not found.");
        }

        str = inData.readLine();

        if (initHeader) {
            //====================================================================
            // Read header (column names) and initialize this.minY and this.maxY.
            // Then exit.
            //====================================================================
            final StringTokenizer hdTok = new StringTokenizer(str);
            i_column = 0;
            while (hdTok.hasMoreTokens()) {
                ++i_column;          // Skipping the first three "g/h n m" fields.
                final String tempStr = hdTok.nextToken();
                if (i_column == 4) {
                    minY = (int) Double.parseDouble(tempStr);
                }
                if ((i_column >= 4) && (!tempStr.contains("-"))) {
                    /* Read year from column.
                     * NOTE: Incomplete preparation for intepreting igrf.d files that are formatted more like
                     * the official IGRF files:<BR>
                     * The last column header (for the column with secular variation?) may have a
                     * column header designating a year interval, e.g. 2015-20, or
                     * may entirely lack a column header (i.e. token).
                     * Must therefore be prepared for that the last token might not be
                     * usable, and that the second-last one should be used.
                     */
                    maxY = (int) Double.parseDouble(tempStr);
                }
            }

            if (minY >= maxY) {
                throw new IOException(INVALID_FILE_FORMAT_EXCEPTION_MSG + " Derived start year is greater than the derived end year.");
            }

            return;    // NOTE: EXIT and do nothing more!!
        }

        /*==============================================================
         * Argument check: Check that file has the corresponding column.
         * -------------------------------------------------------------
         * NOTE: Code checks for year divisible by five since the data file source
         * only contains data for every even five years.
         ==============================================================*/
        if ((year % 5) != 0 || (year < minY) || (maxY < year)) {
            final String msg = "Can not read year from IGRF data file, \"" + dataFile.getCanonicalPath() + "\"."
                    + " The specified year (year=" + year + ") for this function is either (1) outside the allowed"
                    + " interval " + minY + "-" + maxY + " for which there is data in the IGRF data file, or (2) not divisible by 5.";
            throw new IOException(msg);
        }

        // Is this year in Hashtable?
        if (ghTable.containsKey(year)) {
            return;
        }

        //===================================
        // Read g & h coeffs. for year ##year
        //===================================
        final int neededCol = 4 + (year - minY) / 5;          // Definition of needed column
        while (inData.ready()) {  // Iterate over rows in file.
            str = inData.readLine();
            if (str == null) {
                break;
            }
            final StringTokenizer tokGH = new StringTokenizer(str);
            i_column = 0;                   // Number of parsed columns
            while (tokGH.hasMoreTokens()) {  // Parsing one row in file. - Iterate over tokens on row.
                ++i_column;
                final String tmps = tokGH.nextToken();
                switch (i_column) {
                    case 1:                  // g/h marker
                        char tmpc[] = tmps.toCharArray();
                        ghMarker = tmpc[0];
                        break;
                    case 2:                  // getting n index
                        n_idx = Integer.parseInt(tmps);
                        break;
                    case 3:                  // getting m index
                        m_idx = Integer.parseInt(tmps);
                        break;
                }
                if (i_column == neededCol) {           // Found the needed column!
                    flt = Float.parseFloat(tmps);

                    if (n_idx > Nmax || m_idx > Nmax || n_idx < 0 || m_idx < 0) {
                        // NOTE: Best to give proper error message since it is not
                        // obvious that there is an upper limit to n.
                        throw new IOException(INVALID_FILE_FORMAT_EXCEPTION_MSG + " Can not interpret n and/or m indices. (Can e.g. only read up to n=" + Nmax + ")");
                    }

                    switch (ghMarker) {
                        case 'g':
                            ghCoefs.setGcoefs(n_idx, m_idx, flt);
                            break;
                        case 'h':
                            ghCoefs.setHcoefs(n_idx, m_idx, flt);
                            break;
                        default:
                            throw new IOException(INVALID_FILE_FORMAT_EXCEPTION_MSG);
                    }
                } else if (!tokGH.hasMoreTokens()) {  // Is last column?
                    if (hasReadGHSVCoefficients) // ghSVCoefficients already loaded
                    {
                        break;                // goto the next line
                    } else {                   // loading ghSVCoefficients
                        flt = Float.parseFloat(tmps);
                        switch (ghMarker) {
                            case 'g':
                                ghSVCoefficients.setGcoefs(n_idx, m_idx, flt);
                                break;
                            case 'h':
                                ghSVCoefficients.setHcoefs(n_idx, m_idx, flt);
                                break;
                            default:
                                throw new IOException(INVALID_FILE_FORMAT_EXCEPTION_MSG);
                        }
                    }
                }

            }
            // CASE: Iterated over all tokens/columns
            if (i_column < neededCol) {
                throw new IOException(INVALID_FILE_FORMAT_EXCEPTION_MSG);
            }
        }
        inData.close();

        // Putting year #yy (key) & GH coefs. into hash table
        ghTable.put(year, ghCoefs);

        if (!hasReadGHSVCoefficients) {
            hasReadGHSVCoefficients = true;
        }
    }


    /**
     * Set the current time for the IGRF model data.
     *
     * NOTE: Not to be confused with setIgrf(float floatYear). NOTE: The code
     * uses the previous time used for calculating IGRF to determine whether to
     * obtain new IGRF coefficients but the cache stores data once for every
     * year. Inconsistent.
     */
    protected void setIGRF(double mjd) {
        if (Math.abs(IgrfModel.mjdPrev - mjd) <= INVALIDATION_TIME_MARGIN_DAYS) {
            return;  // Do nothing if the already used time is sufficiently close to the requested one.
        }
        IgrfModel.mjdPrev = mjd;

        // Calculate (approximate) year as decimal number.
        // NOTE: Quite approximate since it ROUNDS UP to (approximately) nearest month.
        // E.g. 2010-01-01, 00:00.00 ==> floatYear=2010.08333 (2010+1/12).
        final Time timeTmp = new Time(mjd);
        final float floatYear = (float) timeTmp.getYear() + (float) timeTmp.getMonth() / 12.0f;
        setIgrf(floatYear);
    }


    /*
     * Sets up coefficients <code>Gh</code> for magnetic field computation 
     * and position of the eccentric dipole (re)
     *   <code>Eccrr</code>, <code>Eccdx</code>, <code>Eccdy</code>, <code>Eccdz</code>
     *
     * NOTE: Not to be confused with setIGRF(double mjd).
     * NOTE: Code is only able to extrapolate forward in time, not backward.
     * 
     * NOTE: Can send/display warning message.
     *  
     * NOTE: MINOR BUG -
     * The g & h coefficients are interpolated to a non-integer year ("floatYear"),
     * but the result is still stored in the cache with an entry which implies that
     * the data is valid for an integer year (intYear)!
     * This implies that the g & h coefficients (and hence GSM coordinates?)
     * for a given year may be slightly different for different OVT sessions
     * depending on which dates in a given year triggered filling the cache entry for that year.
     * Note that IGRF data for years in the IGRF data file (igrf.d) are not overwritten
     * since years already present in the cache are never overwritten.
     * /Erik P G Johansson 2015-10-29 (who did not write the code)
     *
     * @see #Gh #Eccrr #Eccdx #Eccdy #Eccdz
     */
    protected void setIgrf(float floatYear) {

        final int intYear = (int) floatYear;

        //GandHcoefs ghFloor = new GandHcoefs(Nmax);   // g & h coefficients for year floorY.
        //GandHcoefs ghCeil = new GandHcoefs(Nmax);    // g & h coefficients for year ceilY, OR(!) secular variation (year^-1) for g & h coefficients.
        final GandHcoefs gANDh = new GandHcoefs(Nmax);
        float w1a = 0.0F, w2a = 0.0F;  // Constants for interpolation (weights) or extrapolation. Initial values are used in case of exception (initHashTable).

        try {
            final File igrfDatFile = Utils.findFile(igrfDatFileName);
            if (!hasReadGHSVCoefficients) {     // Starting for the first time
                initHashTableFromFile(igrfDatFile, 0, true);   // "Initialize cache".
            }

            // Derive floorY and ceilY (both divisible by five) defining
            // a five-year interval containing "year" (and "floatYear").
            int floorY = (int) (intYear / 10) * 10;
            //if((intYear-floorY)>5) { // NOTE: Years XXX[0-5] (six-year interval) ==> floorY=XXX0, whereas years XXX[6-9] (four-year interval) ==> floorY=XXX5
            if ((intYear - floorY) >= 5) {
                floorY += 5;
            }
            final int ceilY = floorY + 5;
            // Make sure floorY refers to a year for which there is IGRF file data
            // (not previously extrapolated data). This is important to make it
            // possible to interpolate beyond five years after the last IGRF data
            // year (maxY).
            floorY = Math.min(maxY, floorY);

            // Request g & h coefficients for year "floorY".
            // NOTE: Should ideally be sure that one only reads data that originates from the IGRF data file.
            initHashTable(igrfDatFile, floorY);
            final GandHcoefs ghFloor = (GandHcoefs) ghTable.get(floorY);

            final GandHcoefs ghCeil;  // g & h coefficients for year ceilY, OR(!) secular variation (year^-1) for g & h coefficients.
            // Assign ghCeil, and construct constants used for interpolation or extrapolation.
            if (ceilY <= maxY) {
                // CASE: Prepare for interpolation. We do not use the additional column (secular variation).
                initHashTable(igrfDatFile, ceilY);   // Requesting CEIL year
                ghCeil = (GandHcoefs) ghTable.get(ceilY);
                w1a = ((float) ceilY - floatYear) / (float) (ceilY - floorY);
                w2a = 1.0F - w1a;
            } else {
                // CASE: Last additional column (secular variation) has been used. Prepare for extrapolation.
                w1a = 1.0F;
                w2a = floatYear - (float) floorY;   // Unit: years
                ghCeil = ghSVCoefficients;    // Using ghSVCoefficients (secular variation). Unit year^-1.

                if ((w2a > EXTRAPOLATION_WARNING_THRESHOLD_YEARS) & (!hasShownExtrapolationWarning)) {

                    // NOTE: Command will automatically not display warning dialog window
                    // during launch when there is no GUI present.
                    // NOTE: "This warning will not be displayed again" is technically wrong for putting in the log.
                    this.magProps.getCore().sendWarningMessage(
                            "Extrapolating IGRF data",
                            "Extrapolating " + (int) w2a + " years"
                            + " into the future from the last year with data (" + floorY + ")"
                            + " in \"" + igrfDatFile.getCanonicalPath() + "\"."
                            + "\nThis warning will not be displayed again.");
                    if (OVTCore.isGuiPresent()) {
                        hasShownExtrapolationWarning = true;
                    }
                }//*/
            }

            // Compute coefficients Gij & Hij for time "floatYear" (non-integer).
            // ------------------------------------------------------------------
            // Either
            // (1) INTERPOLATE between ghFloor (year floorY) and ghCeil (year ceilY), or
            // (2) EXTRAPOLATE from ghFloor (year floorY) using ghCeil=ghSVCoefficients
            // depending on how w1a, w2a have been set.
            for (int i = 0; i <= Nmax; ++i) {
                for (int j = 0; j <= i; ++j) {
                    final float gg = w1a * ghFloor.getGcoefs(i, j) + w2a * ghCeil.getGcoefs(i, j);
                    final float hh = w1a * ghFloor.getHcoefs(i, j) + w2a * ghCeil.getHcoefs(i, j);
                    gANDh.setGHcoefs(i, j, gg, hh);
                }
            }

            // Store calculated g & h coefficients in cache.
            // ---------------------------------------------
            // NOTE / MINOR BUG?: These coefficients are interpolated for the non-integer year ("floatYear"),
            // but still the entry in the cache implies that they are valid for an integer year (intYear)!!
            // See the notice/documentation for method.
            // /Erik P G Johansson 2015-10-29 (who did not write the code)
            // 
            // NOTE: This is the same cache that initHashTableFromFile uses.
            // 
            // IMPLEMENTATION NOTE: Important that values are not cached if there was an error     
            // since the error message will only be triggered when the data is derived,
            // but NOT when the faulty data is later obtained from the cache, and NOT
            // if the data is later used to interpolate other years. The latter should not
            // be able to happen but has happened anyway in the past due to bugs.
            // PAST BUG (2015-10-29):
            // Method argument requested year 2016 ==> floorY=2015
            // ==> 2015 not in data file (1980-2010) ==> Exception (initHashTable)
            // ==> Coefficients=zero stored in cache for 2015.
            // ==> Extrapolations made for e.g. 2017 using cached 2015 data (and secular variation from 2010+).
            // /Erik P G Johansson 2015-10-29 (who did not write the method)
            if (!ghTable.containsKey(intYear)) {
                ghTable.put(intYear, gANDh);
                //ghTable.put(floatYear, gANDh);   // Non-functioning bugfix: Code needs to read float year from cache too.
            }

        } catch (IOException e) {
            // NOTE: Bad way of handling exception but not sure of what would be better.
            // If failure to read IGRF data file due to using a high year, then 
            // it will/should lead to all coefficients being zero. ==> Dipole direction and GSM undefined.
            // NOTE: Error should preferably lead to erronoues g & h values not being stored in the cache at least(?).
            e.printStackTrace();
            System.out.println(e);
        }

        // Calculating (recalculating) Gh
        float f, f0;
        int d1, d2, k;
        f0 = -1.0F;         // -1.0e-5  for output in gauss
        Gh[0] = 0.0F;
        k = 2;
        for (int i = 1; i <= Nmax; ++i) {
            f0 *= 0.5 * (float) i;
            f = f0 / 1.4142136F;    // Divide by sqrt(2.0).
            d1 = i + 1;
            d2 = 1;
            Gh[k - 1] = f0 * gANDh.getGcoefs(d1 - 1, d2 - 1);
            ++k;
            for (int j = 1; j <= i; ++j) {
                final float tmp1 = (float) (i + j);
                final float tmp2 = (float) (i - j + 1);
                f *= Math.sqrt(tmp1 / tmp2);
                d1 = i + 1;
                d2 = j + 1;
                Gh[k - 1] = f * gANDh.getGcoefs(d1 - 1, d2 - 1);
                Gh[k] = f * gANDh.getHcoefs(d1 - 1, d2 - 1);
                k += 2;
            }
        }
     //this.year=year;

        // Calculating (recalculating) d?,Eccrr, ...
        final double h0
                = gANDh.getGcoefs(1, 0) * gANDh.getGcoefs(1, 0)
                + gANDh.getGcoefs(1, 1) * gANDh.getGcoefs(1, 1)
                + gANDh.getHcoefs(1, 1) * gANDh.getHcoefs(1, 1);
        final double dipmom = -Math.sqrt(h0);
        final double w1 = Math.abs(gANDh.getGcoefs(1, 0) / dipmom);
        final double w2 = Math.sqrt(1.0 - w1 * w1);
        final double tmp1d = Math.atan(gANDh.getHcoefs(1, 1) / gANDh.getGcoefs(1, 1));
        Eccdz[0] = w2 * Math.cos(tmp1d);
        Eccdz[1] = w2 * Math.sin(tmp1d);
        Eccdz[2] = w1;
        Eccdx[0] = Eccdx[1] = 0.0;
        Eccdx[2] = 1.0;

        Vect.crossn(Eccdx, Eccdz, Eccdy);
        Vect.crossn(Eccdy, Eccdz, Eccdx);

        //Excentric dipole (Chapman & Bartels, 1940)
        final float sqrt3 = 1.7320508F;

        final double lx = -gANDh.getGcoefs(1, 1) * gANDh.getGcoefs(2, 0)
                + sqrt3 * (gANDh.getGcoefs(1, 0) * gANDh.getGcoefs(2, 1)
                + gANDh.getGcoefs(1, 1) * gANDh.getGcoefs(2, 2)
                + gANDh.getHcoefs(1, 1) * gANDh.getHcoefs(2, 2));
        final double ly = -gANDh.getHcoefs(1, 1) * gANDh.getGcoefs(2, 0)
                + sqrt3 * (gANDh.getGcoefs(1, 0) * gANDh.getHcoefs(2, 1)
                + gANDh.getHcoefs(1, 1) * gANDh.getGcoefs(2, 2)
                - gANDh.getGcoefs(1, 1) * gANDh.getHcoefs(2, 2));
        final double lz = 2.0 * gANDh.getGcoefs(1, 0) * gANDh.getGcoefs(2, 0)
                + sqrt3 * (gANDh.getGcoefs(1, 1) * gANDh.getGcoefs(2, 1)
                + gANDh.getHcoefs(1, 1) * gANDh.getHcoefs(2, 1));
        final double tmp2d = 0.25 * (lz * gANDh.getGcoefs(1, 0) + lx * gANDh.getGcoefs(1, 1)
                + ly * gANDh.getHcoefs(1, 1)) / h0;
        Eccrr[0] = (lx - gANDh.getGcoefs(1, 1) * tmp2d) / (3.0 * h0);
        Eccrr[1] = (ly - gANDh.getHcoefs(1, 1) * tmp2d) / (3.0 * h0);
        Eccrr[2] = (lz - gANDh.getGcoefs(1, 0) * tmp2d) / (3.0 * h0);
    }

    //############################################################################

    /**
     * Informal test code.
     */
    // Checking main block!!!
    public static void main(String a[]) throws IOException {
        // Can not test entirely since since one can not easily instantiate MagProps.

        //IgrfModel igrf = new IgrfModel(null);
        //igrf.setIgrf(1993.34F);        
        //File file = new File("/home/erjo/.ovt/3.0/mdata/igrf.d_new");
        File file = new File("/home/erjo/.ovt/3.0/mdata/igrf.d_old");

        IgrfModel.initHashTableFromFile(file, 2000, true);
        IgrfModel.initHashTable(file, 2010);
        IgrfModel.initHashTable(file, 2015);
        IgrfModel.initHashTable(file, 2020);
        for (int i = 1999; i <= 2010; i += 2) {
            //IgrfModel igrf = new IgrfModel(null);
            //igrf.setIgrf((float) i);
        }

    }//*/
    //******************************************************

}
