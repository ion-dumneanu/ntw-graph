package edu.kit.informatik;

import org.junit.jupiter.api.Test;

import static edu.kit.informatik.util.KoeriTestUtils.ip;
import static edu.kit.informatik.util.KoeriTestUtils.network;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkAddThreeTest {
    // This test adds 2 independent trees to a network with 1 existing trees
    // The 2 trees have to be added to the same tree.
    @Test
    public void testAddThree() {
        Network net1 = network("(1.1.1.1 2.2.2.2 3.3.3.3)");
        Network net2 = network("(3.3.3.3 4.4.4.4 5.5.5.5)");
        Network net3 = network("(2.2.2.2 6.6.6.6 7.7.7.7)");
        net2.add(net3);
        net1.add(net2);
        assertEquals(net1.toString(ip("1.1.1.1")), "(1.1.1.1 (2.2.2.2 6.6.6.6 7.7.7.7) (3.3.3.3 4.4.4.4 5.5.5.5))");
    }
}
