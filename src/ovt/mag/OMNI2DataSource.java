package ovt.mag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ovt.datatype.Time;
import ovt.util.SegmentsCache;

/**
 * Class from which OMNI2 data can be retrieved.
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class OMNI2DataSource {

    /*public OMNI2DataSource(String cacheDir) {

        //new CacheDataSource();
        SegmentsCache sc = new SegmentsCache();
    }

    private static class CacheDataSource implements SegmentsCache.DataSource {

        private final OMNI2RawDataSources rawDataSrc;


        private CacheDataSource(String cacheDir) {
            rawDataSrc = new OMNI2RawDataSources(new File(cacheDir));
        }*/


        /**
         * @param t_begin Mjd.
         * @param t_end Mjd
         */
        /*@Override
        public OMNI2Data getSupersetSegment(double t_begin, double t_end) throws IOException {
            final int beginYear = new Time(t_begin).getYear();
            final int endYear = new Time(t_end).getYear();
            if (endYear < beginYear) {
                throw new IllegalArgumentException();
            }
            
            List<SegmentsCache.DataSegment> dataList = new ArrayList();
            for (int year = beginYear; year <=endYear; year++) {
                dataList.add(rawDataSrc.getOMNI2Data_hourlyAvg(year));
            }
            
            OMNI2Data data = mergeAdjacent(dataList);
            return data;
            
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


        @Override
        public OMNI2Data mergeAdjacent(List<SegmentsCache.DataSegment> segments) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }*/
}
