package org.cocos2dx.cpp;

import android.content.Intent;
import android.util.Log;

import com.intel.stc.events.InviteRequestEvent;
import com.intel.stc.events.InviteResponseEvent;
import com.intel.stc.interfaces.StcConnectionListener;
import com.intel.stc.lib.StcLib;
import com.intel.stc.slib.StcServiceInet;
import com.intel.stc.utility.StcApplicationId;

public class CCFManager extends StcServiceInet implements StcConnectionListener{

	private static final String TAG = "Cocos2dxCCFManager";
	
	@Override
	public Intent GetCloudIntent() {
		Log.i(TAG,"GetCloudIntent");
		return null;
	}

	@Override
	public Class<?> GetUnboxActivityClass() {
		Log.i(TAG,"GetUnboxActivityClass");
		return null;
	}

	@Override
	public StcApplicationId getAppId() {
		Log.i(TAG,"getAppId");
		return MyAppRegister.id;
	}

	@Override
	public StcConnectionListener getConnListener() {
		Log.i(TAG,"getConnListener");
		return this;
	}

	@Override
	protected void stcLibPrepared(StcLib arg0) {
		Log.i(TAG,"stcLibPrepared");
		
	}

	@Override
	protected void stcPlatformMissing() {
		Log.i(TAG,"stcPlatformMissing");		
	}

	//*******************StcConnectionListener*****************
	
	@Override
	public void connectionCompleted(InviteResponseEvent arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG,"connectionCompleted");
	}

	@Override
	public void connectionRequest(InviteRequestEvent arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG,"connectionRequest");
	}

}
