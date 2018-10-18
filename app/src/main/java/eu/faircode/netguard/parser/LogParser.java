package eu.faircode.netguard.parser;

import android.database.Cursor;

import eu.faircode.netguard.db.LogDB;
import eu.faircode.netguard.dto.LogItem;

public class LogParser implements ICursorParser<LogItem> {
    private final int colTime;
    private final int colVersion;
    private final int colProtocol;
    private final int colFlags;
    private final int colSAddr;
    private final int colSPort;
    private final int colDAddr;
    private final int colDPort;
    private final int colDName;
    private final int colUid;
    private final int colData;
    private final int colAllowed;
    private final int colConnection;
    private final int colInteractive;

    public LogParser(Cursor cursor) {
        colTime = cursor.getColumnIndex(LogDB.COL_TIME);
        colVersion = cursor.getColumnIndex(LogDB.COL_VERSION);
        colProtocol = cursor.getColumnIndex(LogDB.COL_PROTOCOL);
        colFlags = cursor.getColumnIndex(LogDB.COL_FLAGS);
        colSAddr = cursor.getColumnIndex(LogDB.COL_SADDR);
        colSPort = cursor.getColumnIndex(LogDB.COL_SPORT);
        colDAddr = cursor.getColumnIndex(LogDB.COL_DADDR);
        colDPort = cursor.getColumnIndex(LogDB.COL_DPORT);
        colDName = cursor.getColumnIndex(LogDB.COL_DNAME);
        colUid = cursor.getColumnIndex(LogDB.COL_UID);
        colData = cursor.getColumnIndex(LogDB.COL_DATA);
        colAllowed = cursor.getColumnIndex(LogDB.COL_ALLOWED);
        colConnection = cursor.getColumnIndex(LogDB.COL_CONNECTION);
        colInteractive = cursor.getColumnIndex(LogDB.COL_INTERACTIVE);
    }

    @Override
    public LogItem parseItem(Cursor cursor) {
        // Get values
        final long time = cursor.getLong(colTime);
        final int version = (cursor.isNull(colVersion) ? -1 : cursor.getInt(colVersion));
        final int protocol = (cursor.isNull(colProtocol) ? -1 : cursor.getInt(colProtocol));
        final String flags = cursor.getString(colFlags);
        final String saddr = cursor.getString(colSAddr);
        final int sport = (cursor.isNull(colSPort) ? -1 : cursor.getInt(colSPort));
        final String daddr = cursor.getString(colDAddr);
        final int dport = (cursor.isNull(colDPort) ? -1 : cursor.getInt(colDPort));
        final String dname = (cursor.isNull(colDName) ? null : cursor.getString(colDName));
        int uid = (cursor.isNull(colUid) ? -1 : cursor.getInt(colUid));
        final String data = cursor.getString(colData);
        final int allowed = (cursor.isNull(colAllowed) ? -1 : cursor.getInt(colAllowed));
        final int connection = (cursor.isNull(colConnection) ? -1 : cursor.getInt(colConnection));
        final int interactive = (cursor.isNull(colInteractive) ? -1 : cursor.getInt(colInteractive));


        return new LogItem(cursor.getLong(colTime), (cursor.isNull(colVersion) ? -1 : cursor.getInt(colVersion)),
                (cursor.isNull(colProtocol) ? -1 : cursor.getInt(colProtocol)),
                cursor.getString(colFlags), cursor.getString(colSAddr),
                (cursor.isNull(colSPort) ? -1 : cursor.getInt(colSPort)), cursor.getString(colDAddr),
                (cursor.isNull(colDPort) ? -1 : cursor.getInt(colDPort)),
                (cursor.isNull(colDName) ? null : cursor.getString(colDName)),
                (cursor.isNull(colUid) ? -1 : cursor.getInt(colUid)), cursor.getString(colData),
                (cursor.isNull(colAllowed) ? -1 : cursor.getInt(colAllowed)),
                (cursor.isNull(colConnection) ? -1 : cursor.getInt(colConnection)),
                (cursor.isNull(colInteractive) ? -1 : cursor.getInt(colInteractive)));
    }
}
