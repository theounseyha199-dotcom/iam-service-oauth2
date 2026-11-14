package kh.edu.iam.infrastructure.workspace;

import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/** Offline lookup only: no DNS resolution and no IP addresses sent to third parties. */
@Component
public final class SessionLocation {
    private final DatabaseReader reader;
    public SessionLocation(@Value("${app.iam.geoip-database:}") String path) throws IOException {
        // An explicitly configured unreadable/wrong database is a deployment error, not a silent fallback.
        reader = path.isBlank() ? null : new DatabaseReader.Builder(new File(path)).build();
    }
    public Map<String, String> lookup(String ip) {
        if (ip == null || !ip.matches("[0-9a-fA-F:.]{2,100}")) return Map.of("status", "unavailable");
        try {
            InetAddress address = InetAddress.ofLiteral(ip);
            byte[] bytes = address.getAddress();
            if (address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress() || address.isMulticastAddress()
                    || (bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc)
                    || (bytes.length == 4 && (bytes[0] & 0xff) == 100 && (bytes[1] & 0xc0) == 64))
                return Map.of("status", "local");
            if (reader == null) return Map.of("status", "unavailable");
            return reader.tryCity(address).map(city -> Map.of("status", "approximate",
                    "city", text(city.city().name()), "country", text(city.country().isoCode())))
                    .orElseGet(() -> Map.of("status", "unavailable"));
        } catch (IOException | com.maxmind.geoip2.exception.GeoIp2Exception | IllegalArgumentException ex) {
            return Map.of("status", "unavailable");
        }
    }
    private static String text(String value) { return value == null ? "" : value; }
    @PreDestroy public void close() throws IOException { if (reader != null) reader.close(); }
}
