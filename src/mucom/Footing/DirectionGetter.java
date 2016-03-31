
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class DirectionGetter extends AsyncTask<Void, Void, Void>{
	
	private GeoPoint sp;
	private GeoPoint ep;
	private MyPointOverlay mpo;
	private Drawable[] icons;
	private MapView map;
	private List<Overlay> overlays;
	private GeoPoint startp;
	private GeoPoint endp;
	private FootingActivity fa=null;
	private int loadcoords=0;
	
	public boolean test=false;
	
	public DirectionGetter(List<Overlay> ml, MyPointOverlay mp, Drawable[] d, MapView mv, GeoPoint s, GeoPoint e)
	{
		Log.i("DGetter", "Created");
		mpo=mp;
		icons=d;
		map=mv;
		overlays=ml;
		startp=s;
		endp=e;
	}
	
	public DirectionGetter(FootingActivity f)
	{
		fa=f;
		mpo=fa.mpo;
		icons=fa.icons;
		map=fa.mapView;
		overlays=fa.mapOverlays;
		startp=fa.startp;
		endp=fa.endp;
	}
	
	public DirectionGetter(FootingActivity f, int load)
	{
		fa=f;
		mpo=fa.mpo;
		icons=fa.icons;
		map=fa.mapView;
		overlays=fa.mapOverlays;
		startp=fa.startp;
		endp=fa.endp;
		loadcoords= load;
	}
	
	public void getRoute()
	//public ArrayList<GeoPoint> getRoute()
	{
		if(loadcoords==0)
		{
			String startc = startp.getLatitudeE6()/1E6+","+startp.getLongitudeE6()/1E6;
			String endc = endp.getLatitudeE6()/1E6+","+endp.getLongitudeE6()/1E6;
			String urlString = "http://maps.google.com/maps/api/directions/json?origin="+startc+"&destination="+endc+"&mode=walking&sensor=false";
			Log.i("Coords", urlString);
			String result="";
			try 
			{
				Log.i("DGetter","Connecting..");
				URL url = new URL(urlString);
				
				Scanner in = new Scanner(new InputStreamReader(url.openStream()));
				Log.i("DGetter","Connected OK");
				
				while (in.hasNext()) 
				{
					result += in.nextLine();
				}
				Log.i("DGetter","Download OK");
			}
			catch (Exception e) 
			{
					Log.e("DGetter","Connection Error");
			}	
	         
	        //parse json data
	        try{
	        	
	        	//overlays.clear();
	        	mpo.removeAllOverlays();
	        	fa.pnts.clear();
	        	
	        	JSONObject jsonObject = new JSONObject(result); // parse response into json object
	        	// routesArray contains ALL routes
	        	JSONArray routesArray = jsonObject.getJSONArray("routes");
	        	// Grab the first route
	        	JSONObject route = routesArray.getJSONObject(0);
	        	// Take all legs from the route
	        	JSONArray legs = route.getJSONArray("legs");
	        	// Grab first leg
	        	JSONObject leg = legs.getJSONObject(0);
	        	// Grab steps
	        	JSONArray steps = leg.getJSONArray("steps");
	        	JSONObject durationObject = leg.getJSONObject("duration");
	        	JSONObject startObj = leg.getJSONObject("start_location");
	        	
	        	String duration = durationObject.getString("text");
	        	Log.i("Dirs", "JSon parsing OK");
	        	double lat;
	        	double lon;
	        	
	        	//add starting point
	        	lat=startObj.getDouble("lat");
	    		lon=startObj.getDouble("lng");
	    		GeoPoint g = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
	    		fa.pnts.add(g);
	    		OverlayItem overlayitem = new OverlayItem(g, "Hola, Mundo!", "I'm in Mexico City!");
	    		mpo.addOverlay(overlayitem, icons[0]);
	    		Log.i("Dirs", "Added "+lat+", "+lon);
	    		//setup segments array
	    		fa.segments= new GeoPoint[steps.length()][2];
	    		Log.i("Segments","array of "+steps.length());
	        	int i=0;
	    		for (i=0; i<steps.length(); i++)
	        	{
	        		lat=steps.getJSONObject(i).getJSONObject("end_location").getDouble("lat");
	        		lon=steps.getJSONObject(i).getJSONObject("end_location").getDouble("lng");
	        		g = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
	        		fa.pnts.add(g);
	        		overlayitem = new OverlayItem(g, "Hola, Mundo!", "I'm in Mexico City!");
	        		if(i<steps.length()-1)
	        			mpo.addOverlay(overlayitem, icons[1]);
	        		else
	        			mpo.addOverlay(overlayitem, icons[2]);
	        		//add to segments array
	        		//add end location
	        		fa.segments[i][1]=g; 
	        		//add start location
	        		lat=steps.getJSONObject(i).getJSONObject("start_location").getDouble("lat");
	        		lon=steps.getJSONObject(i).getJSONObject("start_location").getDouble("lng");
	        		g = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
	        		fa.segments[i][0]=g; 
	        		
	        		Log.i("Dirs", "Added "+lat+", "+lon);
	        	}
	        	
	        	//overlays.add(mpo);
	        	Log.i("Dirs", "OK, added "+mpo.size()+" overlays pts");
	        	
	        	
	        }catch(JSONException e){
	                Log.e("log_tag", "Error parsing data "+e.toString());
	        } 
		}
		else //if asked to load coords
		{
			
			List<GeoPoint> lpoints = new ArrayList<GeoPoint>();
			switch (loadcoords)
			{
				//Glasgow map
			case 1:
				lpoints.add(new GeoPoint((int)(55.861706*1E6), (int)(-4.251194*1E6)));
				lpoints.add(new GeoPoint((int)(55.861427*1E6), (int)(-4.248898*1E6)));
				lpoints.add(new GeoPoint((int)(55.859901*1E6), (int)(-4.249467*1E6)));
				lpoints.add(new GeoPoint((int)(55.859730*1E6), (int)(-4.247664*1E6)));
				lpoints.add(new GeoPoint((int)(55.858677*1E6), (int)(-4.247932*1E6)));
				lpoints.add(new GeoPoint((int)(55.858353*1E6), (int)(-4.245669*1E6)));
				break;
			case 2:
				//Patras 1 - prokat - prutaneia
				lpoints.add(new GeoPoint((int)(38.283730*1E6), (int)(21.788706*1E6)));
				lpoints.add(new GeoPoint((int)(38.284023*1E6), (int)(21.788473*1E6)));
				lpoints.add(new GeoPoint((int)(38.283794*1E6), (int)(21.787975*1E6)));
				lpoints.add(new GeoPoint((int)(38.284672*1E6), (int)(21.787218*1E6)));
				lpoints.add(new GeoPoint((int)(38.284920*1E6), (int)(21.787636*1E6)));
				lpoints.add(new GeoPoint((int)(38.285336*1E6), (int)(21.787300*1E6)));
				lpoints.add(new GeoPoint((int)(38.285599*1E6), (int)(21.787697*1E6)));
				lpoints.add(new GeoPoint((int)(38.286354*1E6), (int)(21.787073*1E6)));
				lpoints.add(new GeoPoint((int)(38.286205*1E6), (int)(21.786655*1E6)));
				break;
			case 3:
				//Patras 2 - prutaneia - near prokat
				lpoints.add(new GeoPoint((int)(38.286205*1E6), (int)(21.786661*1E6)));
				lpoints.add(new GeoPoint((int)(38.286350*1E6), (int)(21.787071*1E6)));
				lpoints.add(new GeoPoint((int)(38.285618*1E6), (int)(21.787687*1E6)));
				lpoints.add(new GeoPoint((int)(38.285339*1E6), (int)(21.787306*1E6)));
				lpoints.add(new GeoPoint((int)(38.284920*1E6), (int)(21.787636*1E6)));
				lpoints.add(new GeoPoint((int)(38.284504*1E6), (int)(21.786835*1E6)));
				lpoints.add(new GeoPoint((int)(38.284107*1E6), (int)(21.787334*1E6)));
				lpoints.add(new GeoPoint((int)(38.283817*1E6), (int)(21.787506*1E6)));
				lpoints.add(new GeoPoint((int)(38.283390*1E6), (int)(21.787561*1E6)));
				break;
			}
			
			mpo.removeAllOverlays();
        	fa.pnts.clear();
        	//add points to fa plist
        	for(GeoPoint p : lpoints)
        	{
        		fa.pnts.add(p);
        	}
    		//add start and end overlay
    		OverlayItem overlayitem = new OverlayItem(lpoints.get(0), "Hola, Mundo!", "I'm in Mexico City!");
    		mpo.addOverlay(overlayitem, icons[0]);
    		overlayitem = new OverlayItem(lpoints.get(lpoints.size()-1), "Hola, Mundo!", "I'm in Mexico City!");
    		mpo.addOverlay(overlayitem, icons[2]);
    		fa.startp=lpoints.get(0);
    		fa.endp=lpoints.get(lpoints.size()-1);
    		
    		//setup segments array
    		fa.segments= new GeoPoint[lpoints.size()-1][2];
    		for (int i=0; i<lpoints.size()-1; i++)
    		{
    			overlayitem = new OverlayItem(lpoints.get(i), "Hola, Mundo!", "I'm in Mexico City!");
        		mpo.addOverlay(overlayitem, icons[1]);
        		fa.segments[i][0]=lpoints.get(i);
        		fa.segments[i][1]=lpoints.get(i+1);
    		}
			
		}
	}

	/*@Override
	public void run() {
		getRoute();
		test=true;
	}*/


	//@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub	
		getRoute();
		return null;
	}
	
	protected void onPostExecute(Void result) {
		overlays.clear();
		overlays.add(mpo);
		fa.lines=new SegmentOverlay(icons[1], fa.mapView, fa.pnts);
		overlays.add(fa.lines);
		map.invalidate();
		((TextView) fa.findViewById(R.id.textRT)).setTextColor(Color.GREEN);
		
	}

}
