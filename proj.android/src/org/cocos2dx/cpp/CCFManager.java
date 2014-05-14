package org.cocos2dx.cpp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.intel.startup.CloudAuthorizationActivity;
import com.intel.startup.NewUnbox;
import com.intel.stc.events.InviteRequestEvent;
import com.intel.stc.events.InviteResponseEvent;
import com.intel.stc.events.StcException;
import com.intel.stc.events.StcSessionUpdateEvent;
import com.intel.stc.interfaces.IStcActivity;
import com.intel.stc.interfaces.StcConnectionListener;
import com.intel.stc.interfaces.StcLocalSessionUpdateListener;
import com.intel.stc.interfaces.StcSessionUpdateListener;
import com.intel.stc.lib.StcLib;
import com.intel.stc.slib.StcServiceInet;
import com.intel.stc.utility.StcApplicationId;
import com.intel.stc.utility.StcSession;

public class CCFManager extends StcServiceInet implements StcConnectionListener, StcSessionUpdateListener, StcLocalSessionUpdateListener, IStcActivity{

	private static final String TAG = "Cocos2dxCCFManager MyGame";
	private boolean bundleParsed = false;
	private Bundle initBundle;
	private ArrayList<ISimpleDiscoveryListener> listeners = new ArrayList<ISimpleDiscoveryListener>();

	@Override
	public Intent GetCloudIntent() {
		Log.i(TAG,"GetCloudIntent");
		
		Intent intent = new Intent(getApplicationContext(), CloudAuthorizationActivity.class);
		intent.putExtra("clientID", MyAppRegister.id.clientId);
		intent.putExtra("redirectURL", MyAppRegister.redirectURL);
		intent.putExtra("appId", MyAppRegister.id.appId.toString());
		return intent;		
	}
	
	//Service callback to inform the UI about sessions list getting updated.
	private void postSessionListChanged() {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener listen : listeners)
				listen.sessionsDiscovered();
		}
	}	

	@Override
	public Class<?> GetUnboxActivityClass() {
		Log.i(TAG,"GetUnboxActivityClass");
		return NewUnbox.class;
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
	protected void stcLibPrepared(StcLib slib) {
		Log.i(TAG,"stcLibPrepared");
		if (slib == null)
			try {
				throw new Exception(
						"the stclib is null in stcLibPrepared. Have you given your app local socket permissions? Have you given your app a registration object?");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		slib.setConnectionListener(this);
		slib.setStcSessionListListener(this);
		slib.addLocalSessionListener(this);
		Log.i(TAG, "registered with the platform");
		
		// bundle will be parsed by parseInitBundle()
		tryParseBundle();

		postSessionListChanged();
	}

	/**
	 * Save away the invite bundle and try to parse it.
	 * 
	 * @param bundle
	 */
	public void parseInitBundle(Bundle bundle) {
		initBundle = bundle;
		bundleParsed = false;
		tryParseBundle();
	}
	
	/**
	 * Attempt to parse the bundle. We should only do this once. We can only do
	 * this when we have an stclib.
	 */
	private void tryParseBundle() {
		StcLib lib = getSTCLib();
		if (lib == null)
			return;

		synchronized (this) {
			if (bundleParsed)
				return;
			bundleParsed = true;
		}

		lib.parseStartMethod(this, initBundle, this);
	}
	
	@Override
	protected void stcPlatformMissing() {
		Log.i(TAG,"stcPlatformMissing");		
	}

	//*******************StcConnectionListener*****************
	
	@Override
	public void connectionCompleted(InviteResponseEvent arg0) {
		Log.i(TAG,"connectionCompleted");
	}

	@Override
	public void connectionRequest(InviteRequestEvent arg0) {
		Log.i(TAG,"connectionRequest");
	}

	@Override
	public void localSessionUpdated() {
		Log.i(TAG, "localSessionUpdated");		
	}

	@Override
	public void sessionUpdated(StcSessionUpdateEvent arg0) {
		Log.i(TAG, "sessionUpdated");		
	}

	@Override
	public void onStartClient(UUID arg0, int arg1) {
		Log.i(TAG, "onStartClient");
	}

	@Override
	public void onStartNormal() {
		Log.i(TAG, "onStartNormal");
	}

	@Override
	public void onStartServer(UUID arg0) {
		Log.i(TAG, "onStartServer");
	}

	/***
	 * Gets the current session list. Returns an empty list if stclib is not
	 * initialized or there are no sessions or something else goes wrong.
	 * 
	 * @return
	 */
	public List<StcSession> getSessions() {
		StcLib lib = getSTCLib();
		if (lib != null) {
			try {
				return lib.getSessionListWithAvatar();
			} catch (StcException e) {
				Log.e(TAG, "unexpected exception", e);
			}
		}
		return new ArrayList<StcSession>();
	}
	
	// /
	// / LISTENER MANAGEMENT AND EVENT PROPAGATION
	// /

	//Add listener to communicate between service and the UI.
	public boolean addListener(ISimpleDiscoveryListener listener) {
		synchronized (listeners) {
			if (listeners.contains(listener))
				return false;
			else
			{
				boolean temp = listeners.add(listener);
				return temp;
			}
		}
	}

	//Remove listener to communicate between service and the UI.
	public boolean removeListener(ISimpleDiscoveryListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}	
		
}
