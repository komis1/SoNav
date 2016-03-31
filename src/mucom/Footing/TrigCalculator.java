/******************************************************************************
 * Copyright 2016 Andreas Komninos  
 * All rights reserved. This program and the accompanying materials   
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *           
 * Contributors: 
 * Andreas Komninos - code implementation
 * http://www.komninos.info
 *****************************************************************************/
 
package mucom.Footing;

import com.google.android.maps.GeoPoint;


public class TrigCalculator {
 

   private final static double Radius= 6371000;//meters
   public static int MILLION = 1000000;
 
 
   TrigCalculator() 
   {
   }
 
 
 
   public static double CalculationByDistance(GeoPoint StartP, GeoPoint EndP) {
 
      double lat1 = StartP.getLatitudeE6()/1E6;
      double lat2 = EndP.getLatitudeE6()/1E6;
      double lon1 = StartP.getLongitudeE6()/1E6;
      double lon2 = EndP.getLongitudeE6()/1E6;
      double dLat = Math.toRadians(lat2-lat1);
      double dLon = Math.toRadians(lon2-lon1);
      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
 
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return Radius * c;
   }
   /*
    * Returns the coordinates of the projection of a point onto a nav segment (line)
     @param p1 the start of the segment
     *
     @param p2 the end of the segment
     *
     @param p3 the point to be projected
     *
     @return GeoPoint the coordinates of p3's projection onto the segment
    */
   public static GeoPoint closestPointOnSegment(GeoPoint p1, GeoPoint p2, GeoPoint p3) {

	final double xDelta = p2.getLatitudeE6() - p1.getLatitudeE6();
	final double yDelta = p2.getLongitudeE6() - p1.getLongitudeE6();

	if ((xDelta == 0) && (yDelta == 0)) {
	    throw new IllegalArgumentException("p1 and p2 cannot be the same point");
	}

	final double u = ((p3.getLatitudeE6() - p1.getLatitudeE6()) * xDelta + (p3.getLongitudeE6() - p1.getLongitudeE6()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

	final GeoPoint closestPoint;
	if (u < 0) {
	    closestPoint = p1;
	} else if (u > 1) {
	    closestPoint = p2;
	} else {
	    closestPoint = new GeoPoint((int)(p1.getLatitudeE6() + u * xDelta), (int)(p1.getLongitudeE6() + u * yDelta));
	}

	return closestPoint;
   }
   /*
    * Returns the distance of a point to a nav segment (line)
     @param p1 the start of the segment
     *
     @param p2 the end of the segment
     *
     @param p3 the point to be projected for measuring distance
     *
     @return double the distance of p3 from the segment
    */
   public static double distanceToSegment(GeoPoint p1, GeoPoint p2, GeoPoint p3) {

	   GeoPoint closestPoint = closestPointOnSegment(p1, p2, p3);
	   return CalculationByDistance(p3, closestPoint);
   }

   /**
    * Computes the bearing in degrees between two points on Earth.
    * 
    * @param p1 First point
    * @param p2 Second point
    * @return Bearing between the two points in degrees. A value of 0 means due
    *         north.
    */
   public static double bearing(GeoPoint p1, GeoPoint p2) {
       double lat1 = p1.getLatitudeE6() / (double) MILLION;
       double lon1 = p1.getLongitudeE6() / (double) MILLION;
       double lat2 = p2.getLatitudeE6() / (double) MILLION;
       double lon2 = p2.getLongitudeE6() / (double) MILLION;

       return bearing(lat1, lon1, lat2, lon2);
   }
   
   /**
    * Computes the bearing in degrees between two points on Earth.
    * 
    * @param lat1 Latitude of the first point
    * @param lon1 Longitude of the first point
    * @param lat2 Latitude of the second point
    * @param lon2 Longitude of the second point
    * @return Bearing between the two points in degrees. A value of 0 means due
    *         north.
    */
   public static double bearing(double lat1, double lon1, double lat2, double lon2) {
       double lat1Rad = Math.toRadians(lat1);
       double lat2Rad = Math.toRadians(lat2);
       double deltaLonRad = Math.toRadians(lon2 - lon1);

       double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
       double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
               * Math.cos(deltaLonRad);
       return radToBearing(Math.atan2(y, x));
   }
   /**
    * Converts an angle in radians to degrees
    */
   public static double radToBearing(double rad) {
       return (Math.toDegrees(rad) + 360) % 360;
   }

 
}
