package edu.kit.informatik;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.kit.informatik.util.KoeriTestUtils.ip;
import static edu.kit.informatik.util.KoeriTestUtils.ips;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class NetworkConstructorListTest {
    @ParameterizedTest
    @MethodSource("invalidArgsProvider")
    void testInvalidArgs(IP root, List<IP> children) {
        assertThrows(RuntimeException.class, () -> new Network(root, children));
    }

    @Test
    void testListSideEffects() {
        List<IP> children = new ArrayList<>(ips("12.246.77.82", "181.16.150.157"));
        Network network = new Network(ip("178.132.155.48"), children);
        children.add(ip("148.9.166.201"));
        assertEquals(3, network.list().size());
        children.clear();
        assertEquals(3, network.list().size());
    }

    static List<Arguments> invalidArgsProvider() {
        IP root = ip("192.168.178.65");
        IP other = ip("13.45.198.56");
        return List.of(
            arguments(null, null),
            arguments(root, null),
            arguments(root, List.of()),
            arguments(null, List.of(root, other)),
            arguments(root, List.of(root, other)),
            arguments(root, List.of(other, other)),
            // List.of does not permit null values
            arguments(root, Arrays.asList(other, null))
        );
    }
}
