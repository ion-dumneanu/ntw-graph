package edu.kit.informatik;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleInteractionTest {
    @Test
    void testExampleInteraction() throws ParseException {
        IP root = new IP("141.255.1.133");
        List<IP> level1 = List.of(new IP("0.146.197.108"), new IP("122.117.67.158"));
        Network network = new Network(root, level1);

        assertTreeProperties(
            network,
            List.of(List.of(root), level1),
            "(141.255.1.133 0.146.197.108 122.117.67.158)"
        );
        assertTreeProperties(
            network,
            List.of(
                List.of(new IP("122.117.67.158")),
                List.of(new IP("141.255.1.133")),
                List.of(new IP("0.146.197.108"))
            ),
            "(122.117.67.158 (141.255.1.133 0.146.197.108))"
        );

        assertFalse(network.add(new Network("(122.117.67.158 0.146.197.108)")));
        assertTrue(network.add(new Network("(85.193.148.81 34.49.145.239 231.189.0.127 141.255.1.133)")));
        assertTrue(network.add(new Network("(231.189.0.127 252.29.23.0 116.132.83.77 39.20.222.120 77.135.84.171)")));

        assertTreeProperties(
            network,
            List.of(
                List.of(new IP("85.193.148.81")),
                List.of(new IP("34.49.145.239"), new IP("141.255.1.133"), new IP("231.189.0.127")),
                List.of(
                    new IP("0.146.197.108"), new IP("39.20.222.120"),
                    new IP("77.135.84.171"), new IP("116.132.83.77"),
                    new IP("122.117.67.158"), new IP("252.29.23.0")
                )
            ),
            "(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108"
                + " 122.117.67.158) (231.189.0.127 39.20.222.120"
                + " 77.135.84.171 116.132.83.77 252.29.23.0))"
        );

        assertEquals(
            List.of(new IP("141.255.1.133"), new IP("85.193.148.81"), new IP("231.189.0.127")),
            network.getRoute(new IP("141.255.1.133"), new IP("231.189.0.127"))
        );

        assertTreeHeight(
            network,
            List.of(
                List.of(new IP("34.49.145.239")),
                List.of(new IP("85.193.148.81")),
                List.of(new IP("141.255.1.133"), new IP("231.189.0.127")),
                List.of(
                    new IP("0.146.197.108"), new IP("39.20.222.120"),
                    new IP("77.135.84.171"), new IP("116.132.83.77"),
                    new IP("122.117.67.158"), new IP("252.29.23.0")
                )
            )
        );

        assertTrue(network.disconnect(new IP("85.193.148.81"), new IP("34.49.145.239")));

        assertEquals(
            List.of(
                new IP("0.146.197.108"), new IP("39.20.222.120"),
                new IP("77.135.84.171"), new IP("85.193.148.81"),
                new IP("116.132.83.77"), new IP("122.117.67.158"),
                new IP("141.255.1.133"), new IP("231.189.0.127"),
                new IP("252.29.23.0")
            ),
            network.list()
        );
    }

    private void assertTreeProperties(Network network, List<List<IP>> levels, String bracketNotation) {
        IP root = levels.get(0).get(0);
        assertEquals(bracketNotation, network.toString(root));
        assertTreeHeight(network, levels);
        assertEquals(levels, network.getLevels(root));
    }

    private void assertTreeHeight(Network network, List<List<IP>> levels) {
        assertEquals(levels.size() - 1, network.getHeight(levels.get(0).get(0)));
    }
}
