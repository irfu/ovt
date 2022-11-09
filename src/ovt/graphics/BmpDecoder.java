/*
 * BmpDecoder.java
 *
 * Created on October 10, 2000, 12:46 PM
 */

package ovt.graphics;

import java.awt.*;
import java.io.*;
import java.awt.image.*;

/**
 *
 * @author  ko
 * @version
 */
public class BmpDecoder extends Object {
    
    /** Creates new BmpDecoder */
    public BmpDecoder() {
    }
    
    /** Returns Image from .bmp file */
    public static Image getImage(String filename) throws IOException {
        
        byte buf[] = new byte[64];
        int pixcol;
        int i,j, xres, yres, temp, temp1, temp2, xlength, offset;
        byte [] color16 = new byte[4];
        Color [] colors = new Color[256];
        
        
        RandomAccessFile in = new RandomAccessFile(filename,"r");
        in.read(buf, 0, 54);
        xres = (buf[18] >= 0) ? buf[18] : buf[18] + 256;
        temp = (buf[19] >= 0) ? buf[19] : buf[19] + 256;
        xres |= temp<<8;
        yres = (buf[22] >= 0) ? buf[22] : buf[22] + 256;
        temp = (buf[23] >= 0) ? buf[23] : buf[23] + 256;
        yres |= temp<<8;
        offset = (buf[10] >= 0) ? buf[10] : buf[10] + 256;
        temp = (buf[11] >= 0) ? buf[11] : buf[11] + 256;
        offset |= temp<<8;
        int pixels[] = new int[xres*(yres+1)];
        
        //g.drawString("Generating Picture " + s,20,20);
        
        switch (buf[28])
        {
            case 4:
                for (i=0; i<16; i++)
                {
                    in.read(color16, 0, 4);
                    temp = (color16[2]<0) ? color16[2] + 256:
                        color16[2];
                        temp1 = (color16[1]<0) ? color16[1] + 256:
                            color16[1];
                            temp2 = (color16[0]<0) ? color16[0] + 256:
                                color16[0];
                                colors[i] = new Color(temp, temp1, temp2);
                }
                in.seek(offset);
                xlength = xres/2;
                while (xlength %4 != 0)
                    xlength++;
                byte pixel[] = new byte[xlength];
                for (i=yres-1; i>=0; i--)
                {
                    //if (i%10 == 0)
                        //System.out.print(".");
                    
                    in.read(pixel, 0, xlength);
                    for (j=0; j<xres; j+=2)                                        {
                        pixcol = (pixel[j/2] >> 4) & 0x0F;
                        pixels[i*xres + j] = colors[pixcol].getRGB();
                        pixcol = (pixel[j/2]) & 0x0F;
                        pixels[i*xres + j + 1] = colors[pixcol].getRGB();
                    }
                }
                break;
            case 8:
                for (i=0; i<256; i++)
                {
                    in.read(color16, 0, 4);
                    temp = (color16[2]<0) ? color16[2] + 256:
                        color16[2];
                        temp1 = (color16[1]<0) ? color16[1] + 256:
                            color16[1];
                            temp2 = (color16[0]<0) ? color16[0] + 256:
                                color16[0];
                                colors[i] = new Color(temp, temp1, temp2);
                }
                in.seek(offset);
                xlength = xres;
                while (xlength %4 != 0)
                    xlength++;
                byte pixel1[] = new byte[xlength];
                for (i=yres-1; i>=0; i--)
                {
                    if (i%10 == 0)
                        System.out.print(".");
                    in.read(pixel1, 0, xlength);
                    for (j=0; j<xres; j++)
                    {
                        pixcol = pixel1[j];
                        if (pixcol < 0)
                            pixcol += 256;
                        pixels[i*xres + j] = colors[pixcol].getRGB();
                    }
                }
                break;
                
            case 24:
                in.seek(offset);
                xlength = 3*xres;
                while (xlength %4 != 0)
                    xlength++;
                byte pixel2[] = new byte[xlength+4];
                for (i=yres-1; i>=0; i--)
                {
                    //if (i%10 == 0)
                        //System.out.print(".");
                    in.read(pixel2, 0, xlength);
                    for (j=0; j<xlength; j+=3)
                    {
                        temp = (pixel2[j+2]<0) ? pixel2[j+2] + 256:
                            pixel2[j+2];
                            pixels[i*xres + j/3] = (255 << 24) |
                            (temp << 16);
                            temp = (pixel2[j+1]<0) ? pixel2[j+1] + 256:
                                pixel2[j+1];
                                pixels[i*xres + j/3] |= (temp << 8);
                                temp = (pixel2[j]<0) ? pixel2[j] + 256: pixel2[j];
                                pixels[i*xres + j/3] |= temp;
                    }
                }
        }
        in.close();
        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(xres, yres, pixels, 0, xres));
    }
    
}
