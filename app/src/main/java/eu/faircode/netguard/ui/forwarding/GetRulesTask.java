package eu.faircode.netguard.ui.forwarding;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.List;

import eu.faircode.netguard.Rule;

public class GetRulesTask extends AsyncTask<Object, Object, List<Rule>> {

    private final ActivityForwarding activity;
    private final ProgressBar pbRuid;
    private final Spinner spRuid;

    GetRulesTask(ActivityForwarding activityForwarding, ProgressBar pbRuid, Spinner spRuid) {
        activity = activityForwarding;
        this.pbRuid = pbRuid;
        this.spRuid = spRuid;
    }

    @Override
    protected void onPreExecute() {
        pbRuid.setVisibility(View.VISIBLE);
        spRuid.setVisibility(View.GONE);
    }

    @Override
    protected List<Rule> doInBackground(Object... objects) {
        return Rule.getRules(true, activity);
    }

    @Override
    protected void onPostExecute(List<Rule> rules) {
        spRuid.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_spinner_item, rules));
        pbRuid.setVisibility(View.GONE);
        spRuid.setVisibility(View.VISIBLE);
    }
}
