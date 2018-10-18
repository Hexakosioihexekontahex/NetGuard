package eu.faircode.netguard.ui.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import eu.faircode.netguard.IFiles;
import eu.faircode.netguard.IPrefs;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.db.DatabaseHelper;

import static eu.faircode.netguard.ui.log.ActivityLog.TAG;

/**
 * Created by Owner
 * on 18/10/2018.
 */
public class ClearLogTask extends AsyncTask<Object, Object, Object> {
    private final SharedPreferences prefs;
    private final File pcap_file;
    private final ActivityLog activity;

    public ClearLogTask(ActivityLog activity) {
        this.activity = activity;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        pcap_file = new File(activity.getDir(IFiles.DATA_DIR, Context.MODE_PRIVATE), IFiles.PCAP);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        DatabaseHelper.getInstance(activity).clearLog(-1);
        if (prefs.getBoolean(IPrefs.KEY_PCAP, false)) {
            ServiceSinkhole.setPcap(false, activity);
            if (pcap_file.exists() && !pcap_file.delete()) {
                Log.w(TAG, "Delete PCAP failed");
            }
            ServiceSinkhole.setPcap(true, activity);
        }
        else {
            if (pcap_file.exists() && !pcap_file.delete())
                Log.w(TAG, "Delete PCAP failed");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (activity.isRunning())
            activity.updateAdapter();
    }
}
