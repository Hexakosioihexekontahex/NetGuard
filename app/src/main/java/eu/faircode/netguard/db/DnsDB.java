package eu.faircode.netguard.db;

/**
 * Created by Max
 * on 18/10/2018.
 */
public interface DnsDB {
    String COL_TIME = "time";
    String COL_GNAME = "qname";
    String COL_ANAME = "aname";
    String COL_RESOURCE = "resource";
    String COL_TTL = "ttl";
}
