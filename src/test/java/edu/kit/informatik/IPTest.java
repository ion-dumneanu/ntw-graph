package edu.kit.informatik;

import edu.kit.informatik.util.KoeriTestUtils;
import edu.kit.informatik.util.LinesSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.kit.informatik.util.KoeriTestUtils.ip;
import static org.junit.jupiter.api.Assertions.*;

public class IPTest {
    @ParameterizedTest
    @NullAndEmptySource
    @LinesSource("ip/invalid")
    void testInvalidIpParsing(String invalidIp) {
        assertThrows(ParseException.class, () -> new IP(invalidIp));
    }

    @ParameterizedTest
    @LinesSource("ip/valid")
    void testIpToString(String validIp) {
        assertEquals(validIp, ip(validIp).toString());
    }

    @ParameterizedTest
    @LinesSource("ip/valid")
    void testIpEquals(String ip) {
        assertEquals(ip(ip), ip(ip));
    }

    @ParameterizedTest
    @LinesSource(value = {"ip/sorted_0", "ip/sorted_1"}, flatten = false)
    void testIpCompare(Stream<String> ipStrings) {
        List<IP> ips = ipStrings.map(KoeriTestUtils::ip).collect(Collectors.toList());
        int idx = 0;
        for (IP ip : ips) {
            assertEquals(ip, ip);
            assertEquals(0, ip.compareTo(ip));
            for (IP lower : ips.subList(0, idx)) {
                assertNotEquals(ip, lower);
                assertTrue(lower.compareTo(ip) < 0);
            }
            for (IP higher : ips.subList(idx + 1, ips.size())) {
                assertNotEquals(ip, higher);
                assertTrue(higher.compareTo(ip) > 0);
            }
            ++idx;
        }
    }
}
