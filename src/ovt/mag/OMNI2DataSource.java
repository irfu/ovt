package ovt.mag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ovt.datatype.Time;
import ovt.util.SegmentsCache;

/**
 * Class from which OMNI2 data can be retrieved and used by the application.
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class OMNI2DataSource {

    //##########################################################################
    private static class CacheDataSource implements SegmentsCache.DataSource {

        private final OMNI2RawDataSource rawDataSrc;


        private CacheDataSource(OMNI2RawDataSource mRawDataSrc) {
            rawDataSrc = mRawDataSrc;
        }


        /**
         * @param t_begin Mjd.
         * @param t_end Mjd.
         */
        @Override
        public OMNI2Data getSupersetSegment(double t_begin, double t_end) throws IOException {
            if (t_end < t_begin) {
                throw new IllegalArgumentException();
            }
            final int beginYear = new Time(t_begin).getYear();
            final int endYear = new Time(t_end).getYear();    // Inclusive.

            final List<SegmentsCache.DataSegment> dataList = new ArrayList();
            for (int year = beginYear; year <= endYear; year++) {

                /*==========================================
                 This is where data is read from the source.
                 ==========================================*/
                dataList.add(rawDataSrc.getData_hourlyAvg(year));
            }

            return mergeAdjacent(dataList);
        }


        @Override
        public OMNI2Data mergeAdjacent(List<SegmentsCache.DataSegment> segments) {

            // Convert List<SegmentsCache.DataSegment> ==> List<OMNI2Data>
            List<OMNI2Data> dataList = new ArrayList();
            for (SegmentsCache.DataSegment seg : segments) {
                dataList.add((OMNI2Data) seg);
            }

            return OMNI2Data.mergeAdjacent(dataList);
        }
    }


    //##########################################################################
    public OMNI2DataSource(String cacheDir) {

        /*==================================
         Choose OMNI2 raw data source. Can implement and choose other for testing purposes.
         ==================================*/
        final OMNI2RawDataSource rawDataSrc = new OMNI2RawDataSourceImpl(new File(cacheDir));

        // Exact value not so important but should probably not be more than
        // the length of time covered by a year or the underlying OMNI2 files (?).
        final double minDataSourceTScale_mjd = 1;

        final int[] yearMinMax = rawDataSrc.getYearMinMax_hourlyAvg();
        final double searchMin_mjd = Time.getMjd(yearMinMax[0], 1, 1, 0, 0, 0);
        final double searchMax_mjd = Time.getMjd(yearMinMax[1] + 1, 1, 1, 0, 0, 0);

        final SegmentsCache sc = new SegmentsCache(
                new CacheDataSource(rawDataSrc),
                minDataSourceTScale_mjd,
                searchMin_mjd,
                searchMax_mjd);
    }
    
    //public get

    //##########################################################################
}
