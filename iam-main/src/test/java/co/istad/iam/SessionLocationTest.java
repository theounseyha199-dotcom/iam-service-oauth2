package co.istad.iam;

import kh.edu.iam.infrastructure.workspace.SessionLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionLocationTest {
    @Test void configuredDatabaseResolvesKnownPublicIpOffline() throws Exception {
        var resource = getClass().getResource("/geoip/GeoIP2-City-Test.mmdb");
        assertNotNull(resource);
        var location = new SessionLocation(java.nio.file.Path.of(resource.toURI()).toString());
        try {
            var result = location.lookup("81.2.69.160");
            assertEquals("approximate", result.get("status"));
            assertEquals("London", result.get("city"));
            assertEquals("GB", result.get("country"));
            assertEquals("local", location.lookup("127.0.0.1").get("status"));
            assertEquals("unavailable", location.lookup("8.8.8.8").get("status"));
        } finally { location.close(); }
    }
    @Test void offlineLookupNeverResolvesHostnamesAndDistinguishesPrivateNetworks() throws Exception {
        var location = new SessionLocation("");
        try {
            for (String ip : new String[]{"127.0.0.1", "::1", "192.168.1.4", "10.0.0.2", "172.16.1.2", "fd00::1", "100.64.0.1"})
                assertEquals("local", location.lookup(ip).get("status"), ip);
            for (String ip : new String[]{"8.8.8.8", "deadbeef", "example.com", "", "999.1.1.1", "2001:db8::1"})
                assertEquals("unavailable", location.lookup(ip).get("status"), ip);
            assertEquals("unavailable", location.lookup(null).get("status"));
        } finally { location.close(); }
    }
}
