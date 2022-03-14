package edu.kit.informatik.util;

import edu.kit.informatik.IP;
import edu.kit.informatik.Network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class KoeriTestUtils {
    public static final String SMALL_NET
        = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 "
        + "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";

    public static final String SMALL_NET_ALTERNATIVE
        = "(77.135.84.171 (231.189.0.127 39.20.222.120 252.29.23.0 116.132.83.77 "
        + "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239)))";

    public static final String SMALL_NET_ALTERNATIVE_SORTED
        = "(77.135.84.171 (231.189.0.127 39.20.222.120 (85.193.148.81 34.49.145.239 " +
        "(141.255.1.133 0.146.197.108 122.117.67.158)) 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_ALTERNATIVE_SORTED_2
        = "(141.255.1.133 0.146.197.108 (85.193.148.81 34.49.145.239 " +
        "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0)) 122.117.67.158)";

    public static final String SMALL_NET_SORTED
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_EXTENDED
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.0.0.0 0.146.197.108 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_EXTENDED_2
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.0.0.0 0.146.197.108 2.2.2.2 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String MEDIUM_NET =
        "(90.240.18.65 (97.22.140.27 (193.77.65.203 206.41.6.234 (137.57.11.178 53.79.153.118 151.175.20.133 72.204.103.14) (172.217.134.246 125.151.42.40 26.135.185.104 12.104.224.21 97.32.83.116)) 27.191.109.156 (207.93.69.7 221.203.203.33 (211.36.119.36 191.214.220.219 7.33.138.146) (126.171.183.35 0.166.201.82 166.114.94.115)) 25.28.90.184) 124.214.225.52 62.116.50.162 (118.255.66.35 228.203.204.177 (71.130.3.224 (131.230.153.36 39.231.53.70 1.77.201.101 13.163.16.235) 166.101.129.76)))";

    private KoeriTestUtils() {
    }

    public static IP ip(String ip) {
        return assertDoesNotThrow(() -> new IP(ip));
    }

    public static List<IP> ips(String... ips) {
        return Arrays.stream(ips).map(KoeriTestUtils::ip).collect(Collectors.toList());
    }

    public static Network network(String bracketNotation) {
        return assertDoesNotThrow(() -> new Network(bracketNotation));
    }

    public static Network network(int id, String file) {
        return network(singleLine("network/" + id + "/" + file));
    }

    public static Stream<Network> networks(int id, String... files) {
        return Arrays.stream(files).map(file -> network(id, file));
    }

    public static BufferedReader reader(String resource) {
        return new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    KoeriTestUtils.class.getClassLoader().getResourceAsStream(resource)
                )
            )
        );
    }

    public static Stream<String> lines(String resource) {
        return reader(resource).lines();
    }

    public static String singleLine(String resource) {
        try (Stream<String> lines = lines(resource)) {
            Iterator<String> it = lines.iterator();
            String line = it.next();
            assertFalse(it.hasNext());
            return line;
        }
    }
}
