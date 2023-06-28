package net.server.coordinator.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IpAddressesTest {

    @Test
    void patternTest() {

        var result = IpAddresses.isLanAddress("192.168.1.7");

        assertTrue(result);

    }

}
