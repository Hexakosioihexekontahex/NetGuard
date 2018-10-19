package eu.faircode.netguard.ui.dns;

import eu.faircode.netguard.Util;
import eu.faircode.netguard.ui.fr.CursorListFragmentContract;

/**
 * Created by Max
 * on 18/10/2018.
 */
public class DnsListContract {

    public interface IHost extends CursorListFragmentContract.IHost {

        void setupToolbar(int strResId, boolean homeAsUp);

        void showApproveDialog(int explanationResId, Util.DoubtListener listener);
    }
}
