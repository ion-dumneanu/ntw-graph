import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NetworkFlexTest {

    @ParameterizedTest
    @CsvSource({"(141.255.1.133 0.146.197.108 122.117.67.158),141.255.1.133,1",
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),141.255.1.133,3"})
    void getHeight(String input, String root, int height) throws ParseException {
        NetworkFlex network = new NetworkFlex(input);
        assertEquals(height, network.getHeight(new IP(root)));
    }

    @ParameterizedTest
    @CsvSource({"(141.255.1.133 0.146.197.108 122.117.67.158),122.117.67.158,true",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),39.20.222.120,true",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),0.0.0.0,false"})
    void contains(String input, String searchItem, boolean expected) throws ParseException {
        NetworkFlex networkFlex = new NetworkFlex(input);
         assertEquals(expected, networkFlex.contains(new IP(searchItem)));
    }

    @Test
    void getLevels() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        NetworkFlex network = new NetworkFlex(input);
        List<List<IP>> expected = List.of(
                List.of(new IP("85.193.148.81")),
                List.of(new IP("34.49.145.239"), new IP("141.255.1.133"), new IP("231.189.0.127")),
                List.of(new IP("0.146.197.108"), new IP("122.117.67.158")),
                List.of(new IP("39.20.222.120"), new IP("77.135.84.171"), new IP("116.132.83.77"), new IP("252.29.23.0"))
        );

        assertEquals(expected, network.getLevels(new IP("85.193.148.81")));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 (141.255.1.133 34.49.145.239) 231.189.0.127),34.49.145.239|85.193.148.81|141.255.1.133|231.189.0.127"})
    void testList(String input, String expectedStr) throws ParseException {

        List<IP> expectedList = Arrays.stream(expectedStr.split("\\|")).map(item-> {
            try {
                return new IP(item);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).sorted().collect(Collectors.toList());
        assertEquals(expectedList, new Network(input).list());
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127),85.193.148.81,(85.193.148.81 34.49.145.239 141.255.1.133 231.189.0.127)",
            "(141.255.1.133 122.117.67.158 0.146.197.108),141.255.1.133,(141.255.1.133 0.146.197.108 122.117.67.158)",
            "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77),231.189.0.127,(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0)",
            "(85.193.148.81 (141.255.1.133 34.49.145.239 30.49.145.239) 140.189.0.127),34.49.145.239,(34.49.145.239 (141.255.1.133 30.49.145.239 85.193.148.81))"})
    void testConstructorWithStr(String input, String root, String expectedToString) throws ParseException {
        assertEquals(expectedToString, new NetworkFlex(input).toString(new IP(root))); ;
    }

    @Test
    void getRoute() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77) (211.189.0.127 (71.135.84.171 (31.20.222.120 (251.29.23.0 111.132.83.77)))))";
//        String input = "(85.193.148.81 111.132.83.77)";
        final NetworkFlex network = new NetworkFlex(input);

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

    // TODO: continue with connect(), disconnect(), add()


}