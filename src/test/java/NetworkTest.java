import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


class NetworkTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"(gresit aici)",
                            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) (0.146.197.108 111.111.111.111) (111.111.111.111 141.255.1.133))"})
    void testNotValid(String input) {
        assertThrows(ParseException.class, ()->{new Network(input);});
    }

    @ParameterizedTest
    @ValueSource(strings = {"(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)",
                            "(141.255.1.133 122.117.67.158 0.146.197.108)",
                            "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)",
                            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))"})
    void testConstructorWithString(String input) throws ParseException {
        new Network(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"(85.193.148.81 (141.255.1.133 34.49.145.239) 231.189.0.127)"})
    void testList(String input) throws ParseException {
        Set<IP> sorted = new TreeSet<>(List.of(new IP("85.193.148.81"),new IP("141.255.1.133"), new IP("34.49.145.239"), new IP("231.189.0.127")));
        assertEquals(new ArrayList<>(sorted), new Network(input).list());
    }


    @ParameterizedTest
    @ValueSource(strings = {"(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)",
            "(141.255.1.133 122.117.67.158 0.146.197.108)",
            "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))"})
    void testConstructorWithStr11(String input) throws ParseException {
        new Network(input);
    }

    @Test
    void testConnect() throws ParseException {
        String input = "(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)";
        assertFalse(new Network(input).connect(new IP("34.49.145.239"), new IP("231.189.0.127")));
    }

    @Test
    void testDisconnect() throws ParseException {
        String input = "(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)";
        final Network ntwGraph = new Network(input);
        assertTrue(ntwGraph.disconnect(new IP("85.193.148.81"), new IP("231.189.0.127")));
    }

    @Test
    void testNotAllowedDisconnect() throws ParseException {
        String input = "(85.193.148.81 231.189.0.127)";
        final Network ntwGraph = new Network(input);
        assertFalse(ntwGraph.disconnect(new IP("85.193.148.81"), new IP("231.189.0.127")));
    }

    @Test
    void testContains() throws ParseException {
        String input = "(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)";
        final Network network = new Network(input);
        assertTrue(network.contains(new IP("34.49.145.239")));
    }

    @Test
    void testGetHeight() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        Network network = new Network(input);
        assertEquals(3, network.getHeight(new IP("85.193.148.81")));
    }

    @Test
    void testGetLevels() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        Network network = new Network(input);
        List<List<IP>> expected = List.of(
                List.of(new IP("85.193.148.81")),
                List.of(new IP("141.255.1.133"), new IP("34.49.145.239"), new IP("231.189.0.127")),
                List.of(new IP("122.117.67.158"), new IP("0.146.197.108")),
                List.of(new IP("77.135.84.171"), new IP("39.20.222.120"), new IP("252.29.23.0"), new IP("116.132.83.77"))
        );

        assertEquals(expected, network.getLevels(new IP("85.193.148.81")));
    }


    @Test
    void testGetRoute() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77) (211.189.0.127 (71.135.84.171 (31.20.222.120 (251.29.23.0 111.132.83.77)))))";
        final Network network = new Network(input);

        final IP start = new IP("85.193.148.81");
        final IP end = new IP("111.132.83.77");

        final List<IP> expectedRoute = List.of("85.193.148.81", "211.189.0.127", "71.135.84.171", "31.20.222.120", "251.29.23.0", "111.132.83.77").stream().map(pointNotation -> {
            try {
                return new IP(pointNotation);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid IP value: " + e.getMessage());
            }
        }).collect(Collectors.toList());
        final List<IP> actualRoute = network.getRoute(start, end);
        assertEquals(expectedRoute, actualRoute);
    }

    @ParameterizedTest
    @CsvSource({
            "(141.255.1.133 0.146.197.108 122.117.67.158),122.117.67.158,(122.117.67.158 (141.255.1.133 0.146.197.108))",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),85.193.148.81,(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))"})
    void testToString(String input, String rootStr, String expected) throws ParseException {
        final Network network = new Network(input);

        assertEquals(expected, network.toString(new IP(rootStr)));
    }

    @ParameterizedTest
    @CsvSource({"(122.117.67.158 (141.255.1.133 0.146.197.108)),122.117.67.158",
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),85.193.148.81"})
    void testGetRoot(String input, String expected) throws ParseException {
        Network network = new Network(input);
        assertEquals(new IP(expected), network.getRoot());
    }

    @Test
    void testAdd() throws ParseException {
        System.out.println(List.of(new IP("123.23.22.22")).subList(1,1));
    }

    @Test
    void testCalcEdges() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        System.out.println(Network.calcEdges(input));
    }

    @Test
    void testAddExistingSubnet() throws ParseException {
        String network = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        String subnet = "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.70)";
        assertFalse(new Network(network).add(new Network(subnet)));
    }

    @Test
    void testAddShapesACycle() throws ParseException {
        String networkString = "(231.189.0.127 (77.135.84.171 (39.20.222.120 (252.29.23.0 116.132.83.70))))";
        String subNetwork = "(111.11.0.1 (39.20.222.120 77.135.84.171))";
        final Network network = new Network(networkString);
        final Network subnetwork = new Network(subNetwork);

        assertFalse(network.add(subnetwork));
    }

    @Test
    void testAddSuccess() throws ParseException {
        String expectedResult = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";

        String networkStr = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 231.189.0.127)";
        String subNetworkStr = "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)";

        final Network network = new Network(networkStr);
        final Network subnetwork = new Network(subNetworkStr);

        final boolean addActionResult = network.add(subnetwork);
        assertTrue(addActionResult);
        assertEquals(expectedResult, network.toString(new IP("85.193.148.81")));
    }

    @Test
    void testAddMerge() throws ParseException {
        Network network = new Network( "(122.117.67.158 (141.255.1.133 0.146.197.108))");
        Network subnetwork = new Network("(85.193.148.81 34.49.145.239 231.189.0.127 141.255.1.133)");

        final boolean addMergeTrue = network.add(subnetwork);
        System.out.println(network.toString(network.getRoot()));
        assertTrue(addMergeTrue);
    }


}
