package eu.faircode.netguard.ui.forwarding;


/*
    This file is part of NetGuard.

    NetGuard is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2018 by Marcel Bokhorst (M66B)
*/

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;

import eu.faircode.netguard.Forward;
import eu.faircode.netguard.Protocols;
import eu.faircode.netguard.R;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.db.DatabaseHelper;

public class ActivityForwardApproval extends Activity implements View.OnClickListener {
    private static final String TAG = "NetGuard.Forward";
    private static final String ACTION_START_PORT_FORWARD = "eu.faircode.netguard.START_PORT_FORWARD";
    private static final String ACTION_STOP_PORT_FORWARD = "eu.faircode.netguard.STOP_PORT_FORWARD";

    // нахуя?
    static {
        try {
            System.loadLibrary("netguard");
        } catch (UnsatisfiedLinkError ignored) {
            System.exit(1);
        }
    }

    private Forward forward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forwardapproval);

        final Intent i = getIntent();
        final String addr = i.getStringExtra("raddr");

        forward = new Forward(i.getIntExtra("protocol", 0),
                i.getIntExtra("dport", 0), (addr == null ? "127.0.0.1" : addr),
                i.getIntExtra("rport", 0), i.getIntExtra("ruid", 0));
        try {
            final InetAddress iraddr = InetAddress.getByName(forward.raddr);
            if (forward.rport < 1024 && (iraddr.isLoopbackAddress() || iraddr.isAnyLocalAddress())) {
                throw new IllegalArgumentException("Port forwarding to privileged port on local address not possible");
            }
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            finish();
        }

        final String pname;
        if (forward.protocol == Protocols.TCP) {
            pname = getString(R.string.menu_protocol_tcp);
        }
        else if (forward.protocol == Protocols.UDP) {
            pname = getString(R.string.menu_protocol_udp);
        }
        else {
            pname = Integer.toString(forward.protocol);
        }

        final TextView tvForward = findViewById(R.id.tvForward);
        if (ACTION_START_PORT_FORWARD.equals(i.getAction())) {
            tvForward.setText(getString(R.string.msg_start_forward,
                    pname, forward.dport, forward.raddr, forward.rport,
                    TextUtils.join(", ", Util.getApplicationNames(forward.ruid, this))));
        }
        else {
            tvForward.setText(getString(R.string.msg_stop_forward, pname, forward.dport));
        }

        findViewById(R.id.btnOk).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(R.id.btnOk == id){
            if (ACTION_START_PORT_FORWARD.equals(getIntent().getAction())) {
/*
am start -a eu.faircode.netguard.START_PORT_FORWARD \
-n eu.faircode.netguard/eu.faircode.netguard.ui.forwarding.ActivityForwardApproval \
--ei protocol 17 \
--ei dport 53 \
--es raddr 8.8.4.4 \
--ei rport 53 \
--ei ruid 9999 \
--user 0
*/
                Log.i(TAG, "Start forwarding protocol " + forward.protocol + " port " +
                        forward.dport + " to " + forward.raddr + "/" + forward.rport + " uid " + forward.ruid);
                final DatabaseHelper dh = DatabaseHelper.getInstance(ActivityForwardApproval.this);
                dh.deleteForward(forward.protocol, forward.dport);
                dh.addForward(forward.protocol, forward.dport, forward.raddr, forward.rport, forward.ruid);

            }
            else if (ACTION_STOP_PORT_FORWARD.equals(getIntent().getAction())) {
/*
am start -a eu.faircode.netguard.STOP_PORT_FORWARD \
-n eu.faircode.netguard/eu.faircode.netguard.ui.forwarding.ActivityForwardApproval \
--ei protocol 17 \
--ei dport 53 \
--user 0
*/
                Log.i(TAG, "Stop forwarding protocol " + forward.protocol + " port " + forward.dport);
                DatabaseHelper.getInstance(ActivityForwardApproval.this).deleteForward(forward.protocol, forward.dport);
            }

            ServiceSinkhole.reload("forwarding", ActivityForwardApproval.this, false);

            finish();
        }
        else if(R.id.btnCancel == id){
            finish();
        }
    }
}
