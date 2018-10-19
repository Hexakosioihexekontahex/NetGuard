package eu.faircode.netguard.ui.forwarding;

import android.os.AsyncTask;
import android.widget.Toast;

import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.db.DatabaseHelper;

class AddForwardTask extends AsyncTask<Object, Object, Throwable>  {

    private final ActivityForwarding activity;
    private final int protocol;
    private final int dport;
    private final String raddr;
    private final int rport;
    private final int ruid;

    AddForwardTask(ActivityForwarding activity, int protocol, int dport, String raddr, int rport, int ruid) {
        this.activity = activity;
        this.protocol = protocol;
        this.dport = dport;
        this.raddr = raddr;
        this.rport = rport;
        this.ruid = ruid;
    }

    @Override
    protected Throwable doInBackground(Object... objects) {
        try {
            DatabaseHelper.getInstance(activity).addForward(protocol, dport, raddr, rport, ruid);
            return null;
        } catch (Throwable ex) {
            return ex;
        }
    }

    @Override
    protected void onPostExecute(Throwable ex) {
        if (activity.isRunning())
            if (ex == null) {
                ServiceSinkhole.reload("forwarding", activity, false);
                activity.resetAdapter();
            } else {
                Toast.makeText(activity, ex.toString(), Toast.LENGTH_LONG).show();
            }
    }
}
