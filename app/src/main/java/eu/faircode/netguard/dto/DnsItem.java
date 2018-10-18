package eu.faircode.netguard.dto;

import java.util.Date;

public class DnsItem {
    public final long time;
    public final int ttl;
    public final String aName;
    public final String qName;
    public final String resource;

    public DnsItem(long time, int ttl, String aName, String qName, String resource) {
        this.time = time;
        this.ttl = ttl;
        this.aName = aName;
        this.qName = qName;
        this.resource = resource;
    }

    public boolean isExpired() {
        return (time + ttl < new Date().getTime());
    }
}
