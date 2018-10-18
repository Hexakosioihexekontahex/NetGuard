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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import eu.faircode.netguard.DatabaseHelper;
import eu.faircode.netguard.R;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.ui.ActionBarActivity;
import eu.faircode.netguard.ui.ApproveDialog;

public class ActivityDns extends ActionBarActivity {
    public static final String TAG = "NetGuard.DNS";

    private AdapterDns adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ListView lvDns = findViewById(R.id.lvDns);
        adapter = new AdapterDns(this, DatabaseHelper.getInstance(this).getDns());
        lvDns.setAdapter(adapter);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.resolving;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setting_show_resolved;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dns, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                updateAdapter();
                return true;

            case R.id.menu_cleanup:
                new CleanupDnsTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;

            case R.id.menu_clear:
                new ApproveDialog(this, R.string.menu_clear, new Util.DoubtListener() {
                    @Override
                    public void onSure() {
                        new ClearDnsTask(ActivityDns.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }).show();
                return true;
        }
        return false;
    }

    void updateAdapter() {
        if (adapter != null)
            adapter.changeCursor(DatabaseHelper.getInstance(this).getDns());
    }

    @Override
    protected void onDestroy() {
        adapter = null;
        super.onDestroy();
    }
}
