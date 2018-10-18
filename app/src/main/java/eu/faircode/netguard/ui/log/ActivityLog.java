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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.faircode.netguard.DatabaseHelper;
import eu.faircode.netguard.IAB;
import eu.faircode.netguard.Packet;
import eu.faircode.netguard.R;
import eu.faircode.netguard.Rule;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.lib.IFiles;
import eu.faircode.netguard.lib.IPrefs;
import eu.faircode.netguard.lib.db.LogDB;
import eu.faircode.netguard.ui.ActionBarActivity;
import eu.faircode.netguard.ui.ActivityMain;
import eu.faircode.netguard.ui.ActivityPro;

public class ActivityLog extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        CompoundButton.OnCheckedChangeListener, FilterQueryProvider, AdapterView.OnItemClickListener {
    public static final String TAG = "NetGuard.Log";

    private boolean running = false;
    private ListView lvLog;
    private AdapterLog adapter;
    private MenuItem menuSearch = null;

    private boolean live;
    private boolean resolve;
    private boolean organization;
    private InetAddress vpn4 = null;
    private InetAddress vpn6 = null;
    private SwitchCompat swEnabled;
    private final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    private static final int REQUEST_PCAP = 1;

    private DatabaseHelper.LogChangedListener listener = new DatabaseHelper.LogChangedListener() {
        @Override
        public void onChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateAdapter();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        running = true;
        // Get settings
        final boolean log = prefs.getBoolean(IPrefs.KEY_LOG, false);
        resolve = prefs.getBoolean(IPrefs.KEY_RESOLVE, false);
        organization = prefs.getBoolean(IPrefs.KEY_ORGANIZATION, false);

        // Show disabled message
        findViewById(R.id.tvDisabled).setVisibility(log ? View.GONE : View.VISIBLE);

        // Set enabled switch
        swEnabled.setChecked(log);
        swEnabled.setOnCheckedChangeListener(this);

        // Listen for preference changes
        prefs.registerOnSharedPreferenceChangeListener(this);

        lvLog = findViewById(R.id.lvLog);

        final boolean udp = prefs.getBoolean(IPrefs.KEY_PROTO_UDP, true);
        final boolean tcp = prefs.getBoolean(IPrefs.KEY_PROTO_TCP, true);
        final boolean other = prefs.getBoolean(IPrefs.KEY_PROTO_OTHER, true);
        final boolean allowed = prefs.getBoolean(IPrefs.KEY_TRAFFIC_ALLOWED, true);
        final boolean blocked = prefs.getBoolean(IPrefs.KEY_TRAFFIC_BLOCKED, true);

        adapter = new AdapterLog(this, DatabaseHelper.getInstance(this).getLog(udp, tcp, other, allowed, blocked), resolve, organization);
        adapter.setFilterQueryProvider(this);
        lvLog.setOnItemClickListener(this);
        lvLog.setAdapter(adapter);

        try {
            vpn4 = InetAddress.getByName(prefs.getString(IPrefs.KEY_VPN4, "10.1.10.1"));
            vpn6 = InetAddress.getByName(prefs.getString(IPrefs.KEY_VPN6, "fd00:1:fd00:1:fd00:1:fd00:1"));
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }

        live = true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.logging;
    }

    @Override
    protected int getTitleResId() {
        return R.string.menu_log;
    }

