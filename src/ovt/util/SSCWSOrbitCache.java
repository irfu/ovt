/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import ovt.Const;
import ovt.OVTCore;
import ovt.datatype.Time;
import ovt.util.IndexedSegmentsCache.CacheSlotContents;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;

/**
 * @author Erik P G Johansson, erik.johansson@irfu.se
 *
 * Class for handling the caching of orbital data downloaded from SSC Web
 * Services. Should contain all cache data that is stored to disk between
 * sessions.
 *
 * NOTE: Uncertain if this is the right package for the class.<BR>
 * NOTE: SSCWS = SSC Web Services
 *
 * IMPLEMENTATION NOTE: The cache slot size is not a STATIC constant to (1) to
 * permit running with different slot sizes if using old caches from earlier
 * sessions (loaded from disk) AND brand new caches started during the current
 * session, (2) simplify automated code testing.
 */
/* IMPLEMENTATION NOTE: The functionality for (1) caching orbits and
 * (b) SSCWS-specific functionality should possibly be separated.
 *    PRO: Makes data source independent of SSCWSLibrary.
 *    CON: Caches the requested time resolution.
 */
public class SSCWSOrbitCache {

    private static final int DEBUG = 3;   // Set the minimum log message level for this class.
    private static final boolean ASSERT_OUTSIDE_EARTH = false;   // You probably want to disable this during automated testing.

    /**
     * Version number of cached data in an object stream. This number always
     * comes first in the stream. The Code will reject data with the wrong
     * version number. This is a functionality to make it possible to change the
     * stream format without needing to manually remove old cache files just to
     * avoid errors due to mismatch in stream format (the code will know to
     * reject them).
     */
    private static final long STREAM_FORMAT_VERSION = 0;

    /**
     * Comment to put in the stream (file). Meant to be human-readable in case
     * someone looks in the stream. The value should be ignored when reading it
     * from the stream.
     */
    private static final String STREAM_COMMENT
            = OVTCore.SIMPLE_APPLICATION_NAME + " " + OVTCore.VERSION
            + " (Build " + OVTCore.BUILD + "). Contains cached satellite orbit data.";

    private final IndexedSegmentsCache segmentsCache;
    private final SSCWSLibrary sscwsLibrary;
    private final SSCWSSatelliteInfo satInfo;

    //##########################################################################
    /**
     * Class that represents the data that is returned to the user of the cache.
     */
    public static class OrbitalData {

        /**
         * Orbit positions. "axisPos" = Indices [axis X/Y/Z/time][position] in
         * km & mjd.
         */
        public final double[][] coords_axisPos_kmMjd;
        public final int worstRequestedResolutionSeconds;  // Unit: seconds
        /**
         * List of indices to data that are followed by a data gap (an unusually
         * large gap time by some definition). Read-only view to list whose
         * underlying data will never change.
         */
        public final List<Integer> dataGaps;


