package eu.faircode.netguard.db;

/**
 * Created by Max
 * on 18/10/2018.
 */
public interface LogDB {
    String COL_TIME = "time";
    String COL_VERSION = "version";
    String COL_PROTOCOL = "protocol";
    String COL_SADDR = "saddr";
    String COL_SPORT = "sport";
    String COL_DADDR = "daddr";
    String COL_DPORT = "dport";
    String COL_DNAME = "dname";
    String COL_UID = "uid";
    String COL_ALLOWED = "allowed";
    String COL_FLAGS = "flags";
    String COL_DATA = "data";
    String COL_CONNECTION = "connection";
    String COL_INTERACTIVE = "interactive";
}
