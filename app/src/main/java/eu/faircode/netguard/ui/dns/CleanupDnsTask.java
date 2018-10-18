package eu.faircode.netguard.ui.dns;

import android.os.AsyncTask;
import android.util.Log;

import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.db.DatabaseHelper;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class CleanupDnsTask extends AsyncTask<Object, Object, Object> {

    private final ActivityDns activity;

    CleanupDnsTask(ActivityDns activityDns) {
        this.activity = activityDns;
    }

    @Override
    protected Long doInBackground(Object... objects) {
        Log.i(ActivityDns.TAG, "Cleanup DNS");
        DatabaseHelper.getInstance(activity).cleanupDns();
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        ServiceSinkhole.reload("DNS cleanup", activity, false);
        activity.updateAdapter();
    }
}
