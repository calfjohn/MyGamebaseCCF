package org.cocos2dx.cpp;

import java.io.InputStream;
import java.io.OutputStream;
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
import com.intel.stc.utility.StcSocket;

public class CCFManager extends StcServiceInet implements 
StcConnectionListener, StcSessionUpdateListener, StcLocalSessionUpdateListener, IServiceIOListener, IStcActivity{
	private static final String TAG = "Cocos2dxCCFManager MyGame";
	private boolean bundleParsed = false;
	private Bundle initBundle;
	StcSocket socket = null;
	WriteEngine wengine;
	ReadEngine rengine;
	private ArrayList<ISimpleDiscoveryListener> listeners = new ArrayList<ISimpleDiscoveryListener>();

	// The service handles one and only one connection. The state machine for
	// the service
	// state starts in NEVER_CONNECTED and ends with CONNECTION_CLOSED.
	public enum ChatState {
		NEVER_CONNECTED, // we have not yet connected
		INVITE_SENT, // we have sent an invite.
		INVITE_RECEIVED, // we have received an invite.
		CONNECTED, // we are connected
		CONNECTION_CLOSED // we have been connected, but are no longer.
	};
	
	ChatState state = ChatState.NEVER_CONNECTED;
	
	@Override
	public Intent GetCloudIntent() {
		Log.i(TAG,"GetCloudIntent");
		
		Intent intent = new Intent(getApplicationContext(), CloudAuthorizationActivity.class);
		intent.putExtra("clientID", MyAppRegister.id.clientId);
		intent.putExtra("redirectURL", MyAppRegister.redirectURL);
		intent.putExtra("appId", MyAppRegister.id.appId.toString());
		return intent;		
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
	public void localSessionUpdated() {
		postSessionListChanged();		
		Log.i(TAG, "localSessionUpdated");		
	}

	@Override
	public void sessionUpdated(StcSessionUpdateEvent arg0) {
		postSessionListChanged();
		Log.i(TAG, "sessionUpdated");				
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
	public void connectionCompleted(InviteResponseEvent arg) {
		Log.i(TAG,"connectionCompleted");
		boolean connectionComplete = false;
		synchronized (state) {
			if (arg.getStatus() == InviteResponseEvent.InviteStatus.sqtAccepted) {
				try {
					socket = getSTCLib().getPreparedSocket(
							arg.getSessionGuid(), arg.getConnectionHandle());

					InputStream iStream = socket.getInputStream();
					rengine = new ReadEngine(iStream, this);

					OutputStream oStream = socket.getOutputStream();
					wengine = new WriteEngine(oStream, this);

					connectionComplete = true;
				} catch (StcException e) {
					Log.e(TAG, e.toString());
				}
			}

			if (connectionComplete)
				state = ChatState.CONNECTED;
			else
				state = ChatState.CONNECTION_CLOSED;
		}
		postConnected(connectionComplete);		
	}

	@Override
	public void connectionRequest(InviteRequestEvent event) {
		Log.i(TAG,"connectionRequest");
		synchronized (state) {
			//If the local user is already connected to the remote user, ignore the invites from other users.
			if(state == ChatState.CONNECTED){
				//Notify the other users on rejecting the invite.
				rejectInvite(event.getConnectionHandle());
				return;
			}
				
			postInviteAlert(event.getSessionUuid(), event.getConnectionHandle(), event.getOOBData());
		}		
	}
	
	private void postInviteAlert(UUID inviterUuid, int inviteHandle, byte[] oobData) {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener listen : listeners)
				listen.inviteAlert(inviterUuid, inviteHandle, oobData);
		}
	}	

	/***
	 * Attempt to accept the invitation if we have not already done so
	 * 
	 * @param uuid
	 *            sessionguid
	 * @param handle
	 *            handle of the connection
	 */
	public void doConnectionRequest(UUID uuid, int handle) {
		synchronized (state) {
			if (state == ChatState.NEVER_CONNECTED || state == ChatState.CONNECTION_CLOSED) {
				boolean connected = false;
				try {
					socket = getSTCLib().acceptInvitation(uuid, handle);

					InputStream iStream = socket.getInputStream();
					rengine = new ReadEngine(iStream, this);

					OutputStream oStream = socket.getOutputStream();
					wengine = new WriteEngine(oStream, this);
					connected = true;
				} catch (StcException e) {
					Log.e(TAG, "exception on connection", e);
				}

				if (connected) {
					Log.i(TAG, "connected");
					state = ChatState.CONNECTED;
				} else {
					Log.i(TAG, "connection failed");
					state = ChatState.CONNECTION_CLOSED;
				}
				postConnected(connected);
			}
		}

	}	
	
	/***
	 * Forces the service to shutdown and terminate.
	 */
	public void exitConnection() {
		Log.i(TAG, "exit requested");
		synchronized (state) {
			state = ChatState.CONNECTION_CLOSED;

			if (wengine != null)
				wengine.stop();
			if (rengine != null)
				rengine.stop();
			if (socket != null) {
				try {
					socket.close();
				} catch (StcException e) {
					Log.i(TAG, "", e);
				}
				socket = null;
			}
		}
	}
	
	/***
	 * Have the sdk reject the invitation that has timed out.
	 * 
	 * @param handle
	 */
	public void rejectInvite(int handle) {
		StcLib lib = getSTCLib();
		try {
			if (lib != null)
				lib.rejectInvitation(handle);
			else
				Log.e(TAG, "unexpected null in rejectInvite");
		} catch (StcException e) {
			Log.e(TAG, "unexpected exception", e);
		}
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
	
	
	/***
	 * Request to invite a session.
	 * 
	 * @param session
	 * @return true if an invitation was sent.
	 */
	public boolean inviteSession(StcSession session) {
		synchronized (state) {
			if (state != ChatState.NEVER_CONNECTED && state != ChatState.CONNECTION_CLOSED)
				return false;
			
			if(!session.isAvailableProximity() && !session.isAvailableCloud())
			{
				Log.e(TAG, "Session is not available or expired");
				return false;
			}
			try {
				Log.i(TAG, "inviting session " + session.getSessionUuid().toString()
						+ " " + session.getUserName());
				//// The oobData can be anything, or null.  It's intention is to help identify the purpose of the invite.
				byte[] oobData = (new String("TEST BLOB")).getBytes();
				//byte[] oobData = null;

				// Setting the invite timeout to 180 seconds.
				getSTCLib().inviteSession(session.getSessionUuid(), oobData, (short) 180);
				return true;
			} catch (StcException e) {
				Log.e(TAG, "invitation unexpected exception", e);
			}

		}

		return false;
	}
	
	private void postConnected(boolean connected) {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener l : listeners)
				l.connected(connected);
		}
	}

	private void postSessionListChanged() {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener l : listeners)
				l.sessionsDiscovered();
		}
	}
	
	private void postLocalSessionChanged() {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener l : listeners)
				l.sessionsDiscovered();
		}
	}

	@Override
	public void lineReceived(int line) {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener l : listeners)
				l.lineReceived(line);
		}		
	}

	@Override
	public void remoteDisconnect() {
		synchronized (listeners) {
			for (ISimpleDiscoveryListener l : listeners) {
				l.remoteDisconnect();
			}
		}
		exitConnection();		
	}
	
    public static void setStringForKey(String inviteUuid) {    	
/*		Log.e(TAG, "setStringForKey is invoke");
    	
		StcSession newSession = null;
		List<StcSession> newList = CCFManager.instance().getSessions();
		for(StcSession user : newList){
			if(user.getSessionUuid().toString().compareTo(inviteUuid)==0){
				newSession = user;
				break;
			}
		}		
		
		if(newSession == null) return;
		
		Log.e(TAG, "setStringForKey is invoke1");
		
		CCFManager.instance().inviteSession(newSession);
		*/
	}
}
