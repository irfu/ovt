package ovt.mag;

import java.io.IOException;

/**
 * Interface for raw OMNI2 data sources. One is the actual source of OMNI2 data.
 * Others can be sources for generating test data for test purposes.
 *
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public interface OMNI2RawDataSource {

    public OMNI2Data getData_hourlyAvg(int year) throws IOException;


    public int[] getYearMinMax_hourlyAvg();
}
