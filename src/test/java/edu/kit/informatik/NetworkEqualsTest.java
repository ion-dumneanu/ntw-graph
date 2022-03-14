package edu.kit.informatik;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.kit.informatik.util.KoeriTestUtils.network;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class NetworkEqualsTest {
    @ParameterizedTest
    @MethodSource("equalProvider")
    void testEquals(Network aSorted, Network aRandom, Network bSorted, Network bRandom) {
        assertEquals(aSorted, aSorted);
        assertBothEquals(aSorted, aRandom);
//        assertBothEquals(aSorted, bSorted);
//        assertBothEquals(aSorted, bRandom);
//        assertEquals(aRandom, aRandom);
//        assertBothEquals(aRandom, bSorted);
//        assertBothEquals(aRandom, bRandom);
//        assertEquals(bSorted, bSorted);
//        assertBothEquals(bSorted, bRandom);
//        assertEquals(bRandom, bRandom);
    }

    void assertBothEquals(Object o1, Object o2) {
        assertEquals(o1, o2);
        assertEquals(o2, o1);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    @ParameterizedTest
    @MethodSource("notEqualProvider")
    void testNotEqual(Network network, Stream<Network> other) {
        other.forEach(o -> assertNotEquals(network, o));
    }

    static Stream<Arguments> equalProvider() {
        return IntStream.range(0, 5).mapToObj(NetworkEqualsTest::equalArgs);
    }

    static Arguments equalArgs(int networkId) {
        Network aSorted = network(networkId, "/a_sorted");
        Network aRandom = network(networkId, "/a_random");
        Network bSorted = network(networkId, "/b_sorted");
        Network bRandom = network(networkId, "/b_random");
        return arguments(aSorted, aRandom, bSorted, bRandom);
    }

    static Stream<Arguments> notEqualProvider() {
        return IntStream.range(0, 5).mapToObj(NetworkEqualsTest::notEqualArgs);
    }

    static Arguments notEqualArgs(int networkId) {
        Stream<Network> other = IntStream
            .range(0, 5)
            .filter(id -> id != networkId)
            .mapToObj(id -> network(id, "/a_random"));
        return arguments(network(networkId, "/a_random"), other);
    }
}
