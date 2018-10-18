package eu.faircode.netguard.parser;

import android.database.Cursor;

public interface ICursorParser<T> {

    T parseItem(Cursor cursor);
}
