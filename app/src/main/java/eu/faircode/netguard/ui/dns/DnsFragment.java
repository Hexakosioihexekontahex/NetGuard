package eu.faircode.netguard.ui.dns;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CursorAdapter;

import eu.faircode.netguard.R;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.db.DatabaseHelper;
import eu.faircode.netguard.dto.DnsItem;
import eu.faircode.netguard.parser.DnsParser;
import eu.faircode.netguard.parser.ICursorParser;
import eu.faircode.netguard.ui.fr.CursorListFragment;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class DnsFragment extends CursorListFragment<DnsListContract.IHost> implements ITaskListener, Util.DoubtListener {
    public static final String TAG = "NetGuard.DNS";

    public static Fragment newInstance(){
        return new DnsFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(host != null){
            host.setupToolbar(R.string.setting_show_resolved, true);
        }
        final Cursor cursor = DatabaseHelper.getInstance(getActivity()).getDns();
        final ICursorParser<DnsItem> parser = new DnsParser(cursor);
        setListAdapter(new AdapterDns(getActivity(), cursor, parser));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dns, menu);
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
                if(host != null){
                    host.showApproveDialog(R.string.menu_clear, this);
                }
                return true;
        }
        return false;
    }

    @Override
    public void updateAdapter() {
        final CursorAdapter listAdapter = (CursorAdapter)getListAdapter();
        if(listAdapter != null){
            listAdapter.changeCursor(DatabaseHelper.getInstance(getActivity()).getDns());
        }
    }

    @Override
    public void onSure() {
        new ClearDnsTask(DnsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
