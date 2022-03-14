package edu.kit.informatik;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.kit.informatik.util.KoeriTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class EneOliNetworkTest {
    @Test
    public void testContains() {
        Network network = network(SMALL_NET);
        assertTrue(network.contains(ip("39.20.222.120")));
        assertFalse(network.contains(ip("42.20.222.120")));
        assertFalse(network.contains(null));
    }

    @Test
    public void toStringSimpleTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        assertEquals("(1.1.1.1 2.2.2.2)", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void toStringNullIpTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");
        assertEquals("", network.toString(null));
    }

    @Test
    public void toStringNonExistingIpTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");
        assertEquals("", network.toString(new IP("0.0.0.0")));
    }

    @Test
    public void toStringExample1Test() throws ParseException {
        Network network = new Network("(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)");
        assertEquals("(85.193.148.81 34.49.145.239 141.255.1.133 231.189.0.127)", network.toString(new IP("85.193.148.81")));
    }

    @Test
    public void toStringExample2Test() throws ParseException {
        Network network = new Network("(141.255.1.133 122.117.67.158 0.146.197.108)");
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(new IP("141.255.1.133")));
    }

    @Test
    public void toStringExample3Test() throws ParseException {
        Network network = new Network("(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)");
        assertEquals("(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0)", network.toString(new IP("231.189.0.127")));
    }

    @Test
    public void toStringExample4Test() throws ParseException {
        Network network = new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))");
        assertEquals("(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) (231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))", network.toString(new IP("85.193.148.81")));
    }

    @Test
    public void toStringExample1TestDifferentNode() throws ParseException {
        Network network = new Network("(85.193.148.81 141.255.1.133 34.49.145.239 231.189.0.127)");
        assertEquals("(34.49.145.239 (85.193.148.81 141.255.1.133 231.189.0.127))", network.toString(new IP("34.49.145.239")));
    }

    @Test
    public void toStringExample2TestDifferentNode() throws ParseException {
        Network network = new Network("(141.255.1.133 122.117.67.158 0.146.197.108)");

        assertEquals("(122.117.67.158 (141.255.1.133 0.146.197.108))", network.toString(new IP("122.117.67.158")));
    }

    @Test
    public void toStringExample3TestDifferentNode() throws ParseException {
        Network network = new Network("(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77)");

        assertEquals("(39.20.222.120 (231.189.0.127 77.135.84.171 116.132.83.77 252.29.23.0))", network.toString(new IP("39.20.222.120")));
    }

    @Test
    public void toStringExample4TestDifferentNode() throws ParseException {
        Network network = new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))");

        assertEquals("(77.135.84.171 (231.189.0.127 39.20.222.120 (85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158)) 116.132.83.77 252.29.23.0))", network.toString(new IP("77.135.84.171")));
    }


    @Test
    public void addSimpleTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2)");
        Network network2 = new Network("(2.2.2.2 3.3.3.3)");

        boolean result = network1.add(network2);

        assertTrue(result);

        List<List<IP>> levels = network1.getLevels(new IP("1.1.1.1"));

        List<IP> level1 = levels.get(0);
        List<IP> level2 = levels.get(1);
        List<IP> level3 = levels.get(2);


        assertEquals(2, network1.getHeight(new IP("1.1.1.1")));

        // =====
        assertEquals(1, level1.size());
        assertEquals("1.1.1.1", level1.get(0).toString());

        // =====
        assertEquals(1, level2.size());
        assertEquals("2.2.2.2", level2.get(0).toString());

        // =====
        assertEquals(1, level3.size());
        assertEquals("3.3.3.3", level3.get(0).toString());
    }

    @Test
    public void addSimpleTestDisjointNetworks() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2)");
        Network network2 = new Network("(3.3.3.3 4.4.4.4)");

        boolean result = network1.add(network2);

        assertTrue(result);

        List<IP> ips = network1.list();

        assertEquals(4, ips.size());
        assertEquals("1.1.1.1", ips.get(0).toString());
        assertEquals("2.2.2.2", ips.get(1).toString());
        assertEquals("3.3.3.3", ips.get(2).toString());
        assertEquals("4.4.4.4", ips.get(3).toString());

        assertEquals("(1.1.1.1 2.2.2.2)", network1.toString(new IP("1.1.1.1")));
        assertEquals("(3.3.3.3 4.4.4.4)", network1.toString(new IP("3.3.3.3")));
    }

    @Test
    public void addTestNullNetwork() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result = network.add(null);

        assertFalse(result);

        List<List<IP>> levels = network.getLevels(new IP("1.1.1.1"));
        List<IP> level1 = levels.get(0);
        List<IP> level2 = levels.get(1);

        assertEquals(2, levels.size());

        // ====
        assertEquals(1, level1.size());
        assertEquals("1.1.1.1", level1.get(0).toString());


        // ====
        assertEquals(1, level2.size());
        assertEquals("2.2.2.2", level2.get(0).toString());
    }

    @Test
    public void addNoSideEffectsTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 4.4.4.4))");
        Network network2 = new Network("(4.4.4.4 5.5.5.5 6.6.6.6 7.7.7.7)");

        network1.add(network2);

        // manipulate network2

        network2.disconnect(new IP("4.4.4.4"), new IP("5.5.5.5"));
        network2.add(new Network("(8.8.8.8 9.9.9.9)"));
        network2.connect(new IP("1.1.1.1"), new IP("8.8.8.8"));

        List<IP> ips = network1.list();

        assertTrue(ips.contains(new IP("5.5.5.5")));
        assertFalse(ips.contains(new IP("8.8.8.8")));
        assertFalse(ips.contains(new IP("9.9.9.9")));
    }

    @Test
    public void addFailingTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2)");
        Network network2 = new Network("(1.1.1.1 (3.3.3.3 2.2.2.2))");

        boolean result = network1.add(network2);

        assertFalse(result);

        // check for side effects

        // ====
        List<List<IP>> network1Levels = network1.getLevels(new IP("1.1.1.1"));
        List<List<IP>> network2Levels = network2.getLevels(new IP("1.1.1.1"));


        // === LEVEL 1 ====
        assertEquals(2, network1Levels.size());

        List<IP> n1Level1 = network1Levels.get(0);
        List<IP> n1Level2 = network1Levels.get(1);

        assertEquals(1, n1Level1.size());
        assertEquals("1.1.1.1", n1Level1.get(0).toString());

        assertEquals(1, n1Level2.size());
        assertEquals("2.2.2.2", n1Level2.get(0).toString());

        // === LEVEL 2 ====
        assertEquals(3, network2Levels.size());

        List<IP> n2Level1 = network2Levels.get(0);
        List<IP> n2Level2 = network2Levels.get(1);
        List<IP> n2Level3 = network2Levels.get(2);

        assertEquals(1, n2Level1.size());
        assertEquals("1.1.1.1", n2Level1.get(0).toString());

        assertEquals(1, n2Level2.size());
        assertEquals("3.3.3.3", n2Level2.get(0).toString());

        assertEquals(1, n2Level3.size());
        assertEquals("2.2.2.2", n2Level3.get(0).toString());
    }

    @Test
    public void addMultipleDifferentTopologiesTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");
        Network network1B = new Network("(4.4.4.4 (5.5.5.5 (6.6.6.6 7.7.7.7)))");
        Network network1C = new Network("(8.8.8.8 9.9.9.9)");
        Network network1D = new Network("(11.11.11.11 12.12.12.12)");

        network1.add(network1B);
        network1.add(network1C);
        network1.add(network1D);

        // ======

        Network network2 = new Network("(1.1.1.1 (4.4.4.4 8.8.8.8))");
        Network network2B = new Network("(9.9.9.9 10.10.10.10)");

        network2.add(network2B);

        // ========

        boolean result = network1.add(network2);

        assertTrue(result);

        List<List<IP>> levels = network1.getLevels(new IP("1.1.1.1"));

        assertEquals(5, levels.size());

        List<IP> level1 = levels.get(0);
        List<IP> level2 = levels.get(1);
        List<IP> level3 = levels.get(2);
        List<IP> level4 = levels.get(3);
        List<IP> level5 = levels.get(4);

        // LEVEL 1
        assertEquals(1, level1.size());
        assertEquals("1.1.1.1", level1.get(0).toString());

        // LEVEL 2
        assertEquals(3, level2.size());
        assertEquals("2.2.2.2", level2.get(0).toString());
        assertEquals("3.3.3.3", level2.get(1).toString());
        assertEquals("4.4.4.4", level2.get(2).toString());

        // LEVEL 3
        assertEquals(2, level3.size());
        assertEquals("5.5.5.5", level3.get(0).toString());
        assertEquals("8.8.8.8", level3.get(1).toString());

        // LEVEL 4
        assertEquals(2, level4.size());
        assertEquals("6.6.6.6", level4.get(0).toString());
        assertEquals("9.9.9.9", level4.get(1).toString());

        // LEVEL 5 - this reminds me of a puzzle I once heard
        assertEquals(2, level5.size());
        assertEquals("7.7.7.7", level5.get(0).toString());
        assertEquals("10.10.10.10", level5.get(1).toString());

        // SECOND TOPOLOGY
        List<List<IP>> levels2 = network1.getLevels(new IP("11.11.11.11"));

        assertEquals(2, levels2.size());

        List<IP> level21 = levels2.get(0);
        List<IP> level22 = levels2.get(1);

        assertEquals(1, level21.size());
        assertEquals("11.11.11.11", level21.get(0).toString());
        assertEquals("12.12.12.12", level22.get(0).toString());
    }

    @Test
    public void addWontChangeTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");
        Network network2 = new Network("(1.1.1.1 2.2.2.2)");

        boolean result = network1.add(network2);

        assertFalse(result);
    }

    @Test
    public void addComplexFailingTest() throws ParseException {
        Network network1 = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");
        Network network1B = new Network("(4.4.4.4 (5.5.5.5 (6.6.6.6 7.7.7.7)))");
        Network network1C = new Network("(8.8.8.8 9.9.9.9)");
        Network network1D = new Network("(11.11.11.11 12.12.12.12)");

        network1.add(network1B);
        network1.add(network1C);
        network1.add(network1D);

        // ======

        Network network2 = new Network("(1.1.1.1 (4.4.4.4 8.8.8.8))");
        Network network2B = new Network("(2.2.2.2 3.3.3.3)");

        network2.add(network2B);

        // ========

        boolean result = network1.add(network2);

        assertFalse(result);

        // check for side effects

        assertEquals("(1.1.1.1 2.2.2.2 3.3.3.3)", network1.toString(new IP("1.1.1.1")));
        assertEquals("(4.4.4.4 (5.5.5.5 (6.6.6.6 7.7.7.7)))", network1.toString(new IP("4.4.4.4")));
        assertEquals("(8.8.8.8 9.9.9.9)", network1.toString(new IP("8.8.8.8")));
        assertEquals("(11.11.11.11 12.12.12.12)", network1.toString(new IP("11.11.11.11")));

        assertEquals("(1.1.1.1 (4.4.4.4 8.8.8.8))", network2.toString(new IP("1.1.1.1")));
        assertEquals("(2.2.2.2 3.3.3.3)", network2.toString(new IP("2.2.2.2")));
    }

    @Test
    public void connectSimpleTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");
        network.add(new Network("(4.4.4.4 5.5.5.5 6.6.6.6)"));

        network.connect(new IP("3.3.3.3"), new IP("4.4.4.4"));

        List<List<IP>> levels = network.getLevels(new IP("1.1.1.1"));

        assertEquals(4, levels.size());

        List<IP> level1 = levels.get(0);
        List<IP> level2 = levels.get(1);
        List<IP> level3 = levels.get(2);
        List<IP> level4 = levels.get(3);

        // LEVEL 1
        assertEquals(1, level1.size());
        assertEquals("1.1.1.1", level1.get(0).toString());

        // LEVEL 2
        assertEquals(2, level2.size());
        assertEquals("2.2.2.2", level2.get(0).toString());
        assertEquals("3.3.3.3", level2.get(1).toString());

        // LEVEL 3
        assertEquals(1, level3.size());
        assertEquals("4.4.4.4", level3.get(0).toString());

        // LEVEL 4
        assertEquals(2, level4.size());
        assertEquals("5.5.5.5", level4.get(0).toString());
        assertEquals("6.6.6.6", level4.get(1).toString());
    }

    @Test
    public void connectNUllTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result1 = network.connect(null, new IP("1.1.1.1"));
        assertFalse(result1);

        boolean result2 = network.connect(new IP("1.1.1.1"), null);
        assertFalse(result2);
    }

    @Test
    public void connectSameNode() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result = network.connect(new IP("1.1.1.1"), new IP("1.1.1.1"));

        assertFalse(result);
    }

    @Test
    public void connectNonExistingNode() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result1 = network.connect(new IP("1.1.1.1"), new IP("0.0.0.0"));
        boolean result2 = network.connect(new IP("0.0.0.0"), new IP("1.1.1.1"));

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    public void connectAlreadyConnectedNode() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result = network.connect(new IP("1.1.1.1"), new IP("2.2.2.2"));

        assertFalse(result);
    }

    @Test
    public void connectCircularTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");

        boolean result = network.connect(new IP("2.2.2.2"), new IP("3.3.3.3"));

        assertFalse(result);

        List<List<IP>> levels = network.getLevels(new IP("2.2.2.2"));

        assertEquals(3, levels.size());

        List<IP> level1 = levels.get(0);
        List<IP> level2 = levels.get(1);
        List<IP> level3 = levels.get(2);

        // LEVEL 1
        assertEquals(1, level1.size());
        assertEquals("2.2.2.2", level1.get(0).toString());

        // LEVEL 2
        assertEquals(1, level2.size());
        assertEquals("1.1.1.1", level2.get(0).toString());

        // LEVEL 3
        assertEquals(1, level3.size());
        assertEquals("3.3.3.3", level3.get(0).toString());
    }

    @Test
    public void disconnectSimpleTest() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))");

        boolean result = network.disconnect(new IP("1.1.1.1"), new IP("3.3.3.3"));

        assertTrue(result);

        List<List<IP>> levelsA = network.getLevels(new IP("1.1.1.1"));
        List<List<IP>> levelsB = network.getLevels(new IP("3.3.3.3"));

        // LEVELS A
        assertEquals(2, levelsA.size());
        List<IP> levelA1 = levelsA.get(0);
        List<IP> levelA2 = levelsA.get(1);

        assertEquals(1, levelA1.size());

        assertEquals("1.1.1.1", levelA1.get(0).toString());

        assertEquals(1, levelA2.size());
        assertEquals("2.2.2.2", levelA2.get(0).toString());


        // LEVELS B
        assertEquals(3, levelsB.size());
        List<IP> levelB1 = levelsB.get(0);
        List<IP> levelB2 = levelsB.get(1);
        List<IP> levelB3 = levelsB.get(2);

        assertEquals(1, levelB1.size());
        assertEquals(1, levelB2.size());
        assertEquals(1, levelB3.size());

        // ======

        assertEquals(1, levelB1.size());

        assertEquals("3.3.3.3", levelB1.get(0).toString());

        // ======

        assertEquals(1, levelB2.size());

        assertEquals("4.4.4.4", levelB2.get(0).toString());

        // ======

        assertEquals(1, levelB3.size());

        assertEquals("5.5.5.5", levelB3.get(0).toString());
    }

    @Test
    public void disconnectNullIps() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))");

        boolean result1 = network.disconnect(null, new IP("4.4.4.4"));

        assertFalse(result1);

        boolean result2 = network.disconnect(new IP("3.3.3.3"), null);

        assertFalse(result2);

        assertEquals("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void disconnectNonExistingIps() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))");

        boolean result1 = network.disconnect(new IP("0.0.0.0"), new IP("4.4.4.4"));

        assertFalse(result1);

        boolean result2 = network.disconnect(new IP("3.3.3.3"), new IP("0.0.0.0"));

        assertFalse(result2);

        assertEquals("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void disconnectNonDirectIps() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))");

        boolean result1 = network.disconnect(new IP("1.1.1.1"), new IP("4.4.4.4"));

        assertFalse(result1);

        assertEquals("(1.1.1.1 2.2.2.2 (3.3.3.3 (4.4.4.4 5.5.5.5)))", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void disconnectIpsInDifferentTopologies() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 (3.3.3.3 4.4.4.4))");

        network.add(new Network("(5.5.5.5 6.6.6.6)"));

        boolean result1 = network.disconnect(new IP("3.3.3.3"), new IP("5.5.5.5"));

        assertFalse(result1);

        assertEquals("(1.1.1.1 2.2.2.2 (3.3.3.3 4.4.4.4))", network.toString(new IP("1.1.1.1")));
        assertEquals("(5.5.5.5 6.6.6.6)", network.toString(new IP("5.5.5.5")));
    }

    @Test
    public void denyDeletingLastConnection() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");

        boolean result = network.disconnect(new IP("1.1.1.1"), new IP("2.2.2.2"));

        assertFalse(result);

        assertEquals("(1.1.1.1 2.2.2.2)", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void disconnectTestRemoveNode() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2 3.3.3.3)");

        network.disconnect(new IP("1.1.1.1"), new IP("3.3.3.3"));

        List<IP> ips = network.list();

        assertTrue(ips.contains(new IP("1.1.1.1")));
        assertTrue(ips.contains(new IP("2.2.2.2")));

        assertFalse(ips.contains(new IP("3.3.3.3")));
    }

    @Test
    public void allowDeletingLastConnectionBecauseMultipleTopologies() throws ParseException {
        Network network = new Network("(1.1.1.1 2.2.2.2)");
        network.add(new Network("(3.3.3.3 4.4.4.4)"));

        boolean result = network.disconnect(new IP("1.1.1.1"), new IP("2.2.2.2"));

        assertTrue(result);

        assertEquals("(3.3.3.3 4.4.4.4)", network.toString(new IP("3.3.3.3")));

        List<IP> ips = network.list();

        assertFalse(ips.contains(new IP("1.1.1.1")));
        assertFalse(ips.contains(new IP("2.2.2.2")));
    }

    @Test
    public void complexDisconnectTest() throws ParseException {
        Network network = new Network("(1.1.1.1 (2.2.2.2 (4.4.4.4 (11.11.11.11 (13.13.13.13 15.15.15.15))) (5.5.5.5 12.12.12.12) 6.6.6.6) (3.3.3.3 (7.7.7.7 8.8.8.8 9.9.9.9 (10.10.10.10 14.14.14.14))))");

        boolean result = network.disconnect(new IP("2.2.2.2"), new IP("4.4.4.4"));
        assertTrue(result);

        System.out.println(network.toString(new IP("1.1.1.1")));
        System.out.println(network.toString(new IP("4.4.4.4")));

        assertEquals("(4.4.4.4 (11.11.11.11 (13.13.13.13 15.15.15.15)))", network.toString(new IP("4.4.4.4")));
        assertEquals("(1.1.1.1 (2.2.2.2 (5.5.5.5 12.12.12.12) 6.6.6.6) (3.3.3.3 (7.7.7.7 8.8.8.8 9.9.9.9 (10.10.10.10 14.14.14.14))))", network.toString(new IP("1.1.1.1")));
    }

    @Test
    public void complexDisconnectTestRemoveNode() throws ParseException {
        Network network = new Network("(1.1.1.1 (2.2.2.2 (4.4.4.4 (11.11.11.11 (13.13.13.13 15.15.15.15))) (5.5.5.5 12.12.12.12) 6.6.6.6) (3.3.3.3 (7.7.7.7 8.8.8.8 9.9.9.9 (10.10.10.10 14.14.14.14))))");

        boolean result = network.disconnect(new IP("10.10.10.10"), new IP("14.14.14.14"));
        assertTrue(result);

        assertEquals("(1.1.1.1 (2.2.2.2 (4.4.4.4 (11.11.11.11 (13.13.13.13 15.15.15.15))) (5.5.5.5 12.12.12.12) 6.6.6.6) (3.3.3.3 (7.7.7.7 8.8.8.8 9.9.9.9 10.10.10.10)))", network.toString(new IP("1.1.1.1")));
        assertFalse(network.list().contains(new IP("14.14.14.14")));
    }
}