    @Override
    protected void updateActionBar() {
        final View actionView = getLayoutInflater().inflate(R.layout.actionlog, null, false);
        swEnabled = actionView.findViewById(R.id.swEnabled);
        final ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setDisplayShowCustomEnabled(true);
            bar.setCustomView(actionView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (live) {
            DatabaseHelper.getInstance(this).addLogChangedListener(listener);
            updateAdapter();
        }
    }

    @Override
    protected void onPause() {
        if (live) {
            DatabaseHelper.getInstance(this).removeLogChangedListener(listener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        running = false;
        adapter = null;
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        Log.i(TAG, "Preference " + name + "=" + prefs.getAll().get(name));
        if (IPrefs.KEY_LOG.equals(name)) {
            // Get enabled
            final boolean log = prefs.getBoolean(name, false);

            // Display disabled warning
            final TextView tvDisabled = findViewById(R.id.tvDisabled);
            tvDisabled.setVisibility(log ? View.GONE : View.VISIBLE);

            // Check switch state
            final SwitchCompat swEnabled = getSupportActionBar().getCustomView().findViewById(R.id.swEnabled);
            if (swEnabled.isChecked() != log)
                swEnabled.setChecked(log);

            ServiceSinkhole.reload("changed " + name, ActivityLog.this, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logging, menu);

        menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private String getUidForName(String query) {
                if (query != null && query.length() > 0) {
                    for (Rule rule : Rule.getRules(true, ActivityLog.this))
                        if (rule.name != null && rule.name.toLowerCase().contains(query.toLowerCase())) {
                            String newQuery = Integer.toString(rule.uid);
                            Log.i(TAG, "Search " + query + " found " + rule.name + " new " + newQuery);
                            return newQuery;
                        }
                    Log.i(TAG, "Search " + query + " not found");
                }
                return query;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(getUidForName(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(getUidForName(newText));
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (adapter != null)
                    adapter.getFilter().filter(null);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // https://gist.github.com/granoeste/5574148
        final File pcap_file = new File(getDir(IFiles.DATA_DIR, MODE_PRIVATE), IFiles.PCAP);
        final boolean export = (getPackageManager().resolveActivity(getIntentPCAPDocument(), 0) != null);

        menu.findItem(R.id.menu_protocol_udp).setChecked(prefs.getBoolean(IPrefs.KEY_PROTO_UDP, true));
        menu.findItem(R.id.menu_protocol_tcp).setChecked(prefs.getBoolean(IPrefs.KEY_PROTO_TCP, true));
        menu.findItem(R.id.menu_protocol_other).setChecked(prefs.getBoolean(IPrefs.KEY_PROTO_OTHER, true));
        menu.findItem(R.id.menu_traffic_allowed).setEnabled(prefs.getBoolean(IPrefs.KEY_FILTER, false));
        menu.findItem(R.id.menu_traffic_allowed).setChecked(prefs.getBoolean(IPrefs.KEY_TRAFFIC_ALLOWED, true));
        menu.findItem(R.id.menu_traffic_blocked).setChecked(prefs.getBoolean(IPrefs.KEY_TRAFFIC_BLOCKED, true));

        menu.findItem(R.id.menu_refresh).setEnabled(!menu.findItem(R.id.menu_log_live).isChecked());
        menu.findItem(R.id.menu_log_resolve).setChecked(prefs.getBoolean(IPrefs.KEY_RESOLVE, false));
        menu.findItem(R.id.menu_log_organization).setChecked(prefs.getBoolean(IPrefs.KEY_ORGANIZATION, false));
        menu.findItem(R.id.menu_pcap_enabled).setChecked(prefs.getBoolean(IPrefs.KEY_PCAP, false));
        menu.findItem(R.id.menu_pcap_export).setEnabled(pcap_file.exists() && export);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(TAG, "Up");
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_protocol_udp:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_PROTO_UDP, item.isChecked()).apply();
                updateAdapter();
                return true;

            case R.id.menu_protocol_tcp:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_PROTO_TCP, item.isChecked()).apply();
                updateAdapter();
                return true;

            case R.id.menu_protocol_other:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_PROTO_OTHER, item.isChecked()).apply();
                updateAdapter();
                return true;

            case R.id.menu_traffic_allowed:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_TRAFFIC_ALLOWED, item.isChecked()).apply();
                updateAdapter();
                return true;

            case R.id.menu_traffic_blocked:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_TRAFFIC_BLOCKED, item.isChecked()).apply();
                updateAdapter();
                return true;

            case R.id.menu_log_live:
                item.setChecked(!item.isChecked());
                live = item.isChecked();
                if (live) {
                    DatabaseHelper.getInstance(this).addLogChangedListener(listener);
                    updateAdapter();
                }
                else {
                    DatabaseHelper.getInstance(this).removeLogChangedListener(listener);
                }
                return true;

            case R.id.menu_refresh:
                updateAdapter();
                return true;

            case R.id.menu_log_resolve:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_RESOLVE, item.isChecked()).apply();
                adapter.setResolve(item.isChecked());
                adapter.notifyDataSetChanged();
                return true;

            case R.id.menu_log_organization:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_ORGANIZATION, item.isChecked()).apply();
                adapter.setOrganization(item.isChecked());
                adapter.notifyDataSetChanged();
                return true;

            case R.id.menu_pcap_enabled:
                item.setChecked(!item.isChecked());
                prefs.edit().putBoolean(IPrefs.KEY_PCAP, item.isChecked()).apply();
                ServiceSinkhole.setPcap(item.isChecked(), ActivityLog.this);
                return true;

            case R.id.menu_pcap_export:
                startActivityForResult(getIntentPCAPDocument(), REQUEST_PCAP);
                return true;

            case R.id.menu_log_clear:
                new ClearLogTask(this).execute(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;

            case R.id.menu_log_support:
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/M66B/NetGuard/blob/master/FAQ.md#FAQ27"));
                if (getPackageManager().resolveActivity(intent, 0) != null) {
                    startActivity(intent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void updateAdapter() {
        if (adapter != null) {
            final boolean udp = prefs.getBoolean(IPrefs.KEY_PROTO_UDP, true);
            final boolean tcp = prefs.getBoolean(IPrefs.KEY_PROTO_TCP, true);
            final boolean other = prefs.getBoolean(IPrefs.KEY_PROTO_OTHER, true);
            final boolean allowed = prefs.getBoolean(IPrefs.KEY_TRAFFIC_ALLOWED, true);
            final boolean blocked = prefs.getBoolean(IPrefs.KEY_TRAFFIC_BLOCKED, true);
            adapter.changeCursor(DatabaseHelper.getInstance(this).getLog(udp, tcp, other, allowed, blocked));
            if (menuSearch != null && menuSearch.isActionViewExpanded()) {
                final SearchView searchView = (SearchView) menuSearch.getActionView();
                adapter.getFilter().filter(searchView.getQuery().toString());
            }
        }
    }

    private Intent getIntentPCAPDocument() {
        final Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (Util.isPackageInstalled("org.openintents.filemanager", this)) {
                intent = new Intent("org.openintents.action.PICK_DIRECTORY");
            }
            else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=org.openintents.filemanager"));
            }
        } else {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_TITLE, "netguard_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date().getTime()) + ".pcap");
        }
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult request=" + requestCode + " result=" + requestCode + " ok=" + (resultCode == RESULT_OK));
        if (requestCode == REQUEST_PCAP) {
            if (resultCode == RESULT_OK && data != null) {
                handleExportPCAP(data);
            }
        }
        else {
            Log.w(TAG, "Unknown activity result request=" + requestCode);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleExportPCAP(final Intent data) {
        new ExportPcapTask(this, data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        prefs.edit().putBoolean(IPrefs.KEY_LOG, isChecked).apply();
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        return DatabaseHelper.getInstance(ActivityLog.this).searchLog(constraint.toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final PackageManager pm = getPackageManager();
        final Cursor cursor = (Cursor) adapter.getItem(position);
        long time = cursor.getLong(cursor.getColumnIndex(LogDB.COL_TIME));
        int version = cursor.getInt(cursor.getColumnIndex(LogDB.COL_VERSION));
        int protocol = cursor.getInt(cursor.getColumnIndex(LogDB.COL_PROTOCOL));
        final String saddr = cursor.getString(cursor.getColumnIndex(LogDB.COL_SADDR));
        final int sport = (cursor.isNull(cursor.getColumnIndex(LogDB.COL_SPORT)) ? -1 : cursor.getInt(cursor.getColumnIndex(LogDB.COL_SPORT)));
        final String daddr = cursor.getString(cursor.getColumnIndex(LogDB.COL_DADDR));
        final int dport = (cursor.isNull(cursor.getColumnIndex(LogDB.COL_DPORT)) ? -1 : cursor.getInt(cursor.getColumnIndex(LogDB.COL_DPORT)));
        final String dname = cursor.getString(cursor.getColumnIndex(LogDB.COL_DNAME));
        final int uid = (cursor.isNull(cursor.getColumnIndex(LogDB.COL_UID)) ? -1 : cursor.getInt(cursor.getColumnIndex(LogDB.COL_UID)));
        int allowed = (cursor.isNull(cursor.getColumnIndex(LogDB.COL_ALLOWED)) ? -1 : cursor.getInt(cursor.getColumnIndex(LogDB.COL_ALLOWED)));

        // Get external address
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(daddr);
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }

        final String ip;
        final int port;
        if (addr.equals(vpn4) || addr.equals(vpn6)) {
            ip = saddr;
            port = sport;
        } else {
            ip = daddr;
            port = dport;
        }

        // Build popup menu
        final PopupMenu popup = new PopupMenu(ActivityLog.this, findViewById(R.id.vwPopupAnchor));
        popup.inflate(R.menu.log);

        // Application name
        if (uid >= 0) {
            popup.getMenu().findItem(R.id.menu_application).setTitle(TextUtils.join(", ", Util.getApplicationNames(uid, ActivityLog.this)));
        }
        else {
            popup.getMenu().removeItem(R.id.menu_application);
        }

        // Destination IP
        popup.getMenu().findItem(R.id.menu_protocol).setTitle(Util.getProtocolName(protocol, version, false));

        // Whois
        final Intent lookupIP = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dnslytics.com/whois-lookup/" + ip));
        if (pm.resolveActivity(lookupIP, 0) == null) {
            popup.getMenu().removeItem(R.id.menu_whois);
        }
        else {
            popup.getMenu().findItem(R.id.menu_whois).setTitle(getString(R.string.title_log_whois, ip));
        }

        // Lookup port
        final Intent lookupPort = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.speedguide.net/port.php?port=" + port));
        if (port <= 0 || pm.resolveActivity(lookupPort, 0) == null) {
            popup.getMenu().removeItem(R.id.menu_port);
        }
        else {
            popup.getMenu().findItem(R.id.menu_port).setTitle(getString(R.string.title_log_port, port));
        }

        if (!prefs.getBoolean("filter", false)) {
            popup.getMenu().removeItem(R.id.menu_allow);
            popup.getMenu().removeItem(R.id.menu_block);
        }

        final Packet packet = new Packet();
        packet.version = version;
        packet.protocol = protocol;
        packet.daddr = daddr;
        packet.dport = dport;
        packet.time = time;
        packet.uid = uid;
        packet.allowed = (allowed > 0);

        // Time
        popup.getMenu().findItem(R.id.menu_time).setTitle(SimpleDateFormat.getDateTimeInstance().format(time));

        // Handle click
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_application: {
                        final Intent main = new Intent(ActivityLog.this, ActivityMain.class);
                        main.putExtra(ActivityMain.EXTRA_SEARCH, Integer.toString(uid));
                        startActivity(main);
                        return true;
                    }

                    case R.id.menu_whois:
                        startActivity(lookupIP);
                        return true;

                    case R.id.menu_port:
                        startActivity(lookupPort);
                        return true;

                    case R.id.menu_allow:
                        if (IAB.isPurchased(ActivityPro.SKU_FILTER, ActivityLog.this)) {
                            DatabaseHelper.getInstance(ActivityLog.this).updateAccess(packet, dname, 0);
                            ServiceSinkhole.reload("allow host", ActivityLog.this, false);
                            final Intent main = new Intent(ActivityLog.this, ActivityMain.class);
                            main.putExtra(ActivityMain.EXTRA_SEARCH, Integer.toString(uid));
                            startActivity(main);
                        } else {
                            startActivity(new Intent(ActivityLog.this, ActivityPro.class));
                        }
                        return true;

                    case R.id.menu_block:
                        if (IAB.isPurchased(ActivityPro.SKU_FILTER, ActivityLog.this)) {
                            DatabaseHelper.getInstance(ActivityLog.this).updateAccess(packet, dname, 1);
                            ServiceSinkhole.reload("block host", ActivityLog.this, false);
                            final Intent main = new Intent(ActivityLog.this, ActivityMain.class);
                            main.putExtra(ActivityMain.EXTRA_SEARCH, Integer.toString(uid));
                            startActivity(main);
                        } else
                            startActivity(new Intent(ActivityLog.this, ActivityPro.class));
                        return true;

                    case R.id.menu_copy:
                        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        final ClipData clip = ClipData.newPlainText("netguard", dname == null ? daddr : dname);
                        clipboard.setPrimaryClip(clip);
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Show
        popup.show();
    }

    public boolean isRunning() {
        return running;
    }
}
