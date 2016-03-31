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



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.pielot.openal.Buffer;
import org.pielot.openal.SoundEnv;
import org.pielot.openal.Source;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;


//
public class FootingActivity extends MapActivity implements LocationListener, SensorEventListener, OnClickListener{
	
	protected MapView mapView;
	private MapController mc;
	protected GeoPoint startp = null; //new GeoPoint((int)(55.865861*1E6), (int)(-4.250812*1E6));
	protected GeoPoint endp = null; //new GeoPoint((int)(55.866199*1E6), (int)(-4.26978*1E6));
	protected GeoPoint closestp=null;
	protected GeoPoint myPosition=null;
	protected GeoPoint prevPosition=null;
	private final GeoPoint glasgow = new GeoPoint((int)(55.86566*1E6), (int)(-4.2572*1E6));
	protected ArrayList<GeoPoint> pnts;
	protected List<Overlay> mapOverlays;
	protected MyPointOverlay mpo;
	protected SegmentOverlay lines;
	protected PathOverlay posline;
	protected Drawable [] icons;
	protected GeoPoint[][] segments;
	private MediaPlayer player;
	private boolean startmarker=false;
	private boolean stopmarker=false;
	private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mValues;
    private boolean play3d=false;
    protected LocationManager locationManager;
    private double closestdist;
    private int soundtoplay = 0;
    private int dist_sensitivity=12; //sensitivity for shifting orientations
    private int dist2play=80; //sensitivity for target sound range
    private int location_sensitivity = 30; //sensitivity for location jumps
    private boolean routeset=false;
    private String[] locationData;
    private int locdataindex=0;
    private boolean tsoundplaying=false;
  
    private SoundEnv env;
    
    
   /* private org.pielot.openal.Source walkSound1;
    private org.pielot.openal.Source riverSound;
    private org.pielot.openal.Source birdSound;
*/
    private Source[] sounds;
	
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setRequestedOrientation(
	    		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    setContentView(R.layout.maplayout);
	    pnts=new ArrayList<GeoPoint>();
	    
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapView.getZoomButtonsController().setAutoDismissed(false);

	    mc=mapView.getController();
	    //mapView.setOnTouchListener(this);
	    mapView.setOnClickListener(this);
	    
	    mc.setCenter(glasgow);
	    mc.setZoom(16);
	    
	    // Acquire a reference to the system Location Manager
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    // Register the listener with the Location Manager to receive location updates
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	    
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	    
	    Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    if (networkLocation !=null)
	    {
	    	networkLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	if (networkLocation !=null)
	    	{
		    	myPosition = new GeoPoint((int)(networkLocation.getLatitude()*1E6), (int)(networkLocation.getLongitude()*1E6));
		    	mc.setCenter(myPosition);
	    	}
	    }
	    locationData=new String[20];
	    
	    //compass
	    mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);  
	    
	    mapOverlays = mapView.getOverlays();
	    icons = new Drawable[4];
	    icons[1] = this.getResources().getDrawable(R.drawable.locationmarker); //path marker
	    icons[3]=this.getResources().getDrawable(R.drawable.user);//user pos marker
	    icons[0]=this.getResources().getDrawable(android.R.drawable.presence_online);//path start
	    icons[2]=this.getResources().getDrawable(android.R.drawable.presence_offline);//path end
	    mpo=new MyPointOverlay(icons[2], this);
	    
	    /*OverlayItem i = new OverlayItem(glasgow, "Hello1", "Hello2");
	    mpo.addOverlay(i);
	    
	    mapOverlays.clear();
	    mapOverlays.add(mpo);
	    mapView.invalidate();*/
	    Log.i("FootActivity","App ready");

	    // DirectionGetter d= new DirectionGetter(this);
	   // d.execute();
	    
	    //prepare the media 
	   /* player = MediaPlayer.create(this, R.raw.walk);
	    if (player!=null)
	    {
	    	player.setLooping(true);
	    	//player.start();
	    }
	    else
	    	Log.i("Player", "Failed to create");*/
	    
