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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.cocos2dx.cpp.AbstractServiceUsingActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.intel.stc.utility.StcSession;

public class AppActivity extends AbstractServiceUsingActivity implements StcDiscoveryNodeUpdateEventListener {
	public static final String TAG = "Cocos2dxActivity MyGame";
	private Dialog platformStartAlert;
	private	Dialog upDialog	= null;	
	private static boolean cloudRegistrationFailed = false;
	public static AppActivity sAppActivity = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		platformStartAlert = ProgressDialog.show(this, "", "Please wait, initializing StcLib platform.");
		platformStartAlert.show();
		
		sAppActivity = this;		
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
	protected void onDestroy()
	{
		if (upDialog != null)
			upDialog.dismiss();
		super.onDestroy();
	}	

	@Override
	public void sessionsDiscovered() {
		Log.i(TAG, "sessionsDiscovered");
		
		List<StcSession> newList = serviceManager.getSessions();
		for( int i = newList.size() - 1; i >= 0 ; i-- )
		{
			StcSession session = newList.get(i);
			AppActivity.SessionUpdate(session.getUserName()+session.getSessionName(), session.getSessionUuid().toString(), saveBmp(session.getPublicAvatar(), ""+i), this);
		}
	}
	
    private static native void SessionUpdate(String SeesionName, String sessionUuid, String strAvatarFilePath, Object object);

	@Override
	protected void onStcLibPrepared() {
		
		final StcLib lib = serviceManager.getSTCLib();
		if(lib!=null){
//			lib.addStcDiscoveryNodeUpdateEventListener(AppActivity.this);
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
	
	/**
	 * 
	 * @param bmp
	 * @param strName : file name to stored
	 */
	public static String saveBmp(Bitmap bmp, String strName) {
		FileOutputStream out = null;
		try {
		    String status = Environment.getExternalStorageState(); 
		    if (status.equals(Environment.MEDIA_MOUNTED)) {
		    	String strDest = Environment
		    			.getExternalStorageDirectory().getAbsolutePath() + "/cocoassist/" + strName;
		    	
		    	String path = Environment
		    			.getExternalStorageDirectory().getAbsolutePath() + "/cocoassist/";
				File dir = new File( path );
		    	if (!dir.exists()) {
					dir.mkdirs();
				}
		        File destFile = new File(strDest); 
		        if (destFile.exists()) { 
		        	destFile.delete();
		        } 

				out = new FileOutputStream(strDest);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				
				return strDest;
		    } 
		} catch (FileNotFoundException e) { 
		    e.printStackTrace(); 
		    return "";
		} catch (IOException e) {
		    e.printStackTrace(); 
		    return "";
		} finally {
			if( null != out ) {
				try {
					out.close();
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
    
	public void inviteSession(String inviterUuid)
	{
		StcSession newSession = null;
		for(StcSession user : serviceManager.getSessions()){
			if(user.getSessionUuid().toString().compareTo(inviterUuid)==0){
				newSession = user;
				break;
			}
		}		
		
		if(newSession == null) return;

		serviceManager.inviteSession(newSession);
		
//		if (serviceManager.inviteSession(newSession))
//			upDialog = ProgressDialog.show(this, "", "Waiting for connection");
	}    

	@Override
	public void lineReceived(int line) {
		Log.i(TAG, "line received event in AppActivity");
	}

	@Override
	public void sessionListChanged() {	
	}

	@Override
	public void localSessionChanged() {		
	}

	@Override
	public void remoteDisconnect() {
		Log.i(TAG, "remote disconnect event in AppActivity");
	}

	@Override
	public void connected(final boolean didConnect) {
		myHandler.post(new Runnable() {
			public void run()
			{
				if (didConnect)
				{
					Log.i(TAG, "successful connection");
					if(upDialog!=null && upDialog.isShowing()){
						upDialog.dismiss();
					}
					
					displayToast("successful connection");
					//Intent ni = new Intent(AppActivity.this, ChatActivity.class);
					//startActivity(ni);
				}
				else
				{
					Log.i(TAG, "connection failed, shutting down.");
					if(upDialog!=null && upDialog.isShowing()){
						upDialog.dismiss();
						displayErrorToast("Invite rejected or timed out.");
					}
				}
				
			}

		});		
	}

	@Override
	public void inviteAlert(final UUID inviterUuid, final int inviteHandle, final byte[] oobData) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		String oobDataStr = "";
		if(oobData != null)
			oobDataStr = "\"" + new String(oobData) + "\"";
		else
			oobDataStr = "empty";
		
		Log.i(LOGC, String.format("Out of band data is %s", oobDataStr));
		
		myHandler.post(new Runnable() {
			
			public void run() {
				
				List<StcSession> list = serviceManager.getSessions();
				String userName = null;
				for(StcSession user : list){
					if(user.getSessionUuid().compareTo(inviterUuid)==0){
						userName = user.getUserName();
						break;
					}
				}
			
				builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   serviceManager.doConnectionRequest(inviterUuid, inviteHandle);
				           }
				       });
				builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   serviceManager.rejectInvite(inviteHandle);
				           }
				       });
				
				builder.setTitle("SimpleChat invite");
				builder.setMessage("Accept connection from "+userName+"?");
				builder.show();
			}
		});
	}

    public static void setStringForKey(String inviteUuid) {
    	if(sAppActivity !=null) sAppActivity.inviteSession(inviteUuid);
    }	
	
}


