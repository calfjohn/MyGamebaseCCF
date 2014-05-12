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

import org.cocos2dx.lib.Cocos2dxActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.intel.stc.lib.StcLib;
import com.intel.stc.slib.IStcServInetClient;

public class AppActivity extends Cocos2dxActivity implements IStcServInetClient {
	public static final String TAG = "Cocos2dxActivity";
	public static final String SERVICE_INTENT = "org.cocos2dx.cpp.CCFManager";

	boolean isBound = false;

	private CCFManager mService;
	private Cocos2dxServiceConnection mConnection = new Cocos2dxServiceConnection();
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doStartService();
	}

	@Override
	public void libPrepared(StcLib arg0) {
		mHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(AppActivity.this, "Platform Prepared",
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void platformError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void platformMissing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestStartActivityForResult(Intent arg0) {
		// TODO Auto-generated method stub

	}

	protected void doStartService() {
		Log.i(TAG, "starting service");
		Intent servIntent = new Intent(SERVICE_INTENT);
		startService(servIntent);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "resuming");
		doBindService();
		super.onResume();
	}

	private void doBindService() {
		if (!isBound) {
			Log.i(TAG, "binding service");
			Intent servIntent = new Intent(SERVICE_INTENT);
			isBound = bindService(servIntent, mConnection, 0);
			if (!isBound)
				Log.i(TAG, "service did not bind.");
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "pausing");
		doUnbindService();
		super.onPause();
	}

	private void doUnbindService() {
		if (isBound) {
			Log.i(TAG, "unbinding service ");
			isBound = false;
			unbindService(mConnection);
		}
	}

	private void doStopService() {
		Log.i(TAG, "shutting down");
		Intent servIntent = new Intent(SERVICE_INTENT);

		doUnbindService();
		stopService(servIntent);
	}

	/* ServiceConnection implementation */
	public class Cocos2dxServiceConnection implements ServiceConnection {
		boolean serviceStopped = false;

		public void onServiceConnected(ComponentName className, IBinder binder) {
			synchronized (this) {
				Log.i(TAG, "service connected.");
				mService = (CCFManager) ((CCFManager.StcServInetBinder) binder).getService();
				if (mService != null)
					mService.setLibPreparedCallback(AppActivity.this, mHandler);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i(TAG, "service disconnected.");

			mService = null;
		}

	};	
}
