package eu.faircode.netguard.dto;

import eu.faircode.netguard.Protocols;

public class LogItem {

    public final long time;
    public final int version;
    public final int protocol;
    public final String flags;
    public final String sAddr;
    public final int sPort;
    public final String dAddr;
    public final int dPort;
    public final String dName;
    public final int uid;
    public final String data;
    public final int allowed;
    public final int connection;
    public final int interactive;

    public LogItem(long time, int version, int protocol, String flags, String sAddr, int sPort,
                   String dAddr, int dPort, String dName, int uid, String data, int allowed,
                   int connection, int interactive) {
        this.time = time;
        this.version = version;
        this.protocol = protocol;
        this.flags = flags;
        this.sAddr = sAddr;
        this.sPort = sPort;
        this.dAddr = dAddr;
        this.dPort = dPort;
        this.dName = dName;
        this.uid = uid;
        this.data = data;
        this.allowed = allowed;
        this.connection = connection;
        this.interactive = interactive;
    }

    public boolean hasConnection() {
        return connection == 1;
    }

    public boolean isAllowed() {
        return allowed > 0;
    }

    public boolean isTcpOrUdp() {
        return protocol == Protocols.TCP || protocol == Protocols.UDP;
    }

    public boolean isSPortInValid() {
        return sPort < 0;
    }

    public boolean isDPortInValid() {
        return dPort < 0;
    }
}
