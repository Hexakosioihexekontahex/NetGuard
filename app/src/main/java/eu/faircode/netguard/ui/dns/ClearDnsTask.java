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
public class ClearDnsTask extends AsyncTask<Object, Object, Object> {

    private final WeakReference<ITaskListener> listener;

    ClearDnsTask(ITaskListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected Long doInBackground(Object... objects) {
        Log.i(DnsFragment.TAG, "Clear DNS");
        DatabaseHelper.getInstance(ApplicationEx.getInstance()).clearDns();
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        ServiceSinkhole.reload("DNS clear", ApplicationEx.getInstance(), false);
        final ITaskListener listener = this.listener.get();
        if(listener != null) {
            listener.updateAdapter();
        }
    }
}
