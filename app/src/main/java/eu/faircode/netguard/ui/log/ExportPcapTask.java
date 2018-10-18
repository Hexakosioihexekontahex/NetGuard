package eu.faircode.netguard.ui.log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import eu.faircode.netguard.IFiles;
import eu.faircode.netguard.R;
import eu.faircode.netguard.ServiceSinkhole;

import static eu.faircode.netguard.ui.log.ActivityLog.TAG;

/**
 * Created by Max
 * on 18/10/2018.
 */
class ExportPcapTask extends AsyncTask<Object, Object, Throwable> {

    private final ActivityLog activity;
    private final Intent data;

    ExportPcapTask(ActivityLog activityLog, Intent data) {
        activity = activityLog;
        this.data = data;
    }

    @Override
    protected Throwable doInBackground(Object... objects) {
        OutputStream out = null;
        FileInputStream in = null;
        try {
            // Stop capture
            ServiceSinkhole.setPcap(false, activity);

            Uri target = data.getData();
            if (data.hasExtra("org.openintents.extra.DIR_PATH"))
                target = Uri.parse(target + "/netguard.pcap");
            Log.i(TAG, "Export PCAP URI=" + target);
            out = activity.getContentResolver().openOutputStream(target);

            final File pcap = new File(activity.getDir(IFiles.DATA_DIR, Context.MODE_PRIVATE), IFiles.PCAP);
            in = new FileInputStream(pcap);

            int len;
            long total = 0;
            final byte[] buf = new byte[4096];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                total += len;
            }
            Log.i(TAG, "Copied bytes=" + total);

            return null;
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            return ex;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
            if (in != null)
                try {
                    in.close();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }

            // Resume capture
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            if (prefs.getBoolean("pcap", false)) {
                ServiceSinkhole.setPcap(true, activity);
            }
        }
    }

    @Override
    protected void onPostExecute(Throwable ex) {
        if (ex == null) {
            Toast.makeText(activity, R.string.msg_completed, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(activity, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
