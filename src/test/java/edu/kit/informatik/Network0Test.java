package edu.kit.informatik;

import edu.kit.informatik.Network.Edge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

class Network0Test {

    @ParameterizedTest
    @CsvSource({"(141.255.1.133 0.146.197.108 122.117.67.158),141.255.1.133,1",
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),141.255.1.133,3",
                "(0.0.0.0 (1.1.1.1 2.2.2.2 3.3.3.3) (4.4.4.4 5.5.5.5 (6.6.6.6 7.7.7.7 (8.8.8.8 (9.9.9.9 10.10.10.10))))),0.0.0.0,5"})
    void getHeight(String input, String root, int height) throws ParseException {
        Network network = new Network(input);
        assertEquals(height, network.getHeight(new IP(root)));
    }

    @ParameterizedTest
    @CsvSource({"(141.255.1.133 0.146.197.108 122.117.67.158),122.117.67.158,true",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),39.20.222.120,true",
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)),0.0.0.0,false"})
    void contains(String input, String searchItem, boolean expected) throws ParseException {
        Network network = new Network(input);
         assertEquals(expected, network.contains(new IP(searchItem)));
    }

    @Test
    void getLevels() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";
        Network network = new Network(input);
        List<List<IP>> expected = List.of(
                List.of(new IP("85.193.148.81")),
                List.of(new IP("34.49.145.239"), new IP("141.255.1.133"), new IP("231.189.0.127")),
                List.of(new IP("0.146.197.108"), new IP("39.20.222.120"),new IP("77.135.84.171"), new IP("116.132.83.77"), new IP("122.117.67.158"), new IP("252.29.23.0"))
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
            "(85.193.148.81 (141.255.1.133 34.49.145.239 30.49.145.239) 140.189.0.127),34.49.145.239,(34.49.145.239 (141.255.1.133 30.49.145.239 (85.193.148.81 140.189.0.127)))",
            "(85.193.148.81 (141.255.1.133 34.49.145.239 (30.49.145.239 0.0.0.0 1.1.1.1)) 140.189.0.127),0.0.0.0,(0.0.0.0 (30.49.145.239 1.1.1.1 (141.255.1.133 34.49.145.239 (85.193.148.81 140.189.0.127))))"})
    void testToString(String input, String root, String expectedToString) throws ParseException {
        assertEquals(expectedToString, new Network(input).toString(new IP(root)));
    }

    @Test
    void getRoute() throws ParseException {
        String input = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77) (211.189.0.127 (71.135.84.171 (31.20.222.120 (251.29.23.0 111.132.83.77)))))";
        final Network network = new Network(input);

        final IP start = new IP("85.193.148.81");
        final IP end = new IP("111.132.83.77");

        final List<IP> expectedRoute = Stream.of("85.193.148.81", "211.189.0.127", "71.135.84.171", "31.20.222.120", "251.29.23.0", "111.132.83.77").map(IP::new).collect(Collectors.toList());
        final List<IP> actualRoute = network.getRoute(start, end);
        assertEquals(expectedRoute, actualRoute);
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127),85.193.148.81,231.189.0.127,true,(85.193.148.81 34.49.145.239 141.255.1.133)"})
    void disconnect(String input, String disconnect1, String disconnect2, boolean isSuccess, String expectedToString) throws ParseException {
        final Network ntwGraph = new Network(input);
        assertEquals(isSuccess, ntwGraph.disconnect(new IP(disconnect1), new IP(disconnect2)));
        assertEquals(expectedToString,ntwGraph.toString(new IP(disconnect1)));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 231.189.0.127),(0.0.0.0 1.1.1.1), true"})
    void addSeparateSubnetwork(String existingNetworkStr, String subnetStr, boolean isSuccess) throws ParseException {
        final Network network = new Network(existingNetworkStr);
        final Network subnetwork = new Network(subnetStr);

        assertEquals(isSuccess, network.add(subnetwork));
        assertEquals("(1.1.1.1 0.0.0.0)", network.toString(new IP("1.1.1.1")));
        assertEquals("(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) 231.189.0.127)", network.toString(new IP("85.193.148.81")));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 231.189.0.127),(1.1.1.1 122.117.67.158 85.193.148.81),false"})
    void addThatShapeACycle(String existingNetworkStr, String subnetStr, boolean isSuccess) throws ParseException {
        final Network network = new Network(existingNetworkStr);
        final Network subnetwork = new Network(subnetStr);

        assertEquals(isSuccess, network.add(subnetwork));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 231.189.0.127),(1.1.1.1 122.117.67.158), true"})
    void addWithConnectingNode(String existingNetworkStr, String subnetStr, boolean isSuccess) throws ParseException {
        final Network network = new Network(existingNetworkStr);
        final Network subnetwork = new Network(subnetStr);

        assertEquals(isSuccess, network.add(subnetwork));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 34.49.145.239 (141.255.1.133 122.117.67.158 0.146.197.108)),(0.0.0.0 1.1.1.1),(34.49.145.239 1.1.1.1)"})
    void addThatConnectSeparateGraphs(String graph1, String graph2, String graph3Connector) throws ParseException {
        final Network network = new Network(graph1);

        assertTrue(network.add(new Network(graph2)));
        System.out.println(network.toString(new IP("85.193.148.81")));
        System.out.println(network.toString(new IP("1.1.1.1")));
        assertTrue(network.add(new Network(graph3Connector)));
        System.out.println(network.toString(new IP("85.193.148.81")));
    }

    @ParameterizedTest
    @CsvSource({"(0.0.0.0 0.0.0.1 0.0.0.2),(1.1.1.1 1.1.1.2 1.1.1.3),(2.2.2.2 2.2.2.3 2.2.2.4),(0.0.0.2 1.1.1.3 2.2.2.4)"})
    void addThatConnect3SeparateGraphs(String graph1, String graph2, String graph3, String graphConnector) throws ParseException {
        final Network network = new Network(graph1);

        assertTrue(network.add(new Network(graph2)));
        assertTrue(network.add(new Network(graph3)));
        System.out.println(network.toString(new IP("0.0.0.0")));
        System.out.println(network.toString(new IP("1.1.1.1")));
        System.out.println(network.toString(new IP("2.2.2.2")));

        assertTrue(network.add(new Network(graphConnector)));
        System.out.println(network.toString(new IP("0.0.0.0")));
    }

    @ParameterizedTest
    @CsvSource({"(0.0.0.0 1.1.1.1 2.2.2.2),0.0.0.0,2.2.2.2,false",
            "(0.0.0.0 (1.1.1.1 2.2.2.2)),0.0.0.0,2.2.2.2,false"})
    void connectNotSuccessCases(String graphStr, String startStr, String endStr, boolean isSuccess) throws ParseException {
        assertEquals(isSuccess,new Network(graphStr).connect(new IP(startStr), new IP(endStr)));
    }

    @ParameterizedTest
    @CsvSource({"(85.193.148.81 34.49.145.239 (141.255.1.133 122.117.67.158 0.146.197.108)),(0.0.0.0 1.1.1.1),34.49.145.239,1.1.1.1"})
    void connectSuccessCase(String graph1, String graph2, String connect1, String connect2) throws ParseException {
        final Network network = new Network(graph1);

        assertTrue(network.add(new Network(graph2)));
        System.out.println(network.toString(new IP("85.193.148.81")));
        System.out.println(network.toString(new IP("1.1.1.1")));
        assertTrue(network.connect(new IP(connect1), new IP(connect2)));
        System.out.println(network.toString(new IP("85.193.148.81")));
    }

