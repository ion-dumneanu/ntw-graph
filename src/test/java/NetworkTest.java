import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


class NetworkTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"(gresit aici)"})
    void testNotValid(String input) {
        assertThrows(ParseException.class, ()->{new Network(input);});
    }

    @ParameterizedTest
    @ValueSource(strings = {"(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)",
                            "(141.255.1.133 122.117.67.158 0.146.197.108)",
                            "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)",
                            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))"})
    void testConstructorWithStr11(String input) throws ParseException {
        new Network(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"(85.193.148.81 (141.255.1.133 34.49.145.239) 231.189.0.127)"})
    void testList(String input) throws ParseException {
        Set<IP> sorted = new TreeSet<>(List.of(new IP("85.193.148.81"),new IP("141.255.1.133"), new IP("34.49.145.239"), new IP("231.189.0.127")));
        assertEquals(new ArrayList<>(sorted), new Network(input).list());
    }


    @Test
    void testAdd() {
        fail("Not yet implemented");
    }


    @Test
    void testConnect() throws ParseException {
        String input = "(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)";
        assertTrue(new Network(input).connect(new IP("34.49.145.239"), new IP("231.189.0.127")));
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
        fail("Not yet implemented");
    }

    @Test
    void testToString() throws ParseException {
        final Network network = new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))");

        final String expected = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        assertEquals(expected, network.toString(new IP("85.193.148.81")));
    }

}
