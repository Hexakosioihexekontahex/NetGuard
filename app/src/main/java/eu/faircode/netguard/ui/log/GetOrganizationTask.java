package eu.faircode.netguard.ui.log;

import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import eu.faircode.netguard.Util;

/**
 * Created by Max
 * on 18/10/2018.
 */
class GetOrganizationTask extends AsyncTask<String, Object, String> {

    private final TextView tvOrganization;
    private final String daddr;

    GetOrganizationTask(TextView tvOrganization, String daddr) {
        this.tvOrganization = tvOrganization;
        this.daddr = daddr;
    }

    @Override
    protected void onPreExecute() {
        ViewCompat.setHasTransientState(tvOrganization, true);
    }

    @Override
    protected String doInBackground(String... args) {
        try {
            return Util.getOrganization(daddr);
        } catch (Throwable ex) {
            Log.w(AdapterLog.TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            return null;
        }
    }

    @Override
    protected void onPostExecute(String organization) {
        if (organization != null) {
            tvOrganization.setText(organization);
            tvOrganization.setVisibility(View.VISIBLE);
        }
        ViewCompat.setHasTransientState(tvOrganization, false);
    }
}