	    //3D media
	    try
	    {
		    this.env = SoundEnv.getInstance(this);
		    sounds = new org.pielot.openal.Source[3];
		    Buffer walk = env.addBuffer("city3");
		    //Buffer river = env.addBuffer("river");
		    Buffer birds = env.addBuffer("house");
		    Buffer target = env.addBuffer("target");
		    
		   // birdSound.setRolloffFactor(0.5f);
		    sounds[0] = env.addSource(walk);
		    sounds[1] = env.addSource(birds);
		    sounds[2] = env.addSource(target);
		    

		    sounds[0].setPosition(0, 0, -10);
		    sounds[1].setPosition(0, 0, -10);
		    sounds[2].setPosition(0, 0, -10);
		    //this.birdSound.setGain(1.0f);
		    
		    this.env.setListenerOrientation(0);
		    this.env.setListenerPos(0, 0, 0);
	    }
	    catch (Exception e)
	    {
	    	Log.i("3D player","Failed to create");
	    }
	}
	
	public void invokeDataLogger()
	{
		Log.i("logger","invoked");
		Intent msgIntent = new Intent(this, Logger.class);
		msgIntent.putExtra(Logger.PARAM_IN_MSG, locationData);
		startService(msgIntent);
	}
	
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.i("Position","ldindex= "+locdataindex);
		if (locdataindex==locationData.length-1)
		{	
			//buffer full, invoke the writing service
			Log.i("Position","logdump indx= "+locdataindex);
			invokeDataLogger();
			locdataindex=0;
		}
		
		locationData[locdataindex]=new DecimalFormat("#.#####").format(location.getLatitude())+", " + new DecimalFormat("#.#####").format(location.getLongitude());
		locationData[locdataindex]+=", "+location.getSpeed()+", "+location.getBearing() +", "+location.getProvider()+", "+location.getTime();
		locdataindex += 1;
		
		Log.i("Position","Changed!");
		String crds = new DecimalFormat("#.#####").format(location.getLatitude())+", ";
		crds+=new DecimalFormat("#.#####").format(location.getLongitude());
		((TextView) this.findViewById(R.id.textCRDS)).setText(" "+crds);
		myPosition = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
		
		/*//detect jumps
		if (prevPosition==null)
		{
			prevPosition=myPosition;
		}
		else
		{
			double ed=TrigCalculator.CalculationByDistance(myPosition, prevPosition);
			if (ed>location_sensitivity)
			{
				myPosition=prevPosition;
				Log.i("Position jump", "diff= "+ed);
				
			}
			else
			{
				prevPosition=myPosition;
			}
		}*/
		
		int segment=-1;
		if (startp!=null && endp!=null)
		{
			
			double ldist2=-1;
			closestdist=1000000000;
						
			for (int i = 0; i<pnts.size()-1; i++)
			{
				
				//the closest segment could be index of point -1 or index
				//calculate distances from both segments
				ldist2 = TrigCalculator.distanceToSegment(segments[i][0], segments[i][1], myPosition);
				Log.i("Distance", "Segment "+i+": "+ldist2+"m");
				if (closestdist>ldist2)
				{
					closestdist=ldist2;
					closestp=TrigCalculator.closestPointOnSegment(segments[i][0], segments[i][1], myPosition);
					segment=i;
				}
			}
			Log.i("Distance", "closest is "+segment);
			((TextView)this.findViewById(R.id.textDIST)).setText(" DST: "+new DecimalFormat("#.##").format(closestdist));
			
			//find distance to end
			double dte=TrigCalculator.CalculationByDistance(myPosition, endp);
			
			//if (player.isPlaying())
			if(this.play3d)
			{
				double scale =1-(0.006*closestdist);
				/**/
				if (scale<0)
					scale=0;
				//player.setVolume((float)scale, (float)scale);
				sounds[soundtoplay].setGain((float)scale);
				((TextView)this.findViewById(R.id.textVOL)).setText("  Vol: "+new DecimalFormat("#.##").format(scale));
				Log.i("Audio", "Volume = "+scale);
				
				//target sound
				
				if (dte<=dist2play)
				{	
					if (!tsoundplaying)
					{
						sounds[2].play(true);
						tsoundplaying=true;
					}
					sounds[2].setGain((float)(0+((dist2play-dte)/dist2play))); //2=targetsound
					Log.i("Targ Snd"," "+(float)(0+((dist2play-dte)/dist2play))+", dist="+dte);
				}
				else
					if(tsoundplaying)
					{
						sounds[2].stop();
						tsoundplaying=false;
					}
			}
		} //end if startp && endp !=null
		
		//draw the position
		mc.animateTo(myPosition);
		//mc.setZoom(16);
		if(segment>=0)
		{
			if (posline==null)
			{
				posline = new PathOverlay(mapView, segments[segment][0], segments[segment][1]);
				mapOverlays.add(posline);
			}
			else
				posline.setPoints(segments[segment][0], segments[segment][1]);
			
		}
		OverlayItem i = new OverlayItem(myPosition, "Hello1", "Hello2");
		Log.i("MPO","size="+mpo.size());
		Log.i("PNTS","size="+pnts.size());
		
		if (mpo.size()>pnts.size())
		{
			Log.i("MPO-Overlay","removed");
			mpo.removeOverlay(mpo.size()-1);
		}
	    mpo.addOverlay(i, icons[3]);
	    mapView.invalidate();
		
	}
	
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//Log.i("Touch Event", " "+event.getAction());
		
		if(event.getPointerCount()>1) {
			Log.i("multitouch","");
			//event.recycle();
			return false;
        }
		else 
		{
			if(event.getAction()==MotionEvent.ACTION_MOVE)
			{
				Log.i("dragging","");
				//event.recycle();
				return false;
			}
			else
			if(event.getAction()==MotionEvent.ACTION_DOWN)
			{
				Projection p = mapView.getProjection();
				GeoPoint pn = p.fromPixels((int) event.getX(), (int) event.getY());
				
				if(startmarker)
				{
					startp= pn;
					Toast.makeText(this, "Start point set\n"+pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
					startmarker=false;
					if (endp !=null)
						{
							DirectionGetter d = new DirectionGetter(this);
							d.execute();
						}
				    
				}
				else 
					if (stopmarker)
					{
						endp= pn;
						Toast.makeText(this, "End point set\n"+pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
						stopmarker=false;
						if(startp!=null)
						{
							DirectionGetter d = new DirectionGetter(this);
							d.execute();					
						}
						
		
					}
					else
						Toast.makeText(this, pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
						//mapView.setOnTouchListener(this);
					Log.i("touch", pn.getLatitudeE6()/1E6+" "+pn.getLongitudeE6()/1E6);
				
			}

				super.dispatchTouchEvent(event);
				return false;
		}
	}
	*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mapmenu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		
		DirectionGetter d;
	    switch (item.getItemId()) {
	    case R.id.play3d:
	    	if(play3d)
	    	{
	    		sounds[soundtoplay].stop();
	    		play3d=false;
	    	}
	    	else
	    	{
	    		sounds[soundtoplay].play(true);
	    		play3d=true;
	    	}
	    	
	    	return true;
	    case R.id.set_start:
	    	startmarker=true;
	        return true;
	    case R.id.set_end:
	    	stopmarker=true;
	        return true;
	    case R.id.exit_app:
	    	this.finish();
	    	
	        return true;
	    case R.id.load_coords:
	    	/*mapView.invalidate();
	    	DirectionGetter d = new DirectionGetter(this);
		    d.execute();
	    	Log.i("Points", "total points "+ pnts.size());*/
	    	pnts.clear();
	    	d = new DirectionGetter(this, 1);
	    	//startmarker=true;
	    	//stopmarker=true;
	        d.execute();
	        return true;
	        
	    case R.id.load_patras1:
	    	//38.283739,21.78871 - prokat
	    	//38.286198,21.786661 - prytaneia    	
	    	d = new DirectionGetter(this, 2);
		    d.execute();
	    	return true;
	    	
	    case R.id.load_patras2:
	    	//38.286198,21.786661 - prytaneia
	    	//38.283402,21.787573 - dipla sta prokat
	    	
	    	d = new DirectionGetter(this, 3);
		    d.execute();
	    	return true;
	    	
	    case R.id.toggle_audio:
	    	
	    	try
	    	{
		    	sounds[soundtoplay].stop();
		    	if (soundtoplay==0)
		    	{
		    		soundtoplay=1;
		    		Toast.makeText(this, "Music", Toast.LENGTH_SHORT).show();
		    	}
		    	else
		    	{
		    		soundtoplay=0;
		    		Toast.makeText(this, "Walking Sound", Toast.LENGTH_SHORT).show();
		    	}
		    	sounds[soundtoplay].play(true);
		    	play3d=true;
	    	}
	    	catch (Exception e)
	    	{
	    		Toast.makeText(this, "Could not toggle sound..", Toast.LENGTH_SHORT).show();
	    	}
	    	
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		
	//	Log.d("Compass","sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
		
		if (closestp!=null && myPosition!=null && closestdist>dist_sensitivity) //change the sound orientation only if >8m away from path
		{
			//calculate the bearing from my loc to the point in segment
			double b = TrigCalculator.bearing(myPosition, closestp);
			double orientation = b-event.values[0];
			
			this.env.setListenerOrientation(360-orientation);
			//Log.d("Compass", "bear2pnt (" + b + ", " + event.values[0] + ")");
			//Log.d("Compass", "orientat ("+(360-orientation)+")");
		} 
		mValues = event.values;
	}
	
	 @Override
	    protected void onStop()
	    {
		 	invokeDataLogger();//write any remaining data to file
		 	if (this.play3d)
		 	{//this.walkSound1.stop();
    		//this.riverSound.stop();
    	
		 		sounds[soundtoplay].stop();
		 		if (tsoundplaying)
		 			sounds[2].stop();
		 		sounds[0].release();
		 		sounds[1].release();
		 		sounds[2].release();
		 	}
		 	if (false) Log.d("Sensor", "onStop");
		 	
	        mSensorManager.unregisterListener(this);
	        locationManager.removeUpdates(this);
	        super.onStop();
	    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.i("Shit", "shit");
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
			int[] offsets= new int[2];
			mapView.getLocationInWindow(offsets);
			if(event.getAction()==MotionEvent.ACTION_DOWN)
			{
				Projection p = mapView.getProjection();
				GeoPoint pn = p.fromPixels((int) event.getX(), (int) event.getY()-offsets[1]);
				
				if(startmarker)//if startmarker mode
				{
					startp= pn;
					Toast.makeText(this, "Start point set\n"+pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
					startmarker=false;
					if (endp !=null)
						{
							DirectionGetter d = new DirectionGetter(this);
							d.execute();
						}
				    
				}
				else 
					if (stopmarker) //if stopmarker mode
					{
						routeset = true;
						endp= pn;
						Toast.makeText(this, "End point set\n"+pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
						stopmarker=false;
						if(startp!=null)
						{
							DirectionGetter d = new DirectionGetter(this);
							d.execute();					
						}
						
		
					}
					else
					{
						Toast.makeText(this, pn.getLatitudeE6()/1E6+"\n"+pn.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
						routeset=false;
						//mapView.setOnTouchListener(this);
					}
					Log.i("touch", pn.getLatitudeE6()/1E6+" "+pn.getLongitudeE6()/1E6);
			}
			super.dispatchTouchEvent(event);
			return false;

	}

}