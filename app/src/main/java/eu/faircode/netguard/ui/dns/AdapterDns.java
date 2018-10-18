package eu.faircode.netguard.ui.dns;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.faircode.netguard.R;
import eu.faircode.netguard.lib.IPrefs;
import eu.faircode.netguard.lib.db.DnsDB;

public class AdapterDns extends CursorAdapter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd HH:mm", Locale.getDefault());
    private final int colorExpired;
    private final int colTime;
    private final int colQName;
    private final int colAName;
    private final int colResource;
    private final int colTTL;

    AdapterDns(Context context, Cursor cursor) {
        super(context, cursor, 0);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(IPrefs.KEY_THEME_DARK, false)) {
            colorExpired = Color.argb(128, Color.red(Color.DKGRAY), Color.green(Color.DKGRAY), Color.blue(Color.DKGRAY));
        }
        else {
            colorExpired = Color.argb(128, Color.red(Color.LTGRAY), Color.green(Color.LTGRAY), Color.blue(Color.LTGRAY));
        }

        colTime = cursor.getColumnIndex(DnsDB.COL_TIME);
        colQName = cursor.getColumnIndex(DnsDB.COL_GNAME);
        colAName = cursor.getColumnIndex(DnsDB.COL_ANAME);
        colResource = cursor.getColumnIndex(DnsDB.COL_RESOURCE);
        colTTL = cursor.getColumnIndex(DnsDB.COL_TTL);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = LayoutInflater.from(context).inflate(R.layout.dns, parent, false);
        v.setTag(new DnsVH(v));
        return v;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        final long time = cursor.getLong(colTime);
        final int ttl = cursor.getInt(colTTL);
        final long now = new Date().getTime();
        final boolean expired = (time + ttl < now);
        view.setBackgroundColor(expired ? colorExpired : Color.TRANSPARENT);

        final DnsVH holder = (DnsVH) view.getTag();
        holder.tvTime.setText(sdf.format(time));
        holder.tvQName.setText(cursor.getString(colQName));
        holder.tvAName.setText(cursor.getString(colAName));
        holder.tvResource.setText(cursor.getString(colResource));
        holder.tvTTL.setText(context.getString(R.string.plus, Integer.toString(ttl / 1000)));
    }


    private static class DnsVH {
        final View root;
        final TextView tvTime;
        final TextView tvQName;
        final TextView tvAName;
        final TextView tvResource;
        final TextView tvTTL;

        DnsVH(View view) {
            root = view;
            tvTime = view.findViewById(R.id.tvTime);
            tvQName = view.findViewById(R.id.tvQName);
            tvAName = view.findViewById(R.id.tvAName);
            tvResource = view.findViewById(R.id.tvResource);
            tvTTL = view.findViewById(R.id.tvTTL);
        }
    }
}
