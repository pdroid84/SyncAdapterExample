package com.pdroid84.deb.syncadapterexample.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by debashis on 22/07/15.
 */
public class DebSyncService extends Service {
    // Object to use as a thread-safe lock
    private static final Object mSyncAdapterLock = new Object();
    // Storage for an instance of the sync adapter
    private static DebSyncAdapter mDebSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("DEB", "DebSyncService -> onCreate is called");
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (mSyncAdapterLock) {
            if (mDebSyncAdapter == null) {
                mDebSyncAdapter = new DebSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("DEB", "DebSyncService -> onBind is called");
         /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return mDebSyncAdapter.getSyncAdapterBinder();
    }
}
