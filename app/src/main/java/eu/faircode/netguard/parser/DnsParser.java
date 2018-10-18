package eu.faircode.netguard.parser;

import android.database.Cursor;

import eu.faircode.netguard.db.DnsDB;
import eu.faircode.netguard.dto.DnsItem;

public class DnsParser implements ICursorParser<DnsItem> {

    private final int colTime;
    private final int colQName;
    private final int colAName;
    private final int colResource;
    private final int colTTL;

    public DnsParser(Cursor cursor) {
        colTime = cursor.getColumnIndex(DnsDB.COL_TIME);
        colQName = cursor.getColumnIndex(DnsDB.COL_GNAME);
        colAName = cursor.getColumnIndex(DnsDB.COL_ANAME);
        colResource = cursor.getColumnIndex(DnsDB.COL_RESOURCE);
        colTTL = cursor.getColumnIndex(DnsDB.COL_TTL);
    }

    @Override
    public DnsItem parseItem(Cursor cursor) {
        return new DnsItem(cursor.getLong(colTime), cursor.getInt(colTTL), cursor.getString(colAName),
                cursor.getString(colQName), cursor.getString(colResource));
    }
}
