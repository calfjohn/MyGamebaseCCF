/****************************************************************************
Copyright (c) 2008-2010 Ricardo Quesada
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2011      Zynga Inc.
Copyright (c) 2013-2014 Chukong Technologies Inc.
 
http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package org.cocos2dx.cpp;

import java.util.UUID;

import org.cocos2dx.cpp.AbstractServiceUsingActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.cocos2dx.cpp.R;
import com.intel.stc.events.DiscoveryNodeUpdateEvent;
import com.intel.stc.events.StcException;
import com.intel.stc.interfaces.StcDiscoveryNodeUpdateEventListener;
import com.intel.stc.ipc.STCLoggingLevel;
import com.intel.stc.ipc.STCLoggingMode;
import com.intel.stc.ipc.STCLoggingModule;
import com.intel.stc.lib.StcLib;
import com.intel.stc.slib.IStcServInetClient;
import com.intel.stc.utility.StcDiscoveryNode;

public class AppActivity extends AbstractServiceUsingActivity implements StcDiscoveryNodeUpdateEventListener {
	public static final String TAG = "Cocos2dxActivity";
	private Dialog platformStartAlert;
	private static boolean cloudRegistrationFailed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//platformStartAlert = ProgressDialog.show(this, "", "Please wait, initializing StcLib platform.");
		//platformStartAlert.show();		
	}

	/*Start**************STC callback.**********/
	private void StartAgent(StcLib slib) {
		// Enable this app for Intel CCF Developer Central Online logging.
		//
		// WARNING:  Enabling online debugging should be made only while the product is in a development environment on secure (non-public) networks.
		//				                     It should NOT be enabled for release products as enabling online debugging poses security risks on non-secure networks.
		//				                     Prior to releasing a product, either remove this call or specify OFFLINE logging only.
		// Start the Agent
		UUID appGuid = MyAppRegister.id.appId;
		String appName = this.getString(R.string.app_name);

		String logPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getPath();
		try {
			if (slib != null) {
				if (slib != null) {					
					slib.startAgent(appGuid, appName,
							STCLoggingMode.LogMode_Live, logPath
									+ "/simpleDiscoveryAgentLog.txt",
							STCLoggingModule.LogModule_All,
							STCLoggingLevel.Info, false);
				}
			}
		} catch (StcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	@Override
	protected void onResume() {
		Log.i(TAG, "resuming");
		super.onResume();
	}


	@Override
	protected void onPause() {
		Log.i(TAG, "pausing");
		super.onPause();
	}

	@Override
	public void sessionsDiscovered() {
		//serviceManager.getSessions();
	}

	@Override
	protected void onStcLibPrepared() {
		
		final StcLib lib = serviceManager.getSTCLib();
		if(lib!=null){
			lib.addStcDiscoveryNodeUpdateEventListener(AppActivity.this);
			StartAgent(lib);
			myHandler.post(new Runnable() {
				@Override
				public void run() {
					if(platformStartAlert!=null && platformStartAlert.isShowing()){
						platformStartAlert.dismiss();
					}
				}
			});
		}
		
		displayToast("Platform Prepared");
	}
	
	@Override
	public void discoveryNodeUpdate(DiscoveryNodeUpdateEvent event) {
		Log.i(TAG, "discoveryNodeUpdate event.getStatus()"+event.getStatus()+event.getNode().getName()+"Event type: "+event.getEventType()+" error code: "+event.getDiscoveryNodeError());
		if(event.getStatus() == 1)
		{
			Log.e(TAG, "Discovery node update failed - "+event.getDiscoveryNodeError().toString());
			if(!event.getDiscoveryNodeError().equals(DiscoveryNodeUpdateEvent.DiscoveryNodeError.noError))
				displayErrorToast(event.getDiscoveryNodeError().toString());
			return;
		}
		
		StcDiscoveryNode node = event.getNode();
		switch(event.getEventType())
		{
			case CREATE:
				Log.i(LOGC , "Creating Node "+node.getName());
				break;
			case DELETE:
				Log.i(LOGC , "Deleting Node "+node.getName());
				break;
			case JOIN:
				Log.i(LOGC , "Joining Node "+node.getName());
				break;
			case LEAVE:
				Log.i(LOGC , "Leaving Node "+node.getName());
				break;
			case PUBLISH:
				Log.i(LOGC , "Publishing Node "+node.getName());
				break;
			default:
				break;
		}		
	}
	
	//To display android toast message.
	private void displayToast(String value){
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
	
	//Display toast- Discovery Node update failed.
	private void displayErrorToast(final String value){
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
				builder.setTitle("Discovery node update failed - "+value);
				builder.setCancelable(true);
				
				builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if(builder!=null)
							dialog.dismiss();
					}
				});
				builder.show();
			}
		});
		
	}	
	
}


