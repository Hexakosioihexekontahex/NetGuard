package eu.faircode.netguard.ui.log;

import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class GetHostNameTask extends AsyncTask<String, Object, String> {

    private final TextView tvDaddr;
    private final String daddr;

    GetHostNameTask(TextView tvDaddr, String daddr) {
        this.tvDaddr = tvDaddr;
        this.daddr = daddr;
    }

    @Override
    protected void onPreExecute() {
        ViewCompat.setHasTransientState(tvDaddr, true);
    }

    @Override
    protected String doInBackground(String... args) {
        try {
            return InetAddress.getByName(daddr).getHostName();
        } catch (UnknownHostException ignored) {
            return args[0];
        }
    }

    @Override
    protected void onPostExecute(String name) {
        tvDaddr.setText(">" + name);
        ViewCompat.setHasTransientState(tvDaddr, false);
    }

}
