package edu.kit.informatik;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static edu.kit.informatik.util.KoeriTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class DisjointTreeTest {
    @ParameterizedTest
    @MethodSource("disjointArgsProvider")
    public void testDisjointToString(Network network, Network tree, IP root) {
        assertEquals(network.toString(root), tree.toString(root));
    }

    @ParameterizedTest
    @MethodSource("disjointArgsProvider")
    public void testDisjointLevels(Network network, Network tree, IP root) {
        assertEquals(network.getLevels(root), tree.getLevels(root));
    }

    static Stream<Arguments> disjointArgsProvider() {
        return Stream.of(disjointArgs(SMALL_NET, MEDIUM_NET, "85.193.148.81"));
    }

    static Arguments disjointArgs(String networkStr, String subnetStr, String root) {
        Network network = network(networkStr);
        network.add(network(subnetStr));
        Network tree = network(networkStr);
        return arguments(network, tree, ip(root));
    }
}
