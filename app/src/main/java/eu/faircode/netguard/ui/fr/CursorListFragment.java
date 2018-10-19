package eu.faircode.netguard.ui.fr;

import android.content.Context;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class CursorListFragment<T> extends android.support.v4.app.ListFragment {

    protected T host;

    public CursorListFragment(){
        super();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        host = (T)context;
    }

    @Override
    public void onDetach() {
        host = null;
        super.onDetach();
    }
}
