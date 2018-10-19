package eu.faircode.netguard.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import eu.faircode.netguard.R;
import eu.faircode.netguard.Util;
import eu.faircode.netguard.ui.dns.DnsFragment;
import eu.faircode.netguard.ui.dns.DnsListContract;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class ListFragmentActivity extends AppCompatActivity implements DnsListContract.IHost {

    private static final String ACTION_DNS = "item_dns";

    public static Intent newIntentDns(Context ctx) {
        return new Intent(ctx, ListFragmentActivity.class).setAction(ACTION_DNS);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        final Intent intent = getIntent();
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_DNS.equals(action)){
                addFragment(DnsFragment.newInstance());
            }
        }
    }

    private void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment).commit();
    }

    @Override
    public void setupToolbar(int strResId, boolean homeAsUp) {
        final ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setTitle(strResId);
            bar.setDisplayHomeAsUpEnabled(homeAsUp);
        }
    }

    @Override
    public void showApproveDialog(int explanationResId, Util.DoubtListener listener) {
        ApproveDialogFragment.newInstance(explanationResId, listener).show(getSupportFragmentManager(), null);
    }
}
