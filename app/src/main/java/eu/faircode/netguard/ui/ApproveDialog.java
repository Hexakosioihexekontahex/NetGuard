package eu.faircode.netguard.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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
public class ApproveDialog extends AlertDialog implements DialogInterface.OnClickListener {

    private final Util.DoubtListener listener;

    public ApproveDialog(@NonNull Context context, int explanation, Util.DoubtListener listener) {
        super(context);
        this.listener = listener;
        final View view = LayoutInflater.from(context).inflate(R.layout.sure, null, false);
        final TextView tvExplanation = view.findViewById(R.id.tvExplanation);
        tvExplanation.setText(explanation);

        setView(view);
        setCancelable(true);
        setButton(DialogInterface.BUTTON_POSITIVE, getString(context, android.R.string.yes), this);
        setButton(DialogInterface.BUTTON_NEGATIVE, getString(context, android.R.string.no), this);
    }

    private String getString(Context context, int strResId) {
        return context.getResources().getString(strResId);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(DialogInterface.BUTTON_POSITIVE == which && listener != null){
            listener.onSure();
        }
    }
}
