package edu.kit.informatik;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.kit.informatik.util.KoeriTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class Acul021NetworkTest {
    @Test
    public void testList() throws ParseException {
        Network net = new Network("(192.168.178.1 192.168.178.0 192.168.178.15 0.0.0.0 29.65.234.123)");

        List<IP> temp = net.list();
        temp.clear();
        assertNotEquals(net.list().size(), temp.size());
        net = new Network("(1.1.1.1 0.0.0.0)");
        assertIterableEquals(ips("0.0.0.0", "1.1.1.1"), net.list());
        net = new Network(
            "(9.9.9.9 (8.8.8.8 (7.7.7.7 (6.6.6.6 (5.5.5.5 (4.4.4.4 (3.3.3.3 (2.2.2.2 (1.1.1.1 0.0.0.0)))))))))");
        assertIterableEquals(
            ips("0.0.0.0", "1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5", "6.6.6.6", "7.7.7.7", "8.8.8.8",
                "9.9.9.9"),
            net.list());
    }

    @Test
    public void testHeight() throws ParseException {
        Network med = new Network(MEDIUM_NET);
        System.out.println(med.toString(new IP("90.240.18.65")));
        assertEquals(0, med.getHeight(null));
        assertEquals(0, med.getHeight(ip("0.0.0.0")));
        assertEquals(4, med.getHeight(ip("90.240.18.65")));
        assertEquals(5, med.getHeight(ip("118.255.66.35")));
        assertEquals(6, med.getHeight(ip("71.130.3.224")));
        assertEquals(7, med.getHeight(ip("211.36.119.36")));
        assertEquals(8, med.getHeight(ip("166.114.94.115")));

        Network net = new Network(SMALL_NET);
        assertEquals(0, net.getHeight(null));
        assertEquals(0, net.getHeight(ip("127.0.0.1")));
        assertEquals(2, net.getHeight(ip("85.193.148.81")));
        assertEquals(3, net.getHeight(ip("141.255.1.133")));
        assertEquals(4, net.getHeight(ip("0.146.197.108")));
    }

    @Test
    public void testAdd() throws ParseException {
        Network net = new Network(SMALL_NET);
        assertFalse(net.add(null));
        assertFalse(net.add(new Network(SMALL_NET_SORTED)));
        assertFalse(net.add(new Network("(141.255.1.133 122.117.67.158 0.146.197.108)")));
        assertTrue(net.add(new Network("(0.0.0.0 141.255.1.133)")));
        assertEquals(SMALL_NET_EXTENDED, net.toString(ip("85.193.148.81")));
        assertFalse(net.add(new Network("(122.117.67.158 0.146.197.108 141.255.1.133 1.1.1.1)")));
        assertTrue(net.add(new Network("(141.255.1.133 122.117.67.158 0.146.197.108 2.2.2.2)")));
        assertEquals(SMALL_NET_EXTENDED_2, net.toString(ip("85.193.148.81")));
        net = new Network(SMALL_NET);
        assertTrue(net.add(new Network("(0.0.0.0 1.1.1.1)")));
        assertEquals("(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) "
                + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))",
            net.toString(ip("85.193.148.81")));
        assertTrue(net.add(network("(1.0.0.0 1.0.0.1 1.0.0.2)")));
        assertEquals("(0.0.0.0 1.1.1.1)", net.toString(ip("0.0.0.0")));
        assertEquals("(1.0.0.0 1.0.0.1 1.0.0.2)", net.toString(ip("1.0.0.0")));
        assertTrue(net.add(network("(1.1.1.1 2.2.2.2 1.0.0.1)")));
        assertEquals("(1.1.1.1 0.0.0.0 (1.0.0.1 (1.0.0.0 1.0.0.2)) 2.2.2.2)", net.toString(ip("1.1.1.1")));
    }

    @Test
    public void testConnect() throws ParseException {
        Network net = new Network(SMALL_NET);
        net.add(network("(0.0.0.0 1.1.1.1)"));
        net.add(network("(1.0.0.1 1.0.0.2)"));
        net.add(network("(0.0.0.1 0.0.0.2 0.0.0.3 0.0.0.4)"));
        assertFalse(() -> net.connect(null, null));
        assertFalse(() -> net.connect(ip("0.0.0.0"), null));
        assertFalse(() -> net.connect(null, ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("9.9.9.9")));
        assertFalse(() -> net.connect(ip("9.9.9.9"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("1.1.1.1")));
        assertFalse(() -> net.connect(ip("1.1.1.1"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("231.189.0.127"), ip("77.135.84.171")));
        assertFalse(() -> net.connect(ip("77.135.84.171"), ip("231.189.0.127")));
        assertFalse(() -> net.connect(ip("122.117.67.158"), ip("77.135.84.171")));
        assertFalse(() -> net.connect(ip("77.135.84.171"), ip("122.117.67.158")));
        assertTrue(() -> net.connect(ip("1.1.1.1"), ip("1.0.0.2")));
        assertTrue(() -> net.connect(ip("1.0.0.2"), ip("0.0.0.3")));
        assertTrue(() -> net.connect(ip("231.189.0.127"), ip("1.0.0.1")));

        assertEquals(4, net.getHeight(ip("1.0.0.1")));
        assertEquals(
            "(1.0.0.1 (1.0.0.2 (0.0.0.3 (0.0.0.1 0.0.0.2 0.0.0.4)) (1.1.1.1 0.0.0.0)) (231.189.0.127 39.20.222.120 77.135.84.171 (85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158)) 116.132.83.77 252.29.23.0))",
            net.toString(ip("1.0.0.1")));
    }

    @Test
    public void testDisconnect() throws ParseException {
        Network net = new Network("(0.0.0.0 1.1.1.1)");
        assertFalse(net.disconnect(ip("0.0.0.0"), ip("1.1.1.1")));

        Network net2 = new Network("(0.0.0.0 (1.1.1.1 2.2.2.2) 3.3.3.3 (4.4.4.4 5.5.5.5))");

        assertFalse(() -> net2.disconnect(null, null));
        assertFalse(() -> net2.disconnect(ip("0.0.0.0"), null));
        assertFalse(() -> net2.disconnect(null, ip("0.0.0.0")));
        assertFalse(() -> net2.disconnect(ip("0.0.0.0"), ip("7.7.7.7")));
        assertFalse(() -> net2.disconnect(ip("7.7.7.7"), ip("0.0.0.0")));

        assertTrue(() -> net2.disconnect(ip("0.0.0.0"), ip("1.1.1.1")));
        assertEquals("(2.2.2.2 1.1.1.1)", net2.toString(ip("2.2.2.2")));
        assertEquals("(0.0.0.0 3.3.3.3 (4.4.4.4 5.5.5.5))", net2.toString(ip("0.0.0.0")));
        assertTrue(() -> net2.disconnect(ip("1.1.1.1"), ip("2.2.2.2")));
        assertFalse(() -> net2.contains(ip("1.1.1.1")));
        assertFalse(() -> net2.contains(ip("2.2.2.2")));
        assertTrue(() -> net2.disconnect(ip("4.4.4.4"), ip("5.5.5.5")));
        assertFalse(() -> net2.contains(ip("5.5.5.5")));
        assertTrue(() -> net2.contains(ip("4.4.4.4")));
        assertEquals("(0.0.0.0 3.3.3.3 4.4.4.4)", net2.toString(ip("0.0.0.0")));
    }

    @Test
    public void testGetLevels() throws ParseException {
        Network med = new Network(SMALL_NET);
        assertIterableEquals(List.of(), med.getLevels(null));
        assertIterableEquals(List.of(), med.getLevels(ip("0.0.0.0")));
        assertIterableEquals(List.of(ips("85.193.148.81"), ips("34.49.145.239", "141.255.1.133", "231.189.0.127"),
                ips("0.146.197.108", "39.20.222.120", "77.135.84.171", "116.132.83.77", "122.117.67.158", "252.29.23.0")),
            med.getLevels(ip("85.193.148.81")));
        assertIterableEquals(
            List.of(ips("77.135.84.171"), ips("231.189.0.127"),
                ips("39.20.222.120", "85.193.148.81", "116.132.83.77", "252.29.23.0"),
                ips("34.49.145.239", "141.255.1.133"), ips("0.146.197.108", "122.117.67.158")),
            med.getLevels(ip("77.135.84.171")));
        assertIterableEquals(
            List.of(ips("141.255.1.133"), ips("0.146.197.108", "85.193.148.81", "122.117.67.158"),
                ips("34.49.145.239", "231.189.0.127"),
                ips("39.20.222.120", "77.135.84.171", "116.132.83.77", "252.29.23.0")),
            med.getLevels(ip("141.255.1.133")));
    }

    @Test
    public void testEqualsAndHashCode() throws ParseException {
        Network net1 = new Network(MEDIUM_NET);
        Network net2 = new Network(MEDIUM_NET);
        Network net3 = new Network(SMALL_NET);
        Network net4 = new Network(SMALL_NET_ALTERNATIVE);

        assertEquals(net1, net2);
        assertEquals(net2, net1);
        assertEquals(net1.hashCode(), net2.hashCode());

        assertNotEquals(net1, net3);
        assertNotEquals(net3, net1);

        assertEquals(net3, net4);
        assertEquals(net4, net3);
        assertEquals(net3.hashCode(), net4.hashCode());
    }

    @Test
    public void testToString() throws ParseException {
        Network small = new Network(SMALL_NET);
        assertEquals(SMALL_NET_SORTED, small.toString(ip("85.193.148.81")));
        assertEquals(SMALL_NET_ALTERNATIVE_SORTED_2, small.toString(ip("141.255.1.133")));
        assertEquals(SMALL_NET_ALTERNATIVE_SORTED, small.toString(ip("77.135.84.171")));
        assertEquals("", small.toString(ip("0.0.0.0")));
        assertEquals("", small.toString(null));

    }

    @Test
    public void testNoSideEffectsAfterAdd() throws ParseException {
        Network network = new Network(MEDIUM_NET);
        //no overlapping nodes
        assertNoDisconnectSideEffects(network, new Network(SMALL_NET), ip("85.193.148.81"), ip("141.255.1.133"));

        //overlapping nodes
        assertNoDisconnectSideEffects(
            network,
            new Network("(116.132.83.77 1.1.1.1 2.2.2.2 (5.5.5.5 3.3.3.3))"),
            ip("116.132.83.77"),
            ip("2.2.2.2")
        );
    }

    private void assertNoDisconnectSideEffects(Network network, Network subnet, IP root, IP childToDisconnect) {
        assertTrue(network.add(subnet));
        String beforeDisconnection = network.toString(root);
        assertTrue(subnet.disconnect(root, childToDisconnect));
        assertEquals(beforeDisconnection, network.toString(root));
    }
}
