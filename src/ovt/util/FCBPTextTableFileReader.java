package ovt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * General purpose-routine(s) for reading and interpreting text table with
 * columns on fixed byte positions on every row and where every row has the same
 * length. FCBP = Fixed column byte positions.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class FCBPTextTableFileReader {

    private FCBPTextTableFileReader() {
    }


    /**
     * Reads until end of stream.
     *
     * NOTE: Table data ends up inside the FileColumnReader objects.
     *
     * @param N_chars_per_line Number of characters per line, excluding carriage
     * return and line feed.
     */
    public static void readTable(Reader in, int N_chars_per_line, List<FileColumnReader> fileColReaders) throws IOException {
        try (final BufferedReader reader = new BufferedReader(in)) {
            while (true) {
                /*
                 * Read line.
                 * ----------
                 * Java API, readLine(): "Returns: A String
                 * containing the contents of the line, not including any
                 * line-termination characters, or null if the end of the stream has
                 * been reached."
                 */
                final String line = reader.readLine();

                /* Check that everything is OK before parsing line. */
                if (line == null) {
                    // CASE: Found end of stream.
                    break;
                } else if (line.length() != N_chars_per_line) {
                    throw new IOException("The stream/file has a row with a number of characters that is different from the expected number.");
                }

                /* Parse line. */
                try {
                    for (FileColumnReader fcr : fileColReaders) {
                        fcr.readRow(line);
                    }
                } catch (NumberFormatException nfe) {
                    throw new IOException("Can not parse text strings.", nfe);
                }

            }
        } catch (IOException e) {
            //in.close();
            throw new IOException("Can not parse file/stream: " + e.getMessage(), e);
        }
    }

    //##########################################################################
    public interface FileColumnReader {

        /**
         * Read whole line/row and extract and store what is of interest.
         *
         * @param line Entire row in file/stream.
         */
        public void readRow(String line);
    }

    //##########################################################################
    /**
     * Helper class for the purpose of defining where a column is in the
     * file/stream and how to interpret and store the data there.
     */
    private abstract static class AbstractFileColumnReader implements FileColumnReader {

        private final int columnBegin, columnEnd;
        private final String srcFillValue;


        /**
         * @param mSrcFillValue Only applies to a whitespace-trimmed fill value. Can be null for no fill value.
         */
        public AbstractFileColumnReader(int mColumnBegin, int mColumnEnd, String mSrcFillValue) {
            columnBegin = mColumnBegin;
            columnEnd = mColumnEnd;
            srcFillValue = mSrcFillValue;

            if ((srcFillValue != null) && ((columnEnd - columnBegin) < srcFillValue.length())) {
                throw new IllegalArgumentException("Badly configured column definition. Stated fill value string is larger than width of column.");
            } else if (columnEnd-columnBegin < 1) {
                throw new IllegalArgumentException("Zero or negative column width.");
            }
        }


        @Override
        public void readRow(String line) {
            final String strValue = line.substring(columnBegin, columnEnd);
            if (strValue.trim().equals(srcFillValue)) {    // NOTE: Trims whitespace before comparing.
                storeInterpretStringValue(null);
            } else {
                storeInterpretStringValue(strValue);
            }
        }


        /**
         * @param strValue Value to be interpreted and stored. Null means a fill
         * value was found.
         */
        abstract protected void storeInterpretStringValue(String strValue);
    }
    //##########################################################################

    public static class FileDoubleColumnReader extends AbstractFileColumnReader {

        private final DoubleArray buffer;
        private final double newFillValue;


        public FileDoubleColumnReader(int mColumnBegin, int mColumnEnd, String mSrcFillValue, double mNewFillValue, int initBufSize) {
            super(mColumnBegin, mColumnEnd, mSrcFillValue);
            buffer = new DoubleArray(initBufSize);
            newFillValue = mNewFillValue;
        }


        @Override
        protected void storeInterpretStringValue(String strValue) {
            if (strValue == null) {
                buffer.add(newFillValue);
            } else {
                buffer.add(Double.parseDouble(strValue.trim()));
            }
        }
        
        public double[] getBuffer() {
            return buffer.getBuffer();
        }
    }

    //##########################################################################
    /**
     * See FileDoubleColumnReader. Entirely analogous.
     */
    public static class FileIntColumnReader extends AbstractFileColumnReader {

        private final IntArray buffer;
        private final int newFillValue;


        public FileIntColumnReader(int mColumnBegin, int mColumnEnd, String mSrcFillValue, int mNewFillValue, int initBufSize) {
            super(mColumnBegin, mColumnEnd, mSrcFillValue);
            buffer = new IntArray(initBufSize);
            newFillValue = mNewFillValue;
        }


        @Override
        protected void storeInterpretStringValue(String strValue) {
            if (strValue == null) {
                buffer.add(newFillValue);
            } else {
                buffer.add(Integer.parseInt(strValue.trim()));
            }
        }
        public int[] getBuffer() {
            return buffer.getBuffer();
        }
    }

    //##########################################################################
    /**
     * Implements growing array for primitive double. Like ArrayList<Double>
     * but much simpler and should be more effective for large arrays.
     */
    // PROPOSAL: Move to Utils.
    public static class DoubleArray {

        private static final int BUFFER_RESIZE_FACTOR = 2;
        private double[] buffer;
        private int usedBufferLength = 0;


        public DoubleArray(int initialBufferSize) {
            buffer = new double[initialBufferSize];
        }


        public void add(double x) {
            if (usedBufferLength + 1 > buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * BUFFER_RESIZE_FACTOR);
            }
            buffer[usedBufferLength] = x;
            usedBufferLength++;
        }


        public double[] getBuffer() {
            return Arrays.copyOf(buffer, usedBufferLength);
        }

    }

    //##########################################################################
    /**
     * See DoubleArray. Entirely analogous.
     */
    // PROPOSAL: Move to Utils.
    public static class IntArray {

        private static final int BUFFER_RESIZE_FACTOR = 2;
        private int[] buffer;
        private int usedBufferLength = 0;


        public IntArray(int initialBufferSize) {
            buffer = new int[initialBufferSize];
        }


        public void add(int x) {
            if (usedBufferLength + 1 > buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * BUFFER_RESIZE_FACTOR);
            }
            buffer[usedBufferLength] = x;
            usedBufferLength++;
        }


        public int[] getBuffer() {
            return Arrays.copyOf(buffer, usedBufferLength);
        }

    }
}
