package eu.faircode.netguard.parser;

import android.database.Cursor;

import eu.faircode.netguard.Forward;
import eu.faircode.netguard.db.ForwardDB;

public class ForwardParser implements ICursorParser<Forward> {

    private final int colProtocol;
    private final int colDPort;
    private final int colRAddr;
    private final int colRPort;
    private final int colRUid;

    public ForwardParser(Cursor cursor) {
        colProtocol = cursor.getColumnIndex(ForwardDB.COL_PROTOCOL);
        colDPort = cursor.getColumnIndex(ForwardDB.COL_DPORT);
        colRAddr = cursor.getColumnIndex(ForwardDB.COL_RADDR);
        colRPort = cursor.getColumnIndex(ForwardDB.COL_RPORT);
        colRUid = cursor.getColumnIndex(ForwardDB.COL_RUID);
    }

    @Override
    public Forward parseItem(Cursor cursor) {
        return new Forward(cursor.getInt(colProtocol), cursor.getInt(colDPort), cursor.getString(colRAddr),
                cursor.getInt(colRPort), cursor.getInt(colRUid));
    }
}
