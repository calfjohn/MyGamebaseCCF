package org.cocos2dx.cpp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import org.cocos2dx.cpp.R;
import com.intel.stc.lib.GadgetRegistration;
import com.intel.stc.utility.StcApplicationId;
import com.intel.stc.lib.AppRegisterService;

public class MyAppRegister extends AppRegisterService {
	private static final String TAG = "MyAppRegister";
	public static final String      LAUNCH_INTENT       = "org.cocos2dx.cpp";
    public static final String      APP_UUID                = "7A1B397B-B576-44C4-943F-1BEF4F490C06";
    // Simple Chat Keys
    private static final String     clientId            = "WqODOHahg3xw6WVB0BbTMi9yazTkBoQG";
    private static final String     clientSecret        = "i8wP2TGxoVjINdX6";
    private static final boolean    allowCloudTransport = true;
    static final StcApplicationId   id                  = new StcApplicationId(APP_UUID, clientId, clientSecret,
                                                                allowCloudTransport);
                                                                
    //Redirect URL to perform cloud registration.
    static final String             redirectURL         = "http://www.intel.com/robots.txt";
    
    //Override method getGadgetList() to register the app with CCF, this will return list of registered GadgetRegistration.
    @Override
    protected List<GadgetRegistration> getGadgetList(Context context) {
    	Log.i(TAG,"getGadgetList");
        String appName = context.getString(R.string.app_name);
        String appDescription = context.getString(R.string.app_name);
        ArrayList<GadgetRegistration> list = new ArrayList<GadgetRegistration>();
        list.add(new GadgetRegistration(appName, R.drawable.ic_launcher, APP_UUID, appDescription, LAUNCH_INTENT, 2, R.string.app_name, R.string.app_name, 0, context));
        return list;
    }
}