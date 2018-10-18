package eu.faircode.netguard;

/**
 * Created by Owner
 * on 18/10/2018.
 */
public interface IPrefs {
    String FILE_WIFI = "wifi";
    String FILE_OTHER = "other";
    String FILE_SCREEN_WIFI = "screen_wifi";
    String FILE_SCREEN_OTHER = "screen_other";
    String FILE_ROAMING = "roaming";
    String FILE_LOCKDOWN = "lockdown";
    String FILE_APPLY = "apply";
    String FILE_NOTIFY = "notify";

    String KEY_WHITE_LIST_WIFI = "whitelist_wifi";
    String KEY_WHITELIST_OTHER = "whitelist_other";
    String KEY_SCREEN_WIFI = "screen_wifi";
    String KEY_SCREEN_OTHER = "screen_other";
    String KEY_WHITELIST_ROAMING = "whitelist_roaming";
    String KEY_MANAGE_SYSTEM = "manage_system";
    String KEY_SCREEN_ON = "screen_on";
    String KEY_SHOW_USER = "show_user";
    String KEY_SHOW_SYSTEM = "show_system";
    String KEY_SHOW_NO_INTERNET = "show_nointernet";
    String KEY_SHOW_DISABLED = "show_disabled";
    String KEY_THEME_DARK = "dark_theme";
    String KEY_RESOLVE = "resolve";
    String KEY_ORGANIZATION = "organization";
    String KEY_LOG = "log";
    String KEY_PROTO_UDP = "proto_udp";
    String KEY_PROTO_TCP = "proto_tcp";
    String KEY_PROTO_OTHER = "proto_other";
    String KEY_TRAFFIC_ALLOWED = "traffic_allowed";
    String KEY_TRAFFIC_BLOCKED = "traffic_blocked";
    String KEY_VPN4 = "vpn4";
    String KEY_VPN6 = "vpn6";
    String KEY_FILTER = "filter";
    String KEY_PCAP = "pcap";
}
