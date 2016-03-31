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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Logger extends IntentService {

    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public Logger() {
		  
	      super("Logging Service for Sonav");
	      Log.i("logger","created");
	  }

	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	  @Override
	  protected void onHandleIntent(Intent intent) {
	      // Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
		  Log.i("Logger","Started log dump");
		  String state = Environment.getExternalStorageState();
		  String[] data = intent.getStringArrayExtra(PARAM_IN_MSG);
		  
	        if (Environment.MEDIA_MOUNTED.equals(state)) {
	            // We can read and write the media
	        	try
	            {
	        		File root = new File(Environment.getExternalStorageDirectory(), "SoNav");
	                if (!root.exists()) {
	                    root.mkdirs();
	                }

	        		File gpxfile = new File(root,"sensordata.txt");
	        		FileWriter writer = new FileWriter(gpxfile, true);
	        		for (int i=0; i<data.length; i++)
	        		{
	        			if(data[i]!=null)
	        				writer.append(data[i]+"\n");
	        		}
	        		writer.flush();
	        		writer.close();
	        		Log.i("Logger","Finished writing");
	            }
	        	catch(IOException e)
	            {
	                 Log.i("Logger","Cannot write");
	            }

	        } //else
	        	//Toast.makeText(this, "Cannot append log to file", Toast.LENGTH_SHORT);
	  }
	
	}