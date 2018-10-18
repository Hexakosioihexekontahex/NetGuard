package eu.faircode.netguard.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import eu.faircode.netguard.Util;

/**
 * Created by Max
 * on 18/10/2018.
 */
public abstract class ActionBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        updateActionBar();
        final ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setTitle(getTitleResId());
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void updateActionBar() { }

    protected abstract int getLayoutResId();

    protected abstract int getTitleResId();
}
