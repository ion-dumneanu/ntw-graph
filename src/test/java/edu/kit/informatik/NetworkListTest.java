package edu.kit.informatik;

import edu.kit.informatik.util.KoeriTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.kit.informatik.util.KoeriTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class NetworkListTest {
    @ParameterizedTest
    @MethodSource("listProvider")
    void testList(List<IP> list, Stream<Network> networks) {
        networks.forEach(network -> assertEquals(list, network.list()));
    }

    @Test
    void testListSideEffects() {
        Network original = network(0, "a_random");
        Network network = network(0, "a_random");
        List<IP> list = network.list();
        tryModify(() -> list.add(ip("45.12.93.120")));
        assertEquals(original, network);
        tryModify(() -> Collections.shuffle(list));
        assertEquals(original, network);
        tryModify(list::clear);
        assertEquals(original, network);
    }

    void tryModify(Runnable action) {
        try {
            action.run();
        } catch (UnsupportedOperationException ignored) {
            // Unmodifiable collections may throw
        }
    }

    static Stream<Arguments> listProvider() {
        return IntStream.range(0, 5).mapToObj(NetworkListTest::listArgs);
    }

    static Arguments listArgs(int id) {
        List<IP> list = lines("network/" + id + "/list")
            .map(KoeriTestUtils::ip)
            .collect(Collectors.toList());
        return arguments(list, networks(id, "a_sorted", "a_random", "b_sorted", "b_random"));
    }
}
