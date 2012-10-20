package me.werner291.navigator.vecmath;

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
    public static double distanceToSegment(double x1, double y1, double z1,
    		double x2, double y2, double z2, double x3, double y3, double z3) {
		double[] closestPoint = getClosestPointOnSegment(x1,y1,z1,x2,y2,z2,x3,y3,z3);
		double deltaX = x3 - closestPoint[0];
		double deltaY = y3 - closestPoint[1];
		double deltaZ = z3 - closestPoint[2];
		
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
    }
    
    // TODO check if results are correct
    public static double[] getClosestPointOnSegment(double x1, double y1, double z1,
    		double x2, double y2, double z2, double x3, double y3, double z3) {

    	final double xDelta = x2-x1;
    	final double yDelta = y2-y1;
    	final double zDelta = z2-z1;

    	if ((xDelta == 0) && (yDelta == 0) && (zDelta == 0)) {
    	    return new double[]{x1,y1,z1};
    	}

    	final double u = ( (x1-x3)*xDelta + (y1-y3)*yDelta + (z1-z3)*zDelta )
    						/ (xDelta * xDelta + yDelta * yDelta + zDelta * zDelta);

    	final double[] closestPoint;
    	if (u < 0) {
    	    closestPoint = new double[]{x1,y1,z1};
    	} else if (u > 1) {
    	    closestPoint = new double[]{x2,y2,z2};
    	} else {
    	    closestPoint = new double[]{x1 + u*xDelta, y1 + u * yDelta, z1 + u * zDelta};
    	}

    	return closestPoint;
    }

}
