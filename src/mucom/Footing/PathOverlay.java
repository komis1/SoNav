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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class PathOverlay extends Overlay{
	
	private MapView map;
	private GeoPoint s, e;
	
	
	public PathOverlay(Drawable defaultMarker) {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public PathOverlay(MapView m, GeoPoint s1, GeoPoint e1) {
		  super();
		  map = m;
		  s=s1;
		  e=e1;
		}
	public void setPoints(GeoPoint s1, GeoPoint e1)
	{
		s=s1;
		e=e1;
	}

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {

        super.draw(canvas, map, shadow);

        Paint paint=new Paint();
        Point screenCoords=new Point();
        Point screenCoords1=new Point();
        paint.setStrokeWidth(3);
		paint.setColor(Color.RED);

    	map.getProjection().toPixels(s, screenCoords);
        int x1=screenCoords.x;
        int y1=screenCoords.y;

        map.getProjection().toPixels(e, screenCoords1);
        int x2=screenCoords1.x;
        int y2=screenCoords1.y;

       
        canvas.drawLine(x1, y1, x2, y2, paint);
      }
       
}
