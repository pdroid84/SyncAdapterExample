package com.pdroid84.deb.syncadapterexample.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DebAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private DebAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.d("DEB",DebAuthenticatorService.class.getName() + "->onCreate is called");
        // Create a new authenticator object
        mAuthenticator = new DebAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("DEB",DebAuthenticatorService.class.getName() + "->onBind is called");
        return mAuthenticator.getIBinder();
    }
}
