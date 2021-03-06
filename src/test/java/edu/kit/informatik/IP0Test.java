package edu.kit.informatik;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class IP0Test {

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0.0", "1.2.3.4", "99.99.99.99", "199.199.199.199", "34.49.145.249", "252.29.23.0", "116.132.83.77", "255.255.255.255","78.203.152.185","78.203.0.0"})
    void testValid(String input) throws ParseException {
        new IP(input);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ","03.0.0.0", "1.02.3.4", "99.99.099.99", "199.199.199.099", "256.255.255.255"})
    void testNotValid(String input) throws ParseException {
        assertThrows(ParseException.class, ()->{new IP(input);});
    }

    @Test
    void testPair(){
        boolean equalPair = Set.of(new IP("0.0.0.0"), new IP("0.0.0.1")).equals(Set.of(new IP("0.0.0.1"), new IP("0.0.0.0")));
        System.out.println(equalPair);
    }
}
