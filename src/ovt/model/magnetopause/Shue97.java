/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/model/magnetopause/Shue97.java,v $
 Date:      $Date: 2005/12/13 16:34:16 $
 Version:   $Revision: 2.2 $


 =========================================================================*/
// Created 5 Apr 2000 11:44 UTC by kono
package ovt.model.magnetopause;

import ovt.mag.*;
import ovt.util.*;

import ovt.util.Utils;

import java.lang.Math;

/**
 * Apears to calculate the distance to the magnetopause. Uncertain which
 * coordinate system it uses for the IMF B_z component but I suspect GSM. The
 * formulas seem related to, but not identical to, Shue et al., 1997, "A new
 * functional form to study the solar wind control of the magnetopause size and
 * shape", JGR vol 102., NO A5, pp 9497-9511<BR>
 * /Erik P G Johansson 2015-09-14
 *
 * Web site documentation states "The magnetopause model is according to Shue et
 * al., JGR, v. 103, p. 17691, 1998. "<BR>
 * /Erik P G Johansson 2015-10-01
 */
public class Shue97 {

    public static double getR(double cosTheta, double mjd, MagProps magProps) {
        return getR(cosTheta, magProps.getSWP(mjd), magProps.getIMF(mjd)[2]);
    }


    //[bz]=nT, [swp]=nPa, [teta]=radians
    public static double getR(double cosTheta, double swp, double bz) {
        final double cc = -1.0 / 6.6;
        double r, r0, alfa;

        alfa = (0.58 - 0.007 * bz) * (1.0 + 0.024 * Math.log(swp));
        r0 = (10.22 + 1.29 * Utils.tanh(0.184 * (bz + 8.14))) * Math.pow(swp, cc);

        r = r0 * Math.pow(2.0 / (1.0 + cosTheta), alfa);

        return r;
    }


    /**
     * Returns a VERY ROUGH estimate of a distance from the point (gsm) to the
     * magnetopause.
     */
    public static double distance_to_magnetopause(double[] gsm, double swp, double bz) {
        double r = Vect.absv(gsm);
        double cosTheta = gsm[0] / r;
        double r_mpause = getR(cosTheta, swp, bz);
        return r - r_mpause;
    }

}
