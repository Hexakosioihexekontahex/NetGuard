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

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import eu.faircode.netguard.Forward;
import eu.faircode.netguard.R;
import eu.faircode.netguard.ServiceSinkhole;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.db.DatabaseHelper;
import eu.faircode.netguard.parser.ForwardParser;
import eu.faircode.netguard.parser.ICursorParser;
import eu.faircode.netguard.ui.ActionBarActivity;

public class ActivityForwarding extends ActionBarActivity implements AdapterView.OnItemClickListener, DialogInterface.OnDismissListener {
    private boolean running;
    private ListView lvForwarding;
    private AdapterForwarding adapter;
    private AlertDialog dialog = null;
    private ICursorParser<Forward> parser;

    private final DatabaseHelper.ForwardChangedListener listener = new DatabaseHelper.ForwardChangedListener() {
        @Override
        public void onChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null)
                        adapter.changeCursor(DatabaseHelper.getInstance(ActivityForwarding.this).getForwarding());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        running = true;

        lvForwarding = findViewById(R.id.lvForwarding);
        resetAdapter();
        lvForwarding.setOnItemClickListener(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.forwarding;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setting_forwarding;
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHelper.getInstance(this).addForwardChangedListener(listener);
        if (adapter != null)
            adapter.changeCursor(DatabaseHelper.getInstance(ActivityForwarding.this).getForwarding());
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseHelper.getInstance(this).removeForwardChangedListener(listener);
    }

    @Override
    protected void onDestroy() {
        running = false;
        adapter = null;
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forwarding, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                dialog = new AddForwardDialog(ActivityForwarding.this);
                dialog.setOnDismissListener(this);
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Forward item = parser.parseItem((Cursor) adapter.getItem(position));

        final PopupMenu popup = new PopupMenu(ActivityForwarding.this, view);
        popup.inflate(R.menu.forward);
        popup.getMenu().findItem(R.id.menu_port).setTitle(
                Util.getProtocolName(item.protocol, 0, false) + " " +
                        item.dport + " > " + item.raddr + "/" + item.rport);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_delete) {
                    DatabaseHelper.getInstance(ActivityForwarding.this).deleteForward(item.protocol, item.dport);
                    ServiceSinkhole.reload("forwarding", ActivityForwarding.this, false);
                    adapter = new AdapterForwarding(ActivityForwarding.this,
                            DatabaseHelper.getInstance(ActivityForwarding.this).getForwarding(), parser);
                    lvForwarding.setAdapter(adapter);
                }
                return false;
            }
        });

        popup.show();
    }

    public boolean isRunning() {
        return running;
    }

    public void resetAdapter() {
        final Cursor cursor = DatabaseHelper.getInstance(this).getForwarding();
        if(parser == null) {
            parser = new ForwardParser(cursor);
        }
        adapter = new AdapterForwarding(this, cursor, parser);
        lvForwarding.setAdapter(adapter);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialog = null;
    }
}
