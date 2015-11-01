package com.pdroid84.deb.syncadapterexample.gcm;

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created on 30/10/2015.
 */
public class DebInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "DebInstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, DebRegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]
}
