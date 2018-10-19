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

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import eu.faircode.netguard.Forward;
import eu.faircode.netguard.R;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.parser.ICursorParser;

public class AdapterForwarding extends CursorAdapter {
    private final ICursorParser<Forward> parser;

    AdapterForwarding(Context context, Cursor cursor, ICursorParser<Forward> parser) {
        super(context, cursor, 0);
        this.parser = parser;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.forward, parent, false);
        view.setTag(new ForwardVH(view));
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ((ForwardVH)view.getTag()).bindItem(context, cursor);
    }

    private class ForwardVH {

        private final View root;
        private final TextView tvProtocol;
        private final TextView tvDPort;
        private final TextView tvRAddr;
        private final TextView tvRPort;
        private final TextView tvRUid;

        ForwardVH(View view){
            root = view;
            tvProtocol = view.findViewById(R.id.tvProtocol);
            tvDPort = view.findViewById(R.id.tvDPort);
            tvRAddr = view.findViewById(R.id.tvRAddr);
            tvRPort = view.findViewById(R.id.tvRPort);
            tvRUid = view.findViewById(R.id.tvRUid);
        }

        void bindItem(Context context, Cursor cursor) {
            final Forward item = parser.parseItem(cursor);
            tvProtocol.setText(Util.getProtocolName(item.protocol, 0, false));
            tvDPort.setText(String.valueOf(item.dport));
            tvRAddr.setText(item.raddr);
            tvRPort.setText(String.valueOf(item.rport));
            tvRUid.setText(TextUtils.join(", ", Util.getApplicationNames(item.ruid, context)));
        }
    }
}
