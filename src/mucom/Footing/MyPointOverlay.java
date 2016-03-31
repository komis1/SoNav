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
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MyPointOverlay extends ItemizedOverlay{
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public MyPointOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();
		// TODO Auto-generated constructor stub
	}
	
	public MyPointOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  populate();
		  mContext = context;
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void addOverlay(OverlayItem overlay, Drawable d) {
	    overlay.setMarker(d);
	    super.boundCenterBottom(d);
		mOverlays.add(overlay);
	    populate();
	    
	}
	
	public void removeOverlay(int index)
	{
		mOverlays.remove(index);
		populate();
	}
	public void removeAllOverlays()
	{
		mOverlays.clear();
		//populate();
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
	protected boolean onTap(int index) {
	  //OverlayItem item = mOverlays.get(index);
	  //Toast.makeText(mContext,item.getPoint().getLatitudeE6()+", "+item.getPoint().getLongitudeE6(), Toast.LENGTH_SHORT).show();
	  /*AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();*/
	  return true;
	}
}
