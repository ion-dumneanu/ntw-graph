import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


class NetworkTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"(gresit iar aici)"})
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
        System.out.println(new Network(input).list());
        System.out.println(new ArrayList<>(sorted));
        assertTrue(new ArrayList<>(sorted).equals(new Network(input).list()));
    }


    @Test
    void testAdd() {
        fail("Not yet implemented");
    }


    @Test
    void testConnect() {
        fail("Not yet implemented");
    }

    @Test
    void testDisconnect() {
        fail("Not yet implemented");
    }

    @Test
    void testContains() {
        fail("Not yet implemented");
    }

    @Test
    void testGetHeight() {
        fail("Not yet implemented");
    }

    @Test
    void testGetLevels() {
        fail("Not yet implemented");
    }

    @Test
    void testGetRoute() throws ParseException {
        fail("Not yet implemented");
    }

    @Test
    void testToString(String input) throws ParseException {

        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root), List.of(new IP("122.117.67.158"), new IP("0.146.197.108")));
        final Network network = new Network(root, levels.get(1));

        final String expected = "(141.255.1.133 0.146.197.108 122.117.67.158)";
        assertEquals(expected, network.toString(root));
    }

}
