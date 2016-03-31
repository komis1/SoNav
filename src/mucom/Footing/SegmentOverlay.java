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
import com.google.android.maps.OverlayItem;

public class SegmentOverlay extends ItemizedOverlay{
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private MapView map;
	ArrayList<GeoPoint> pnts = new ArrayList<GeoPoint>();
	
	public SegmentOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();
		// TODO Auto-generated constructor stub
	}
	
	public SegmentOverlay(Drawable defaultMarker, MapView m, ArrayList<GeoPoint> p_pnts) {
		  super(boundCenterBottom(defaultMarker));
		  populate();
		  map = m;
		  pnts=p_pnts;
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();

	}
    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {

        super.draw(canvas, map, shadow);

        Paint paint=new Paint();
        Point screenCoords=new Point();
        Point screenCoords1=new Point();
        paint.setStrokeWidth(3);
		paint.setColor(Color.GREEN);
		for (int x=0; x<pnts.size(); x++)
        {
        	if (x<pnts.size()-1)
        	{
	        	map.getProjection().toPixels(pnts.get(x), screenCoords);
		        int x1=screenCoords.x;
		        int y1=screenCoords.y;
		
		        map.getProjection().toPixels(pnts.get(x+1), screenCoords1);
		        int x2=screenCoords1.x;
		        int y2=screenCoords1.y;
		
		       
		        canvas.drawLine(x1, y1, x2, y2, paint);
        	}
        }
        
    }


	
	@Override
	protected boolean onTap(int index) {
		Log.i("Lines ","Touched");
	  //OverlayItem item = mOverlays.get(index);
	  //Toast.makeText(mContext,item.getPoint().getLatitudeE6()+", "+item.getPoint().getLongitudeE6(), Toast.LENGTH_SHORT).show();
	  /*AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();*/
	  //return true;
		return true;
	}
}
