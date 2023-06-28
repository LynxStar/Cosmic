package net.server.coordinator.session;

import client.Client;
import config.YamlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IpAddresses {

    private static final Logger log = LoggerFactory.getLogger(IpAddresses.class);

    private static final List<Pattern> LOCAL_ADDRESS_PATTERNS = loadLocalAddressPatterns();

    private static List<Pattern> loadLocalAddressPatterns() {
        return Stream.of("10\\.", "192\\.168\\.", "172\\.(1[6-9]|2[0-9]|3[0-1])\\.")
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    public static String evaluateRemoteAddress(String inetAddress) {

        var result = isLocalAddress(inetAddress) || isLanAddress(inetAddress)
                ? inetAddress
                : YamlConfig.config.server.HOST
                ;

        log.debug("Evaluating Remote Address: {} and got result {}", inetAddress, result);

        return result;
    }

    public static boolean isLocalAddress(String inetAddress) {
        return inetAddress.startsWith("127.");
    }

    public static boolean isLanAddress(String inetAddress) {
        return LOCAL_ADDRESS_PATTERNS.stream()
                .anyMatch(pattern -> matchesPattern(pattern, inetAddress));
    }

    private static boolean matchesPattern(Pattern pattern, String searchTerm) {
        return pattern.matcher(searchTerm).find();
    }
}
