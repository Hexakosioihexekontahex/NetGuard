package eu.faircode.netguard.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import eu.faircode.netguard.R;
import eu.faircode.netguard.Util;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class ApproveDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String EXPLANATION = "explain";
    private Util.DoubtListener listener;

    public static DialogFragment newInstance(int explanationResId, Util.DoubtListener listener){
        final Bundle b = new Bundle();
        b.putInt(EXPLANATION, explanationResId);
        final ApproveDialogFragment f = new ApproveDialogFragment();
        f.setListener(listener);
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.sure, null, false);
        final TextView tvExplanation = view.findViewById(R.id.tvExplanation);

        final Bundle args = getArguments();
        if(args != null){
            tvExplanation.setText(args.getInt(EXPLANATION));
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, this)
                .create();
    }

    public void setListener(Util.DoubtListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(DialogInterface.BUTTON_POSITIVE == which && listener != null) {
            listener.onSure();
        }
        else if(DialogInterface.BUTTON_NEGATIVE == which){
            // Do nothing
        }
    }
}
