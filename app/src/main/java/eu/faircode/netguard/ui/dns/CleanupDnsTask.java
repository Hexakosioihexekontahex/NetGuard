package eu.faircode.netguard.ui.dns;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import eu.faircode.netguard.ApplicationEx;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.db.DatabaseHelper;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class CleanupDnsTask extends AsyncTask<Object, Object, Object> {

    private final WeakReference<ITaskListener> activity;

    CleanupDnsTask(ITaskListener activityDns) {
        this.activity = new WeakReference<>(activityDns);
    }

    @Override
    protected Long doInBackground(Object... objects) {
        Log.i(DnsFragment.TAG, "Cleanup DNS");
        DatabaseHelper.getInstance(ApplicationEx.getInstance()).cleanupDns();
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        ServiceSinkhole.reload("DNS cleanup", ApplicationEx.getInstance(), false);
        final ITaskListener listener = activity.get();
        if(listener != null) {
            listener.updateAdapter();
        }
    }
}
