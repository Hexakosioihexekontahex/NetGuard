package eu.faircode.netguard.ui.dns;

import android.os.AsyncTask;
import android.util.Log;

import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.db.DatabaseHelper;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class ClearDnsTask extends AsyncTask<Object, Object, Object> {

    private final ActivityDns activity;

    ClearDnsTask(ActivityDns activityDns) {
        this.activity = activityDns;
    }

    @Override
    protected Long doInBackground(Object... objects) {
        Log.i(ActivityDns.TAG, "Clear DNS");
        DatabaseHelper.getInstance(activity).clearDns();
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        ServiceSinkhole.reload("DNS clear", activity, false);
        activity.updateAdapter();
    }
}
