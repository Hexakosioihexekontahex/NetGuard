package eu.faircode.netguard.ui.log;

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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import eu.faircode.netguard.GlideApp;
import eu.faircode.netguard.IPrefs;
import eu.faircode.netguard.R;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.dto.LogItem;
import eu.faircode.netguard.parser.ICursorParser;
import eu.faircode.netguard.parser.LogParser;

public class AdapterLog extends CursorAdapter {
    static String TAG = "NetGuard.Log";

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final ICursorParser<LogItem> parser;
    private boolean resolve;
    private boolean organization;
    private final int colorOn;
    private final int colorOff;
    private final int iconSize;
    private InetAddress dns1 ;
    private InetAddress dns2;
    private InetAddress vpn4;
    private InetAddress vpn6;

    AdapterLog(Context context, Cursor cursor, boolean resolve, boolean organization) {
        super(context, cursor, 0);
        this.resolve = resolve;
        this.organization = organization;
        parser = new LogParser(cursor);

        final TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorOn, tv, true);
        colorOn = tv.data;
        context.getTheme().resolveAttribute(R.attr.colorOff, tv, true);
        colorOff = tv.data;

        iconSize = Util.dips2pixels(24, context);

        try {
            final List<InetAddress> lstDns = ServiceSinkhole.getDns(context);
            dns1 = (lstDns.size() > 0 ? lstDns.get(0) : null);
            dns2 = (lstDns.size() > 1 ? lstDns.get(1) : null);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            vpn4 = InetAddress.getByName(prefs.getString(IPrefs.KEY_VPN4, "10.1.10.1"));
            vpn6 = InetAddress.getByName(prefs.getString(IPrefs.KEY_VPN6, "fd00:1:fd00:1:fd00:1:fd00:1"));
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            dns1 = null;
            dns2 = null;
            vpn4 = null;
            vpn6 = null;
        }
    }

    void setResolve(boolean resolve) {
        this.resolve = resolve;
    }

    public void setOrganization(boolean organization) {
        this.organization = organization;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.log, parent, false);
        view.setTag(new LogVH(view));
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ((LogVH) view.getTag()).bindItem(context, cursor);
    }

    private class LogVH {
        final TextView tvTime;
        final TextView tvProtocol;
        final TextView tvFlags;
        final TextView tvSAddr;
        final TextView tvSPort;
        final TextView tvDaddr;
        final TextView tvDPort;
        final TextView tvOrganization;
        final ImageView ivIcon;
        final TextView tvUid;
        final TextView tvData;
        final ImageView ivConnection;
        final ImageView ivInteractive;
        final View root;

        LogVH(View view){
            root = view;
            tvTime = view.findViewById(R.id.tvTime);
            tvProtocol = view.findViewById(R.id.tvProtocol);
            tvFlags = view.findViewById(R.id.tvFlags);
            tvSAddr = view.findViewById(R.id.tvSAddr);
            tvSPort = view.findViewById(R.id.tvSPort);
            tvDaddr = view.findViewById(R.id.tvDAddr);
            tvDPort = view.findViewById(R.id.tvDPort);
            tvOrganization = view.findViewById(R.id.tvOrganization);
            ivIcon = view.findViewById(R.id.ivIcon);
            tvUid = view.findViewById(R.id.tvUid);
            tvData = view.findViewById(R.id.tvData);
            ivConnection = view.findViewById(R.id.ivConnection);
            ivInteractive = view.findViewById(R.id.ivInteractive);
        }

        void bindItem(Context context, Cursor cursor) {
            final LogItem item = parser.parseItem(cursor);

            // Show time
            tvTime.setText(sdf.format(item.time));

            // Show connection type
            if (item.connection <= 0) {
                ivConnection.setImageResource(item.allowed > 0 ? R.drawable.host_allowed : R.drawable.host_blocked);
            }
            else {
                if (item.isAllowed()) {
                    ivConnection.setImageResource(item.hasConnection() ? R.drawable.wifi_on : R.drawable.other_on);
                }
                else {
                    ivConnection.setImageResource(item.hasConnection() ? R.drawable.wifi_off : R.drawable.other_off);
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                final Drawable wrap = DrawableCompat.wrap(ivConnection.getDrawable());
                DrawableCompat.setTint(wrap, item.isAllowed() ? colorOn : colorOff);
            }

            // Show if screen on
            if (item.interactive <= 0) {
                ivInteractive.setImageDrawable(null);
            }
            else {
                ivInteractive.setImageResource(R.drawable.screen_on);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    final Drawable wrap = DrawableCompat.wrap(ivInteractive.getDrawable());
                    DrawableCompat.setTint(wrap, colorOn);
                }
            }

            // Show protocol name
            tvProtocol.setText(Util.getProtocolName(item.protocol, item.version, false));

            // SHow TCP flags
            tvFlags.setText(item.flags);
            tvFlags.setVisibility(TextUtils.isEmpty(item.flags) ? View.GONE : View.VISIBLE);

            // Show source and destination port
            if (item.isTcpOrUdp()) {
                tvSPort.setText(item.isSPortInValid() ? "" : getKnownPort(item.sPort));
                tvDPort.setText(item.isDPortInValid() ? "" : getKnownPort(item.dPort));
            }
            else {
                tvSPort.setText(item.isSPortInValid() ? "" : String.valueOf(item.sPort));
                tvDPort.setText(item.isDPortInValid() ? "" : String.valueOf(item.dPort));
            }

            // Application icon
            ApplicationInfo info = null;
            final PackageManager pm = context.getPackageManager();
            final String[] pkg = pm.getPackagesForUid(item.uid);
            if (pkg != null && pkg.length > 0)
                try {
                    info = pm.getApplicationInfo(pkg[0], 0);
                } catch (PackageManager.NameNotFoundException ignored) { }

            if (info == null) {
                ivIcon.setImageDrawable(null);
            }
            else {
                if (info.icon <= 0) {
                    ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                }
                else {
                    final Uri uri = Uri.parse("android.resource://" + info.packageName + "/" + info.icon);
                    GlideApp.with(context).load(uri).override(iconSize, iconSize).into(ivIcon);
                }
            }

            final boolean we = (android.os.Process.myUid() == item.uid);

            // https://android.googlesource.com/platform/system/core/+/master/include/private/android_filesystem_config.h
            int uid = item.uid % 100000; // strip off user ID
            if (uid == -1) {
                tvUid.setText("");
            }
            else if (item.uid == 0) {
                tvUid.setText(context.getString(R.string.title_root));
            }
            else if (item.uid == 9999) {
                tvUid.setText("-"); // nobody
            }
            else {
                tvUid.setText(String.valueOf(item.uid));
            }

            // Show source address
            tvSAddr.setText(getKnownAddress(item.sAddr));

            // Show destination address
            if (!we && resolve && isKnownAddress(item.dAddr)) {
                if (item.dName == null) {
                    tvDaddr.setText(item.dAddr);
                    new GetHostNameTask(tvDaddr, item.dAddr).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    tvDaddr.setText(item.dName);
                }
            }
            else {
                tvDaddr.setText(getKnownAddress(item.dAddr));
            }

            // Show organization
            tvOrganization.setVisibility(View.GONE);
            if (!we && organization && isKnownAddress(item.dAddr)) {
                new GetOrganizationTask(tvOrganization, item.dAddr).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            // Show extra data
            if (TextUtils.isEmpty(item.data)) {
                //tvData.setText("");
                tvData.setVisibility(View.GONE);
            }
            else {
                tvData.setText(item.data);
                tvData.setVisibility(View.VISIBLE);
            }
        }

        private boolean isKnownAddress(String addr) {
            try {
                final InetAddress a = InetAddress.getByName(addr);
                return !a.equals(dns1) && !a.equals(dns2) && !a.equals(vpn4) && !a.equals(vpn6);
            } catch (UnknownHostException ignored) {}
            return true;
        }

        private String getKnownAddress(String addr) {
            try {
                final InetAddress a = InetAddress.getByName(addr);
                if (a.equals(dns1) || a.equals(dns2)) {
                    return "dns";
                }
                if (a.equals(vpn4) || a.equals(vpn6)) {
                    return "vpn";
                }
            } catch (UnknownHostException ignored) {}
            return addr;
        }

        private String getKnownPort(int port) {
            // https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers#Well-known_ports
            switch (port) {
                case 7:
                    return "echo";
                case 25:
                    return "smtp";
                case 53:
                    return "dns";
                case 80:
                    return "http";
                case 110:
                    return "pop3";
                case 143:
                    return "imap";
                case 443:
                    return "https";
                case 465:
                    return "smtps";
                case 993:
                    return "imaps";
                case 995:
                    return "pop3s";
                default:
                    return String.valueOf(port);
            }
        }
    }
}