        /**
         * Constructor. Only public to make testing easier.
         */
        public OrbitalData(double[][] mCoords_axisPos, int mWorstRequestedResolutionSeconds, List<Integer> mDataGaps) {
            this.coords_axisPos_kmMjd = mCoords_axisPos;
            this.worstRequestedResolutionSeconds = mWorstRequestedResolutionSeconds;
            this.dataGaps = Collections.unmodifiableList(new ArrayList(mDataGaps));
        }


        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (o == this) {
                return true;
            } else if (!(o instanceof OrbitalData)) {
                return false;
            }
            final OrbitalData od = (OrbitalData) o;
            return Arrays.deepEquals(coords_axisPos_kmMjd, od.coords_axisPos_kmMjd)
                    & (worstRequestedResolutionSeconds == worstRequestedResolutionSeconds)
                    & dataGaps.equals(dataGaps);
        }
    }
    //##########################################################################


    /**
     * Constructor. Construct empty cache.
     */
    public SSCWSOrbitCache(
            SSCWSLibrary mSSCWSLibrary,
            String SSCWSSatID,
            double mCacheSlotSizeMjd,
            int proactiveFillMargin) throws IOException {

        if (mCacheSlotSizeMjd <= 0) {
            throw new IllegalArgumentException("Cache slot size =< 0.");
        }
        this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWSSatID);
        this.sscwsLibrary = mSSCWSLibrary;
        this.segmentsCache = new IndexedSegmentsCache(
                new CacheDataSource(),
                satInfo.availableBeginTimeMjd, satInfo.availableEndTimeMjd,
                mCacheSlotSizeMjd,
                proactiveFillMargin);
    }


    /**
     * Constructor. Tries to load data from stream. The code will reject the old
     * cache only if it can not be used. The caller has to deside whether it
     * will still accept the cache due to e.g. different cache slot size.
     *
     * NOTE: It is not obvious how much reasoning the constructor should do on
     * whether to use or reject the cached data. There are a couple of
     * principally different cases:<BR>
     * (1) Can not obtain cache at all: because of I/O error, because of stream
     * format change (detected with STREAM_FORMAT_VERSION).<BR>
     * (2) Can read the old cache from the stream but it is for another
     * satellite.<BR>
     * (3) Can read the old cache from the stream but it has a different slot
     * size.<BR>
     * (4) Can read the old cache from the stream but there is reason to believe
     * that it does not contain the same data as at the SSC (e.g. since the time
     * interval of available data has changed).<BR>
     *
     * NOTE: The caller still has to find a file with cache data exists, i.e.
     * can not call this function if the caller does not find any.
     *
     * IMPLEMENTATION NOTE: It is useful to use an ObjectInput stream as
     * argument rather than a file for testing purposes.
     *
     * @param mCacheSlotSizeMjd. Cache slot size to use the constructor rejects
     * the old cache.
     * @param approxProactiveCachingFillMarginMjd Approximate time interval of
     * proactive caching fill margin, i.e. how much extra time of data (both
     * before and below) should be downloaded when downloading from SSC Web
     * services anyway.
     *
     * @throws IOException e.g. if SSCWSSatID does not match the stored cache.
     */
    public SSCWSOrbitCache(
            ObjectInput in,
            SSCWSLibrary mSSCWSLibrary, String SSCWSSatID,
            double approxProactiveCachingFillMarginMjd)
            throws IOException, ClassNotFoundException {

        /* IMPLEMENTATION NOTE: Lets the caller set the proactive caching fill
         margin in units of (approximate) TIME, NOT SLOTS. 
         If it was set in number of slots, then it had to be set together with
         the cache slot size to avoid that one value comes from the old cache, and
         one from the new cache since the amount of proactive caching is the product
         of the two (mCacheSlotSizeMJD*proactiveCachingFillMarginSLOTS).
         That may lead to too small or (way) too large cache fill margins (slows down downloading).        
         */
        /*if (mCacheSlotSizeMjd <= 0) {
         throw new IllegalArgumentException("Cache slot size =< 0.");
         }*/
        if (approxProactiveCachingFillMarginMjd < 0) {
            throw new IllegalArgumentException("Proactive caching fill margin < 0.");
        }
        this.satInfo = mSSCWSLibrary.getSatelliteInfo(SSCWSSatID);
        this.sscwsLibrary = mSSCWSLibrary;

        /*===================================================================
         Read data from stream.
         ----------------------
         IMPLEMENTATION NOTE: Do NOT catch ClassNotFoundException since it is
         unknown what happens to the InputStream read pointer after exception.
         IMPLEMENTATION NOTE: Tries to read everything, in particular a
         potentially large cache, before deciding whether to reject it or not.
         Might be slightly inefficient if it is rejected but that should be rare.
         ====================================================================*/
        final long stream_format_version = in.readLong();
        if (stream_format_version != STREAM_FORMAT_VERSION) {
            throw new IOException("Stream format has changed. Can/will not read data from stream.");
        }
        final SSCWSSatelliteInfo oldSatInfo;
        try {
            // The return value is never used, but the object must be read.
            // The return value should be a String so there is a point in checking for that.
            final String streamComment = (String) in.readObject();

            oldSatInfo = (SSCWSSatelliteInfo) in.readObject();
        } catch (ClassCastException e) {
            throw new IOException("Read object of the wrong class: " + e.getMessage(), e);
        }
        if (!oldSatInfo.ID.equals(SSCWSSatID)) {
            throw new IOException("SSCWS satellite ID (\"" + SSCWSSatID + "\") does not match ID in old cache stream (\"" + oldSatInfo.ID + "\").");
        }
        this.segmentsCache = new IndexedSegmentsCache(
                in,
                new CacheDataSource(),
                0);   // Temporary proactive caching fill margin. Set later.

        // Proactive caching fill margin.
        final int proactiveCachingSlots
                = (int) (approxProactiveCachingFillMarginMjd / segmentsCache.getSlotSize());  // (int) yields rounding toward zero.
        this.segmentsCache.setProactiveCachingFillMargin(proactiveCachingSlots);
    }


    public void writeToStream(ObjectOutput out) throws IOException {
        out.writeLong(STREAM_FORMAT_VERSION);
        out.writeObject(STREAM_COMMENT);
        out.writeObject(satInfo);
        segmentsCache.writeToStream(out);
    }


    public SSCWSSatelliteInfo getSSCWSSatelliteInfo() {
        return satInfo;
    }


    public double getSlotSizeMjd() {
        return this.segmentsCache.getSlotSize();
    }


    public int getNbrOfFilledCacheSlots() {
        return this.segmentsCache.getNbrOfFilledCacheSlots();
    }


    /*public void setCachingEnabled(boolean cachingEnabled) {
     this.segmentsCache.setCachingEnabled(cachingEnabled);
     }*/
    //
    //
    /**
     * @throws IndexedSegmentsCache.NoSuchTPositionException if any of the
     * requested start/end positions do not exist.
     */
    public OrbitalData getOrbitData(
            double beginMjdInclusive, double endMjdInclusive,
            RoundingMode tBeginRoundingMode, RoundingMode tEndRoundingMode,
            int beginIndexMargin, int endIndexMargin,
            int callerRequestedResolution)
            throws IOException, IndexedSegmentsCache.NoSuchTPositionException {

        if (callerRequestedResolution <= 0) {
            throw new IllegalArgumentException("Illegal requested time resolution. callerRequestedResolution = " + callerRequestedResolution);
        }

        /**
         * Limit requested solution to the best possible. May otherwise
         * unnecessarily reject old cached data and fetch new data that can not
         * possible have a higher resolution anyway. Since the
         * accept-cache-slot-contents function is defined here, the check also
         * has to be done here.
         *
         * NOTE: Need to define "final" variable so that the value can be used
         * in the lambda function (anonymous class).
         */
        final int actualRequestedResolution = Math.max(callerRequestedResolution, satInfo.bestTimeResolution);

        // NOTE: This function can not be moved outside since it relies on a locally defined (final) variable.
        final Predicate ACCEPT_CACHED_SLOT_CONTENTS_FUNCTION
                = (Predicate) (Object o) -> {
                    final LocalCacheSlotContents unit = (LocalCacheSlotContents) o;
                    return (unit.requestedTimeResolutionSeconds <= actualRequestedResolution);
                };

        final Object data = this.segmentsCache.getData(
                beginMjdInclusive, endMjdInclusive,
                tBeginRoundingMode, tEndRoundingMode,
                beginIndexMargin, endIndexMargin,
                ACCEPT_CACHED_SLOT_CONTENTS_FUNCTION, actualRequestedResolution);

        return (OrbitalData) data;
    }

    //##########################################################################
    // NOTE: Implicitly implements Serializable through IndexedSegmentsCache.CacheSlotContents.
    private static class LocalCacheSlotContents implements IndexedSegmentsCache.CacheSlotContents {

        /**
         * The requested time resolution (after multiplying with the resolution
         * factor) used when requesting data from SSC Web Services.
         *
         * NOTE: The actual data is NOT required to have this time resolution.
         * There may be data gaps or irregular jumps in time.
         *
         * NOTE: SatelliteDescription#getResolution() returns int.
         */
        final int requestedTimeResolutionSeconds;   // Unit: seconds
        final double[][] coordinates;


        // NOTE: Only soft-copies mCoordinates.
        public LocalCacheSlotContents(int mResolutionFactor, double[][] mCoordinates) {
            this.requestedTimeResolutionSeconds = mResolutionFactor;
            this.coordinates = mCoordinates;
        }


        @Override
        public double[] getTArray() {
            return coordinates[3];
        }
    }

    //##########################################################################
    /**
     * Class which only purpose is to serve as a
     * IndexedSegmentsCache.DataSource. (1) In principle the outer class could
     * be used for this but that would mean that the methods prescribed by
     * IndexedSegmentsCache.DataSource had to be public. This way less about the
     * implementation is revealed to the outside. (2) In principle, those
     * methods (the implementations) in the outer class that are called from
     * this class could also be moved here but that would only make the code
     * confusing since the inner class would become so large.
     */
    private class CacheDataSource implements IndexedSegmentsCache.DataSource {

        /*@Override
         public int getCacheSlotIndex(double t) {
         return SSCWSOrbitCache.this.getCacheSlotIndex(t);
         }*/
        @Override
        public Object getDataFromCacheSlotContents(List<CacheSlotContents> requestedCacheSlotsContents, int i_beginSlotArrayInclusive, int i_endSlotArrayExclusive) {
            return SSCWSOrbitCache.this.getDataFromCacheSlotContents(requestedCacheSlotsContents, i_beginSlotArrayInclusive, i_endSlotArrayExclusive);
        }


        @Override
        public List<CacheSlotContents> getCacheSlotContents(int i_beginInclusive, int i_endExclusive, Object getCacheSlotContentsArgument) throws IOException {
            return SSCWSOrbitCache.this.getCacheSlotContents(i_beginInclusive, i_endExclusive, getCacheSlotContentsArgument);
        }
    }


    /**
     * Fill specified cache slots with new data downloaded from SSC Web
     * Services.
     */
    private List<CacheSlotContents> getCacheSlotContents(
            int i_beginInclusive, int i_endExclusive,
            Object getCacheSlotContentsArgument)
            throws IOException {

        Log.log(this.getClass().getSimpleName()
                + ".getCacheSlotContents(i_beginInclusive=" + i_beginInclusive
                + ", i_endExclusive=" + i_endExclusive + ", ...)", DEBUG);

        final int requestedTimeResolution = (int) getCacheSlotContentsArgument;
        final double requestBeginMjd = this.segmentsCache.getCacheSlotSpanMjd(i_beginInclusive)[0];
        final double requestEndMjd = this.segmentsCache.getCacheSlotSpanMjd(i_endExclusive - 1)[1];
        final List<CacheSlotContents> filledSlots = new ArrayList();

        /*======================================================================
         Print log message - Complicated since Time#toString can fail (testing).
         ======================================================================*/
        {
            String requestBeginStr;
            String requestEndStr;
            try {
                requestBeginStr = Time.toString(requestBeginMjd);
                requestEndStr = Time.toString(requestEndMjd);
            } catch (IllegalArgumentException e) {
                requestBeginStr = "";
                requestEndStr = "";
            }
            Log.log(this.getClass().getSimpleName() + ".getCacheSlotContents: "
                    + requestBeginStr + " (" + requestBeginMjd + ") to "
                    + requestEndStr + " (" + requestEndMjd + ")", DEBUG);
        }

        /*======================================================================
         Download data        
         ---------------
         NOTE: The resolution factor must not be lower than one.
         NOTE: It is better to round down to a better resolution, and integer
         division does round down.
         ======================================================================*/
        final int resolutionFactor = Math.max(requestedTimeResolution / this.satInfo.bestTimeResolution, 1);
        System.out.println("Downloading data from SSC Web Services.");   // Printout (not log message) is here to cover the SSCWSLibrary for testing.
        final long t_start = System.nanoTime();
        final double[][] coords_axisPos = sscwsLibrary.getTrajectory_GEI(this.satInfo.ID, requestBeginMjd, requestEndMjd, resolutionFactor);
        final double duration = (System.nanoTime() - t_start) / 1.0e9;  // Unit: seconds
        System.out.printf("   Time used for downloading data: %.1f [s]\n", duration);

        /*==============
         Check assertion
         ==============*/
        if (ASSERT_OUTSIDE_EARTH) {
            final double[] origin = new double[]{0, 0, 0};
            for (int i = 0; i < coords_axisPos[3].length; i++) {
                double[] x_km = new double[]{
                    coords_axisPos[0][i],
                    coords_axisPos[1][i],
                    coords_axisPos[2][i]};
                double r_km = Vect.distance(origin, x_km);
                if (r_km < Const.RE) {
                    //System.out.print();
                    Log.log("Encountered satellite position inside Earth, r_km = " + r_km + " [km].", DEBUG);
                }
            }
        }

        /*===============================================
         Create cache slots containing sections of data.
         ===============================================*/
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final double[] slotBeginEndMjd = this.segmentsCache.getCacheSlotSpanMjd(i);
            final double[][] slotCoords_axisPos;
            if (coords_axisPos[3].length != 0) {
                slotCoords_axisPos = new double[4][];
                final int[] n_slotInterval = Utils.findInterval(coords_axisPos[3], slotBeginEndMjd[0], slotBeginEndMjd[1], true, false);

                for (int k = 0; k < 4; k++) {
                    slotCoords_axisPos[k] = Arrays.copyOfRange(coords_axisPos[k], n_slotInterval[0], n_slotInterval[1]);   // Parameters are inclusive+exclusive.
                }
            } else {
                slotCoords_axisPos = new double[4][0];   // Important to create zero-length sub-arrays rather than keep null(s).
            }
            filledSlots.add(new LocalCacheSlotContents(requestedTimeResolution, slotCoords_axisPos));
        }

        return filledSlots;
    }


    private Object getDataFromCacheSlotContents(
            List<CacheSlotContents> slotContentsList,
            int i_beginSlotTArrayInclusive,
            int i_endSlotTArrayExclusive) {
        /* Merge and clip time series from the various cache slot into one long
         time series (array) for every component X/Y/Z/mjd.
         -----------------------------------------------------------------
         NOTE: Returning the whole array unclipped (returning more data than
         requested) is probably OK for the purposes of OVT,
         but clipping simplifies automated testing since it makes the output more
         predictable and directly comparable with the return value of a simple
         call to SSCWSLibrary#getTrajectory_GEI.
         */

        // Argument checks
        if (i_beginSlotTArrayInclusive < 0) {
            throw new IllegalArgumentException("Illegal argument i_beginSlotTArrayInclusive=" + i_beginSlotTArrayInclusive);
        } else if ((slotContentsList == null) && !((i_beginSlotTArrayInclusive == 0) && (i_endSlotTArrayExclusive == 0))) {
            throw new IllegalArgumentException("requestedSlotContents == null");
        }

        // Indices: [component X/Y/Z/mjd][cache slot][position index]
        final double[][][] coordUnmerged_axisSlotPos = new double[4][slotContentsList.size()][];

        int worstResolutionSeconds = 0;
        for (int i_slotInList = 0; i_slotInList < slotContentsList.size(); i_slotInList++) {
            final LocalCacheSlotContents slotContents = (LocalCacheSlotContents) slotContentsList.get(i_slotInList);   // NOTE: Typecasting
            worstResolutionSeconds = Math.max(worstResolutionSeconds, slotContents.requestedTimeResolutionSeconds);

            // Construct indices to copy from this particular unit.
            // NOTE: A slot may be BOTH FIRST AND LAST in the list.
            int j_begin = 0;
            int j_end = slotContents.coordinates[3].length;

            if (i_slotInList == 0) {
                // CASE: slot is FIRST in list.
                j_begin = i_beginSlotTArrayInclusive;
            }

            if (i_slotInList == slotContentsList.size() - 1) {
                // CASE: slot is LAST in list.
                j_end = i_endSlotTArrayExclusive;
            }

            for (int k_comp = 0; k_comp < 4; k_comp++) {
                coordUnmerged_axisSlotPos[k_comp][i_slotInList] = Utils.selectArrayIntervalMC(slotContents.coordinates[k_comp], j_begin, j_end);
            }
        }

        final double[][] coordMerged_axisPos = new double[4][];  // AxisPos = Indices [axis X/Y/Z/mjd][position index]
        for (int k_comp = 0; k_comp < 4; k_comp++) {
            coordMerged_axisPos[k_comp] = Utils.concatDoubleArrays(coordUnmerged_axisSlotPos[k_comp]);
        }

        /*=================================
         Detect data gaps
         ----------------
         Move code to OrbitalData?
         ================================*/
        final List<Integer> dataGaps = Utils.findJumps(coordMerged_axisPos[3], worstResolutionSeconds * 2 * Time.DAYS_IN_SECOND);
        if (DEBUG > 0) {
            final double[] t = coordMerged_axisPos[3];
            for (int i_dg = 0; i_dg < dataGaps.size(); i_dg++) {  // dg = data gap
                int i = dataGaps.get(i_dg);
                final Time dgBegin = new Time(t[i]);
                final Time dgEnd = new Time(t[i + 1]);
                System.out.println("Detected data gap " + i_dg + ": " + dgBegin.toString() + " to " + dgEnd.toString() + ", length=" + (t[i + 1] - t[i]) + " (mjd)");
                System.out.println("                     " + t[i] + " to " + t[i + 1] + " (mjd)");
                // NOTE: Log.log and System.out.println might not always print in the order they are executed.
            }
        }

        final OrbitalData data = new OrbitalData(coordMerged_axisPos, worstResolutionSeconds, dataGaps);
        return data;
    }

    //##########################################################################
}
