package eu.faircode.netguard.ui.forwarding;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.InetAddress;

import eu.faircode.netguard.R;
import eu.faircode.netguard.Rule;

class AddForwardDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private final ActivityForwarding activity;
    private final AsyncTask task;
    private final Spinner spProtocol;
    private final EditText etDPort;
    private final EditText etRAddr;
    private final EditText etRPort;
    private final ProgressBar pbRuid;
    private final Spinner spRuid;

    AddForwardDialog(Context context) {
        super(context);
        activity = (ActivityForwarding)context;
        final View view = LayoutInflater.from(context).inflate(R.layout.forwardadd, null, false);
        spProtocol = view.findViewById(R.id.spProtocol);
        etDPort = view.findViewById(R.id.etDPort);
        etRAddr = view.findViewById(R.id.etRAddr);
        etRPort = view.findViewById(R.id.etRPort);
        pbRuid = view.findViewById(R.id.pbRUid);
        spRuid = view.findViewById(R.id.spRUid);

        setView(view);
        setCancelable(true);

        setButton(DialogInterface.BUTTON_POSITIVE, getString(context, android.R.string.yes), this);
        setButton(DialogInterface.BUTTON_NEGATIVE, getString(context, android.R.string.no), this);

        task = new GetRulesTask(activity, pbRuid, spRuid);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String getString(Context context, int strResId) {
        return context.getResources().getString(strResId);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(DialogInterface.BUTTON_NEGATIVE == which){
            task.cancel(false);
            dialog.dismiss();
        }
        else if(DialogInterface.BUTTON_POSITIVE == which){
            try {
                final int pos = spProtocol.getSelectedItemPosition();
                final String[] values = getContext().getResources().getStringArray(R.array.protocolValues);
                final int protocol = Integer.valueOf(values[pos]);
                final int dport = Integer.parseInt(etDPort.getText().toString());
                final String raddr = etRAddr.getText().toString();
                final int rport = Integer.parseInt(etRPort.getText().toString());
                final int ruid = ((Rule) spRuid.getSelectedItem()).uid;

                final InetAddress iraddr = InetAddress.getByName(raddr);
                if (rport < 1024 && (iraddr.isLoopbackAddress() || iraddr.isAnyLocalAddress())) {
                    throw new IllegalArgumentException("Port forwarding to privileged port on local address not possible");
                }
                new AddForwardTask(activity, protocol, dport, raddr, rport, ruid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            catch (Throwable ex) {
                Toast.makeText(activity, ex.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
