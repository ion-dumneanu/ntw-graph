package edu.kit.informatik;

import edu.kit.informatik.util.LinesSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static edu.kit.informatik.util.KoeriTestUtils.network;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NetworkConstructorParseTest {
    @ParameterizedTest
    @NullAndEmptySource
    @LinesSource("network/invalid")
    void testInvalidArgs(String bracketNotation) {
        assertThrows(ParseException.class, () -> new Network(bracketNotation));
    }

    @ParameterizedTest
    @LinesSource("network/valid")
    void testValidArgs(String bracketNotation) {
        network(bracketNotation);
    }
}