//    @Test
    void testRegexAll(){
        // String to be scanned to find the pattern.
        String line = "(0.0.0.0 (1.1.1.1 (2.2.2.2 (3.3.3.3 4.4.4.4))) (11.11.11.11 12.12.12.12))";
        String pattern = "(\\("+IP.REGEXP+"(\\s"+IP.REGEXP+")+\\))";

        final List<Edge> edges = new ArrayList<>();
        final Pattern r = Pattern.compile(pattern);
        System.out.println(line);

        while(!line.matches(IP.REGEXP)){
            Matcher m = r.matcher(line);
            m.find();
            String groupMatch = m.group();
            line = line.replaceFirst("\\("+groupMatch+"\\)", process(groupMatch, edges));
            System.out.println(line);
        }

        System.out.println(edges);
    }
    private String process(String value, List<Edge> edges){
        String onlyIpWithSpaces = value.replaceAll("[()]", "");
        List<String> nodes = asList(onlyIpWithSpaces.split("\\s"));
        String root = nodes.get(0);
        nodes.subList(1,nodes.size()).forEach(ipStr->edges.add(new Edge(new IP(root), new IP(ipStr))));
//        System.out.println(edges);
        return root;
    }


    @Test
    void testRegexIps(){
//        // String to be scanned to find the pattern.
//        String line = "(0.0.0.0 (1.1.1.1 (2.2.2.2 (3.3.3.3 4.4.4.4))) (11.11.11.11 12.12.12.12))";
//        String pattern = "(\\("+IP.REGEXP+"(\\s"+IP.REGEXP+")+\\))";
//
//        final Pattern r = Pattern.compile(IP.REGEXP);
//        Matcher m = r.matcher(line);
//
//        int index =0;
//        while(m.find()){
//            System.out.println(++index+ " "+ m.group());
//        }
        System.out.println("(9900(((".matches(".*[(]{2,}.*"));

    }

}