package me.werner291.navigator.vecmath;

import java.util.Arrays;

/*
 * DistancePointSegmentExample, calculate distance to line
 * Copyright (C) 2008 Pieter Iserbyt <pieter.iserbyt@gmail.com>
 * Modified by Werner Kroneman to support 3D.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Example implementation of "Minimum Distance between a Point and a Line" as
 * described by Paul Bourke on
 * See http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/.
 */


public class VecMath2D {

    /**
     * Returns the distance of p3 to the segment defined by p1,p2;
     * 
     * @param x1
     *                First point of the segment
     * @param p2
     *                Second point of the segment
     * @param p3
     *                Point to which we want to know the distance of the segment
     *                defined by p1,p2
     * @return The distance of p3 to the segment defined by p1,p2
     */
	//TODO check if works correctly
	@Deprecated
    public static double distanceToSegmentSquared(
    		double x1, double y1, double z1,
            double x2, double y2, double z2,
            double px, double py, double pz) {
    	System.out.println(Arrays.toString(new double[]{x1,y1,z1,x2,y2,z2,px,py,pz}));
    	
            // Adjust vectors relative to x1,y1
            // x2,y2 becomes relative vector from x1,y1 to end of segment
            x2 -= x1;
            y2 -= y1;
            z2 -= z1;
            // px,py becomes relative vector from x1,y1 to test point
            px -= x1;
            py -= y1;
            pz -= z1;
            double dotprod = px * x2 + py * y2 + pz * z2;
            double projlenSq;
            if (dotprod <= 0.0) {
                // px,py is on the side of x1,y1 away from x2,y2
                // distance to segment is length of px,py vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0;
            } else {
                // switch to backwards vectors relative to x2,y2
                // x2,y2 are already the negative of x1,y1=>x2,y2
                // to get px,py to be the negative of px,py=>x2,y2
                // the dot product of two negated vectors is the same
                // as the dot product of the two normal vectors
                px = x2 - px;
                py = y2 - py;
                py = z2 - pz;
                dotprod = px * x2 + py * y2 + pz * z2;
                if (dotprod <= 0.0) {
                    // px,py is on the side of x2,y2 away from x1,y1
                    // distance to segment is length of (backwards) px,py vector
                    // "length of its (clipped) projection" is now 0.0
                    projlenSq = 0.0;
                } else {
                    // px,py is between x1,y1 and x2,y2
                    // dotprod is the length of the px,py vector
                    // projected on the x2,y2=>x1,y1 vector times the
                    // length of the x2,y2=>x1,y1 vector
                    projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2 + z2 * z2);
                }
            }
            // Distance to line is now the length of the relative point
            // vector minus the length of its projection onto the line
            // (which is zero if the projection falls outside the range
            //  of the line segment).
            double lenSq = px * px + py * py + pz * pz - projlenSq;
            if (lenSq < 0) {
                lenSq = 0;
            }
            return Math.sqrt(lenSq);
        
    }
    
    // TODO check if results are correct
    @Deprecated
    public static double[] getClosestPointOnSegment(double x1, double y1, double z1,
    		double x2, double y2, double z2, double px, double py, double pz) {

    	double[] uVec = new double[]{x2-x1,y2-y1,z2-z1};
    	double[] vVec = new double[]{px-x1,py-y1,pz-z1};
    	
    	double uLen = Math.sqrt(uVec[0]*uVec[0]+uVec[1]*uVec[1]+uVec[2]*uVec[2]);
    	uVec = new double[]{uVec[0]/uLen,uVec[1]/uLen,uVec[1]/uLen};
    	
    	double udotv = uVec[0]*vVec[0]+uVec[1]*vVec[1]+uVec[2]*vVec[2];
    	
    	return new double[]{x1+uVec[0]*udotv,y1+uVec[1]*udotv,z1+uVec[2]*udotv};
    }

}
